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
package org.apache.pivot.beans;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Comparator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.immutable.ImmutableList;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.ThreadUtilities;
import org.apache.pivot.util.Vote;

/**
 * Notifies listeners of events fired from a source bean.
 */
public class BeanMonitor {
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

    private static class PropertyNameComparator implements Comparator<String> {
        @Override
        public int compare(String propertyName1, String propertyName2) {
            return propertyName1.compareTo(propertyName2);
        }
    }

    private class MonitorInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method event, Object[] arguments) throws Throwable {
            beanMonitorListeners.eventFired(BeanMonitor.this, event, arguments);

            String eventName = event.getName();
            if (eventName.endsWith(PROPERTY_CHANGE_SUFFIX)) {
                String propertyName = eventName.substring(0, eventName.length()
                    - PROPERTY_CHANGE_SUFFIX.length());

                if (notifyingProperties.indexOf(propertyName) >= 0) {
                    beanMonitorListeners.propertyChanged(BeanMonitor.this, propertyName);
                }
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

    private static class BeanMonitorListenerList extends ListenerList<BeanMonitorListener>
        implements BeanMonitorListener {
        @Override
        public void sourceChanged(BeanMonitor beanMonitor, Object previousSource) {
            for (BeanMonitorListener listener : this) {
                listener.sourceChanged(beanMonitor, previousSource);
            }
        }

        @Override
        public void eventFired(BeanMonitor beanMonitor, Method event, Object[] arguments) {
            for (BeanMonitorListener listener : this) {
                listener.eventFired(beanMonitor, event, arguments);
            }
        }

        @Override
        public void propertyChanged(BeanMonitor beanMonitor, String propertyName) {
            for (BeanMonitorListener listener : this) {
                listener.propertyChanged(beanMonitor, propertyName);
            }
        }
    }

    private Object source = null;

    private HashMap<Class<?>, Object> eventListenerProxies = new HashMap<Class<?>, Object>();
    private MonitorInvocationHandler monitorInvocationHandler = new MonitorInvocationHandler();

    private ArrayList<Method> declaredEvents = new ArrayList<Method>(eventComparator);
    private ArrayList<String> notifyingProperties = new ArrayList<String>(propertyNameComparator);

    private BeanMonitorListenerList beanMonitorListeners = new BeanMonitorListenerList();

    private static EventComparator eventComparator = new EventComparator();
    private static PropertyNameComparator propertyNameComparator = new PropertyNameComparator();

    private static final String PROPERTY_CHANGE_SUFFIX = "Changed";

    /**
     * Creates a new bean monitor that is initially associated with no source
     * object.
     */
    public BeanMonitor() {
        this(null);
    }

    /**
     * Creates a new bean monitor that will monitor the specified source
     * object.
     * <p>
     * <b>NOTE</b>: failing to clear the source of a bean monitor may result in
     * memory leaks, as the source object will maintain references to the bean
     * monitor as long as the source is set.
     */
    public BeanMonitor(Object source) {
        setSource(source);
    }

    /**
     * Gets the source of the bean monitor.
     *
     * @return
     * The source object, or <tt>null</tt> if no source has been set.
     */
    public Object getSource() {
        return source;
    }

    /**
     * Sets the source of the bean monitor.
     * <p>
     * <b>NOTE</b>: failing to clear the source of a bean monitor may result in
     * memory leaks, as the source object will maintain references to the bean
     * monitor as long as the source is set.
     *
     * @param source
     * The source object, or <tt>null</tt> to clear the source.
     */
    public void setSource(Object source) {
        Object previousSource = this.source;

        if (source != previousSource) {
            this.source = source;

            if (previousSource != null) {
                unregisterEventListeners(previousSource);
            }

            declaredEvents.clear();
            notifyingProperties.clear();

            if (source != null) {
                registerEventListeners(source);
            }

            beanMonitorListeners.sourceChanged(this, previousSource);
        }
    }

    /**
     * Gets the list of events that the source bean may fire.
     *
     * @return
     * The event listener methods that the source bean may invoke.
     */
    public Sequence<Method> getDeclaredEvents() {
        return new ImmutableList<Method>(declaredEvents);
    }

    /**
     * Gets the list of source bean property names for which property change
     * events will be fired.
     *
     * @return
     * The property names that fire change events.
     */
    public Sequence<String> getNotifyingProperties() {
        return new ImmutableList<String>(notifyingProperties);
    }

    /**
     * Tells whether or not the specified property fires change events.
     *
     * @return
     * <tt>true</tt> if the property fires change events; <tt>false</tt>
     * otherwise.
     */
    public boolean isNotifyingProperty(String propertyName) {
        return (notifyingProperties.indexOf(propertyName) >= 0);
    }

    /**
     * Registers event listeners on a bean.
     */
    private void registerEventListeners(Object bean) {
        BeanDictionary beanDictionary = new BeanDictionary(bean);
        Method[] methods = bean.getClass().getMethods();

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
                        String interfaceMethodName = interfaceMethod.getName();

                        declaredEvents.add(interfaceMethod);

                        if (interfaceMethodName.endsWith(PROPERTY_CHANGE_SUFFIX)) {
                            String propertyName = interfaceMethodName.substring(0,
                                interfaceMethodName.length() - PROPERTY_CHANGE_SUFFIX.length());

                            if (beanDictionary.containsKey(propertyName)) {
                                notifyingProperties.add(propertyName);
                            }
                        }
                    }

                    // Get the listener list
                    Object listenerList;
                    try {
                        listenerList = method.invoke(bean);
                    } catch (InvocationTargetException exception) {
                        throw new RuntimeException(exception);
                    } catch (IllegalAccessException exception) {
                        throw new RuntimeException(exception);
                    }

                    // Get the listener for this interface
                    Object listener = eventListenerProxies.get(listenerInterface);
                    if (listener == null) {
                        listener = Proxy.newProxyInstance(ThreadUtilities.getClassLoader(),
                            new Class[]{listenerInterface}, monitorInvocationHandler);
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

    /**
     * Un-registers event listeners on a bean.
     */
    private void unregisterEventListeners(Object bean) {
        Method[] methods = bean.getClass().getMethods();

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
                        listenerList = method.invoke(bean);
                    } catch (InvocationTargetException exception) {
                        throw new RuntimeException(exception);
                    } catch (IllegalAccessException exception) {
                        throw new RuntimeException(exception);
                    }

                    // Get the listener for this interface
                    Object listener = eventListenerProxies.get(listenerInterface);
                    if (listener == null) {
                        throw new IllegalStateException("Listener proxy is null.");
                    }

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

    public ListenerList<BeanMonitorListener> getBeanMonitorListeners() {
        return beanMonitorListeners;
    }
}
