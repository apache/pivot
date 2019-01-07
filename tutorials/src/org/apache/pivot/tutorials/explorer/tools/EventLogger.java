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
package org.apache.pivot.tutorials.explorer.tools;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Iterator;

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.collections.Group;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.HashSet;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;

/**
 * A component that monitors a source component for events.
 */
public class EventLogger extends Container {
    /**
     * Event logger skin interface. Event logger skins must implement this.
     */
    public interface Skin {
        /**
         * Clears the event log.
         */
        public void clearLog();

        /**
         * Select/Deselect all Events to log.
         *
         * @param select if true, all events will be selected for the log,
         * otherwise all events will be deselected
         */
        public void selectAllEvents(boolean select);
    }

    /**
     * A read-only group of events that an event logger is capable of firing. To
     * make an event logger actually fire declared events, callers add them to
     * the event logger's include event group.
     */
    public final class DeclaredEventGroup implements Group<Method>, Iterable<Method> {
        private DeclaredEventGroup() {
        }

        @Override
        @UnsupportedOperation
        public boolean add(Method event) {
            throw new UnsupportedOperationException();
        }

        @Override
        @UnsupportedOperation
        public boolean remove(Method event) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Method event) {
            return declaredEvents.contains(event);
        }

        @Override
        public Iterator<Method> iterator() {
            return new ImmutableIterator<>(declaredEvents.iterator());
        }
    }

    /**
     * A read/write group of events that an event logger will actually fire.
     * This group is guaranteed to be a subset of the declared event group.
     */
    public final class IncludeEventGroup implements Group<Method>, Iterable<Method> {
        private IncludeEventGroup() {
        }

        @Override
        public boolean add(Method event) {
            boolean added = false;

            if (!declaredEvents.contains(event)) {
                throw new IllegalArgumentException("Event has not been declared.");
            }

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
        public Iterator<Method> iterator() {
            return new ImmutableIterator<>(includeEvents.iterator());
        }
    }

    /**
     * Event logger invocation handler.
     */
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

    private Component source = null;

    private HashMap<Class<?>, Object> eventListenerProxies = new HashMap<>();
    private LoggerInvocationHandler loggerInvocationHandler = new LoggerInvocationHandler();

    private HashSet<Method> declaredEvents = new HashSet<>();
    private DeclaredEventGroup declaredEventGroup = new DeclaredEventGroup();

    private HashSet<Method> includeEvents = new HashSet<>();
    private IncludeEventGroup includeEventGroup = new IncludeEventGroup();

    private EventLoggerListener.Listeners eventLoggerListeners = new EventLoggerListener.Listeners();

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
     * @return The source component, or <tt>null</tt> if no source has been set.
     */
    public Component getSource() {
        return source;
    }

    /**
     * Sets this event logger's source component.
     *
     * @param source The source component, or <tt>null</tt> to clear the source.
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
     * Gets the declared event group, a read-only group that includes the
     * complete list of events that this event logger's source declares.
     *
     * @return the declared events group.
     */
    public DeclaredEventGroup getDeclaredEvents() {
        return declaredEventGroup;
    }

    /**
     * Gets the include events group, which callers can use to include or
     * exclude declared events from those that get fired by this logger. This
     * group is guaranteed to be a subset of the declared event group (attempts
     * to add events to this group that are not included in the declared event
     * group will fail).
     *
     * @return The include events group.
     */
    public IncludeEventGroup getIncludeEvents() {
        return includeEventGroup;
    }

    /**
     * Clears the event log.
     */
    public void clearLog() {
        EventLogger.Skin eventLoggerSkin = (EventLogger.Skin) getSkin();
        eventLoggerSkin.clearLog();
    }

    /**
     * Select/Deselect all Events to log.
     *
     * @param select if true, all events will be selected for the log, otherwise
     * all events will be deselected
     */
    public void selectAllEvents(boolean select) {
        // Include or exclude each possible method from the group of monitored
        // events
        IncludeEventGroup includeEventsLocal = getIncludeEvents();
        for (Method event : declaredEvents) {
            if (select) {
                includeEventsLocal.add(event);
            } else {
                includeEventsLocal.remove(event);
            }
        }
        // Update the skin (Checkboxes)
        EventLogger.Skin eventLoggerSkin = (EventLogger.Skin) getSkin();
        eventLoggerSkin.selectAllEvents(select);
    }

    /**
     * Registers event listeners on this event logger's source.
     */
    private void registerEventListeners() {
        Method[] methods = source.getClass().getMethods();

        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];

            if (ListenerList.class.isAssignableFrom(method.getReturnType())
                && (method.getModifiers() & Modifier.STATIC) == 0) {
                ParameterizedType genericType = (ParameterizedType) method.getGenericReturnType();
                Type[] typeArguments = genericType.getActualTypeArguments();

                if (typeArguments.length == 1) {
                    Type type = typeArguments[0];
                    Class<?> listenerInterface;
                    if (type instanceof ParameterizedType) {
                        ParameterizedType paramType = (ParameterizedType) type;
                        listenerInterface = (Class<?>) paramType.getRawType();
                    } else {
                        listenerInterface = (Class<?>) type;
                    }

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
                        listener = Proxy.newProxyInstance(
                            Thread.currentThread().getContextClassLoader(),
                            new Class<?>[] {listenerInterface}, loggerInvocationHandler);
                        eventListenerProxies.put(listenerInterface, listener);
                    }

                    // Add the listener
                    Class<?> listenerListClass = listenerList.getClass();
                    Method addMethod;
                    try {
                        addMethod = listenerListClass.getMethod("add", Object.class);
                    } catch (NoSuchMethodException exception) {
                        throw new RuntimeException(exception);
                    }

                    try {
                        addMethod.invoke(listenerList, listener);
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
     * Unregisters event listeners on this event logger's source.
     */
    private void unregisterEventListeners() {
        Method[] methods = source.getClass().getMethods();

        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];

            if (ListenerList.class.isAssignableFrom(method.getReturnType())
                && (method.getModifiers() & Modifier.STATIC) == 0) {
                ParameterizedType genericType = (ParameterizedType) method.getGenericReturnType();
                Type[] typeArguments = genericType.getActualTypeArguments();

                if (typeArguments.length == 1) {
                    Type type = typeArguments[0];
                    Class<?> listenerInterface;
                    if (type instanceof ParameterizedType) {
                        ParameterizedType paramType = (ParameterizedType) type;
                        listenerInterface = (Class<?>) paramType.getRawType();
                    } else {
                        listenerInterface = (Class<?>) type;
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

                    // Remove the listener
                    Class<?> listenerListClass = listenerList.getClass();
                    Method removeMethod;
                    try {
                        removeMethod = listenerListClass.getMethod("remove", Object.class);
                    } catch (NoSuchMethodException exception) {
                        throw new RuntimeException(exception);
                    }

                    try {
                        removeMethod.invoke(listenerList, listener);
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
