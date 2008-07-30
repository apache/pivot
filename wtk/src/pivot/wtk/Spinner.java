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

import java.util.Comparator;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ListenerList;
import pivot.wtk.content.SpinnerItemRenderer;

/**
 * Component that presents a means of cycling through a list of items.
 *
 * @author tvolkert
 */
@ComponentInfo(icon="Spinner.png")
public class Spinner extends Container {
    /**
     * Spinner renderer interface.
     *
     * @author tvolkert
     */
    public interface ItemRenderer extends Renderer {
        /**
         * Prepares the renderer for layout or paint.
         *
         * @param item
         * The item to render.
         *
         * @param spinner
         * The host component.
         */
        public void render(Object item, Spinner spinner);
    }

    /**
     * Maps spinner item data to and from their ordinal values.
     *
     * @author tvolkert
     */
    public interface ValueMapping {
        public int indexOf(List<?> list, Object value);
        public Object valueOf(List<?> list, int index);
    }

    /**
     * List event handler.
     *
     * @author tvolkert
     */
    private class ListHandler implements ListListener<Object> {
        public void itemInserted(List<Object> list, int index) {
            int previousSelectedIndex = selectedIndex;

            if (index <= selectedIndex) {
                selectedIndex++;
            }

            // Notify listeners that items were inserted
            spinnerItemListeners.itemInserted(Spinner.this, index);

            // If the selection was modified, notify listeners of selection
            // change
            if (previousSelectedIndex != selectedIndex) {
                spinnerSelectionListeners.selectedIndexChanged(Spinner.this,
                    previousSelectedIndex);
            }
        }

        public void itemsRemoved(List<Object> list, int index, Sequence<Object> items) {
            int previousSelectedIndex = selectedIndex;

            if (items == null) {
                // All items were removed; clear the selection and notify
                // listeners
                selectedIndex = -1;
                spinnerItemListeners.itemsRemoved(Spinner.this, index, -1);
            } else {
                int count = items.getLength();

                if (index + count <= selectedIndex) {
                    selectedIndex--;
                } else if (index <= selectedIndex) {
                    selectedIndex = -1;
                }

                // Notify listeners that items were removed
                spinnerItemListeners.itemsRemoved(Spinner.this, index, count);
            }

            // Notify listeners of selection change if necessary
            if (previousSelectedIndex != selectedIndex) {
                spinnerSelectionListeners.selectedIndexChanged(Spinner.this,
                    previousSelectedIndex);
            }
        }

        public void itemUpdated(List<Object> list, int index, Object previousItem) {
            spinnerItemListeners.itemUpdated(Spinner.this, index);
        }

        public void comparatorChanged(List<Object> list,
            Comparator<Object> previousComparator) {
            if (list.getComparator() != null) {
                int previousSelectedIndex = selectedIndex;

                selectedIndex = -1;
                spinnerItemListeners.itemsSorted(Spinner.this);

                if (previousSelectedIndex != selectedIndex) {
                    spinnerSelectionListeners.selectedIndexChanged(Spinner.this,
                        previousSelectedIndex);
                }
            }
        }
    }

    /**
     * Spinner listener list.
     *
     * @author tvolkert
     */
    private class SpinnerListenerList extends ListenerList<SpinnerListener>
        implements SpinnerListener {
        public void spinnerDataChanged(Spinner spinner, List<?> previousSpinnerData) {
            for (SpinnerListener listener : this) {
                listener.spinnerDataChanged(spinner, previousSpinnerData);
            }
        }

        public void itemRendererChanged(Spinner spinner,
            Spinner.ItemRenderer previousItemRenderer) {
            for (SpinnerListener listener : this) {
                listener.itemRendererChanged(spinner, previousItemRenderer);
            }
        }

        public void circularChanged(Spinner spinner) {
            for (SpinnerListener listener : this) {
                listener.circularChanged(spinner);
            }
        }

        public void selectedValueKeyChanged(Spinner spinner,
            String previousSelectedValueKey) {
            for (SpinnerListener listener : this) {
                listener.selectedValueKeyChanged(spinner, previousSelectedValueKey);
            }
        }

        public void valueMappingChanged(Spinner spinner,
            Spinner.ValueMapping previousValueMapping) {
            for (SpinnerListener listener : this) {
                listener.valueMappingChanged(spinner, previousValueMapping);
            }
        }
    }

    /**
     * Spinner item listener list.
     *
     * @author tvolkert
     */
    private class SpinnerItemListenerList extends ListenerList<SpinnerItemListener>
        implements SpinnerItemListener {
        public void itemInserted(Spinner spinner, int index) {
            for (SpinnerItemListener listener : this) {
                listener.itemInserted(spinner, index);
            }
        }

        public void itemsRemoved(Spinner spinner, int index, int count) {
            for (SpinnerItemListener listener : this) {
                listener.itemsRemoved(spinner, index, count);
            }
        }

        public void itemUpdated(Spinner spinner, int index) {
            for (SpinnerItemListener listener : this) {
                listener.itemUpdated(spinner, index);
            }
        }

        public void itemsSorted(Spinner spinner) {
            for (SpinnerItemListener listener : this) {
                listener.itemsSorted(spinner);
            }
        }
    }

    /**
     * Spinner selection listener list.
     *
     * @author tvolkert
     */
    private class SpinnerSelectionListenerList
        extends ListenerList<SpinnerSelectionListener>
        implements SpinnerSelectionListener {
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

    private String selectedValueKey = null;

    private ValueMapping valueMapping = new ValueMapping() {
        @SuppressWarnings("unchecked")
        public int indexOf(List<?> list, Object value) {
            return ((List<Object>)list).indexOf(value);
        }

        public Object valueOf(List<?> list, int index) {
            return list.get(index);
        }
    };

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

        installSkin(Spinner.class);
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
                setSelectedIndex(-1);

                ((List<Object>)previousSpinnerData).getListListeners().remove(spinnerDataHandler);
            }

            ((List<Object>)spinnerData).getListListeners().add(spinnerDataHandler);

            // Update the spinner data and fire change event
            this.spinnerData = spinnerData;
            spinnerListeners.spinnerDataChanged(this, previousSpinnerData);
        }
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
     *
     */
    public boolean isCircular() {
        return circular;
    }

    /**
     *
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
     * @param index
     * The index to select, or <tt>-1</tt> to clear the selection.
     */
    public void setSelectedIndex(int selectedIndex) {
        int previousSelectedIndex = this.selectedIndex;

        if (previousSelectedIndex != selectedIndex) {
            this.selectedIndex = selectedIndex;
            spinnerSelectionListeners.selectedIndexChanged(this, previousSelectedIndex);
        }
    }

    /**
     *
     */
    public Object getSelectedValue() {
        int index = getSelectedIndex();
        Object value = null;

        if (index >= 0) {
            value = valueMapping.valueOf(spinnerData, index);
        }

        return value;
    }

    /**
     *
     */
    public void setSelectedValue(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        int index = valueMapping.indexOf(spinnerData, value);
        if (index == -1) {
            throw new IllegalArgumentException("\"" + value + "\" is not a valid selection.");
        }

        setSelectedIndex(index);
    }

    /**
     * Gets the data binding key that is set on this spinner.
     */
    public String getSelectedValueKey() {
        return selectedValueKey;
    }

    /**
     * Sets this spinner's data binding key.
     */
    public void setSelectedValueKey(String selectedValueKey) {
        String previousSelectedValueKey = this.selectedValueKey;

        if ((selectedValueKey == null ^ previousSelectedValueKey == null)
            || (selectedValueKey != null && !selectedValueKey.equals(previousSelectedValueKey))) {
            this.selectedValueKey = selectedValueKey;
            spinnerListeners.selectedValueKeyChanged(this, previousSelectedValueKey);
        }
    }

    /**
     *
     */
    public ValueMapping getValueMapping() {
        return valueMapping;
    }

    /**
     *
     */
    public void setValueMapping(ValueMapping valueMapping) {
        if (valueMapping == null) {
            throw new IllegalArgumentException("valueMapping is null.");
        }

        ValueMapping previousValueMapping = this.valueMapping;

        if (previousValueMapping != valueMapping) {
            this.valueMapping = valueMapping;
            spinnerListeners.valueMappingChanged(this, previousValueMapping);
        }
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
