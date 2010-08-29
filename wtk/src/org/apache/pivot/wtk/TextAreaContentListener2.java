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

/**
 * Text area text listener interface.
 */
public interface TextAreaContentListener2 {
    /**
     * Text input text listener adapter.
     */
    public static class Adapter implements TextAreaContentListener2 {
        @Override
        public void textInserted(TextArea2 textArea, int index, int count) {
        }

        @Override
        public void textRemoved(TextArea2 textArea, int index, int count) {
        }

        @Override
        public void textChanged(TextArea2 textArea) {
        }
    }

    /**
     * Called when text has been inserted into a text area.
     *
     * @param textArea
     * The source of the event.
     *
     * @param index
     * The index at which the text was inserted.
     *
     * @param count
     * The number of characters that were inserted.
     */
    public void textInserted(TextArea2 textArea, int index, int count);

    /**
     * Called when characters have been removed from a text area.
     *
     * @param textArea
     * The source of the event.
     *
     * @param index
     * The index from which the text was removed.
     *
     * @param count
     * The number of characters that were removed.
     */
    public void textRemoved(TextArea2 textArea, int index, int count);

    /**
     * Called when a text area's text has changed.
     *
     * @param textArea
     * The source of the event.
     */
    public void textChanged(TextArea2 textArea);
}
