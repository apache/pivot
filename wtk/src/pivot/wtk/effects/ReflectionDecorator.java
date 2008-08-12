package pivot.wtk.effects;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import pivot.wtk.Component;
import pivot.wtk.ComponentListener;
import pivot.wtk.Container;
import pivot.wtk.Cursor;
import pivot.wtk.Decorator;
import pivot.wtk.Display;
import pivot.wtk.Point;
import pivot.wtk.Rectangle;
import pivot.wtk.Skin;

/**
 * TODO Make gradient properties configurable.
 */
public class ReflectionDecorator implements Decorator {
    private class ComponentHandler implements ComponentListener {
        public void skinClassChanged(Component component, Class<? extends Skin> previousSkinClass) {
            // No-op
        }

        public void parentChanged(Component component, Container previousParent) {
            visibleChanged(component);
        }

        public void sizeChanged(Component component, int previousWidth, int previousHeight) {
            Display display = Display.getInstance();
            Point origin = component.mapPointToAncestor(display, 0, 0);

            display.repaint(origin.x, origin.y + previousHeight, previousWidth, previousHeight);

            recreateBuffer();
        }

        public void locationChanged(Component component, int previousX, int previousY) {
            Container parent = component.getParent();
            Display display = Display.getInstance();
            Point previousOrigin = parent.mapPointToAncestor(display, previousX, previousY);

            int width = component.getWidth();
            int height = component.getHeight();
            display.repaint(previousOrigin.x, previousOrigin.y + height, width, height);
        }

        public void visibleChanged(Component component) {
            Rectangle bounds = component.getBounds();
            Display display = Display.getInstance();
            display.repaint(bounds.x, bounds.y + bounds.height, bounds.width, bounds.height);
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

    private BufferedImage bufferedImage = null;

    private Graphics2D graphics = null;
    private Graphics2D bufferedImageGraphics = null;

    public void install(Component component) {
        this.component = component;
        component.getComponentListeners().add(componentHandler);

        recreateBuffer();
    }

    public void uninstall() {
        component.getComponentListeners().remove(componentHandler);
        component = null;
    }

    public Graphics2D prepare(Component component, Graphics2D graphics) {
        this.graphics = graphics;

        bufferedImageGraphics = bufferedImage.createGraphics();
        bufferedImageGraphics.setClip(graphics.getClip());

        // Clear the image background
        bufferedImageGraphics.setComposite(AlphaComposite.Clear);
        bufferedImageGraphics.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        bufferedImageGraphics.setComposite(AlphaComposite.SrcOver);

        return bufferedImageGraphics;
    }

    public void update() {
        bufferedImage.flush();

        // Draw the component
        graphics.drawImage(bufferedImage, 0, 0, null);

        // Draw the reflection
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        GradientPaint mask = new GradientPaint(0, height / 4, new Color(1.0f, 1.0f, 1.0f, 0.0f),
            0, height, new Color(1.0f, 1.0f, 1.0f, 0.5f));
        bufferedImageGraphics.setPaint(mask);

        bufferedImageGraphics.setComposite(AlphaComposite.DstIn);
        bufferedImageGraphics.fillRect(0, 0, width, height);

        Graphics2D reflectionGraphics = (Graphics2D)graphics.create();
        reflectionGraphics.scale(1.0, -1.0);
        reflectionGraphics.translate(0, -(height * 2));
        reflectionGraphics.setClip(graphics.getClip());

        reflectionGraphics.drawImage(bufferedImage, 0, 0, null);

        // Dispose of the graphics
        reflectionGraphics.dispose();
        bufferedImageGraphics.dispose();
    }

    private void recreateBuffer() {
        int width = component.getWidth();
        int height = component.getHeight();

        if (width > 0
            && height > 0) {
            bufferedImage = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
        } else {
            bufferedImage = null;
        }
    }
}
