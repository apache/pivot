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
import pivot.util.ListenerList;
import pivot.wtk.content.ListButtonDataRenderer;
import pivot.wtk.skin.ListButtonSkin;

/**
 * Component that allows a user to select one of several list options. The
 * options are hidden until the user pushes the button.
 *
 * @author gbrown
 */
@ComponentInfo(icon="ListButton.png")
public class ListButton extends Button {
    /**
     * List button skin interface.
     *
     * @author gbrown
     */
    public interface Skin {
        public ListView getListView();
    }

    /**
     * List button listener list.
     *
     * @author gbrown
     */
    private class ListButtonListenerList extends ListenerList<ListButtonListener>
        implements ListButtonListener {
        public void listDataChanged(ListButton listButton, List<?> previousListData) {
            ListButtonSkin listButtonSkin = (ListButtonSkin)getSkin();
            if (listButtonSkin != null) {
                listButtonSkin.listDataChanged(listButton, previousListData);
            }

            for (ListButtonListener listener : this) {
                listener.listDataChanged(listButton, previousListData);
            }
        }

        public void itemRendererChanged(ListButton listButton, ListView.ItemRenderer previousItemRenderer) {
            ListButtonSkin listButtonSkin = (ListButtonSkin)getSkin();
            if (listButtonSkin != null) {
                listButtonSkin.itemRendererChanged(listButton, previousItemRenderer);
            }

            for (ListButtonListener listener : this) {
                listener.itemRendererChanged(listButton, previousItemRenderer);
            }
        }

        public void selectedValueKeyChanged(ListButton listButton, String previousSelectedValueKey) {
            ListButtonSkin listButtonSkin = (ListButtonSkin)getSkin();
            if (listButtonSkin != null) {
                listButtonSkin.selectedValueKeyChanged(listButton, previousSelectedValueKey);
            }

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
    private class ListButtonSelectionListenerList extends ListenerList<ListButtonSelectionListener>
        implements ListButtonSelectionListener {
        public void selectedIndexChanged(ListButton listButton, int previousSelectedIndex) {
            ListButtonSkin listButtonSkin = (ListButtonSkin)getSkin();
            if (listButtonSkin != null) {
                listButtonSkin.selectedIndexChanged(listButton, previousSelectedIndex);
            }

            for (ListButtonSelectionListener listener : this) {
                listener.selectedIndexChanged(listButton, previousSelectedIndex);
            }
        }
    }

    private ListButtonListenerList listButtonListeners = new ListButtonListenerList();
    private ListButtonSelectionListenerList listButtonSelectionListeners = new ListButtonSelectionListenerList();

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

        setDataRenderer(new ListButtonDataRenderer());
        installSkin(ListButton.class);

        setListData(listData);
    }

    @Override
    protected void setSkin(pivot.wtk.Skin skin) {
        if (!(skin instanceof ListButtonSkin)) {
            throw new IllegalArgumentException("Skin class must extend "
                + ListButtonSkin.class.getName());
        }

        super.setSkin(skin);
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
        ListButton.Skin listButtonSkin = (ListButton.Skin)getSkin();
        ListView listView = listButtonSkin.getListView();

        return listView.getListData();
    }

    /**
     * Sets the list data associated with this list button.
     * <p>
     * Fires {@link ListButtonListener#listDataChanged(ListButton, List)}.
     *
     * @param listData
     * The list data.
     */
    public void setListData(List<?> listData) {
        ListButton.Skin listButtonSkin = (ListButton.Skin)getSkin();
        ListView listView = listButtonSkin.getListView();
        List<?> previousListData = listView.getListData();

        if (previousListData != listData) {
            listView.setListData(listData);
            listButtonListeners.listDataChanged(this, previousListData);
        }
    }

    /**
     * Returns the renderer used to display items in the list.
     *
     * @return
     * The item renderer instance.
     */
    public ListView.ItemRenderer getItemRenderer() {
        ListButton.Skin listButtonSkin = (ListButton.Skin)getSkin();
        ListView listView = listButtonSkin.getListView();

        return listView.getItemRenderer();
    }

    /**
     * Sets the renderer used to display items in the list.
     * <p>
     * Fires {@link ListButtonListener#itemRendererChanged(ListButton,
     * pivot.wtk.ListView.ItemRenderer)}.
     * <p>
     * Use {@link #setDataRenderer(pivot.wtk.Button.DataRenderer)} to define
     * the renderer used to draw the button data.
     *
     * @param itemRenderer
     * The item renderer instance.
     */
    public void setItemRenderer(ListView.ItemRenderer itemRenderer) {
        ListButton.Skin listButtonSkin = (ListButton.Skin)getSkin();
        ListView listView = listButtonSkin.getListView();
        ListView.ItemRenderer previousItemRenderer = listView.getItemRenderer();

        if (previousItemRenderer != itemRenderer) {
            listView.setItemRenderer(itemRenderer);
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
        ListButton.Skin listButtonSkin = (ListButton.Skin)getSkin();
        ListView listView = listButtonSkin.getListView();

        return listView.getSelectedIndex();
    }

    /**
     * Sets the selection.
     *
     * @param selectedIndex
     * The index of the list item to select, or <tt>-1</tt> to clear the
     * selection.
     */
    public void setSelectedIndex(int selectedIndex) {
        ListButton.Skin listButtonSkin = (ListButton.Skin)getSkin();
        ListView listView = listButtonSkin.getListView();
        int previousSelectedIndex = listView.getSelectedIndex();

        if (previousSelectedIndex != selectedIndex) {
            listView.setSelectedIndex(selectedIndex);
            listButtonSelectionListeners.selectedIndexChanged(this, previousSelectedIndex);
        }
    }

    public Object getSelectedValue() {
        ListButton.Skin listButtonSkin = (ListButton.Skin)getSkin();
        ListView listView = listButtonSkin.getListView();

        return listView.getSelectedValue();
    }

    public void setSelectedValue(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        ListButton.Skin listButtonSkin = (ListButton.Skin)getSkin();
        ListView listView = listButtonSkin.getListView();

        int previousSelectedIndex = listView.getSelectedIndex();
        listView.setSelectedValue(value);

        if (listView.getSelectedIndex() != previousSelectedIndex) {
            listButtonSelectionListeners.selectedIndexChanged(this, previousSelectedIndex);
        }
    }

    public String getSelectedValueKey() {
        ListButton.Skin listButtonSkin = (ListButton.Skin)getSkin();
        ListView listView = listButtonSkin.getListView();

        return listView.getSelectedValueKey();
    }

    public void setSelectedValueKey(String selectedValueKey) {
        ListButton.Skin listButtonSkin = (ListButton.Skin)getSkin();
        ListView listView = listButtonSkin.getListView();

        String previousSelectedValueKey = listView.getSelectedValueKey();
        listView.setSelectedValueKey(selectedValueKey);
        listButtonListeners.selectedValueKeyChanged(this, previousSelectedValueKey);
    }

    @Override
    public void load(Dictionary<String, Object> context) {
        String selectedValueKey = getSelectedValueKey();
        if (selectedValueKey != null
            && context.containsKey(selectedValueKey)) {
            Object value = context.get(selectedValueKey);
            setSelectedValue(value);
        }
    }

    @Override
    public void store(Dictionary<String, Object> context) {
        String selectedValueKey = getSelectedValueKey();
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
