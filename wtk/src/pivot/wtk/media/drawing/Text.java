package pivot.wtk.media.drawing;

import java.awt.Font;
import java.awt.Graphics2D;

import pivot.wtk.Bounds;

/**
 * TODO We may need to specify a font here - otherwise, we won't be able to
 * calculate the bounds.
 */
public class Text extends Shape {
    private String text = null;
    private Font font = null;
    private int wrapWidth = -1;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        // TODO We may need to throw if null
        this.font = font;
    }

    public int getWrapWidth() {
        return wrapWidth;
    }

    public void setWrapWidth(int wrapWidth) {
        if (wrapWidth < -1) {
            throw new IllegalArgumentException(wrapWidth
                + " is not a valid value for wrap width.");
        }

        this.wrapWidth = wrapWidth;
    }

    @Override
    public Bounds getUntransformedBounds() {
        // TODO Apply font as needed
        // TODO Wrap text as needed

        return null;
    }

    @Override
    public void fill(Graphics2D graphics) {
        // TODO Auto-generated method stub
    }

    @Override
    public void stroke(Graphics2D graphics) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean contains(int x, int y) {
        // TODO
        return false;
    }
}
