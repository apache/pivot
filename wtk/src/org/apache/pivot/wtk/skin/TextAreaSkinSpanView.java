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
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.text.Element;
import org.apache.pivot.wtk.text.Node;

class TextAreaSkinSpanView extends TextAreaSkinElementView {

    private final TextAreaSkin textAreaSkin;

    public TextAreaSkinSpanView(TextAreaSkin textAreaSkin, org.apache.pivot.wtk.text.Span span) {
        super(span);
        this.textAreaSkin = textAreaSkin;
    }

    @Override
    public void validate() {
        if (!isValid()) {
            // I have to re-create my children here instead of in attach(), because that is how ParagraphView works,
            // and ParagraphView is always my parent node.

            // Clear all existing views
            remove(0, getLength());

            // Attach child node views
            org.apache.pivot.wtk.text.Span span = (org.apache.pivot.wtk.text.Span)getNode();
            for (Node node : span) {
                add(textAreaSkin.createNodeView(node));
            }

            // TODO like TextAreaSkinTextNodeView, I need to implement line-breaking
            
            int breakWidth = getBreakWidth();

            int width = 0;
            int height = 0;

            for (TextAreaSkinNodeView nodeView : this) {
                nodeView.setBreakWidth(breakWidth);
                nodeView.validate();

                nodeView.setLocation(0, height);

                width = Math.max(width, nodeView.getWidth());
                height += nodeView.getHeight();
            }

            setSize(width, height);

            super.validate();
        }
    }

    @Override
    public int getInsertionPoint(int x, int y) {
        int offset = -1;

        for (int i = 0, n = getLength(); i < n; i++) {
            TextAreaSkinNodeView nodeView = get(i);
            Bounds nodeViewBounds = nodeView.getBounds();

            if (y >= nodeViewBounds.y
                && y < nodeViewBounds.y + nodeViewBounds.height) {
                offset = nodeView.getInsertionPoint(x - nodeView.getX(), y - nodeView.getY())
                    + nodeView.getOffset();
                break;
            }
        }

        return offset;
    }

    @Override
    public TextAreaSkinNodeView getNext() {
        return null;
    }

    @Override
    public int getNextInsertionPoint(int x, int from, FocusTraversalDirection direction) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getRowCount() {
        int rowCount = 0;

        for (TextAreaSkinNodeView nodeView : this) {
            rowCount += nodeView.getRowCount();
        }

        return rowCount;
    }

    @Override
    public int getRowIndex(int offset) {
        int rowIndex = 0;

        for (TextAreaSkinNodeView nodeView : this) {
            int nodeViewOffset = nodeView.getOffset();
            int characterCount = nodeView.getCharacterCount();

            if (offset >= nodeViewOffset
                && offset < nodeViewOffset + characterCount) {
                rowIndex += nodeView.getRowIndex(offset - nodeView.getOffset());
                break;
            }

            rowIndex += nodeView.getRowCount();
        }

        return rowIndex;
    }

    @Override
    protected void setSkinLocation(int skinX, int skinY) {
        for (TextAreaSkinNodeView nodeView : this) {
            nodeView.setSkinLocation(skinX, skinY + nodeView.getY());
        }
    }

    @Override
    public void nodeInserted(Element element, int index) {
        super.nodeInserted(element, index);

        org.apache.pivot.wtk.text.Span span = (org.apache.pivot.wtk.text.Span)getNode();
        insert(textAreaSkin.createNodeView(span.get(index)), index);
    }

    @Override
    public void nodesRemoved(Element element, int index, Sequence<Node> nodes) {
        remove(index, nodes.getLength());

        super.nodesRemoved(element, index, nodes);
    }
}