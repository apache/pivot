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

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Point;


/**
 * Shape representing a collection of other shapes.
 */
public class Group extends Shape implements Sequence<Shape>, Iterable<Shape> {
    private static class GroupListenerList extends ListenerList<GroupListener>
        implements GroupListener {
        public void shapeInserted(Group group, int index) {
            for (GroupListener listener : this) {
                listener.shapeInserted(group, index);
            }
        }

        public void shapesRemoved(Group group, int index, int count) {
            for (GroupListener listener : this) {
                listener.shapesRemoved(group, index, count);
            }
        }
    }

    private ArrayList<Shape> shapes = new ArrayList<Shape>();

    private GroupListenerList groupListeners = new GroupListenerList();

    @Override
    public void setFill(Paint fill) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStroke(Paint stroke) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStrokeThickness(int strokeThickness) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(int x, int y) {
        // TODO
        return false;
    }

    public Shape getShapeAt(int x, int y) {
        Shape shape = null;

        int i = shapes.getLength() - 1;
        while (i >= 0) {
            shape = shapes.get(i);

            if (shape.isVisible()) {
                Bounds transformedBounds = shape.getTransformedBounds();

                if (transformedBounds.contains(x, y)) {
                    Point origin = shape.getOrigin();

                    // Transform into shape coordinates
                    AffineTransform affineTransform = shape.getTransforms().getAffineTransform();
                    java.awt.Point location = new java.awt.Point(x - origin.x, y - origin.y);

                    try {
                        affineTransform.inverseTransform(location, location);
                        if (shape.contains(location.x, location.y)) {
                            break;
                        }
                    } catch (NoninvertibleTransformException exception) {
                        // No-op
                    }
                }
            }

            i--;
        }

        if (i < 0) {
            shape = null;
        }

        return shape;
    }

    public Shape getDescendantAt(int x, int y) {
        Shape shape = getShapeAt(x, y);

        if (shape instanceof Group) {
            Group group = (Group)shape;
            Point origin = group.getOrigin();
            shape = group.getDescendantAt(x - origin.x, y - origin.y);
        }

        if (shape == null) {
            shape = this;
        }

        return shape;
    }

    @Override
    public void draw(Graphics2D graphics) {
        Bounds clipBounds = new Bounds(graphics.getClipBounds());

        // Draw each sub-shape
        for (Shape shape : shapes) {
            if (shape.isVisible()) {
                int x = shape.getX();
                int y = shape.getY();
                Bounds transformedBounds = shape.getTransformedBounds();

                if (transformedBounds.intersects(clipBounds)) {
                    Graphics2D shapeGraphics = (Graphics2D)graphics.create();
                    shapeGraphics.translate(x, y);
                    shapeGraphics.transform(shape.getTransforms().getAffineTransform());
                    shape.draw(shapeGraphics);
                    shapeGraphics.dispose();
                }
            }
        }

        // Draw a debug rectangle
        /*
        graphics.setColor(Color.DARK_GRAY);
        graphics.setStroke(new BasicStroke(0));
        Bounds bounds = getBounds();
        graphics.draw(new Rectangle2D.Double(bounds.x, bounds.y, bounds.width, bounds.height));
        */
    }

    @Override
    protected void validate() {
        if (!isValid()) {
            // Recalculate bounds
            int top = 0;
            int left = 0;
            int bottom = 0;
            int right = 0;

            for (int i = 0, n = shapes.getLength(); i < n; i++) {
                Shape shape = shapes.get(i);
                if (shape.isVisible()) {
                    shape.validate();
                    int x = shape.getX();
                    int y = shape.getY();
                    Bounds transformedBounds = shape.getTransformedBounds();

                    if (i == 0) {
                        top = y + transformedBounds.y;
                        left = x + transformedBounds.x;
                        bottom = y + transformedBounds.y
                            + transformedBounds.height - 1;
                        right = x + transformedBounds.x
                            + transformedBounds.width - 1;
                    } else {
                        top = Math.min(y + transformedBounds.y, top);
                        left = Math.min(x + transformedBounds.x, left);
                        bottom = Math.max(y + transformedBounds.y
                            + transformedBounds.height - 1, bottom);
                        right = Math.max(x + transformedBounds.x
                            + transformedBounds.width - 1, right);
                    }
                }
            }

            setBounds(left, top, right - left + 1, bottom - top + 1);
        }
    }

    public int add(Shape shape) {
        int index = shapes.getLength();
        insert(shape, index);

        return index;
    }

    public void insert(Shape shape, int index) {
        if (shape.getParent() != null) {
            throw new IllegalArgumentException();
        }

        shape.setParent(this);
        shapes.insert(shape, index);
        invalidate();
        groupListeners.shapeInserted(this, index);
    }

    public Shape update(int index, Shape shape) {
        throw new UnsupportedOperationException();
    }

    public int remove(Shape shape) {
        int index = shapes.indexOf(shape);
        if (index != -1) {
            remove(index, 1);
        }

        return index;
    }

    public Sequence<Shape> remove(int index, int count) {
        Sequence<Shape> removed = shapes.remove(index, count);

        for (int i = 0, n = removed.getLength(); i < n; i++) {
            Shape shape = removed.get(i);
            shape.setParent(null);
        }

        if (removed.getLength() > 0) {
            invalidate();
            groupListeners.shapesRemoved(this, index, count);
        }

        return removed;
    }

    public Shape get(int index) {
        return shapes.get(index);
    }

    public int indexOf(Shape shape) {
        return shapes.indexOf(shape);
    }

    public int getLength() {
        return shapes.getLength();
    }

    public Iterator<Shape> iterator() {
        return new ImmutableIterator<Shape>(shapes.iterator());
    }

    public ListenerList<GroupListener> getGroupListeners() {
        return groupListeners;
    }
}
