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
package org.apache.pivot.tools.wtk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Group;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.HashSet;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.immutable.ImmutableList;
import org.apache.pivot.collections.immutable.ImmutableSet;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.ThreadUtilities;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;

/**
 *
 */
public class EventLogger extends Container {
    /**
     * Declared event sequence.
     */
    public final class DeclaredEventSequence implements Sequence<Method>, Iterable<Method> {
        private DeclaredEventSequence() {
        }

        @Override
        public int add(Method event) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insert(Method event, int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Method update(int index, Method event) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(Method event) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Sequence<Method> remove(int index, int count) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Method get(int index) {
            return declaredEvents.get(index);
        }

        @Override
        public int indexOf(Method event) {
            return declaredEvents.indexOf(event);
        }

        @Override
        public int getLength() {
            return declaredEvents.getLength();
        }

        @Override
        public Iterator<Method> iterator() {
            return new ImmutableIterator<Method>(declaredEvents.iterator());
        }
    }

    /**
     * Include events group.
     */
    public final class IncludeEventGroup implements Group<Method>, Iterable<Method> {
        private IncludeEventGroup() {
        }

        @Override
        public boolean add(Method event) {
            boolean added = false;

            if (!includeEvents.contains(event)) {
                includeEvents.add(event);
                eventLoggerListeners.eventIncluded(EventLogger.this, event);
                added = true;
            }

            return added;
        }

        @Override
        public boolean remove(Method event) {
            boolean removed = false;

            if (includeEvents.contains(event)) {
                includeEvents.remove(event);
                eventLoggerListeners.eventExcluded(EventLogger.this, event);
                removed = true;
            }

            return removed;
        }

        @Override
        public boolean contains(Method event) {
            return includeEvents.contains(event);
        }

        @Override
        public boolean isEmpty() {
            return includeEvents.isEmpty();
        }

        @Override
        public Iterator<Method> iterator() {
            return new ImmutableIterator<Method>(includeEvents.iterator());
        }
    }

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
    private DeclaredEventSequence declaredEventSequence = new DeclaredEventSequence();

    private HashSet<Method> includeEvents = new HashSet<Method>();
    private IncludeEventGroup includeEventGroup = new IncludeEventGroup();

    private EventLoggerListenerList eventLoggerListeners = new EventLoggerListenerList();

    /**
     * Creates a new event logger that is not tied to any source component.
     */
    public EventLogger() {
        this(null);
    }

    /**
     * Creates a new event logger that will log events on the specified source.
     */
    public EventLogger(Component source) {
        setSource(source);
        setSkin(new EventLoggerSkin());
    }

    /**
     * Gets this event logger's source component.
     *
     * @return
     * The source component, or <tt>null</tt> if no source has been set.
     */
    public Component getSource() {
        return source;
    }

    /**
     * Sets this event logger's source component.
     *
     * @param source
     * The source component, or <tt>null</tt> to clear the source.
     */
    public void setSource(Component source) {
        Component previousSource = this.source;

        if (source != previousSource) {
            if (previousSource != null) {
                unregisterEventListeners();
            }

            this.source = source;

            declaredEvents.clear();
            includeEvents.clear();

            if (source != null) {
                registerEventListeners();
            }

            eventLoggerListeners.sourceChanged(this, previousSource);
        }
    }

    /**
     * Gets the declared events sequence, a read-only sequence that includes
     * the complete list of events that this event logger's source declares.
     *
     * @return
     * the declared events sequence.
     */
    public DeclaredEventSequence getDeclaredEvents() {
        return declaredEventSequence;
    }

    /**
     * Gets the include events group, which callers can use to include or
     * exclude declared events from those that get fired by this logger.
     *
     * @return
     * The include events group.
     */
    public IncludeEventGroup getIncludeEvents() {
        return includeEventGroup;
    }

    private void registerEventListeners() {
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

    private void unregisterEventListeners() {
        Method[] methods = source.getClass().getMethods();

        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];

            if (ListenerList.class.isAssignableFrom(method.getReturnType())
                && (method.getModifiers() & Modifier.STATIC) == 0) {
                ParameterizedType genericType = (ParameterizedType)method.getGenericReturnType();
                Type[] typeArguments = genericType.getActualTypeArguments();

                if (typeArguments.length == 1) {
                    Class<?> listenerInterface = (Class<?>)typeArguments[0];

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

                    // Remove the listener
                    Class<?> listenerListClass = listenerList.getClass();
                    Method removeMethod;
                    try {
                        removeMethod = listenerListClass.getMethod("remove",
                            new Class<?>[] {Object.class});
                    } catch (NoSuchMethodException exception) {
                        throw new RuntimeException(exception);
                    }

                    try {
                        removeMethod.invoke(listenerList, new Object[] {listener});
                    } catch (IllegalAccessException exception) {
                        throw new RuntimeException(exception);
                    } catch (InvocationTargetException exception) {
                        throw new RuntimeException(exception);
                    }
                }
            }
        }
    }

    /**
     * Gets the event logger listener list.
     */
    public ListenerList<EventLoggerListener> getEventLoggerListeners() {
        return eventLoggerListeners;
    }
}
