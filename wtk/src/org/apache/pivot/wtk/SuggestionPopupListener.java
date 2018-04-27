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

import org.apache.pivot.collections.List;
import org.apache.pivot.util.ListenerList;

/**
 * Suggestion popup listener interface.
 */
public interface SuggestionPopupListener {
    /**
     * Suggestion popup listeners.
     */
    public static class Listeners extends ListenerList<SuggestionPopupListener>
        implements SuggestionPopupListener {
        @Override
        public void suggestionDataChanged(SuggestionPopup suggestionPopup,
            List<?> previousSuggestionData) {
            forEach(listener -> listener.suggestionDataChanged(suggestionPopup, previousSuggestionData));
        }

        @Override
        public void suggestionRendererChanged(SuggestionPopup suggestionPopup,
            ListView.ItemRenderer previousSuggestionRenderer) {
            forEach(listener -> listener.suggestionRendererChanged(suggestionPopup, previousSuggestionRenderer));
        }

        @Override
        public void listSizeChanged(SuggestionPopup suggestionPopup, int previousListSize) {
            forEach(listener -> listener.listSizeChanged(suggestionPopup, previousListSize));
        }
    }

    /**
     * Suggestion popup listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements SuggestionPopupListener {
        @Override
        public void suggestionDataChanged(SuggestionPopup suggestionPopup,
            List<?> previousSuggestionData) {
            // empty block
        }

        @Override
        public void suggestionRendererChanged(SuggestionPopup suggestionPopup,
            ListView.ItemRenderer previousSuggestionRenderer) {
            // empty block
        }

        @Override
        public void listSizeChanged(SuggestionPopup suggestionPopup, int previousListSize) {
            // empty block
        }
    }

    /**
     * Called when a suggestion popup's suggestions have changed.
     *
     * @param suggestionPopup The source of this event.
     * @param previousSuggestionData The previous data that was being shown.
     */
    default void suggestionDataChanged(SuggestionPopup suggestionPopup,
        List<?> previousSuggestionData) {
    }

    /**
     * Called when a suggestion popup's item renderer has changed.
     *
     * @param suggestionPopup The source of this event.
     * @param previousSuggestionRenderer The previous item renderer.
     */
    default void suggestionRendererChanged(SuggestionPopup suggestionPopup,
        ListView.ItemRenderer previousSuggestionRenderer) {
    }

    /**
     * Called when a suggestion popup's list size has changed.
     *
     * @param suggestionPopup The source of this event.
     * @param previousListSize The previous value of the visible window.
     */
    default void listSizeChanged(SuggestionPopup suggestionPopup, int previousListSize) {
    }
}
