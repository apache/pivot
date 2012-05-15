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
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.content.ListViewItemRenderer;

/**
 * Popup that presents a list of text suggestions to the user.
 */
public class SuggestionPopup extends Window {
    private static class SuggestionPopupListenerList extends WTKListenerList<SuggestionPopupListener>
        implements SuggestionPopupListener {
        @Override
        public void suggestionDataChanged(SuggestionPopup suggestionPopup,
            List<?> previousSuggestionData) {
            for (SuggestionPopupListener listener : this) {
                listener.suggestionDataChanged(suggestionPopup, previousSuggestionData);
            }
        }

        @Override
        public void suggestionRendererChanged(SuggestionPopup suggestionPopup,
            ListView.ItemRenderer previousSuggestionRenderer) {
            for (SuggestionPopupListener listener : this) {
                listener.suggestionRendererChanged(suggestionPopup, previousSuggestionRenderer);
            }
        }

        @Override
        public void listSizeChanged(SuggestionPopup suggestionPopup, int previousListSize) {
            for (SuggestionPopupListener listener : this) {
                listener.listSizeChanged(suggestionPopup, previousListSize);
            }
        }
    }

    private static class SuggestionPopupItemListenerList extends WTKListenerList<SuggestionPopupItemListener>
        implements SuggestionPopupItemListener {
        @Override
        public void itemInserted(SuggestionPopup suggestionPopup, int index) {
            for (SuggestionPopupItemListener listener : this) {
                listener.itemInserted(suggestionPopup, index);
            }
        }

        @Override
        public void itemsRemoved(SuggestionPopup suggestionPopup, int index, int count) {
            for (SuggestionPopupItemListener listener : this) {
                listener.itemsRemoved(suggestionPopup, index, count);
            }
        }

        @Override
        public void itemUpdated(SuggestionPopup suggestionPopup, int index) {
            for (SuggestionPopupItemListener listener : this) {
                listener.itemUpdated(suggestionPopup, index);
            }
        }

        @Override
        public void itemsCleared(SuggestionPopup suggestionPopup) {
            for (SuggestionPopupItemListener listener : this) {
                listener.itemsCleared(suggestionPopup);
            }
        }

        @Override
        public void itemsSorted(SuggestionPopup suggestionPopup) {
            for (SuggestionPopupItemListener listener : this) {
                listener.itemsSorted(suggestionPopup);
            }
        }
    }

    private static class SuggestionPopupSelectionListenerList extends WTKListenerList<SuggestionPopupSelectionListener>
        implements SuggestionPopupSelectionListener {
        @Override
        public void selectedIndexChanged(SuggestionPopup suggestionPopup,
            int previousSelectedIndex) {
            for (SuggestionPopupSelectionListener listener : this) {
                listener.selectedIndexChanged(suggestionPopup, previousSelectedIndex);
            }
        }

        @Override
        public void selectedSuggestionChanged(SuggestionPopup suggestionPopup, Object previousSelectedSuggestion) {
            for (SuggestionPopupSelectionListener listener : this) {
                listener.selectedSuggestionChanged(suggestionPopup, previousSelectedSuggestion);
            }
        }
    }

    private static class SuggestionPopupStateListenerList extends WTKListenerList<SuggestionPopupStateListener>
        implements SuggestionPopupStateListener {
        @Override
        public Vote previewSuggestionPopupClose(SuggestionPopup suggestionPopup, boolean result) {
            Vote vote = Vote.APPROVE;

            for (SuggestionPopupStateListener listener : this) {
                vote = vote.tally(listener.previewSuggestionPopupClose(suggestionPopup, result));
            }

            return vote;
        }

        @Override
        public void suggestionPopupCloseVetoed(SuggestionPopup suggestionPopup, Vote reason) {
            for (SuggestionPopupStateListener listener : this) {
                listener.suggestionPopupCloseVetoed(suggestionPopup, reason);
            }
        }

        @Override
        public void suggestionPopupClosed(SuggestionPopup suggestionPopup) {
            for (SuggestionPopupStateListener listener : this) {
                listener.suggestionPopupClosed(suggestionPopup);
            }
        }
    }

    private TextInput textInput = null;
    private SuggestionPopupCloseListener suggestionPopupCloseListener = null;

    private List<?> suggestionData;
    private ListView.ItemRenderer suggestionRenderer;
    private int selectedIndex = -1;
    private int listSize = -1;

    private boolean result = false;

    private boolean closing = false;

    private ListListener<Object> suggestionDataListener = new ListListener<Object>() {
        @Override
        public void itemInserted(List<Object> list, int index) {
            int previousSelectedIndex = selectedIndex;

            if (index <= selectedIndex) {
                selectedIndex++;
            }

            suggestionPopupItemListeners.itemInserted(SuggestionPopup.this, index);

            if (selectedIndex != previousSelectedIndex) {
                suggestionPopupSelectionListeners.selectedIndexChanged(SuggestionPopup.this, selectedIndex);
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

            suggestionPopupItemListeners.itemsRemoved(SuggestionPopup.this, index, count);

            if (selectedIndex != previousSelectedIndex) {
                suggestionPopupSelectionListeners.selectedIndexChanged(SuggestionPopup.this, selectedIndex);

                if (selectedIndex == -1) {
                    suggestionPopupSelectionListeners.selectedSuggestionChanged(SuggestionPopup.this, null);
                }
            }
        }

        @Override
        public void itemUpdated(List<Object> list, int index, Object previousItem) {
            suggestionPopupItemListeners.itemUpdated(SuggestionPopup.this, index);
        }

        @Override
        public void listCleared(List<Object> list) {
            int previousSelectedIndex = selectedIndex;
            selectedIndex = -1;

            suggestionPopupItemListeners.itemsCleared(SuggestionPopup.this);

            if (previousSelectedIndex != selectedIndex) {
                suggestionPopupSelectionListeners.selectedIndexChanged(SuggestionPopup.this, selectedIndex);
                suggestionPopupSelectionListeners.selectedSuggestionChanged(SuggestionPopup.this, getSelectedSuggestion());
            }
        }

        @Override
        public void comparatorChanged(List<Object> list, Comparator<Object> previousComparator) {
            if (list.getComparator() != null) {
                int previousSelectedIndex = selectedIndex;
                selectedIndex = -1;

                suggestionPopupItemListeners.itemsSorted(SuggestionPopup.this);

                if (previousSelectedIndex != selectedIndex) {
                    suggestionPopupSelectionListeners.selectedIndexChanged(SuggestionPopup.this, selectedIndex);
                    suggestionPopupSelectionListeners.selectedSuggestionChanged(SuggestionPopup.this, getSelectedSuggestion());
                }
            }
        }
    };

    private SuggestionPopupListenerList suggestionPopupListeners = new SuggestionPopupListenerList();
    private SuggestionPopupItemListenerList suggestionPopupItemListeners = new SuggestionPopupItemListenerList();
    private SuggestionPopupSelectionListenerList suggestionPopupSelectionListeners = new SuggestionPopupSelectionListenerList();
    private SuggestionPopupStateListenerList suggestionPopupStateListeners = new SuggestionPopupStateListenerList();

    private static final ListView.ItemRenderer DEFAULT_SUGGESTION_RENDERER = new ListViewItemRenderer();

    public SuggestionPopup() {
        this(new ArrayList<Object>());
    }

    public SuggestionPopup(List<?> suggestions) {
        setSuggestionRenderer(DEFAULT_SUGGESTION_RENDERER);
        setSuggestionData(suggestions);

        installSkin(SuggestionPopup.class);
    }

    /**
     * Returns the text input for which suggestions will be provided.
     */
    public TextInput getTextInput() {
        return textInput;
    }

    /**
     * Returns the list of suggestions presented by the popup.
     */
    public List<?> getSuggestionData() {
        return suggestionData;
    }

    /**
     * Sets the list of suggestions presented by the popup.
     *
     * @param suggestionData
     */
    @SuppressWarnings("unchecked")
    public void setSuggestionData(List<?> suggestionData) {
        if (suggestionData == null) {
            throw new IllegalArgumentException("suggestionData is null.");
        }

        List<?> previousSuggestionData = this.suggestionData;

        if (previousSuggestionData != suggestionData) {
            int previousSelectedIndex = selectedIndex;

            if (previousSuggestionData != null) {
                // Clear any existing selection
                selectedIndex = -1;

                ((List<Object>)previousSuggestionData).getListListeners().remove(suggestionDataListener);
            }

            ((List<Object>)suggestionData).getListListeners().add(suggestionDataListener);

            // Update the list data and fire change event
            this.suggestionData = suggestionData;
            suggestionPopupListeners.suggestionDataChanged(this, previousSuggestionData);

            if (selectedIndex != previousSelectedIndex) {
                suggestionPopupSelectionListeners.selectedIndexChanged(this, selectedIndex);
                suggestionPopupSelectionListeners.selectedSuggestionChanged(this, null);
            }
        }
    }

    /**
     * Returns the list view item renderer used to present suggestions.
     */
    public ListView.ItemRenderer getSuggestionRenderer() {
        return suggestionRenderer;
    }

    /**
     * Sets the list view item renderer used to present suggestions.
     *
     * @param suggestionRenderer
     */
    public void setSuggestionRenderer(ListView.ItemRenderer suggestionRenderer) {
        ListView.ItemRenderer previousSuggestionRenderer = this.suggestionRenderer;

        if (previousSuggestionRenderer != suggestionRenderer) {
            this.suggestionRenderer = suggestionRenderer;
            suggestionPopupListeners.suggestionRendererChanged(this, previousSuggestionRenderer);
        }
    }

    /**
     * Returns the current selection.
     *
     * @return
     * The index of the currently selected suggestion, or <tt>-1</tt> if
     * nothing is selected.
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Sets the selection.
     *
     * @param selectedIndex
     * The index of the suggestion to select, or <tt>-1</tt> to clear the
     * selection.
     */
    public void setSelectedIndex(int selectedIndex) {
        indexBoundsCheck("selectedIndex", selectedIndex, -1, suggestionData.getLength() - 1);

        int previousSelectedIndex = this.selectedIndex;

        if (previousSelectedIndex != selectedIndex) {
            this.selectedIndex = selectedIndex;
            suggestionPopupSelectionListeners.selectedIndexChanged(this, previousSelectedIndex);
            suggestionPopupSelectionListeners.selectedSuggestionChanged(this, (previousSelectedIndex == -1) ?
                null : suggestionData.get(previousSelectedIndex));
        }
    }

    public Object getSelectedSuggestion() {
        int index = getSelectedIndex();
        Object item = null;

        if (index >= 0) {
            item = suggestionData.get(index);
        }

        return item;
    }

    @SuppressWarnings("unchecked")
    public void setSelectedSuggestion(Object suggestion) {
        setSelectedIndex((suggestion == null) ? -1 : ((List<Object>)suggestionData).indexOf(suggestion));
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
            suggestionPopupListeners.listSizeChanged(this, previousListSize);
        }
    }

    @Override
    public final void open(Display display, Window owner) {
        if (textInput == null) {
            throw new IllegalStateException("textInput is null.");
        }

        setSelectedIndex(-1);

        super.open(display, owner);
    }

    /**
     * Opens the suggestion popup window.
     *
     * @param textInputArgument
     * The text input for which suggestions will be provided.
     */
    public final void open(TextInput textInputArgument) {
        open(textInputArgument, null);
    }

    /**
     * Opens the suggestion popup window.
     *
     * @param textInputArgument
     * The text input for which suggestions will be provided.
     *
     * @param suggestionPopupCloseListenerArgument
     * A listener that will be called when the suggestion popup has closed.
     */
    public void open(TextInput textInputArgument, SuggestionPopupCloseListener suggestionPopupCloseListenerArgument) {
        if (textInputArgument == null) {
            throw new IllegalArgumentException();
        }

        this.textInput = textInputArgument;
        this.suggestionPopupCloseListener = suggestionPopupCloseListenerArgument;

        result = false;

        super.open(textInputArgument.getWindow());
    }

    @Override
    public boolean isClosing() {
        return closing;
    }

    @Override
    public final void close() {
        close(false);
    }

    public void close(boolean resultArgument) {
        if (!isClosed()) {
            closing = true;

            Vote vote = suggestionPopupStateListeners.previewSuggestionPopupClose(this, resultArgument);

            if (vote == Vote.APPROVE) {
                super.close();

                closing = super.isClosing();

                if (isClosed()) {
                    this.result = resultArgument;

                    suggestionPopupStateListeners.suggestionPopupClosed(this);

                    if (suggestionPopupCloseListener != null) {
                        suggestionPopupCloseListener.suggestionPopupClosed(this);
                        suggestionPopupCloseListener = null;
                    }
                }
            } else {
                if (vote == Vote.DENY) {
                    closing = false;
                }

                suggestionPopupStateListeners.suggestionPopupCloseVetoed(this, vote);
            }
        }
    }

    public SuggestionPopupCloseListener getSuggestionPopupCloseListener() {
        return suggestionPopupCloseListener;
    }

    public boolean getResult() {
        return result;
    }

    public ListenerList<SuggestionPopupListener> getSuggestionPopupListeners() {
        return suggestionPopupListeners;
    }

    public ListenerList<SuggestionPopupItemListener> getSuggestionPopupItemListeners() {
        return suggestionPopupItemListeners;
    }

    public ListenerList<SuggestionPopupSelectionListener> getSuggestionPopupSelectionListeners() {
        return suggestionPopupSelectionListeners;
    }

    public ListenerList<SuggestionPopupStateListener> getSuggestionPopupStateListeners() {
        return suggestionPopupStateListeners;
    }
}
