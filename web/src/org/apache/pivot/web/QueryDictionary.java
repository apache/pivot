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
    private HashMap<String, ArrayList<String>> map =  new HashMap<String, ArrayList<String>>();

    @Override
    public String get(String key) {
        ArrayList<String> list = map.get(key);
        if (list != null && list.getLength() > 0) {
            return list.get(0);
        }
        return null;
    }

    public String get(String key, int index) {
        ArrayList<String> list = map.get(key);
        if (list == null || list.getLength() <= index) {
            throw new IndexOutOfBoundsException();
        }
        return list.get(index);
    }

    @Override
    public String put(String key, String value) {
        ArrayList<String> list = new ArrayList<String>();
        list.add(value);

        ArrayList<String> previous = map.put(key, list);
        if (previous != null && previous.getLength() > 0) {
            return previous.get(0);
        }

        return null;
    }

    public int add(String key, String value) {
        ArrayList<String> list = map.get(key);
        if (list == null) {
            put(key, value);
            return 0;
        }

        list.add(value);
        return list.getLength() - 1;
    }

    public void insert(String key, String value, int index) {
        ArrayList<String> list = map.get(key);

        // e.g if index = 0 and length = 0, throw an exception
        if (list == null || list.getLength() <= index) {
            throw new IndexOutOfBoundsException();
        }

        list.insert(value, index);
    }

    @Override
    public String remove(String key) {
        ArrayList<String> list = map.remove(key);
        if (list != null && list.getLength() > 0) {
            return list.get(0);
        }

        return null;
    }

    public String remove(String key, int index) {
        ArrayList<String> list = map.get(key);
        if (list == null || list.getLength() <= index) {
            throw new IndexOutOfBoundsException();
        }
        return list.get(index);
    }

    public void clear() {
        map.clear();
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int getLength(String key) {
        ArrayList<String> list = map.get(key);
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
