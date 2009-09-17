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

import org.apache.pivot.beans.BeanDictionary;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.SortDirection;
import org.apache.pivot.wtk.TableView;

/**
 * Compares two rows. The dictionary values are expected to implement {@link Comparable}. If
 * not, the string representation of the value will be used.
 */
public class TableViewRowComparator implements Comparator<Object> {
    private TableView tableView;

    public TableViewRowComparator(TableView tableView) {
        if (tableView == null) {
            throw new IllegalArgumentException();
        }

        this.tableView = tableView;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compare(Object o1, Object o2) {
        int result;

        TableView.SortDictionary sort = tableView.getSort();

        if (sort.getLength() > 0) {
            // TODO Support sorting on multiple columns
            if (sort.getLength() > 1) {
                throw new UnsupportedOperationException("Multi-column sort not yet supported.");
            }

            Dictionary<String, ?> row1;
            if (o1 instanceof Dictionary<?, ?>) {
                row1 = (Dictionary<String, ?>)o1;
            } else {
                row1 = new BeanDictionary(o1);
            }

            Dictionary<String, ?> row2;
            if (o2 instanceof Dictionary<?, ?>) {
                row2 = (Dictionary<String, ?>)o2;
            } else {
                row2 = new BeanDictionary(o2);
            }

            String columnName = sort.get(0).key;
            SortDirection sortDirection = sort.get(0).value;

            Comparable<Object> comparable = (Comparable<Object>)row1.get(columnName);
            Object value = row2.get(columnName);

            if (comparable == null
                && value == null) {
                result = 0;
            } else if (comparable == null) {
                result = 1;
            } else if (value == null) {
                result = -1;
            } else {
                result = (comparable.compareTo(value)) * (sortDirection == SortDirection.ASCENDING ? 1 : -1);
            }
        } else {
            result = 0;
        }

        return result;
    }
}
