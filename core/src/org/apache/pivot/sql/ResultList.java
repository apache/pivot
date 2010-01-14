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
package org.apache.pivot.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;

/**
 * Implementation of the {@link List} interface that is backed by a
 * instance of {@link java.sql.ResultSet}.
 * <p>
 * Note that this list is not suitable for random access and can only be
 * navigated via an iterator.
 */
public class ResultList implements List<Map<String, Object>> {
    /**
     * Class that maps a result set column to a map key/value pair.
     */
    public static final class Field {
        /**
         * The source column name.
         */
        public final String columnName;

        /**
         * The name of the map key. If <tt>null</tt>, the column name will be used.
         */
        public final String key;

        /**
         * The type of the map value. If <tt>null</tt>, the default SQL type will be used.
         */
        public final Class<?> type;

        public Field(String columnName) {
            this(columnName, null, null);
        }

        public Field(String columnName, String key) {
            this(columnName, key, null);
        }

        public Field(String columnName, String key, Class<?> type) {
            this.columnName = columnName;
            this.key = key;
            this.type = type;
        }
    }

    private class ResultListItemIterator implements ItemIterator<Map<String, Object>> {
        @Override
        public boolean hasNext() {
            boolean hasNext;

            try {
                hasNext = !resultSet.isLast();
            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }

            return hasNext;
        }

        @Override
        public Map<String, Object> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            try {
                resultSet.next();
            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }

            return current();
        }

        @Override
        public boolean hasPrevious() {
            boolean hasPrevious;

            try {
                hasPrevious = !resultSet.isFirst();
            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }

            return hasPrevious;
        }

        @Override
        public Map<String, Object> previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }

            try {
                resultSet.previous();
            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }

            return current();
        }

        private Map<String, Object> current() {
            HashMap<String, Object> current = new HashMap<String, Object>();

            try {
                for (Field field : fields) {
                    Object value;

                    if (field.type == Boolean.class
                        || field.type == Boolean.TYPE) {
                        value = resultSet.getBoolean(field.columnName);
                    } else if (field.type == Byte.class
                        || field.type == Byte.TYPE) {
                        value = resultSet.getByte(field.columnName);
                    } else if (field.type == Short.class
                        || field.type == Short.TYPE) {
                        value = resultSet.getShort(field.columnName);
                    } else if (field.type == Integer.class
                        || field.type == Integer.TYPE) {
                        value = resultSet.getInt(field.columnName);
                    } else if (field.type == Long.class
                        || field.type == Long.TYPE) {
                        value = resultSet.getLong(field.columnName);
                    } else if (field.type == Float.class
                        || field.type == Float.TYPE) {
                        value = resultSet.getFloat(field.columnName);
                    } else if (field.type == Double.class
                        || field.type == Double.TYPE) {
                        value = resultSet.getDouble(field.columnName);
                    } else if (field.type == String.class) {
                        value = resultSet.getString(field.columnName);
                    } else {
                        value = resultSet.getObject(field.columnName);
                    }

                    if (resultSet.wasNull()) {
                        value = null;
                    }

                    String key = (field.key == null) ? field.columnName : field.key;
                    current.put(key, value);
                }
            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }

            return current;
        }

        @Override
        public void toStart() {
            try {
                resultSet.beforeFirst();
            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        }

        @Override
        public void toEnd() {
            try {
                resultSet.afterLast();
            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        }

        @Override
        public void insert(Map<String, Object> item) {
            // TODO
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(Map<String, Object> item) {
            // TODO
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove() {
            try {
                resultSet.deleteRow();
            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    private ResultSet resultSet;
    private ArrayList<Field> fields;

    private ListListenerList<Map<String, Object>> listListeners =
        new ListListenerList<Map<String,Object>>();

    public ResultList(ResultSet resultSet, Field... fields) {
        this(resultSet, new ArrayList<Field>(fields));
    }

    public ResultList(ResultSet resultSet, Sequence<Field> fields) {
        this(resultSet, new ArrayList<Field>(fields));
    }

    private ResultList(ResultSet resultSet, ArrayList<Field> fields) {
        if (resultSet == null) {
            throw new IllegalArgumentException();
        }

        for (Field field : fields) {
            if (field.columnName == null) {
                throw new IllegalArgumentException("columnName is required.");
            }

            if (!(field.type == null
                || field.type == Boolean.class
                || field.type == Boolean.TYPE
                || field.type == Byte.class
                || field.type == Byte.TYPE
                || field.type == Short.class
                || field.type == Short.TYPE
                || field.type == Integer.class
                || field.type == Integer.TYPE
                || field.type == Long.class
                || field.type == Long.TYPE
                || field.type == Float.class
                || field.type == Float.TYPE
                || field.type == Double.class
                || field.type == Double.TYPE
                || field.type == String.class)) {
                throw new IllegalArgumentException(field.type.getName()
                    + " is not a supported type.");
            }
        }

        this.resultSet = resultSet;
        this.fields = fields;
    }


    public Field getField(int index) {
        return fields.get(index);
    }

    public int getFieldCount() {
        return fields.getLength();
    }

    @Override
    public int add(Map<String, Object> item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insert(Map<String, Object> item, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> update(int index, Map<String, Object> item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int remove(Map<String, Object> item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Sequence<Map<String, Object>> remove(int index, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> get(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Map<String, Object> item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLength() {
        return -1;
    }

    @Override
    public Comparator<Map<String, Object>> getComparator() {
        return null;
    }

    @Override
    public void setComparator(Comparator<Map<String, Object>> comparator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemIterator<Map<String, Object>> iterator() {
        return new ResultListItemIterator();
    }

    @Override
    public ListenerList<ListListener<Map<String, Object>>> getListListeners() {
        return listListeners;
    }
}
