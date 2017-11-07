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
import org.apache.pivot.collections.Sequence;

/**
 * Represents a collection of keyed data associated with a query. Allows
 * multiple values to be set against a given key.
 */
public final class QueryDictionary implements Dictionary<String, String>, Iterable<String> {
    private boolean caseSensitiveKeys;
    private HashMap<String, ArrayList<String>> map = new HashMap<>();

    public QueryDictionary(boolean caseSensitiveKeys) {
        this.caseSensitiveKeys = caseSensitiveKeys;
    }

    private String getSearchKey(String key) {
        return caseSensitiveKeys ? key : key.toLowerCase();
    }

    private ArrayList<String> getListCheckIndex(String key, int index) {
        String searchKey = getSearchKey(key);

        ArrayList<String> list = map.get(searchKey);
        // e.g if index = 0 and length = 0, throw an exception
        if (list == null || list.getLength() <= index) {
            throw new IndexOutOfBoundsException("No list for search key or list length <= index " + index);
        }

        return list;
    }

    @Override
    public String get(final String key) {
        String searchKey = getSearchKey(key);

        ArrayList<String> list = map.get(searchKey);
        if (list != null && list.getLength() > 0) {
            return list.get(0);
        }

        return null;
    }

    public String get(final String key, int index) {
        ArrayList<String> list = getListCheckIndex(key, index);

        return list.get(index);
    }

    @Override
    public String put(final String key, final String value) {
        String searchKey = getSearchKey(key);

        ArrayList<String> list = new ArrayList<>();
        list.add(value);

        ArrayList<String> previous = map.put(searchKey, list);
        if (previous != null && previous.getLength() > 0) {
            return previous.get(0);
        }

        return null;
    }

    public int add(final String key, final String value) {
        String searchKey = getSearchKey(key);

        ArrayList<String> list = map.get(searchKey);
        if (list == null) {
            put(searchKey, value);
            return 0;
        }

        list.add(value);
        return list.getLength() - 1;
    }

    public void insert(final String key, final String value, int index) {
        ArrayList<String> list = getListCheckIndex(key, index);

        list.insert(value, index);
    }

    @Override
    public String remove(final String key) {
        String searchKey = getSearchKey(key);

        ArrayList<String> list = map.remove(searchKey);
        if (list != null && list.getLength() > 0) {
            return list.get(0);
        }

        return null;
    }

    public String remove(final String key, int index) {
        ArrayList<String> list = getListCheckIndex(key, index);

        Sequence<String> removed = list.remove(index, 1);
        if (removed != null && removed.getLength() > 0) {
            return removed.get(0);
        }

        return null;
    }

    public void clear() {
        map.clear();
    }

    @Override
    public boolean containsKey(final String key) {
        String searchKey = getSearchKey(key);

        return map.containsKey(searchKey);
    }

    public int getLength(final String key) {
        String searchKey = getSearchKey(key);

        ArrayList<String> list = map.get(searchKey);
        return (list == null) ? 0 : list.getLength();
    }

    @Override
    public Iterator<String> iterator() {
        return map.iterator();
    }
}
