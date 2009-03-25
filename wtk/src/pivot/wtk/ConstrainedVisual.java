package pivot.wtk;

/**
 * Interface representing a visual that is used in layout.
 *
 * @author gbrown
 */
public interface ConstrainedVisual extends Visual {
    /**
     * Sets the visual's render size.
     *
     * @param width
     * @param height
     */
    public void setSize(int width, int height);

    /**
     * Returns the visual's preferred width given the provided height
     * constraint.
     *
     * @param height
     * The height by which to constrain the preferred width, or <tt>-1</tt>
     * for no constraint.
     */
    public int getPreferredWidth(int height);

    /**
     * Returns the visual's preferred height given the provided width
     * constraint.
     *
     * @param width
     * The width by which to constrain the preferred height, or <tt>-1</tt>
     * for no constraint.
     */
    public int getPreferredHeight(int width);

    /**
     * Returns the visual's unconstrained preferred size.
     */
    public Dimensions getPreferredSize();
}
