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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;

/**
 * Element representing a paragraph. <p> TODO Add indent property.
 * <p> Every paragraph has a trailing newline ('\n') character, which is
 * not actually present in any of its child text nodes.  Therefore all the
 * logic in here must account for this extra phantom character.
 */
public class Paragraph extends Block {
    public Paragraph() {
        super();
        add(new TextNode());
    }

    public Paragraph(final String text) {
        super();
        add(text);
    }

    public Paragraph(final Paragraph paragraph, final boolean recursive) {
        super(paragraph, recursive);
        if (!recursive) {
            add(new TextNode());
        }
    }

    /**
     * Remove a range of characters from this paragraph.
     * @param offset Offset into this paragraph.
     * @param characterCount How many characters to remove.
     * @return A new {@link Node} containing the removed characters.
     */
    @Override
    public Node removeRange(final int offset, final int characterCount) {
        if (offset + characterCount == getCharacterCount()) {
            return super.removeRange(offset, characterCount - 1);
        }

        return super.removeRange(offset, characterCount);
    }

    /**
     * Get a new {@link Paragraph} containing the given range
     * of characters from this paragraph.
     * @param offset Offset into this paragraph.
     * @param characterCount How many characters to get.
     * @return New {@link Paragraph} with these characters.
     */
    @Override
    public Paragraph getRange(final int offset, final int characterCount) {
        if (offset + characterCount == getCharacterCount()) {
            return (Paragraph) super.getRange(offset, characterCount - 1);
        }

        return (Paragraph) super.getRange(offset, characterCount);
    }

    /**
     * Retrieve the character at the given offset in this paragraph.
     * @param offset Offset into this paragraph.
     * @return The character at that position.
     */
    @Override
    public char getCharacterAt(final int offset) {
        char c;
        if (offset == getCharacterCount() - 1) {
            c = '\n';
        } else {
            c = super.getCharacterAt(offset);
        }

        return c;
    }

    /**
     * @return The count of characters in this paragraph, which is one more
     * than the number of characters in all child nodes (because of the
     * trailing newline implicitly present).
     */
    @Override
    public int getCharacterCount() {
        return super.getCharacterCount() + 1;
    }

    /**
     * Add a piece of text to this paragraph (at the end, but
     * before the trailing newline).  If the paragraph had
     * no text previously then a new {@link TextNode} child is
     * created containing this text.  Otherwise, if the last
     * child node contains text, then just append this new text
     * to the existing node.
     *
     * @param text The text to add.
     * @return The index of the (new or existing) text node where
     * the text was added.
     */
    public int add(final String text) {
        int length = getLength();
        if (length > 0) {
            // Add to the last node if it is a text node already
            Node node = get(length - 1);
            if (node instanceof TextNode) {
                ((TextNode) node).appendText(text);
                return length - 1;
            }
        }
        // Default is to just create a new node with the text
        return add(new TextNode(text));
    }

    @Override
    public void insert(final Node node, final int index) {
        if (node instanceof Block) {
            throw new IllegalArgumentException("Child node must not be an instance of "
                + Block.class.getName());
        }

        super.insert(node, index);
    }

    /**
     * Get the path through our descendants for the given
     * offset into this paragraph.
     * @param offset Offset into this paragraph.
     * @return The path to that offset, which will be empty
     * for the trailing newline character.
     */
    @Override
    public Sequence<Integer> getPathAt(final int offset) {
        Sequence<Integer> path;

        if (offset < super.getCharacterCount()) {
            path = super.getPathAt(offset);
        } else {
            path = new ArrayList<>();
        }

        return path;
    }

    /**
     * Get the descendant node at the given offset.  If the offset
     * is the last character in this paragraph (namely the phantom
     * newline) then return ourselves, otherwise the normal descendant
     * at that offset.
     * @param offset Offset into this paragraph.
     * @return The descendant node at that offset.
     */
    @Override
    public Node getDescendantAt(final int offset) {
        Node descendant;

        if (offset < super.getCharacterCount()) {
            descendant = super.getDescendantAt(offset);
        } else {
            descendant = this;
        }

        return descendant;
    }

    @Override
    public Paragraph duplicate(final boolean recursive) {
        return new Paragraph(this, recursive);
    }
}
