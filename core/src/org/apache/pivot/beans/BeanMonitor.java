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
import org.apache.pivot.util.Utils;
import org.apache.pivot.util.Vote;

/**
 * Class for monitoring Java bean property changes.
 */
public class BeanMonitor {
    private class BeanInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(final Object proxy, final Method event, final Object[] arguments) throws Throwable {
            String propertyName;
            if ((propertyName = getPropertyChangeName(event.getName())) != null) {
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

    private class PropertyChangeListenerList extends ListenerList<PropertyChangeListener> implements
        PropertyChangeListener {
        @Override
        public void add(final PropertyChangeListener listener) {
            if (isEmpty()) {
                registerBeanListeners();
            }

            super.add(listener);
        }

        @Override
        public void remove(final PropertyChangeListener listener) {
            super.remove(listener);

            if (isEmpty()) {
                unregisterBeanListeners();
            }
        }

        @Override
        public void propertyChanged(final Object beanArgument, final String propertyName) {
            forEach(listener -> listener.propertyChanged(beanArgument, propertyName));
        }
    }

    private Object bean = null;

    private HashMap<Class<?>, Object> beanListenerProxies = new HashMap<>();
    private BeanInvocationHandler invocationHandler = new BeanInvocationHandler();
    private HashSet<String> notifyingProperties = new HashSet<>();

    private PropertyChangeListenerList propertyChangeListeners = new PropertyChangeListenerList();

    public static final String LISTENERS_SUFFIX = "Listeners";
    public static final String PROPERTY_CHANGE_SUFFIX = "Changed";

    private static String getPropertyChangeName(final String name) {
        if (name.endsWith(PROPERTY_CHANGE_SUFFIX)) {
            return name.substring(0, name.length() - PROPERTY_CHANGE_SUFFIX.length());
        }
        return null;
    }

    public BeanMonitor(final Object bean) {
        Utils.checkNull(bean, "bean object");

        this.bean = bean;

        BeanAdapter beanAdapter = new BeanAdapter(bean);

        for (Method method : bean.getClass().getMethods()) {
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
                        throw new RuntimeException(listenerInterface.getName() + " is not an interface.");
                    }

                    for (Method interfaceMethod : listenerInterface.getMethods()) {
                        String propertyName;
                        if ((propertyName = getPropertyChangeName(interfaceMethod.getName())) != null) {
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
     *
     * @return The object we are wrapping.
     */
    public Object getBean() {
        return bean;
    }

    /**
     * Tests whether a property fires change events.
     *
     * @param key The property name.
     * @return <tt>true</tt> if the property fires change events; <tt>false</tt>
     * otherwise.
     */
    public boolean isNotifying(final String key) {
        return notifyingProperties.contains(key);
    }

    private void invoke(final String methodName, final boolean addProxy) {
        for (Method method : bean.getClass().getMethods()) {
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
                        listenerList = method.invoke(bean);
                    } catch (IllegalAccessException | InvocationTargetException exception) {
                        throw new RuntimeException(exception);
                    }

                    // Get the listener for this interface
                    Object listener = beanListenerProxies.get(listenerInterface);
                    if (listener == null) {
                        if (addProxy) {
                            listener = Proxy.newProxyInstance(
                                Thread.currentThread().getContextClassLoader(),
                                new Class<?>[] {listenerInterface}, invocationHandler);
                            beanListenerProxies.put(listenerInterface, listener);
                        } else {
                            throw new IllegalStateException("Listener proxy is null.");
                        }
                    }

                    Class<?> listenerListClass = listenerList.getClass();
                    Method classMethod;
                    try {
                        classMethod = listenerListClass.getMethod(methodName, new Class<?>[] {Object.class});
                    } catch (NoSuchMethodException exception) {
                        throw new RuntimeException(exception);
                    }

                    try {
                        classMethod.invoke(listenerList, new Object[] {listener});
                    } catch (IllegalAccessException | InvocationTargetException exception) {
                        throw new RuntimeException(exception);
                    }
                }
            }
        }
    }

    /**
     * Registers event listeners on the bean so that the dictionary can fire
     * property change events and report which properties can fire change
     * events.
     */
    private void registerBeanListeners() {
        invoke("add", true);
    }

    /**
     * Un-registers event listeners on the bean.
     */
    private void unregisterBeanListeners() {
        invoke("remove", false);
    }

    public ListenerList<PropertyChangeListener> getPropertyChangeListeners() {
        return propertyChangeListeners;
    }
}
