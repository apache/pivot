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
    }

    private TextInput textInput = null;
    private List<?> suggestions;
    private ListView.ItemRenderer suggestionRenderer;

    private SuggestionPopupListenerList suggestionPopupListeners = new SuggestionPopupListenerList();

    private static final ListView.ItemRenderer DEFAULT_SUGGESTION_RENDERER =
        new ListViewItemRenderer();

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

    @Override
    public final void open(Display display, Window owner) {
        if (textInput == null) {
            throw new IllegalStateException("textInput is null.");
        }

        super.open(display, owner);
    }

    /**
     * Opens the suggestion popup window.
     *
     * @param textInput
     * The text input for which suggestions will be provided.
     */
    public void open(TextInput textInput) {
        if (textInput == null) {
            throw new IllegalArgumentException();
        }

        this.textInput = textInput;

        super.open(textInput.getWindow());

        if (!isOpen()) {
            this.textInput = null;
        }
    }

    @Override
    public void close() {
        super.close();

        if (isClosed()) {
            textInput = null;
        }
    }

    public ListenerList<SuggestionPopupListener> getSuggestionPopupListeners() {
        return suggestionPopupListeners;
    }
}
