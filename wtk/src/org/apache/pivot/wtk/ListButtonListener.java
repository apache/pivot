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
import org.apache.pivot.util.ListenerList;

/**
 * List button listener list interface.
 */
public interface ListButtonListener {
    /**
     * List button listeners.
     */
    public static class Listeners extends ListenerList<ListButtonListener>
        implements ListButtonListener {
        @Override
        public void listDataChanged(ListButton listButton, List<?> previousListData) {
            forEach(listener -> listener.listDataChanged(listButton, previousListData));
        }

        @Override
        public void itemRendererChanged(ListButton listButton,
            ListView.ItemRenderer previousItemRenderer) {
            forEach(listener -> listener.itemRendererChanged(listButton, previousItemRenderer));
        }

        @Override
        public void repeatableChanged(ListButton listButton) {
            forEach(listener -> listener.repeatableChanged(listButton));
        }

        @Override
        public void disabledItemFilterChanged(ListButton listButton,
            Filter<?> previousDisabledItemFilter) {
            forEach(listener -> listener.disabledItemFilterChanged(listButton, previousDisabledItemFilter));
        }

        @Override
        public void listSizeChanged(ListButton listButton, int previousListSize) {
            forEach(listener -> listener.listSizeChanged(listButton, previousListSize));
        }
    }

    /**
     * List button listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements ListButtonListener {
        @Override
        public void listDataChanged(ListButton listButton, List<?> previousListData) {
            // empty block
        }

        @Override
        public void itemRendererChanged(ListButton listButton,
            ListView.ItemRenderer previousItemRenderer) {
            // empty block
        }

        @Override
        public void repeatableChanged(ListButton listButton) {
            // empty block
        }

        @Override
        public void disabledItemFilterChanged(ListButton listButton,
            Filter<?> previousDisabledItemFilter) {
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
     * @param listButton       The list button that has changed.
     * @param previousListData The previous list data.
     */
    default void listDataChanged(ListButton listButton, List<?> previousListData) {
    }

    /**
     * Called when a list button's item renderer has changed.
     *
     * @param listButton           The list button that was changed.
     * @param previousItemRenderer The previous renderer for the button's items.
     */
    default void itemRendererChanged(ListButton listButton,
        ListView.ItemRenderer previousItemRenderer) {
    }

    /**
     * Called when a list button's repeatable flag has changed.
     *
     * @param listButton The list button that has changed.
     */
    default void repeatableChanged(ListButton listButton) {
    }

    /**
     * Called when a list button's disabled item filter has changed.
     *
     * @param listButton                 The list button that has changed.
     * @param previousDisabledItemFilter The previous disabled item filter for the list button.
     */
    default void disabledItemFilterChanged(ListButton listButton,
        Filter<?> previousDisabledItemFilter) {
    }

    /**
     * Called when a list button's list size has changed.
     *
     * @param listButton       The list button that has changed.
     * @param previousListSize The previous value of the visible list size.
     */
    default void listSizeChanged(ListButton listButton, int previousListSize) {
    }
}
