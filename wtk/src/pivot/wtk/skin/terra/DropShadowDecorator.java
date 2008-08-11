package pivot.wtk.skin.terra;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

import pivot.wtk.Component;
import pivot.wtk.ComponentListener;
import pivot.wtk.Container;
import pivot.wtk.Cursor;
import pivot.wtk.Decorator;
import pivot.wtk.Display;
import pivot.wtk.Skin;

/**
 * Adds drop shadows to components.
 *
 * @author gbrown
 * @author tvolkert
 */
public class DropShadowDecorator implements Decorator {
    private static class ComponentHandler implements ComponentListener {
        public void skinClassChanged(Component component, Class<? extends Skin> previousSkinClass) {
            // No-op
        }

        public void parentChanged(Component component, Container previousParent) {
            visibleChanged(component);
        }

        public void sizeChanged(Component component, int previousWidth, int previousHeight) {
            Display display = Display.getInstance();

            int x = component.getX();
            int y = component.getY();

            display.repaint(x + DROP_SHADOW_OFFSET, y + DROP_SHADOW_OFFSET,
                previousWidth, previousHeight);

            display.repaint(x + DROP_SHADOW_OFFSET, y + DROP_SHADOW_OFFSET,
                component.getWidth(), component.getHeight());
        }

        public void locationChanged(Component component, int previousX, int previousY) {
            Display display = Display.getInstance();

            int width = component.getWidth();
            int height = component.getHeight();

            display.repaint(previousX + DROP_SHADOW_OFFSET,
                previousY + DROP_SHADOW_OFFSET,
                width, height);

            display.repaint(component.getX() + DROP_SHADOW_OFFSET,
                component.getY() + DROP_SHADOW_OFFSET,
                width, height);
        }

        public void visibleChanged(Component component) {
            Display.getInstance().repaint(component.getX() + DROP_SHADOW_OFFSET,
                component.getY() + DROP_SHADOW_OFFSET,
                component.getWidth(), component.getHeight());
        }

        public void styleUpdated(Component component, String styleKey, Object previousValue) {
            // No-op
        }

        public void cursorChanged(Component component, Cursor previousCursor) {
            // No-op
        }

        public void tooltipTextChanged(Component component, String previousTooltipText) {
            // No-op
        }
    }

    private Component component = null;
    private ComponentHandler componentHandler = new ComponentHandler();

    public static final Color DROP_SHADOW_COLOR = Color.BLACK;
    public static final float DROP_SHADOW_OPACITY = 0.33f;
    public static final int DROP_SHADOW_OFFSET = 4;

    public void install(Component component) {
        this.component = component;
        component.getComponentListeners().add(componentHandler);
    }

    public void uninstall() {
        component.getComponentListeners().remove(componentHandler);
    }

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
}
