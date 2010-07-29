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

import org.apache.pivot.util.ListenerList;

/**
 * Node representing a sequence of characters.
 */
public final class TextNode extends Node {
    private class TextNodeCharacterIterator implements CharacterIterator {
        private final int beginIndex;
        private final int endIndex;

        private int index;

        public TextNodeCharacterIterator(int beginIndex, int endIndex) {
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;

            index = 0;
        }

        public TextNodeCharacterIterator(TextNodeCharacterIterator textNodeCharacterIterator) {
            beginIndex = textNodeCharacterIterator.beginIndex;
            endIndex = textNodeCharacterIterator.endIndex;
            index = textNodeCharacterIterator.index;
        }

        @Override
        public char first() {
            return setIndex(beginIndex);
        }

        @Override
        public char last() {
            return setIndex(getCharacterCount() == 0 ? endIndex : endIndex - 1);
        }

        @Override
        public char next() {
            return setIndex(index < endIndex ? index + 1 : DONE);
        }

        @Override
        public char previous() {
            return setIndex(index > beginIndex ? index - 1 : DONE);
        }

        @Override
        public char current() {
            return (index < endIndex) ? getCharacterAt(index) : DONE;
        }

        @Override
        public int getBeginIndex() {
            return beginIndex;
        }

        @Override
        public int getEndIndex() {
            return endIndex;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public char setIndex(int index) {
            if (index < beginIndex
                || index > endIndex) {
                throw new IndexOutOfBoundsException();
            }

            this.index = index;

            return current();
        }

        @Override
        public Object clone() {
            return new TextNodeCharacterIterator(this);
        }
    }

    private static class TextNodeListenerList extends ListenerList<TextNodeListener>
        implements TextNodeListener {
        @Override
        public void charactersInserted(TextNode textNode, int index, int count) {
            for (TextNodeListener listener : this) {
                listener.charactersInserted(textNode, index, count);
            }
        }

        @Override
        public void charactersRemoved(TextNode textNode, int index, String characters) {
            for (TextNodeListener listener : this) {
                listener.charactersRemoved(textNode, index, characters);
            }
        }
    }

    private String text;
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
        
        this.text = text;
    }

    public void insertText(char character, int index) {
        insertText(Character.toString(character), index);
    }

    public void insertText(String insertText, int index) {
        if (insertText == null) {
            throw new IllegalArgumentException("text is null.");
        }

        if (index < 0
            || index > text.length()) {
            throw new IndexOutOfBoundsException();
        }

        int characterCount = insertText.length();
        if (characterCount > 0) {
            StringBuilder textBuilder = new StringBuilder(text);
            textBuilder.insert(index, insertText);
            text = textBuilder.toString();
            rangeInserted(index, characterCount);
            textNodeListeners.charactersInserted(this, index, characterCount);
        }
    }

    public String removeText(int index, int count) {
        if (index < 0
            || index + count > text.length()) {
            throw new IndexOutOfBoundsException();
        }

        String removedText;
        if (count == 0) {
            removedText = "";
        } else {
            int start = index;
            int end = index + count;

            removedText = text.substring(start, end);
            StringBuilder textBuilder = new StringBuilder(text);
            textBuilder.delete(start, end);
            text = textBuilder.toString();
            textNodeListeners.charactersRemoved(this, index, removedText);

            rangeRemoved(index, count);
        }

        return removedText;
    }

    @Override
    public char getCharacterAt(int index) {
        if (index < 0
            || index >= text.length()) {
            throw new IndexOutOfBoundsException();
        }

        return text.charAt(index);
    }

    @Override
    public int getCharacterCount() {
        return text.length();
    }

    public CharacterIterator getCharacterIterator() {
        return getCharacterIterator(0, getCharacterCount());
    }

    public CharacterIterator getCharacterIterator(int beginIndex) {
        return getCharacterIterator(beginIndex, getCharacterCount());
    }

    public CharacterIterator getCharacterIterator(int beginIndex, int endIndex) {
        return new TextNodeCharacterIterator(beginIndex, endIndex);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text is null.");
        }

        removeText(0, getCharacterCount());
        insertText(text, 0);
    }

    public String getSubstring(int start, int end) {
        return text.substring(start, end);
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

        String removed = removeText(offset, characterCount);
        TextNode range = new TextNode(removed);

        return range;
    }

    @Override
    public Node getRange(int offset, int characterCount) {
        if (characterCount < 0) {
            throw new IllegalArgumentException("characterCount is negative.");
        }

        if (offset < 0
            || offset + characterCount > text.length()) {
            throw new IndexOutOfBoundsException();
        }

        int start = offset;
        int end = offset + characterCount;

        String rangeText = text.substring(start, end);
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
