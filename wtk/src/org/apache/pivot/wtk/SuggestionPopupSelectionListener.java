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

import org.apache.pivot.util.ListenerList;

/**
 * Suggestion popup selection listener interface.
 */
public interface SuggestionPopupSelectionListener {
    /**
     * Suggestion popup selection listeners.
     */
    public static class Listeners extends ListenerList<SuggestionPopupSelectionListener>
        implements SuggestionPopupSelectionListener {
        @Override
        public void selectedIndexChanged(SuggestionPopup suggestionPopup, int previousSelectedIndex) {
            forEach(listener -> listener.selectedIndexChanged(suggestionPopup, previousSelectedIndex));
        }

        @Override
        public void selectedSuggestionChanged(SuggestionPopup suggestionPopup,
            Object previousSelectedSuggestion) {
            forEach(listener -> listener.selectedSuggestionChanged(suggestionPopup, previousSelectedSuggestion));
        }
    }

    /**
     * Suggestion popup selection listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements SuggestionPopupSelectionListener {
        @Override
        public void selectedIndexChanged(SuggestionPopup suggestionPopup, int previousSelectedIndex) {
            // empty block
        }

        @Override
        public void selectedSuggestionChanged(SuggestionPopup suggestionPopup,
            Object previousSelectedSuggestion) {
            // empty block
        }
    }

    /**
     * Called when a suggestion popup's selected index has changed.
     *
     * @param suggestionPopup The source of this event.
     * @param previousSelectedIndex What the selected index used to be.
     */
    default void selectedIndexChanged(SuggestionPopup suggestionPopup, int previousSelectedIndex) {
    }

    /**
     * Called when a suggestion popup's selected suggestion has changed.
     *
     * @param suggestionPopup The source of the event.
     * @param previousSelectedSuggestion The item that was previously selected,
     * or <tt>null</tt> if the previous selection cannot be determined.
     */
    default void selectedSuggestionChanged(SuggestionPopup suggestionPopup,
        Object previousSelectedSuggestion) {
    }
}
