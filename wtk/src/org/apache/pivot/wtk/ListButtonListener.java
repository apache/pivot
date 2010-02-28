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
package org.apache.pivot.wtk;

import org.apache.pivot.collections.List;
import org.apache.pivot.util.Filter;

/**
 * List button listener list interface.
 */
public interface ListButtonListener {
    /**
     * List button listener adapter.
     */
    public static class Adapter implements ListButtonListener {
        @Override
        public void listDataChanged(ListButton listButton, List<?> previousListData) {
        }

        @Override
        public void itemRendererChanged(ListButton listButton, ListView.ItemRenderer previousItemRenderer) {
        }

        @Override
        public void disabledItemFilterChanged(ListButton listButton, Filter<?> previousDisabledItemFilter) {
        }

        @Override
        public void selectedItemKeyChanged(ListButton listButton, String previousSelectedItemKey) {
        }

        @Override
        public void selectedItemBindMappingChanged(ListButton listButton, ListView.SelectedItemBindMapping previousSelectedItemBindMapping) {
        }
    }

    /**
     * Called when a list button's list data has changed.
     *
     * @param listButton
     * @param previousListData
     */
    public void listDataChanged(ListButton listButton, List<?> previousListData);

    /**
     * Called when a list button's item renderer has changed.
     *
     * @param listButton
     * @param previousItemRenderer
     */
    public void itemRendererChanged(ListButton listButton, ListView.ItemRenderer previousItemRenderer);

    /**
     * Called when a list button's disabled item filter has changed.
     *
     * @param listButton
     * @param previousDisabledItemFilter
     */
    public void disabledItemFilterChanged(ListButton listButton, Filter<?> previousDisabledItemFilter);

    /**
     * Called when a list button's selected value key has changed.
     *
     * @param listButton
     * @param previousSelectedItemKey
     */
    public void selectedItemKeyChanged(ListButton listButton, String previousSelectedItemKey);

    /**
     * Called when a list button's selection bind mapping has changed.
     *
     * @param listButton
     * @param previousSelectedItemBindMapping
     */
    public void selectedItemBindMappingChanged(ListButton listButton,
        ListView.SelectedItemBindMapping previousSelectedItemBindMapping);
}
