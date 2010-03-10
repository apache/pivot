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

import java.util.Comparator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.JSON;
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.content.SpinnerItemRenderer;

/**
 * Component that presents a means of cycling through a list of items.
 */
public class Spinner extends Container {
    /**
     * Spinner renderer interface.
     */
    public interface ItemRenderer extends Renderer {
        /**
         * Prepares the renderer for layout or paint.
         *
         * @param item
         * The item to render, or <tt>null</tt> if called to calculate preferred
         * height for skins that assume a fixed renderer height.
         *
         * @param spinner
         * The host component.
         */
        public void render(Object item, Spinner spinner);

        /**
         * Converts a spinner item to a string representation.
         *
         * @param item
         *
         * @return
         * The item's string representation, or <tt>null</tt> if the item does not
         * have a string representation.
         * <p>
         * Note that this method may be called often during keyboard navigation, so
         * implementations should avoid unnecessary string allocations.
         */
        public String toString(Object item);
    }

    /**
     * Spinner skin interface. Spinner skins must implement this interface to
     * facilitate additional communication between the component and the skin.
     */
    public interface Skin {
        public Bounds getContentBounds();
    }

    /**
     * Translates between spinner and bind context data during data binding.
     */
    public interface SelectedItemBindMapping {
        /**
         * Returns the index of the item in the source list.
         *
         * @param spinnerData
         * The source spinner data.
         *
         * @param item
         * The item to locate.
         *
         * @return
         * The index of first occurrence of the item if it exists in the list;
         * <tt>-1</tt>, otherwise.
         */
        public int indexOf(List<?> spinnerData, Object item);

        /**
         * Retrieves the item at the given index.
         *
         * @param listData
         * The source spinner data.
         *
         * @param index
         * The index of the item to retrieve.
         */
        public Object get(List<?> spinnerData, int index);
    }

    /**
     * List event handler.
     */
    private class ListHandler implements ListListener<Object> {
        @Override
        public void itemInserted(List<Object> list, int index) {
            if (index <= selectedIndex) {
                selectedIndex++;
            }

            // Notify listeners that items were inserted
            spinnerItemListeners.itemInserted(Spinner.this, index);
        }

        @Override
        public void itemsRemoved(List<Object> list, int index, Sequence<Object> items) {
            int count = items.getLength();

            if (index + count <= selectedIndex) {
                selectedIndex--;
            } else if (index <= selectedIndex) {
                selectedIndex = -1;
            }

            // Notify listeners that items were removed
            spinnerItemListeners.itemsRemoved(Spinner.this, index, count);
        }

        @Override
        public void itemUpdated(List<Object> list, int index, Object previousItem) {
            spinnerItemListeners.itemUpdated(Spinner.this, index);
        }

        @Override
        public void listCleared(List<Object> list) {
            // All items were removed; clear the selection and notify
            // listeners
            selectedIndex = -1;
            spinnerItemListeners.itemsCleared(Spinner.this);
        }

        @Override
        public void comparatorChanged(List<Object> list,
            Comparator<Object> previousComparator) {
            if (list.getComparator() != null) {
                selectedIndex = -1;
                spinnerItemListeners.itemsSorted(Spinner.this);
            }
        }
    }

    /**
     * Spinner listener list.
     */
    private static class SpinnerListenerList extends ListenerList<SpinnerListener>
        implements SpinnerListener {
        @Override
        public void spinnerDataChanged(Spinner spinner, List<?> previousSpinnerData) {
            for (SpinnerListener listener : this) {
                listener.spinnerDataChanged(spinner, previousSpinnerData);
            }
        }

        @Override
        public void itemRendererChanged(Spinner spinner,
            Spinner.ItemRenderer previousItemRenderer) {
            for (SpinnerListener listener : this) {
                listener.itemRendererChanged(spinner, previousItemRenderer);
            }
        }

        @Override
        public void circularChanged(Spinner spinner) {
            for (SpinnerListener listener : this) {
                listener.circularChanged(spinner);
            }
        }

        @Override
        public void selectedItemKeyChanged(Spinner spinner, String previousSelectedItemKey) {
            for (SpinnerListener listener : this) {
                listener.selectedItemKeyChanged(spinner, previousSelectedItemKey);
            }
        }

        @Override
        public void selectedItemBindMappingChanged(Spinner spinner, SelectedItemBindMapping previousSelectedItemBindMapping) {
            for (SpinnerListener listener : this) {
                listener.selectedItemBindMappingChanged(spinner, previousSelectedItemBindMapping);
            }
        }
    }

    /**
     * Spinner item listener list.
     */
    private static class SpinnerItemListenerList extends ListenerList<SpinnerItemListener>
        implements SpinnerItemListener {
        @Override
        public void itemInserted(Spinner spinner, int index) {
            for (SpinnerItemListener listener : this) {
                listener.itemInserted(spinner, index);
            }
        }

        @Override
        public void itemsRemoved(Spinner spinner, int index, int count) {
            for (SpinnerItemListener listener : this) {
                listener.itemsRemoved(spinner, index, count);
            }
        }

        @Override
        public void itemUpdated(Spinner spinner, int index) {
            for (SpinnerItemListener listener : this) {
                listener.itemUpdated(spinner, index);
            }
        }

        @Override
        public void itemsCleared(Spinner spinner) {
            for (SpinnerItemListener listener : this) {
                listener.itemsCleared(spinner);
            }
        }

        @Override
        public void itemsSorted(Spinner spinner) {
            for (SpinnerItemListener listener : this) {
                listener.itemsSorted(spinner);
            }
        }
    }

    /**
     * Spinner selection listener list.
     */
    private static class SpinnerSelectionListenerList
        extends ListenerList<SpinnerSelectionListener>
        implements SpinnerSelectionListener {
        @Override
        public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex) {
            for (SpinnerSelectionListener listener : this) {
                listener.selectedIndexChanged(spinner, previousSelectedIndex);
            }
        }
    }

    private List<?> spinnerData = null;
    private ListHandler spinnerDataHandler = new ListHandler();

    private ItemRenderer itemRenderer = null;

    private boolean circular = false;
    private int selectedIndex = -1;
    private String selectedItemKey = null;
    private SelectedItemBindMapping selectedItemBindMapping = null;

    private SpinnerListenerList spinnerListeners = new SpinnerListenerList();
    private SpinnerItemListenerList spinnerItemListeners = new SpinnerItemListenerList();
    private SpinnerSelectionListenerList spinnerSelectionListeners =
        new SpinnerSelectionListenerList();

    /**
     * Creates a spinner populated with an empty array list.
     */
    public Spinner() {
        this(new ArrayList<Object>());
    }

    /**
     * Creates a spinner populated with the given spinner data.
     *
     * @param spinnerData
     */
    public Spinner(List<?> spinnerData) {
        setItemRenderer(new SpinnerItemRenderer());
        setSpinnerData(spinnerData);

        installThemeSkin(Spinner.class);
    }

    /**
     * Returns the spinner data.
     *
     * @return
     * The data currently presented by the spinner.
     */
    public List<?> getSpinnerData() {
        return spinnerData;
    }

    /**
     * Sets the spinner data. Clears any existing selection state.
     *
     * @param spinnerData
     * The data to be presented by the spinner.
     */
    @SuppressWarnings("unchecked")
    public void setSpinnerData(List<?> spinnerData) {
        if (spinnerData == null) {
            throw new IllegalArgumentException("spinnerData is null.");
        }

        List<?> previousSpinnerData = this.spinnerData;

        if (previousSpinnerData != spinnerData) {
            if (previousSpinnerData != null) {
                // Clear any existing selection
                selectedIndex = -1;

                ((List<Object>)previousSpinnerData).getListListeners().remove(spinnerDataHandler);
            }

            ((List<Object>)spinnerData).getListListeners().add(spinnerDataHandler);

            // Update the spinner data and fire change event
            this.spinnerData = spinnerData;
            spinnerListeners.spinnerDataChanged(this, previousSpinnerData);
        }
    }

    public final void setSpinnerData(String spinnerData) {
        if (spinnerData == null) {
            throw new IllegalArgumentException("spinnerData is null.");
        }

        try {
            setSpinnerData(JSONSerializer.parseList(spinnerData));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    @Override
    protected void setSkin(org.apache.pivot.wtk.Skin skin) {
        if (!(skin instanceof Spinner.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + Spinner.Skin.class.getName());
        }

        super.setSkin(skin);
    }

    /**
     * Returns the item renderer used for items in this list.
     */
    public ItemRenderer getItemRenderer() {
        return itemRenderer;
    }

    /**
     * Sets the item renderer to be used for items in this list.
     *
     * @param itemRenderer
     * The item renderer for the list.
     */
    public void setItemRenderer(ItemRenderer itemRenderer) {
        if (itemRenderer == null) {
            throw new IllegalArgumentException("itemRenderer is null.");
        }

        ItemRenderer previousItemRenderer = this.itemRenderer;

        if (previousItemRenderer != itemRenderer) {
            this.itemRenderer = itemRenderer;
            spinnerListeners.itemRendererChanged(this, previousItemRenderer);
        }
    }

    /**
     */
    public boolean isCircular() {
        return circular;
    }

    /**
     */
    public void setCircular(boolean circular) {
        if (circular != this.circular) {
            this.circular = circular;
            spinnerListeners.circularChanged(this);
        }
    }

    /**
     * Returns the currently selected index.
     *
     * @return
     * The currently selected index.
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Sets the selection to the specified index.
     *
     * @param selectedIndex
     * The index to select, or <tt>-1</tt> to clear the selection.
     */
    public void setSelectedIndex(int selectedIndex) {
        int previousSelectedIndex = this.selectedIndex;

        if (previousSelectedIndex != selectedIndex) {
            this.selectedIndex = selectedIndex;
            spinnerSelectionListeners.selectedIndexChanged(this, previousSelectedIndex);
        }
    }

    public Object getSelectedItem() {
        int index = getSelectedIndex();
        Object item = null;

        if (index >= 0) {
            item = spinnerData.get(index);
        }

        return item;
    }

    @SuppressWarnings("unchecked")
    public void setSelectedItem(Object item) {
        setSelectedIndex((item == null) ? -1 : ((List<Object>)spinnerData).indexOf(item));
    }

    /**
     * Gets the data binding key that is set on this spinner.
     */
    public String getSelectedItemKey() {
        return selectedItemKey;
    }

    /**
     * Sets this spinner's data binding key.
     */
    public void setSelectedItemKey(String selectedItemKey) {
        String previousSelectedItemKey = this.selectedItemKey;

        if (previousSelectedItemKey != selectedItemKey) {
            this.selectedItemKey = selectedItemKey;
            spinnerListeners.selectedItemKeyChanged(this, previousSelectedItemKey);
        }
    }

    public SelectedItemBindMapping getSelectedItemBindMapping() {
        return selectedItemBindMapping;
    }

    public void setSelectedItemBindMapping(SelectedItemBindMapping selectedItemBindMapping) {
        SelectedItemBindMapping previousSelectedItemBindMapping = this.selectedItemBindMapping;

        if (previousSelectedItemBindMapping != selectedItemBindMapping) {
            this.selectedItemBindMapping = selectedItemBindMapping;
            spinnerListeners.selectedItemBindMappingChanged(this, previousSelectedItemBindMapping);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(Dictionary<String, ?> context) {
        if (selectedItemKey != null
            && JSON.containsKey(context, selectedItemKey)) {
            Object item = JSON.get(context, selectedItemKey);

            int index;
            if (selectedItemBindMapping == null) {
                index = ((List<Object>)spinnerData).indexOf(item);
            } else {
                index = selectedItemBindMapping.indexOf(spinnerData, item);
            }

            setSelectedIndex(index);
        }
    }

    @Override
    public void store(Dictionary<String, ?> context) {
        if (isEnabled()
            && selectedItemKey != null) {
            Object item;
            if (selectedIndex == -1) {
                item = null;
            } else {
                if (selectedItemBindMapping == null) {
                    item = spinnerData.get(selectedIndex);
                } else {
                    item = selectedItemBindMapping.get(spinnerData, selectedIndex);
                }
            }

            JSON.put(context, selectedItemKey, item);
        }
    }

    @Override
    public void clear() {
        if (selectedItemKey != null) {
            setSelectedItem(null);
        }
    }

    /**
     * Gets the bounding area of the spinner content (the area in which the
     * item renderer will render the content).
     *
     * @return
     * The bounding area of the spinner content.
     */
    public Bounds getContentBounds() {
        Spinner.Skin spinnerSkin = (Spinner.Skin)getSkin();
        return spinnerSkin.getContentBounds();
    }

    /**
     * Returns the spinner listener list.
     */
    public ListenerList<SpinnerListener> getSpinnerListeners() {
        return spinnerListeners;
    }

    /**
     * Returns the spinner item listener list.
     */
    public ListenerList<SpinnerItemListener> getSpinnerItemListeners() {
        return spinnerItemListeners;
    }

    /**
     * Returns the spinner selection listener list.
     */
    public ListenerList<SpinnerSelectionListener> getSpinnerSelectionListeners() {
        return spinnerSelectionListeners;
    }
}
