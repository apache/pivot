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

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.immutable.ImmutableList;
import org.apache.pivot.json.JSON;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.content.SpinnerItemRenderer;

/**
 * Component that presents a means of cycling through a list of items.
 */
@DefaultProperty("spinnerData")
public class Spinner extends Container {
    /**
     * {@link Renderer} interface to customize the appearance of the data in a
     * Spinner.
     */
    public interface ItemRenderer extends Renderer {
        /**
         * Prepares the renderer for layout or paint.
         *
         * @param item The item to render, or <tt>null</tt> if called to
         * calculate preferred height for skins that assume a fixed renderer
         * height.
         * @param spinner The host component.
         */
        public void render(Object item, Spinner spinner);

        /**
         * Converts a spinner item to a string representation.
         *
         * @param item The item from the spinner's data.
         * @return The item's string representation, or <tt>null</tt> if the item
         * does not have a string representation. <p> Note that this method may
         * be called often during keyboard navigation, so implementations should
         * avoid unnecessary string allocations.
         */
        public String toString(Object item);
    }

    /**
     * Translates between spinner and bind context data during data binding.
     */
    public interface SpinnerDataBindMapping {
        /**
         * Converts a context value to spinner data during a
         * {@link Component#load(Object)} operation.
         *
         * @param value The value retrieved from the user context.
         * @return That object converted to a list.
         */
        public List<?> toSpinnerData(Object value);

        /**
         * Converts spinner data to a context value during a
         * {@link Component#store(Object)} operation.
         *
         * @param spinnerData The spinner data list.
         * @return This list converted to data that can be stored in the user context.
         */
        public Object valueOf(List<?> spinnerData);
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
    public interface ItemBindMapping {
        /**
         * Returns the index of the item in the source list during a
         * {@link Component#load(Object)} operation.
         *
         * @param spinnerData The source spinner data.
         * @param value The value to locate.
         * @return The index of first occurrence of the value if it exists in the
         * list; <tt>-1</tt>, otherwise.
         */
        public int indexOf(List<?> spinnerData, Object value);

        /**
         * Retrieves the item at the given index during a
         * {@link Component#store(Object)} operation.
         *
         * @param spinnerData The source spinner data.
         * @param index The index of the value to retrieve.
         * @return The item at the given index.
         */
        public Object get(List<?> spinnerData, int index);
    }

    private List<?> spinnerData = null;

    private ItemRenderer itemRenderer = null;

    private boolean circular = false;
    private int selectedIndex = -1;

    private String spinnerDataKey = null;
    private BindType spinnerDataBindType = BindType.BOTH;
    private SpinnerDataBindMapping spinnerDataBindMapping = null;

    private String selectedItemKey = null;
    private BindType selectedItemBindType = BindType.BOTH;
    private ItemBindMapping selectedItemBindMapping = null;

    private ListListener<Object> spinnerDataListener = new ListListener<Object>() {
        @Override
        public void itemInserted(List<Object> list, int index) {
            int previousSelectedIndex = selectedIndex;
            if (index <= selectedIndex) {
                selectedIndex++;
            }

            // Notify listeners that items were inserted
            spinnerItemListeners.itemInserted(Spinner.this, index);

            if (selectedIndex != previousSelectedIndex) {
                spinnerSelectionListeners.selectedIndexChanged(Spinner.this, selectedIndex);
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

            spinnerItemListeners.itemsRemoved(Spinner.this, index, count);

            if (selectedIndex != previousSelectedIndex) {
                spinnerSelectionListeners.selectedIndexChanged(Spinner.this, selectedIndex);

                if (selectedIndex == -1) {
                    spinnerSelectionListeners.selectedItemChanged(Spinner.this, null);
                }
            }
        }

        @Override
        public void itemUpdated(List<Object> list, int index, Object previousItem) {
            spinnerItemListeners.itemUpdated(Spinner.this, index);
        }

        @Override
        public void listCleared(List<Object> list) {
            int previousSelectedIndex = selectedIndex;
            selectedIndex = -1;

            spinnerItemListeners.itemsCleared(Spinner.this);

            if (previousSelectedIndex != selectedIndex) {
                spinnerSelectionListeners.selectedIndexChanged(Spinner.this, selectedIndex);
                spinnerSelectionListeners.selectedItemChanged(Spinner.this, getSelectedItem());
            }
        }

        @Override
        public void comparatorChanged(List<Object> list, Comparator<Object> previousComparator) {
            if (list.getComparator() != null) {
                int previousSelectedIndex = selectedIndex;
                selectedIndex = -1;

                spinnerItemListeners.itemsSorted(Spinner.this);

                if (previousSelectedIndex != selectedIndex) {
                    spinnerSelectionListeners.selectedIndexChanged(Spinner.this, selectedIndex);
                    spinnerSelectionListeners.selectedItemChanged(Spinner.this, getSelectedItem());
                }
            }
        }
    };

    private SpinnerListener.Listeners spinnerListeners = new SpinnerListener.Listeners();
    private SpinnerItemListener.Listeners spinnerItemListeners = new SpinnerItemListener.Listeners();
    private SpinnerSelectionListener.Listeners spinnerSelectionListeners = new SpinnerSelectionListener.Listeners();
    private SpinnerBindingListener.Listeners spinnerBindingListeners = new SpinnerBindingListener.Listeners();

    private static final ItemRenderer DEFAULT_ITEM_RENDERER = new SpinnerItemRenderer();

    /**
     * Creates a spinner populated with an empty array list. <p> The default
     * contents is an {@link ImmutableList} so that if the default property
     * (which is "spinnerData") is invoked in a BXML file,
     * <code>BXMLSerializer</code> trying to add to this immutable sequence will
     * catch an exception and will do a {@link #setSpinnerData
     * setSpinnerData(List&lt;?&gt;)} instead.
     */
    public Spinner() {
        this(new ImmutableList<>(new ArrayList<>()));
    }

    /**
     * Creates a spinner populated with the given spinner data. <p> Note that
     * the default renderer uses (as last option) the toString method on list
     * elements, so override it to return whatever you want to display as text,
     * or implement your own custom renderer.
     *
     * @param spinnerData The data to set.
     * @see SpinnerItemRenderer
     */
    public Spinner(List<?> spinnerData) {
        setItemRenderer(DEFAULT_ITEM_RENDERER);
        setSpinnerData(spinnerData);

        installSkin(Spinner.class);
    }

    /**
     * @return The data currently presented by the spinner.
     */
    public List<?> getSpinnerData() {
        return spinnerData;
    }

    /**
     * Sets the spinner data. Clears any existing selection state.
     *
     * @param spinnerData The data to be presented by the spinner.
     */
    @SuppressWarnings("unchecked")
    public void setSpinnerData(List<?> spinnerData) {
        Utils.checkNull(spinnerData, "spinnerData");

        List<?> previousSpinnerData = this.spinnerData;

        if (previousSpinnerData != spinnerData) {
            int previousSelectedIndex = selectedIndex;

            if (previousSpinnerData != null) {
                // Clear any existing selection
                selectedIndex = -1;

                ((List<Object>) previousSpinnerData).getListListeners().remove(spinnerDataListener);
            }

            ((List<Object>) spinnerData).getListListeners().add(spinnerDataListener);

            // Update the spinner data and fire change event
            this.spinnerData = spinnerData;
            spinnerListeners.spinnerDataChanged(this, previousSpinnerData);

            if (selectedIndex != previousSelectedIndex) {
                spinnerSelectionListeners.selectedIndexChanged(Spinner.this, selectedIndex);
                spinnerSelectionListeners.selectedItemChanged(this, null);
            }
        }
    }

    public final void setSpinnerData(String spinnerData) {
        Utils.checkNullOrEmpty(spinnerData, "spinnerData");

        try {
            setSpinnerData(JSONSerializer.parseList(spinnerData));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    @Override
    protected void setSkin(org.apache.pivot.wtk.Skin skin) {
        checkSkin(skin, Spinner.Skin.class);

        super.setSkin(skin);
    }

    /**
     * @return The item renderer used for items in this list.
     */
    public ItemRenderer getItemRenderer() {
        return itemRenderer;
    }

    /**
     * Sets the item renderer to be used for items in this list.
     *
     * @param itemRenderer The new item renderer for the list.
     */
    public void setItemRenderer(ItemRenderer itemRenderer) {
        Utils.checkNull(itemRenderer, "itemRenderer");

        ItemRenderer previousItemRenderer = this.itemRenderer;

        if (previousItemRenderer != itemRenderer) {
            this.itemRenderer = itemRenderer;
            spinnerListeners.itemRendererChanged(this, previousItemRenderer);
        }
    }

    /**
     * @return Whether or not this spinner's values are circular.
     */
    public boolean isCircular() {
        return circular;
    }

    /**
     * Set whether the values in this spinner wrap around from the end
     * back to the beginning (and vice-versa).
     *
     * @param circular The new setting for this spinner.
     */
    public void setCircular(boolean circular) {
        if (circular != this.circular) {
            this.circular = circular;
            spinnerListeners.circularChanged(this);
        }
    }

    /**
     * @return The currently selected index.
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Sets the selection to the specified index.
     *
     * @param selectedIndex The index to select, or <tt>-1</tt> to clear the
     * selection.
     */
    public void setSelectedIndex(int selectedIndex) {
        indexBoundsCheck("selectedIndex", selectedIndex, -1, spinnerData.getLength() - 1);

        int previousSelectedIndex = this.selectedIndex;

        if (previousSelectedIndex != selectedIndex) {
            this.selectedIndex = selectedIndex;
            spinnerSelectionListeners.selectedIndexChanged(this, previousSelectedIndex);
            spinnerSelectionListeners.selectedItemChanged(this,
                (previousSelectedIndex == -1) ? null : spinnerData.get(previousSelectedIndex));
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
        setSelectedIndex((item == null) ? -1 : ((List<Object>) spinnerData).indexOf(item));
    }

    public String getSpinnerDataKey() {
        return spinnerDataKey;
    }

    public void setSpinnerDataKey(String spinnerDataKey) {
        String previousSpinnerDataKey = this.spinnerDataKey;
        if (previousSpinnerDataKey != spinnerDataKey) {
            this.spinnerDataKey = spinnerDataKey;
            spinnerBindingListeners.spinnerDataKeyChanged(this, previousSpinnerDataKey);
        }
    }

    public BindType getSpinnerDataBindType() {
        return spinnerDataBindType;
    }

    public void setSpinnerDataBindType(BindType spinnerDataBindType) {
        Utils.checkNull(spinnerDataBindType, "spinnerDataBindType");

        BindType previousSpinnerDataBindType = this.spinnerDataBindType;

        if (previousSpinnerDataBindType != spinnerDataBindType) {
            this.spinnerDataBindType = spinnerDataBindType;
            spinnerBindingListeners.spinnerDataBindTypeChanged(this, previousSpinnerDataBindType);
        }
    }

    public SpinnerDataBindMapping getSpinnerDataBindMapping() {
        return spinnerDataBindMapping;
    }

    public void setSpinnerDataBindMapping(SpinnerDataBindMapping spinnerDataBindMapping) {
        SpinnerDataBindMapping previousSpinnerDataBindMapping = this.spinnerDataBindMapping;

        if (previousSpinnerDataBindMapping != spinnerDataBindMapping) {
            this.spinnerDataBindMapping = spinnerDataBindMapping;
            spinnerBindingListeners.spinnerDataBindMappingChanged(this,
                previousSpinnerDataBindMapping);
        }
    }

    public String getSelectedItemKey() {
        return selectedItemKey;
    }

    public void setSelectedItemKey(String selectedItemKey) {
        String previousSelectedItemKey = this.selectedItemKey;

        if (previousSelectedItemKey != selectedItemKey) {
            this.selectedItemKey = selectedItemKey;
            spinnerBindingListeners.selectedItemKeyChanged(this, previousSelectedItemKey);
        }
    }

    public BindType getSelectedItemBindType() {
        return selectedItemBindType;
    }

    public void setSelectedItemBindType(BindType selectedItemBindType) {
        Utils.checkNull(selectedItemBindType, "selectedItemBindType");

        BindType previousSelectedItemBindType = this.selectedItemBindType;
        if (previousSelectedItemBindType != selectedItemBindType) {
            this.selectedItemBindType = selectedItemBindType;
            spinnerBindingListeners.selectedItemBindTypeChanged(this, previousSelectedItemBindType);
        }
    }

    public ItemBindMapping getSelectedItemBindMapping() {
        return selectedItemBindMapping;
    }

    public void setSelectedItemBindMapping(ItemBindMapping selectedItemBindMapping) {
        ItemBindMapping previousSelectedItemBindMapping = this.selectedItemBindMapping;

        if (previousSelectedItemBindMapping != selectedItemBindMapping) {
            this.selectedItemBindMapping = selectedItemBindMapping;
            spinnerBindingListeners.selectedItemBindMappingChanged(this,
                previousSelectedItemBindMapping);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(Object context) {
        // Bind to spinner data
        if (spinnerDataKey != null && spinnerDataBindType != BindType.STORE
            && JSON.containsKey(context, spinnerDataKey)) {
            Object value = JSON.get(context, spinnerDataKey);

            List<?> spinnerDataLocal;
            if (spinnerDataBindMapping == null) {
                spinnerDataLocal = (List<?>) value;
            } else {
                spinnerDataLocal = spinnerDataBindMapping.toSpinnerData(value);
            }

            setSpinnerData(spinnerDataLocal);
        }

        // Bind to selected item
        if (selectedItemKey != null && JSON.containsKey(context, selectedItemKey)
            && selectedItemBindType != BindType.STORE) {
            Object item = JSON.get(context, selectedItemKey);

            int index;
            if (selectedItemBindMapping == null) {
                index = ((List<Object>) spinnerData).indexOf(item);
            } else {
                index = selectedItemBindMapping.indexOf(spinnerData, item);
            }

            setSelectedIndex(index);
        }
    }

    @Override
    public void store(Object context) {
        // Bind to spinner data
        if (spinnerDataKey != null && spinnerDataBindType != BindType.LOAD) {
            Object value;
            if (spinnerDataBindMapping == null) {
                value = spinnerData;
            } else {
                value = spinnerDataBindMapping.valueOf(spinnerData);
            }

            JSON.put(context, spinnerDataKey, value);
        }

        // Bind to selected item
        if (selectedItemKey != null && selectedItemBindType != BindType.LOAD) {
            Object item;
            if (selectedItemBindMapping == null) {
                if (selectedIndex == -1) {
                    item = null;
                } else {
                    item = spinnerData.get(selectedIndex);
                }
            } else {
                item = selectedItemBindMapping.get(spinnerData, selectedIndex);
            }

            JSON.put(context, selectedItemKey, item);
        }
    }

    @Override
    public void clear() {
        if (spinnerDataKey != null) {
            setSpinnerData(new ArrayList<>());
        }

        if (selectedItemKey != null) {
            setSelectedItem(null);
        }
    }

    /**
     * Clears the selection.
     */
    public void clearSelection() {
        setSelectedItem(null);
    }

    /**
     * Gets the bounding area of the spinner content (the area in which the item
     * renderer will render the content).
     *
     * @return The bounding area of the spinner content.
     */
    public Bounds getContentBounds() {
        Spinner.Skin spinnerSkin = (Spinner.Skin) getSkin();
        return spinnerSkin.getContentBounds();
    }

    /**
     * @return The spinner listener list.
     */
    public ListenerList<SpinnerListener> getSpinnerListeners() {
        return spinnerListeners;
    }

    /**
     * @return The spinner item listener list.
     */
    public ListenerList<SpinnerItemListener> getSpinnerItemListeners() {
        return spinnerItemListeners;
    }

    /**
     * @return The spinner selection listener list.
     */
    public ListenerList<SpinnerSelectionListener> getSpinnerSelectionListeners() {
        return spinnerSelectionListeners;
    }

    /**
     * @return The spinner binding listener list.
     */
    public ListenerList<SpinnerBindingListener> getSpinnerBindingListeners() {
        return spinnerBindingListeners;
    }
}
