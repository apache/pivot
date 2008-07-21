/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk.skin.terra;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.ComponentAttributeListener;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Container;
import pivot.wtk.Cursor;
import pivot.wtk.Dimensions;
import pivot.wtk.Display;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.ImageView;
import pivot.wtk.Insets;
import pivot.wtk.Label;
import pivot.wtk.Mouse;
import pivot.wtk.Point;
import pivot.wtk.PushButton;
import pivot.wtk.Rectangle;
import pivot.wtk.TitlePane;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.Window;
import pivot.wtk.media.Image;
import pivot.wtk.skin.WindowSkin;

/**
 * TODO Don't display a resize handle if maximized (or set to default size)?
 *
 * @author gbrown
 */
public abstract class AbstractFrameSkin extends WindowSkin
    implements ButtonPressListener, ComponentAttributeListener {
    public static class FrameButton extends PushButton {
        private Window window = null;

        public FrameButton(Window window) {
            this(window, null);
        }

        public FrameButton(Window window, Object buttonData) {
            super(buttonData);

            this.window = window;

            installSkin(FrameButton.class);
        }

        public Window getWindow() {
            return window;
        }
    }

    public static class FrameButtonSkin extends PushButtonSkin {
        public void install(Component component) {
            validateComponentType(component, FrameButton.class);

            super.install(component);
        }

        public void paint(Graphics2D graphics) {
            // Apply frame styles to the button
            FrameButton frameButton = (FrameButton)getComponent();
            Window window = frameButton.getWindow();

            Component.StyleDictionary windowStyles = window.getStyles();

            if (window.isActive()) {
                color = (Color)windowStyles.get("titleBarColor");
                backgroundColor = (Color)windowStyles.get("titleBarBackgroundColor");
                bevelColor = (Color)windowStyles.get("titleBarBevelColor");
                pressedBevelColor = (Color)windowStyles.get("titleBarPressedBevelColor");
                borderColor = (Color)windowStyles.get("titleBarBorderColor");
            } else {
                color = (Color)windowStyles.get("inactiveTitleBarColor");
                backgroundColor = (Color)windowStyles.get("inactiveTitleBarBackgroundColor");
                bevelColor = (Color)windowStyles.get("inactiveTitleBarBevelColor");
                pressedBevelColor = (Color)windowStyles.get("inactiveTitleBarPressedBevelColor");
                borderColor = (Color)windowStyles.get("inactiveTitleBarBorderColor");
            }

            super.paint(graphics);
        }

        @Override
        public boolean isFocusable() {
            return false;
        }

        @Override
        public boolean mouseDown(Mouse.Button button, int x, int y) {
            super.mouseDown(button, x, y);
            return true;
        }

        @Override
        public boolean mouseUp(Mouse.Button button, int x, int y) {
            super.mouseUp(button, x, y);
            return true;
        }
    }

    protected abstract class ButtonImage extends ImageAsset {
        public int getPreferredWidth(int height) {
            return 8;
        }

        public int getPreferredHeight(int width) {
            return 8;
        }

        public Dimensions getPreferredSize() {
            return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
        }
    }

    protected class MinimizeImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            Window window = (Window)getComponent();
            graphics.setPaint(window.isActive() ? titleBarColor : inactiveTitleBarColor);
            graphics.fill(new Rectangle2D.Double(0, 6, 8, 2));
        }
    }

    protected class MaximizeImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            Window window = (Window)getComponent();
            graphics.setPaint(window.isActive() ? titleBarColor : inactiveTitleBarColor);
            graphics.fill(new Rectangle2D.Double(0, 0, 8, 8));

            graphics.setPaint(window.isActive() ? titleBarBackgroundColor : inactiveTitleBarBackgroundColor);
            graphics.fill(new Rectangle2D.Double(2, 2, 4, 4));
        }
    }

    protected class RestoreImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            Window window = (Window)getComponent();
            graphics.setPaint(window.isActive() ?
                titleBarColor : inactiveTitleBarColor);
            graphics.fill(new Rectangle2D.Double(1, 1, 6, 6));

            graphics.setPaint(window.isActive() ?
                titleBarBackgroundColor : inactiveTitleBarBackgroundColor);
            graphics.fill(new Rectangle2D.Double(3, 3, 2, 2));
        }
    }

    protected class CloseImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            Window window = (Window)getComponent();
            graphics.setPaint(window.isActive() ?
                titleBarColor : inactiveTitleBarColor);
            graphics.setStroke(new BasicStroke(2));

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.draw(new Line2D.Double(0, 0, 7, 7));
            graphics.draw(new Line2D.Double(0, 7, 7, 0));
        }
    }

    protected class ResizeImage extends ImageAsset {
        public int getPreferredWidth(int height) {
            return 5;
        }

        public int getPreferredHeight(int width) {
            return 5;
        }

        public Dimensions getPreferredSize() {
            return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
        }

        public void paint(Graphics2D graphics) {
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));

            graphics.setPaint(Color.BLACK);
            graphics.fillRect(3, 0, 2, 1);
            graphics.fillRect(0, 3, 2, 1);
            graphics.fillRect(3, 3, 2, 1);

            graphics.setPaint(contentBorderColor);
            graphics.fillRect(3, 1, 2, 1);
            graphics.fillRect(0, 4, 2, 1);
            graphics.fillRect(3, 4, 2, 1);
        }
    }

    private class MoveMouseHandler implements ComponentMouseListener, ComponentMouseButtonListener {
        public void mouseMove(Component component, int x, int y) {
            Display display = (Display)component;

            // Pretend that the mouse can't move off screen (off the display)
            x = Math.min(Math.max(x, 0), display.getWidth() - 1);
            y = Math.min(Math.max(y, 0), display.getHeight() - 1);

            // Calculate the would-be new window location
            int windowX = x - dragOffset.x;
            int windowY = y - dragOffset.y;

            Window window = (Window)getComponent();
            window.setLocation(windowX, windowY);
        }

        public void mouseOver(Component component) {
        }

        public void mouseOut(Component component) {
        }

        public void mouseDown(Component component, Mouse.Button button, int x, int y) {
        }

        public void mouseUp(Component component, Mouse.Button button, int x, int y) {
            Display.getInstance().getComponentMouseListeners().remove(this);
            Display.getInstance().getComponentMouseButtonListeners().remove(this);
        }

        public void mouseClick(Component component, Mouse.Button button, int x, int y,
            int count) {
        }
    }

    private class ResizeMouseHandler implements ComponentMouseListener, ComponentMouseButtonListener {
        public void mouseMove(Component component, int x, int y) {
            Display display = (Display)component;

            // Pretend that the mouse can't move off screen (off the display)
            x = Math.min(Math.max(x, 0), display.getWidth() - 1);
            y = Math.min(Math.max(y, 0), display.getHeight() - 1);

            // Calculate the would-be new window size
            Window window = (Window)getComponent();

            int preferredWidth = -1;
            int preferredHeight = -1;

            if (window.isPreferredWidthSet()) {
                preferredWidth = Math.max(x - window.getX() + dragOffset.x,
                    titleBarFlowPane.getPreferredWidth(-1) + 2);
            }

            if (window.isPreferredHeightSet()) {
                preferredHeight = Math.max(y - window.getY() + dragOffset.y,
                    titleBarFlowPane.getHeight() + resizeHandle.getHeight() + 7);
            }

            window.setPreferredSize(preferredWidth, preferredHeight);
        }

        public void mouseOver(Component component) {
        }

        public void mouseOut(Component component) {
        }

        public void mouseDown(Component component, Mouse.Button button, int x, int y) {
        }

        public void mouseUp(Component component, Mouse.Button button, int x, int y) {
            Display.getInstance().getComponentMouseListeners().remove(this);
            Display.getInstance().getComponentMouseButtonListeners().remove(this);
        }

        public void mouseClick(Component component, Mouse.Button button, int x, int y,
            int count) {
        }
    }

    private Image minimizeImage = new MinimizeImage();
    private Image maximizeImage = new MaximizeImage();
    private Image restoreImage = new RestoreImage();
    private Image closeImage = new CloseImage();
    private Image resizeImage = new ResizeImage();

    private FlowPane titleBarFlowPane = new FlowPane();
    private FlowPane titleFlowPane = new FlowPane();
    private FlowPane frameButtonFlowPane = new FlowPane();

    private ImageView iconImageView = new ImageView();
    private Label titleLabel = new Label();

    protected FrameButton minimizeButton = null;
    protected FrameButton maximizeButton = null;
    protected FrameButton closeButton = null;
    protected ImageView resizeHandle = new ImageView(resizeImage);

    private Point dragOffset = null;
    private MoveMouseHandler moveMouseHandler = new MoveMouseHandler();
    private ResizeMouseHandler resizeMouseHandler = new ResizeMouseHandler();

    private Point restoreLocation = null;

    private static float INACTIVE_ICON_OPACITY = 0.5f;

    // Style properties
    protected Font titleBarFont = DEFAULT_TITLE_BAR_FONT;
    protected Color titleBarColor = DEFAULT_TITLE_BAR_COLOR;
    protected Color titleBarBackgroundColor = DEFAULT_TITLE_BAR_BACKGROUND_COLOR;
    protected Color titleBarBevelColor = DEFAULT_TITLE_BAR_BEVEL_COLOR;
    protected Color titleBarPressedBevelColor = DEFAULT_TITLE_BAR_PRESSED_BEVEL_COLOR;
    protected Color titleBarBorderColor = DEFAULT_TITLE_BAR_BORDER_COLOR;
    protected Color inactiveTitleBarColor = DEFAULT_INACTIVE_TITLE_BAR_COLOR;
    protected Color inactiveTitleBarBackgroundColor = DEFAULT_INACTIVE_TITLE_BAR_BACKGROUND_COLOR;
    protected Color inactiveTitleBarBevelColor = DEFAULT_INACTIVE_TITLE_BAR_BEVEL_COLOR;
    protected Color inactiveTitleBarPressedBevelColor = DEFAULT_INACTIVE_TITLE_BAR_PRESSED_BEVEL_COLOR;
    protected Color inactiveTitleBarBorderColor = DEFAULT_INACTIVE_TITLE_BAR_BORDER_COLOR;
    protected Color contentBorderColor = DEFAULT_CONTENT_BORDER_COLOR;
    protected Color contentBevelColor = DEFAULT_CONTENT_BEVEL_COLOR;
    protected Insets padding = DEFAULT_PADDING;
    protected boolean resizable = DEFAULT_RESIZABLE;

    // Default style values
    private static final Color DEFAULT_BACKGROUND_COLOR = new Color(0xcc, 0xca, 0xc2);

    private static final Font DEFAULT_TITLE_BAR_FONT = new Font("Verdana", Font.BOLD, 11);
    private static final Color DEFAULT_TITLE_BAR_COLOR = Color.WHITE;
    private static final Color DEFAULT_TITLE_BAR_BACKGROUND_COLOR = new Color(0x3c, 0x77, 0xb2);
    private static final Color DEFAULT_TITLE_BAR_BEVEL_COLOR = new Color(0x45, 0x89, 0xcc);
    private static final Color DEFAULT_TITLE_BAR_PRESSED_BEVEL_COLOR = new Color(0x34, 0x66, 0x99);
    private static final Color DEFAULT_TITLE_BAR_BORDER_COLOR = new Color(0x2c, 0x56, 0x80);
    private static final Color DEFAULT_INACTIVE_TITLE_BAR_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color DEFAULT_INACTIVE_TITLE_BAR_BACKGROUND_COLOR = new Color(0xcc, 0xca, 0xc2);
    private static final Color DEFAULT_INACTIVE_TITLE_BAR_BEVEL_COLOR = new Color(0xe6, 0xe3, 0xda);
    private static final Color DEFAULT_INACTIVE_TITLE_BAR_PRESSED_BEVEL_COLOR = new Color(0xCC, 0xCA, 0xC2);
    private static final Color DEFAULT_INACTIVE_TITLE_BAR_BORDER_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color DEFAULT_CONTENT_BORDER_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color DEFAULT_CONTENT_BEVEL_COLOR = new Color(0xe6, 0xe3, 0xda);
    private static final Insets DEFAULT_PADDING = new Insets(8);
    private static final boolean DEFAULT_SHOW_MINIMIZE_BUTTON = true;
    private static final boolean DEFAULT_SHOW_MAXIMIZE_BUTTON = true;
    private static final boolean DEFAULT_SHOW_CLOSE_BUTTON = true;
    private static final boolean DEFAULT_RESIZABLE = true;

    // Style keys
    protected static final String TITLE_BAR_FONT_KEY = "titleBarFont";
    protected static final String TITLE_BAR_COLOR_KEY = "titleBarColor";
    protected static final String TITLE_BAR_BACKGROUND_COLOR_KEY = "titleBarBackgroundColor";
    protected static final String TITLE_BAR_BEVEL_COLOR_KEY = "titleBarBevelColor";
    protected static final String TITLE_BAR_PRESSED_BEVEL_COLOR_KEY = "titleBarPressedBevelColor";
    protected static final String TITLE_BAR_BORDER_COLOR_KEY = "titleBarBorderColor";
    protected static final String INACTIVE_TITLE_BAR_COLOR_KEY = "inactiveTitleBarColor";
    protected static final String INACTIVE_TITLE_BAR_BACKGROUND_COLOR_KEY = "inactiveTitleBarBackgroundColor";
    protected static final String INACTIVE_TITLE_BAR_BEVEL_COLOR_KEY = "inactiveTitleBarBevelColor";
    protected static final String INACTIVE_TITLE_BAR_PRESSED_BEVEL_COLOR_KEY = "inactiveTitleBarPressedBevelColor";
    protected static final String INACTIVE_TITLE_BAR_BORDER_COLOR_KEY = "inactiveTitleBarBorderColor";
    protected static final String CONTENT_BORDER_COLOR_KEY = "contentBorderColor";
    protected static final String CONTENT_BEVEL_COLOR_KEY = "contentBevelColor";
    protected static final String PADDING_KEY = "padding";
    protected static final String SHOW_MINIMIZE_BUTTON_KEY = "showMinimizeButton";
    protected static final String SHOW_MAXIMIZE_BUTTON_KEY = "showMaximizeButton";
    protected static final String SHOW_CLOSE_BUTTON_KEY = "showCloseButton";
    protected static final String RESIZABLE_KEY = "resizable";

    public AbstractFrameSkin() {
        setBackgroundColor(DEFAULT_BACKGROUND_COLOR);

        // The title bar flow pane contains two nested flow panes: one for
        // the title contents and the other for the buttons
        titleBarFlowPane.getComponents().add(titleFlowPane);
        titleBarFlowPane.getComponents().add(frameButtonFlowPane);

        titleBarFlowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.JUSTIFY);
        titleBarFlowPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        // TODO Style, like button spacing in TabPaneSkin?
        titleBarFlowPane.getStyles().put("padding", new Insets(2));

        // Initialize the title flow pane
        titleFlowPane.getComponents().add(iconImageView);
        titleFlowPane.getComponents().add(titleLabel);
        titleFlowPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

        titleLabel.getStyles().put("font", titleBarFont);
        iconImageView.getStyles().put("backgroundColor", null);

        // Initialize the button flow pane
        frameButtonFlowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);
        frameButtonFlowPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
    }

    @Override
    public void install(Component component) {
        validateComponentType(component, Window.class);

        super.install(component);

        Window window = (Window)component;
        Container.ComponentSequence windowComponents = window.getComponents();

        windowComponents.add(titleBarFlowPane);

        // Create the frame buttons (we'll set an appropriate image on the
        // maximize button later)
        minimizeButton = new FrameButton(window, minimizeImage);
        minimizeButton.setDisplayable(DEFAULT_SHOW_MINIMIZE_BUTTON);

        maximizeButton = new FrameButton(window);
        maximizeButton.setDisplayable(DEFAULT_SHOW_MAXIMIZE_BUTTON);

        closeButton = new FrameButton(window, closeImage);
        closeButton.setDisplayable(DEFAULT_SHOW_CLOSE_BUTTON);

        frameButtonFlowPane.getComponents().add(minimizeButton);
        frameButtonFlowPane.getComponents().add(maximizeButton);
        frameButtonFlowPane.getComponents().add(closeButton);

        minimizeButton.getButtonPressListeners().add(this);
        maximizeButton.getButtonPressListeners().add(this);
        closeButton.getButtonPressListeners().add(this);

        resizeHandle.setCursor(Cursor.RESIZE_SOUTH_EAST);
        windowComponents.add(resizeHandle);

        // Add this as an attribute listener so we can preserve a restore
        // location when the window is maximized
        window.getComponentAttributeListeners().add(this);

        iconChanged(window, null);
        titleChanged(window, null);
        activeChanged(window);

        updateMaximizedState();
    }

    @Override
    public void uninstall() {
        Window window = (Window)getComponent();
        Container.ComponentSequence windowComponents = window.getComponents();

        windowComponents.remove(titleBarFlowPane);

        frameButtonFlowPane.getComponents().remove(minimizeButton);
        frameButtonFlowPane.getComponents().remove(maximizeButton);
        frameButtonFlowPane.getComponents().remove(closeButton);

        minimizeButton = null;
        maximizeButton = null;
        closeButton = null;

        // Remove this as an attribute listener
        window.getComponentAttributeListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        Window window = (Window)getComponent();
        Component content = window.getContent();

        Dimensions preferredTitleBarSize = titleBarFlowPane.getPreferredSize();
        preferredWidth = preferredTitleBarSize.width;

        if (content != null
            && content.isDisplayable()) {
            if (height != -1) {
                height = Math.max(height - preferredTitleBarSize.height - 4 -
                    padding.top - padding.bottom, 0);
            }

            preferredWidth = Math.max(preferredWidth,
                content.getPreferredWidth(height));
        }

        preferredWidth += (padding.left + padding.right) + 2;

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        Window window = (Window)getComponent();
        Component content = window.getContent();

        if (width != -1) {
            width = Math.max(width - 2, 0);
        }

        preferredHeight = titleBarFlowPane.getPreferredHeight(width);

        if (content != null
            && content.isDisplayable()) {
            if (width != -1) {
                width = Math.max(width - padding.left - padding.right, 0);
            }

            preferredHeight += content.getPreferredHeight(width);
        }

        preferredHeight += (padding.top + padding.bottom) + 4;

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        Window window = (Window)getComponent();
        Component content = window.getContent();

        Dimensions preferredTitleBarSize = titleBarFlowPane.getPreferredSize();

        preferredWidth = preferredTitleBarSize.width;
        preferredHeight = preferredTitleBarSize.height;

        if (content != null
            && content.isDisplayable()) {
            Dimensions preferredContentSize = content.getPreferredSize();

            preferredWidth = Math.max(preferredWidth, preferredContentSize.width);
            preferredHeight += preferredContentSize.height;
        }

        preferredWidth += (padding.left + padding.right) + 2;
        preferredHeight += (padding.top + padding.bottom) + 4;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    public void layout() {
        Window window = (Window)getComponent();

        int width = getWidth();
        int height = getHeight();

        // Size/position title bar
        titleBarFlowPane.setLocation(1, 1);
        titleBarFlowPane.setSize(Math.max(width - 2, 0),
            Math.max(titleBarFlowPane.getPreferredHeight(width - 2), 0));

        // Size/position resize handle
        resizeHandle.setSize(resizeHandle.getPreferredSize());
        resizeHandle.setLocation(width - resizeHandle.getWidth() - 2,
            height - resizeHandle.getHeight() - 2);

        Boolean maximized = (Boolean)window.getAttributes().get(Display.MAXIMIZED_ATTRIBUTE);
        resizeHandle.setVisible(resizable
            && (maximized == null || !maximized)
            && (window.isPreferredWidthSet() || window.isPreferredHeightSet()));

        // Size/position content
        Component content = window.getContent();

        if (content != null) {
            if (content.isDisplayable()) {
                content.setVisible(true);

                content.setLocation(padding.left + 1,
                    titleBarFlowPane.getHeight() + padding.top + 3);

                int contentWidth = Math.max(width - (padding.left + padding.right + 2), 0);
                int contentHeight = Math.max(height - (titleBarFlowPane.getHeight()
                    + padding.top + padding.bottom + 4), 0);

                content.setSize(contentWidth, contentHeight);
            } else {
                content.setVisible(false);
            }
        }
    }

    public void paint(Graphics2D graphics) {
        // Call the base class to paint the background
        super.paint(graphics);

        Window window = (Window)getComponent();

        int width = getWidth();
        int height = getHeight();
        Dimensions titleBarSize = titleBarFlowPane.getSize();

        // Draw the borders with a 1px solid stroke
        graphics.setStroke(new BasicStroke());

        // Draw the title area
        Rectangle titleBarRectangle = new Rectangle(0, 0, width - 1, titleBarSize.height + 1);
        graphics.setPaint(window.isActive() ?
            titleBarBackgroundColor : inactiveTitleBarBackgroundColor);
        graphics.fill(titleBarRectangle);

        graphics.setPaint(window.isActive() ?
            titleBarBorderColor : inactiveTitleBarBorderColor);
        graphics.draw(titleBarRectangle);

        Line2D titleBarBevelLine = new Line2D.Double(titleBarRectangle.x + 1, titleBarRectangle.y + 1,
            titleBarRectangle.width - 1, titleBarRectangle.y + 1);
        graphics.setPaint(window.isActive() ?
            titleBarBevelColor : inactiveTitleBarBevelColor);
        graphics.draw(titleBarBevelLine);

        // Draw the content area
        Rectangle contentAreaRectangle = new Rectangle(0, titleBarSize.height + 2,
            width - 1, height - (titleBarSize.height + 3));
        graphics.setPaint(contentBorderColor);
        graphics.draw(contentAreaRectangle);

        Line2D contentAreaBevelLine = new Line2D.Double(contentAreaRectangle.x + 1, contentAreaRectangle.y + 1,
            contentAreaRectangle.width - 1, contentAreaRectangle.y + 1);
        graphics.setPaint(contentBevelColor);
        graphics.draw(contentAreaBevelLine);
    }

    @Override
    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        if (key.equals(TITLE_BAR_FONT_KEY)) {
            value = titleBarFont;
        } else if (key.equals(TITLE_BAR_COLOR_KEY)) {
            value = titleBarColor;
        } else if (key.equals(TITLE_BAR_BACKGROUND_COLOR_KEY)) {
            value = titleBarBackgroundColor;
        } else if (key.equals(TITLE_BAR_BEVEL_COLOR_KEY)) {
            value = titleBarBevelColor;
        } else if (key.equals(TITLE_BAR_PRESSED_BEVEL_COLOR_KEY)) {
            value = titleBarPressedBevelColor;
        } else if (key.equals(TITLE_BAR_BORDER_COLOR_KEY)) {
            value = titleBarBorderColor;
        } else if (key.equals(INACTIVE_TITLE_BAR_COLOR_KEY)) {
            value = inactiveTitleBarColor;
        } else if (key.equals(INACTIVE_TITLE_BAR_BACKGROUND_COLOR_KEY)) {
            value = inactiveTitleBarBackgroundColor;
        } else if (key.equals(INACTIVE_TITLE_BAR_BEVEL_COLOR_KEY)) {
            value = inactiveTitleBarBevelColor;
        } else if (key.equals(INACTIVE_TITLE_BAR_PRESSED_BEVEL_COLOR_KEY)) {
            value = inactiveTitleBarPressedBevelColor;
        } else if (key.equals(INACTIVE_TITLE_BAR_BORDER_COLOR_KEY)) {
            value = inactiveTitleBarBorderColor;
        } else if (key.equals(CONTENT_BORDER_COLOR_KEY)) {
            value = contentBorderColor;
        } else if (key.equals(CONTENT_BEVEL_COLOR_KEY)) {
            value = contentBevelColor;
        } else if (key.equals(PADDING_KEY)) {
            value = padding;
        } else if (key.equals(SHOW_MINIMIZE_BUTTON_KEY)) {
            value = minimizeButton.isDisplayable();
        } else if (key.equals(SHOW_MAXIMIZE_BUTTON_KEY)) {
            value = maximizeButton.isDisplayable();
        } else if (key.equals(SHOW_CLOSE_BUTTON_KEY)) {
            value = closeButton.isDisplayable();
        } else if (key.equals(RESIZABLE_KEY)) {
            value = resizable;
        } else {
            value = super.get(key);
        }

        return value;
    }

    @Override
    public Object put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(TITLE_BAR_FONT_KEY)) {
            validatePropertyType(key, value, Font.class, false);

            previousValue = titleBarFont;
            titleBarFont = (Font)value;

            titleLabel.getStyles().put("font", titleBarFont);

            invalidateComponent();
        } else if (key.equals(TITLE_BAR_COLOR_KEY)) {
            validatePropertyType(key, value, Color.class, false);

            previousValue = titleBarColor;
            titleBarColor = (Color)value;

            repaintComponent();
        } else if (key.equals(TITLE_BAR_BACKGROUND_COLOR_KEY)) {
            validatePropertyType(key, value, Color.class, false);

            previousValue = titleBarBackgroundColor;
            titleBarBackgroundColor = (Color)value;

            repaintComponent();
        } else if (key.equals(TITLE_BAR_BEVEL_COLOR_KEY)) {
            validatePropertyType(key, value, Color.class, false);

            previousValue = titleBarBevelColor;
            titleBarBevelColor = (Color)value;

            repaintComponent();
        } else if (key.equals(TITLE_BAR_PRESSED_BEVEL_COLOR_KEY)) {
            validatePropertyType(key, value, Color.class, false);

            previousValue = titleBarPressedBevelColor;
            titleBarPressedBevelColor = (Color)value;

            repaintComponent();
        } else if (key.equals(TITLE_BAR_BORDER_COLOR_KEY)) {
            validatePropertyType(key, value, Color.class, false);

            previousValue = titleBarBorderColor;
            titleBarBorderColor = (Color)value;

            repaintComponent();
        } else if (key.equals(INACTIVE_TITLE_BAR_COLOR_KEY)) {
            validatePropertyType(key, value, Color.class, false);

            previousValue = inactiveTitleBarColor;
            inactiveTitleBarColor = (Color)value;

            repaintComponent();
        } else if (key.equals(INACTIVE_TITLE_BAR_BACKGROUND_COLOR_KEY)) {
            validatePropertyType(key, value, Color.class, false);

            previousValue = inactiveTitleBarBackgroundColor;
            inactiveTitleBarBackgroundColor = (Color)value;

            repaintComponent();
        } else if (key.equals(INACTIVE_TITLE_BAR_BEVEL_COLOR_KEY)) {
            validatePropertyType(key, value, Color.class, false);

            previousValue = inactiveTitleBarBevelColor;
            inactiveTitleBarBevelColor = (Color)value;

            repaintComponent();
        } else if (key.equals(INACTIVE_TITLE_BAR_PRESSED_BEVEL_COLOR_KEY)) {
            validatePropertyType(key, value, Color.class, false);

            previousValue = inactiveTitleBarPressedBevelColor;
            inactiveTitleBarPressedBevelColor = (Color)value;

            repaintComponent();
        } else if (key.equals(INACTIVE_TITLE_BAR_BORDER_COLOR_KEY)) {
            validatePropertyType(key, value, Color.class, false);

            previousValue = inactiveTitleBarBorderColor;
            inactiveTitleBarBorderColor = (Color)value;

            repaintComponent();
        } else if (key.equals(CONTENT_BORDER_COLOR_KEY)) {
            validatePropertyType(key, value, Color.class, false);

            previousValue = contentBorderColor;
            contentBorderColor = (Color)value;

            repaintComponent();
        } else if (key.equals(CONTENT_BEVEL_COLOR_KEY)) {
            validatePropertyType(key, value, Color.class, false);

            previousValue = contentBevelColor;
            contentBevelColor = (Color)value;

            repaintComponent();
        } else if (key.equals(PADDING_KEY)) {
            validatePropertyType(key, value, Insets.class, false);

            previousValue = padding;
            padding = (Insets)value;

            invalidateComponent();
        } else if (key.equals(SHOW_MINIMIZE_BUTTON_KEY)) {
            validatePropertyType(key, value, Boolean.class, false);

            previousValue = minimizeButton.isDisplayable();
            minimizeButton.setDisplayable((Boolean)value);
        } else if (key.equals(SHOW_MAXIMIZE_BUTTON_KEY)) {
            validatePropertyType(key, value, Boolean.class, false);

            previousValue = maximizeButton.isDisplayable();
            maximizeButton.setDisplayable((Boolean)value);
        } else if (key.equals(SHOW_CLOSE_BUTTON_KEY)) {
            validatePropertyType(key, value, Boolean.class, false);

            previousValue = closeButton.isDisplayable();
            closeButton.setDisplayable((Boolean)value);
        } else if (key.equals(RESIZABLE_KEY)) {
            validatePropertyType(key, value, Boolean.class, false);

            previousValue = resizable;
            resizable = (Boolean)value;

            invalidateComponent();
        } else {
            previousValue = super.put(key, value);
        }

        return previousValue;
    }

    @Override
    public Object remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(TITLE_BAR_FONT_KEY)) {
            previousValue = put(key, DEFAULT_TITLE_BAR_FONT);
        } else if (key.equals(TITLE_BAR_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_TITLE_BAR_COLOR);
        } else if (key.equals(TITLE_BAR_BACKGROUND_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_TITLE_BAR_BACKGROUND_COLOR);
        } else if (key.equals(TITLE_BAR_BEVEL_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_TITLE_BAR_BEVEL_COLOR);
        } else if (key.equals(TITLE_BAR_PRESSED_BEVEL_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_TITLE_BAR_PRESSED_BEVEL_COLOR);
        } else if (key.equals(TITLE_BAR_BORDER_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_TITLE_BAR_BORDER_COLOR);
        } else if (key.equals(INACTIVE_TITLE_BAR_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_INACTIVE_TITLE_BAR_COLOR);
        } else if (key.equals(INACTIVE_TITLE_BAR_BACKGROUND_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_INACTIVE_TITLE_BAR_BACKGROUND_COLOR);
        } else if (key.equals(INACTIVE_TITLE_BAR_BEVEL_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_INACTIVE_TITLE_BAR_BEVEL_COLOR);
        } else if (key.equals(INACTIVE_TITLE_BAR_PRESSED_BEVEL_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_INACTIVE_TITLE_BAR_PRESSED_BEVEL_COLOR);
        } else if (key.equals(INACTIVE_TITLE_BAR_BORDER_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_INACTIVE_TITLE_BAR_BORDER_COLOR);
        } else if (key.equals(CONTENT_BORDER_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_CONTENT_BORDER_COLOR);
        } else if (key.equals(CONTENT_BEVEL_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_CONTENT_BEVEL_COLOR);
        } else if (key.equals(PADDING_KEY)) {
            previousValue = put(key, DEFAULT_PADDING);
        } else if (key.equals(SHOW_MINIMIZE_BUTTON_KEY)) {
            previousValue = put(key, DEFAULT_SHOW_MINIMIZE_BUTTON);
        } else if (key.equals(SHOW_MAXIMIZE_BUTTON_KEY)) {
            previousValue = put(key, DEFAULT_SHOW_MAXIMIZE_BUTTON);
        } else if (key.equals(SHOW_CLOSE_BUTTON_KEY)) {
            previousValue = put(key, DEFAULT_SHOW_CLOSE_BUTTON);
        } else if (key.equals(RESIZABLE_KEY)) {
            previousValue = put(key, DEFAULT_RESIZABLE);
        } else {
            previousValue = super.remove(key);
        }

        return previousValue;
    }

    @Override
    public boolean containsKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        return (key.equals(TITLE_BAR_FONT_KEY)
            || key.equals(TITLE_BAR_COLOR_KEY)
            || key.equals(TITLE_BAR_BACKGROUND_COLOR_KEY)
            || key.equals(TITLE_BAR_BEVEL_COLOR_KEY)
            || key.equals(TITLE_BAR_PRESSED_BEVEL_COLOR_KEY)
            || key.equals(TITLE_BAR_BORDER_COLOR_KEY)
            || key.equals(INACTIVE_TITLE_BAR_COLOR_KEY)
            || key.equals(INACTIVE_TITLE_BAR_BACKGROUND_COLOR_KEY)
            || key.equals(INACTIVE_TITLE_BAR_BEVEL_COLOR_KEY)
            || key.equals(INACTIVE_TITLE_BAR_PRESSED_BEVEL_COLOR_KEY)
            || key.equals(INACTIVE_TITLE_BAR_BORDER_COLOR_KEY)
            || key.equals(CONTENT_BORDER_COLOR_KEY)
            || key.equals(CONTENT_BEVEL_COLOR_KEY)
            || key.equals(PADDING_KEY)
            || key.equals(SHOW_MINIMIZE_BUTTON_KEY)
            || key.equals(SHOW_MAXIMIZE_BUTTON_KEY)
            || key.equals(SHOW_CLOSE_BUTTON_KEY)
            || super.containsKey(key));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    private void updateMaximizedState() {
        Window window = (Window)getComponent();
        Boolean maximized = (Boolean)window.getAttributes().get(Display.MAXIMIZED_ATTRIBUTE);

        if (maximized == null
            || !maximized) {
            maximizeButton.setButtonData(maximizeImage);

            if (restoreLocation != null) {
                window.setLocation(restoreLocation.x, restoreLocation.y);
            }
        } else {
            maximizeButton.setButtonData(restoreImage);
            restoreLocation = window.getLocation();
        }
    }

    @Override
    public boolean mouseDown(Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(button, x, y);

        Window window = (Window)getComponent();
        Boolean maximized = (Boolean)window.getAttributes().get(Display.MAXIMIZED_ATTRIBUTE);

        if (button == Mouse.Button.LEFT
            && (maximized == null || !maximized)) {
            Rectangle titleBarBounds = titleBarFlowPane.getBounds();

            if (titleBarBounds.contains(x, y)) {
                dragOffset = new Point(x, y);

                Display.getInstance().getComponentMouseListeners().add(moveMouseHandler);
                Display.getInstance().getComponentMouseButtonListeners().add(moveMouseHandler);
            } else {
                Rectangle resizeHandleBounds = resizeHandle.getBounds();

                if (resizeHandleBounds.contains(x, y)) {
                    dragOffset = new Point(getWidth() - x, getHeight() - y);

                    Display.getInstance().getComponentMouseListeners().add(resizeMouseHandler);
                    Display.getInstance().getComponentMouseButtonListeners().add(resizeMouseHandler);
                }
            }
        }

        return consumed;
    }

    public void attributeAdded(Component component, Container.Attribute attribute) {
        if (attribute == Display.MAXIMIZED_ATTRIBUTE) {
            updateMaximizedState();
        }
    }

    public void attributeUpdated(Component component, Container.Attribute attribute,
        Object previousValue) {
        if (attribute == Display.MAXIMIZED_ATTRIBUTE) {
            updateMaximizedState();
        }
    }

    public void attributeRemoved(Component component, Container.Attribute attribute,
        Object value) {
        if (attribute == Display.MAXIMIZED_ATTRIBUTE) {
            updateMaximizedState();
        }
    }

    @Override
    public void titleChanged(TitlePane dialog, String previousTitle) {
        String title = dialog.getTitle();
        titleLabel.setDisplayable(title != null);
        titleLabel.setText(title);
    }

    @Override
    public void iconChanged(Window dialog, Image previousIcon) {
        Image icon = dialog.getIcon();
        iconImageView.setDisplayable(icon != null);
        iconImageView.setImage(icon);
    }

    @Override
    public void activeChanged(Window window) {
        boolean active = window.isActive();

        titleLabel.getStyles().put("color", active ? titleBarColor : inactiveTitleBarColor);
        iconImageView.getStyles().put("opacity", active ? 1.0f : INACTIVE_ICON_OPACITY);

        repaintComponent();
    }

    /**
     * Listener for frame button events.
     *
     * @param button
     * The source of the button event.
     */
    public void buttonPressed(Button button) {
        Window window = (Window)getComponent();

        if (button == minimizeButton) {
            window.setDisplayable(false);
        } else if (button == maximizeButton) {
            // Toggle the maximized state
            Component.AttributeDictionary windowAttributes = window.getAttributes();
            Boolean maximized = (Boolean)windowAttributes.get(Display.MAXIMIZED_ATTRIBUTE);
            windowAttributes.put(Display.MAXIMIZED_ATTRIBUTE, (maximized == null || !maximized));
        } else if (button == closeButton) {
            window.close();
        }
    }
}
