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
package org.apache.pivot.ui;

/**
 * Text input text listener.
 */
public interface TextInputContentListener {
    /**
     * Called to preview a text insertion.
     *
     * @param textInput
     * The source of the event.
     *
     * @param text
     * The text that will be inserted.
     *
     * @param index
     * The index at which the text will be inserted.
     */
    public boolean previewInsertText(TextInput textInput, CharSequence text, int index);

    /**
     * Called when a text insertion has been vetoed.
     *
     * @param textInput
     * The source of the event.
     */
    public void insertTextVetoed(TextInput textInput);

    /**
     * Called when text has been inserted into a text input.
     *
     * @param textInput
     * The source of the event.
     *
     * @param text
     * The text that was inserted.
     *
     * @param text
     * The index at which the text was inserted.
     */
    public void textInserted(TextInput textInput, CharSequence text, int index);

    /**
     * Called to preview a text removal.
     *
     * @param textInput
     * The source of the event.
     *
     * @param start
     * The starting index from which the text will be removed.
     *
     * @param length
     * The number of characters that will be removed.
     */
    public boolean previewRemoveText(TextInput textInput, int start, int length);

    /**
     * Called when a text removal has been vetoed.
     *
     * @param textInput
     * The source of the event.
     */
    public void removeTextVetoed(TextInput textInput);

    /**
     * Called when text has been removed from a text input.
     *
     * @param textInput
     * The source of the event.
     *
     * @param start
     * The starting index from which the text was removed.
     *
     * @param length
     * The number of characters that were removed.
     */
    public void textRemoved(TextInput textInput, int start, int count);

    /**
     * Called when a text input's text has changed.
     *
     * @param textInput
     * The source of the event.
     */
    public void textChanged(TextInput textInput);
}
