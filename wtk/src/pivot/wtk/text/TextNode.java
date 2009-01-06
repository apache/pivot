/*
 * Copyright (c) 2009 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk.text;

/**
 * Node representing a sequence of characters.
 *
 * @author gbrown
 */
public final class TextNode extends Node {
    private StringBuilder textBuilder = new StringBuilder();

    public TextNode() {
    }

    public TextNode(String text) {
        setText(text);
    }

    public TextNode(TextNode textNode) {
        setText(textNode.getText());
    }

    public void insertText(char character, int index) {
        insertText(Character.toString(character), index);
    }

    public void insertText(String text, int index) {
        if (text == null) {
            throw new IllegalArgumentException("text is null.");
        }

        insertRange(new TextNode(text), index);
    }

    public String removeText(int index, int count) {
        Node range = removeRange(index, count);
        TextNode textNode = (TextNode)range;

        return textNode.getText();
    }

    public char getCharacter(int index) {
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

    public String getText() {
        return textBuilder.toString();
    }

    public void setText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text is null.");
        }

        textBuilder = new StringBuilder(text);
    }

    @Override
    public void insertRange(Node range, int offset) {
        if (offset < 0
            || offset > textBuilder.length()) {
            throw new IndexOutOfBoundsException();
        }

        if (!(range instanceof TextNode)) {
            throw new IllegalArgumentException("range is not a text node.");
        }

        TextNode textNode = (TextNode)range;
        textBuilder.insert(offset, textNode.getText());

        rangeInserted(range, offset);
    }

    @Override
    public Node removeRange(int offset, int characterCount) {
        if (offset < 0
            || offset + characterCount > textBuilder.length()) {
            throw new IndexOutOfBoundsException();
        }

        int start = offset;
        int end = offset + characterCount;

        String text = textBuilder.substring(start, end);
        textBuilder.delete(start, end);

        TextNode textNode = new TextNode(text);
        rangeRemoved(offset, textNode);

        return textNode;
    }

    @Override
    public Node getRange(int offset, int characterCount) {
        if (offset < 0
            || offset + characterCount > textBuilder.length()) {
            throw new IndexOutOfBoundsException();
        }

        int start = offset;
        int end = offset + characterCount;

        String text = textBuilder.substring(start, end);
        TextNode textNode = new TextNode(text);

        return textNode;
    }

    @Override
    public Node duplicate(boolean recursive) {
        return new TextNode(this);
    }
}
