/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtkx2;

import java.util.Comparator;
import java.util.Iterator;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ListenerList;

public class Element implements Dictionary<String, Object>, List<Object> {
    private HashMap<String, Object> dictionary = new HashMap<String, Object>();
    private ArrayList<Object> list = new ArrayList<Object>();

    private ListListenerList<Object> listListeners = new ListListenerList<Object>();

    // Dictionary methods
    public Object get(String key) {
        return dictionary.get(key);
    }

    public Object put(String key, Object value) {
        return dictionary.put(key, value);
    }

    public Object remove(String key) {
        return dictionary.remove(key);
    }

    public boolean containsKey(String key) {
        return dictionary.containsKey(key);
    }

    public boolean isEmpty() {
        return dictionary.isEmpty();
    }

    public Iterator<String> getKeys() {
        return dictionary.iterator();
    }

    // List methods
    public int add(Object item) {
        int index = getLength();
        insert(item, index);

        return index;
    }

    public void insert(Object item, int index) {
        // TODO Auto-generated method stub

    }

    public int remove(Object item) {
        int index = indexOf(item);
        remove(index, 1);

        return index;
    }

    public Sequence<Object> remove(int index, int count) {
        // TODO Auto-generated method stub
        return null;
    }

    public void clear() {
        // TODO Auto-generated method stub

    }

    public Object update(int index, Object item) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object get(int index) {
        return list.get(index);
    }

    public int indexOf(Object item) {
        return list.indexOf(item);
    }

    public int getLength() {
        return list.getLength();
    }

    public Comparator<Object> getComparator() {
        return list.getComparator();
    }

    public void setComparator(Comparator<Object> comparator) {
        // TODO Auto-generated method stub

    }

    public Iterator<Object> iterator() {
        // TODO Wrap in private iterator so we can prevent removals
        return list.iterator();
    }

    public ListenerList<ListListener<Object>> getListListeners() {
        return listListeners;
    }
}
