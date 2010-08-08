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
import org.apache.pivot.collections.List;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.content.ListViewItemRenderer;

/**
 * Popup that presents a list of text suggestions to the user.
 */
public class SuggestionPopup extends Window {
    private static class SuggestionPopupListenerList extends ListenerList<SuggestionPopupListener>
        implements SuggestionPopupListener {
        @Override
        public void suggestionsChanged(SuggestionPopup suggestionPopup,
            List<?> previousSuggestions) {
            for (SuggestionPopupListener listener : this) {
                listener.suggestionsChanged(suggestionPopup, previousSuggestions);
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
        public void selectedIndexChanged(SuggestionPopup suggestionPopup,
            int previousSelectedIndex) {
            for (SuggestionPopupListener listener : this) {
                listener.selectedIndexChanged(suggestionPopup, previousSelectedIndex);
            }
        }
    }

    private static class SuggestionPopupStateListenerList extends ListenerList<SuggestionPopupStateListener>
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

    private List<?> suggestions;
    private ListView.ItemRenderer suggestionRenderer;
    private int selectedIndex = -1;

    private boolean result = false;

    private boolean closing = false;

    private SuggestionPopupListenerList suggestionPopupListeners = new SuggestionPopupListenerList();
    private SuggestionPopupStateListenerList suggestionPopupStateListeners = new SuggestionPopupStateListenerList();

    private static final ListView.ItemRenderer DEFAULT_SUGGESTION_RENDERER = new ListViewItemRenderer();

    public SuggestionPopup() {
        this(new ArrayList<Object>());
    }

    public SuggestionPopup(List<?> suggestions) {
        setSuggestionRenderer(DEFAULT_SUGGESTION_RENDERER);
        setSuggestions(suggestions);

        installThemeSkin(SuggestionPopup.class);
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
    public List<?> getSuggestions() {
        return suggestions;
    }

    /**
     * Sets the list of suggestions presented by the popup.
     *
     * @param suggestions
     */
    public void setSuggestions(List<?> suggestions) {
        if (suggestions == null) {
            throw new IllegalArgumentException();
        }

        List<?> previousSuggestions = this.suggestions;

        if (previousSuggestions != suggestions) {
            this.suggestions = suggestions;
            suggestionPopupListeners.suggestionsChanged(this, previousSuggestions);
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
        if (selectedIndex < -1
            || selectedIndex >= suggestions.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        int previousSelectedIndex = this.selectedIndex;

        if (previousSelectedIndex != selectedIndex) {
            this.selectedIndex = selectedIndex;
            suggestionPopupListeners.selectedIndexChanged(this, previousSelectedIndex);
        }
    }

    /**
     * Returns the current selection.
     *
     * @return
     * The currently selected suggestion, or <tt>null</tt> if nothing is selected.
     */
    public Object getSelectedSuggestion() {
        int index = getSelectedIndex();
        Object item = null;

        if (index >= 0) {
            item = suggestions.get(index);
        }

        return item;
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
     * @param textInput
     * The text input for which suggestions will be provided.
     */
    public final void open(TextInput textInput) {
        open(textInput, null);
    }

    /**
     * Opens the suggestion popup window.
     *
     * @param textInput
     * The text input for which suggestions will be provided.
     *
     * @param suggestionPopupCloseListener
     * A listener that will be called when the suggestion popup has closed.
     */
    public void open(TextInput textInput, SuggestionPopupCloseListener suggestionPopupCloseListener) {
        if (textInput == null) {
            throw new IllegalArgumentException();
        }

        this.textInput = textInput;
        this.suggestionPopupCloseListener = suggestionPopupCloseListener;

        result = false;

        super.open(textInput.getWindow());
    }

    @Override
    public boolean isClosing() {
        return closing;
    }

    @Override
    public final void close() {
        close(false);
    }

    public void close(boolean result) {
        if (!isClosed()) {
            closing = true;

            Vote vote = suggestionPopupStateListeners.previewSuggestionPopupClose(this, result);

            if (vote == Vote.APPROVE) {
                super.close();

                closing = super.isClosing();

                if (isClosed()) {
                    this.result = result;

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

    public ListenerList<SuggestionPopupStateListener> getSuggestionPopupStateListeners() {
        return suggestionPopupStateListeners;
    }
}
