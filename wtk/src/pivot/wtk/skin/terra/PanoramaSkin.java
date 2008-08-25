package pivot.wtk.skin.terra;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.Dimensions;
import pivot.wtk.LinkButton;
import pivot.wtk.Panorama;
import pivot.wtk.Rectangle;
import pivot.wtk.Viewport;
import pivot.wtk.ViewportListener;
import pivot.wtk.skin.ContainerSkin;

public class PanoramaSkin extends ContainerSkin
    implements Viewport.Skin, ViewportListener, ComponentMouseListener {
    /**
     * Abstract base class for button images.
     */
    protected abstract class ScrollButtonImage extends ImageAsset {
        public int getPreferredWidth(int height) {
            return BUTTON_SIZE;
        }

        public int getPreferredHeight(int width) {
            return BUTTON_SIZE;
        }

        public Dimensions getPreferredSize() {
            return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
        }

        public void paint(Graphics2D graphics) {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.setStroke(new BasicStroke(0));
            graphics.setPaint(BUTTON_COLOR);
        }
    }

    /**
     * North button image.
     */
    protected class NorthButtonImage extends ScrollButtonImage {
        public void paint(Graphics2D graphics) {
            super.paint(graphics);

            int[] xPoints = {3, 6, 9};
            int[] yPoints = {9, 5, 9};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    /**
     * South button image.
     */
    protected class SouthButtonImage extends ScrollButtonImage {
        public void paint(Graphics2D graphics) {
            super.paint(graphics);

            int[] xPoints = {3, 6, 9};
            int[] yPoints = {5, 9, 5};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    /**
     * East button image.
     */
    protected class EastButtonImage extends ScrollButtonImage {
        public void paint(Graphics2D graphics) {
            super.paint(graphics);

            int[] xPoints = {5, 9, 5};
            int[] yPoints = {3, 6, 9};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    /**
     * West button image.
     */
    protected class WestButtonImage extends ScrollButtonImage {
        public void paint(Graphics2D graphics) {
            super.paint(graphics);

            int[] xPoints = {9, 5, 9};
            int[] yPoints = {3, 6, 9};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    private class ScrollCallback implements Runnable {
        public void run() {
            Panorama panorama = (Panorama)getComponent();

            if (northButton.isMouseOver()) {
                int scrollTop = Math.max(panorama.getScrollTop()
                    - (int)scrollDistance, 0);
                if (scrollTop == 0) {
                    ApplicationContext.clearInterval(scrollIntervalID);
                }

                panorama.setScrollTop(scrollTop);
            } else if (southButton.isMouseOver()) {
                int maxScrollTop = getMaxScrollTop();
                int scrollTop = Math.min(panorama.getScrollTop()
                    + (int)scrollDistance, maxScrollTop);
                if (scrollTop == maxScrollTop) {
                    ApplicationContext.clearInterval(scrollIntervalID);
                }

                panorama.setScrollTop(scrollTop);
            } else if (eastButton.isMouseOver()) {
                int maxScrollLeft = getMaxScrollLeft();
                int scrollLeft = Math.min(panorama.getScrollLeft()
                    + (int)scrollDistance, maxScrollLeft);
                if (scrollLeft == maxScrollLeft) {
                    ApplicationContext.clearInterval(scrollIntervalID);
                }

                panorama.setScrollLeft(scrollLeft);
            } else if (westButton.isMouseOver()) {
                int scrollLeft = Math.max(panorama.getScrollLeft()
                    - (int)scrollDistance, 0);
                if (scrollLeft == 0) {
                    ApplicationContext.clearInterval(scrollIntervalID);
                }

                panorama.setScrollLeft(scrollLeft);
            }

            scrollDistance = Math.min(scrollDistance * SCROLL_ACCELERATION,
                MAXIMUM_SCROLL_DISTANCE);
        }
    }

    private Color BUTTON_COLOR = Color.BLACK;

    private LinkButton northButton = new LinkButton(new NorthButtonImage());
    private LinkButton southButton = new LinkButton(new SouthButtonImage());
    private LinkButton eastButton = new LinkButton(new EastButtonImage());
    private LinkButton westButton = new LinkButton(new WestButtonImage());

    private float scrollDistance = 0;
    private ScrollCallback scrollCallback = new ScrollCallback();
    private int scrollIntervalID = -1;

    private static final int SCROLL_RATE = 50;
    private static final float INITIAL_SCROLL_DISTANCE = 10;
    private static final float SCROLL_ACCELERATION = 1.06f;
    private static final float MAXIMUM_SCROLL_DISTANCE = 150f;

    private static final int BUTTON_SIZE = 13;

    @Override
    public void install(Component component) {
        validateComponentType(component, Panorama.class);

        super.install(component);

        Panorama panorama = (Panorama)component;
        panorama.getViewportListeners().add(this);

        // Add scroll arrow link buttons and attach mouse listeners
        // to them; the mouse handlers should call setScrollTop() and
        // setScrollLeft() on the panorama as appropriate
        panorama.add(northButton);
        northButton.getComponentMouseListeners().add(this);

        panorama.add(southButton);
        southButton.getComponentMouseListeners().add(this);

        panorama.add(eastButton);
        eastButton.getComponentMouseListeners().add(this);

        panorama.add(westButton);
        westButton.getComponentMouseListeners().add(this);

        updateScrollButtonVisibility();
    }

    @Override
    public void uninstall() {
        Panorama panorama = (Panorama)getComponent();
        panorama.getViewportListeners().remove(this);

        // Remove scroll arrow link buttons
        panorama.remove(northButton);
        panorama.remove(southButton);
        panorama.remove(eastButton);
        panorama.remove(westButton);
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
        int width = getWidth();
        int height = getHeight();

        Component view = panorama.getView();
        if (view != null) {
            view.setSize(view.getPreferredSize());
            int viewWidth = view.getWidth();
            int viewHeight = view.getHeight();

            int scrollTop = panorama.getScrollTop();
            int maxScrollTop = getMaxScrollTop();
            if (scrollTop > maxScrollTop) {
                panorama.setScrollTop(maxScrollTop);
                scrollTop = maxScrollTop;
            }

            int scrollLeft = panorama.getScrollLeft();
            int maxScrollLeft = getMaxScrollLeft();
            if (scrollLeft > maxScrollLeft) {
                panorama.setScrollLeft(maxScrollLeft);
                scrollLeft = maxScrollLeft;
            }

            if (width < viewWidth) {
                // Show east/west buttons
                eastButton.setSize(BUTTON_SIZE, height);
                eastButton.setLocation(width - eastButton.getWidth(), 0);

                westButton.setSize(BUTTON_SIZE, height);
                westButton.setLocation(0, 0);
            }

            if (height < viewHeight) {
                // Show north/south buttons
                northButton.setSize(width, BUTTON_SIZE);
                northButton.setLocation(0, 0);

                southButton.setSize(width, BUTTON_SIZE);
                southButton.setLocation(0, height - southButton.getHeight());
            }
        }
    }

    public Rectangle getViewportBounds() {
        // The viewport bounds is simply the skin's bounding rectangle
        return new Rectangle(0, 0, getWidth(), getHeight());
    }

    public int getMaxScrollTop() {
        int maxScrollTop = 0;

        Panorama panorama = (Panorama)getComponent();
        int height = getHeight();

        Component view = panorama.getView();
        if (view != null) {
            maxScrollTop = Math.max(view.getHeight() - height, 0);
        }

        return maxScrollTop;
    }

    public int getMaxScrollLeft() {
        int maxScrollLeft = 0;

        Panorama panorama = (Panorama)getComponent();
        int width = getWidth();

        Component view = panorama.getView();
        if (view != null) {
            maxScrollLeft = Math.max(view.getWidth() - width, 0);
        }

        return maxScrollLeft;
    }

    protected void updateScrollButtonVisibility() {
        Panorama panorama = (Panorama)getComponent();
        boolean mouseOver = panorama.isMouseOver();

        int scrollTop = panorama.getScrollTop();
        int maxScrollTop = getMaxScrollTop();
        northButton.setVisible(mouseOver
            && scrollTop > 0);
        southButton.setVisible(mouseOver
            && scrollTop < maxScrollTop);

        int scrollLeft = panorama.getScrollLeft();
        int maxScrollLeft = getMaxScrollLeft();
        westButton.setVisible(mouseOver
            && scrollLeft > 0);
        eastButton.setVisible(mouseOver
            && scrollLeft < maxScrollLeft);
    }

    // User input
    @Override
    public void mouseOver() {
        super.mouseOver();
        updateScrollButtonVisibility();
    }

    @Override
    public void mouseOut() {
        super.mouseOut();
        updateScrollButtonVisibility();
    }

    // Viewport events
    public void scrollTopChanged(Viewport panorama, int previousScrollTop) {
        Component view = panorama.getView();
        if (view != null) {
            int maxScrollTop = getMaxScrollTop();
            int scrollTop = Math.min(panorama.getScrollTop(), maxScrollTop);
            view.setLocation(view.getX(), -scrollTop);
            updateScrollButtonVisibility();
        }
    }

    public void scrollLeftChanged(Viewport panorama, int previousScrollLeft) {
        Component view = panorama.getView();
        if (view != null) {
            int maxScrollLeft = getMaxScrollLeft();
            int scrollLeft = Math.min(panorama.getScrollLeft(), maxScrollLeft);
            view.setLocation(-scrollLeft, view.getY());
            updateScrollButtonVisibility();
        }
    }

    public void viewChanged(Viewport scrollPane, Component previousView) {
        invalidateComponent();
    }

    // Component mouse events
    public void mouseMove(Component component, int x, int y) {
        // No-op
    }

    public void mouseOver(Component component) {
        // Start scroll timer
        scrollDistance = INITIAL_SCROLL_DISTANCE;
        scrollIntervalID = ApplicationContext.setInterval(scrollCallback, SCROLL_RATE);
    }

    public void mouseOut(Component component) {
        // Stop scroll timer
        ApplicationContext.clearInterval(scrollIntervalID);
    }
}
