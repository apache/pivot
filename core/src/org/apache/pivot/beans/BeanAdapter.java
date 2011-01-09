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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.ObservableMap;
import org.apache.pivot.util.ObservableMapListener;

/**
 * Exposes Java bean properties of an object via the {@link Map} interface.
 * A call to {@link Map#get(Object)} invokes the getter for the corresponding
 * property, and a call to {@link Map#put(Object, Object)} invokes the property's
 * setter.
 * <p>
 * Properties may define multiple setters; the appropriate setter to invoke
 * is determined by the type of the value being set. If the value is
 * <tt>null</tt> or there is no explicit setter for a given type, the
 * {@link #coerce(Object, Class)} method is used to attempt to convert the
 * value to the actual property type (defined by the return value of the
 * getter method).
 */
public class BeanAdapter extends AbstractMap<String, Object>
    implements ObservableMap<String , Object> {
    /**
     * Represents a property key/value pair.
     */
    private class PropertyEntry implements Entry<String, Object> {
        private String key;

        public PropertyEntry(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return get(key);
        }

        @Override
        public Object setValue(Object value) {
            return put(key, value);
        }
    }

    /**
     * Property entry set.
     */
    private class PropertyEntrySet extends AbstractSet<Entry<String, Object>> {
        /**
         * Property entry iterator. Returns a key/value pair for each property
         * defined by the bean.
         */
        private class PropertyEntryIterator implements Iterator<Entry<String, Object>> {
            private Class<?> type;
            private Method[] methods;

            private int i = 0;
            private String nextProperty = null;

            public PropertyEntryIterator() {
                type = bean.getClass();
                methods = type.getMethods();
                nextProperty();
            }

            @Override
            public boolean hasNext() {
                return (nextProperty != null);
            }

            @Override
            public Entry<String, Object> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                String nextProperty = this.nextProperty;
                nextProperty();

                return new PropertyEntry(nextProperty);
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

                            // Ignore read-only properties
                            if (getSetterMethod(type, nextProperty, method.getReturnType()) == null) {
                                nextProperty = null;
                            }
                        }
                    }
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<Entry<String, Object>> iterator() {
            return new PropertyEntryIterator();
        }
    }

    private Object bean;
    private PropertyEntrySet entrySet = new PropertyEntrySet();

    private ObservableMapListenerList<String, Object> observableMapListeners = null;

    public static final String GET_PREFIX = "get";
    public static final String IS_PREFIX = "is";
    public static final String SET_PREFIX = "set";

    private static final String VALUE_OF_METHOD_NAME = "valueOf";

    /**
     * Creates a new bean adapter.
     *
     * @param bean
     * The bean object to wrap.
     */
    public BeanAdapter(Object bean) {
        if (bean == null) {
            throw new IllegalArgumentException();
        }

        this.bean = bean;
    }

    /**
     * Returns the bean object this adapter wraps.
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
     * exists. The {@link #containsKey(String)} method can be used to
     * distinguish between these two cases.
     */
    @Override
    public Object get(Object key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }

        Object value = null;

        Method getterMethod = getGetterMethod(bean.getClass(), key.toString());

        if (getterMethod != null) {
            try {
                value = getterMethod.invoke(bean, new Object[] {});
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            } catch (InvocationTargetException exception) {
                throw new RuntimeException(exception);
            }
        }

        return value;
    }

    /**
     * Invokes a setter method for the given property. If the value is
     * <tt>null</tt> or there is no explicit setter for a given type, the
     * {@link #coerce(Object, Class)} method is used to attempt to convert the
     * value to the actual property type (defined by the return value of the
     * getter method).
     *
     * @param key
     * The property name.
     *
     * @param value
     * The new property value.
     *
     * @return
     * Returns <tt>null</tt>, since returning the previous value would require
     * a call to the getter method, which may neither be necessary nor
     * efficient.
     *
     * @throws PropertyNotFoundException
     * If the given property does not exist or is read-only.
     */
    @Override
    public Object put(String key, Object value) {
        Method setterMethod = null;

        Class<?> type = bean.getClass();

        if (value != null) {
            // Get the setter method for the value type
            setterMethod = getSetterMethod(type, key, value.getClass());
        }

        if (setterMethod == null) {
            // Get the property type and attempt to coerce the value to it
            Class<?> propertyType = getType(key);

            if (propertyType != null) {
                setterMethod = getSetterMethod(type, key, propertyType);
                value = coerce(value, propertyType);
            }
        }

        if (setterMethod == null) {
            throw new PropertyNotFoundException(String.format("Property %s does not exist"
                + " or is read-only.", key));
        }

        try {
            setterMethod.invoke(bean, new Object[] {value});
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        } catch (InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }

        return null;
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
    public boolean containsKey(Object key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }

        return (getGetterMethod(bean.getClass(), key.toString()) != null);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return entrySet;
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
        return (getSetterMethod(bean.getClass(), key, getType(key)) == null);
    }

    /**
     * Returns the type of a property.
     *
     * @param key
     * The property name.
     */
    public Class<?> getType(String key) {
        Method getterMethod = getGetterMethod(bean.getClass(), key);
        return (getterMethod == null) ? null : getterMethod.getReturnType();
    }

    /**
     * Returns the generic type of a property.
     *
     * @param key
     * The property name.
     */
    public Type getGenericType(String key) {
        Method getterMethod = getGetterMethod(bean.getClass(), key);
        return (getterMethod == null) ? null : getterMethod.getGenericReturnType();
    }

    @Override
    public ListenerList<ObservableMapListener<String, Object>> getObservableMapListeners() {
        // TODO If observableMapListeners is null, create the listener list and
        // register listeners on all notifying bean properties, using the @Property
        // annotation to determine which listeners to create.

        return observableMapListeners;
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
        } else if (type.isAssignableFrom(value.getClass())) {
            // Value doesn't need coercion
            coercedValue = value;
        } else if (type.isEnum()) {
            // Coerce the value to the enum type
            try {
                String name = value.toString();
                if (Character.isLowerCase(name.charAt(0))) {
                    name = toAllCaps(name);
                }

                Method valueOfMethod = type.getMethod(VALUE_OF_METHOD_NAME, String.class);
                coercedValue = valueOfMethod.invoke(null, name);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            } catch (InvocationTargetException exception) {
                throw new RuntimeException(exception);
            } catch (SecurityException exception) {
                throw new RuntimeException(exception);
            } catch (NoSuchMethodException exception) {
                throw new RuntimeException(exception);
            }
        } else if (type == Class.class) {
            try {
                coercedValue = Class.forName(value.toString());
            } catch (ClassNotFoundException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else if (type == Boolean.class
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
            throw new IllegalArgumentException(String.format("Unable to coerce %s to %s.",
                value.getClass().getName(), type));
        }

        return (T)coercedValue;
    }

    /**
     * Returns the value at a given path.
     *
     * @param root
     * The root object.
     *
     * @param path
     * The path to the value as a string.
     *
     * @return
     * The value at the given path.
     *
     * @see #get(Object, Sequence)
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Object root, String path) {
        return (T)get(root, parsePath(path));
    }

    /**
     * Returns the value at a given path.
     *
     * @param root
     * The root object.
     *
     * @param keys
     * The path to the value as a list of keys.
     *
     * @return
     * The value at the given path.
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Object root, List<String> keys) {
        if (keys == null) {
            throw new IllegalArgumentException();
        }

        Object value = root;

        for (int i = 0, n = keys.size(); i < n; i++) {
            if (value == null) {
                break;
            }

            String key = keys.get(i);

            if (value instanceof Map<?, ?>) {
                Map<String, Object> map = (Map<String, Object>)value;
                value = map.get(key);
            } else if (value instanceof List<?> && Character.isDigit(key.charAt(0))) {
                List<Object> list = (List<Object>)value;
                value = list.get(Integer.parseInt(key));
            } else {
                BeanAdapter beanAdapter = new BeanAdapter(value);
                value = beanAdapter.get(key);
            }
        }

        return (T)value;
    }

    public static byte getByte(Object root, String path) {
        Number value = get(root, path);
        return value.byteValue();
    }

    public static short getShort(Object root, String path) {
        Number value = get(root, path);
        return value.shortValue();
    }

    public static int getInt(Object root, String path) {
        Number value = get(root, path);
        return value.intValue();
    }

    public static long getLong(Object root, String path) {
        Number value = get(root, path);
        return value.longValue();
    }

    public static float getFloat(Object root, String path) {
        Number value = get(root, path);
        return value.floatValue();
    }

    public static double getDouble(Object root, String path) {
        Number value = get(root, path);
        return value.doubleValue();
    }

    /**
     * Sets the value at the given path.
     *
     * @param root
     * @param path
     * @param value
     *
     * @return
     * The value previously associated with the path.
     */
    @SuppressWarnings("unchecked")
    public static <T> T set(Object root, String path, T value) {
        if (root == null) {
            throw new IllegalArgumentException();
        }

        List<String> keys = parsePath(path);
        if (keys.isEmpty()) {
            throw new IllegalArgumentException("Path is empty.");
        }

        String key = keys.remove(keys.size() - 1);
        Object parent = get(root, keys);

        Object previousValue;

        if (parent instanceof Map<?, ?>) {
            Map<String, Object> map = (Map<String, Object>)parent;
            previousValue = map.put(key, value);
        } else if (parent instanceof List<?> && Character.isDigit(key.charAt(0))) {
            List<Object> list = (List<Object>)parent;
            previousValue = list.set(Integer.parseInt(key), value);
        } else {
            if (parent == null) {
                throw new IllegalArgumentException("Invalid path.");
            }

            BeanAdapter beanAdapter = new BeanAdapter(parent);
            previousValue = beanAdapter.put(key, value);
        }

        return (T)previousValue;
    }

    /**
     * Tests the existence of a path in a given object.
     *
     * @param root
     * @param path
     *
     * @return
     * <tt>true</tt> if the path exists; <tt>false</tt>, otherwise.
     */
    @SuppressWarnings("unchecked")
    public static boolean isDefined(Object root, String path) {
        if (root == null) {
            throw new IllegalArgumentException();
        }

        List<String> keys = parsePath(path);
        if (keys.isEmpty()) {
            throw new IllegalArgumentException("Path is empty.");
        }

        String key = keys.remove(keys.size() - 1);
        Object parent = get(root, keys);

        boolean defined;

        if (parent instanceof Map<?, ?>) {
            Map<String, Object> map = (Map<String, Object>)parent;
            defined = map.containsKey(key);
        } else if (parent instanceof List<?> && Character.isDigit(key.charAt(0))) {
            List<Object> list = (List<Object>)parent;
            defined = (list.size() > Integer.parseInt(key));
        } else {
            if (parent == null) {
                defined = false;
            } else {
                BeanAdapter beanAdapter = new BeanAdapter(parent);
                defined = beanAdapter.containsKey(key);
            }
        }

        return defined;
    }

    /**
     * Parses a path into a list of string keys.
     *
     * @param path
     */
    public static List<String> parsePath(String path) {
        if (path == null) {
            throw new IllegalArgumentException();
        }

        List<String> keys = new ArrayList<String>();

        int i = 0;
        int n = path.length();

        while (i < n) {
            char c = path.charAt(i++);

            StringBuilder keyBuilder = new StringBuilder();

            boolean bracketed = (c == '[');
            if (bracketed
                && i < n) {
                c = path.charAt(i++);

                char quote = Character.UNASSIGNED;

                boolean quoted = (c == '"'
                    || c == '\'');
                if (quoted
                    && i < n) {
                    quote = c;
                    c = path.charAt(i++);
                }

                while (i <= n
                    && bracketed) {
                    bracketed = quoted || (c != ']');

                    if (bracketed) {
                        if (c == quote) {
                            if (i < n) {
                                c = path.charAt(i++);
                                quoted = (c == quote);
                            }
                        }

                        if (quoted || c != ']') {
                            if (Character.isISOControl(c)) {
                                throw new IllegalArgumentException("Illegal identifier character.");
                            }

                            keyBuilder.append(c);

                            if (i < n) {
                                c = path.charAt(i++);
                            }
                        }
                    }
                }

                if (quoted) {
                    throw new IllegalArgumentException("Unterminated quoted identifier.");
                }

                if (bracketed) {
                    throw new IllegalArgumentException("Unterminated bracketed identifier.");
                }

                if (i < n) {
                    c = path.charAt(i);

                    if (c == '.') {
                        i++;
                    }
                }
            } else {
                while(i <= n
                    && c != '.'
                    && c != '[') {
                    if (!Character.isJavaIdentifierPart(c)) {
                        throw new IllegalArgumentException("Illegal identifier character.");
                    }

                    keyBuilder.append(c);

                    if (i < n) {
                        c = path.charAt(i);
                    }

                    i++;
                }

                if (c == '[') {
                    i--;
                }
            }

            if (c == '.'
                && i == n) {
                throw new IllegalArgumentException("Path cannot end with a '.' character.");
            }

            if (keyBuilder.length() == 0) {
                throw new IllegalArgumentException("Missing identifier.");
            }

            keys.add(keyBuilder.toString());
        }

        return keys;
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
        if (key == null) {
            throw new IllegalArgumentException();
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
     * The setter method, or <tt>null</tt> if the method does not exist.
     */
    public static Method getSetterMethod(Class<?> beanClass, String key, Class<?> valueType) {
        if (key == null) {
            throw new IllegalArgumentException();
        }

        if (valueType == null) {
            throw new IllegalArgumentException();
        }

        Method setterMethod = null;

        // Upper-case the first letter and prepend the "set" prefix to
        // determine the method name
        key = Character.toUpperCase(key.charAt(0)) + key.substring(1);
        final String methodName = SET_PREFIX + key;

        try {
            setterMethod = beanClass.getMethod(methodName, valueType);
        } catch (NoSuchMethodException exception) {
            // No-op
        }

        return setterMethod;
    }

    public static String toCamelCase(String allCaps) {
        if (allCaps == null) {
            throw new IllegalArgumentException();
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0, n = allCaps.length(); i < n; i++) {
            char c = allCaps.charAt(i);

            if (c == '_' && i < n - 1) {
                c = Character.toUpperCase(allCaps.charAt(++i));
            } else {
                c = Character.toLowerCase(c);
            }

            sb.append(c);
        }

        return sb.toString();
    }

    public static String toAllCaps(String camelCase) {
        if (camelCase == null) {
            throw new IllegalArgumentException();
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0, n = camelCase.length(); i < n; i++) {
            char c = camelCase.charAt(i);

            if (Character.isUpperCase(c)) {
                sb.append('_');
            }

            sb.append(Character.toUpperCase(c));
        }

        return sb.toString();
    }
}
