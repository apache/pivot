package pivot.wtk.skin.terra;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Spacer;
import pivot.wtk.skin.ComponentSkin;

public class SpacerSkin extends ComponentSkin {
    private Color color = Color.BLACK;
    private int thickness = 1;
    private int padding = 4;

    public void install(Component component) {
        validateComponentType(component, Spacer.class);

        super.install(component);
    }

    public int getPreferredWidth(int height) {
        return 0;
    }

    public int getPreferredHeight(int width) {
        return thickness + padding * 2;
    }

    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();

        Line2D line = new Line2D.Double(0, height / 2, width, height / 2);
        graphics.setStroke(new BasicStroke(thickness));
        graphics.setColor(color);
        graphics.draw(line);
    }

    /**
     * @return
     * <tt>false</tt>; spacers are not focusable.
     */
    @Override
    public boolean isFocusable() {
        return false;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.color = color;
        repaintComponent();
    }

    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(Color.decode(color));
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
        invalidateComponent();
    }

    public final void setThickness(String thickness) {
        if (thickness == null) {
            throw new IllegalArgumentException("thickness is null.");
        }

        setThickness(Integer.parseInt(thickness));
    }
}
