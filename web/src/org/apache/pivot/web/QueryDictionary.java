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

import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;


/**
 * Represents a collection of keyed data associated with a query. Allows
 * multiple values to be set against a given key.
 */
public final class QueryDictionary implements Dictionary<String, String>, Iterable<String> {
    private boolean caseSensitiveKeys;
    private HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();

    public QueryDictionary(boolean caseSensitiveKeys) {
        this.caseSensitiveKeys = caseSensitiveKeys;
    }

    @Override
    public String get(final String key) {
        String searchKey = key;
        if (!caseSensitiveKeys) {
            searchKey = searchKey.toLowerCase();
        }

        ArrayList<String> list = map.get(searchKey);
        if (list != null && list.getLength() > 0) {
            return list.get(0);
        }

        return null;
    }

    public String get(final String key, int index) {
        String searchKey = key;
        if (!caseSensitiveKeys) {
            searchKey = searchKey.toLowerCase();
        }

        ArrayList<String> list = map.get(searchKey);
        if (list == null || list.getLength() <= index) {
            throw new IndexOutOfBoundsException();
        }

        return list.get(index);
    }

    @Override
    public String put(final String key, final String value) {
        String searchKey = key;
        if (!caseSensitiveKeys) {
            searchKey = searchKey.toLowerCase();
        }

        ArrayList<String> list = new ArrayList<String>();
        list.add(value);

        ArrayList<String> previous = map.put(searchKey, list);
        if (previous != null && previous.getLength() > 0) {
            return previous.get(0);
        }

        return null;
    }

    public int add(final String key, final String value) {
        String searchKey = key;
        if (!caseSensitiveKeys) {
            searchKey = searchKey.toLowerCase();
        }

        ArrayList<String> list = map.get(searchKey);
        if (list == null) {
            put(searchKey, value);
            return 0;
        }

        list.add(value);
        return list.getLength() - 1;
    }

    public void insert(final String key, final String value, int index) {
        String searchKey = key;
        if (!caseSensitiveKeys) {
            searchKey = searchKey.toLowerCase();
        }

        ArrayList<String> list = map.get(searchKey);

        // e.g if index = 0 and length = 0, throw an exception
        if (list == null || list.getLength() <= index) {
            throw new IndexOutOfBoundsException();
        }

        list.insert(value, index);
    }

    @Override
    public String remove(final String key) {
        String searchKey = key;
        if (!caseSensitiveKeys) {
            searchKey = searchKey.toLowerCase();
        }

        ArrayList<String> list = map.remove(searchKey);
        if (list != null && list.getLength() > 0) {
            return list.get(0);
        }

        return null;
    }

    public String remove(final String key, int index) {
        String searchKey = key;
        if (!caseSensitiveKeys) {
            searchKey = searchKey.toLowerCase();
        }

        ArrayList<String> list = map.get(searchKey);
        if (list == null || list.getLength() <= index) {
            throw new IndexOutOfBoundsException();
        }

        return list.get(index);
    }

    public void clear() {
        map.clear();
    }

    @Override
    public boolean containsKey(final String key) {
        String searchKey = key;
        if (!caseSensitiveKeys) {
            searchKey = searchKey.toLowerCase();
        }

        return map.containsKey(searchKey);
    }


    public int getLength(final String key) {
        String searchKey = key;
        if (!caseSensitiveKeys) {
            searchKey = searchKey.toLowerCase();
        }

        ArrayList<String> list = map.get(searchKey);
        if (list == null) {
            return 0;
        }
        return list.getLength();
    }

    @Override
    public Iterator<String> iterator() {
        return map.iterator();
    }
}
