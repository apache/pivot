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
 * List view listener interface.
 */
public interface ListViewListener {
    /**
     * List view listeners.
     */
    public static class Listeners extends ListenerList<ListViewListener> implements
        ListViewListener {
        @Override
        public void listDataChanged(ListView listView, List<?> previousListData) {
            forEach(listener -> listener.listDataChanged(listView, previousListData));
        }

        @Override
        public void itemRendererChanged(ListView listView,
            ListView.ItemRenderer previousItemRenderer) {
            forEach(listener -> listener.itemRendererChanged(listView, previousItemRenderer));
        }

        @Override
        public void itemEditorChanged(ListView listView, ListView.ItemEditor previousItemEditor) {
            forEach(listener -> listener.itemEditorChanged(listView, previousItemEditor));
        }

        @Override
        public void selectModeChanged(ListView listView, ListView.SelectMode previousSelectMode) {
            forEach(listener -> listener.selectModeChanged(listView, previousSelectMode));
        }

        @Override
        public void checkmarksEnabledChanged(ListView listView) {
            forEach(listener -> listener.checkmarksEnabledChanged(listView));
        }

        @Override
        public void checkmarksTriStateChanged(ListView listView) {
            forEach(listener -> listener.checkmarksTriStateChanged(listView));
        }

        @Override
        public void checkmarksMixedAsCheckedChanged(ListView listView) {
            forEach(listener -> listener.checkmarksMixedAsCheckedChanged(listView));
        }

        @Override
        public void disabledItemFilterChanged(ListView listView,
            Filter<?> previousDisabledItemFilter) {
            forEach(listener -> listener.disabledItemFilterChanged(listView, previousDisabledItemFilter));
        }

        @Override
        public void disabledCheckmarkFilterChanged(ListView listView,
            Filter<?> previousDisabledCheckmarkFilter) {
            forEach(listener -> listener.disabledCheckmarkFilterChanged(listView, previousDisabledCheckmarkFilter));
        }
    }

    /**
     * List view listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements ListViewListener {
        @Override
        public void listDataChanged(ListView listView, List<?> previousListData) {
            // empty block
        }

        @Override
        public void itemRendererChanged(ListView listView,
            ListView.ItemRenderer previousItemRenderer) {
            // empty block
        }

        @Override
        public void itemEditorChanged(ListView listView, ListView.ItemEditor previousItemEditor) {
            // empty block
        }

        @Override
        public void selectModeChanged(ListView listView, ListView.SelectMode previousSelectMode) {
            // empty block
        }

        @Override
        public void checkmarksEnabledChanged(ListView listView) {
            // empty block
        }

        @Override
        public void checkmarksTriStateChanged(ListView listView) {
            // empty block
        }

        @Override
        public void checkmarksMixedAsCheckedChanged(ListView listView) {
            // empty block
        }

        @Override
        public void disabledItemFilterChanged(ListView listView,
            Filter<?> previousDisabledItemFilter) {
            // empty block
        }

        @Override
        public void disabledCheckmarkFilterChanged(ListView listView,
            Filter<?> previousDisabledCheckmarkFilter) {
            // empty block
        }
    }

    /**
     * Called when a list view's list data has changed.
     *
     * @param listView         The source of the event.
     * @param previousListData The previous list data that was displayed.
     */
    default void listDataChanged(ListView listView, List<?> previousListData) {
    }

    /**
     * Called when a list view's item renderer has changed.
     *
     * @param listView             The source of the event.
     * @param previousItemRenderer The previous renderer used for each item.
     */
    default void itemRendererChanged(ListView listView, ListView.ItemRenderer previousItemRenderer) {
    }

    /**
     * Called when a list view's item editor has changed.
     *
     * @param listView           The source of the event.
     * @param previousItemEditor The previous editor used for updating items.
     */
    default void itemEditorChanged(ListView listView, ListView.ItemEditor previousItemEditor) {
    }

    /**
     * Called when a list view's select mode has changed.
     *
     * @param listView           The source of the event.
     * @param previousSelectMode The previous selection mode.
     */
    default void selectModeChanged(ListView listView, ListView.SelectMode previousSelectMode) {
    }

    /**
     * Called when a list view's checkmarks enabled flag has changed.
     *
     * @param listView The list view that has been changed.
     */
    default void checkmarksEnabledChanged(ListView listView) {
    }

    /**
     * Called when a list view's tri-state checkmarks flag has been changed.
     *
     * @param listView The list view that has been changed.
     */
    default void checkmarksTriStateChanged(ListView listView) {
    }

    /**
     * Called when a list view's flag to decide if mixed checkbox state should
     * be treated as checked has been changed.
     *
     * @param listView The list view that has been changed.
     */
    default void checkmarksMixedAsCheckedChanged(ListView listView) {
    }

    /**
     * Called when a list view's disabled item filter has changed.
     *
     * @param listView                   The source of the event.
     * @param previousDisabledItemFilter The previous filter function used to disable specific items.
     */
    default void disabledItemFilterChanged(ListView listView, Filter<?> previousDisabledItemFilter) {
    }

    /**
     * Called when a list view's disabled checkmark filter has changed.
     *
     * @param listView                        The source of the event.
     * @param previousDisabledCheckmarkFilter The previous filter function used to disable checkmarks
     *                                        for certain items.
     */
    default void disabledCheckmarkFilterChanged(ListView listView,
        Filter<?> previousDisabledCheckmarkFilter) {
    }
}
