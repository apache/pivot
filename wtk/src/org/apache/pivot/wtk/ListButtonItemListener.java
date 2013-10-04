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

/**
 * List button item listener interface.
 */
public interface ListButtonItemListener {
    /**
     * List button item listener adapter.
     */
    public static class Adapter implements ListButtonItemListener {
        @Override
        public void itemInserted(ListButton listButton, int index) {
            // empty block
        }

        @Override
        public void itemsRemoved(ListButton listButton, int index, int count) {
            // empty block
        }

        @Override
        public void itemUpdated(ListButton listButton, int index) {
            // empty block
        }

        @Override
        public void itemsCleared(ListButton listButton) {
            // empty block
        }

        @Override
        public void itemsSorted(ListButton listButton) {
            // empty block
        }
    }

    /**
     * Called when an item is inserted into a list button's list data.
     *
     * @param listButton
     * @param index
     */
    public void itemInserted(ListButton listButton, int index);

    /**
     * Called when items are removed from a list button's list data.
     *
     * @param listButton
     * @param index
     * @param count
     */
    public void itemsRemoved(ListButton listButton, int index, int count);

    /**
     * Called when an item is updated within a list button's list data.
     *
     * @param listButton
     * @param index
     */
    public void itemUpdated(ListButton listButton, int index);

    /**
     * Called when a list button's list data has been cleared.
     *
     * @param listButton
     */
    public void itemsCleared(ListButton listButton);

    /**
     * Called when a list button's list data is sorted.
     *
     * @param listButton
     */
    public void itemsSorted(ListButton listButton);
}
