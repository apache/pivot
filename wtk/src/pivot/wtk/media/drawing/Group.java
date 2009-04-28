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
package pivot.wtk.media.drawing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Iterator;
import pivot.collections.ArrayList;
import pivot.collections.Sequence;
import pivot.util.ImmutableIterator;
import pivot.util.ListenerList;
import pivot.wtk.Bounds;

/**
 * Shape representing a collection of other shapes.
 *
 * @author gbrown
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

    private Bounds bounds = null;
    private ArrayList<Shape> shapes = new ArrayList<Shape>();

    private GroupListenerList groupListeners = new GroupListenerList();

    @Override
    public Bounds getBounds() {
        if (bounds == null) {
            int top = 0;
            int left = 0;
            int bottom = 0;
            int right = 0;

            // Recalculate bounds
            for (Shape shape : shapes) {
                Bounds shapeBounds = shape.getBounds();
                top = Math.min(shapeBounds.y, top);
                left = Math.min(shapeBounds.x, left);
                bottom = Math.max(shapeBounds.y + shapeBounds.height - 1, bottom);
                right = Math.max(shapeBounds.x + shapeBounds.width - 1, right);
            }

            bounds = new Bounds(left,top, right - left + 1, bottom - top + 1);
        }

        return bounds;
    }

    public void draw(Graphics2D graphics) {
        // Draw each sub-shape
        for (Shape shape : shapes) {
            // TODO Only draw if transformed bounds intersects clip rect

            Graphics2D shapeGraphics = (Graphics2D)graphics.create();
            shapeGraphics.translate(shape.getX(), shape.getY());

            // TODO Transform graphics

            shape.draw(shapeGraphics);
            shapeGraphics.dispose();
        }

        // Draw a debug rectangle
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND, 2.0f, new float[] {0.0f, 4.0f}, 0.0f));
        graphics.draw(getBounds().toRectangle());
    }

    @Override
    protected void invalidate() {
        super.invalidate();
        bounds = null;
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

    @Override
    public boolean contains(int x, int y) {
        // TODO Ask each shape if it contains the point (translate to sub-shape
        // space first and apply inverse transform)
        return false;
    }

    public Shape getShapeAt(int x, int y) {
        // TODO Walk shape list from top to bottom; if shape bounds contains
        // x, y, call contains() on the shape
        return null;
    }

    public Shape getDescendantAt(int x, int y) {
        // TODO Mirror behavior of Container#getDescendantAt()
        return null;
    }

    public Iterator<Shape> iterator() {
        return new ImmutableIterator<Shape>(shapes.iterator());
    }

    public ListenerList<GroupListener> getGroupListeners() {
        return groupListeners;
    }
}
