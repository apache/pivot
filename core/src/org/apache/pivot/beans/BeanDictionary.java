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
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.pivot.collections.Dictionary;


/**
 * Exposes Java bean properties of an object via the {@link Dictionary}
 * interface. A call to {@link Dictionary#get(Object)} invokes the getter for
 * the corresponding property, and a call to
 * {@link Dictionary#put(Object, Object)} invokes the property's setter.
 * <p>
 * Properties may provide multiple setters; the appropriate setter to invoke
 * is determined by the type of the value being set. If the value is
 * <tt>null</tt>, the return type of the getter method is used.
 *
 * @author gbrown
 */
public class BeanDictionary implements Dictionary<String, Object>, Iterable<String> {
    /**
     * Property iterator. Walks the list of methods defined by the bean and
     * returns a value for each getter method.
     */
    private class PropertyIterator implements Iterator<String> {
        private Method[] methods = null;

        int i = 0;
        private String nextProperty = null;

        public PropertyIterator() {
            Class<?> type = bean.getClass();
            methods = type.getMethods();
            nextProperty();
        }

        public boolean hasNext() {
            return (nextProperty != null);
        }

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
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private Object bean;
    private boolean ignoreReadOnlyProperties;

    public static final String GET_PREFIX = "get";
    public static final String IS_PREFIX = "is";
    public static final String SET_PREFIX = "set";
    public static final String LISTENERS_SUFFIX = "Listeners";

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
        if (bean == null) {
            throw new IllegalArgumentException("bean is null.");
        }

        this.bean = bean;
        this.ignoreReadOnlyProperties = ignoreReadOnlyProperties;
    }

    /**
     * Returns the bean object this dictionary wraps.
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
    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        Method getterMethod = getGetterMethod(key);

        if (getterMethod != null) {
            try {
                value = getterMethod.invoke(bean, new Object[] {});
            } catch(IllegalAccessException exception) {
                // No-op
            } catch(InvocationTargetException exception) {
                // No-op
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
    public Object put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Class<?> valueType = null;

        if (value == null) {
            Method getterMethod = getGetterMethod(key);
            if (getterMethod == null) {
                throw new PropertyNotFoundException("Property \"" + key + "\"" +
                    " does not exist.");
            }

            valueType = getterMethod.getReturnType();
        } else {
            valueType = value.getClass();
        }

        Method setterMethod = getSetterMethod(key, valueType);

        if (setterMethod == null) {
            throw new PropertyNotFoundException("Property \"" + key + "\""
                + " of type " + valueType.getName()
                + " does not exist or is read-only.");
        }

        try {
            setterMethod.invoke(bean, new Object[] {value});
        } catch(IllegalAccessException exception) {
            throw new RuntimeException(exception);
        } catch(InvocationTargetException exception) {
            Throwable cause = exception.getCause();

            if (cause instanceof RuntimeException) {
                throw (RuntimeException)cause;
            } else {
                throw new RuntimeException(cause);
            }
        }

        return null;
    }

    /**
     * @throws UnsupportedOperationException
     * This method is not supported.
     */
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
    public boolean containsKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        return (getGetterMethod(key) != null);
    }

    /**
     * Verifies that the bean contains at least one property.
     */
    public boolean isEmpty() {
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
        return isReadOnly(bean.getClass(), key);
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
        return getType(bean.getClass(), key);
    }

    /**
     * Returns an iterator over the bean's properties.
     *
     * @return
     * A property iterator for this bean.
     */
    public Iterator<String> iterator() {
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
     * Tests the read-only state of a property.
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

        return (getSetterMethod(type, key, getType(type, key)) == null);
    }

    /**
     * Returns the type of a property.
     *
     * @param type
     * The bean class.
     *
     * @param key
     * The property name.
     *
     * @return
     * The type of the property, or <tt>null</tt> if no such bean property
     * exists.
     */
    public static Class<?> getType(Class<?> type, String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Method getterMethod = getGetterMethod(type, key);

        return (getterMethod == null) ? null : getterMethod.getReturnType();
    }

    /**
     * Returns the getter method for a property.
     *
     * @param type
     * The bean class.
     *
     * @param key
     * The property name.
     *
     * @return
     * The getter method, or <tt>null</tt> if the method does not exist.
     */
    public static Method getGetterMethod(Class<?> type, String key) {
        // Upper-case the first letter
        key = Character.toUpperCase(key.charAt(0)) + key.substring(1);
        Method method = null;

        try {
            method = type.getMethod(GET_PREFIX + key, new Class<?>[] {});
        } catch(NoSuchMethodException exception) {
            // No-op
        }

        if (method == null) {
            try {
                method = type.getMethod(IS_PREFIX + key, new Class<?>[] {});
            } catch(NoSuchMethodException exception) {
                // No-op
            }
        }

        return method;
    }

    /**
     * Returns the setter method for a property.
     *
     * @param type
     * The bean class.
     *
     * @param key
     * The property name.
     *
     * @return
     * The getter method, or <tt>null</tt> if the method does not exist.
     */
    public static Method getSetterMethod(Class<?> type, String key, Class<?> valueType) {
        Method method = null;

        if (valueType != null) {
            // Upper-case the first letter and prepend the "set" prefix to
            // determine the method name
            key = Character.toUpperCase(key.charAt(0)) + key.substring(1);
            final String methodName = SET_PREFIX + key;

            try {
                method = type.getMethod(methodName, new Class<?>[] {valueType});
            } catch(NoSuchMethodException exception) {
                // No-op
            }

            if (method == null) {
                // Look for a match on the value's super type
                Class<?> superType = valueType.getSuperclass();
                method = getSetterMethod(type, key, superType);
            }

            if (method == null) {
                // If value type is a primitive wrapper, look for a method
                // signature with the corresponding primitive type
                try {
                    Field primitiveTypeField = valueType.getField("TYPE");
                    Class<?> primitiveValueType = (Class<?>)primitiveTypeField.get(null);

                    try {
                        method = type.getMethod(methodName, new Class<?>[] {primitiveValueType});
                    } catch(NoSuchMethodException exception) {
                        // No-op
                    }
                } catch(NoSuchFieldException exception) {
                    // No-op; not a wrapper type
                } catch(IllegalAccessException exception) {
                    // No-op
                }
            }

            if (method == null) {
                // Walk the interface graph to find a matching method
                Class<?>[] interfaces = valueType.getInterfaces();

                int i = 0, n = interfaces.length;
                while (method == null
                    && i < n) {
                    Class<?> interfaceType = interfaces[i++];
                    method = getSetterMethod(type, key, interfaceType);
                }
            }
        }

        return method;
    }
}
