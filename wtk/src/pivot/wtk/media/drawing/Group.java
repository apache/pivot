package pivot.wtk.media.drawing;

import java.awt.Graphics2D;
import java.util.Iterator;

import pivot.collections.ArrayList;
import pivot.collections.Sequence;
import pivot.util.ImmutableIterator;
import pivot.wtk.Bounds;

public class Group extends Shape implements Sequence<Shape>, Iterable<Shape> {
    private ArrayList<Shape> shapes = new ArrayList<Shape>();

    public Bounds getUntransformedBounds() {
        // TODO
        return null;
    }

    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        for (Shape shape : this) {
            Graphics2D shapeGraphics = (Graphics2D)graphics.create();

            // TODO Apply transform to graphics

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

        // TODO Update bounds
    }

    public Shape update(int index, Shape shape) {
        Shape previousShape = shapes.update(index, shape);

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

    public Iterator<Shape> iterator() {
        return new ImmutableIterator<Shape>(shapes.iterator());
    }
}
