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

import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.text.Node;
import org.apache.pivot.wtk.text.Paragraph;

class TextAreaSkinParagraphView extends TextAreaSkinElementView {
    
    private static final int PARAGRAPH_TERMINATOR_WIDTH = 4;
    
    private final TextAreaSkin textAreaSkin;

    private class Row {
        public int x = 0;
        public int y = 0;
        public int width = 0;
        public int height = 0;
        public ArrayList<TextAreaSkinNodeView> nodeViews = new ArrayList<TextAreaSkinNodeView>();
    }

    private ArrayList<Row> rows = null;
    private Bounds terminatorBounds = new Bounds(0, 0, 0, 0);

    public TextAreaSkinParagraphView(TextAreaSkin textAreaSkin, Paragraph paragraph) {
        super(paragraph);
        this.textAreaSkin = textAreaSkin;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        terminatorBounds = null;
    }

    @Override
    public void validate() {
        if (!isValid()) {
            // Break the views into multiple rows
            int breakWidth = getBreakWidth();

            Paragraph paragraph = (Paragraph)getNode();
            rows = new ArrayList<Row>();

            Row row = new Row();
            for (Node node : paragraph) {
                TextAreaSkinNodeView nodeView = textAreaSkin.createNodeView(node);

                nodeView.setBreakWidth(Math.max(breakWidth - (row.width
                    + PARAGRAPH_TERMINATOR_WIDTH), 0));
                nodeView.validate();

                int nodeViewWidth = nodeView.getWidth();

                if (row.width + nodeViewWidth > breakWidth
                    && row.width > 0) {
                    // The view is too big to fit in the remaining space,
                    // and it is not the only view in this row
                    rows.add(row);
                    row = new Row();
                    row.width = 0;
                }

                // Add the view to the row
                row.nodeViews.add(nodeView);
                row.width += nodeViewWidth;

                // If the view was split into multiple views, add them to
                // their own rows
                nodeView = nodeView.getNext();
                while (nodeView != null) {
                    rows.add(row);
                    row = new Row();

                    nodeView.setBreakWidth(breakWidth);
                    nodeView.validate();

                    row.nodeViews.add(nodeView);
                    row.width = nodeView.getWidth();

                    nodeView = nodeView.getNext();
                }
            }

            // Add the last row
            if (row.nodeViews.getLength() > 0) {
                rows.add(row);
            }

            // Clear all existing views
            remove(0, getLength());

            // Add the row views to this view, lay out, and calculate height
            int x = 0;
            int width = 0;
            int height = 0;
            for (int i = 0, n = rows.getLength(); i < n; i++) {
                row = rows.get(i);
                row.y = height;

                width = Math.max(width, row.width);

                // Determine the row height
                for (TextAreaSkinNodeView nodeView : row.nodeViews) {
                    row.height = Math.max(row.height, nodeView.getHeight());
                }

                // TODO Align horizontally when Elements support a horizontal
                // alignment property
                x = 0;
                for (TextAreaSkinNodeView nodeView : row.nodeViews) {
                    // TODO Align to baseline
                    int y = row.height - nodeView.getHeight();

                    nodeView.setLocation(x, y + height);
                    x += nodeView.getWidth();

                    add(nodeView);
                }

                height += row.height;
            }

            // Recalculate terminator bounds
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            LineMetrics lm = textAreaSkin.getFont().getLineMetrics("", 0, 0, fontRenderContext);
            int terminatorHeight = (int)Math.ceil(lm.getHeight());

            int terminatorY;
            if (getCharacterCount() == 1) {
                // The terminator is the only character in this paragraph
                terminatorY = 0;
            } else {
                terminatorY = height - terminatorHeight;
            }

            terminatorBounds = new Bounds(x, terminatorY,
                PARAGRAPH_TERMINATOR_WIDTH, terminatorHeight);

            // Ensure that the paragraph is visible even when empty
            width += terminatorBounds.width;
            height = Math.max(height, terminatorBounds.height);

            setSize(width, height);
        }

        super.validate();
    }

    @Override
    protected void setSkinLocation(int skinX, int skinY) {
        for (int i = 0, n = rows.getLength(); i < n; i++) {
            Row row = rows.get(i);
            for (TextAreaSkinNodeView nodeView : row.nodeViews) {
                nodeView.setSkinLocation(skinX + nodeView.getX(), skinY + nodeView.getY());
            }
        }
    }

    @Override
    public TextAreaSkinNodeView getNext() {
        return null;
    }

    @Override
    public int getInsertionPoint(int x, int y) {
        int offset = -1;

        int n = rows.getLength();
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                Row row = rows.get(i);

                if (y >= row.y
                    && y < row.y + row.height) {
                    if (x < row.x) {
                        TextAreaSkinNodeView firstNodeView = row.nodeViews.get(0);
                        offset = firstNodeView.getOffset();
                    } else if (x > row.x + row.width - 1) {
                        TextAreaSkinNodeView lastNodeView = row.nodeViews.get(row.nodeViews.getLength() - 1);
                        offset = lastNodeView.getOffset() + lastNodeView.getCharacterCount();

                        if (offset < getCharacterCount() - 1) {
                            offset--;
                        }
                    } else {
                        for (TextAreaSkinNodeView nodeView : row.nodeViews) {
                            Bounds nodeViewBounds = nodeView.getBounds();

                            if (nodeViewBounds.contains(x, y)) {
                                offset = nodeView.getInsertionPoint(x - nodeView.getX(), y - nodeView.getY())
                                    + nodeView.getOffset();
                                break;
                            }
                        }
                    }
                }

                if (offset != -1) {
                    break;
                }
            }
        } else {
            offset = getCharacterCount() - 1;
        }

        return offset;
    }

    @Override
    public int getNextInsertionPoint(int x, int from, FocusTraversalDirection direction) {
        int offset = -1;

        int n = rows.getLength();
        if (n == 0
            && from == -1) {
            // There are no rows; select the terminator character
            offset = 0;
        } else {
            int i;
            if (from == -1) {
                i = (direction == FocusTraversalDirection.FORWARD) ? -1 : rows.getLength();
            } else {
                // Find the row that contains offset
                if (from == getCharacterCount() - 1) {
                    i = rows.getLength() - 1;
                } else {
                    i = 0;
                    while (i < n) {
                        Row row = rows.get(i);
                        TextAreaSkinNodeView firstNodeView = row.nodeViews.get(0);
                        TextAreaSkinNodeView lastNodeView = row.nodeViews.get(row.nodeViews.getLength() - 1);
                        if (from >= firstNodeView.getOffset()
                            && from < lastNodeView.getOffset() + lastNodeView.getCharacterCount()) {
                            break;
                        }

                        i++;
                    }
                }
            }

            // Move to the next or previous row
            if (direction == FocusTraversalDirection.FORWARD) {
                i++;
            } else {
                i--;
            }

            if (i >= 0
                && i < n) {
                // Find the node view that contains x and get the insertion point from it
                Row row = rows.get(i);

                for (TextAreaSkinNodeView nodeView : row.nodeViews) {
                    Bounds bounds = nodeView.getBounds();
                    if (x >= bounds.x
                        && x < bounds.x + bounds.width) {
                        offset = nodeView.getNextInsertionPoint(x - nodeView.getX(), -1, direction)
                            + nodeView.getOffset();
                        break;
                    }
                }

                if (offset == -1) {
                    // No node view contained the x position; move to the end of the row
                    TextAreaSkinNodeView lastNodeView = row.nodeViews.get(row.nodeViews.getLength() - 1);
                    offset = lastNodeView.getOffset() + lastNodeView.getCharacterCount();

                    if (offset < getCharacterCount() - 1) {
                        offset--;
                    }
                }
            }
        }

        return offset;
    }

    @Override
    public int getRowIndex(int offset) {
        int rowIndex = -1;

        if (offset == getCharacterCount() - 1) {
            rowIndex = (rows.getLength() == 0) ? 0 : rows.getLength() - 1;
        } else {
            for (int i = 0, n = rows.getLength(); i < n; i++) {
                Row row = rows.get(i);
                TextAreaSkinNodeView firstNodeView = row.nodeViews.get(0);
                TextAreaSkinNodeView lastNodeView = row.nodeViews.get(row.nodeViews.getLength() - 1);

                if (offset >= firstNodeView.getOffset()
                    && offset < lastNodeView.getOffset() + lastNodeView.getCharacterCount()) {
                    rowIndex = i;
                    break;
                }
            }
        }

        return rowIndex;
    }

    @Override
    public int getRowCount() {
        return Math.max(rows.getLength(), 1);
    }

    @Override
    public Bounds getCharacterBounds(int offset) {
        Bounds bounds;

        if (offset == getCharacterCount() - 1) {
            bounds = terminatorBounds;
        } else {
            bounds = super.getCharacterBounds(offset);
        }

        return bounds;
    }
}