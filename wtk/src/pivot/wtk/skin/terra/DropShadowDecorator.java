package pivot.wtk.skin.terra;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import pivot.wtk.Rectangle;
import pivot.wtk.effects.AbstractDecorator;

/**
 * Adds drop shadows to components.
 *
 * @author gbrown
 * @author tvolkert
 */
public class DropShadowDecorator extends AbstractDecorator {
    public static final Color DROP_SHADOW_COLOR = Color.BLACK;
    public static final float DROP_SHADOW_OPACITY = 0.33f;
    public static final int DROP_SHADOW_OFFSET = 4;

    public Rectangle getDirtyRegion(Rectangle bounds) {
        // TODO Bound to visual width and height plus shadow offset?

        bounds.width += DROP_SHADOW_OFFSET;
        bounds.height += DROP_SHADOW_OFFSET;

        return bounds;
    }

    public void paint(Graphics2D graphics) {
        // Paint the drop shadow
        Graphics2D shadowGraphics = (Graphics2D)graphics.create();
        Shape clip = shadowGraphics.getClip();

        if (clip != null) {
            Rectangle2D clipBounds = clip.getBounds();
            clipBounds.setFrame(clipBounds.getX() + DROP_SHADOW_OFFSET,
                clipBounds.getY() + DROP_SHADOW_OFFSET,
                clipBounds.getWidth(),
                clipBounds.getHeight());
            shadowGraphics.clip(clipBounds);
        }

        shadowGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
            DROP_SHADOW_OPACITY));
        shadowGraphics.setColor(DROP_SHADOW_COLOR);

        shadowGraphics.fillRect(DROP_SHADOW_OFFSET, DROP_SHADOW_OFFSET,
            visual.getWidth(), visual.getHeight());

        shadowGraphics.dispose();

        // Paint the visual
        visual.paint(graphics);
    }
}

