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

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;

/**
 * Contains utility methods for working with JSON or JSON-like data structures.
 */
public class JSON {
    /**
     * Returns the value at the given path.
     *
     * @param root
     * The root object.
     *
     * @param path
     * The path to the value as a JavaScript path.
     *
     * @return
     * The value at the given path.
     *
     * @see #get(Object, Sequence)
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Object root, String path) {
        if (root == null) {
            throw new IllegalArgumentException("root is null.");
        }

        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        return (T)get(root, parse(path));
    }

    /**
     * Returns the value at a given index along a path.
     *
     * @param root
     * The root object; must be an instance of {@link org.apache.pivot.collections.Map}
     * or {@link org.apache.pivot.collections.List} or a Java bean object.
     *
     * @param keys
     * The path to the value, as a set of keys.
     *
     * @return
     * The value at the given path.
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Object root, Sequence<String> keys) {
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

        return (T)value;
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
    public static <T> T put(Object root, String path, T value) {
        if (root == null) {
            throw new IllegalArgumentException("root is null.");
        }

        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        Object previousValue;

        Sequence<String> keys = parse(path);
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

        return (T)previousValue;
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
    public static <T> T remove(Object root, String path) {
        if (root == null) {
            throw new IllegalArgumentException("root is null.");
        }

        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        Object previousValue;

        Sequence<String> keys = parse(path);
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
    public static boolean containsKey(Object root, String path) {
        if (root == null) {
            throw new IllegalArgumentException("root is null.");
        }

        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        boolean containsKey;

        Sequence<String> keys = parse(path);
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

    /**
     * Parses a JSON path into a sequence of string keys.
     *
     * @param path
     */
    public static Sequence<String> parse(String path) {
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
}
