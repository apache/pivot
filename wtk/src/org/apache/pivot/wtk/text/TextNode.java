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

import java.text.CharacterIterator;

import org.apache.pivot.text.CharSequenceCharacterIterator;
import org.apache.pivot.util.ListenerList;

/**
 * Node representing a sequence of characters.
 */
public final class TextNode extends Node {
    private static class TextNodeListenerList extends ListenerList<TextNodeListener>
        implements TextNodeListener {
        @Override
        public void charactersInserted(TextNode textNode, int index, int count) {
            for (TextNodeListener listener : this) {
                listener.charactersInserted(textNode, index, count);
            }
        }

        @Override
        public void charactersRemoved(TextNode textNode, int index, int count) {
            for (TextNodeListener listener : this) {
                listener.charactersRemoved(textNode, index, count);
            }
        }
    }

    private StringBuilder textBuilder = new StringBuilder();
    private TextNodeListenerList textNodeListeners = new TextNodeListenerList();

    public TextNode() {
        this("");
    }

    public TextNode(TextNode textNode) {
        this(textNode.getText());
    }

    public TextNode(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text is null.");
        }

        textBuilder = new StringBuilder(text);
    }

    public String getText() {
        return getText(0, getCharacterCount());
    }

    public String getText(int beginIndex, int endIndex) {
        return textBuilder.substring(beginIndex, endIndex);
    }

    public void setText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text is null.");
        }

        removeText(0, getCharacterCount());
        insertText(text, 0);
    }

    public void insertText(char text, int index) {
        insertText(Character.toString(text), index);
    }

    public void insertText(String text, int index) {
        if (text == null) {
            throw new IllegalArgumentException("text is null.");
        }

        if (index < 0
            || index > textBuilder.length()) {
            throw new IndexOutOfBoundsException();
        }

        int characterCount = text.length();
        if (characterCount > 0) {
            textBuilder.insert(index, text);
            rangeInserted(index, characterCount);
            textNodeListeners.charactersInserted(this, index, characterCount);
        }
    }

    public void removeText(int index, int count) {
        if (index < 0
            || index + count > textBuilder.length()) {
            throw new IndexOutOfBoundsException();
        }

        if (count > 0) {
            textBuilder.delete(index, index + count);

            textNodeListeners.charactersRemoved(this, index, count);
            rangeRemoved(index, count);
        }
    }

    public String getSubstring(int start, int end) {
        return textBuilder.substring(start, end);
    }

    @Override
    public char getCharacterAt(int index) {
        if (index < 0
            || index >= textBuilder.length()) {
            throw new IndexOutOfBoundsException();
        }

        return textBuilder.charAt(index);
    }

    @Override
    public int getCharacterCount() {
        return textBuilder.length();
    }

    public CharacterIterator getCharacterIterator() {
        return getCharacterIterator(0, getCharacterCount());
    }

    public CharacterIterator getCharacterIterator(int beginIndex, int endIndex) {
        return new CharSequenceCharacterIterator(textBuilder, beginIndex, endIndex);
    }

    @Override
    public void insertRange(Node range, int offset) {
        if (!(range instanceof TextNode)) {
            throw new IllegalArgumentException("range is not a text node.");
        }

        TextNode textNode = (TextNode)range;
        insertText(textNode.getText(), offset);
    }

    @Override
    public Node removeRange(int offset, int characterCount) {
        if (characterCount < 0) {
            throw new IllegalArgumentException("characterCount is negative.");
        }

        String removed = textBuilder.substring(offset, offset + characterCount);
        removeText(offset, characterCount);
        TextNode range = new TextNode(removed);

        return range;
    }

    @Override
    public Node getRange(int offset, int characterCount) {
        if (characterCount < 0) {
            throw new IllegalArgumentException("characterCount is negative.");
        }

        if (offset < 0
            || offset + characterCount > textBuilder.length()) {
            throw new IndexOutOfBoundsException();
        }

        int start = offset;
        int end = offset + characterCount;

        String rangeText = textBuilder.substring(start, end);
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
