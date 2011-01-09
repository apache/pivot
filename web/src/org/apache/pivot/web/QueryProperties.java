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
package org.apache.pivot.web;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Represents a collection of keyed data associated with a query. Keys are
 * optionally case sensitive, and multiple values may be set for a given
 * key.
 */
public final class QueryProperties extends AbstractMap<String, String> {
    /**
     * Represents a property key/value pair.
     */
    private class PropertyEntry implements Map.Entry<String, String> {
        private String key;

        public PropertyEntry(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return get(key);
        }

        @Override
        public String setValue(String value) {
            return put(key, value);
        }
    }

    /**
     * Properties entry set.
     */
    private class PropertyEntrySet extends AbstractSet<Map.Entry<String, String>> {
        /**
         * Property entry iterator. Returns a key/value pair for each property
         * defined by the dictionary.
         */
        private class PropertyEntryIterator implements Iterator<Map.Entry<String, String>> {
            private Iterator<String> keyIterator = properties.keySet().iterator();
            private String key = null;

            @Override
            public boolean hasNext() {
                return keyIterator.hasNext();
            }

            @Override
            public Map.Entry<String, String> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                key = keyIterator.next();
                return new PropertyEntry(key);
            }

            public void remove() {
                if (key == null) {
                    throw new IllegalStateException();
                }

                QueryProperties.this.remove(key);
            }
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            return new PropertyEntryIterator();
        }
    }

    private boolean caseSensitive;
    private Map<String, List<String>> properties = new HashMap<String, List<String>>();
    private PropertyEntrySet entrySet = new PropertyEntrySet();

    public QueryProperties(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    @Override
    public String get(Object key) {
        return get(key.toString());
    }

    public String get(String key) {
        if (!caseSensitive) {
            key = key.toLowerCase();
        }

        List<String> values = properties.get(key);

        return (values == null) ? null : join(values);
    }

    public String get(String key, int index) {
        if (!caseSensitive) {
            key = key.toLowerCase();
        }

        List<String> values = properties.get(key);

        return (values == null) ? null : values.get(index);
    }

    @Override
    public String put(String key, String value) {
        if (!caseSensitive) {
            key = key.toLowerCase();
        }

        List<String> values = properties.get(key);
        if (values == null) {
            values = new ArrayList<String>();
            properties.put(key, values);
        }

        values.add(value);

        return null;
    }

    @Override
    public String remove(Object key) {
        return remove(key.toString());
    }

    public String remove(String key) {
        if (!caseSensitive) {
            key = key.toLowerCase();
        }

        properties.remove(key);

        return null;
    }

    public String remove(String key, int index) {
        if (!caseSensitive) {
            key = key.toLowerCase();
        }

        List<String> values = properties.get(key);
        if (values != null) {
            values.remove(index);
        }

        return null;
    }

    @Override
    public void clear() {
        properties.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return containsKey(key.toString());
    }

    public boolean containsKey(String key) {
        if (!caseSensitive) {
            key = key.toLowerCase();
        }

        return properties.containsKey(key);
    }

    public int size(String key) {
        if (!caseSensitive) {
            key = key.toLowerCase();
        }

        List<String> values = properties.get(key);

        return (values == null) ? 0 : values.size();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return entrySet;
    }

    private static String join(List<?> list) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0, n = list.size(); i < n; i++) {
            Object item = list.get(i);
            sb.append((item == null) ? null : item.toString());

            if (i < n - 1) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }
}
