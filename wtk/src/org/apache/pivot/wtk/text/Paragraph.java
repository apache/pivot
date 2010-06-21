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
 * Element representing a paragraph.
 * <p>
 * TODO Add indent property.
 */
public class Paragraph extends Block {
    public Paragraph() {
        super();
    }

    public Paragraph(String text) {
        super();
        add(new TextNode(text));
    }

    public Paragraph(Paragraph paragraph, boolean recursive) {
        super(paragraph, recursive);
    }

    @Override
    public Node removeRange(int offset, int characterCount) {
        if (offset + characterCount == getCharacterCount()) {
            characterCount--;
        }

        return super.removeRange(offset, characterCount);
    }

    @Override
    public Paragraph getRange(int offset, int characterCount) {
        if (offset + characterCount == getCharacterCount()) {
            characterCount--;
        }

        return (Paragraph) super.getRange(offset, characterCount);
    }

    @Override
    public char getCharacterAt(int offset) {
        char c;
        if (offset == getCharacterCount() - 1) {
            c = '\n';
        } else {
            c = super.getCharacterAt(offset);
        }

        return c;
    }

    @Override
    public int getCharacterCount() {
        return super.getCharacterCount() + 1;
    }

    public int add(String text) {
        return add(new TextNode(text));
    }

    @Override
    public void insert(Node node, int index) {
        if (node instanceof Block) {
            throw new IllegalArgumentException("Child node must not be an instance of "
                + Block.class.getName());
        }

        super.insert(node, index);
    }

    @Override
    public Sequence<Integer> getPathAt(int offset) {
        Sequence<Integer> path;

        if (offset < super.getCharacterCount()) {
            path = super.getPathAt(offset);
        } else {
            path = new ArrayList<Integer>();
        }

        return path;
    }

    @Override
    public Node getDescendantAt(int offset) {
        Node descendant;

        if (offset < super.getCharacterCount()) {
            descendant = super.getDescendantAt(offset);
        } else {
            descendant = this;
        }

        return descendant;
    }

    @Override
    public Paragraph duplicate(boolean recursive) {
        return new Paragraph(this, recursive);
    }
}
