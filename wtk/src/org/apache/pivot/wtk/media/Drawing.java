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
import java.awt.Paint;
import java.awt.RenderingHints;

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.media.drawing.Canvas;
import org.apache.pivot.wtk.media.drawing.CanvasListener;

/**
 * Image representing a vector drawing.
 */
@DefaultProperty("canvas")
public class Drawing extends Image {
    private static class DrawingListenerList extends ListenerList<DrawingListener>
        implements DrawingListener {
        @Override
        public void canvasChanged(Drawing drawing, Canvas previousCanvas) {
            for (DrawingListener listener : this) {
                listener.canvasChanged(drawing, previousCanvas);
            }
        }

        @Override
        public void backgroundChanged(Drawing drawing, Paint previousBackground) {
            for (DrawingListener listener : this) {
                listener.backgroundChanged(drawing, previousBackground);
            }
        }
    }

    private Canvas canvas = null;
    private Paint background = null;

    private Dimensions size = null;

    private int baseline = -1;

    private CanvasListener canvasListener = new CanvasListener() {
        @Override
        public void regionUpdated(Canvas canvas, int x, int y, int width, int height) {
            imageListeners.regionUpdated(Drawing.this, x, y, width, height);
        }

        @Override
        public void canvasInvalidated(Canvas canvas) {
            int previousWidth = size.width;
            int previousHeight = size.height;

            invalidate();

            imageListeners.sizeChanged(Drawing.this, previousWidth, previousHeight);
        }
    };

    private DrawingListenerList drawingListeners = new DrawingListenerList();

    public Drawing() {
        this(null);
    }

    public Drawing(Canvas canvas) {
        setCanvas(canvas);
    }

    @Override
    public int getWidth() {
        validate();
        return size.width;
    }

    @Override
    public int getHeight() {
        validate();
        return size.height;
    }

    @Override
    public int getBaseline() {
        return baseline;
    }

    public void setBaseline(int baseline) {
        int previousBaseline = this.baseline;

        if (baseline != previousBaseline) {
            this.baseline = baseline;
            imageListeners.baselineChanged(this, previousBaseline);
        }
    }

    private void invalidate() {
        size = null;
    }

    private void validate() {
        if (size == null) {
            int width, height;
            if (canvas == null) {
                width = 0;
                height = 0;
            } else {
                Bounds canvasBounds = canvas.getBounds();
                width = Math.max(canvasBounds.x + canvasBounds.width, 0);
                height = Math.max(canvasBounds.y + canvasBounds.height, 0);
            }

            size = new Dimensions(width, height);
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        Canvas previousCanvas = this.canvas;

        if (previousCanvas != canvas) {
            this.canvas = canvas;

            if (previousCanvas != null) {
                previousCanvas.getCanvasListeners().remove(canvasListener);
            }

            if (canvas != null) {
                canvas.getCanvasListeners().add(canvasListener);
            }

            size = null;

            drawingListeners.canvasChanged(this, previousCanvas);
        }
    }

    public Paint getBackground() {
        return background;
    }

    public void setBackground(Paint background) {
        Paint previousBackground = this.background;
        if (previousBackground != background) {
            this.background = background;
            drawingListeners.backgroundChanged(this, previousBackground);
        }
    }

    public final void setBackground(String background) {
        if (background == null) {
            throw new IllegalArgumentException("background is null.");
        }

        setBackground(GraphicsUtilities.decodePaint(background));
    }

    @Override
    public void paint(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();

        graphics.clipRect(0, 0, width, height);

        if (background != null) {
            graphics.setPaint(background);
            graphics.fillRect(0, 0, width, height);
        }

        if (canvas != null) {
            // TODO Make this configurable?
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            canvas.draw(graphics);
        }
    }

    public ListenerList<DrawingListener> getDrawingListeners() {
        return drawingListeners;
    }
}
