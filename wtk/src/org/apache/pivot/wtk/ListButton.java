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

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSON;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.ListView.ListDataBindMapping;
import org.apache.pivot.wtk.content.ListButtonDataRenderer;
import org.apache.pivot.wtk.content.ListViewItemRenderer;

/**
 * Component that allows a user to select one of several list options. The
 * options are hidden until the user pushes the button.
 */
@DefaultProperty("listData")
public class ListButton extends Button {
    private static class ListButtonListenerList extends WTKListenerList<ListButtonListener>
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
        public void repeatableChanged(ListButton listButton) {
            for (ListButtonListener listener : this) {
                listener.repeatableChanged(listButton);
            }
        }

        @Override
        public void disabledItemFilterChanged(ListButton listButton, Filter<?> previousDisabledItemFilter) {
            for (ListButtonListener listener : this) {
                listener.disabledItemFilterChanged(listButton, previousDisabledItemFilter);
            }
        }

        @Override
        public void listSizeChanged(ListButton listButton, int previousListSize) {
            for (ListButtonListener listener : this) {
                listener.listSizeChanged(listButton, previousListSize);
            }
        }
    }

    private static class ListButtonItemListenerList extends WTKListenerList<ListButtonItemListener>
        implements ListButtonItemListener {
        @Override
        public void itemInserted(ListButton listButton, int index) {
            for (ListButtonItemListener listener : this) {
                listener.itemInserted(listButton, index);
            }
        }

        @Override
        public void itemsRemoved(ListButton listButton, int index, int count) {
            for (ListButtonItemListener listener : this) {
                listener.itemsRemoved(listButton, index, count);
            }
        }

        @Override
        public void itemUpdated(ListButton listButton, int index) {
            for (ListButtonItemListener listener : this) {
                listener.itemUpdated(listButton, index);
            }
        }

        @Override
        public void itemsCleared(ListButton listButton) {
            for (ListButtonItemListener listener : this) {
                listener.itemsCleared(listButton);
            }
        }

        @Override
        public void itemsSorted(ListButton listButton) {
            for (ListButtonItemListener listener : this) {
                listener.itemsSorted(listButton);
            }
        }
    }

    private static class ListButtonSelectionListenerList extends WTKListenerList<ListButtonSelectionListener>
        implements ListButtonSelectionListener {
        @Override
        public void selectedIndexChanged(ListButton listButton, int previousSelectedIndex) {
            for (ListButtonSelectionListener listener : this) {
                listener.selectedIndexChanged(listButton, previousSelectedIndex);
            }
        }

        @Override
        public void selectedItemChanged(ListButton listButton, Object previousSelectedItem) {
            for (ListButtonSelectionListener listener : this) {
                listener.selectedItemChanged(listButton, previousSelectedItem);
            }
        }
    }

    private static class ListButtonBindingListenerList extends WTKListenerList<ListButtonBindingListener>
        implements ListButtonBindingListener {
        @Override
        public void listDataKeyChanged(ListButton listButton, String previousListDataKey) {
            for (ListButtonBindingListener listener : this) {
                listener.listDataKeyChanged(listButton, previousListDataKey);
            }
        }

        @Override
        public void listDataBindTypeChanged(ListButton listButton, BindType previousListDataBindType) {
            for (ListButtonBindingListener listener : this) {
                listener.listDataBindTypeChanged(listButton, previousListDataBindType);
            }
        }

        @Override
        public void listDataBindMappingChanged(ListButton listButton,
            ListView.ListDataBindMapping previousListDataBindMapping) {
            for (ListButtonBindingListener listener : this) {
                listener.listDataBindMappingChanged(listButton, previousListDataBindMapping);
            }
        }

        @Override
        public void selectedItemKeyChanged(ListButton listButton, String previousSelectedItemKey) {
            for (ListButtonBindingListener listener : this) {
                listener.selectedItemKeyChanged(listButton, previousSelectedItemKey);
            }
        }

        @Override
        public void selectedItemBindTypeChanged(ListButton listButton, BindType previousSelectedItemBindType) {
            for (ListButtonBindingListener listener : this) {
                listener.selectedItemBindTypeChanged(listButton, previousSelectedItemBindType);
            }
        }

        @Override
        public void selectedItemBindMappingChanged(ListButton listButton, ListView.ItemBindMapping previousSelectedItemBindMapping) {
            for (ListButtonBindingListener listener : this) {
                listener.selectedItemBindMappingChanged(listButton, previousSelectedItemBindMapping);
            }
        }
    }

    /**
     * ListButton skin interface. ListButton skins must implement
     * this interface to facilitate additional communication between the
     * component and the skin.
     */
    public interface Skin {
        public Window getListViewPopup();
    }

    private List<?> listData;
    private ListView.ItemRenderer itemRenderer;
    private boolean repeatable = false;
    private int selectedIndex = -1;
    private Filter<?> disabledItemFilter = null;
    private int listSize = -1;

    private String listDataKey = null;
    private BindType listDataBindType = BindType.BOTH;
    private ListDataBindMapping listDataBindMapping = null;

    private String selectedItemKey = null;
    private BindType selectedItemBindType = BindType.BOTH;
    private ListView.ItemBindMapping selectedItemBindMapping = null;

    private ListListener<Object> listDataListener = new ListListener<Object>() {
        @Override
        public void itemInserted(List<Object> list, int index) {
            int previousSelectedIndex = selectedIndex;
            if (index <= selectedIndex) {
                selectedIndex++;
            }

            listButtonItemListeners.itemInserted(ListButton.this, index);

            if (selectedIndex != previousSelectedIndex) {
                listButtonSelectionListeners.selectedIndexChanged(ListButton.this, selectedIndex);
            }
        }

        @Override
        public void itemsRemoved(List<Object> list, int index, Sequence<Object> items) {
            int count = items.getLength();

            int previousSelectedIndex = selectedIndex;

            if (selectedIndex >= index) {
                if (selectedIndex < index + count) {
                    selectedIndex = -1;
                } else {
                    selectedIndex -= count;
                }
            }

            listButtonItemListeners.itemsRemoved(ListButton.this, index, count);

            if (selectedIndex != previousSelectedIndex) {
                listButtonSelectionListeners.selectedIndexChanged(ListButton.this, selectedIndex);

                if (selectedIndex == -1) {
                    listButtonSelectionListeners.selectedItemChanged(ListButton.this, null);
                }
            }
        }

        @Override
        public void itemUpdated(List<Object> list, int index, Object previousItem) {
            listButtonItemListeners.itemUpdated(ListButton.this, index);
        }

        @Override
        public void listCleared(List<Object> list) {
            int previousSelectedIndex = selectedIndex;
            selectedIndex = -1;

            listButtonItemListeners.itemsCleared(ListButton.this);

            if (previousSelectedIndex != selectedIndex) {
                listButtonSelectionListeners.selectedIndexChanged(ListButton.this, selectedIndex);
                listButtonSelectionListeners.selectedItemChanged(ListButton.this, getSelectedItem());
            }
        }

        @Override
        public void comparatorChanged(List<Object> list, Comparator<Object> previousComparator) {
            if (list.getComparator() != null) {
                int previousSelectedIndex = selectedIndex;
                selectedIndex = -1;

                listButtonItemListeners.itemsSorted(ListButton.this);

                if (previousSelectedIndex != selectedIndex) {
                    listButtonSelectionListeners.selectedIndexChanged(ListButton.this, selectedIndex);
                    listButtonSelectionListeners.selectedItemChanged(ListButton.this, getSelectedItem());
                }
            }
        }
    };

    private ListButtonListenerList listButtonListeners = new ListButtonListenerList();
    private ListButtonItemListenerList listButtonItemListeners = new ListButtonItemListenerList();
    private ListButtonSelectionListenerList listButtonSelectionListeners = new ListButtonSelectionListenerList();
    private ListButtonBindingListenerList listButtonBindingListeners = new ListButtonBindingListenerList();

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

        installSkin(ListButton.class);
    }

    @Override
    protected void setSkin(org.apache.pivot.wtk.Skin skin) {
        if (!(skin instanceof ListButton.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + ListButton.Skin.class.getName());
        }

        super.setSkin(skin);
    }

    /**
     * @return the popup window associated with this components skin
     */
    public Window getListPopup() {
        return ((ListButton.Skin) getSkin()).getListViewPopup();
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
    @SuppressWarnings("unchecked")
    public void setListData(List<?> listData) {
        if (listData == null) {
            throw new IllegalArgumentException("listData is null.");
        }

        List<?> previousListData = this.listData;

        if (previousListData != listData) {
            int previousSelectedIndex = selectedIndex;

            if (previousListData != null) {
                // Clear any existing selection
                selectedIndex = -1;

                ((List<Object>)previousListData).getListListeners().remove(listDataListener);
            }

            ((List<Object>)listData).getListListeners().add(listDataListener);

            // Update the list data and fire change event
            this.listData = listData;
            listButtonListeners.listDataChanged(this, previousListData);

            if (selectedIndex != previousSelectedIndex) {
                listButtonSelectionListeners.selectedIndexChanged(this, selectedIndex);
                listButtonSelectionListeners.selectedItemChanged(this, null);
            }
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
     * Sets the list button's list data.
     *
     * @param listData
     * A URL referring to a JSON file containing the data to be presented by
     * the list button.
     */
    public void setListData(URL listData) {
        if (listData == null) {
            throw new IllegalArgumentException("listData is null.");
        }

        JSONSerializer jsonSerializer = new JSONSerializer();

        try {
            setListData((List<?>)jsonSerializer.readObject(listData.openStream()));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        } catch (IOException exception) {
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
     * Returns the list button's repeatable flag.
     */
    public boolean isRepeatable() {
        return repeatable;
    }

    /**
     * Sets the list button's repeatable flag.
     *
     * @param repeatable
     */
    public void setRepeatable(boolean repeatable) {
        if (this.repeatable != repeatable) {
            this.repeatable = repeatable;
            listButtonListeners.repeatableChanged(this);
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
        indexBoundsCheck("selectedIndex", selectedIndex, -1, listData.getLength() - 1);

        int previousSelectedIndex = this.selectedIndex;

        if (previousSelectedIndex != selectedIndex) {
            this.selectedIndex = selectedIndex;
            listButtonSelectionListeners.selectedIndexChanged(this, previousSelectedIndex);
            listButtonSelectionListeners.selectedItemChanged(this, (previousSelectedIndex == -1) ?
                null : listData.get(previousSelectedIndex));
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

    /**
     * Returns the list size.
     */
    public int getListSize() {
        return listSize;
    }

    /**
     * Sets the list size. If the number of items in the list exceeds this value,
     * the list will scroll.
     *
     * @param listSize
     */
    public void setListSize(int listSize) {
        if (listSize < -1) {
            throw new IllegalArgumentException("Invalid list size.");
        }

        int previousListSize = this.listSize;
        if (previousListSize != listSize) {
            this.listSize = listSize;
            listButtonListeners.listSizeChanged(this, previousListSize);
        }
    }

    public String getListDataKey() {
        return listDataKey;
    }

    public void setListDataKey(String listDataKey) {
        String previousListDataKey = this.listDataKey;
        if (previousListDataKey != listDataKey) {
            this.listDataKey = listDataKey;
            listButtonBindingListeners.listDataKeyChanged(this, previousListDataKey);
        }
    }

    public BindType getListDataBindType() {
        return listDataBindType;
    }

    public void setListDataBindType(BindType listDataBindType) {
        if (listDataBindType == null) {
            throw new IllegalArgumentException();
        }

        BindType previousListDataBindType = this.listDataBindType;

        if (previousListDataBindType != listDataBindType) {
            this.listDataBindType = listDataBindType;
            listButtonBindingListeners.listDataBindTypeChanged(this, previousListDataBindType);
        }
    }

    public ListDataBindMapping getListDataBindMapping() {
        return listDataBindMapping;
    }

    public void setListDataBindMapping(ListDataBindMapping listDataBindMapping) {
        ListDataBindMapping previousListDataBindMapping = this.listDataBindMapping;

        if (previousListDataBindMapping != listDataBindMapping) {
            this.listDataBindMapping = listDataBindMapping;
            listButtonBindingListeners.listDataBindMappingChanged(this, previousListDataBindMapping);
        }
    }

    public String getSelectedItemKey() {
        return selectedItemKey;
    }

    public void setSelectedItemKey(String selectedItemKey) {
        String previousSelectedItemKey = this.selectedItemKey;

        if (previousSelectedItemKey != selectedItemKey) {
            this.selectedItemKey = selectedItemKey;
            listButtonBindingListeners.selectedItemKeyChanged(this, previousSelectedItemKey);
        }
    }

    public BindType getSelectedItemBindType() {
        return selectedItemBindType;
    }

    public void setSelectedItemBindType(BindType selectedItemBindType) {
        if (selectedItemBindType == null) {
            throw new IllegalArgumentException();
        }

        BindType previousSelectedItemBindType = this.selectedItemBindType;
        if (previousSelectedItemBindType != selectedItemBindType) {
            this.selectedItemBindType = selectedItemBindType;
            listButtonBindingListeners.selectedItemBindTypeChanged(this, previousSelectedItemBindType);
        }
    }

    public ListView.ItemBindMapping getSelectedItemBindMapping() {
        return selectedItemBindMapping;
    }

    public void setSelectedItemBindMapping(ListView.ItemBindMapping selectedItemBindMapping) {
        ListView.ItemBindMapping previousSelectedItemBindMapping = this.selectedItemBindMapping;

        if (previousSelectedItemBindMapping != selectedItemBindMapping) {
            this.selectedItemBindMapping = selectedItemBindMapping;
            listButtonBindingListeners.selectedItemBindMappingChanged(this, previousSelectedItemBindMapping);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(Object context) {
        // Bind to list data
        if (listDataKey != null
            && listDataBindType != BindType.STORE
            && JSON.containsKey(context, listDataKey)) {
            Object value = JSON.get(context, listDataKey);

            List<?> listDataLocal;
            if (listDataBindMapping == null) {
                listDataLocal = (List<?>)value;
            } else {
                listDataLocal = listDataBindMapping.toListData(value);
            }

            setListData(listDataLocal);
        }

        // Bind to selected item
        if (selectedItemKey != null
            && selectedItemBindType != BindType.STORE
            && JSON.containsKey(context, selectedItemKey)) {
            Object item = JSON.get(context, selectedItemKey);

            int index;
            if (selectedItemBindMapping == null) {
                index = ((List<Object>)listData).indexOf(item);
            } else {
                index = selectedItemBindMapping.indexOf(listData, item);
            }

            setSelectedIndex(index);
        }
    }

    @Override
    public void store(Object context) {
        // Bind to list data
        if (listDataKey != null
            && listDataBindType != BindType.LOAD) {
            Object value;
            if (listDataBindMapping == null) {
                value = listData;
            } else {
                value = listDataBindMapping.valueOf(listData);
            }

            JSON.put(context, listDataKey, value);
        }

        // Bind to selected item
        if (selectedItemKey != null
            && selectedItemBindType != BindType.LOAD) {
            Object item;

            int selectedIndexLocal = getSelectedIndex();
            if (selectedIndexLocal == -1) {
                item = null;
            } else {
                if (selectedItemBindMapping == null) {
                    item = listData.get(selectedIndexLocal);
                } else {
                    item = selectedItemBindMapping.get(listData, selectedIndexLocal);
                }
            }

            JSON.put(context, selectedItemKey, item);
        }
    }

    @Override
    public void clear() {
        if (listDataKey != null) {
            setListData(new ArrayList<Object>());
        }

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
     * Returns the list button item listener list.
     */
    public ListenerList<ListButtonItemListener> getListButtonItemListeners() {
        return listButtonItemListeners;
    }

    /**
     * Returns the list button selection listener list.
     */
    public ListenerList<ListButtonSelectionListener> getListButtonSelectionListeners() {
        return listButtonSelectionListeners;
    }

    /**
     * Returns the list button binding listener list.
     */
    public ListenerList<ListButtonBindingListener> getListButtonBindingListeners() {
        return listButtonBindingListeners;
    }
}
