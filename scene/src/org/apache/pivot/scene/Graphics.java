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
package org.apache.pivot.scene;

import org.apache.pivot.scene.media.Raster;

/**
 * Interface representing a drawing surface.
 */
public abstract class Graphics {
    /**
     * Enumeration representing a compositing operation.
     */
    public enum CompositeOperation {
        SOURCE_ATOP,
        SOURCE_IN,
        SOURCE_OUT,
        SOURCE_OVER,
        DESTINATION_ATOP,
        DESTINATION_IN,
        DESTINATION_OUT,
        DESTINATION_OVER,
        CLEAR,
        XOR
    }

    // Clipping
    public void clip(Bounds bounds) {
        clip(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public abstract void clip(int x, int y, int width, int height);
    public abstract Bounds getClipBounds();

    // Compositing
    public abstract float getAlpha();
    public abstract void setAlpha(float alpha);

    public abstract CompositeOperation getCompositeOperation();
    public abstract void setCompositeOperation(CompositeOperation compositeOperation);

    // Anti-aliasing
    public abstract boolean isAntiAliased();
    public abstract void setAntiAliased(boolean antiAliased);

    // Primitive drawing/filling
    public abstract Stroke getStroke();
    public abstract void setStroke(Stroke stroke);

    public abstract Paint getPaint();
    public abstract void setPaint(Paint paint);

    public abstract void drawLine(float x1, float y1, float x2, float y2);

    public abstract void drawRectangle(float x, float y, float width, float height, float cornerRadius);
    public abstract void drawArc(float x, float y, float width, float height, float start, float extent);
    public abstract void drawEllipse(float x, float y, float width, float height);
    public abstract void drawPath(PathGeometry pathGeometry);

    public abstract void fillRectangle(float x, float y, float width, float height, float cornerRadius);
    public abstract void fillArc(float x, float y, float width, float height, float start, float extent);
    public abstract void fillEllipse(float x, float y, float width, float height);
    public abstract void fillPath(PathGeometry pathGeometry);

    // Raster drawing
    public void drawRaster(Raster raster, int x, int y) {
        drawRaster(raster, x, y, raster.getWidth(), raster.getHeight());
    }

    public abstract void drawRaster(Raster raster, int x, int y, int width, int height);

    // Blitting
    public abstract void copyArea(int x, int y, int width, int height, int dx, int dy);

    // Text
    public abstract Font getFont();
    public abstract void setFont(Font font);

    public void drawText(CharSequence text, float x, float y) {
        drawText(text, 0, text.length(), x, y);
    }

    public abstract void drawText(CharSequence text, int start, int length, float x, float y);

    // Transformations
    public void translate(float dx, float dy) {
        // TODO
    }

    public void rotate(float theta) {
        // TODO
    }

    public void rotate(float theta, float x, float y) {
        // TODO
    }

    public void scale(float sx, float sy) {
        // TODO
    }

    public void transform(Transform transform) {
        transform(transform.m11, transform.m12, transform.m21, transform.m22, transform.dx, transform.dy);
    }

    public abstract void transform(float m11, float m12, float m21, float m22, float dx, float dy);
    public abstract Transform getCurrentTransform();

    // Creation/disposal
    public abstract Graphics create();
    public abstract void dispose();

    public abstract Object getNativeGraphics();
}
