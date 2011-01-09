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
package org.apache.pivot.scene.shape;

import org.apache.pivot.scene.Color;
import org.apache.pivot.scene.Graphics;
import org.apache.pivot.scene.Node;
import org.apache.pivot.scene.Paint;
import org.apache.pivot.scene.SolidColorPaint;
import org.apache.pivot.scene.Stroke;

/**
 * Abstract base class for shapes.
 */
public abstract class Shape extends Node {
    private Paint fill = null;
    private Paint outline = new SolidColorPaint(Color.BLACK);
    private Stroke stroke = new Stroke();
    private boolean antiAliased = true;

    public Paint getFill() {
        return fill;
    }

    public void setFill(Paint fill) {
        Paint previousFill = this.fill;
        if (previousFill != fill) {
            this.fill = fill;
            repaint();
        }
    }

    public final void setFill(String fill) {
        if (fill == null) {
            throw new IllegalArgumentException("fill is null.");
        }

        setFill(fill.length() == 0 ? null : Paint.decode(fill));
    }

    public Paint getOutline() {
        return outline;
    }

    public void setOutline(Paint outline) {
        Paint previousOutline = this.outline;
        if (previousOutline != outline) {
            this.outline = outline;
            repaint();
        }
    }

    public final void setOutline(String outline) {
        if (outline == null) {
            throw new IllegalArgumentException("outline is null.");
        }

        setOutline(outline.length() == 0 ? null : Paint.decode(outline));
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(Stroke stroke) {
        Stroke previousStroke = this.stroke;
        if (previousStroke != stroke) {
            this.stroke = stroke;
            invalidate();
        }
    }

    public void setStroke(String stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("stroke is null.");
        }

        setStroke(stroke.length() == 0 ? null : Stroke.decode(stroke));
    }

    public boolean isAntiAliased() {
        return antiAliased;
    }

    public void setAntiAliased(boolean antiAliased) {
        if (this.antiAliased != antiAliased) {
            this.antiAliased = antiAliased;
            invalidate();
        }
    }

    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public int getPreferredWidth(int height) {
        return 0;
    }

    @Override
    public int getPreferredHeight(int width) {
        return 0;
    }

    @Override
    public int getBaseline(int width, int height) {
        return -1;
    }

    @Override
    public void layout() {
        // No-op
    }

    @Override
    public void paint(Graphics graphics) {
        if (fill != null) {
            graphics.setPaint(fill);
            fillShape(graphics);
        }

        if (outline != null) {
            graphics.setStroke(stroke);
            drawShape(graphics);
        }
    }

    protected abstract void drawShape(Graphics graphics);
    protected abstract void fillShape(Graphics graphics);
}
