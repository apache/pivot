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
package org.apache.pivot.wtk.skin;

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.text.Element;
import org.apache.pivot.wtk.text.Node;
import org.apache.pivot.wtk.text.TextNode;

/**
 * Span node view.
 */
class TextPaneSkinSpanView extends TextPaneSkinElementView {

    private final TextPaneSkin textPaneSkin;

    public TextPaneSkinSpanView(TextPaneSkin textPaneSkin, org.apache.pivot.wtk.text.Span span) {
        super(span);
        this.textPaneSkin = textPaneSkin;
    }

    @Override
    public void validate() {

        if (!isValid()) {
            // I have to re-create my children here instead of in attach(),
            // because that is how ParagraphView works,
            // and ParagraphView is always my parent node.

            // Clear all existing views
            remove(0, getLength());

            org.apache.pivot.wtk.text.Span span = (org.apache.pivot.wtk.text.Span)getNode();

            // for now, assume that span contains at most one child, and
            // that child is a TextNode
            if (span.getLength() > 1) {
                throw new IllegalStateException();
            }

            if (span.getLength() == 0) {
                setSize(0, 0);
            } else {

                // create and attach child node views
                add(new TextPaneSkinTextNodeView(textPaneSkin, (TextNode)span.get(0), 0));

                int breakWidth = getBreakWidth();

                TextPaneSkinNodeView nodeView = get(0);
                nodeView.setBreakWidth(breakWidth);
                nodeView.validate();

                setSize(nodeView.getWidth(), nodeView.getHeight());
            }
        }

        super.validate();
    }

    @Override
    public int getCharacterCount() {
        if (getLength() == 0) {
            return 0;
        } else {
            return get(0).getCharacterCount();
        }
    }

    @Override
    public TextPaneSkinNodeView getNext() {
        if (getLength() == 0) {
            return null;
        } else {
            return get(0).getNext();
        }
    }

    @Override
    public int getInsertionPoint(int x, int y) {
        if (getLength() == 0) {
            return -1;
        } else {
            return get(0).getInsertionPoint(x, y);
        }
    }

    @Override
    public int getNextInsertionPoint(int x, int from, TextPane.ScrollDirection direction) {
        if (getLength() == 0) {
            return -1;
        } else {
            return get(0).getNextInsertionPoint(x, from, direction);
        }
    }

    @Override
    public int getRowCount() {
        if (getLength() == 0) {
            return 0;
        } else {
            return get(0).getRowCount();
        }
    }

    @Override
    public int getRowAt(int offset) {
        if (getLength() == 0) {
            return 0;
        } else {
            return get(0).getRowAt(offset);
        }
    }

    @Override
    protected void setSkinLocation(int skinX, int skinY) {
        for (TextPaneSkinNodeView nodeView : this) {
            nodeView.setSkinLocation(skinX, skinY + nodeView.getY());
        }
    }

    @Override
    public void nodeInserted(Element element, int index) {
        super.nodeInserted(element, index);

        org.apache.pivot.wtk.text.Span span = (org.apache.pivot.wtk.text.Span)getNode();
        insert(textPaneSkin.createNodeView(span.get(index)), index);
    }

    @Override
    public void nodesRemoved(Element element, int index, Sequence<Node> nodes) {
        remove(index, nodes.getLength());

        super.nodesRemoved(element, index, nodes);
    }
}