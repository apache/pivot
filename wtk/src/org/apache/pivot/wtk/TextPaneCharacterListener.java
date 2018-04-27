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
 * Text pane character listener interface.
 */
public interface TextPaneCharacterListener {
    /**
     * Text pane character listeners.
     */
    public static class Listeners extends ListenerList<TextPaneCharacterListener>
        implements TextPaneCharacterListener {
        /**
         * @param index Index into the whole document.
         */
        @Override
        public void charactersInserted(TextPane textPane, int index, int count) {
            forEach(listener -> listener.charactersInserted(textPane, index, count));
        }

        /**
         * @param index Index into the whole document.
         */
        @Override
        public void charactersRemoved(TextPane textPane, int index, int count) {
            forEach(listener -> listener.charactersRemoved(textPane, index, count));
        }
    }

    /**
     * Text pane character listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements TextPaneCharacterListener {
        @Override
        public void charactersInserted(TextPane textPane, int index, int count) {
            // empty block
        }

        @Override
        public void charactersRemoved(TextPane textPane, int index, int count) {
            // empty block
        }
    }

    /**
     * Called when characters have been inserted into a text pane.
     *
     * @param textPane The text pane whose text has changed.
     * @param index    The starting point of the text insertion.
     * @param count    The count of characters inserted there.
     */
    default void charactersInserted(TextPane textPane, int index, int count) {
    }

    /**
     * Called when characters have been removed from a text pane.
     *
     * @param textPane The text pane whose text has changed.
     * @param index    The starting point where text was removed.
     * @param count    Number of characters removed starting from there.
     */
    default void charactersRemoved(TextPane textPane, int index, int count) {
    }
}
