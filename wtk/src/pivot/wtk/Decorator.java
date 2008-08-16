package pivot.wtk;

public interface Decorator extends Visual {
    /**
     * Prepare to decorate a visual. This method may be called during layout,
     * so it should execute quickly.
     *
     * @param visual
     * The visual to decorate.
     */
    public void prepare(Visual visual);

    /**
     * Returns the area of the decorator that would be affected by a change
     * to a given area in the source visual.
     *
     * @param bounds
     * The bounding area in the source visual.
     *
     * @return
     * The affected area in the decorator.
     */
    public Rectangle getDirtyRegion(Rectangle bounds);
}
