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
package pivot.wtkx;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Comparator;
import java.util.Iterator;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.serialization.Serializer;
import pivot.serialization.SerializationException;
import pivot.util.ListenerList;

public class WTKXSerializer implements Serializer {
    public static class Element implements Dictionary<String, Object>, List<Object> {
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

    private URL location = null;
    private Dictionary<String, ?> resources = null;

    private HashMap<String, Object> namedObjects = new HashMap<String, Object>();
    private HashMap<String, WTKXSerializer> includeSerializers = new HashMap<String, WTKXSerializer>();

    public static final String URL_PREFIX = "@";
    public static final String RESOURCE_KEY_PREFIX = "%";
    public static final String OBJECT_REFERENCE_PREFIX = "$";

    public static final String ID_ATTRIBUTE = "wtkx:id";
    public static final String OUTER_ATTRIBUTE = "wtkx:outer";

    public static final String INCLUDE_TAG = "wtkx:include";
    public static final String INCLUDE_SRC_ATTRIBUTE = "src";
    public static final String INCLUDE_NAMESPACE_ATTRIBUTE = "namespace";

    public static final String MIME_TYPE = "application/wtkx";

    public WTKXSerializer() {
        this(null);
    }

    public WTKXSerializer(Dictionary<String, ?> resources) {
        this.resources = resources;
    }

    public Object readObject(String resourceName)
        throws IOException, SerializationException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL location = classLoader.getResource(resourceName);

        if (location == null) {
            throw new SerializationException("Could not find resource named \""
                + resourceName + "\".");
        }

        return readObject(location);
    }

    public Object readObject(URL location)
        throws IOException, SerializationException {
        this.location = location;

        return readObject(new BufferedInputStream(location.openStream()));
    }

    public Object readObject(InputStream inputStream)
        throws IOException, SerializationException {
        Object object = null;

        // TODO

        // Clear the location so any previous value won't be re-used in a
        // subsequent call to this method
        location = null;

        return object;
    }

    public void writeObject(Object object, OutputStream outputStream)
        throws IOException, SerializationException {
        throw new UnsupportedOperationException();
    }

    public String getMIMEType() {
        return MIME_TYPE;
    }

    /**
     * Retrieves a named object.
     *
     * @param name
     * The name of the object, relative to this loader. The values's name is
     * the concatentation of its parent namespaces and its ID, separated by
     * periods (e.g. "foo.bar.baz").
     *
     * @return
     * The named object, or <tt>null</tt> if an object with the given name does
     * not exist.
     */
    public Object getObjectByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null.");
        }

        Object object = null;
        WTKXSerializer serializer = this;
        String[] namespacePath = name.split("\\.");

        int i = 0;
        int n = namespacePath.length - 1;
        while (i < n
            && serializer != null) {
            String namespace = namespacePath[i++];
            serializer = serializer.includeSerializers.get(namespace);
        }

        if (serializer != null) {
            object = serializer.namedObjects.get(namespacePath[i]);
        }

        return object;
    }
}
