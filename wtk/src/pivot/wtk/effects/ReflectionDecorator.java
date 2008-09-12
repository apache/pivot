package pivot.wtk.effects;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import pivot.wtk.Component;
import pivot.wtk.Decorator;
import pivot.wtk.Bounds;

/**
 * <p>Decorator that paints a reflection of a component.</p>
 *
 * <p>TODO Make gradient properties configurable.</p>
 *
 * @author gbrown
 */
public class ReflectionDecorator implements Decorator {
    private Graphics2D graphics = null;

    private BufferedImage bufferedImage = null;
    private Graphics2D bufferedImageGraphics = null;

    public Graphics2D prepare(Component component, Graphics2D graphics) {
        this.graphics = graphics;

        int width = component.getWidth();
        int height = component.getHeight();

        if (bufferedImage == null
            || bufferedImage.getWidth() < width
            || bufferedImage.getHeight() < height) {
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        bufferedImageGraphics = bufferedImage.createGraphics();
        bufferedImageGraphics.setClip(graphics.getClip());

        // Clear the image background
        bufferedImageGraphics.setComposite(AlphaComposite.Clear);
        bufferedImageGraphics.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        bufferedImageGraphics.setComposite(AlphaComposite.SrcOver);

        return bufferedImageGraphics;
    }

    public void update() {
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

        bufferedImageGraphics.dispose();
        bufferedImage.flush();

        Graphics2D reflectionGraphics = (Graphics2D)graphics.create();
        reflectionGraphics.scale(1.0, -1.0);
        reflectionGraphics.translate(0, -(height * 2));

        reflectionGraphics.setClip(null);
        reflectionGraphics.drawImage(bufferedImage, 0, 0, null);

        // Dispose of the graphics
        reflectionGraphics.dispose();
    }

    public Bounds getAffectedArea(Component component, int x, int y, int width, int height) {
        return new Bounds(x + component.getX(),
            (component.getHeight() * 2) - (y + height) + component.getY(),
            width, height);
    }
}
