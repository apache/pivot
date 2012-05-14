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

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.HashSet;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Vote;

/**
 * Class for monitoring Java bean property changes.
 */
public class BeanMonitor {
    private class BeanInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method event, Object[] arguments) throws Throwable {
            String eventName = event.getName();
            if (eventName.endsWith(PROPERTY_CHANGE_SUFFIX)) {
                String propertyName = eventName.substring(0, eventName.length()
                    - PROPERTY_CHANGE_SUFFIX.length());

                if (notifyingProperties.contains(propertyName)) {
                    propertyChangeListeners.propertyChanged(bean, propertyName);
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

    private class PropertyChangeListenerList extends ListenerList<PropertyChangeListener>
        implements PropertyChangeListener {
        @Override
        public void add(PropertyChangeListener listener) {
            if (isEmpty()) {
                registerBeanListeners();
            }

            super.add(listener);
        }

        @Override
        public void remove(PropertyChangeListener listener) {
            super.remove(listener);

            if (isEmpty()) {
                unregisterBeanListeners();
            }
        }

        @Override
        public void propertyChanged(Object beanArgument, String propertyName) {
            for (PropertyChangeListener listener : this) {
                listener.propertyChanged(beanArgument, propertyName);
            }
        }
    }

    private Object bean = null;

    private HashMap<Class<?>, Object> beanListenerProxies = new HashMap<Class<?>, Object>();
    private BeanInvocationHandler invocationHandler = new BeanInvocationHandler();
    private HashSet<String> notifyingProperties = new HashSet<String>();

    private PropertyChangeListenerList propertyChangeListeners = new PropertyChangeListenerList();

    public static final String LISTENERS_SUFFIX = "Listeners";
    public static final String PROPERTY_CHANGE_SUFFIX = "Changed";

    public BeanMonitor(Object bean) {
        if (bean == null) {
            throw new IllegalArgumentException();
        }

        this.bean = bean;

        BeanAdapter beanAdapter = new BeanAdapter(bean);
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

                        if (interfaceMethodName.endsWith(PROPERTY_CHANGE_SUFFIX)) {
                            String propertyName = interfaceMethodName.substring(0,
                                interfaceMethodName.length() - PROPERTY_CHANGE_SUFFIX.length());

                            if (beanAdapter.containsKey(propertyName)) {
                                notifyingProperties.add(propertyName);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the bean object that this monitor wraps.
     */
    public Object getBean() {
        return bean;
    }

    /**
     * Tests whether a property fires change events.
     *
     * @param key
     * The property name.
     *
     * @return
     * <tt>true</tt> if the property fires change events; <tt>false</tt>
     * otherwise.
     */
    public boolean isNotifying(String key) {
        return notifyingProperties.contains(key);
    }

    /**
     * Registers event listeners on the bean so that the dictionary can fire
     * property change events and report which properties can fire change
     * events.
     */
    private void registerBeanListeners() {
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
                    Object listener = beanListenerProxies.get(listenerInterface);
                    if (listener == null) {
                        listener = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                            new Class<?>[]{listenerInterface}, invocationHandler);
                        beanListenerProxies.put(listenerInterface, listener);
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
     * Un-registers event listeners on the bean.
     */
    private void unregisterBeanListeners() {
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
                    Object listener = beanListenerProxies.get(listenerInterface);
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

    public ListenerList<PropertyChangeListener> getPropertyChangeListeners() {
        return propertyChangeListeners;
    }
}
