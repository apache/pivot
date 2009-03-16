/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.awt.Graphics2D;
import java.util.Iterator;

import pivot.collections.ArrayList;
import pivot.collections.Sequence;
import pivot.util.ImmutableIterator;
import pivot.wtk.Bounds;

/**
 * Shape representing a collection of other shapes.
 * <p>
 * TODO Throw in setStroke() and setFill()?
 */
public class Group extends Shape implements Sequence<Shape>, Iterable<Shape> {
    private ArrayList<Shape> shapes = new ArrayList<Shape>();

    public Bounds getUntransformedBounds() {
        // TODO
        return null;
    }

    public void paint(Graphics2D graphics) {
        for (Shape shape : this) {
            Graphics2D shapeGraphics = (Graphics2D)graphics.create();

            // TODO Translate to origin, then apply transform to graphics

            // TODO Only paint if shape bounds intersects clip bounds

            shape.paint(shapeGraphics);
            shapeGraphics.dispose();
        }
    }

    public void fill(Graphics2D graphics) {
        // No-op
    }

    public void stroke(Graphics2D graphics) {
        // No-op
    }

    public int add(Shape shape) {
        int index = shapes.getLength();
        insert(shape, index);

        return index;
    }

    public void insert(Shape shape, int index) {
        shapes.insert(shape, index);

        // TODO Set parent
        // TODO Update bounds
    }

    public Shape update(int index, Shape shape) {
        Shape previousShape = shapes.update(index, shape);

        // TODO Set parent
        // TODO Update bounds

        return previousShape;
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

        // TODO Clear parent
        // TODO Update bounds

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
        // TODO
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
}
