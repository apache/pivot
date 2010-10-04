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
package org.apache.pivot.wtk.media;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.apache.pivot.wtk.media.Image;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGRoot;
import com.kitfox.svg.animation.AnimationElement;

/**
 * Image encapsulating an SVG diagram.
 */
public class Drawing extends Image {
    private SVGDiagram diagram;

    public Drawing(SVGDiagram diagram) {
        if (diagram == null) {
            throw new IllegalArgumentException();
        }

        this.diagram = diagram;
    }

    public SVGDiagram getDiagram() {
        return diagram;
    }

    @Override
    public int getWidth() {
        return (int)Math.ceil(diagram.getWidth());
    }

    @Override
    public int getHeight() {
        return (int)Math.ceil(diagram.getHeight());
    }

    public void setSize(int width, int height) {
        int previousWidth = getWidth();
        int previousHeight = getHeight();

        SVGRoot root = diagram.getRoot();
        try {
            root.setAttribute("width", AnimationElement.AT_XML, Integer.toString(width));
            root.setAttribute("height", AnimationElement.AT_XML, Integer.toString(height));
        } catch (SVGElementException exception) {
            throw new RuntimeException(exception);
        }

        try {
            diagram.updateTime(0.0);
        } catch (SVGException exception) {
            throw new RuntimeException(exception);
        }

        imageListeners.sizeChanged(this, previousWidth, previousHeight);
    }

    @Override
    public void paint(Graphics2D graphics) {
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            diagram.render(graphics);
        } catch (SVGException exception) {
            throw new RuntimeException(exception);
        }
    }
}
