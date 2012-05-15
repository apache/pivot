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

/**
 * Suggestion popup listener interface.
 */
public interface SuggestionPopupListener {
    /**
     * Suggestion popup listener adapter.
     */
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
     * @param suggestionPopup
     * @param previousSuggestionData
     */
    public void suggestionDataChanged(SuggestionPopup suggestionPopup, List<?> previousSuggestionData);

    /**
     * Called when a suggestion popup's item renderer has changed.
     *
     * @param suggestionPopup
     * @param previousSuggestionRenderer
     */
    public void suggestionRendererChanged(SuggestionPopup suggestionPopup,
        ListView.ItemRenderer previousSuggestionRenderer);

    /**
     * Called when a suggestion popup's list size has changed.
     *
     * @param suggestionPopup
     * @param previousListSize
     */
    public void listSizeChanged(SuggestionPopup suggestionPopup, int previousListSize);
}
