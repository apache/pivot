package pivot.wtk.media.drawing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.Iterator;

import pivot.collections.Sequence;
import pivot.wtk.Bounds;
import pivot.wtk.Point;

public abstract class Shape {
    public final class TransformSequence extends Transform
        implements Sequence<Transform>, Iterable<Transform> {
        public float[][] getMatrix() {
            // TODO
            return null;
        }

        public int add(Transform item) {
            // TODO Auto-generated method stub
            return 0;
        }

        public void insert(Transform item, int index) {
            // TODO Auto-generated method stub

        }

        public Transform update(int index, Transform item) {
            // TODO Auto-generated method stub
            return null;
        }

        public int remove(Transform item) {
            // TODO Auto-generated method stub
            return 0;
        }

        public Sequence<Transform> remove(int index, int count) {
            // TODO Auto-generated method stub
            return null;
        }

        public Transform get(int index) {
            // TODO Auto-generated method stub
            return null;
        }

        public int indexOf(Transform item) {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getLength() {
            // TODO Auto-generated method stub
            return 0;
        }

        public Iterator<Transform> iterator() {
            // TODO
            return null;
        }
    }

    private int x = 0;
    private int y = 0;

    private Paint fill = null;
    private Paint stroke = Color.BLACK;
    private int strokeThickness = 1;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Point getOrigin() {
        return new Point(x, y);
    }

    public void setOrigin(int x, int y) {
        setX(x);
        setY(y);
    }

    public void setOrigin(Point origin) {
        if (origin == null) {
            throw new IllegalArgumentException("origin is null.");
        }

        setOrigin(origin.x, origin.y);
    }

    public Paint getFill() {
        return fill;
    }

    public void setFill(Paint fill) {
        this.fill = fill;
    }

    public void setFill(String fill) {
        if (fill == null) {
            throw new IllegalArgumentException("fill is null.");
        }

        // TODO Support an encoding for gradient paints?

        setFill(Color.decode(fill));
    }

    public Paint getStroke() {
        return stroke;
    }

    public void setStroke(Paint stroke) {
        this.stroke = stroke;
    }

    public void setStroke(String stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("stroke is null.");
        }

        // TODO Support an encoding for gradient paints?

        setStroke(Color.decode(stroke));
    }

    public Bounds getBounds() {
        // TODO Transform untransformed bounds
        return null;
    }

    public abstract Bounds getUntransformedBounds();

    public TransformSequence getTransform() {
        // TODO
        return null;
    }

    public void paint(Graphics2D graphics) {
        graphics = (Graphics2D)graphics.create();

        // TODO Apply transform to graphics

        if (fill != null) {
            graphics.setPaint(fill);
            fill(graphics);
        }

        if (stroke != null) {
            graphics.setPaint(stroke);
            graphics.setStroke(new BasicStroke(strokeThickness));
            stroke(graphics);
        }

        graphics.dispose();
    }

    public abstract void fill(Graphics2D graphics);
    public abstract void stroke(Graphics2D graphics);
}
