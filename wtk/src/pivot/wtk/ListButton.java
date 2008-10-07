/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.Dictionary;
import pivot.serialization.JSONSerializer;
import pivot.util.ListenerList;
import pivot.wtk.content.ListButtonDataRenderer;
import pivot.wtk.content.ListViewItemRenderer;

/**
 * Component that allows a user to select one of several list options. The
 * options are hidden until the user pushes the button.
 *
 * @author gbrown
 */
@ComponentInfo(icon="ListButton.png")
public class ListButton extends Button {
    /**
     * List button listener list.
     *
     * @author gbrown
     */
    private static class ListButtonListenerList extends ListenerList<ListButtonListener>
        implements ListButtonListener {
        public void listDataChanged(ListButton listButton, List<?> previousListData) {
            for (ListButtonListener listener : this) {
                listener.listDataChanged(listButton, previousListData);
            }
        }

        public void itemRendererChanged(ListButton listButton, ListView.ItemRenderer previousItemRenderer) {
            for (ListButtonListener listener : this) {
                listener.itemRendererChanged(listButton, previousItemRenderer);
            }
        }

        public void selectedValueKeyChanged(ListButton listButton, String previousSelectedValueKey) {
            for (ListButtonListener listener : this) {
                listener.selectedValueKeyChanged(listButton, previousSelectedValueKey);
            }
        }
    }

    /**
     * List button selection listener list.
     *
     * @author gbrown
     */
    private static class ListButtonSelectionListenerList extends ListenerList<ListButtonSelectionListener>
        implements ListButtonSelectionListener {
        public void selectedIndexChanged(ListButton listButton, int previousSelectedIndex) {
            for (ListButtonSelectionListener listener : this) {
                listener.selectedIndexChanged(listButton, previousSelectedIndex);
            }
        }
    }

    private List<?> listData;
    private ListView.ItemRenderer itemRenderer = null;
    private int selectedIndex = -1;
    private String selectedValueKey = null;

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

        installSkin(ListButton.class);
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
            if (previousListData != null) {
                setSelectedIndex(-1);
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
    public void setListData(String listData) {
        if (listData == null) {
            throw new IllegalArgumentException("listData is null.");
        }

        setListData(JSONSerializer.parseList(listData));
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
     * Use {@link #setDataRenderer(pivot.wtk.Button.DataRenderer)} to define
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
        int previousSelectedIndex = this.selectedIndex;

        if (previousSelectedIndex != selectedIndex) {
            this.selectedIndex = selectedIndex;
            listButtonSelectionListeners.selectedIndexChanged(this, previousSelectedIndex);
        }
    }

    public Object getSelectedValue() {
        int index = getSelectedIndex();
        Object value = null;

        if (index >= 0) {
            value = listData.get(index);
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    public void setSelectedValue(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        int index = ((List<Object>)listData).indexOf(value);
        if (index == -1) {
            throw new IllegalArgumentException("\"" + value + "\" is not a valid selection.");
        }

        setSelectedIndex(index);
    }

    public String getSelectedValueKey() {
        return selectedValueKey;
    }

    public void setSelectedValueKey(String selectedValueKey) {
        String previousSelectedValueKey = this.selectedValueKey;
        this.selectedValueKey = selectedValueKey;
        listButtonListeners.selectedValueKeyChanged(this, previousSelectedValueKey);
    }

    @Override
    public void load(Dictionary<String, Object> context) {
        if (selectedValueKey != null
            && context.containsKey(selectedValueKey)) {
            Object value = context.get(selectedValueKey);
            setSelectedValue(value);
        }
    }

    @Override
    public void store(Dictionary<String, Object> context) {
        if (selectedValueKey != null) {
            Object value = getSelectedValue();
            context.put(selectedValueKey, value);
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
