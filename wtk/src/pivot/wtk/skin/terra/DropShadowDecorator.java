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
 * @author gbrown
 * @author tvolkert
 */
public class DropShadowDecorator implements Decorator {
    public static final Color DROP_SHADOW_COLOR = Color.BLACK;
    public static final float DROP_SHADOW_OPACITY = 0.33f;
    public static final int DROP_SHADOW_OFFSET = 4;

    public Graphics2D prepare(Component component, Graphics2D graphics) {
        Graphics2D shadowGraphics = (Graphics2D)graphics.create();
        shadowGraphics.setClip(null);

        shadowGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
            DROP_SHADOW_OPACITY));
        shadowGraphics.setColor(DROP_SHADOW_COLOR);
        shadowGraphics.fillRect(DROP_SHADOW_OFFSET, DROP_SHADOW_OFFSET,
            component.getWidth(), component.getHeight());

        shadowGraphics.dispose();

        return graphics;
    }

    public void update() {
        // No-op
    }

    public Rectangle getDirtyRegion(Component component, int x, int y, int width, int height) {
        return new Rectangle(x + DROP_SHADOW_OFFSET, y + DROP_SHADOW_OFFSET,
            width, height);
    }
}

