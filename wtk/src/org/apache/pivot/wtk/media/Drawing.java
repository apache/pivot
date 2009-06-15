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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.media.drawing.Canvas;
import org.apache.pivot.wtk.media.drawing.CanvasListener;


/**
 * Image representing a vector drawing.
 *
 * @author gbrown
 */
public class Drawing extends Image {
    private static class DrawingListenerList extends ListenerList<DrawingListener>
        implements DrawingListener {
        public void canvasChanged(Drawing drawing, Canvas previousCanvas) {
            for (DrawingListener listener : this) {
                listener.canvasChanged(drawing, previousCanvas);
            }
        }

        public void backgroundChanged(Drawing drawing, Paint previousBackground) {
            for (DrawingListener listener : this) {
                listener.backgroundChanged(drawing, previousBackground);
            }
        }
    }

    private Canvas canvas;
    private Paint background = null;
    private int width = 0;
    private int height = 0;

    private CanvasListener canvasListener = new CanvasListener() {
        public void regionUpdated(Canvas canvas, int x, int y, int width, int height) {
            Bounds bounds = new Bounds(0, 0, Drawing.this.width, Drawing.this.height);
            bounds = bounds.intersect(new Bounds(x, y, width, height));
            imageListeners.regionUpdated(Drawing.this, x, y, width, height);
        }
    };

    private DrawingListenerList drawingListeners = new DrawingListenerList();

    public Drawing() {
        this(new Canvas());
    }

    public Drawing(Canvas canvas) {
        setCanvas(canvas);
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

            drawingListeners.canvasChanged(this, previousCanvas);
            imageListeners.regionUpdated(this, 0, 0, width, height);
        }
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        setSize(width, height);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        setSize(width, height);
    }

    public void setSize(int width, int height) {
        int previousWidth = this.width;
        int previousHeight = this.height;

        if (previousWidth != width
            || previousHeight != height) {
            this.width = width;
            this.height = height;
            imageListeners.sizeChanged(this, previousWidth, previousHeight);
        }
    }

    public Paint getBackground() {
        return background;
    }

    public void setBackground(Paint background) {
        Paint previousBackground = this.background;
        if (previousBackground != background) {
            this.background = background;
            imageListeners.regionUpdated(this, 0, 0, width, height);
            drawingListeners.backgroundChanged(this, previousBackground);
        }
    }

    public final void setBackground(String background) {
        if (background == null) {
            throw new IllegalArgumentException("background is null.");
        }

        setBackground(Color.decode(background));
    }

    public void paint(Graphics2D graphics) {
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
