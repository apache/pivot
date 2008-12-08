package pivot.wtk.effects;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;

import pivot.wtk.Component;
import pivot.wtk.Bounds;

/**
 * Decorator that applies an opacity to a component.
 *
 * @author gbrown
 */
public class FadeDecorator implements Decorator {
    private float opacity;

    public FadeDecorator() {
        this(0.5f);
    }

    public FadeDecorator(float opacity) {
        this.opacity = opacity;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        if (opacity < 0f
            || opacity > 1f) {
            throw new IllegalArgumentException("opacity must be a value between 0 and 1, inclusive.");
        }

        this.opacity = opacity;
    }

    public Graphics2D prepare(Component component, Graphics2D graphics) {
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        return graphics;
    }

    public void update() {
        // No-op
    }

    public Bounds getAffectedArea(Component component, int x, int y, int width, int height) {
        return new Bounds(x, y, width, height);
    }
}
