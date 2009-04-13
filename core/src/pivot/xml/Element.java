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
package pivot.xml;

import java.util.Comparator;
import java.util.Iterator;

import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ListenerList;

/**
 * Class representing an XML element.
 * <p>
 * NOTE This class is incomplete.
 * <p>
 * TODO Adding or updating a child element should throw if the element's
 * namespace is not defined.
 *
 * @author gbrown
 */
public class Element implements List<Element>, Dictionary<String, String> {
    public class NamespaceDictionary implements Dictionary<String, String> {
        public String get(String prefix) {
            // TODO
            return null;
        }

        public String put(String prefix, String uri) {
            // TODO
            return null;
        }

        public String remove(String prefix) {
            // TODO
            return null;
        }

        public boolean containsKey(String prefix) {
            // TODO
            return false;
        }

        public boolean isEmpty() {
            // TODO
            return true;
        }
    }

    public static class Name {
        public String getNamespacePrefix() {
            // TODO
            return null;
        }

        public String getLocalName() {
            // TODO
            return null;
        }
    }


    public Name getName() {
        // TODO
        return null;
    }

    public void setName(Name name) {
        // TODO
    }

    public Element getParent() {
        // TODO
        return null;
    }

    protected void setParent(Element element) {
        // TODO
    }

    public NamespaceDictionary getNamespaces() {
        // TODO
        return null;
    }

    public int add(Element element) {
        // TODO
        return -1;
    }

    public void insert(Element element, int index) {
        // TODO
    }

    public Element update(int index, Element element) {
        // TODO
        return null;
    }

    public int remove(Element element) {
        // TODO
        return -1;
    }

    public Sequence<Element> remove(int index, int count) {
        // TODO
        return null;
    }

    public void clear() {
        // TODO
    }

    public Element get(int index) {
        // TODO
        return null;
    }

    public int indexOf(Element element) {
        // TODO
        return -1;
    }

    public int getLength() {
        // TODO
        return 0;
    }

    public String get(String attribute) {
        // TODO
        return null;
    }

    public String put(String attribute, String value) {
        // TODO
        return null;
    }

    public String remove(String attribute) {
        // TODO
        return null;
    }

    public boolean containsKey(String attribute) {
        // TODO
        return false;
    }

    public boolean isEmpty() {
        // TODO
        return true;
    }

    public String getAttribute(int index) {
        // TODO
        return null;
    }

    public int getAttributeCount() {
        // TODO
        return 0;
    }

    public Comparator<Element> getComparator() {
        return null;
    }

    public void setComparator(Comparator<Element> comparator) {
        throw new UnsupportedOperationException();
    }

    public Iterator<Element> iterator() {
        return null;
    }

    public ListenerList<ListListener<Element>> getListListeners() {
        // TODO
        return null;
    }
}