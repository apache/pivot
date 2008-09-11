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
     * Returns the bounds of the area affected by a change to a given region
     * within a component.
     *
     * @param component
     * @param x
     * @param y
     * @param width
     * @param height
     *
     * @return
     * The bounds of the affected area, relative to the component's
     * origin. The bounds may exceed the actual bounds of the component.
     */
    public Bounds getAffectedArea(Component component, int x, int y, int width, int height);
}
