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

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;

/**
 * Text area text listener interface.
 */
public interface TextAreaContentListener {
    /**
     * Text area content listeners.
     */
    public static class Listeners extends ListenerList<TextAreaContentListener>
        implements TextAreaContentListener {
        @Override
        public void paragraphInserted(TextArea textArea, int index) {
            forEach(listener -> listener.paragraphInserted(textArea, index));
        }

        @Override
        public void paragraphsRemoved(TextArea textArea, int index,
            Sequence<TextArea.Paragraph> removed) {
            forEach(listener -> listener.paragraphsRemoved(textArea, index, removed));
        }

        @Override
        public void textChanged(TextArea textArea) {
            forEach(listener -> listener.textChanged(textArea));
        }
    }

    /**
     * Text input text listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements TextAreaContentListener {
        @Override
        public void paragraphInserted(TextArea textArea, int index) {
            // empty block
        }

        @Override
        public void paragraphsRemoved(TextArea textArea, int index,
            Sequence<TextArea.Paragraph> removed) {
            // empty block
        }

        @Override
        public void textChanged(TextArea textArea) {
            // empty block
        }
    }

    /**
     * Called when a paragraph has been inserted into a text area's paragraph
     * sequence.
     *
     * @param textArea The source of the event.
     * @param index The index at which the paragraph was inserted.
     */
    default void paragraphInserted(TextArea textArea, int index) {
    }

    /**
     * Called when paragraphs have been removed from a text area's paragraph
     * sequence.
     *
     * @param textArea The source of the event.
     * @param index The starting index from which the paragraphs were removed.
     * @param removed The paragraphs that were removed.
     */
    default void paragraphsRemoved(TextArea textArea, int index, Sequence<TextArea.Paragraph> removed) {
    }

    /**
     * Called when a text area's text has changed.
     *
     * @param textArea The source of the event.
     */
    default void textChanged(TextArea textArea) {
    }
}
