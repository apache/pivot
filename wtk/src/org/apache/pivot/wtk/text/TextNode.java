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

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Span;

/**
 * Node representing a sequence of characters.
 */
public final class TextNode extends Node {
    private static class TextNodeListenerList extends ListenerList<TextNodeListener> implements
        TextNodeListener {
        /**
         * @param textNode The text node that changed.
         * @param index    Index into this node.
         * @param count    Count of characters inserted here.
         */
        @Override
        public void charactersInserted(TextNode textNode, int index, int count) {
            for (TextNodeListener listener : this) {
                listener.charactersInserted(textNode, index, count);
            }
        }

        /**
         * @param textNode The text node that changed.
         * @param index    Index into this node.
         * @param count    Count of characters removed here.
         */
        @Override
        public void charactersRemoved(TextNode textNode, int index, int count) {
            for (TextNodeListener listener : this) {
                listener.charactersRemoved(textNode, index, count);
            }
        }
    }

    private StringBuilder characters = new StringBuilder();
    private TextNodeListenerList textNodeListeners = new TextNodeListenerList();

    public TextNode() {
        this("");
    }

    public TextNode(TextNode textNode) {
        this(textNode.getText());
    }

    public TextNode(String text) {
        Utils.checkNull(text, "text");

        characters = new StringBuilder(text);
    }

    public String getText() {
        return getText(0, getCharacterCount());
    }

    public String getText(int beginIndex, int endIndex) {
        return characters.substring(beginIndex, endIndex);
    }

    public void setText(String text) {
        Utils.checkNull(text, "text");

        removeText(0, getCharacterCount());
        insertText(text, 0);
    }

    /**
     * @param text  The new text to insert into this node.
     * @param index Starting index into this node for the insertion.
     */
    public void insertText(CharSequence text, int index) {
        Utils.checkNull(text, "text");

        if (index < 0 || index > characters.length()) {
            throw new IndexOutOfBoundsException("Index " + index + " outside of [0, " + characters.length() + "]");
        }

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
    public void removeText(int index, int count) {
        if (index < 0 || index + count > characters.length()) {
            throw new IndexOutOfBoundsException("Index " + index +
                    " less than 0 or index+count " + count +
                    " greater than length " + characters.length());
        }

        if (count > 0) {
            characters.delete(index, index + count);

            textNodeListeners.charactersRemoved(this, index, count);
            rangeRemoved(index, count);
        }
    }

    public String getSubstring(Span range) {
        return characters.substring(range.start, range.end + 1);
    }

    public String getSubstring(int start, int end) {
        return characters.substring(start, end);
    }

    public CharSequence getCharacters() {
        return characters;
    }

    public CharSequence getCharacters(int start, int end) {
        return characters.subSequence(start, end);
    }

    public CharSequence getCharacters(Span range) {
        return characters.subSequence(range.start, range.end + 1);
    }

    @Override
    public char getCharacterAt(int index) {
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
    public void insertRange(Node range, int offset) {
        if (!(range instanceof TextNode)) {
            throw new IllegalArgumentException("range is not a text node.");
        }

        TextNode textNode = (TextNode) range;
        insertText(textNode.getText(), offset);
    }

    @Override
    public Node removeRange(int offset, int characterCount) {
        Utils.checkNonNegative(characterCount, "characterCount");

        String removed = characters.substring(offset, offset + characterCount);
        removeText(offset, characterCount);
        TextNode range = new TextNode(removed);

        return range;
    }

    @Override
    public Node getRange(int offset, int characterCount) {
        Utils.checkNonNegative(characterCount, "characterCount");

        if (offset < 0 || offset + characterCount > characters.length()) {
            throw new IndexOutOfBoundsException();
        }

        int start = offset;
        int end = offset + characterCount;

        String rangeText = characters.substring(start, end);
        TextNode textNode = new TextNode(rangeText);

        return textNode;
    }

    @Override
    public Node duplicate(boolean recursive) {
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
