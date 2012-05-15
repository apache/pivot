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
            // empty block
        }

        @Override
        public void itemRendererChanged(ListButton listButton, ListView.ItemRenderer previousItemRenderer) {
            // empty block
        }

        @Override
        public void repeatableChanged(ListButton listButton) {
            // empty block
        }

        @Override
        public void disabledItemFilterChanged(ListButton listButton, Filter<?> previousDisabledItemFilter) {
            // empty block
        }

        @Override
        public void listSizeChanged(ListButton listButton, int previousListSize) {
            // empty block
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
     * Called when a list button's repeatable flag has changed.
     *
     * @param listButton
     */
    public void repeatableChanged(ListButton listButton);

    /**
     * Called when a list button's disabled item filter has changed.
     *
     * @param listButton
     * @param previousDisabledItemFilter
     */
    public void disabledItemFilterChanged(ListButton listButton, Filter<?> previousDisabledItemFilter);

    /**
     * Called when a list button's list size has changed.
     *
     * @param listButton
     * @param previousListSize
     */
    public void listSizeChanged(ListButton listButton, int previousListSize);
}
