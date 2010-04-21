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
package org.apache.pivot.wtk.content;

import java.util.Comparator;

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.SortDirection;
import org.apache.pivot.wtk.TableView;

/**
 * Compares two rows in a table view.
 */
public class TableViewRowComparator implements Comparator<Object> {
    private TableView tableView;

    public TableViewRowComparator(TableView tableView) {
        if (tableView == null) {
            throw new IllegalArgumentException();
        }

        this.tableView = tableView;
    }

    /**
     * Compares two rows in a table view. If the column values implement
     * {@link Comparable}, the {@link Comparable#compareTo(Object)} method will be used
     * to compare the values. Otherwise, the values will be compared as strings using
     * {@link Object#toString()}. If either value is <tt>null</tt>, it will be
     * considered as less than the other value. If both values are <tt>null</tt>, they
     * will be considered equal.
     */
    @Override
    @SuppressWarnings("unchecked")
    public int compare(Object o1, Object o2) {
        int result;

        TableView.SortDictionary sort = tableView.getSort();

        if (sort.getLength() > 0) {
            Dictionary<String, ?> row1;
            if (o1 instanceof Dictionary<?, ?>) {
                row1 = (Dictionary<String, ?>)o1;
            } else {
                row1 = new BeanAdapter(o1);
            }

            Dictionary<String, ?> row2;
            if (o2 instanceof Dictionary<?, ?>) {
                row2 = (Dictionary<String, ?>)o2;
            } else {
                row2 = new BeanAdapter(o2);
            }

            result = 0;

            int n = sort.getLength();
            int i = 0;

            while (i < n
                && result == 0) {
                Dictionary.Pair<String, SortDirection> pair = sort.get(i);

                String columnName = pair.key;
                SortDirection sortDirection = sort.get(columnName);

                Object value1 = row1.get(columnName);
                Object value2 = row2.get(columnName);

                if (value1 == null
                    && value2 == null) {
                    result = 0;
                } else if (value1 == null) {
                    result = -1;
                } else if (value2 == null) {
                    result = 1;
                } else {
                    if (value1 instanceof Comparable<?>) {
                        result = ((Comparable<Object>)value1).compareTo(value2);
                    } else {
                        String s1 = value1.toString();
                        String s2 = value2.toString();
                        result = s1.compareTo(s2);
                    }
                }

                result *= (sortDirection == SortDirection.ASCENDING ? 1 : -1);

                i++;
            }
        } else {
            result = 0;
        }

        return result;
    }
}
