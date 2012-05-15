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

import java.awt.Graphics2D;

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.media.ImageListener;
import org.apache.pivot.wtk.text.ImageNode;
import org.apache.pivot.wtk.text.ImageNodeListener;

class TextPaneSkinImageNodeView extends TextPaneSkinNodeView implements ImageNodeListener, ImageListener {
    public TextPaneSkinImageNodeView(ImageNode imageNode) {
        super(imageNode);
    }

    @Override
    protected void attach() {
        super.attach();

        ImageNode imageNode = (ImageNode)getNode();
        imageNode.getImageNodeListeners().add(this);

        Image image = imageNode.getImage();
        if (image != null) {
            image.getImageListeners().add(this);
        }
    }

    @Override
    protected void detach() {
        super.detach();

        ImageNode imageNode = (ImageNode)getNode();
        imageNode.getImageNodeListeners().remove(this);
    }

    @Override
    protected void childLayout(int breakWidth) {
        ImageNode imageNode = (ImageNode)getNode();
        Image image = imageNode.getImage();

        if (image == null) {
            setSize(0, 0);
        } else {
            setSize(image.getWidth(), image.getHeight());
        }
    }

    @Override
    public Dimensions getPreferredSize(int breakWidth) {
        ImageNode imageNode = (ImageNode)getNode();
        Image image = imageNode.getImage();

        if (image == null) {
            return new Dimensions(0, 0);
        }
        return new Dimensions(image.getWidth(), image.getHeight());
    }

    @Override
    protected void setSkinLocation(int skinX, int skinY) {
        // empty block
    }

    @Override
    public int getBaseline() {
        ImageNode imageNode = (ImageNode)getNode();
        Image image = imageNode.getImage();

        int baseline = -1;

        if (image != null) {
            baseline = image.getBaseline();
        }

        return baseline;
    }

    @Override
    public void paint(Graphics2D graphics) {
        ImageNode imageNode = (ImageNode)getNode();
        Image image = imageNode.getImage();

        if (image != null) {
            image.paint(graphics);
        }
    }

    @Override
    public int getInsertionPoint(int x, int y) {
        return 0;
    }

    @Override
    public int getNextInsertionPoint(int x, int from, TextPane.ScrollDirection direction) {
        return (from == -1) ? 0 : -1;
    }

    @Override
    public int getRowAt(int offset) {
        return -1;
    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public Bounds getCharacterBounds(int offset) {
        return new Bounds(0, 0, getWidth(), getHeight());
    }

    @Override
    public void imageChanged(ImageNode imageNode, Image previousImage) {
        invalidateUpTree();

        Image image = imageNode.getImage();
        if (image != null) {
            image.getImageListeners().add(this);
        }

        if (previousImage != null) {
            previousImage.getImageListeners().remove(this);
        }
    }

    @Override
    public void sizeChanged(Image image, int previousWidth, int previousHeight) {
        invalidateUpTree();
    }

    @Override
    public void baselineChanged(Image image, int previousBaseline) {
        // TODO Invalidate once baseline alignment of node view is supported
    }

    @Override
    public void regionUpdated(Image image, int x, int y, int width, int height) {
        repaint(x, y, width, height);
    }
}