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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.content.ListButtonDataRenderer;
import org.apache.pivot.wtk.content.ListViewItemRenderer;

/**
 * Component that allows a user to select one of several list options. The
 * options are hidden until the user pushes the button.
 */
public class ListButton extends Button {
    private static class ListButtonListenerList extends ListenerList<ListButtonListener>
        implements ListButtonListener {
        @Override
        public void listDataChanged(ListButton listButton, List<?> previousListData) {
            for (ListButtonListener listener : this) {
                listener.listDataChanged(listButton, previousListData);
            }
        }

        @Override
        public void itemRendererChanged(ListButton listButton, ListView.ItemRenderer previousItemRenderer) {
            for (ListButtonListener listener : this) {
                listener.itemRendererChanged(listButton, previousItemRenderer);
            }
        }

        @Override
        public void disabledItemFilterChanged(ListButton listButton, Filter<?> previousDisabledItemFilter) {
            for (ListButtonListener listener : this) {
                listener.disabledItemFilterChanged(listButton, previousDisabledItemFilter);
            }
        }

        @Override
        public void selectedItemKeyChanged(ListButton listButton, String previousSelectedItemKey) {
            for (ListButtonListener listener : this) {
                listener.selectedItemKeyChanged(listButton, previousSelectedItemKey);
            }
        }

        @Override
        public void selectionBindMappingChanged(ListButton listButton, ListView.BindMapping previousSelectionBindMapping) {
            for (ListButtonListener listener : this) {
                listener.selectionBindMappingChanged(listButton, previousSelectionBindMapping);
            }
        }
    }

    private static class ListButtonSelectionListenerList extends ListenerList<ListButtonSelectionListener>
        implements ListButtonSelectionListener {
        @Override
        public void selectedIndexChanged(ListButton listButton, int previousSelectedIndex) {
            for (ListButtonSelectionListener listener : this) {
                listener.selectedIndexChanged(listButton, previousSelectedIndex);
            }
        }
    }

    private List<?> listData;
    private ListView.ItemRenderer itemRenderer;
    private int selectedIndex = -1;
    private Filter<?> disabledItemFilter = null;

    private String selectedItemKey = null;
    private ListView.BindMapping selectionBindMapping = null;

    private ListButtonListenerList listButtonListeners = new ListButtonListenerList();
    private ListButtonSelectionListenerList listButtonSelectionListeners = new ListButtonSelectionListenerList();

    private static final Button.DataRenderer DEFAULT_DATA_RENDERER = new ListButtonDataRenderer();
    private static final ListView.ItemRenderer DEFAULT_ITEM_RENDERER = new ListViewItemRenderer();

    /**
     * Creates an empty list button.
     */
    public ListButton() {
        this(new ArrayList<Object>());
    }

    /**
     * Creates a list button with the given button data and an empty list.
     *
     * @param buttonData
     */
    public ListButton(Object buttonData) {
        this(buttonData, new ArrayList<Object>());
    }

    /**
     * Creates a list button with no button data and the given list data.
     * @param listData
     */
    public ListButton(List<?> listData) {
        this(null, listData);
    }

    /**
     * Creates a list button with the given button and list data.
     *
     * @param buttonData
     * @param listData
     */
    public ListButton(Object buttonData, List<?> listData) {
        super(buttonData);

        setDataRenderer(DEFAULT_DATA_RENDERER);
        setItemRenderer(DEFAULT_ITEM_RENDERER);
        setListData(listData);

        installThemeSkin(ListButton.class);
    }

    /**
     * @throws UnsupportedOperationException
     * This method is not supported by ListButton.
     */
    @Override
    public void setToggleButton(boolean toggleButton) {
        throw new UnsupportedOperationException("List buttons cannot be toggle buttons.");
    }

    /**
     * Returns the list data associated with this list button.
     *
     * @return
     * The list data.
     */
    public List<?> getListData() {
        return listData;
    }

    /**
     * Sets the list button's list data.
     *
     * @param listData
     * The list data to be presented by the list button.
     */
    public void setListData(List<?> listData) {
        if (listData == null) {
            throw new IllegalArgumentException("listData is null.");
        }

        List<?> previousListData = this.listData;

        if (previousListData != listData) {
            if (previousListData != null) {
                selectedIndex = -1;
            }

            // Update the list data and fire change event
            this.listData = listData;
            listButtonListeners.listDataChanged(this, previousListData);
        }
    }

    /**
     * Sets the list button's list data.
     *
     * @param listData
     * The list data to be presented by the list button as a JSON array.
     */
    public final void setListData(String listData) {
        if (listData == null) {
            throw new IllegalArgumentException("listData is null.");
        }

        try {
            setListData(JSONSerializer.parseList(listData));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    /**
     * Returns the renderer used to display items in the list.
     *
     * @return
     * The item renderer instance.
     */
    public ListView.ItemRenderer getItemRenderer() {
        return itemRenderer;
    }

    /**
     * Sets the renderer used to display items in the list.
     * <p>
     * Use {@link #setDataRenderer(org.apache.pivot.wtk.Button.DataRenderer)} to define
     * the renderer used to draw the button data.
     *
     * @param itemRenderer
     * The item renderer instance.
     */
    public void setItemRenderer(ListView.ItemRenderer itemRenderer) {
        ListView.ItemRenderer previousItemRenderer = this.itemRenderer;

        if (previousItemRenderer != itemRenderer) {
            this.itemRenderer = itemRenderer;
            listButtonListeners.itemRendererChanged(this, previousItemRenderer);
        }
    }

    /**
     * Returns the current selection.
     *
     * @return
     * The index of the currently selected list item, or <tt>-1</tt> if
     * nothing is selected.
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Sets the selection.
     *
     * @param selectedIndex
     * The index of the list item to select, or <tt>-1</tt> to clear the
     * selection.
     */
    public void setSelectedIndex(int selectedIndex) {
        if (selectedIndex < -1
            || selectedIndex >= listData.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        int previousSelectedIndex = this.selectedIndex;

        if (previousSelectedIndex != selectedIndex) {
            this.selectedIndex = selectedIndex;
            listButtonSelectionListeners.selectedIndexChanged(this, previousSelectedIndex);
        }
    }

    public Object getSelectedItem() {
        int index = getSelectedIndex();
        Object item = null;

        if (index >= 0) {
            item = listData.get(index);
        }

        return item;
    }

    @SuppressWarnings("unchecked")
    public void setSelectedItem(Object item) {
        setSelectedIndex((item == null) ? -1 : ((List<Object>)listData).indexOf(item));
    }

    /**
     * Returns an item's disabled state.
     *
     * @param index
     * The index of the item whose disabled state is to be tested.
     *
     * @return
     * <tt>true</tt> if the item is disabled; <tt>false</tt>,
     * otherwise.
     */
    @SuppressWarnings("unchecked")
    public boolean isItemDisabled(int index) {
        boolean disabled = false;

        if (disabledItemFilter != null) {
            Object item = listData.get(index);
            disabled = ((Filter<Object>)disabledItemFilter).include(item);
        }

        return disabled;
    }

    /**
     * Returns the disabled item filter.
     *
     * @return
     * The disabled item filter, or <tt>null</tt> if no disabled item filter is
     * set.
     */
    public Filter<?> getDisabledItemFilter() {
        return disabledItemFilter;
    }

    /**
     * Sets the disabled item filter.
     *
     * @param disabledItemFilter
     * The disabled item filter, or <tt>null</tt> for no disabled item filter.
     */
    public void setDisabledItemFilter(Filter<?> disabledItemFilter) {
        Filter<?> previousDisabledItemFilter = this.disabledItemFilter;

        if (previousDisabledItemFilter != disabledItemFilter) {
            this.disabledItemFilter = disabledItemFilter;
            listButtonListeners.disabledItemFilterChanged(this, previousDisabledItemFilter);
        }
    }

    public String getSelectedItemKey() {
        return selectedItemKey;
    }

    public void setSelectedItemKey(String selectedItemKey) {
        String previousSelectedItemKey = this.selectedItemKey;

        if (previousSelectedItemKey != selectedItemKey) {
            this.selectedItemKey = selectedItemKey;
            listButtonListeners.selectedItemKeyChanged(this, previousSelectedItemKey);
        }
    }

    public ListView.BindMapping getSelectionBindMapping() {
        return selectionBindMapping;
    }

    public void setSelectionBindMapping(ListView.BindMapping bindMapping) {
        ListView.BindMapping previousSelectionBindMapping = this.selectionBindMapping;

        if (previousSelectionBindMapping != bindMapping) {
            this.selectionBindMapping = bindMapping;
            listButtonListeners.selectionBindMappingChanged(this, previousSelectionBindMapping);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(Dictionary<String, ?> context) {
        if (selectedItemKey != null
            && JSONSerializer.containsKey(context, selectedItemKey)) {
            Object item = JSONSerializer.get(context, selectedItemKey);

            int index;
            if (selectionBindMapping == null) {
                index = ((List<Object>)listData).indexOf(item);
            } else {
                index = selectionBindMapping.indexOf(listData, item);
            }

            setSelectedIndex(index);
        }
    }

    @Override
    public void store(Dictionary<String, ?> context) {
        if (isEnabled()
            && selectedItemKey != null) {
            int selectedIndex = getSelectedIndex();

            Object item;
            if (selectionBindMapping == null) {
                item = listData.get(selectedIndex);
            } else {
                item = selectionBindMapping.get(listData, selectedIndex);
            }

            JSONSerializer.put(context, selectedItemKey, item);
        }
    }

    @Override
    public void clear() {
        if (selectedItemKey != null) {
            setSelectedItem(null);
        }
    }

    /**
     * Returns the list button listener list.
     */
    public ListenerList<ListButtonListener> getListButtonListeners() {
        return listButtonListeners;
    }

    /**
     * Returns the list button selection listener list.
     */
    public ListenerList<ListButtonSelectionListener> getListButtonSelectionListeners() {
        return listButtonSelectionListeners;
    }
}
