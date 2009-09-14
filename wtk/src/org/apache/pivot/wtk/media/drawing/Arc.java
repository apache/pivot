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
package org.apache.pivot.wtk.media.drawing;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Arc2D;

import org.apache.pivot.util.ListenerList;

/**
 * Shape representing an arc.
 */
public class Arc extends Shape {
    /**
     * Enum representing an arc closure type.
     */
    public enum Type {
        CHORD,
        OPEN,
        PIE
    }

    private static class ArcListenerList extends ListenerList<ArcListener>
        implements ArcListener {
        public void sizeChanged(Arc arc, int previousWidth, int previousHeight) {
            for (ArcListener listener : this) {
                listener.sizeChanged(arc, previousWidth, previousHeight);
            }
        }

        public void startChanged(Arc arc, float previousStart) {
            for (ArcListener listener : this) {
                listener.startChanged(arc, previousStart);
            }
        }

        public void extentChanged(Arc arc, float previousExtent) {
            for (ArcListener listener : this) {
                listener.extentChanged(arc, previousExtent);
            }
        }

        public void typeChanged(Arc arc, Arc.Type previousType) {
            for (ArcListener listener : this) {
                listener.typeChanged(arc, previousType);
            }
        }
    }

    private Arc2D.Float arc2D = new Arc2D.Float();

    private ArcListenerList arcListeners = new ArcListenerList();

    public int getWidth() {
        return (int)arc2D.width;
    }

    public void setWidth(int width) {
        setSize(width, (int)arc2D.height);
    }

    public int getHeight() {
        return (int)arc2D.height;
    }

    public void setHeight(int height) {
        setSize((int)arc2D.width, height);
    }

    public void setSize(int width, int height) {
        int previousWidth = (int)arc2D.width;
        int previousHeight = (int)arc2D.height;
        if (previousWidth != width
            || previousHeight != height) {
            arc2D.width = width;
            arc2D.height = height;
            invalidate();
            arcListeners.sizeChanged(this, previousWidth, previousHeight);
        }
    }

    public float getStart() {
        return arc2D.start;
    }

    public void setStart(float start) {
        float previousStart = arc2D.start;
        if (previousStart != start) {
            arc2D.start = start;
            invalidate();
            arcListeners.startChanged(this, previousStart);
        }
    }

    public float getExtent() {
        return arc2D.extent;
    }

    public void setExtent(float extent) {
        float previousExtent = arc2D.extent;
        if (previousExtent != extent) {
            arc2D.extent = extent;
            invalidate();
            arcListeners.extentChanged(this, previousExtent);
        }
    }

    public Type getType() {
        Type type = null;

        switch(arc2D.getArcType()) {
            case Arc2D.CHORD: {
                type = Type.CHORD;
                break;
            }

            case Arc2D.OPEN: {
                type = Type.OPEN;
                break;
            }

            case Arc2D.PIE: {
                type = Type.PIE;
                break;
            }
        }

        return type;
    }

    public void setType(Type type) {
        Type previousType = getType();
        if (previousType != type) {
            switch(type) {
                case CHORD: {
                    arc2D.setArcType(Arc2D.CHORD);
                    break;
                }

                case OPEN: {
                    arc2D.setArcType(Arc2D.OPEN);
                    break;
                }

                case PIE: {
                    arc2D.setArcType(Arc2D.PIE);
                    break;
                }
            }

            invalidate();
            arcListeners.typeChanged(this, previousType);
        }
    }

    public void setType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null.");
        }

        setType(Type.valueOf(type.toUpperCase()));
    }

    @Override
    public void draw(Graphics2D graphics) {
        Paint fill = getFill();
        if (fill != null) {
            graphics.setPaint(fill);
            graphics.fill(arc2D);
        }

        Paint stroke = getStroke();
        if (stroke != null) {
            int strokeThickness = getStrokeThickness();
            graphics.setPaint(stroke);
            graphics.setStroke(new BasicStroke(strokeThickness));
            graphics.draw(arc2D);
        }
    }

    @Override
    protected void validate() {
        if (!isValid()) {
            int strokeThickness = getStrokeThickness();
            setBounds(-strokeThickness / 2, -strokeThickness / 2,
                (int)arc2D.width + strokeThickness,
                (int)arc2D.height + strokeThickness);
        }
    }

    public ListenerList<ArcListener> getArcListeners() {
        return arcListeners;
    }
}
