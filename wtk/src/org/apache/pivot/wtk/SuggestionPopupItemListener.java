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
 * Suggestion popup item listener interface.
 */
public interface SuggestionPopupItemListener {
    /**
     * Suggestion popup item listeners.
     */
    public static class Listeners extends ListenerList<SuggestionPopupItemListener>
        implements SuggestionPopupItemListener {
        @Override
        public void itemInserted(SuggestionPopup suggestionPopup, int index) {
            forEach(listener -> listener.itemInserted(suggestionPopup, index));
        }

        @Override
        public void itemsRemoved(SuggestionPopup suggestionPopup, int index, int count) {
            forEach(listener -> listener.itemsRemoved(suggestionPopup, index, count));
        }

        @Override
        public void itemUpdated(SuggestionPopup suggestionPopup, int index) {
            forEach(listener -> listener.itemUpdated(suggestionPopup, index));
        }

        @Override
        public void itemsCleared(SuggestionPopup suggestionPopup) {
            forEach(listener -> listener.itemsCleared(suggestionPopup));
        }

        @Override
        public void itemsSorted(SuggestionPopup suggestionPopup) {
            forEach(listener -> listener.itemsSorted(suggestionPopup));
        }
    }

    /**
     * Suggestion popup item listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements SuggestionPopupItemListener {
        @Override
        public void itemInserted(SuggestionPopup suggestionPopup, int index) {
            // empty block
        }

        @Override
        public void itemsRemoved(SuggestionPopup suggestionPopup, int index, int count) {
            // empty block
        }

        @Override
        public void itemUpdated(SuggestionPopup suggestionPopup, int index) {
            // empty block
        }

        @Override
        public void itemsCleared(SuggestionPopup suggestionPopup) {
            // empty block
        }

        @Override
        public void itemsSorted(SuggestionPopup suggestionPopup) {
            // empty block
        }
    }

    /**
     * Called when an item is inserted into the suggestion popup's data list.
     *
     * @param suggestionPopup The source of this event.
     * @param index The location where the item was inserted.
     */
    default void itemInserted(SuggestionPopup suggestionPopup, int index) {
    }

    /**
     * Called when items are removed from the suggestion popup's data list.
     *
     * @param suggestionPopup The source of this event.
     * @param index The starting index where items were removed.
     * @param count The number of items that were removed.
     */
    default void itemsRemoved(SuggestionPopup suggestionPopup, int index, int count) {
    }

    /**
     * Called when an item is updated within a suggestion popup's data list.
     *
     * @param suggestionPopup The source of this event.
     * @param index Which item was updated.
     */
    default void itemUpdated(SuggestionPopup suggestionPopup, int index) {
    }

    /**
     * Called when a list button's list data has been cleared.
     *
     * @param suggestionPopup The source of this event.
     */
    default void itemsCleared(SuggestionPopup suggestionPopup) {
    }

    /**
     * Called when a list button's list data is sorted.
     *
     * @param suggestionPopup The source of this event.
     */
    default void itemsSorted(SuggestionPopup suggestionPopup) {
    }
}
