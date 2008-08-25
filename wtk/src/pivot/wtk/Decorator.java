package pivot.wtk;

import java.awt.Graphics2D;

public interface Decorator {
    /**
     * Prepares the graphics context into which the component or prior
     * decorator will paint. This method is called immediately prior to
     * {@link Component#paint(Graphics2D)}; decorators are called in
     * descending order.
     *
     * @param component
     * @param graphics
     *
     * @return
     * The graphics context that should be used by the component or prior
     * decorators.
     */
    public Graphics2D prepare(Component component, Graphics2D graphics);

    /**
     * Updates the graphics context into which the component or prior
     * decorator was painted. This method is called immediately after
     * {@link Component#paint(Graphics2D)}; decorators are called in
     * ascending order.
     */
    public void update();

    /**
     * Returns the decorator's bounding rectangle.
     *
     * @param component
     * @return
     * The bounds of the decorator relative to the component's origin.
     */
    public Rectangle getBounds(Component component);

    /**
     * Notifies the decorator that an area in the component has been repainted.
     *
     * @param component
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void repaint(Component component, int x, int y, int width, int height);
}
