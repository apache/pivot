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
 * List view binding listener interface.
 */
public interface ListViewBindingListener {
    /**
     * List view binding listener adapter.
     */
    public static class Adapter implements ListViewBindingListener {
        @Override
        public void listDataKeyChanged(ListView listView, String previousListDataKey) {
        }

        @Override
        public void listDataBindTypeChanged(ListView listView, BindType previousListDataBindType) {
        }

        @Override
        public void listDataBindMappingChanged(ListView listView,
            ListView.ListDataBindMapping previousListDataBindMapping) {
        }

        @Override
        public void selectedItemKeyChanged(ListView listView, String previousSelectedItemKey) {
        }

        @Override
        public void selectedItemBindTypeChanged(ListView listView, BindType previousSelectedItemBindType) {
        }

        @Override
        public void selectedItemBindMappingChanged(ListView listView,
            ListView.ItemBindMapping previousSelectedItemBindMapping) {
        }

        @Override
        public void selectedItemsKeyChanged(ListView listView, String previousSelectedItemsKey) {
        }

        @Override
        public void selectedItemsBindTypeChanged(ListView listView, BindType previousSelectedItemsBindType) {
        }

        @Override
        public void selectedItemsBindMappingChanged(ListView listView,
            ListView.ItemBindMapping previousSelectedItemsBindMapping) {
        }

        @Override
        public void checkedItemsKeyChanged(ListView listView, String previousCheckedItemsKey) {
        }

        @Override
        public void checkedItemsBindTypeChanged(ListView listView, BindType previousCheckedItemsBindType) {
        }

        @Override
        public void checkedItemsBindMappingChanged(ListView listView,
            ListView.ItemBindMapping previousCheckedItemsBindMapping) {
        }
    }

    /**
     * Called when a list view's list data key has changed.
     *
     * @param listView
     * @param previousListDataKey
     */
    public void listDataKeyChanged(ListView listView, String previousListDataKey);

    /**
     * Called when a list view's list data bind type has changed.
     *
     * @param listView
     * @param previousListDataBindType
     */
    public void listDataBindTypeChanged(ListView listView, BindType previousListDataBindType);

    /**
     * Called when a list view's list data bind mapping has changed.
     *
     * @param listView
     * @param previousListDataBindMapping
     */
    public void listDataBindMappingChanged(ListView listView, ListView.ListDataBindMapping previousListDataBindMapping);

    /**
     * Called when a list view's selected item key has changed.
     *
     * @param listView
     * @param previousSelectedItemKey
     */
    public void selectedItemKeyChanged(ListView listView, String previousSelectedItemKey);

    /**
     * Called when a list view's selected item bind type has changed.
     *
     * @param listView
     * @param previousSelectedItemBindType
     */
    public void selectedItemBindTypeChanged(ListView listView, BindType previousSelectedItemBindType);

    /**
     * Called when a list view's selected item bind mapping has changed.
     *
     * @param listView
     * @param previousSelectedItemBindMapping
     */
    public void selectedItemBindMappingChanged(ListView listView,
        ListView.ItemBindMapping previousSelectedItemBindMapping);

    /**
     * Called when a list view's selected items key has changed.
     *
     * @param listView
     * @param previousSelectedItemsKey
     */
    public void selectedItemsKeyChanged(ListView listView, String previousSelectedItemsKey);

    /**
     * Called when a list view's selected items bind type has changed.
     *
     * @param listView
     * @param previousSelectedItemsBindType
     */
    public void selectedItemsBindTypeChanged(ListView listView, BindType previousSelectedItemsBindType);

    /**
     * Called when a list view's selected items bind mapping has changed.
     *
     * @param listView
     * @param previousSelectedItemsBindMapping
     */
    public void selectedItemsBindMappingChanged(ListView listView,
        ListView.ItemBindMapping previousSelectedItemsBindMapping);

    /**
     * Called when a list view's checked items key has changed.
     *
     * @param listView
     * @param previousCheckedItemsKey
     */
    public void checkedItemsKeyChanged(ListView listView, String previousCheckedItemsKey);

    /**
     * Called when a list view's checked items bind type has changed.
     *
     * @param listView
     * @param previousCheckedItemsBindType
     */
    public void checkedItemsBindTypeChanged(ListView listView, BindType previousCheckedItemsBindType);

    /**
     * Called when a list view's checked items bind mapping has changed.
     *
     * @param listView
     * @param previousCheckedItemsBindMapping
     */
    public void checkedItemsBindMappingChanged(ListView listView,
        ListView.ItemBindMapping previousCheckedItemsBindMapping);
}
