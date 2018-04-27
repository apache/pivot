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
 * List button binding listener list interface.
 */
public interface ListButtonBindingListener {
    /**
     * List button binding listeners.
     */
    public static class Listeners extends ListenerList<ListButtonBindingListener>
        implements ListButtonBindingListener {
        @Override
        public void listDataKeyChanged(ListButton listButton, String previousListDataKey) {
            forEach(listener -> listener.listDataKeyChanged(listButton, previousListDataKey));
        }

        @Override
        public void listDataBindTypeChanged(ListButton listButton, BindType previousListDataBindType) {
            forEach(listener -> listener.listDataBindTypeChanged(listButton, previousListDataBindType));
        }

        @Override
        public void listDataBindMappingChanged(ListButton listButton,
            ListView.ListDataBindMapping previousListDataBindMapping) {
            forEach(listener -> listener.listDataBindMappingChanged(listButton, previousListDataBindMapping));
        }

        @Override
        public void selectedItemKeyChanged(ListButton listButton, String previousSelectedItemKey) {
            forEach(listener -> listener.selectedItemKeyChanged(listButton, previousSelectedItemKey));
        }

        @Override
        public void selectedItemBindTypeChanged(ListButton listButton,
            BindType previousSelectedItemBindType) {
            forEach(listener -> listener.selectedItemBindTypeChanged(listButton, previousSelectedItemBindType));
        }

        @Override
        public void selectedItemBindMappingChanged(ListButton listButton,
            ListView.ItemBindMapping previousSelectedItemBindMapping) {
            forEach(listener -> listener.selectedItemBindMappingChanged(listButton, previousSelectedItemBindMapping));
        }
    }

    /**
     * List button binding listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements ListButtonBindingListener {
        @Override
        public void listDataKeyChanged(ListButton listButton, String previousListDataKey) {
            // empty block
        }

        @Override
        public void listDataBindTypeChanged(ListButton listButton, BindType previousListDataBindType) {
            // empty block
        }

        @Override
        public void listDataBindMappingChanged(ListButton listButton,
            ListView.ListDataBindMapping previousListDataBindMapping) {
            // empty block
        }

        @Override
        public void selectedItemKeyChanged(ListButton listButton, String previousSelectedItemKey) {
            // empty block
        }

        @Override
        public void selectedItemBindTypeChanged(ListButton listButton,
            BindType previousSelectedItemBindType) {
            // empty block
        }

        @Override
        public void selectedItemBindMappingChanged(ListButton listButton,
            ListView.ItemBindMapping previousSelectedItemBindMapping) {
            // empty block
        }
    }

    /**
     * Called when a list button's list data key has changed.
     *
     * @param listButton          The list button that has changed.
     * @param previousListDataKey The previous binding key for the list data.
     */
    default void listDataKeyChanged(ListButton listButton, String previousListDataKey) {
    }

    /**
     * Called when a list button's list data bind type has changed.
     *
     * @param listButton               The list button that has changed.
     * @param previousListDataBindType The previous bind type for the list data.
     */
    default void listDataBindTypeChanged(ListButton listButton, BindType previousListDataBindType) {
    }

    /**
     * Called when a list button's list data bind mapping has changed.
     *
     * @param listButton                  The list button that has changed.
     * @param previousListDataBindMapping The previous bind mapping for the list data.
     */
    default void listDataBindMappingChanged(ListButton listButton,
        ListView.ListDataBindMapping previousListDataBindMapping) {
    }

    /**
     * Called when a list button's selected item key has changed.
     *
     * @param listButton              The list button that has changed.
     * @param previousSelectedItemKey The previous binding key for the button's selected item.
     */
    default void selectedItemKeyChanged(ListButton listButton, String previousSelectedItemKey) {
    }

    /**
     * Called when a list button's selected item bind type has changed.
     *
     * @param listButton                   The list button that has changed.
     * @param previousSelectedItemBindType The previous bind type for the selected item.
     */
    default void selectedItemBindTypeChanged(ListButton listButton,
        BindType previousSelectedItemBindType) {
    }

    /**
     * Called when a list button's selected item bind mapping has changed.
     *
     * @param listButton                      The list button that has changed.
     * @param previousSelectedItemBindMapping The previous bind mapping for the button's selected item.
     */
    default void selectedItemBindMappingChanged(ListButton listButton,
        ListView.ItemBindMapping previousSelectedItemBindMapping) {
    }
}
