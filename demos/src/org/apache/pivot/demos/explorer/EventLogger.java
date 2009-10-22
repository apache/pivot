/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.demos.explorer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Comparator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Group;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.HashSet;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.immutable.ImmutableList;
import org.apache.pivot.collections.immutable.ImmutableSet;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.ThreadUtilities;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;

public class EventLogger extends Container {
    private static class EventComparator implements Comparator<Method> {
        @Override
        public int compare(Method event1, Method event2) {
            int result = 0;

            Class<?> listenerInterface1 = event1.getDeclaringClass();
            Class<?> listenerInterface2 = event2.getDeclaringClass();

            if (listenerInterface1 != listenerInterface2) {
                result = listenerInterface1.getName().compareTo(listenerInterface2.getName());
            }

            if (result == 0) {
                result = event1.getName().compareTo(event2.getName());
            }

            return result;
        }
    }

    private class LoggerInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method event, Object[] arguments) throws Throwable {
            if (includeEvents.contains(event)) {
                eventLoggerListeners.eventFired(EventLogger.this, event, arguments);
            }

            Object result = null;
            Class<?> returnType = event.getReturnType();
            if (returnType == Vote.class) {
                result = Vote.APPROVE;
            } else if (returnType == Boolean.TYPE) {
                result = false;
            }

            return result;
        }
    }

    private static class EventLoggerListenerList extends ListenerList<EventLoggerListener>
        implements EventLoggerListener {
        @Override
        public void sourceChanged(EventLogger eventLogger, Component previousSource) {
            for (EventLoggerListener listener : this) {
                listener.sourceChanged(eventLogger, previousSource);
            }
        }

        @Override
        public void eventIncluded(EventLogger eventLogger, Method event) {
            for (EventLoggerListener listener : this) {
                listener.eventIncluded(eventLogger, event);
            }
        }

        @Override
        public void eventExcluded(EventLogger eventLogger, Method event) {
            for (EventLoggerListener listener : this) {
                listener.eventExcluded(eventLogger, event);
            }
        }

        @Override
        public void eventFired(EventLogger eventLogger, Method event, Object[] arguments) {
            for (EventLoggerListener listener : this) {
                listener.eventFired(eventLogger, event, arguments);
            }
        }
    }

    private Component source = null;

    private HashMap<Class<?>, Object> eventListenerProxies = new HashMap<Class<?>, Object>();
    private LoggerInvocationHandler loggerInvocationHandler = new LoggerInvocationHandler();

    private ArrayList<Method> declaredEvents = new ArrayList<Method>(new EventComparator());

    private HashSet<Method> includeEvents = new HashSet<Method>();

    private EventLoggerListenerList eventLoggerListeners = new EventLoggerListenerList();

    public EventLogger() {
        this(null);
    }

    public EventLogger(Component source) {
        setSource(source);
        setSkin(new EventLoggerSkin());
    }

    public Component getSource() {
        return source;
    }

    public void setSource(Component source) {
        Component previousSource = this.source;

        if (source != previousSource) {
            this.source = source;

            if (previousSource != null) {
                unregisterEventListeners(previousSource);
            }

            if (source != null) {
                registerEventListeners(source);
            }

            eventLoggerListeners.sourceChanged(this, previousSource);
        }
    }

    public Sequence<Method> getDeclaredEvents() {
        return new ImmutableList<Method>(declaredEvents);
    }

    public Group<Method> getIncludeEvents() {
        return new ImmutableSet<Method>(includeEvents);
    }

    public void includeEvent(Method event) {
        if (!includeEvents.contains(event)) {
            includeEvents.add(event);
            eventLoggerListeners.eventIncluded(this, event);
        }
    }

    public void excludeEvent(Method event) {
        if (includeEvents.contains(event)) {
            includeEvents.remove(event);
            eventLoggerListeners.eventExcluded(this, event);
        }
    }

    public boolean isEventIncluded(Method event) {
        return includeEvents.contains(event);
    }

    private void registerEventListeners(Component source) {
        declaredEvents.clear();

        Method[] methods = source.getClass().getMethods();

        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];

            if (ListenerList.class.isAssignableFrom(method.getReturnType())
                && (method.getModifiers() & Modifier.STATIC) == 0) {
                ParameterizedType genericType = (ParameterizedType)method.getGenericReturnType();
                Type[] typeArguments = genericType.getActualTypeArguments();

                if (typeArguments.length == 1) {
                    Class<?> listenerInterface = (Class<?>)typeArguments[0];

                    if (!listenerInterface.isInterface()) {
                        throw new RuntimeException(listenerInterface.getName()
                            + " is not an interface.");
                    }

                    Method[] interfaceMethods = listenerInterface.getMethods();
                    for (int j = 0; j < interfaceMethods.length; j++) {
                        Method interfaceMethod = interfaceMethods[j];
                        declaredEvents.add(interfaceMethod);
                    }

                    // Get the listener list
                    Object listenerList;
                    try {
                        listenerList = method.invoke(source);
                    } catch (InvocationTargetException exception) {
                        throw new RuntimeException(exception);
                    } catch (IllegalAccessException exception) {
                        throw new RuntimeException(exception);
                    }

                    // Get the listener for this interface
                    Object listener = eventListenerProxies.get(listenerInterface);
                    if (listener == null) {
                        listener = Proxy.newProxyInstance(ThreadUtilities.getClassLoader(),
                            new Class[]{listenerInterface}, loggerInvocationHandler);
                        eventListenerProxies.put(listenerInterface, listener);
                    }

                    // Add the listener
                    Class<?> listenerListClass = listenerList.getClass();
                    Method addMethod;
                    try {
                        addMethod = listenerListClass.getMethod("add",
                            new Class<?>[] {Object.class});
                    } catch (NoSuchMethodException exception) {
                        throw new RuntimeException(exception);
                    }

                    try {
                        addMethod.invoke(listenerList, new Object[] {listener});
                    } catch (IllegalAccessException exception) {
                        throw new RuntimeException(exception);
                    } catch (InvocationTargetException exception) {
                        throw new RuntimeException(exception);
                    }
                }
            }
        }
    }

    private void unregisterEventListeners(Component source) {
        declaredEvents.clear();

        // TODO
    }

    public ListenerList<EventLoggerListener> getEventLoggerListeners() {
        return eventLoggerListeners;
    }
}
