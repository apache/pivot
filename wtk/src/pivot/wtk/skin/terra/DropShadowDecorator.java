package pivot.wtk.skin.terra;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

import pivot.collections.Sequence;
import pivot.wtk.Component;
import pivot.wtk.ComponentListener;
import pivot.wtk.Container;
import pivot.wtk.Cursor;
import pivot.wtk.Decorator;
import pivot.wtk.Display;
import pivot.wtk.DragHandler;
import pivot.wtk.DropHandler;
import pivot.wtk.Skin;
import pivot.wtk.Window;

/**
 * Adds drop shadows to windows.
 *
 * @author gbrown
 * @author tvolkert
 */
public class DropShadowDecorator implements Decorator {
    private class WindowHandler implements ComponentListener {
        public void skinClassChanged(Component component, Class<? extends Skin> previousSkinClass) {
        }

        public void decoratorInserted(Component component, int index) {
        }

        public void decoratorsRemoved(Component component, int index,
            Sequence<Decorator> removed) {
        }

        public void parentChanged(Component component, Container previousParent) {
            visibleChanged(component);
        }

        public void sizeChanged(Component component, int previousWidth, int previousHeight) {
            Display display = Display.getInstance();

            int x = window.getX();
            int y = window.getY();

            display.repaint(x + DROP_SHADOW_OFFSET, y + DROP_SHADOW_OFFSET,
                previousWidth, previousHeight);

            display.repaint(x + DROP_SHADOW_OFFSET, y + DROP_SHADOW_OFFSET,
                window.getWidth(), window.getHeight());
        }

        public void locationChanged(Component component, int previousX, int previousY) {
            Display display = Display.getInstance();

            int width = window.getWidth();
            int height = window.getHeight();

            display.repaint(previousX + DROP_SHADOW_OFFSET,
                previousY + DROP_SHADOW_OFFSET,
                width, height);

            display.repaint(window.getX() + DROP_SHADOW_OFFSET,
                window.getY() + DROP_SHADOW_OFFSET,
                width, height);
        }

        public void visibleChanged(Component component) {
            Display.getInstance().repaint(window.getX() + DROP_SHADOW_OFFSET,
                window.getY() + DROP_SHADOW_OFFSET,
                window.getWidth(), window.getHeight());
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

        public void dragHandlerChanged(Component component, DragHandler previousDragHandler) {
            // No-op
        }

        public void dropHandlerChanged(Component component, DropHandler previousDropHandler) {
            // No-op
        }
    }

    private WindowHandler windowHandler = new WindowHandler();

    public static final Color DROP_SHADOW_COLOR = Color.BLACK;
    public static final float DROP_SHADOW_OPACITY = 0.33f;
    public static final int DROP_SHADOW_OFFSET = 4;

    private Window window = null;

    public DropShadowDecorator(Window window) {
        window.getComponentListeners().add(windowHandler);

        this.window = window;
    }

    public void dispose() {
        window.getComponentListeners().remove(windowHandler);
        window = null;
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
