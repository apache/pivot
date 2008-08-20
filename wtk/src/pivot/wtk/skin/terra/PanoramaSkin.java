package pivot.wtk.skin.terra;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

import pivot.wtk.Component;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.Dimensions;
import pivot.wtk.LinkButton;
import pivot.wtk.Mouse;
import pivot.wtk.Panorama;
import pivot.wtk.Rectangle;
import pivot.wtk.Viewport;
import pivot.wtk.ViewportListener;
import pivot.wtk.skin.ContainerSkin;

public class PanoramaSkin extends ContainerSkin
    implements Viewport.Skin, ViewportListener,
    ComponentMouseListener, ComponentMouseButtonListener {
    /**
     * Abstract base class for button images.
     */
    protected abstract class ButtonImage extends ImageAsset {
        public int getPreferredWidth(int height) {
            return 7;
        }

        public int getPreferredHeight(int width) {
            return 7;
        }

        public Dimensions getPreferredSize() {
            return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
        }
    }

    /**
     * North button image.
     */
    protected class NorthImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            graphics.setStroke(new BasicStroke(0));
            graphics.setPaint(arrowColor);

            GeneralPath shape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
            shape.moveTo(0, 6);
            shape.lineTo(3, 3);
            shape.lineTo(6, 6);
            shape.closePath();

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.draw(shape);
            graphics.fill(shape);
        }
    }

    // TODO More button images

    private Color arrowColor = Color.BLACK;

    private LinkButton northButton = new LinkButton(new NorthImage());
    // TODO Remaining buttons

    @Override
    public void install(Component component) {
        validateComponentType(component, Panorama.class);

        super.install(component);

        Panorama panorama = (Panorama)component;
        panorama.getViewportListeners().add(this);

        // TODO Add scroll arrow link buttons and attach mouse listeners
        // to them; the mouse handlers should call setScrollTop() and
        // setScrollLeft() on the panorama as appropriate
        panorama.add(northButton);
    }

    @Override
    public void uninstall() {
        Panorama panorama = (Panorama)getComponent();
        panorama.getViewportListeners().remove(this);

        // TODO Remove scroll arrow link buttons
        panorama.remove(northButton);
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        // The panorama's preferred width is the preferred width of the view
        Panorama panorama = (Panorama)getComponent();
        Component view = panorama.getView();
        if (view != null) {
            preferredWidth = view.getPreferredWidth(height);
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        // The panorama's preferred height is the preferred height of the view
        Panorama panorama = (Panorama)getComponent();
        Component view = panorama.getView();
        if (view != null) {
            preferredHeight = view.getPreferredHeight(width);
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        Dimensions preferredSize = null;

        // The panorama's preferred size is the preferred size of the view
        Panorama panorama = (Panorama)getComponent();
        Component view = panorama.getView();
        if (view == null) {
            preferredSize = new Dimensions(0, 0);
        } else {
            preferredSize = view.getPreferredSize();
        }

        return preferredSize;
    }

    public void layout() {
        Panorama panorama = (Panorama)getComponent();
        Component view = panorama.getView();

        // TODO Reposition the view so it is consistent with the values of
        // scroll top and scroll left within the current panorama bounds

        // TODO Center N, S, E, and W buttons on their respective edges
    }

    public Rectangle getViewportBounds() {
        // The viewport bounds is simply the skin's bounding rectangle
        return new Rectangle(0, 0, getWidth(), getHeight());
    }

    // Styles
    public Color getArrowColor() {
        return arrowColor;
    }

    public void setArrowColor(Color arrowColor) {
        if (arrowColor == null) {
            throw new IllegalArgumentException("arrowColor is null.");
        }

        this.arrowColor = arrowColor;
        repaintComponent();
    }

    public final void setArrowColor(String arrowColor) {
        if (arrowColor == null) {
            throw new IllegalArgumentException("arrowColor is null.");
        }

        setArrowColor(Color.decode(arrowColor));
    }

    // User input
    @Override
    public boolean mouseMove(int x, int y) {
        // TODO
        return super.mouseMove(x, y);
    }

    @Override
    public void mouseOver() {
        super.mouseOver();
        // TODO
    }

    @Override
    public void mouseOut() {
        super.mouseOut();
        // TODO
    }

    @Override
    public boolean mouseDown(Mouse.Button button, int x, int y) {
        // TODO
        return super.mouseDown(button, x, y);
    }

    @Override
    public boolean mouseUp(Mouse.Button button, int x, int y) {
        // TODO
        return super.mouseUp(button, x, y);
    }

    public void mouseClick(Mouse.Button button, int x, int y, int count) {
        super.mouseClick(button, x, y, count);
        // TODO
    }

    public boolean mouseWheel(Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        // TODO
        return super.mouseWheel(scrollType, scrollAmount, wheelRotation, x, y);
    }

    // Viewport events
    public void scrollTopChanged(Viewport scrollPane, int previousScrollTop) {
        repaintComponent();
    }

    public void scrollLeftChanged(Viewport scrollPane, int previousScrollLeft) {
        repaintComponent();
    }

    public void viewChanged(Viewport scrollPane, Component previousView) {
        invalidateComponent();
    }

    // Component mouse events
    public void mouseMove(Component component, int x, int y) {
        // TODO
    }

    public void mouseOver(Component component) {
        // TODO
    }

    public void mouseOut(Component component) {
        // TODO
    }

    // Component mouse button events
    public void mouseDown(Component component, Mouse.Button button, int x, int y) {
        // TODO
    }

    public void mouseUp(Component component, Mouse.Button button, int x, int y) {
        // TODO
    }

    public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        // TODO
    }
}
