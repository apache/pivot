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
package pivot.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import pivot.collections.HashMap;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ListenerList;

/**
 * Wraps a {@link java.sql.ResultSet} in a list collection.
 * <p>
 * NOTE This class is incomplete.
 * <p>
 * TODO Add a forwardOnly flag; if true, we'll only support iteration; this mode
 * will be most efficient for serialization from a server. Otherwise, we'll
 * navigate through the list on-demand and cache the results in an internal
 * list. This mode will be useful for presenting result set contents in a
 * list or table view.
 *
 * @author gbrown
 */
public class ResultSetAdapter implements List<Object> {
    private class ResultSetIterator implements Iterator<Object> {
        public boolean hasNext() {
            boolean hasNext;
            try {
                hasNext = resultSet.isAfterLast();
            } catch(SQLException exception) {
                throw new RuntimeException(exception);
            }

            return hasNext;
        }

        public Object next() {
            Object next;

            try {
                // Advance to the next row
                if (!resultSet.next()) {
                    throw new NoSuchElementException();
                }

                next = new HashMap<String, Object>();

                // TODO Extract row contents into map
            } catch(SQLException exception) {
                throw new RuntimeException(exception);
            }

            return next;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Represents a field in the result list.
     *
     * @author gbrown
     */
    public static final class Field {
        /**
         * The key that will be used to represent the field in the row map.
         */
        public final String key;

        /**
         * The zero-based column index of the source column.
         */
        public final int columnIndex;

        /**
         * The type
         */
        public final Class<?> type;

        public Field(String key, int columnIndex, Class<?> type) {
            if (key == null
                || columnIndex < 0
                || type == null) {
                throw new IllegalArgumentException();
            }

            this.key = key;
            this.columnIndex = columnIndex;
            this.type = type;
        }
    }

    private ResultSet resultSet;
    private ResultSetIterator resultSetIterator = new ResultSetIterator();

    private ListListenerList<Object> listListeners = new ListListenerList<Object>();

    public ResultSetAdapter(ResultSet resultSet) {
        if (resultSet == null) {
            throw new IllegalArgumentException();
        }

        this.resultSet = resultSet;
    }

    public int add(Object item) {
        throw new UnsupportedOperationException();
    }

    public void insert(Object item, int index) {
        throw new UnsupportedOperationException();
    }

    public int remove(Object item) {
        throw new UnsupportedOperationException();
    }

    public Sequence<Object> remove(int index, int count) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public Object update(int index, Object item) {
        throw new UnsupportedOperationException();
    }

    public Object get(int index) {
        // TODO
        return null;
    }

    public int indexOf(Object item) {
        // TODO
        return -1;
    }

    public int getLength() {
        // TODO
        return -1;
    }

    public Comparator<Object> getComparator() {
        return null;
    }

    public void setComparator(Comparator<Object> comparator) {
        throw new UnsupportedOperationException();
    }

    public Iterator<Object> iterator() {
        // TODO This may be a read-only iterator on an internal list
        return resultSetIterator;
    }

    public ListenerList<ListListener<Object>> getListListeners() {
        return listListeners;
    }
}
