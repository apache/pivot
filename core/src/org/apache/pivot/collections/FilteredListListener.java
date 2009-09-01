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
package org.apache.pivot.collections;

import org.apache.pivot.util.Filter;

/**
 * Filtered list listener interface.
 */
public interface FilteredListListener<T> {
    /**
     * Filtered list listener adapter.
     *
     * @param <T>
     */
    public static class Adapter<T> implements FilteredListListener<T> {
        @Override
        public void sourceChanged(FilteredList<T> filteredList, List<T> previousSource) {
        }

        @Override
        public void filterChanged(FilteredList<T> filteredList, Filter<T> previousFilter) {
        }
    }

    /**
     * Called when a filtered list's source has changed.
     *
     * @param filteredList
     * @param previousSource
     */
    public void sourceChanged(FilteredList<T> filteredList, List<T> previousSource);

    /**
     * Called when a filtered list's filter has changed.
     *
     * @param filteredList
     * @param previousFilter
     */
    public void filterChanged(FilteredList<T> filteredList, Filter<T> previousFilter);
}
