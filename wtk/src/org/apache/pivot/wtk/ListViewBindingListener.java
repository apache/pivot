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

import org.apache.pivot.util.ListenerList;

/**
 * List view binding listener interface.
 */
public interface ListViewBindingListener {
    /**
     * List view binding listeners.
     */
    public static class Listeners extends ListenerList<ListViewBindingListener>
        implements ListViewBindingListener {
        @Override
        public void listDataKeyChanged(ListView listView, String previousListDataKey) {
            forEach(listener -> listener.listDataKeyChanged(listView, previousListDataKey));
        }

        @Override
        public void listDataBindTypeChanged(ListView listView, BindType previousListDataBindType) {
            forEach(listener -> listener.listDataBindTypeChanged(listView, previousListDataBindType));
        }

        @Override
        public void listDataBindMappingChanged(ListView listView,
            ListView.ListDataBindMapping previousListDataBindMapping) {
            forEach(listener -> listener.listDataBindMappingChanged(listView, previousListDataBindMapping));
        }

        @Override
        public void selectedItemKeyChanged(ListView listView, String previousSelectedItemKey) {
            forEach(listener -> listener.selectedItemKeyChanged(listView, previousSelectedItemKey));
        }

        @Override
        public void selectedItemBindTypeChanged(ListView listView,
            BindType previousSelectedItemBindType) {
            forEach(listener -> listener.selectedItemBindTypeChanged(listView, previousSelectedItemBindType));
        }

        @Override
        public void selectedItemBindMappingChanged(ListView listView,
            ListView.ItemBindMapping previousSelectedItemBindMapping) {
            forEach(listener -> listener.selectedItemBindMappingChanged(listView, previousSelectedItemBindMapping));
        }

        @Override
        public void selectedItemsKeyChanged(ListView listView, String previousSelectedItemsKey) {
            forEach(listener -> listener.selectedItemsKeyChanged(listView, previousSelectedItemsKey));
        }

        @Override
        public void selectedItemsBindTypeChanged(ListView listView,
            BindType previousSelectedItemsBindType) {
            forEach(listener -> listener.selectedItemsBindTypeChanged(listView, previousSelectedItemsBindType));
        }

        @Override
        public void selectedItemsBindMappingChanged(ListView listView,
            ListView.ItemBindMapping previousSelectedItemsBindMapping) {
            forEach(listener -> listener.selectedItemsBindMappingChanged(listView, previousSelectedItemsBindMapping));
        }

        @Override
        public void checkedItemsKeyChanged(ListView listView, String previousCheckedItemsKey) {
            forEach(listener -> listener.checkedItemsKeyChanged(listView, previousCheckedItemsKey));
        }

        @Override
        public void checkedItemsBindTypeChanged(ListView listView,
            BindType previousCheckedItemsBindType) {
            forEach(listener -> listener.checkedItemsBindTypeChanged(listView, previousCheckedItemsBindType));
        }

        @Override
        public void checkedItemsBindMappingChanged(ListView listView,
            ListView.ItemBindMapping previousCheckedItemsBindMapping) {
            forEach(listener -> listener.checkedItemsBindMappingChanged(listView, previousCheckedItemsBindMapping));
        }

        @Override
        public void itemsStateKeyChanged(ListView listView, String previousItemsStateKey) {
            forEach(listener -> listener.itemsStateKeyChanged(listView, previousItemsStateKey));
        }

        @Override
        public void itemsStateBindTypeChanged(ListView listView, BindType previousItemsStateBindType) {
            forEach(listener -> listener.itemsStateBindTypeChanged(listView, previousItemsStateBindType));
        }

        @Override
        public void itemsStateBindMappingChanged(ListView listView,
            ListView.ItemStateBindMapping previousItemsStateBindMapping) {
            forEach(listener -> listener.itemsStateBindMappingChanged(listView, previousItemsStateBindMapping));
        }
    }

    /**
     * List view binding listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements ListViewBindingListener {
        @Override
        public void listDataKeyChanged(ListView listView, String previousListDataKey) {
            // empty block
        }

        @Override
        public void listDataBindTypeChanged(ListView listView, BindType previousListDataBindType) {
            // empty block
        }

        @Override
        public void listDataBindMappingChanged(ListView listView,
            ListView.ListDataBindMapping previousListDataBindMapping) {
            // empty block
        }

        @Override
        public void selectedItemKeyChanged(ListView listView, String previousSelectedItemKey) {
            // empty block
        }

        @Override
        public void selectedItemBindTypeChanged(ListView listView,
            BindType previousSelectedItemBindType) {
            // empty block
        }

        @Override
        public void selectedItemBindMappingChanged(ListView listView,
            ListView.ItemBindMapping previousSelectedItemBindMapping) {
            // empty block
        }

        @Override
        public void selectedItemsKeyChanged(ListView listView, String previousSelectedItemsKey) {
            // empty block
        }

        @Override
        public void selectedItemsBindTypeChanged(ListView listView,
            BindType previousSelectedItemsBindType) {
            // empty block
        }

        @Override
        public void selectedItemsBindMappingChanged(ListView listView,
            ListView.ItemBindMapping previousSelectedItemsBindMapping) {
            // empty block
        }

        @Override
        public void checkedItemsKeyChanged(ListView listView, String previousCheckedItemsKey) {
            // empty block
        }

        @Override
        public void checkedItemsBindTypeChanged(ListView listView,
            BindType previousCheckedItemsBindType) {
            // empty block
        }

        @Override
        public void checkedItemsBindMappingChanged(ListView listView,
            ListView.ItemBindMapping previousCheckedItemsBindMapping) {
            // empty block
        }

        @Override
        public void itemsStateKeyChanged(ListView listView, String previousItemsStateKey) {
            // empty block
        }

        @Override
        public void itemsStateBindTypeChanged(ListView listView, BindType previousItemsStateBindType) {
            // empty block
        }

        @Override
        public void itemsStateBindMappingChanged(ListView listView,
            ListView.ItemStateBindMapping previousItemsStateBindMapping) {
            // empty block
        }
    }

    /**
     * Called when a list view's list data key has changed.
     *
     * @param listView            The list view whose binding has changed.
     * @param previousListDataKey The previous binding key for the list data.
     */
    default void listDataKeyChanged(ListView listView, String previousListDataKey) {
    }

    /**
     * Called when a list view's list data bind type has changed.
     *
     * @param listView                 The list view whose binding has changed.
     * @param previousListDataBindType The previous bind type for the list data.
     */
    default void listDataBindTypeChanged(ListView listView, BindType previousListDataBindType) {
    }

    /**
     * Called when a list view's list data bind mapping has changed.
     *
     * @param listView                    The list view whose binding has changed.
     * @param previousListDataBindMapping The previous bind mapping for the list data.
     */
    default void listDataBindMappingChanged(ListView listView,
        ListView.ListDataBindMapping previousListDataBindMapping) {
    }

    /**
     * Called when a list view's selected item key has changed.
     *
     * @param listView                The list view whose binding has changed.
     * @param previousSelectedItemKey The previous binding key for the selected item.
     */
    default void selectedItemKeyChanged(ListView listView, String previousSelectedItemKey) {
    }

    /**
     * Called when a list view's selected item bind type has changed.
     *
     * @param listView                     The list view whose binding has changed.
     * @param previousSelectedItemBindType The previous bind type for the selected item.
     */
    default void selectedItemBindTypeChanged(ListView listView, BindType previousSelectedItemBindType) {
    }

    /**
     * Called when a list view's selected item bind mapping has changed.
     *
     * @param listView                        The list view whose binding has changed.
     * @param previousSelectedItemBindMapping The previous bind mapping for the selected item.
     */
    default void selectedItemBindMappingChanged(ListView listView,
        ListView.ItemBindMapping previousSelectedItemBindMapping) {
    }

    /**
     * Called when a list view's selected items key has changed.
     *
     * @param listView                 The list view whose binding has changed.
     * @param previousSelectedItemsKey The previous binding key for the selected items.
     */
    default void selectedItemsKeyChanged(ListView listView, String previousSelectedItemsKey) {
    }

    /**
     * Called when a list view's selected items bind type has changed.
     *
     * @param listView                      The list view whose binding has changed.
     * @param previousSelectedItemsBindType The previous bind type for the selected items.
     */
    default void selectedItemsBindTypeChanged(ListView listView,
        BindType previousSelectedItemsBindType) {
    }

    /**
     * Called when a list view's selected items bind mapping has changed.
     *
     * @param listView                         The list view whose binding has changed.
     * @param previousSelectedItemsBindMapping The previous bind mapping for the selected items.
     */
    default void selectedItemsBindMappingChanged(ListView listView,
        ListView.ItemBindMapping previousSelectedItemsBindMapping) {
    }

    /**
     * Called when a list view's checked items key has changed.
     *
     * @param listView                The list view whose binding has changed.
     * @param previousCheckedItemsKey The previous binding key for the checked items.
     */
    default void checkedItemsKeyChanged(ListView listView, String previousCheckedItemsKey) {
    }

    /**
     * Called when a list view's checked items bind type has changed.
     *
     * @param listView                     The list view whose binding has changed.
     * @param previousCheckedItemsBindType The previous bind type for the checked items.
     */
    default void checkedItemsBindTypeChanged(ListView listView, BindType previousCheckedItemsBindType) {
    }

    /**
     * Called when a list view's checked items bind mapping has changed.
     *
     * @param listView                        The list view whose binding has changed.
     * @param previousCheckedItemsBindMapping The previous bind mapping function for the checked items.
     */
    default void checkedItemsBindMappingChanged(ListView listView,
        ListView.ItemBindMapping previousCheckedItemsBindMapping) {
    }

    /**
     * Called when a list view's items' state key has changed.
     *
     * @param listView              The list view whose binding has changed.
     * @param previousItemsStateKey The previous bind key for the items' state.
     */
    default void itemsStateKeyChanged(ListView listView, String previousItemsStateKey) {
    }

    /**
     * Called when a list view's items' state bind type has changed.
     *
     * @param listView                   The list view whose binding has changed.
     * @param previousItemsStateBindType The previous bind type for the items' state.
     */
    default void itemsStateBindTypeChanged(ListView listView, BindType previousItemsStateBindType) {
    }

    /**
     * Called when a list view's items state bind mapping has changed.
     *
     * @param listView                      The list view whose binding has changed.
     * @param previousItemsStateBindMapping The previous bind mapping function for the items' state.
     */
    default void itemsStateBindMappingChanged(ListView listView,
        ListView.ItemStateBindMapping previousItemsStateBindMapping) {
    }
}
