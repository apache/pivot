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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.MapListener;
import org.apache.pivot.util.ListenerList;

/**
 * Exposes Java bean properties of an object via the {@link Map}
 * interface. A call to {@link Map#get(Object)} invokes the getter for
 * the corresponding property, and a call to
 * {@link Map#put(Object, Object)} invokes the property's setter.
 * <p>
 * Properties may provide multiple setters; the appropriate setter to invoke
 * is determined by the type of the value being set. If the value is
 * <tt>null</tt>, the return type of the getter method is used.
 */
public class BeanAdapter implements Map<String, Object> {
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

                        if (nextProperty.equals("class")) {
                            nextProperty = null;
                        }
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

    private Object bean;
    private boolean ignoreReadOnlyProperties;

    private MapListenerList<String, Object> mapListeners = new MapListenerList<String, Object>();

    public static final String GET_PREFIX = "get";
    public static final String IS_PREFIX = "is";
    public static final String SET_PREFIX = "set";
    public static final String FIELD_PREFIX = "~";

    private static final String ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT =
        "Unable to access property \"%s\" for type %s.";
    
    /**
     * Creates a new bean dictionary.
     *
     * @param bean
     * The bean object to wrap.
     */
    public BeanAdapter(Object bean) {
        this(bean, false);
    }

    /**
     * Creates a new bean dictionary.
     *
     * @param bean
     * The bean object to wrap.
     */
    public BeanAdapter(Object bean, boolean ignoreReadOnlyProperties) {
        if (bean == null) {
            throw new IllegalArgumentException();
        }

        this.bean = bean;
        this.ignoreReadOnlyProperties = ignoreReadOnlyProperties;
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
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        if (key.length() == 0) {
            throw new IllegalArgumentException("key is empty.");
        }

        Object value = null;

        if (key.startsWith(FIELD_PREFIX)) {
            Field field = getField(key.substring(1));

            if (field != null) {
                try {
                    value = field.get(bean);
                } catch (IllegalAccessException exception) {
                    throw new RuntimeException(String.format(ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT,
                        key, bean.getClass().getName()), exception);
                }
            }
        } else {
            Method getterMethod = getGetterMethod(key);

            if (getterMethod != null) {
                try {
                    value = getterMethod.invoke(bean, new Object[] {});
                } catch (IllegalAccessException exception) {
                    throw new RuntimeException(String.format(ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT,
                        key, bean.getClass().getName()), exception);
                } catch (InvocationTargetException exception) {
                    throw new RuntimeException(String.format("Error getting property \"%s\" for type %s.",
                        key, bean.getClass().getName()), exception.getCause());
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
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        if (key.length() == 0) {
            throw new IllegalArgumentException("key is empty.");
        }

        if (key.startsWith(FIELD_PREFIX)) {
            Field field = getField(key.substring(1));

            if (field == null) {
                throw new PropertyNotFoundException("Property \"" + key + "\""
                    + " does not exist or is final.");
            }

            Class<?> fieldType = field.getType();
            Class<?> valueType = value.getClass();
            if (!fieldType.isAssignableFrom(valueType)) {
                value = coerce(value, fieldType);
            }

            try {
                field.set(bean, value);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(String.format(ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT,
                    key, bean.getClass().getName()), exception);
            }
        } else {
            Method setterMethod = null;

            if (value != null) {
                // Get the setter method for the value type
                setterMethod = getSetterMethod(key, value.getClass());
            }

            if (setterMethod == null) {
                // Get the property type and attempt to coerce the value to it
                Class<?> propertyType = getType(key);

                if (propertyType != null) {
                    setterMethod = getSetterMethod(key, propertyType);
                    value = coerce(value, propertyType);
                }
            }

            if (setterMethod == null) {
                throw new PropertyNotFoundException("Property \"" + key + "\""
                    + " does not exist or is read-only.");
            }

            try {
                setterMethod.invoke(bean, new Object[] {value});
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(String.format(ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT,
                    key, bean.getClass().getName()), exception);
            } catch (InvocationTargetException exception) {
                throw new RuntimeException(String.format("Error setting property \"%s\" for type %s to value \"%s\"",
                    key, bean.getClass().getName(), "" + value), exception.getCause());
            }
        }

        Object previousValue = null;
        mapListeners.valueUpdated(this, key, previousValue);

        return previousValue;
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
     * @throws UnsupportedOperationException
     * This method is not supported.
     */
    @Override
    public synchronized void clear() {
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
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        if (key.length() == 0) {
            throw new IllegalArgumentException("key is empty.");
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
     * @throws UnsupportedOperationException
     * This method is not supported.
     */
    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     * This method is not supported.
     */
    @Override
    public int getCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Comparator<String> getComparator() {
        return null;
    }

    /**
     * @throws UnsupportedOperationException
     * This method is not supported.
     */
    @Override
    public void setComparator(Comparator<String> comparator) {
        throw new UnsupportedOperationException();
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
        return isReadOnly(bean.getClass(), key);
    }

    /**
     * Returns the type of a property.
     *
     * @param key
     * The property name.
     *
     * @see
     * #getType(Class, String)
     */
    public Class<?> getType(String key) {
        return getType(bean.getClass(), key);
    }

    /**
     * Returns the generic type of a property.
     *
     * @param key
     * The property name.
     *
     * @see
     * #getGenericType(Class, String)
     */
    public Type getGenericType(String key) {
        return getGenericType(bean.getClass(), key);
    }

    /**
     * Returns an iterator over the bean's properties.
     *
     * @return
     * A property iterator for this bean.
     */
    @Override
    public Iterator<String> iterator() {
        return new PropertyIterator();
    }

    @Override
    public ListenerList<MapListener<String, Object>> getMapListeners() {
        return mapListeners;
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
        if (fieldName == null) {
            throw new IllegalArgumentException("fieldName is null.");
        }

        return getField(bean.getClass(), fieldName);
    }

    /**
     * Tests the read-only state of a property. Note that if no such property
     * exists, this method will return <tt>true</tt> (it will <u>not</u> throw
     * an exception).
     *
     * @param beanClass
     * The bean class.
     *
     * @param key
     * The property name.
     *
     * @return
     * <tt>true</tt> if the property is read-only; <tt>false</tt>, otherwise.
     */
    public static boolean isReadOnly(Class<?> beanClass, String key) {
        if (beanClass == null) {
            throw new IllegalArgumentException("beanClass is null.");
        }

        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        if (key.length() == 0) {
            throw new IllegalArgumentException("key is empty.");
        }

        boolean isReadOnly = true;

        if (key.startsWith(FIELD_PREFIX)) {
            Field field = getField(beanClass, key.substring(1));
            if (field != null) {
                isReadOnly = ((field.getModifiers() & Modifier.FINAL) != 0);
            }
        } else {
            Method getterMethod = getGetterMethod(beanClass, key);
            if (getterMethod != null) {
                Method setterMethod = getSetterMethod(beanClass, key, getType(beanClass, key));
                isReadOnly = (setterMethod == null);
            }
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
        if (beanClass == null) {
            throw new IllegalArgumentException("beanClass is null.");
        }

        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        if (key.length() == 0) {
            throw new IllegalArgumentException("key is empty.");
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
     * Returns the generic type of a property.
     *
     * @param beanClass
     * The bean class.
     *
     * @param key
     * The property name.
     *
     * @return
     * The generic type of the property, or <tt>null</tt> if no such
     * bean property exists. If the type is a generic, an instance of
     * {@link java.lang.reflect.ParameterizedType} will be returned. Otherwise,
     * an instance of {@link java.lang.Class} will be returned.
     */
    public static Type getGenericType(Class<?> beanClass, String key) {
        if (beanClass == null) {
            throw new IllegalArgumentException("beanClass is null.");
        }

        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        if (key.length() == 0) {
            throw new IllegalArgumentException("key is empty.");
        }

        Type genericType = null;

        if (key.startsWith(FIELD_PREFIX)) {
            Field field = getField(beanClass, key.substring(1));

            if (field != null) {
                genericType = field.getGenericType();
            }
        } else {
            Method getterMethod = getGetterMethod(beanClass, key);

            if (getterMethod != null) {
                genericType = getterMethod.getGenericReturnType();
            }
        }

        return genericType;
    }

    /**
     * Returns the public, non-static fields for a property. Note that fields
     * will only be consulted for bean properties after bean methods.
     *
     * @param beanClass
     * The bean class.
     *
     * @param key
     * The property name.
     *
     * @return
     * The field, or <tt>null</tt> if the field does not exist, or is
     * non-public or static.
     */
    public static Field getField(Class<?> beanClass, String key) {
        if (beanClass == null) {
            throw new IllegalArgumentException("beanClass is null.");
        }

        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        if (key.length() == 0) {
            throw new IllegalArgumentException("key is empty.");
        }

        Field field = null;

        try {
            field = beanClass.getField(key);

            int modifiers = field.getModifiers();

            // Exclude non-public and static fields
            if ((modifiers & Modifier.PUBLIC) == 0
                || (modifiers & Modifier.STATIC) > 0) {
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
        if (beanClass == null) {
            throw new IllegalArgumentException("beanClass is null.");
        }

        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        if (key.length() == 0) {
            throw new IllegalArgumentException("key is empty.");
        }

        // Upper-case the first letter
        key = Character.toUpperCase(key.charAt(0)) + key.substring(1);
        Method getterMethod = null;

        try {
            getterMethod = beanClass.getMethod(GET_PREFIX + key);
        } catch (NoSuchMethodException exception) {
            // No-op
        }

        if (getterMethod == null) {
            try {
                getterMethod = beanClass.getMethod(IS_PREFIX + key);
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
        if (beanClass == null) {
            throw new IllegalArgumentException("beanClass is null.");
        }

        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        if (key.length() == 0) {
            throw new IllegalArgumentException("key is empty.");
        }

        Method setterMethod = null;

        if (valueType != null) {
            // Upper-case the first letter and prepend the "set" prefix to
            // determine the method name
            key = Character.toUpperCase(key.charAt(0)) + key.substring(1);
            final String methodName = SET_PREFIX + key;

            try {
                setterMethod = beanClass.getMethod(methodName, valueType);
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
                        setterMethod = beanClass.getMethod(methodName, primitiveValueType);
                    } catch (NoSuchMethodException exception) {
                        // No-op
                    }
                } catch (NoSuchFieldException exception) {
                    // No-op
                } catch (IllegalAccessException exception) {
                    throw new RuntimeException(String.format(ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT,
                        key, beanClass.getName()), exception);
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
     * Coerces a value to a given type.
     *
     * @param value
     * @param type
     *
     * @return
     * The coerced value.
     */
    @SuppressWarnings("unchecked")
    public static <T> T coerce(Object value, Class<? extends T> type) {
        if (type == null) {
            throw new IllegalArgumentException();
        }

        Object coercedValue;

        if (value == null) {
            // Null values can only be coerced to null
            coercedValue = null;
        } else {
            if (type.isAssignableFrom(value.getClass())) {
                // Value doesn't need coercion
                coercedValue = value;
            } else {
                // Coerce the value to the requested type
                if (type == Boolean.class
                    || type == Boolean.TYPE) {
                    coercedValue = Boolean.parseBoolean(value.toString());
                } else if (type == Character.class
                    || type == Character.TYPE) {
                    coercedValue = value.toString().charAt(0);
                } else if (type == Byte.class
                    || type == Byte.TYPE) {
                    if (value instanceof Number) {
                        coercedValue = ((Number)value).byteValue();
                    } else {
                        coercedValue = Byte.parseByte(value.toString());
                    }
                } else if (type == Short.class
                    || type == Short.TYPE) {
                    if (value instanceof Number) {
                        coercedValue = ((Number)value).shortValue();
                    } else {
                        coercedValue = Short.parseShort(value.toString());
                    }
                } else if (type == Integer.class
                    || type == Integer.TYPE) {
                    if (value instanceof Number) {
                        coercedValue = ((Number)value).intValue();
                    } else {
                        coercedValue = Integer.parseInt(value.toString());
                    }
                } else if (type == Long.class
                    || type == Long.TYPE) {
                    if (value instanceof Number) {
                        coercedValue = ((Number)value).longValue();
                    } else {
                        coercedValue = Long.parseLong(value.toString());
                    }
                } else if (type == Float.class
                    || type == Float.TYPE) {
                    if (value instanceof Number) {
                        coercedValue = ((Number)value).floatValue();
                    } else {
                        coercedValue = Float.parseFloat(value.toString());
                    }
                } else if (type == Double.class
                    || type == Double.TYPE) {
                    if (value instanceof Number) {
                        coercedValue = ((Number)value).doubleValue();
                    } else {
                        coercedValue = Double.parseDouble(value.toString());
                    }
                } else if (type == String.class) {
                    coercedValue = value.toString();
                } else {
                    throw new IllegalArgumentException("Unable to coerce " + value.getClass().getName()
                        + " to " + type + ".");
                }
            }
        }

        return (T)coercedValue;
    }
}
