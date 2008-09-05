package pivot.wtk.media.drawing;

import java.awt.Graphics2D;

import pivot.wtk.Bounds;
import pivot.wtk.Dimensions;

public class Rectangle extends Shape {
    private int width = 0;
    private int height = 0;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        if (width < 0) {
            throw new IllegalArgumentException("width is null.");
        }

        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        if (height < 0) {
            throw new IllegalArgumentException("height is null.");
        }

        this.height = height;
    }

    public Dimensions getSize() {
        return new Dimensions(getWidth(), getHeight());
    }

    public void setSize(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    public void setSize(Dimensions size) {
        if (size == null) {
            throw new IllegalArgumentException("size is null.");
        }

        setSize(size.width, size.height);
    }

    @Override
    public Bounds getUntransformedBounds() {
        return new Bounds(getX(), getY(), width, height);
    }

    @Override
    public void fill(Graphics2D graphics) {
        graphics.fillRect(getX(), getY(), width, height);
    }

    @Override
    public void stroke(Graphics2D graphics) {
        graphics.drawRect(getX(), getY(), width, height);
    }

    @Override
    public boolean contains(int x, int y) {
        // TODO
        return false;
    }
}
