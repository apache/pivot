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
import org.apache.pivot.util.Vote;
import org.apache.pivot.util.VoteResult;

/**
 * Suggestion popup state listener interface.
 */
public interface SuggestionPopupStateListener extends SuggestionPopupCloseListener {
    /**
     * Suggestion popup state listeners.
     */
    public static class Listeners extends ListenerList<SuggestionPopupStateListener>
        implements SuggestionPopupStateListener {
        @Override
        public Vote previewSuggestionPopupClose(SuggestionPopup suggestionPopup, boolean result) {
            VoteResult vote = new VoteResult();

            forEach(listener -> vote.tally(listener.previewSuggestionPopupClose(suggestionPopup, result)));

            return vote.get();
        }

        @Override
        public void suggestionPopupCloseVetoed(SuggestionPopup suggestionPopup, Vote reason) {
            forEach(listener -> listener.suggestionPopupCloseVetoed(suggestionPopup, reason));
        }

        @Override
        public void suggestionPopupClosed(SuggestionPopup suggestionPopup) {
            forEach(listener -> listener.suggestionPopupClosed(suggestionPopup));
        }
    }

    /**
     * Suggestion popup state listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
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
     * @param suggestionPopup The source of this event.
     * @param result What the result would be.
     * @return What this listener thinks about closing the popup with this result.
     */
    default Vote previewSuggestionPopupClose(SuggestionPopup suggestionPopup, boolean result) {
        return Vote.APPROVE;
    }

    /**
     * Called when a suggestion popup close event has been vetoed.
     *
     * @param suggestionPopup The source of this event.
     * @param reason The accumulated vote that forced the veto.
     */
    default void suggestionPopupCloseVetoed(SuggestionPopup suggestionPopup, Vote reason) {
    }
}
