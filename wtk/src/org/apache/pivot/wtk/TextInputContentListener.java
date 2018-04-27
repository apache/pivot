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
 * Text input text listener.
 */
public interface TextInputContentListener {
    /**
     * Text input content listener listeners list.
     */
    public static class Listeners extends ListenerList<TextInputContentListener>
        implements TextInputContentListener {
        @Override
        public Vote previewInsertText(TextInput textInput, CharSequence text, int index) {
            VoteResult result = new VoteResult();

            forEach(listener -> result.tally(listener.previewInsertText(textInput, text, index)));

            return result.get();
        }

        @Override
        public void insertTextVetoed(TextInput textInput, Vote reason) {
            forEach(listener -> listener.insertTextVetoed(textInput, reason));
        }

        @Override
        public void textInserted(TextInput textInput, int index, int count) {
            forEach(listener -> listener.textInserted(textInput, index, count));
        }

        @Override
        public Vote previewRemoveText(TextInput textInput, int index, int count) {
            VoteResult result = new VoteResult();

            forEach(listener -> result.tally(listener.previewRemoveText(textInput, index, count)));

            return result.get();
        }

        @Override
        public void removeTextVetoed(TextInput textInput, Vote reason) {
            forEach(listener -> listener.removeTextVetoed(textInput, reason));
        }

        @Override
        public void textRemoved(TextInput textInput, int index, int count) {
            forEach(listener -> listener.textRemoved(textInput, index, count));
        }

        @Override
        public void textChanged(TextInput textInput) {
            forEach(listener -> listener.textChanged(textInput));
        }
    }

    /**
     * Text input content listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements TextInputContentListener {
        @Override
        public Vote previewInsertText(TextInput textInput, CharSequence text, int index) {
            return Vote.APPROVE;
        }

        @Override
        public void insertTextVetoed(TextInput textInput, Vote reason) {
            // empty block
        }

        @Override
        public void textInserted(TextInput textInput, int index, int count) {
            // empty block
        }

        @Override
        public Vote previewRemoveText(TextInput textInput, int index, int count) {
            return Vote.APPROVE;
        }

        @Override
        public void removeTextVetoed(TextInput textInput, Vote reason) {
            // empty block
        }

        @Override
        public void textRemoved(TextInput textInput, int index, int count) {
            // empty block
        }

        @Override
        public void textChanged(TextInput textInput) {
            // empty block
        }
    }

    /**
     * Called to preview a text insertion.
     *
     * @param textInput The source of the event.
     * @param text The text that will be inserted.
     * @param index The index at which the text will be inserted.
     * @return The accumulated vote as to whether to allow this insertion.
     */
    default Vote previewInsertText(TextInput textInput, CharSequence text, int index) {
        return Vote.APPROVE;
    }

    /**
     * Called when a text insertion has been vetoed.
     *
     * @param textInput The source of the event.
     * @param reason The reason the event was vetoed.
     */
    default void insertTextVetoed(TextInput textInput, Vote reason) {
    }

    /**
     * Called when text has been inserted into a text input.
     *
     * @param textInput The source of the event.
     * @param index The index at which the text was inserted.
     * @param count The number of characters that were inserted.
     */
    default void textInserted(TextInput textInput, int index, int count) {
    }

    /**
     * Called to preview a text removal.
     *
     * @param textInput The source of the event.
     * @param index The starting index from which the text will be removed.
     * @param count The count of characters to be removed starting from that index.
     * @return The accumulated vote as to whether to allow this removal.
     */
    default Vote previewRemoveText(TextInput textInput, int index, int count) {
        return Vote.APPROVE;
    }

    /**
     * Called when a text removal has been vetoed.
     *
     * @param textInput The source of the event.
     * @param reason The reason the event was vetoed.
     */
    default void removeTextVetoed(TextInput textInput, Vote reason) {
    }

    /**
     * Called when text has been removed from a text input.
     *
     * @param textInput The source of the event.
     * @param index The index from which the text was removed.
     * @param count The number of characters that were removed.
     */
    default void textRemoved(TextInput textInput, int index, int count) {
    }

    /**
     * Called when a text input's text has changed.
     *
     * @param textInput The source of the event.
     */
    default void textChanged(TextInput textInput) {
    }
}
