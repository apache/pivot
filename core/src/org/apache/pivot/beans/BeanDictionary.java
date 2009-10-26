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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.HashSet;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.ThreadUtilities;
import org.apache.pivot.util.Vote;

/**
 * Exposes Java bean properties of an object via the {@link Dictionary}
 * interface. A call to {@link Dictionary#get(Object)} invokes the getter for
 * the corresponding property, and a call to
 * {@link Dictionary#put(Object, Object)} invokes the property's setter.
 * <p>
 * Properties may provide multiple setters; the appropriate setter to invoke
 * is determined by the type of the value being set. If the value is
 * <tt>null</tt>, the return type of the getter method is used.
 */
public class BeanDictionary implements Dictionary<String, Object>, Iterable<String> {
    /**
     * Property iterator. Walks the list of methods defined by the bean and
     * returns a value for each getter method.
     */
    private class PropertyIterator implements Iterator<String> {
        private Method[] methods = null;
        private Field[] fields = null;

        int i = 0, j = 0;
        private String nextProperty = null;

        public PropertyIterator() {
            Class<?> type = bean.getClass();
            methods = type.getMethods();
            fields = type.getFields();
            nextProperty();
        }

        @Override
        public boolean hasNext() {
            return (nextProperty != null);
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            String nextProperty = this.nextProperty;
            nextProperty();

            return nextProperty;
        }

        private void nextProperty() {
            nextProperty = null;

            while (i < methods.length
                && nextProperty == null) {
                Method method = methods[i++];

                if (method.getParameterTypes().length == 0
                    && (method.getModifiers() & Modifier.STATIC) == 0) {
                    String methodName = method.getName();

                    String prefix = null;
                    if (methodName.startsWith(GET_PREFIX)) {
                        prefix = GET_PREFIX;
                    } else {
                        if (methodName.startsWith(IS_PREFIX)) {
                            prefix = IS_PREFIX;
                        }
                    }

                    if (prefix != null) {
                        int propertyOffset = prefix.length();
                        nextProperty = Character.toLowerCase(methodName.charAt(propertyOffset))
                            + methodName.substring(propertyOffset + 1);
                    }

                    if (nextProperty != null
                        && ignoreReadOnlyProperties
                        && isReadOnly(nextProperty)) {
                        nextProperty = null;
                    }
                }
            }

            if (nextProperty == null) {
                while (j < fields.length
                    && nextProperty == null) {
                    Field field = fields[j++];

                    int modifiers = field.getModifiers();
                    if ((modifiers & Modifier.PUBLIC) != 0
                        && (modifiers & Modifier.STATIC) == 0) {
                        nextProperty = FIELD_PREFIX + field.getName();
                    }

                    if (nextProperty != null
                        && ignoreReadOnlyProperties
                        && (modifiers & Modifier.FINAL) != 0) {
                        nextProperty = null;
                    }
                }
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class BeanInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method event, Object[] arguments) throws Throwable {
            String eventName = event.getName();
            if (eventName.endsWith(PROPERTY_CHANGE_SUFFIX)) {
                String propertyName = eventName.substring(0, eventName.length()
                    - PROPERTY_CHANGE_SUFFIX.length());

                if (notifyingProperties.contains(propertyName)) {
                    beanDictionaryListeners.propertyChanged(BeanDictionary.this, propertyName);
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

    private static class BeanDictionaryListenerList extends ListenerList<BeanDictionaryListener>
        implements BeanDictionaryListener {
        @Override
        public void beanChanged(BeanDictionary beanDictionary, Object previousBean) {
            for (BeanDictionaryListener listener : this) {
                listener.beanChanged(beanDictionary, previousBean);
            }
        }

        @Override
        public void propertyChanged(BeanDictionary beanDictionary, String propertyName) {
            for (BeanDictionaryListener listener : this) {
                listener.propertyChanged(beanDictionary, propertyName);
            }
        }
    }

    private Object bean;
    private boolean ignoreReadOnlyProperties;

    private HashMap<Class<?>, Object> beanListenerProxies = null;
    private BeanInvocationHandler invocationHandler = null;
    private HashSet<String> notifyingProperties = null;

    private BeanDictionaryListenerList beanDictionaryListeners = null;

    public static final String GET_PREFIX = "get";
    public static final String IS_PREFIX = "is";
    public static final String SET_PREFIX = "set";
    public static final String FIELD_PREFIX = "~";
    public static final String LISTENERS_SUFFIX = "Listeners";
    public static final String PROPERTY_CHANGE_SUFFIX = "Changed";

    /**
     * Creates a new bean dictionary that is not associated with a bean.
     */
    public BeanDictionary() {
        this(null);
    }

    /**
     * Creates a new bean dictionary.
     *
     * @param bean
     * The bean object to wrap.
     */
    public BeanDictionary(Object bean) {
        this(bean, false);
    }

    /**
     * Creates a new bean dictionary.
     *
     * @param bean
     * The bean object to wrap.
     */
    public BeanDictionary(Object bean, boolean ignoreReadOnlyProperties) {
        this.ignoreReadOnlyProperties = ignoreReadOnlyProperties;
        setBean(bean);
    }

    /**
     * Returns the bean object this dictionary wraps.
     *
     * @return
     * The bean object, or <tt>null</tt> if no bean has been set.
     */
    public Object getBean() {
        return bean;
    }

    /**
     * Sets the bean object that this dictionary will wrap.
     * <p>
     * <b>NOTE</b>: failing to clear the bean of a bean dictionary may result in
     * memory leaks, as the bean object may maintain references to the bean
     * dictionary as long as it is set.
     *
     * @param bean
     * The bean object, or <tt>null</tt> to clear the bean.
     */
    public void setBean(Object bean) {
        Object previousBean = this.bean;

        if (bean != previousBean) {
            if (beanDictionaryListeners != null
                && previousBean != null) {
                unregisterBeanListeners();
            }

            this.bean = bean;

            if (beanDictionaryListeners != null) {
                if (bean != null) {
                    registerBeanListeners();
                }

                beanDictionaryListeners.beanChanged(this, previousBean);
            }
        }
    }

    /**
     * Invokes the getter method for the given property.
     *
     * @param key
     * The property name.
     *
     * @return
     * The value returned by the method, or <tt>null</tt> if no such method
     * exists.
     */
    @Override
    public Object get(String key) {
        if (bean == null) {
            throw new IllegalStateException("bean is null.");
        }

        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        if (key.startsWith(FIELD_PREFIX)) {
            Field field = getField(key.substring(1));

            if (field != null) {
                try {
                    value = field.get(bean);
                } catch (IllegalAccessException exception) {
                    // No-op
                }
            }
        } else {
            Method getterMethod = getGetterMethod(key);

            if (getterMethod != null) {
                try {
                    value = getterMethod.invoke(bean, new Object[] {});
                } catch (IllegalAccessException exception) {
                    // No-op
                } catch (InvocationTargetException exception) {
                    // No-op
                }
            }
        }

        return value;
    }

    /**
     * Invokes the a setter method for the given property. The method
     * signature is determined by the type of the value. If the value is
     * <tt>null</tt>, the return type of the getter method is used.
     *
     * @param key
     * The property name.
     *
     * @param value
     * The new property value.
     *
     * @return
     * Returns <tt>null</tt>, since returning the previous value would require
     * a call to the getter method, which may not be an efficient operation.
     *
     * @throws PropertyNotFoundException
     * If the given property does not exist or is read-only.
     */
    @Override
    public Object put(String key, Object value) {
        if (bean == null) {
            throw new IllegalStateException("bean is null.");
        }

        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        if (key.startsWith(FIELD_PREFIX)) {
            Field field = getField(key.substring(1));

            if (field == null) {
                throw new PropertyNotFoundException("Property \"" + key + "\""
                    + " does not exist or is final.");
            }

            Class<?> fieldType = field.getType();
            Class<?> valueType = value.getClass();
            if (fieldType != valueType
                && valueType == String.class) {
                value = coerce((String)value, fieldType);
            }

            try {
                field.set(bean, value);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }
        } else {
            Method setterMethod = null;

            if (value != null) {
                setterMethod = getSetterMethod(key, value.getClass());
            }

            if (setterMethod == null) {
                Class<?> propertyType = getType(key);

                if (propertyType != null) {
                    setterMethod = getSetterMethod(key, propertyType);

                    if (value instanceof String) {
                        value = coerce((String)value, propertyType);
                    }
                }
            }

            if (setterMethod == null) {
                throw new PropertyNotFoundException("Property \"" + key + "\""
                    + " does not exist or is read-only.");
            }

            try {
                setterMethod.invoke(bean, new Object[] {value});
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            } catch (InvocationTargetException exception) {
                Throwable cause = exception.getCause();

                if (cause instanceof RuntimeException) {
                    throw (RuntimeException)cause;
                } else {
                    throw new RuntimeException(cause);
                }
            }
        }

        return null;
    }

    /**
     * @throws UnsupportedOperationException
     * This method is not supported.
     */
    @Override
    public Object remove(String key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Verifies the existence of a property. The property must have a getter
     * method; write-only properties are not supported.
     *
     * @param key
     * The property name.
     *
     * @return
     * <tt>true</tt> if the property exists; <tt>false</tt>, otherwise.
     */
    @Override
    public boolean containsKey(String key) {
        if (bean == null) {
            throw new IllegalStateException("bean is null.");
        }

        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        boolean containsKey;

        if (key.startsWith(FIELD_PREFIX)) {
            containsKey = (getField(key.substring(1)) != null);
        } else {
            containsKey = (getGetterMethod(key) != null);
        }

        return containsKey;
    }

    /**
     * Verifies that the bean contains at least one property.
     */
    @Override
    public boolean isEmpty() {
        if (bean == null) {
            throw new IllegalStateException("bean is null.");
        }

        return !iterator().hasNext();
    }

    /**
     * Tests the read-only state of a property.
     *
     * @param key
     * The property name.
     *
     * @return
     * <tt>true</tt> if the property is read-only; <tt>false</tt>, otherwise.
     */
    public boolean isReadOnly(String key) {
        if (bean == null) {
            throw new IllegalStateException("bean is null.");
        }

        return isReadOnly(bean.getClass(), key);
    }

    /**
     * Tells whether or not the specified property fires change events.
     *
     * @param key
     * The property name.
     *
     * @return
     * <tt>true</tt> if the property fires change events; <tt>false</tt>
     * otherwise.
     */
    public boolean isNotifying(String key) {
        if (bean == null) {
            throw new IllegalStateException("bean is null.");
        }

        if (beanDictionaryListeners == null) {
            beanDictionaryListeners = new BeanDictionaryListenerList();
            beanListenerProxies = new HashMap<Class<?>, Object>();
            invocationHandler = new BeanInvocationHandler();
            notifyingProperties = new HashSet<String>();

            registerBeanListeners();
        }

        return notifyingProperties.contains(key);
    }

    /**
     * Returns the type of a property.
     *
     * @param key
     * The property name.
     *
     * @return
     * The type of the property.
     */
    public Class<?> getType(String key) {
        if (bean == null) {
            throw new IllegalStateException("bean is null.");
        }

        return getType(bean.getClass(), key);
    }

    /**
     * Returns an iterator over the bean's properties.
     *
     * @return
     * A property iterator for this bean.
     */
    @Override
    public Iterator<String> iterator() {
        if (bean == null) {
            throw new IllegalStateException("bean is null.");
        }

        return new PropertyIterator();
    }

    /**
     * Returns the getter method for a property.
     *
     * @param key
     * The property name.
     *
     * @return
     * The getter method, or <tt>null</tt> if the method does not exist.
     */
    private Method getGetterMethod(String key) {
        return getGetterMethod(bean.getClass(), key);
    }

    /**
     * Returns the public, non-static field for a property. Note that fields
     * will only be consulted for bean properties after bean methods.
     *
     * @param fieldName
     * The property name
     *
     * @return
     * The field, or <tt>null</tt> if the field does not exist, or is
     * non-public or static
     */
    private Field getField(String fieldName) {
        return getField(bean.getClass(), fieldName);
    }

    /**
     * Returns the setter method for a property.
     *
     * @param key
     * The property name.
     *
     * @return
     * The getter method, or <tt>null</tt> if the method does not exist.
     */
    private Method getSetterMethod(String key, Class<?> valueType) {
        return getSetterMethod(bean.getClass(), key, valueType);
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

                            if (containsKey(propertyName)) {
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
                    Object listener = beanListenerProxies.get(listenerInterface);
                    if (listener == null) {
                        listener = Proxy.newProxyInstance(ThreadUtilities.getClassLoader(),
                            new Class[]{listenerInterface}, invocationHandler);
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

        notifyingProperties.clear();
    }

    /**
     * Tests the read-only state of a property. Note that is no such property
     * exists, this method will return <tt>true</tt> (it will <u>not</u> throw
     * an exception).
     *
     * @param type
     * The bean class.
     *
     * @param key
     * The property name.
     *
     * @return
     * <tt>true</tt> if the property is read-only; <tt>false</tt>, otherwise.
     */
    public static boolean isReadOnly(Class<?> type, String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        boolean isReadOnly = true;

        if (key.startsWith(FIELD_PREFIX)) {
            Field field = getField(type, key.substring(1));
            isReadOnly = (field == null
                || (field.getModifiers() & Modifier.FINAL) != 0);
        } else {
            Method setterMethod = getSetterMethod(type, key, getType(type, key));
            isReadOnly = (setterMethod == null);
        }

        return isReadOnly;
    }

    /**
     * Returns the type of a property.
     *
     * @param beanClass
     * The bean class.
     *
     * @param key
     * The property name.
     *
     * @return
     * The type of the property, or <tt>null</tt> if no such bean property
     * exists.
     */
    public static Class<?> getType(Class<?> beanClass, String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Class<?> type = null;

        if (key.startsWith(FIELD_PREFIX)) {
            Field field = getField(beanClass, key.substring(1));

            if (field != null) {
                type = field.getType();
            }
        } else {
            Method getterMethod = getGetterMethod(beanClass, key);

            if (getterMethod != null) {
                type = getterMethod.getReturnType();
            }
        }

        return type;
    }

    /**
     * Returns the public, non-static field for a property. Note that fields
     * will only be consulted for bean properties after bean methods.
     *
     * @param type
     * The bean class
     *
     * @param fieldName
     * The property name
     *
     * @return
     * The field, or <tt>null</tt> if the field does not exist, or is
     * non-public or static
     */
    public static Field getField(Class<?> type, String fieldName) {
        Field field = null;

        try {
            field = type.getField(fieldName);

            int modifiers = field.getModifiers();

            // Exclude non-public, static, and final fields
            if ((modifiers & Modifier.PUBLIC) == 0
                || (modifiers & Modifier.STATIC) > 0
                || (modifiers & Modifier.FINAL) > 0) {
                field = null;
            }
        } catch (NoSuchFieldException exception) {
            // No-op
        }

        return field;
    }

    /**
     * Returns the getter method for a property.
     *
     * @param beanClass
     * The bean class.
     *
     * @param key
     * The property name.
     *
     * @return
     * The getter method, or <tt>null</tt> if the method does not exist.
     */
    public static Method getGetterMethod(Class<?> beanClass, String key) {
        // Upper-case the first letter
        key = Character.toUpperCase(key.charAt(0)) + key.substring(1);
        Method getterMethod = null;

        try {
            getterMethod = beanClass.getMethod(GET_PREFIX + key, new Class<?>[] {});
        } catch (NoSuchMethodException exception) {
            // No-op
        }

        if (getterMethod == null) {
            try {
                getterMethod = beanClass.getMethod(IS_PREFIX + key, new Class<?>[] {});
            } catch (NoSuchMethodException exception) {
                // No-op
            }
        }

        return getterMethod;
    }

    /**
     * Returns the setter method for a property.
     *
     * @param beanClass
     * The bean class.
     *
     * @param key
     * The property name.
     *
     * @return
     * The getter method, or <tt>null</tt> if the method does not exist.
     */
    public static Method getSetterMethod(Class<?> beanClass, String key, Class<?> valueType) {
        Method setterMethod = null;

        if (valueType != null) {
            // Upper-case the first letter and prepend the "set" prefix to
            // determine the method name
            key = Character.toUpperCase(key.charAt(0)) + key.substring(1);
            final String methodName = SET_PREFIX + key;

            try {
                setterMethod = beanClass.getMethod(methodName, new Class<?>[] {valueType});
            } catch (NoSuchMethodException exception) {
                // No-op
            }

            if (setterMethod == null) {
                // Look for a match on the value's super type
                Class<?> superType = valueType.getSuperclass();
                setterMethod = getSetterMethod(beanClass, key, superType);
            }

            if (setterMethod == null) {
                // If value type is a primitive wrapper, look for a method
                // signature with the corresponding primitive type
                try {
                    Field primitiveTypeField = valueType.getField("TYPE");
                    Class<?> primitiveValueType = (Class<?>)primitiveTypeField.get(null);

                    try {
                        setterMethod = beanClass.getMethod(methodName, new Class<?>[] {primitiveValueType});
                    } catch (NoSuchMethodException exception) {
                        // No-op
                    }
                } catch (NoSuchFieldException exception) {
                    // No-op; not a wrapper type
                } catch (IllegalAccessException exception) {
                    // No-op
                }
            }

            if (setterMethod == null) {
                // Walk the interface graph to find a matching method
                Class<?>[] interfaces = valueType.getInterfaces();

                int i = 0, n = interfaces.length;
                while (setterMethod == null
                    && i < n) {
                    Class<?> interfaceType = interfaces[i++];
                    setterMethod = getSetterMethod(beanClass, key, interfaceType);
                }
            }
        }

        return setterMethod;
    }

    /**
     * Coerces a string value to a primitive type.
     *
     * @param value
     * @param type
     *
     * @return
     * The coerced value.
     */
    public static Object coerce(String value, Class<?> type) {
        Object coercedValue;

        if (type == Boolean.class
            || type == Boolean.TYPE) {
            coercedValue = Boolean.parseBoolean(value);
        } else if (type == Character.class
            || type == Character.TYPE) {
            if (value.length() == 1) {
                coercedValue = value.charAt(0);
            } else {
                throw new IllegalArgumentException("\"" + value + "\" is not a valid character");
            }
        } else if (type == Byte.class
            || type == Byte.TYPE) {
            coercedValue = Byte.parseByte(value);
        } else if (type == Short.class
            || type == Short.TYPE) {
            coercedValue = Short.parseShort(value);
        } else if (type == Integer.class
            || type == Integer.TYPE) {
            coercedValue = Integer.parseInt(value);
        } else if (type == Long.class
            || type == Long.TYPE) {
            coercedValue = Long.parseLong(value);
        } else if (type == Float.class
            || type == Float.TYPE) {
            coercedValue = Float.parseFloat(value);
        } else if (type == Double.class
            || type == Double.TYPE) {
            coercedValue = Double.parseDouble(value);
        } else {
            coercedValue = value;
        }

        return coercedValue;
    }

    public ListenerList<BeanDictionaryListener> getBeanDictionaryListeners() {
        if (beanDictionaryListeners == null) {
            beanDictionaryListeners = new BeanDictionaryListenerList();
            beanListenerProxies = new HashMap<Class<?>, Object>();
            invocationHandler = new BeanInvocationHandler();
            notifyingProperties = new HashSet<String>();

            if (bean != null) {
                registerBeanListeners();
            }
        }

        return beanDictionaryListeners;
    }
}
