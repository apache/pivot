package pivot.wtk.skin.terra;

import pivot.wtk.Component;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.Dimensions;
import pivot.wtk.Mouse;
import pivot.wtk.Panorama;
import pivot.wtk.Rectangle;
import pivot.wtk.Viewport;
import pivot.wtk.ViewportListener;
import pivot.wtk.skin.ContainerSkin;

public class PanoramaSkin extends ContainerSkin
    implements Viewport.Skin, ViewportListener,
    ComponentMouseListener, ComponentMouseButtonListener {
    @Override
    public void install(Component component) {
        validateComponentType(component, Panorama.class);

        super.install(component);

        // TODO Add scroll arrow link buttons and attach mouse listeners
        // to them; the mouse handlers should call setScrollTop() and
        // setScrollLeft() on the panorama as appropriate
    }

    @Override
    public void uninstall() {
        // TODO Remove scroll arrow link buttons
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

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

        Panorama panorama = (Panorama)getComponent();
        Component view = panorama.getView();
        if (view != null) {
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
        return new Rectangle(0, 0, getWidth(), getHeight());
    }

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
