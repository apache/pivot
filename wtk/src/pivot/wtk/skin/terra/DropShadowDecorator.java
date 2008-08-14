package pivot.wtk.skin.terra;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

import pivot.wtk.Component;
import pivot.wtk.Decorator;
import pivot.wtk.Rectangle;

/**
 * Adds drop shadows to components.
 *
 * TODO Currently only works for Windows - need to convert component coordinates
 * to display coordinates for it to work with other components.
 *
 * @author gbrown
 * @author tvolkert
 */
public class DropShadowDecorator implements Decorator {
    public static final Color DROP_SHADOW_COLOR = Color.BLACK;
    public static final float DROP_SHADOW_OPACITY = 0.33f;
    public static final int DROP_SHADOW_OFFSET = 4;

    public Graphics2D prepare(Component component, Graphics2D graphics) {
        // Paint the drop shadow
        Graphics2D shadowGraphics = (Graphics2D)graphics.create();
        shadowGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
            DROP_SHADOW_OPACITY));
        shadowGraphics.setColor(DROP_SHADOW_COLOR);

        shadowGraphics.setClip(null);
        shadowGraphics.fillRect(DROP_SHADOW_OFFSET, DROP_SHADOW_OFFSET,
            component.getWidth(), component.getHeight());

        shadowGraphics.dispose();

        return graphics;
    }

    public void update() {
        // No-op
    }

    public Rectangle transform(Component component, Rectangle bounds) {
        bounds.x += DROP_SHADOW_OFFSET;
        bounds.y += DROP_SHADOW_OFFSET;

        return bounds;
    }
}

