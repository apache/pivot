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
package org.apache.pivot.wtk.text;

import org.apache.pivot.text.CharSpan;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Span;

/**
 * Node representing a sequence of characters.
 */
public final class TextNode extends Node {
    private StringBuilder characters = new StringBuilder();
    private TextNodeListener.Listeners textNodeListeners = new TextNodeListener.Listeners();

    public TextNode() {
        this("");
    }

    public TextNode(final TextNode textNode) {
        this(textNode.getText());
    }

    public TextNode(final String text) {
        Utils.checkNull(text, "text");

        characters = new StringBuilder(text);
    }

    public String getText() {
        return characters.toString();
    }

    public String getText(final int beginIndex, final int endIndex) {
        return characters.substring(beginIndex, endIndex);
    }

    public String getText(final Span span) {
        Utils.checkNull(span, "span");

        return characters.substring(span.normalStart(), span.normalEnd() + 1);
    }

    public String getText(final CharSpan charSpan) {
        Utils.checkNull(charSpan, "charSpan");

        return characters.substring(charSpan.start, charSpan.start + charSpan.length);
    }

    public void setText(final String text) {
        Utils.checkNull(text, "text");

        removeText(0, getCharacterCount());
        insertText(text, 0);
    }

    public void appendText(final CharSequence text) {
        insertText(text, characters.length());
    }

    /**
     * @param text  The new text to insert into this node.
     * @param index Starting index into this node for the insertion.
     */
    public void insertText(final CharSequence text, final int index) {
        Utils.checkNull(text, "text");
        Utils.checkIndexBounds(index, 0, characters.length());

        int characterCount = text.length();
        if (characterCount > 0) {
            characters.insert(index, text);
            rangeInserted(index, characterCount);
            textNodeListeners.charactersInserted(this, index, characterCount);
        }
    }

    /**
     * @param index Index into this node.
     * @param count Count of characters to remove.
     */
    public void removeText(final int index, final int count) {
        Utils.checkIndexBounds(index, count, 0, characters.length());

        if (count > 0) {
            // Save the deleted characters for possible undo later
            CharSequence removedChars = getCharacters(index, index + count);
            characters.delete(index, index + count);

            textNodeListeners.charactersRemoved(this, index, count);
            rangeRemoved(this, index, count, removedChars);
        }
    }

    public String getSubstring(final Span range) {
        Utils.checkNull(range, "range");
        return characters.substring(range.start, range.end + 1);
    }

    public String getSubstring(final int start, final int end) {
        return characters.substring(start, end);
    }

    public String getSubstring(final CharSpan charSpan) {
        Utils.checkNull(charSpan, "charSpan");
        return characters.substring(charSpan.start, charSpan.start + charSpan.length);
    }

    public CharSequence getCharacters() {
        return characters;
    }

    public CharSequence getCharacters(final int start, final int end) {
        return characters.subSequence(start, end);
    }

    public CharSequence getCharacters(final Span range) {
        Utils.checkNull(range, "range");
        return characters.subSequence(range.start, range.end + 1);
    }

    public CharSequence getCharacters(final CharSpan charSpan) {
        Utils.checkNull(charSpan, "charSpan");
        return characters.subSequence(charSpan.start, charSpan.start + charSpan.length);
    }

    @Override
    public char getCharacterAt(final int index) {
        return characters.charAt(index);
    }

    @Override
    public int getCharacterCount() {
        return characters.length();
    }

    /**
     * @param offset Offset into this text node.
     */
    @Override
    public void insertRange(final Node range, final int offset) {
        if (!(range instanceof TextNode)) {
            throw new IllegalArgumentException("Range node ("
                + range.getClass().getSimpleName() + ") is not a text node.");
        }

        TextNode textNode = (TextNode) range;
        insertText(textNode.getText(), offset);
    }

    @Override
    public Node removeRange(final int offset, final int characterCount) {
        Utils.checkNonNegative(characterCount, "characterCount");

        String removed = characters.substring(offset, offset + characterCount);
        removeText(offset, characterCount);
        TextNode range = new TextNode(removed);

        return range;
    }

    @Override
    public Node getRange(final int offset, final int characterCount) {
        Utils.checkNonNegative(characterCount, "characterCount");
        Utils.checkIndexBounds(offset, characterCount, 0, characters.length());

        int start = offset;
        int end = offset + characterCount;

        String rangeText = characters.substring(start, end);
        TextNode textNode = new TextNode(rangeText);

        return textNode;
    }

    @Override
    public Node duplicate(final boolean recursive) {
        return new TextNode(this);
    }

    @Override
    public String toString() {
        return getText();
    }

    public ListenerList<TextNodeListener> getTextNodeListeners() {
        return textNodeListeners;
    }
}
