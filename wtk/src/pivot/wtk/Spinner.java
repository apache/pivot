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
import pivot.serialization.JSONSerializer;
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
     * Spinner skin interface. Spinner skins must implement this interface to
     * facilitate additional communication between the component and the skin.
     *
     * @author tvolkert
     */
    public interface Skin {
        public Bounds getContentBounds();
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
    private static class SpinnerListenerList extends ListenerList<SpinnerListener>
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
    }

    /**
     * Spinner item listener list.
     *
     * @author tvolkert
     */
    private static class SpinnerItemListenerList extends ListenerList<SpinnerItemListener>
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
    private static class SpinnerSelectionListenerList
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

    public void setSpinnerData(String spinnerData) {
        if (spinnerData == null) {
            throw new IllegalArgumentException("spinnerData is null.");
        }

        setSpinnerData(JSONSerializer.parseList(spinnerData));
    }

    @Override
    protected void setSkin(pivot.wtk.Skin skin) {
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

    public Object getSelectedValue() {
        int index = getSelectedIndex();
        Object value = null;

        if (index >= 0) {
            value = spinnerData.get(index);
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    public void setSelectedValue(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        int index = ((List<Object>)spinnerData).indexOf(value);
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
     * Adds a listener to the spinner listener list.
     *
     * @param listener
     */
    public void setSpinnerListener(SpinnerListener listener) {
        spinnerListeners.add(listener);
    }

    /**
     * Returns the spinner item listener list.
     */
    public ListenerList<SpinnerItemListener> getSpinnerItemListeners() {
        return spinnerItemListeners;
    }

    /**
     * Adds a listener to the spinner item listener list.
     *
     * @param listener
     */
    public void setSpinnerItemListener(SpinnerItemListener listener) {
        spinnerItemListeners.add(listener);
    }

    /**
     * Returns the spinner selection listener list.
     */
    public ListenerList<SpinnerSelectionListener> getSpinnerSelectionListeners() {
        return spinnerSelectionListeners;
    }

    /**
     * Adds a listener to the spinner selection listener list.
     *
     * @param listener
     */
    public void setSpinnerSelectionListener(SpinnerSelectionListener listener) {
        spinnerSelectionListeners.add(listener);
    }
}
