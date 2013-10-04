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

import org.apache.pivot.util.Vote;

/**
 * Suggestion popup state listener interface.
 */
public interface SuggestionPopupStateListener extends SuggestionPopupCloseListener {
    /**
     * Suggestion popup state listener adapter.
     */
    public static class Adapter implements SuggestionPopupStateListener {
        @Override
        public Vote previewSuggestionPopupClose(SuggestionPopup suggestionPopup, boolean result) {
            return Vote.APPROVE;
        }

        @Override
        public void suggestionPopupCloseVetoed(SuggestionPopup suggestionPopup, Vote reason) {
            // empty block
        }

        @Override
        public void suggestionPopupClosed(SuggestionPopup suggestionPopup) {
            // empty block
        }
    }

    /**
     * Called to preview a suggestion popup close event.
     *
     * @param suggestionPopup
     * @param result
     */
    public Vote previewSuggestionPopupClose(SuggestionPopup suggestionPopup, boolean result);

    /**
     * Called when a suggestion popup close event has been vetoed.
     *
     * @param suggestionPopup
     * @param reason
     */
    public void suggestionPopupCloseVetoed(SuggestionPopup suggestionPopup, Vote reason);
}
