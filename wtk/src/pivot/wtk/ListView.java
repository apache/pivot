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
import pivot.util.Vote;
import pivot.wtk.content.ListViewItemRenderer;

/**
 * Component that displays a sequence of items, optionally allowing a user
 * to select or check one or more items.
 *
 * @author gbrown
 */
@ComponentInfo(icon="ListView.png")
public class ListView extends Component {
    /**
     * Enumeration defining supported selection modes.
     */
    public enum SelectMode {
        /**
         * Selection is disabled.
         */
        NONE,

        /**
         * A single index may be selected at a time.
         */
        SINGLE,

        /**
         * Multiple indexes may be concurrently selected.
         */
        MULTI;

        public static SelectMode decode(String value) {
            return valueOf(value.toUpperCase());
        }
    }

    /**
     * List item renderer interface.
     *
     * @author gbrown
     */
    public interface ItemRenderer extends Renderer {
        /**
         * Prepares the renderer for layout or paint.
         *
         * @param item
         * The item to render.
         *
         * @param listView
         * The host component.
         *
         * @param selected
         * If <tt>true</tt>, the renderer should present a selected state for
         * the item.
         *
         * @param highlighted
         * If <tt>true</tt>, the renderer should present a highlighted state
         * for the item.
         */
        public void render(Object item, ListView listView, boolean selected,
            boolean highlighted, boolean disabled);
    }

    /**
     * List view skin interface. List view skins are required to implement
     * this.
     *
     * @author gbrown
     */
    public interface Skin {
        public int getItemAt(int y);
        public Bounds getItemBounds(int index);
    }

    /**
     * List event handler.
     *
     * @author gbrown
     */
    private class ListHandler implements ListListener<Object> {
        public void itemInserted(List<Object> list, int index) {
            int m = selectedRanges.insertIndex(index);

            // Notify listeners that items were inserted
            listViewItemListeners.itemInserted(ListView.this, index);

            // If any spans were modified, notify listeners of selection change
            if (m > 0) {
                listViewSelectionListeners.selectionChanged(ListView.this);
            }
        }

        public void itemsRemoved(List<Object> list, int index, Sequence<Object> items) {
            if (items == null) {
                // All items were removed; clear the selection and notify
                // listeners
                selectedRanges.clear();
                listViewItemListeners.itemsRemoved(ListView.this, index, -1);
                listViewSelectionListeners.selectionChanged(ListView.this);
            } else {
                int count = items.getLength();

                int s = selectedRanges.getLength();
                int m = selectedRanges.removeIndexes(index, count);

                // Notify listeners that items were removed
                listViewItemListeners.itemsRemoved(ListView.this, index, count);

                // If any selection values were removed or any spans were modified,
                // notify listeners of selection change
                if (s != selectedRanges.getLength()
                    || m > 0) {
                    listViewSelectionListeners.selectionChanged(ListView.this);
                }
            }
        }

        public void itemUpdated(List<Object> list, int index, Object previousItem) {
            listViewItemListeners.itemUpdated(ListView.this, index);
        }

        public void comparatorChanged(List<Object> list,
            Comparator<Object> previousComparator) {
            if (list.getComparator() != null) {
                int s = selectedRanges.getLength();
                selectedRanges.clear();

                listViewItemListeners.itemsSorted(ListView.this);

                if (s > 0) {
                    listViewSelectionListeners.selectionChanged(ListView.this);
                }
            }
        }
    }

    /**
     * List view listener list.
     *
     * @author gbrown
     */
    private static class ListViewListenerList extends ListenerList<ListViewListener> implements
            ListViewListener {
        public void listDataChanged(ListView listView, List<?> previousListData) {
            for (ListViewListener listener : this) {
                listener.listDataChanged(listView, previousListData);
            }
        }

        public void itemRendererChanged(ListView listView,
            ListView.ItemRenderer previousItemRenderer) {
            for (ListViewListener listener : this) {
                listener.itemRendererChanged(listView, previousItemRenderer);
            }
        }

        public void selectModeChanged(ListView listView,
            ListView.SelectMode previousSelectMode) {
            for (ListViewListener listener : this) {
                listener.selectModeChanged(listView, previousSelectMode);
            }
        }

        public void checkmarksEnabledChanged(ListView listView) {
            for (ListViewListener listener : this) {
                listener.checkmarksEnabledChanged(listView);
            }
        }

        public void selectedValueKeyChanged(ListView listView, String previousSelectedValueKey) {
            for (ListViewListener listener : this) {
                listener.selectedValueKeyChanged(listView, previousSelectedValueKey);
            }
        }

        public void selectedValuesKeyChanged(ListView listView, String previousSelectedValuesKey) {
            for (ListViewListener listener : this) {
                listener.selectedValuesKeyChanged(listView, previousSelectedValuesKey);
            }
        }
    }

    /**
     * List view item listener list.
     *
     * @author gbrown
     */
    private static class ListViewItemListenerList extends ListenerList<ListViewItemListener>
        implements ListViewItemListener {
        public void itemInserted(ListView listView, int index) {
            for (ListViewItemListener listener : this) {
                listener.itemInserted(listView, index);
            }
        }

        public void itemsRemoved(ListView listView, int index, int count) {
            for (ListViewItemListener listener : this) {
                listener.itemsRemoved(listView, index, count);
            }
        }

        public void itemUpdated(ListView listView, int index) {
            for (ListViewItemListener listener : this) {
                listener.itemUpdated(listView, index);
            }
        }

        public void itemsSorted(ListView listView) {
            for (ListViewItemListener listener : this) {
                listener.itemsSorted(listView);
            }
        }
    }

    /**
     * List view item state listener list.
     *
     * @author gbrown
     */
    private static class ListViewItemStateListenerList extends ListenerList<ListViewItemStateListener>
        implements ListViewItemStateListener {
        public Vote previewItemDisabledChange(ListView listView, int index) {
            Vote vote = Vote.APPROVE;

            for (ListViewItemStateListener listener : this) {
                vote = vote.tally(listener.previewItemDisabledChange(listView, index));
            }

            return vote;
        }

        public void itemDisabledChangeVetoed(ListView listView, int index, Vote reason) {
            for (ListViewItemStateListener listener : this) {
                listener.itemDisabledChangeVetoed(listView, index, reason);
            }
        }

        public void itemDisabledChanged(ListView listView, int index) {
            for (ListViewItemStateListener listener : this) {
                listener.itemDisabledChanged(listView, index);
            }
        }

        public Vote previewItemCheckedChange(ListView listView, int index) {
            Vote vote = Vote.APPROVE;

            for (ListViewItemStateListener listener : this) {
                vote = vote.tally(listener.previewItemCheckedChange(listView, index));
            }

            return vote;
        }

        public void itemCheckedChangeVetoed(ListView listView, int index, Vote reason) {
            for (ListViewItemStateListener listener : this) {
                listener.itemCheckedChangeVetoed(listView, index, reason);
            }
        }

        public void itemCheckedChanged(ListView listView, int index) {
            for (ListViewItemStateListener listener : this) {
                listener.itemCheckedChanged(listView, index);
            }
        }
    }

    /**
     * List view selection listener list.
     *
     * @author gbrown
     */
    private static class ListViewSelectionListenerList extends ListenerList<ListViewSelectionListener>
        implements ListViewSelectionListener {
        public void selectionChanged(ListView listView) {
            for (ListViewSelectionListener listener : this) {
                listener.selectionChanged(listView);
            }
        }
    }

    /**
     * List view selection detail listener list.
     *
     * @author gbrown
     */
    private static class ListViewSelectionDetailListenerList extends ListenerList<ListViewSelectionDetailListener>
        implements ListViewSelectionDetailListener {
        public void selectedRangeAdded(ListView listView, int rangeStart, int rangeEnd) {
            for (ListViewSelectionDetailListener listener : this) {
                listener.selectedRangeAdded(listView, rangeStart, rangeEnd);
            }
        }

        public void selectedRangeRemoved(ListView listView, int rangeStart, int rangeEnd) {
            for (ListViewSelectionDetailListener listener : this) {
                listener.selectedRangeRemoved(listView, rangeStart, rangeEnd);
            }
        }

        public void selectionReset(ListView listView, Sequence<Span> previousSelection) {
            for (ListViewSelectionDetailListener listener : this) {
                listener.selectionReset(listView, previousSelection);
            }
        }
    }

    private List<?> listData = null;
    private ListHandler listDataHandler = new ListHandler();

    private ItemRenderer itemRenderer = null;

    private SpanSequence selectedRanges = new SpanSequence();
    private SelectMode selectMode = SelectMode.SINGLE;

    private boolean checkmarksEnabled = false;
    private ArrayList<Integer> checkedIndexes = new ArrayList<Integer>();

    private ArrayList<Integer> disabledIndexes = new ArrayList<Integer>();

    private String selectedValueKey = null;
    private String selectedValuesKey = null;

    private ListViewListenerList listViewListeners = new ListViewListenerList();
    private ListViewItemListenerList listViewItemListeners = new ListViewItemListenerList();
    private ListViewItemStateListenerList listViewItemStateListeners =
        new ListViewItemStateListenerList();
    private ListViewSelectionListenerList listViewSelectionListeners =
        new ListViewSelectionListenerList();
    private ListViewSelectionDetailListenerList listViewSelectionDetailListeners =
        new ListViewSelectionDetailListenerList();

    private static final ItemRenderer DEFAULT_ITEM_RENDERER = new ListViewItemRenderer();

    /**
     * Creates a list view populated with an empty array list.
     */
    public ListView() {
        this(new ArrayList<Object>());
    }

    /**
     * Creates a list view populated with the given list data.
     *
     * @param listData
     */
    public ListView(List<?> listData) {
        setItemRenderer(DEFAULT_ITEM_RENDERER);
        setListData(listData);

        installSkin(ListView.class);
    }

    /**
     * Returns the list data.
     *
     * @return
     * The data currently presented by the list view.
     */
    public List<?> getListData() {
        return this.listData;
    }

    /**
     * Sets the list data.
     *
     * @param listData
     * The data to be presented by the list view.
     */
    @SuppressWarnings("unchecked")
    public void setListData(List<?> listData) {
        if (listData == null) {
            throw new IllegalArgumentException("listData is null.");
        }

        List<?> previousListData = this.listData;

        if (previousListData != listData) {
            if (previousListData != null) {
                // Clear any existing selection
                clearSelection();

                ((List<Object>)previousListData).getListListeners().remove(listDataHandler);
            }

            ((List<Object>)listData).getListListeners().add(listDataHandler);

            // Update the list data and fire change event
            this.listData = listData;
            listViewListeners.listDataChanged(this, previousListData);
        }
    }

    /**
     * Sets the list data.
     *
     * @param listData
     * The data to be presented by the list view as a JSON array.
     */
    public void setListData(String listData) {
        if (listData == null) {
            throw new IllegalArgumentException("listData is null.");
        }

        setListData(JSONSerializer.parseList(listData));
    }

    @Override
    protected void setSkin(pivot.wtk.Skin skin) {
        if (!(skin instanceof ListView.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + ListView.Skin.class.getName());
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
            listViewListeners.itemRendererChanged(this, previousItemRenderer);
        }
    }

    /**
     * When in single-select mode, returns the currently selected index.
     *
     * @return
     * The currently selected index.
     */
    public int getSelectedIndex() {
        if (selectMode != SelectMode.SINGLE) {
            throw new IllegalStateException("List view is not in single-select mode.");
        }

        return (selectedRanges.getLength() == 0) ? -1 : selectedRanges.get(0).getStart();
    }

    /**
     * Sets the selection to a single index.
     *
     * @param index
     * The index to select, or <tt>-1</tt> to clear the selection.
     */
    public void setSelectedIndex(int index) {
        ArrayList<Span> selectedRanges = new ArrayList<Span>();

        if (index >= 0) {
            selectedRanges.add(new Span(index, index));
        }

        setSelectedRanges(selectedRanges);
    }

    /**
     * Returns the list's current selection.
     */
    public Sequence<Span> getSelectedRanges() {
        // Return a copy of the selection list (including copies of the
        // list contents)
        ArrayList<Span> selection = new ArrayList<Span>();

        for (int i = 0, n = selectedRanges.getLength(); i < n; i++) {
            selection.add(new Span(selectedRanges.get(i)));
        }

        return selection;
    }

    /**
     * Sets the selection to the given span sequence. Any overlapping or
     * connecting spans will be consolidated, and the resulting selection will
     * be sorted in ascending order.
     *
     * @param selectedRanges
     */
    public void setSelectedRanges(Sequence<Span> selectedRanges) {
        if (selectedRanges == null) {
            throw new IllegalArgumentException("selectedRanges is null.");
        }

        if (selectMode == SelectMode.NONE) {
            throw new IllegalArgumentException("Selection is not enabled.");
        }

        if (selectMode == SelectMode.SINGLE) {
            int n = selectedRanges.getLength();

            if (n > 1) {
                throw new IllegalArgumentException("Selection length is greater than 1.");
            } else {
                if (n > 0) {
                    Span selectedRange = selectedRanges.get(0);

                    if (selectedRange.getLength() > 1) {
                        throw new IllegalArgumentException("Selected range length is greater than 1.");
                    }
                }
            }
        }

        // Update the selection
        SpanSequence ranges = new SpanSequence();

        for (int i = 0, n = selectedRanges.getLength(); i < n; i++) {
            Span range = selectedRanges.get(i);

            if (range == null) {
                throw new IllegalArgumentException("range is null.");
            }

            if (range.getStart() < 0 || range.getEnd() >= listData.getLength()) {
                throw new IndexOutOfBoundsException();
            }

            ranges.add(range);
        }

        SpanSequence previousSelectedRanges = this.selectedRanges;
        this.selectedRanges = ranges;

        // Notify listeners
        listViewSelectionDetailListeners.selectionReset(this, previousSelectedRanges);
        listViewSelectionListeners.selectionChanged(this);
    }

    /**
     * Returns the first selected index.
     *
     * @return
     * The first selected index, or <tt>-1</tt> if nothing is selected.
     */
    public int getFirstSelectedIndex() {
        return (selectedRanges.getLength() > 0) ?
            selectedRanges.get(0).getStart() : -1;
    }

    /**
     * Returns the last selected index.
     *
     * @return
     * The last selected index, or <tt>-1</tt> if nothing is selected.
     */
    public int getLastSelectedIndex() {
        return (selectedRanges.getLength() > 0) ?
            selectedRanges.get(selectedRanges.getLength() - 1).getEnd() : -1;
    }

    /**
     * Adds a single index to the selection.
     *
     * @param index
     * The index to add.
     */
    public void addSelectedIndex(int index) {
        addSelectedRange(index, index);
    }

    /**
     * Adds a range of indexes to the selection.
     *
     * @param rangeStart
     * The first index in the range.
     *
     * @param rangeEnd
     * The last index in the range.
     */
    public void addSelectedRange(int rangeStart, int rangeEnd) {
        addSelectedRange(new Span(rangeStart, rangeEnd));
    }

    /**
     * Adds a range of indexes to the selection.
     *
     * @param range
     * The range to add.
     */
    public void addSelectedRange(Span range) {
        if (selectMode != SelectMode.MULTI) {
            throw new IllegalStateException("List view is not in multi-select mode.");
        }

        if (range == null) {
            throw new IllegalArgumentException("range is null.");
        }

        if (range.getStart() < 0 || range.getEnd() >= listData.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        selectedRanges.add(range);

        listViewSelectionDetailListeners.selectedRangeAdded(this,
            range.getStart(), range.getEnd());
        listViewSelectionListeners.selectionChanged(this);
    }

    /**
     * Removes a single index from the selection.
     *
     * @param index
     * The index to remove.
     */
    public void removeSelectedIndex(int index) {
        removeSelectedRange(index, index);
    }

    /**
     * Removes a range of indexes from the selection.
     *
     * @param rangeStart
     * The start of the range to remove.
     *
     * @param rangeEnd
     * The end of the range to remove.
     */
    public void removeSelectedRange(int rangeStart, int rangeEnd) {
        removeSelectedRange(new Span(rangeStart, rangeEnd));
    }

    /**
     * Removes a range of indexes from the selection.
     *
     * @param range
     * The range to remove.
     */
    public void removeSelectedRange(Span range) {
        if (selectMode != SelectMode.MULTI) {
            throw new IllegalStateException("List view is not in multi-select mode.");
        }

        if (range == null) {
            throw new IllegalArgumentException("range is null.");
        }

        if (range.getStart() < 0 || range.getEnd() >= listData.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        selectedRanges.remove(range);

        listViewSelectionDetailListeners.selectedRangeRemoved(this,
            range.getStart(), range.getEnd());
        listViewSelectionListeners.selectionChanged(this);
    }

    /**
     * Clears the selection.
     */
    public void clearSelection() {
        if (selectedRanges.getLength() > 0) {
            SpanSequence previousSelectedSpans = this.selectedRanges;
            selectedRanges = new SpanSequence();

            listViewSelectionDetailListeners.selectionReset(this, previousSelectedSpans);
            listViewSelectionListeners.selectionChanged(this);
        }
    }

    /**
     * Returns the selection state of a given index.
     *
     * @param index
     * The index whose selection state is to be tested.
     *
     * @return <tt>true</tt> if the index is selected; <tt>false</tt>,
     * otherwise.
     */
    public boolean isIndexSelected(int index) {
        return isRangeSelected(index, index);
    }

    /**
     * Returns the selection state of a given range.
     *
     * @param rangeStart
     * The first index in the range.
     *
     * @param rangeEnd
     * The last index in the range.
     *
     * @return <tt>true</tt> if the entire range is selected; <tt>false</tt>,
     * otherwise.
     */
    public boolean isRangeSelected(int rangeStart, int rangeEnd) {
        return isRangeSelected(new Span(rangeStart, rangeEnd));
    }

    /**
     * Returns the selection state of a given range.
     *
     * @param range
     * The range whose selection state is to be tested.
     *
     * @return <tt>true</tt> if the entire range is selected; <tt>false</tt>,
     * otherwise.
     */
    public boolean isRangeSelected(Span range) {
        boolean selected = false;

        if (range == null) {
            throw new IllegalArgumentException("range is null.");
        }

        if (range.getStart() < 0 || range.getEnd() >= listData.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        // Locate the span in the selection
        int i = selectedRanges.indexOf(range);

        // If the selected span contains the given span, it is considered
        // selected
        if (i >= 0) {
            Span selectedSpan = selectedRanges.get(i);
            selected = selectedSpan.contains(range);
        }

        return selected;
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

    public Sequence<Object> getSelectedValues() {
        ArrayList<Object> values = new ArrayList<Object>();

        for (int i = 0, n = selectedRanges.getLength(); i < n; i++) {
            Span span = selectedRanges.get(i);

            for (int index = span.getStart(), end = span.getEnd(); index <= end; index++) {
                Object value = listData.get(index);
                values.add(value);
            }
        }

        return values;
    }

    @SuppressWarnings("unchecked")
    public void setSelectedValues(Sequence<Object> values) {
        ArrayList<Span> selectedRanges = new ArrayList<Span>();

        for (int i = 0, n = values.getLength(); i < n; i++) {
            Object value = values.get(i);
            if (value == null) {
                throw new IllegalArgumentException("value is null");
            }

            int index = ((List<Object>)listData).indexOf(value);
            if (index == -1) {
                throw new IllegalArgumentException("\"" + value + "\" is not a valid selection.");
            }

            selectedRanges.add(new Span(index, index));
        }

        setSelectedRanges(selectedRanges);
    }

    /**
     * Returns the current selection mode.
     */
    public SelectMode getSelectMode() {
        return selectMode;
    }

    /**
     * Sets the selection mode. Clears the selection if the mode has changed
     * (but does not fire a selection change event).
     *
     * @param selectMode
     * The new selection mode.
     */
    public void setSelectMode(SelectMode selectMode) {
        if (selectMode == null) {
            throw new IllegalArgumentException("selectMode is null.");
        }

        SelectMode previousSelectMode = this.selectMode;

        if (previousSelectMode != selectMode) {
            // Clear any current selection
        	selectedRanges = new SpanSequence();

            // Update the selection mode
            this.selectMode = selectMode;

            // Fire select mode change event
            listViewListeners.selectModeChanged(this, previousSelectMode);
        }
    }

    /**
     * Sets the selection mode.
     *
     * @param selectMode
     *
     * @see #setSelectMode(pivot.wtk.ListView.SelectMode)
     */
    public void setSelectMode(String selectMode) {
        if (selectMode == null) {
            throw new IllegalArgumentException("selectMode is null.");
        }

        setSelectMode(SelectMode.decode(selectMode));
    }

    /**
     * Returns the current check mode.
     */
    public boolean getCheckmarksEnabled() {
    	return checkmarksEnabled;
    }

    /**
     * Enables or disabled checkmarks. Clears the check state if the check
     * mode has changed (but does not fire any check state change events).
     *
     * @param checkmarksEnabled
     */
    public void setCheckmarksEnabled(boolean checkmarksEnabled) {
        if (this.checkmarksEnabled != checkmarksEnabled) {
            // Clear any current check state
        	checkedIndexes.clear();

            // Update the check mode
            this.checkmarksEnabled = checkmarksEnabled;

            // Fire select mode change event
            listViewListeners.checkmarksEnabledChanged(this);
        }
    }

    /**
     * Returns an item's checked state.
     *
     * @param index
     */
    public boolean isItemChecked(int index) {
        return (Sequence.Search.binarySearch(checkedIndexes, index) >= 0);
    }

    /**
     * Sets an item's checked state.
     *
     * @param index
     * @param checked
     */
    public void setItemChecked(int index, boolean checked) {
    	if (!checkmarksEnabled) {
    		throw new IllegalStateException("Checkmarks are not enabled.");
    	}

        int i = Sequence.Search.binarySearch(checkedIndexes, index);

        if ((i < 0 && checked)
            || (i >= 0 && !checked)) {
            Vote vote = listViewItemStateListeners.previewItemCheckedChange(this, index);

            if (vote.isApproved()) {
                if (checked) {
                	checkedIndexes.insert(index, -(i + 1));
                } else {
                	checkedIndexes.remove(i, 1);
                }

                listViewItemStateListeners.itemCheckedChanged(this, index);
            } else {
                listViewItemStateListeners.itemCheckedChangeVetoed(this, index, vote);
            }
        }
    }

    /**
     * Returns the indexes of currently checked items.
     */
    public Sequence<Integer> getCheckedIndexes() {
        ArrayList<Integer> checkedIndexes = new ArrayList<Integer>();

        for (int i = 0, n = this.checkedIndexes.getLength(); i < n; i++) {
        	checkedIndexes.add(this.checkedIndexes.get(i));
        }

        return checkedIndexes;
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
    public boolean isItemDisabled(int index) {
        return (Sequence.Search.binarySearch(disabledIndexes, index) >= 0);
    }

    /**
     * Sets an item's disabled state.
     *
     * @param index
     * The index of the item whose disabled state is to be set.
     *
     * @param disabled
     * <tt>true</tt> to disable the item; <tt>false</tt>, otherwise.
     */
    public void setItemDisabled(int index, boolean disabled) {
        int i = Sequence.Search.binarySearch(disabledIndexes, index);

        if ((i < 0 && disabled)
            || (i >= 0 && !disabled)) {
            Vote vote = listViewItemStateListeners.previewItemDisabledChange(this, index);

            if (vote.isApproved()) {
                if (disabled) {
                    disabledIndexes.insert(index, -(i + 1));
                } else {
                    disabledIndexes.remove(i, 1);
                }

                listViewItemStateListeners.itemDisabledChanged(this, index);
            } else {
                listViewItemStateListeners.itemDisabledChangeVetoed(this, index, vote);
            }
        }
    }

    /**
     * Returns the indexes of currently disabled items.
     */
    public Sequence<Integer> getDisabledIndexes() {
        ArrayList<Integer> disabledIndexes = new ArrayList<Integer>();

        for (int i = 0, n = this.disabledIndexes.getLength(); i < n; i++) {
            disabledIndexes.add(this.disabledIndexes.get(i));
        }

        return disabledIndexes;
    }

    public String getSelectedValueKey() {
        return selectedValueKey;
    }

    public void setSelectedValueKey(String selectedValueKey) {
        String previousSelectedValueKey = this.selectedValueKey;
        this.selectedValueKey = selectedValueKey;
        listViewListeners.selectedValueKeyChanged(this, previousSelectedValueKey);
    }

    public String getSelectedValuesKey() {
        return selectedValuesKey;
    }

    public void setSelectedValuesKey(String selectedValuesKey) {
        String previousSelectedValuesKey = this.selectedValuesKey;
        this.selectedValuesKey = selectedValuesKey;
        listViewListeners.selectedValuesKeyChanged(this, previousSelectedValuesKey);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(Dictionary<String, Object> context) {
        if (selectedValueKey != null
            && context.containsKey(selectedValueKey)) {
            Object value = context.get(selectedValueKey);
            setSelectedValue(value);
        }

        if (selectedValuesKey != null
            && context.containsKey(selectedValuesKey)) {
            Sequence<Object> values = (Sequence<Object>)context.get(selectedValuesKey);
            setSelectedValues(values);
        }

        // TODO Add support for checked values
    }

    @Override
    public void store(Dictionary<String, Object> context) {
        if (selectedValueKey != null) {
            Object value = getSelectedValue();
            context.put(selectedValueKey, value);
        }

        if (selectedValuesKey != null) {
            Sequence<Object> values = getSelectedValues();
            context.put(selectedValuesKey, values);
        }

        // TODO Add support for checked values
    }

    /**
     * Returns the index of the item at a given location.
     *
     * @param y
     * The y-coordinate of the item to identify.
     *
     * @return
     * The item index, or <tt>-1</tt> if there is no item at the given
     * y-coordinate.
     */
    public int getItemAt(int y) {
        ListView.Skin listViewSkin = (ListView.Skin)getSkin();
        return listViewSkin.getItemAt(y);
    }

    /**
     * Returns the bounding area of a given item.
     *
     * @param index
     * The item index.
     *
     * @return
     * The bounding area of the item.
     */
    public Bounds getItemBounds(int index) {
        ListView.Skin listViewSkin = (ListView.Skin)getSkin();
        return listViewSkin.getItemBounds(index);
    }

    /**
     * Returns the list view listener list.
     */
    public ListenerList<ListViewListener> getListViewListeners() {
        return listViewListeners;
    }

    /**
     * Returns the list view item listener list.
     */
    public ListenerList<ListViewItemListener> getListViewItemListeners() {
        return listViewItemListeners;
    }

    /**
     * Returns the list view item state listener list.
     */
    public ListenerList<ListViewItemStateListener> getListViewItemStateListeners() {
        return listViewItemStateListeners;
    }

    /**
     * Returns the list view selection listener list.
     */
    public ListenerList<ListViewSelectionListener> getListViewSelectionListeners() {
        return listViewSelectionListeners;
    }

    /**
     * Returns the list view selection detail listener list.
     */
    public ListenerList<ListViewSelectionDetailListener> getListViewSelectionDetailListeners() {
        return listViewSelectionDetailListeners;
    }
}
