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
package org.apache.pivot.json;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;

/**
 * Contains utility methods for working with JSON or JSON-like data structures.
 */
public class JSON {
    /**
     * Returns the value at the given path.
     *
     * @param root
     * The root object; must be an instance of {@link org.apache.pivot.collections.Map}
     * or {@link org.apache.pivot.collections.List}.
     *
     * @param path
     * The path to the value, in JavaScript path notation.
     *
     * @return
     * The value at the given path.
     */
    public static Object get(Object root, String path) {
        if (root == null) {
            throw new IllegalArgumentException("root is null.");
        }

        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        return get(root, split(path));
    }

    @SuppressWarnings("unchecked")
    private static Object get(Object root, Sequence<String> keys) {
        Object value = root;

        for (int i = 0, n = keys.getLength(); i < n; i++) {
            String key = keys.get(i);

            if (value instanceof Sequence<?>) {
                Sequence<Object> sequence = (Sequence<Object>)value;
                value = sequence.get(Integer.parseInt(key));
            } else {
                Dictionary<String, Object> dictionary;
                if (value instanceof Dictionary<?, ?>) {
                    dictionary = (Dictionary<String, Object>)value;
                    value = dictionary.get(key);
                } else {
                    dictionary = new BeanAdapter(value);
                }

                if (dictionary.containsKey(key)) {
                    value = dictionary.get(key);
                } else {
                    value = null;
                    break;
                }
            }
        }

        return value;
    }

    /**
     * Returns the value at the given path as a string.
     *
     * @param root
     * @param path
     *
     * @see #get(Object, String)
     */
    public static String getString(Object root, String path) {
        return (String)get(root, path);
    }

    /**
     * Returns the value at the given path as a number.
     *
     * @param root
     * @param path
     *
     * @see #get(Object, String)
     */
    public static Number getNumber(Object root, String path) {
        return (Number)get(root, path);
    }

    /**
     * Returns the value at the given path as a short.
     *
     * @param root
     * @param path
     *
     * @see #get(Object, String)
     */
    public static Short getShort(Object root, String path) {
        Number number = getNumber(root, path);
        return (number == null) ? null : number.shortValue();
    }

    /**
     * Returns the value at the given path as an integer.
     *
     * @param root
     * @param path
     *
     * @see #get(Object, String)
     */
    public static Integer getInteger(Object root, String path) {
        Number number = getNumber(root, path);
        return (number == null) ? null : number.intValue();
    }

    /**
     * Returns the value at the given path as a long.
     *
     * @param root
     * @param path
     *
     * @see #get(Object, String)
     */
    public static Long getLong(Object root, String path) {
        Number number = getNumber(root, path);
        return (number == null) ? null : number.longValue();
    }

    /**
     * Returns the value at the given path as a float.
     *
     * @param root
     * @param path
     *
     * @see #get(Object, String)
     */
    public static Float getFloat(Object root, String path) {
        Number number = getNumber(root, path);
        return (number == null) ? null : number.floatValue();
    }

    /**
     * Returns the value at the given path as a double.
     *
     * @param root
     * @param path
     *
     * @see #get(Object, String)
     */
    public static Double getDouble(Object root, String path) {
        Number number = getNumber(root, path);
        return (number == null) ? null : number.doubleValue();
    }

    /**
     * Returns the value at the given path as a boolean.
     *
     * @param root
     * @param path
     *
     * @see #get(Object, String)
     */
    public static Boolean getBoolean(Object root, String path) {
        return (Boolean)get(root, path);
    }

    /**
     * Returns the value at the given path as a list.
     *
     * @param root
     * @param path
     *
     * @see #get(Object, String)
     */
    public static List<?> getList(Object root, String path) {
        return (List<?>)get(root, path);
    }

    /**
     * Returns the value at the given path as a map.
     *
     * @param root
     * @param path
     *
     * @see #get(Object, String)
     */
    @SuppressWarnings("unchecked")
    public static Map<String, ?> getMap(Object root, String path) {
        return (Map<String, ?>)get(root, path);
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
    public static Object put(Object root, String path, Object value) {
        if (root == null) {
            throw new IllegalArgumentException("root is null.");
        }

        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        Object previousValue;

        Sequence<String> keys = split(path);
        if (keys.getLength() == 0) {
            throw new IllegalArgumentException("Bad path.");
        }

        String key = keys.remove(keys.getLength() - 1, 1).get(0);

        Object parent = get(root, keys);
        if (parent instanceof Sequence<?>) {
            Sequence<Object> sequence = (Sequence<Object>)parent;
            previousValue = sequence.update(Integer.parseInt(key), value);
        } else {
            Dictionary<String, Object> dictionary;
            if (parent instanceof Dictionary<?, ?>) {
                dictionary = (Dictionary<String, Object>)parent;
            } else {
                dictionary = new BeanAdapter(parent);
            }

            previousValue = dictionary.put(key, value);
        }

        return previousValue;
    }

    /**
     * Removes the value at the given path.
     *
     * @param root
     * @param path
     *
     * @return
     * The value that was removed.
     */
    @SuppressWarnings("unchecked")
    public static Object remove(Object root, String path) {
        if (root == null) {
            throw new IllegalArgumentException("root is null.");
        }

        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        Object previousValue;

        Sequence<String> keys = split(path);
        if (keys.getLength() == 0) {
            throw new IllegalArgumentException("Bad path.");
        }

        String key = keys.remove(keys.getLength() - 1, 1).get(0);

        Object parent = get(root, keys);
        if (parent instanceof Sequence<?>) {
            Sequence<Object> sequence = (Sequence<Object>)parent;
            previousValue = sequence.remove(Integer.parseInt(key), 1).get(0);
        } else {
            Dictionary<String, Object> dictionary;
            if (parent instanceof Dictionary<?, ?>) {
                dictionary = (Dictionary<String, Object>)parent;
            } else {
                dictionary = new BeanAdapter(parent);
            }

            previousValue = dictionary.remove(key);
        }

        return previousValue;
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
    public static boolean containsKey(Object root, String path) {
        if (root == null) {
            throw new IllegalArgumentException("root is null.");
        }

        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        boolean containsKey;

        Sequence<String> keys = split(path);
        if (keys.getLength() == 0) {
            throw new IllegalArgumentException("Bad path.");
        }

        String key = keys.remove(keys.getLength() - 1, 1).get(0);

        Object parent = get(root, keys);
        if (parent instanceof Sequence<?>) {
            Sequence<Object> sequence = (Sequence<Object>)parent;
            containsKey = (sequence.getLength() > Integer.parseInt(key));
        } else {
            Dictionary<String, Object> dictionary;
            if (parent instanceof Dictionary<?, ?>) {
                dictionary = (Dictionary<String, Object>)parent;
            } else {
                dictionary = new BeanAdapter(parent);
            }

            containsKey = dictionary.containsKey(key);
        }

        return containsKey;
    }

    private static Sequence<String> split(String path) {
        ArrayList<String> keys = new ArrayList<String>();

        int i = 0;
        int n = path.length();

        while (i < n) {
            char c = path.charAt(i++);

            StringBuilder identifierBuilder = new StringBuilder();

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

                            identifierBuilder.append(c);

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

                    identifierBuilder.append(c);

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

            if (identifierBuilder.length() == 0) {
                throw new IllegalArgumentException("Missing identifier.");
            }

            keys.add(identifierBuilder.toString());
        }

        return keys;
    }

    /**
     * Binds a JSON value to a Java type.
     *
     * @param source
     * The source value.
     *
     * @param type
     * The Java class to which to which the value will be bound.
     *
     * @return
     * The bound instance of the Java type.
     */
    @SuppressWarnings("unchecked")
    public static <T> T bind(Object source, Class<? extends T> targetType) {
        return (T)bind(source, (Type)targetType);
    }

    @SuppressWarnings("unchecked")
    private static <T> T bind(Object source, Type targetType) {
        if (targetType == null) {
            throw new IllegalArgumentException();
        }

        Object target;
        if (source == null) {
            target = null;
        } else if (source instanceof Boolean
            || source instanceof Number
            || source instanceof String) {
            if (!(targetType instanceof Class<?>)) {
                throw new IllegalArgumentException(source + " cannot be bound to " + targetType + ".");
            }

            target = BeanAdapter.coerce(source, (Class<?>)targetType);
        } else if (source instanceof List<?>) {
            List<?> sourceList = (List<?>)source;

            if (targetType instanceof ParameterizedType) {
                // Instantiate the target sequence
                ParameterizedType parameterizedType = (ParameterizedType)targetType;
                Class<?> rawType = (Class<?>)parameterizedType.getRawType();
                if (!Sequence.class.isAssignableFrom(rawType)) {
                    throw new IllegalArgumentException(source + " cannot be bound to "
                        + rawType.getName() + ".");
                }

                try {
                    target = rawType.newInstance();
                } catch (InstantiationException exception) {
                    throw new RuntimeException(exception);
                } catch (IllegalAccessException exception) {
                    throw new RuntimeException(exception);
                }

                Sequence<Object> targetSequence = (Sequence<Object>)target;

                // Get the target item type
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                Type targetItemType = actualTypeArguments[0];

                // Populate the sequence
                for (Object item : sourceList) {
                    targetSequence.add(bind(item, targetItemType));
                }
            } else {
                Class<?> targetClass = (Class<?>)targetType;
                if (!targetClass.isArray()) {
                    throw new IllegalArgumentException(source + " cannot be bound to "
                        + targetClass.getName() + ".");
                }

                // Instantiate the target array
                Class<?> targetComponentType = targetClass.getComponentType();
                target = Array.newInstance(targetComponentType, sourceList.getLength());

                // Populate the array
                int i = 0;
                for (Object item : sourceList) {
                    Array.set(target, i++, bind(item, targetComponentType));
                }
            }
        } else if (source instanceof Map<?, ?>) {
            Map<String, ?> sourceMap = (Map<String, ?>)source;

            if (targetType instanceof ParameterizedType) {
                // Instantiate the target dictionary
                ParameterizedType parameterizedType = (ParameterizedType)targetType;
                Class<?> rawType = (Class<?>)parameterizedType.getRawType();
                if (!Dictionary.class.isAssignableFrom(rawType)) {
                    throw new IllegalArgumentException(source + " cannot be bound to "
                        + rawType.getName() + ".");
                }

                try {
                    target = rawType.newInstance();
                } catch (InstantiationException exception) {
                    throw new RuntimeException(exception);
                } catch (IllegalAccessException exception) {
                    throw new RuntimeException(exception);
                }

                Dictionary<String, Object> targetDictionary = (Dictionary<String, Object>)target;

                // Get the target value type
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                Type targetValueType = actualTypeArguments[1];

                // Populate the dictionary
                for (String key : sourceMap) {
                    targetDictionary.put(key, bind(sourceMap.get(key), targetValueType));
                }
            } else {
                Class<?> targetClass = (Class<?>)targetType;

                try {
                    target = targetClass.newInstance();
                } catch (InstantiationException exception) {
                    throw new RuntimeException(exception);
                } catch (IllegalAccessException exception) {
                    throw new RuntimeException(exception);
                }

                BeanAdapter targetAdapter = new BeanAdapter(target);

                for (String key : sourceMap) {
                    targetAdapter.put(key, bind(sourceMap.get(key), targetAdapter.getGenericType(key)));
                }
            }
        } else {
            throw new IllegalArgumentException(source.getClass() + " is not a supported source type.");
        }

        return (T)target;
    }
}
