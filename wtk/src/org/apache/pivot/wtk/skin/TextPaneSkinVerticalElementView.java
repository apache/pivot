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

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.text.Element;

/**
 * Some of the classes in the text hierarchy are very similar in layout ie. they lay their children out vertically. This class groups that functionality.
 */
abstract class TextPaneSkinVerticalElementView extends TextPaneSkinElementView {

    public TextPaneSkinVerticalElementView(Element element) {
        super(element);
    }

    @Override
    protected void childLayout(int breakWidth) {
        // TODO At some point, we may want to optimize this method by deferring layout of
        // non-visible views. If so, we should not recycle views but rather recreate them
        // (as is done in ParagraphView). This way, we avoid thread contention over the
        // existing views (e.g. trying to paint one while modifying its size/location, etc.).
        // Any invalid node views are simply replaced (in the queued callback, when the
        // thread has finished processing the new ones). This allows the definition of
        // validate() to remain as-is. Of course, if we redefine NodeView to implement
        // ConstrainedVisual, this may no longer be an issue.
        // Note that, if anything happens to invalidate the existence of the new views before
        // they are added to the document view, we need to make sure they are disposed (i.e.
        // detached).

        int width = 0;
        int height = 0;

        for (TextPaneSkinNodeView nodeView : this) {
            nodeView.layout(breakWidth);

            nodeView.setLocation(0, height);

            width = Math.max(width, nodeView.getWidth());
            height += nodeView.getHeight();
        }

        setSize(width, height);
    }

    @Override
    public Dimensions getPreferredSize(int breakWidth) {
        int width = 0;
        int height = 0;

        for (TextPaneSkinNodeView nodeView : this) {
            Dimensions childDimensions = nodeView.getPreferredSize(breakWidth);

            width = Math.max(width, childDimensions.width);
            height += childDimensions.height;
        }

        return new Dimensions(width, height);
    }

    @Override
    protected void setSkinLocation(int skinX, int skinY) {
        super.setSkinLocation(skinX, skinY);
        for (TextPaneSkinNodeView nodeView : this) {
            nodeView.setSkinLocation(skinX, skinY + nodeView.getY());
        }
    }

    @Override
    public int getInsertionPoint(int x, int y) {
        int offset = -1;

        for (int i = 0, n = getLength(); i < n; i++) {
            TextPaneSkinNodeView nodeView = get(i);
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

    @SuppressWarnings("null")  // false warning from eclipse
    @Override
    public int getNextInsertionPoint(int x, int from, TextPane.ScrollDirection direction) {
        int offset = -1;

        if (getLength() > 0) {
            if (from == -1) {
                int i = (direction == TextPane.ScrollDirection.DOWN) ? 0 : getLength() - 1;
                TextPaneSkinNodeView nodeView = get(i);
                offset = nodeView.getNextInsertionPoint(x - nodeView.getX(), -1, direction);

                if (offset != -1) {
                    offset += nodeView.getOffset();
                }
            } else {
                // Find the node view that contains the offset
                int n = getLength();
                int i = 0;

                while (i < n) {
                    TextPaneSkinNodeView nodeView = get(i);
                    int nodeViewOffset = nodeView.getOffset();
                    int characterCount = nodeView.getCharacterCount();

                    if (from >= nodeViewOffset
                        && from < nodeViewOffset + characterCount) {
                        break;
                    }

                    i++;
                }

                if (i < n) {
                    TextPaneSkinNodeView nodeView = get(i);
                    offset = nodeView.getNextInsertionPoint(x - nodeView.getX(),
                        from - nodeView.getOffset(), direction);

                    if (offset == -1) {
                        // Move to the next or previous node view
                        if (direction == TextPane.ScrollDirection.DOWN) {
                            nodeView = (i < n - 1) ? get(i + 1) : null;
                        } else {
                            nodeView = (i > 0) ? get(i - 1) : null;
                        }

                        if (nodeView != null) {
                            offset = nodeView.getNextInsertionPoint(x - nodeView.getX(), -1, direction);
                        }
                    }

                    if (offset != -1) {
                        offset += nodeView.getOffset();
                    }
                }
            }
        }

        return offset;
    }

    @Override
    public int getRowAt(int offset) {
        int rowIndex = 0;

        for (TextPaneSkinNodeView nodeView : this) {
            int nodeViewOffset = nodeView.getOffset();
            int characterCount = nodeView.getCharacterCount();

            if (offset >= nodeViewOffset
                && offset < nodeViewOffset + characterCount) {
                rowIndex += nodeView.getRowAt(offset - nodeView.getOffset());
                break;
            }

            rowIndex += nodeView.getRowCount();
        }

        return rowIndex;
    }

    @Override
    public int getRowCount() {
        int rowCount = 0;

        for (TextPaneSkinNodeView nodeView : this) {
            rowCount += nodeView.getRowCount();
        }

        return rowCount;
    }
}
