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

import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.text.TextSpan;

/**
 * Span node view.
 */
class TextPaneSkinSpanView extends TextPaneSkinElementView {

    public TextPaneSkinSpanView(TextSpan span) {
        super(span);
    }

    @Override
    protected void attach() {
        super.attach();

        TextSpan span = (TextSpan)getNode();

        // for now, assume that span contains at most one child, and
        // that child is a TextNode
        if (span.getLength() > 1) {
            throw new IllegalStateException();
        }
    }

    @Override
    protected void childLayout(int breakWidth) {
        if (getLength() == 0) {
            setSize(0, 0);
        } else {
            TextPaneSkinNodeView nodeView = get(0);
            nodeView.layout(breakWidth);

            setSize(nodeView.getWidth(), nodeView.getHeight());
        }
    }

    @Override
    public Dimensions getPreferredSize(int breakWidth) {
        if (getLength() == 0) {
            return new Dimensions(0, 0);
        }

        TextPaneSkinNodeView nodeView = get(0);
        Dimensions childDimensions = nodeView.getPreferredSize(breakWidth);

        return childDimensions;
    }

    @Override
    public int getCharacterCount() {
        if (getLength() == 0) {
            return 0;
        }
        return get(0).getCharacterCount();
    }

    /**
     * Used by TextPaneSkinParagraphView when it breaks child nodes into multiple views.
     */
    public TextPaneSkinTextNodeView getNext() {
        if (getLength() == 0) {
            return null;
        }
        return (TextPaneSkinTextNodeView) ((TextPaneSkinTextNodeView) get(0)).getNext();
    }

    @Override
    public int getInsertionPoint(int x, int y) {
        if (getLength() == 0) {
            return -1;
        }
        return get(0).getInsertionPoint(x, y);
    }

    @Override
    public int getNextInsertionPoint(int x, int from, TextPane.ScrollDirection direction) {
        if (getLength() == 0) {
            return -1;
        }
        return get(0).getNextInsertionPoint(x, from, direction);
    }

    @Override
    public int getRowCount() {
        if (getLength() == 0) {
            return 0;
        }
        return get(0).getRowCount();
    }

    @Override
    public int getRowAt(int offset) {
        if (getLength() == 0) {
            return 0;
        }
        return get(0).getRowAt(offset);
    }

    @Override
    protected void setSkinLocation(int skinX, int skinY) {
        super.setSkinLocation(skinX, skinY);
        for (TextPaneSkinNodeView nodeView : this) {
            nodeView.setSkinLocation(skinX, skinY + nodeView.getY());
        }
    }

}