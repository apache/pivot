package pivot.wtk.effects;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import pivot.wtk.Decorator;
import pivot.wtk.Dimensions;
import pivot.wtk.Point;
import pivot.wtk.Rectangle;
import pivot.wtk.Visual;

/**
 * TODO Make gradient properties configurable.
 */
public class ReflectionDecorator implements Decorator {
    private Visual visual = null;
    private BufferedImage bufferedImage = null;

    private float heightRatio = 0.25f;

    public void prepare(Visual visual) {
        this.visual = visual;
    }

    public Rectangle getDirtyRegion(Rectangle bounds) {
        // TODO This should be the entire dirty region, not just the
        // reflected dirty region; also, don't return a negative height

        int height = visual.getHeight();
        bounds.y = (height * 2) - (bounds.y + bounds.height);

        return bounds;
    }

    public Point mapPointToVisual(int x, int y) {
        // TODO If the point is outside the bounds of the source visual,
        // return null

        return new Point(x, y);
    }

    public int getWidth() {
        return visual.getWidth();
    }

    public int getHeight() {
        int height = visual.getHeight();
        return (int)((float)height * (2.0f - heightRatio));
    }

    public void setSize(int width, int height) {
        // TODO The height of the visual should not shrink, otherwise
        // we won't be aspect-correct; how do we ensure this? Keep track of
        // the visual's preferred height?
        visual.setSize(width, (int)((float)height * (1.0f - heightRatio)));
    }

    public int getPreferredWidth(int height) {
        return visual.getPreferredWidth(height);
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = visual.getPreferredHeight(width);
        return (int)((float)preferredHeight * (2.0f - heightRatio));
    }

    public Dimensions getPreferredSize() {
        // TODO
        return null;
    }

    public void paint(Graphics2D graphics) {
        int width = visual.getWidth();
        int height = visual.getHeight();

        if (bufferedImage == null
            || bufferedImage.getWidth() != width
            || bufferedImage.getHeight() != height) {
            bufferedImage = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
        }

        Graphics2D bufferedImageGraphics = (Graphics2D)bufferedImage.createGraphics();
        bufferedImageGraphics.setClip(graphics.getClip());

        // Clear the image background
        bufferedImageGraphics.setComposite(AlphaComposite.Clear);
        bufferedImageGraphics.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        // Paint the visual to the buffer
        bufferedImageGraphics.setComposite(AlphaComposite.SrcOver);
        visual.paint(bufferedImageGraphics);

        bufferedImage.flush();

        // Draw the buffered image to the source graphics
        graphics.drawImage(bufferedImage, 0, 0, null);

        // Draw the reflection to the source graphics
        GradientPaint mask = new GradientPaint(0, (float)height * heightRatio,
            new Color(1.0f, 1.0f, 1.0f, 0.0f),
            0, height,
            new Color(1.0f, 1.0f, 1.0f, 0.5f));
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
}
