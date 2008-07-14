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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import pivot.collections.Map;
import pivot.collections.Sequence;
import pivot.wtk.Button;
import pivot.wtk.ButtonStateListener;
import pivot.wtk.Component;
import pivot.wtk.Container;
import pivot.wtk.Dimensions;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Insets;
import pivot.wtk.Mouse;
import pivot.wtk.Orientation;
import pivot.wtk.Rectangle;
import pivot.wtk.TabPane;
import pivot.wtk.TabPaneListener;
import pivot.wtk.TabPaneSelectionListener;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.Button.Group;
import pivot.wtk.content.ButtonData;
import pivot.wtk.content.ButtonDataRenderer;
import pivot.wtk.media.Image;
import pivot.wtk.skin.ButtonSkin;
import pivot.wtk.skin.ContainerSkin;

/**
 * TODO Make tab buttons focusable.
 *
 * TODO Put the button flow pane in a panorama so users can scroll to buttons
 * that are out of view
 *
 * TODO Add showCloseButton style.
 *
 * @author gbrown
 */
public class TabPaneSkin extends ContainerSkin
    implements TabPaneListener, TabPaneSelectionListener, Button.GroupListener {
    public static class TabButton extends Button {
        private TabPane tabPane = null;

        public TabButton(TabPane tabPane) {
            this(tabPane, null);
        }

        public TabButton(TabPane tabPane, Object buttonData) {
            super(buttonData);

            this.tabPane = tabPane;

            setToggleButton(true);
            setDataRenderer(new ButtonDataRenderer());

            installSkin(TabButton.class);
        }

        public TabPane getTabPane() {
            return tabPane;
        }

        @Override
        public boolean isEnabled() {
            return tabPane.isEnabled();
        }

        @Override
        public void setEnabled(boolean enabled) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setTriState(boolean triState) {
            throw new UnsupportedOperationException();
        }

        public void press() {
            super.press();

            // If the tab pane is collapsible, toggle the button selection;
            // otherwise, select it
            setSelected(tabPane.isCollapsible() ? !isSelected() : true);
        }
    }

    public static class TabButtonSkin extends ButtonSkin implements ButtonStateListener {
        private boolean pressed = false;

        protected static final String FONT_KEY = "font";
        protected static final String COLOR_KEY = "color";
        protected static final String DISABLED_COLOR_KEY = "disabledColor";

        public TabButtonSkin() {
        }

        @Override
        public void install(Component component) {
            validateComponentType(component, TabPaneSkin.TabButton.class);

            super.install(component);

            TabPaneSkin.TabButton tabButton = (TabPaneSkin.TabButton)component;
            tabButton.getButtonStateListeners().add(this);
        }

        @Override
        public void uninstall() {
            TabPaneSkin.TabButton tabButton = (TabPaneSkin.TabButton)getComponent();
            tabButton.getButtonStateListeners().remove(this);

            super.uninstall();
        }

        public int getPreferredWidth(int height) {
            TabPaneSkin.TabButton tabButton = (TabPaneSkin.TabButton)getComponent();
            TabPane tabPane = tabButton.getTabPane();

            Button.DataRenderer dataRenderer = tabButton.getDataRenderer();
            dataRenderer.render(tabButton.getButtonData(), tabButton, false);

            Insets padding = (Insets)tabPane.getStyles().get(BUTTON_PADDING_KEY);

            // Include padding in constraint
            if (height != -1) {
                height = Math.max(height - (padding.top + padding.bottom + 2), 0);
            }

            int preferredWidth = 0;
            switch (tabPane.getTabOrientation()) {
                case HORIZONTAL: {
                    preferredWidth = dataRenderer.getPreferredWidth(height)
                        + padding.left + padding.right + 2;
                    break;
                }

                case VERTICAL: {
                    preferredWidth = dataRenderer.getPreferredHeight(height)
                        + padding.top + padding.bottom + 2;
                    break;
                }
            }

            return preferredWidth;
        }

        public int getPreferredHeight(int width) {
            TabPaneSkin.TabButton tabButton = (TabPaneSkin.TabButton)getComponent();
            TabPane tabPane = tabButton.getTabPane();

            Button.DataRenderer dataRenderer = tabButton.getDataRenderer();
            dataRenderer.render(tabButton.getButtonData(), tabButton, false);

            Insets padding = (Insets)tabPane.getStyles().get(BUTTON_PADDING_KEY);

            // Include padding in constraint
            if (width != -1) {
                width = Math.max(width - (padding.left + padding.right + 2), 0);
            }

            int preferredHeight = 0;
            switch (tabPane.getTabOrientation()) {
                case HORIZONTAL: {
                    preferredHeight = dataRenderer.getPreferredHeight(width)
                        + padding.top + padding.bottom + 2;
                    break;
                }

                case VERTICAL: {
                    preferredHeight = dataRenderer.getPreferredWidth(width)
                        + padding.left + padding.right + 2;
                    break;
                }
            }

            return preferredHeight;
        }

        public Dimensions getPreferredSize() {
            TabPaneSkin.TabButton tabButton = (TabPaneSkin.TabButton)getComponent();
            TabPane tabPane = tabButton.getTabPane();

            Button.DataRenderer dataRenderer = tabButton.getDataRenderer();
            dataRenderer.render(tabButton.getButtonData(), tabButton, false);

            Insets padding = (Insets)tabPane.getStyles().get(BUTTON_PADDING_KEY);

            Dimensions preferredContentSize = dataRenderer.getPreferredSize();

            int preferredWidth = 0;
            int preferredHeight = 0;
            switch (tabPane.getTabOrientation()) {
                case HORIZONTAL: {
                    preferredWidth = preferredContentSize.width
                        + padding.left + padding.right + 2;

                    preferredHeight = preferredContentSize.height
                        + padding.top + padding.bottom + 2;

                    break;
                }

                case VERTICAL: {
                    preferredWidth = preferredContentSize.height
                        + padding.top + padding.bottom + 2;

                    preferredHeight = preferredContentSize.width
                        + padding.left + padding.right + 2;

                    break;
                }
            }

            return new Dimensions(preferredWidth, preferredHeight);
        }

        public void paint(Graphics2D graphics) {
            TabPaneSkin.TabButton tabButton = (TabPaneSkin.TabButton)getComponent();
            TabPane tabPane = tabButton.getTabPane();
            Orientation tabOrientation = tabPane.getTabOrientation();

            int width = getWidth();
            int height = getHeight();

            // Get the style values from the tab pane
            Component.StyleDictionary tabPaneStyles = tabPane.getStyles();

            Color activeTabColor = (Color)tabPaneStyles.get(ACTIVE_TAB_COLOR_KEY);
            Color inactiveTabColor = (Color)tabPaneStyles.get(INACTIVE_TAB_COLOR_KEY);
            Color backgroundColor = (Color)(tabButton.isSelected() ? activeTabColor : inactiveTabColor);

            Color borderColor = (Color)tabPaneStyles.get(BORDER_COLOR_KEY);
            Color bevelColor = (Color)tabPaneStyles.get(BUTTON_BEVEL_COLOR_KEY);
            Insets padding = (Insets)tabPaneStyles.get(BUTTON_PADDING_KEY);

            // Draw all lines with a 1px solid stroke
            graphics.setStroke(new BasicStroke());

            // Paint the background
            Rectangle bounds = new Rectangle(0, 0, width - 1, height - 1);
            graphics.setPaint(backgroundColor);
            graphics.fill(bounds);

            // Draw the border
            graphics.setPaint(borderColor);
            graphics.draw(bounds);

            // Draw the divider for the selected tab
            Line2D.Double bevelLine = new Line2D.Double(1, 1, width - 2, 1);

            if (tabButton.isSelected()) {
                Line2D dividerLine = null;

                switch (tabOrientation) {
                    case HORIZONTAL: {
                        dividerLine = new Line2D.Double(1, height - 1,
                            width - 2, height - 1);
                        break;
                    }

                    case VERTICAL: {
                        dividerLine = new Line2D.Double(width - 1, 1,
                            width - 1, height - 2);

                        // Extend the bevel line so it reaches the edge of
                        // the button
                        bevelLine.x2 += 1;
                        break;
                    }
                }

                graphics.setPaint(activeTabColor);
                graphics.draw(dividerLine);
            }

            // Draw the bevel
            graphics.setPaint(bevelColor);
            graphics.draw(bevelLine);

            // Paint the content
            Button.DataRenderer dataRenderer = tabButton.getDataRenderer();
            dataRenderer.render(tabButton.getButtonData(), tabButton, false);

            Graphics2D contentGraphics = (Graphics2D)graphics.create();

            switch (tabOrientation) {
                case HORIZONTAL: {
                    dataRenderer.setSize(Math.max(getWidth() - (padding.left + padding.right + 2), 0),
                        Math.max(getHeight() - (padding.top + padding.bottom + 2), 0));

                    contentGraphics.translate(padding.left + 1, padding.top + 1);
                    break;
                }

                case VERTICAL: {
                    dataRenderer.setSize(Math.max(getHeight() - (padding.top + padding.bottom + 2), 0),
                        Math.max(getWidth() - (padding.left + padding.right + 2), 0));

                    contentGraphics.translate(padding.top + 1, padding.left + 1);
                    contentGraphics.rotate(-Math.PI / 2d);
                    contentGraphics.translate(-dataRenderer.getWidth(), 0);
                    break;
                }
            }

            contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
            dataRenderer.paint(contentGraphics);
        }

        public boolean isPressed() {
            return pressed;
        }

        @Override
        public boolean isFocusable() {
            return false;
        }

        @Override
        public Object get(String key) {
            if (key == null) {
                throw new IllegalArgumentException("key is null.");
            }

            TabPaneSkin.TabButton tabButton = (TabPaneSkin.TabButton)getComponent();
            TabPane tabPane = tabButton.getTabPane();

            Object value = null;

            if (key.equals(FONT_KEY)) {
                value = tabPane.getStyles().get("buttonFont");
            }
            else if (key.equals(COLOR_KEY)) {
                value = tabPane.getStyles().get("buttonColor");
            }
            else if (key.equals(DISABLED_COLOR_KEY)) {
                value = tabPane.getStyles().get("disabledButtonColor");
            }
            else {
                value = super.get(key);
            }

            return value;
        }

        @Override
        public Object put(String key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(String key) {
            if (key == null) {
                throw new IllegalArgumentException("key is null.");
            }

            return (key.equals(FONT_KEY)
                || key.equals(COLOR_KEY)
                || key.equals(DISABLED_BUTTON_COLOR_KEY)
                || super.containsKey(key));
        }

        public boolean isEmpty() {
            return false;
        }

        public void stateChanged(Button button, Button.State previousState) {
            repaintComponent();
        }

        @Override
        public void enabledChanged(Component component) {
            repaintComponent();
        }

        @Override
        public void mouseOut() {
            super.mouseOut();

            if (pressed) {
                pressed = false;
                repaintComponent();
            }
        }

        @Override
        public boolean mouseDown(Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseDown(button, x, y);

            pressed = true;
            repaintComponent();

            return consumed;
        }

        @Override
        public boolean mouseUp(Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseUp(button, x, y);

            pressed = false;
            repaintComponent();

            return consumed;
        }

        @Override
        public void mouseClick(Mouse.Button button, int x, int y, int count) {
            TabPaneSkin.TabButton tabButton = (TabPaneSkin.TabButton)getComponent();
            tabButton.press();
        }
    }

    protected FlowPane buttonFlowPane = new FlowPane();
    private Button.Group tabButtonGroup = new Button.Group();

    // Style properties
    protected Color activeTabColor = DEFAULT_ACTIVE_TAB_COLOR;
    protected Color inactiveTabColor = DEFAULT_INACTIVE_TAB_COLOR;
    protected Color borderColor = DEFAULT_BORDER_COLOR;
    protected Insets padding = DEFAULT_PADDING;
    protected Font buttonFont = DEFAULT_BUTTON_FONT;
    protected Color buttonColor = DEFAULT_BUTTON_COLOR;
    protected Color disabledButtonColor = DEFAULT_DISABLED_BUTTON_COLOR;
    protected Color buttonBevelColor = DEFAULT_BUTTON_BEVEL_COLOR;
    protected Insets buttonPadding = DEFAULT_BUTTON_PADDING;

    // Default style values
    private static final Color DEFAULT_ACTIVE_TAB_COLOR = new Color(0xF7, 0xF5, 0xEB);
    private static final Color DEFAULT_INACTIVE_TAB_COLOR = new Color(0xCC, 0xCA, 0xC2);
    private static final Color DEFAULT_BORDER_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Insets DEFAULT_PADDING = new Insets(6);
    private static final Font DEFAULT_BUTTON_FONT = new Font("Verdana", Font.PLAIN, 11);
    private static final Color DEFAULT_BUTTON_COLOR = Color.BLACK;
    private static final Color DEFAULT_DISABLED_BUTTON_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color DEFAULT_BUTTON_BEVEL_COLOR = new Color(0xE6, 0xE3, 0xDA);
    private static final Insets DEFAULT_BUTTON_PADDING = new Insets(3, 4, 3, 4);
    private static final int DEFAULT_BUTTON_SPACING = 2;

    // Style keys
    protected static final String ACTIVE_TAB_COLOR_KEY = "activeTabColor";
    protected static final String INACTIVE_TAB_COLOR_KEY = "inactiveTabColor";
    protected static final String BORDER_COLOR_KEY = "borderColor";
    protected static final String PADDING_KEY = "padding";
    protected static final String BUTTON_FONT_KEY = "buttonFont";
    protected static final String BUTTON_COLOR_KEY = "buttonColor";
    protected static final String DISABLED_BUTTON_COLOR_KEY = "disabledButtonColor";
    protected static final String BUTTON_BEVEL_COLOR_KEY = "buttonBevelColor";
    protected static final String BUTTON_PADDING_KEY = "buttonPadding";
    protected static final String BUTTON_SPACING_KEY = "buttonSpacing";

    public TabPaneSkin() {
        tabButtonGroup.getGroupListeners().add(this);

        buttonFlowPane.getStyles().put("spacing", DEFAULT_BUTTON_SPACING);
    }

    public void install(Component component) {
        validateComponentType(component, TabPane.class);

        super.install(component);

        TabPane tabPane = (TabPane)component;

        // Add this as a listener on the tab pane
        tabPane.getTabPaneListeners().add(this);
        tabPane.getTabPaneSelectionListeners().add(this);

        // Add the button flow pane
        tabPane.getComponents().add(buttonFlowPane);

        // Apply the current tab orientation
        tabOrientationChanged(tabPane);

        // Add buttons for all existing tabs
        for (Component tab : tabPane.getTabs()) {
            TabPaneSkin.TabButton tabButton = new TabPaneSkin.TabButton(tabPane,
                getButtonData(tab));
            tabButton.setGroup(tabButtonGroup);

            buttonFlowPane.getComponents().add(tabButton);
        }
    }

    public void uninstall() {
        TabPane tabPane = (TabPane)getComponent();

        // Remove this as a listener on the tab pane
        tabPane.getTabPaneListeners().remove(this);
        tabPane.getTabPaneSelectionListeners().remove(this);

        // Remove existing buttons
        buttonFlowPane.getComponents().removeAll();

        // Remove the button flow pane
        tabPane.getComponents().remove(buttonFlowPane);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        TabPane tabPane = (TabPane)getComponent();
        Orientation tabOrientation = tabPane.getTabOrientation();

        switch (tabOrientation) {
            case HORIZONTAL: {
                if (height != -1) {
                    height -= buttonFlowPane.getPreferredHeight(-1);
                }

                if (tabPane.getSelectedIndex() != -1) {
                    for (Component tab : tabPane.getTabs()) {
                        preferredWidth = Math.max(preferredWidth,
                            tab.getPreferredWidth(height));
                    }

                    preferredWidth += (padding.left + padding.right + 2);
                }

                preferredWidth = Math.max(preferredWidth,
                    buttonFlowPane.getPreferredWidth(-1));

                break;
            }

            case VERTICAL: {
                if (tabPane.getSelectedIndex() != -1) {
                    for (Component tab : tabPane.getTabs()) {
                        preferredWidth = Math.max(preferredWidth,
                            tab.getPreferredWidth(height));
                    }

                    preferredWidth += (padding.left + padding.right + 2);
                }

                preferredWidth += buttonFlowPane.getPreferredWidth(height);

                break;
            }
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        TabPane tabPane = (TabPane)getComponent();
        Orientation tabOrientation = tabPane.getTabOrientation();

        switch (tabOrientation) {
            case HORIZONTAL: {
                if (tabPane.getSelectedIndex() != -1) {
                    for (Component tab : tabPane.getTabs()) {
                        preferredHeight = Math.max(preferredHeight,
                            tab.getPreferredHeight(width));
                    }

                    preferredHeight += (padding.top + padding.bottom + 2);
                }

                preferredHeight += buttonFlowPane.getPreferredHeight(width);

                break;
            }

            case VERTICAL: {
                if (width != -1) {
                    width -= buttonFlowPane.getPreferredWidth(-1);
                }

                if (tabPane.getSelectedIndex() != -1) {
                    for (Component tab : tabPane.getTabs()) {
                        preferredHeight = Math.max(preferredHeight,
                            tab.getPreferredHeight(width));
                    }

                    preferredHeight += (padding.top + padding.bottom + 2);
                }

                preferredHeight = Math.max(preferredHeight,
                    buttonFlowPane.getPreferredHeight(-1));

                break;
            }
        }

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        // TODO Optimize
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        TabPane tabPane = (TabPane)getComponent();
        int width = getWidth();
        int height = getHeight();

        buttonFlowPane.setLocation(0, 0);

        int tabX = 0;
        int tabY = 0;
        int tabWidth = 0;
        int tabHeight = 0;

        switch (tabPane.getTabOrientation()) {
            case HORIZONTAL: {
                buttonFlowPane.setSize(width, buttonFlowPane.getPreferredHeight(-1));

                tabX = padding.left + 1;
                tabY = padding.top + buttonFlowPane.getHeight() + 1;
                tabWidth = Math.max(width - (padding.left + padding.right + 2), 0);
                tabHeight = Math.max(height - (padding.top + padding.bottom
                    + buttonFlowPane.getHeight() + 2), 0);

                break;
            }

            case VERTICAL: {
                buttonFlowPane.setSize(buttonFlowPane.getPreferredWidth(-1), height);

                tabX = padding.left + buttonFlowPane.getWidth() + 1;
                tabY = padding.top + 1;
                tabWidth = Math.max(width - (padding.left + padding.right
                    + buttonFlowPane.getWidth() + 2), 0);
                tabHeight = Math.max(height - (padding.top + padding.bottom + 2), 0);

                break;
            }
        }

        TabPane.TabSequence tabs = tabPane.getTabs();
        int selectedIndex = tabPane.getSelectedIndex();

        for (int i = 0, n = tabs.getLength(); i < n; i++) {
            Component tab = tabs.get(i);
            if (i == selectedIndex) {
                // Show the selected tab
                tab.setVisible(true);

                // Set the tab's size and location
                tab.setLocation(tabX, tabY);
                tab.setSize(tabWidth, tabHeight);
            } else {
                tab.setVisible(false);
            }
        }
    }

    public void paint(Graphics2D graphics) {
        TabPane tabPane = (TabPane)getComponent();

        Rectangle tabPaneBounds = tabPane.getBounds();

        // Call the base class to paint the background
        super.paint(graphics);

        // Draw all lines with a 1px solid stroke
        graphics.setStroke(new BasicStroke());

        // Paint the content background and border
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;

        Orientation tabOrientation = tabPane.getTabOrientation();

        switch (tabOrientation) {
            case HORIZONTAL: {
                x = 0;
                y = Math.max(buttonFlowPane.getHeight() - 1, 0);
                width = Math.max(tabPaneBounds.width - 1, 0);
                height = Math.max(tabPaneBounds.height - y - 1, 0);

                break;
            }

            case VERTICAL: {
                x = Math.max(buttonFlowPane.getWidth() - 1, 0);
                y = 0;
                width = Math.max(tabPaneBounds.width - x - 1, 0);
                height = Math.max(tabPaneBounds.height - 1, 0);

                break;
            }
        }

        Rectangle contentBounds = new Rectangle(x, y, width, height);

        if (!contentBounds.isEmpty()) {
            // If a tab is selected, paint the active background color; otherwise,
            // paint the inactive background color
            int selectedIndex = tabPane.getSelectedIndex();
            graphics.setPaint((selectedIndex == -1) ? inactiveTabColor : activeTabColor);
            graphics.fill(contentBounds);

            // Draw the border
            graphics.setPaint(borderColor);
            graphics.draw(contentBounds);

            // Draw the bevel for vertical tabs
            if (tabOrientation == Orientation.VERTICAL) {
                Line2D bevelLine = new Line2D.Double(contentBounds.x + 1, contentBounds.y + 1,
                    contentBounds.x + contentBounds.width - 1, contentBounds.y + 1);
                graphics.setPaint(buttonBevelColor);
                graphics.draw(bevelLine);
            }
        }
    }

    @Override
    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        if (key.equals(ACTIVE_TAB_COLOR_KEY)) {
            value = activeTabColor;
        } else if (key.equals(INACTIVE_TAB_COLOR_KEY)) {
            value = inactiveTabColor;
        } else if (key.equals(BORDER_COLOR_KEY)) {
            value = borderColor;
        } else if (key.equals(PADDING_KEY)) {
            value = padding;
        } else if (key.equals(BUTTON_FONT_KEY)) {
            value = buttonFont;
        } else if (key.equals(BUTTON_COLOR_KEY)) {
            value = buttonColor;
        } else if (key.equals(DISABLED_BUTTON_COLOR_KEY)) {
            value = disabledButtonColor;
        } else if (key.equals(BUTTON_BEVEL_COLOR_KEY)) {
            value = buttonBevelColor;
        } else if (key.equals(BUTTON_PADDING_KEY)) {
            value = buttonPadding;
        } else if (key.equals(BUTTON_SPACING_KEY)) {
            value = buttonFlowPane.getStyles().get("spacing");
        } else {
            value = super.get(key);
        }

        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(ACTIVE_TAB_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = activeTabColor;
            activeTabColor = (Color)value;

            repaintComponent();
        } else if (key.equals(INACTIVE_TAB_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = inactiveTabColor;
            inactiveTabColor = (Color)value;

            repaintComponent();
        } else if (key.equals(BORDER_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = borderColor;
            borderColor = (Color)value;
        } else if (key.equals(PADDING_KEY)) {
            if (value instanceof Number) {
                value = new Insets(((Number)value).intValue());
            } else {
                if (value instanceof Map<?, ?>) {
                    value = new Insets((Map<String, Object>)value);
                }
            }

            validatePropertyType(key, value, Insets.class, false);

            previousValue = padding;
            padding = (Insets)value;

            invalidateComponent();
        } else if (key.equals(BUTTON_FONT_KEY)) {
            if (value instanceof String) {
                value = Font.decode((String)value);
            }

            validatePropertyType(key, value, Font.class, false);

            previousValue = buttonFont;
            buttonFont = (Font)value;

            invalidateComponent();
        } else if (key.equals(BUTTON_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = buttonColor;
            buttonColor = (Color)value;

            repaintComponent();
        } else if (key.equals(DISABLED_BUTTON_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = disabledButtonColor;
            disabledButtonColor = (Color)value;

            repaintComponent();
        } else if (key.equals(BUTTON_BEVEL_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = buttonBevelColor;
            buttonBevelColor = (Color)value;

            repaintComponent();
        } else if (key.equals(BUTTON_PADDING_KEY)) {
            if (value instanceof Number) {
                value = new Insets(((Number)value).intValue());
            } else {
                if (value instanceof Map<?, ?>) {
                    value = new Insets((Map<String, Object>)value);
                }
            }

            validatePropertyType(key, value, Insets.class, false);

            previousValue = buttonPadding;
            buttonPadding = (Insets)value;

            invalidateComponent();
        } else if (key.equals(BUTTON_SPACING_KEY)) {
            if (value instanceof Number) {
                value = ((Number)value).intValue();
            }

            previousValue = buttonFlowPane.getStyles().put("spacing", value);
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

        if (key.equals(ACTIVE_TAB_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_ACTIVE_TAB_COLOR);
        } else if (key.equals(INACTIVE_TAB_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_INACTIVE_TAB_COLOR);
        } else if (key.equals(BORDER_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_BORDER_COLOR);
        } else if (key.equals(PADDING_KEY)) {
            previousValue = put(key, DEFAULT_PADDING);
        } else if (key.equals(BUTTON_FONT_KEY)) {
            previousValue = put(key, DEFAULT_BUTTON_FONT);
        } else if (key.equals(BUTTON_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_BUTTON_COLOR);
        } else if (key.equals(DISABLED_BUTTON_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_DISABLED_BUTTON_COLOR);
        } else if (key.equals(BUTTON_BEVEL_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_BUTTON_BEVEL_COLOR);
        } else if (key.equals(BUTTON_PADDING_KEY)) {
            previousValue = put(key, BUTTON_PADDING_KEY);
        } else if (key.equals(BUTTON_SPACING_KEY)) {
            previousValue = put(key, DEFAULT_BUTTON_SPACING);
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

        return (key.equals(ACTIVE_TAB_COLOR_KEY)
            || key.equals(INACTIVE_TAB_COLOR_KEY)
            || key.equals(BORDER_COLOR_KEY)
            || key.equals(PADDING_KEY)
            || key.equals(BUTTON_FONT_KEY)
            || key.equals(BUTTON_COLOR_KEY)
            || key.equals(DISABLED_BUTTON_COLOR_KEY)
            || key.equals(BUTTON_BEVEL_COLOR_KEY)
            || key.equals(BUTTON_PADDING_KEY)
            || key.equals(BUTTON_SPACING_KEY)
            || super.containsKey(key));
    }

    @Override
    public void attributeAdded(Component component, Container.Attribute attribute) {
        super.attributeAdded(component, attribute);

        if (attribute == TabPane.ICON_ATTRIBUTE
            || attribute == TabPane.LABEL_ATTRIBUTE) {
            updateButtonData(component);
        }
    }

    @Override
    public void attributeUpdated(Component component, Container.Attribute attribute,
        Object previousValue) {
        super.attributeUpdated(component, attribute, previousValue);

        if (attribute == TabPane.ICON_ATTRIBUTE
            || attribute == TabPane.LABEL_ATTRIBUTE) {
            updateButtonData(component);
        }
    }

    @Override
    public void attributeRemoved(Component component, Container.Attribute attribute,
        Object value) {
        super.attributeRemoved(component, attribute, value);

        if (attribute == TabPane.ICON_ATTRIBUTE
            || attribute == TabPane.LABEL_ATTRIBUTE) {
            updateButtonData(component);
        }
    }

    protected ButtonData getButtonData(Component tab) {
        Image icon = (Image)tab.getAttributes().get(TabPane.ICON_ATTRIBUTE);
        String label = (String)tab.getAttributes().get(TabPane.LABEL_ATTRIBUTE);

        return new ButtonData(icon, label);
    }

    protected void updateButtonData(Component tab) {
        TabPane tabPane = (TabPane)getComponent();
        int tabIndex = tabPane.getTabs().indexOf(tab);

        if (tabIndex != -1) {
            TabPaneSkin.TabButton tabButton = (TabPaneSkin.TabButton)buttonFlowPane.getComponents().get(tabIndex);
            tabButton.setButtonData(getButtonData(tab));
        }
    }

    // Tab pane events
    public void tabOrientationChanged(TabPane tabPane) {
        Orientation tabOrientation = tabPane.getTabOrientation();

        buttonFlowPane.setOrientation(tabOrientation);

        Component.StyleDictionary buttonFlowPaneStyles = buttonFlowPane.getStyles();
        switch (tabOrientation) {
            case HORIZONTAL: {
                buttonFlowPaneStyles.put("horizontalAlignment", HorizontalAlignment.LEFT);
                buttonFlowPaneStyles.put("verticalAlignment", VerticalAlignment.JUSTIFY);
                break;
            }

            case VERTICAL: {
                buttonFlowPaneStyles.put("horizontalAlignment", HorizontalAlignment.JUSTIFY);
                buttonFlowPaneStyles.put("verticalAlignment", VerticalAlignment.TOP);
                break;
            }
        }
    }

    public void collapsibleChanged(TabPane tabPane) {
        // No-op
    }

    public void tabInserted(TabPane tabPane, int index) {
        // Create a new button for the tab
        Component tab = tabPane.getTabs().get(index);
        TabPaneSkin.TabButton tabButton = new TabPaneSkin.TabButton(tabPane,
            getButtonData(tab));
        tabButton.setGroup(tabButtonGroup);

        buttonFlowPane.getComponents().insert(tabButton, index);
    }

    public void tabsRemoved(TabPane tabPane, int index, Sequence<Component> tabs) {
        // Remove the buttons
        Sequence<Component> removed = buttonFlowPane.getComponents().remove(index, tabs.getLength());

        for (int i = 0, n = removed.getLength(); i < n; i++) {
            TabButton tabButton = (TabButton)removed.get(i);
            tabButton.setGroup(null);
        }
    }

    // Tab pane selection events
    public void selectedIndexChanged(TabPane tabPane, int previousSelectedIndex) {
        int selectedIndex = tabPane.getSelectedIndex();

        if (selectedIndex == -1) {
            Button button = tabButtonGroup.getSelection();
            if (button != null) {
                button.setSelected(false);
            }
        } else {
            Button button = (Button)buttonFlowPane.getComponents().get(selectedIndex);
            button.setSelected(true);
        }

        invalidateComponent();
    }

    // Button group events
    public void selectionChanged(Group group, Button previousSelection) {
        Button button = tabButtonGroup.getSelection();
        int index = (button == null) ? -1 : buttonFlowPane.getComponents().indexOf(button);

        TabPane tabPane = (TabPane)getComponent();
        tabPane.setSelectedIndex(index);
    }
}
