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

import pivot.collections.Dictionary;
import pivot.collections.Sequence;
import pivot.wtk.Button;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Insets;
import pivot.wtk.Mouse;
import pivot.wtk.Orientation;
import pivot.wtk.Panorama;
import pivot.wtk.Bounds;
import pivot.wtk.TabPane;
import pivot.wtk.TabPaneListener;
import pivot.wtk.TabPaneSelectionListener;
import pivot.wtk.TabPaneAttributeListener;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.Button.Group;
import pivot.wtk.content.ButtonData;
import pivot.wtk.content.ButtonDataRenderer;
import pivot.wtk.media.Image;
import pivot.wtk.skin.ButtonSkin;
import pivot.wtk.skin.ContainerSkin;

/**
 * Tab pane skin.
 * <p>
 * TODO Make tab buttons focusable?
 * <p>
 * TODO Disable the tab button when the component is disabled? We'd need
 * style properties to present a disabled tab button state. We'd also need
 * to manage button enabled state independently of tab pane enabled state.
 * <p>
 * TODO Support the displayable flag to show/hide tabs.
 * <p>
 * TODO Add showCloseButton style.
 *
 * @author gbrown
 */
public class TabPaneSkin extends ContainerSkin
    implements TabPaneListener, TabPaneSelectionListener, TabPaneAttributeListener,
        Button.GroupListener {
    protected class TabButton extends Button {
        public TabButton() {
            this(null);
        }

        public TabButton(Object buttonData) {
            super(buttonData);

            super.setToggleButton(true);
            setDataRenderer(new ButtonDataRenderer());

            setSkin(new TabButtonSkin());
        }

        @Override
        public boolean isEnabled() {
            TabPane tabPane = (TabPane)TabPaneSkin.this.getComponent();
            return tabPane.isEnabled();
        }

        @Override
        public void setEnabled(boolean enabled) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setToggleButton(boolean toggleButton) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setTriState(boolean triState) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void press() {
            // If the tab pane is collapsible, toggle the button selection;
            // otherwise, select it
            TabPane tabPane = (TabPane)TabPaneSkin.this.getComponent();
            setSelected(tabPane.isCollapsible() ? !isSelected() : true);

            super.press();
        }
    }

    protected class TabButtonSkin extends ButtonSkin {
        private boolean pressed = false;

        public int getPreferredWidth(int height) {
            TabButton tabButton = (TabButton)getComponent();
            TabPane tabPane = (TabPane)TabPaneSkin.this.getComponent();

            Button.DataRenderer dataRenderer = tabButton.getDataRenderer();
            dataRenderer.render(tabButton.getButtonData(), tabButton, false);

            // Include padding in constraint
            if (height != -1) {
                height = Math.max(height - (buttonPadding.top + buttonPadding.bottom + 2), 0);
            }

            int preferredWidth = 0;
            switch (tabPane.getTabOrientation()) {
                case HORIZONTAL: {
                    preferredWidth = dataRenderer.getPreferredWidth(height)
                        + buttonPadding.left + buttonPadding.right + 2;
                    break;
                }

                case VERTICAL: {
                    preferredWidth = dataRenderer.getPreferredHeight(height)
                        + buttonPadding.top + buttonPadding.bottom + 2;
                    break;
                }
            }

            return preferredWidth;
        }

        public int getPreferredHeight(int width) {
            TabButton tabButton = (TabButton)getComponent();
            TabPane tabPane = (TabPane)TabPaneSkin.this.getComponent();

            Button.DataRenderer dataRenderer = tabButton.getDataRenderer();
            dataRenderer.render(tabButton.getButtonData(), tabButton, false);

            // Include padding in constraint
            if (width != -1) {
                width = Math.max(width - (buttonPadding.left + buttonPadding.right + 2), 0);
            }

            int preferredHeight = 0;
            switch (tabPane.getTabOrientation()) {
                case HORIZONTAL: {
                    preferredHeight = dataRenderer.getPreferredHeight(width)
                        + buttonPadding.top + buttonPadding.bottom + 2;
                    break;
                }

                case VERTICAL: {
                    preferredHeight = dataRenderer.getPreferredWidth(width)
                        + buttonPadding.left + buttonPadding.right + 2;
                    break;
                }
            }

            return preferredHeight;
        }

        public Dimensions getPreferredSize() {
            TabButton tabButton = (TabButton)getComponent();
            TabPane tabPane = (TabPane)TabPaneSkin.this.getComponent();

            Button.DataRenderer dataRenderer = tabButton.getDataRenderer();
            dataRenderer.render(tabButton.getButtonData(), tabButton, false);

            Dimensions preferredContentSize = dataRenderer.getPreferredSize();

            int preferredWidth = 0;
            int preferredHeight = 0;
            switch (tabPane.getTabOrientation()) {
                case HORIZONTAL: {
                    preferredWidth = preferredContentSize.width
                        + buttonPadding.left + buttonPadding.right + 2;

                    preferredHeight = preferredContentSize.height
                        + buttonPadding.top + buttonPadding.bottom + 2;

                    break;
                }

                case VERTICAL: {
                    preferredWidth = preferredContentSize.height
                        + buttonPadding.top + buttonPadding.bottom + 2;

                    preferredHeight = preferredContentSize.width
                        + buttonPadding.left + buttonPadding.right + 2;

                    break;
                }
            }

            return new Dimensions(preferredWidth, preferredHeight);
        }

        public void paint(Graphics2D graphics) {
            TabButton tabButton = (TabButton)getComponent();
            TabPane tabPane = (TabPane)TabPaneSkin.this.getComponent();
            Orientation tabOrientation = tabPane.getTabOrientation();

            Color backgroundColor = (tabButton.isSelected()) ?
                activeTabColor : inactiveTabColor;
            Color bevelColor = (pressed
                || tabButton.isSelected()) ? pressedButtonBevelColor : buttonBevelColor;

            int width = getWidth();
            int height = getHeight();

            // Draw all lines with a 1px solid stroke
            graphics.setStroke(new BasicStroke());

            // Paint the background
            Bounds bounds = new Bounds(0, 0, width - 1, height - 1);
            graphics.setPaint(backgroundColor);
            graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

            // Draw the border
            graphics.setPaint(borderColor);
            graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

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

                graphics.setPaint(backgroundColor);
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
                    dataRenderer.setSize(Math.max(getWidth() - (buttonPadding.left + buttonPadding.right + 2), 0),
                        Math.max(getHeight() - (buttonPadding.top + buttonPadding.bottom + 2), 0));

                    contentGraphics.translate(buttonPadding.left + 1, buttonPadding.top + 1);
                    break;
                }

                case VERTICAL: {
                    dataRenderer.setSize(Math.max(getHeight() - (buttonPadding.top + buttonPadding.bottom + 2), 0),
                        Math.max(getWidth() - (buttonPadding.left + buttonPadding.right + 2), 0));

                    contentGraphics.translate(buttonPadding.top + 1, buttonPadding.left + 1);
                    contentGraphics.rotate(-Math.PI / 2d);
                    contentGraphics.translate(-dataRenderer.getWidth(), 0);
                    break;
                }
            }

            contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
            dataRenderer.paint(contentGraphics);

            contentGraphics.dispose();
        }

        @Override
        public boolean isFocusable() {
            return false;
        }

        @Override
        public void mouseOut(Component component) {
            super.mouseOut(component);

            if (pressed) {
                pressed = false;
                repaintComponent();
            }
        }

        @Override
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseDown(component, button, x, y);

            pressed = true;
            repaintComponent();

            return consumed;
        }

        @Override
        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseUp(component, button, x, y);

            pressed = false;
            repaintComponent();

            return consumed;
        }

        @Override
        public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            TabButton tabButton = (TabButton)getComponent();
            tabButton.press();
        }

        @Override
        public void enabledChanged(Component component) {
            repaintComponent();
        }
    }

    protected Panorama buttonPanorama = new Panorama();
    protected FlowPane buttonFlowPane = new FlowPane();
    private Button.Group tabButtonGroup = new Button.Group();

    private Color activeTabColor = new Color(0xF7, 0xF5, 0xEB);
    private Color inactiveTabColor = new Color(0xCC, 0xCA, 0xC2);
    private Color borderColor = new Color(0x99, 0x99, 0x99);
    private Insets padding = new Insets(6);
    private Font buttonFont = new Font("Verdana", Font.PLAIN, 11);
    private Color buttonColor = Color.BLACK;
    private Color buttonBevelColor = new Color(0xE6, 0xE3, 0xDA);
    private Color pressedButtonBevelColor = new Color(0xE6, 0xE3, 0xDA);
    private Insets buttonPadding = new Insets(3, 4, 3, 4);

    public TabPaneSkin() {
        tabButtonGroup.getGroupListeners().add(this);

        buttonFlowPane.getStyles().put("spacing", 2);
    }

    public void install(Component component) {
        super.install(component);

        TabPane tabPane = (TabPane)component;

        // Add this as a listener on the tab pane
        tabPane.getTabPaneListeners().add(this);
        tabPane.getTabPaneSelectionListeners().add(this);
        tabPane.getTabPaneAttributeListeners().add(this);

        // Add the button panorama and flow pane
        buttonPanorama.getStyles().put("buttonBackgroundColor", borderColor);
        buttonPanorama.getStyles().put("buttonPadding", 6);
        buttonPanorama.setView(buttonFlowPane);
        tabPane.add(buttonPanorama);

        // Apply the current tab orientation
        tabOrientationChanged(tabPane);

        // Add buttons for all existing tabs
        for (Component tab : tabPane.getTabs()) {
            TabButton tabButton = new TabButton(new ButtonData(TabPane.getIcon(tab),
                TabPane.getName(tab)));
            tabButton.setGroup(tabButtonGroup);

            buttonFlowPane.add(tabButton);
        }
    }

    public void uninstall() {
        TabPane tabPane = (TabPane)getComponent();

        // Remove this as a listener on the tab pane
        tabPane.getTabPaneListeners().remove(this);
        tabPane.getTabPaneSelectionListeners().remove(this);
        tabPane.getTabPaneAttributeListeners().remove(this);

        // Remove the button panorama
        tabPane.remove(buttonPanorama);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        TabPane tabPane = (TabPane)getComponent();
        Orientation tabOrientation = tabPane.getTabOrientation();
        Component corner = tabPane.getCorner();

        switch (tabOrientation) {
            case HORIZONTAL: {
                if (height != -1) {
                    if (corner != null
                        && corner.isDisplayable()) {
                        height = Math.max(height - Math.max(corner.getPreferredHeight(-1),
                            Math.max(buttonPanorama.getPreferredHeight(-1) - 1, 0)), 0);
                    } else {
                        height = Math.max(height - (buttonPanorama.getPreferredHeight(-1) - 1), 0);
                    }

                    height = Math.max(height - (padding.top + padding.bottom + 2), 0);
                }

                if (tabPane.getSelectedIndex() != -1) {
                    for (Component tab : tabPane.getTabs()) {
                        preferredWidth = Math.max(preferredWidth,
                            tab.getPreferredWidth(height));
                    }

                    preferredWidth += (padding.left + padding.right + 2);
                }

                int buttonAreaPreferredWidth = buttonPanorama.getPreferredWidth(-1);

                if (corner != null
                    && corner.isDisplayable()) {
                    buttonAreaPreferredWidth += corner.getPreferredWidth(-1);
                }

                preferredWidth = Math.max(preferredWidth,
                    buttonAreaPreferredWidth);

                break;
            }

            case VERTICAL: {
                if (height != -1) {
                    height = Math.max(height - (padding.top + padding.bottom + 2), 0);
                }

                if (tabPane.getSelectedIndex() != -1) {
                    for (Component tab : tabPane.getTabs()) {
                        preferredWidth = Math.max(preferredWidth,
                            tab.getPreferredWidth(height));
                    }

                    preferredWidth += (padding.left + padding.right + 2);
                } else {
                    preferredWidth += 1;
                }

                if (corner != null
                    && corner.isDisplayable()) {
                    preferredWidth += Math.max(corner.getPreferredWidth(-1),
                        Math.max(buttonPanorama.getPreferredWidth(-1) - 1, 0));
                } else {
                    preferredWidth += Math.max(buttonPanorama.getPreferredWidth(-1) - 1, 0);
                }

                break;
            }
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        TabPane tabPane = (TabPane)getComponent();
        Orientation tabOrientation = tabPane.getTabOrientation();
        Component corner = tabPane.getCorner();

        switch (tabOrientation) {
            case HORIZONTAL: {
                if (width != -1) {
                    width = Math.max(width - (padding.left + padding.right + 2), 0);
                }

                if (tabPane.getSelectedIndex() != -1) {
                    for (Component tab : tabPane.getTabs()) {
                        preferredHeight = Math.max(preferredHeight,
                            tab.getPreferredHeight(width));
                    }

                    preferredHeight += (padding.top + padding.bottom + 2);
                } else {
                    preferredHeight += 1;
                }

                if (corner != null
                    && corner.isDisplayable()) {
                    preferredHeight += Math.max(corner.getPreferredHeight(-1),
                        Math.max(buttonPanorama.getPreferredHeight(-1) - 1, 0));
                } else {
                    preferredHeight += Math.max(buttonPanorama.getPreferredHeight(-1) - 1, 0);
                }

                break;
            }

            case VERTICAL: {
                if (width != -1) {
                    if (corner != null
                        && corner.isDisplayable()) {
                        width = Math.max(width - Math.max(corner.getPreferredWidth(-1),
                            Math.max(buttonPanorama.getPreferredWidth(-1) - 1, 0)), 0);
                    } else {
                        width = Math.max(width - (buttonPanorama.getPreferredWidth(-1) - 1), 0);
                    }

                    width = Math.max(width - (padding.left + padding.right + 2), 0);
                }

                if (tabPane.getSelectedIndex() != -1) {
                    for (Component tab : tabPane.getTabs()) {
                        preferredHeight = Math.max(preferredHeight,
                            tab.getPreferredHeight(width));
                    }

                    preferredHeight += (padding.top + padding.bottom + 2);
                }

                int buttonAreaPreferredHeight = buttonPanorama.getPreferredHeight(-1);

                if (corner != null
                    && corner.isDisplayable()) {
                    buttonAreaPreferredHeight += corner.getPreferredHeight(-1);
                }

                preferredHeight = Math.max(preferredHeight,
                    buttonAreaPreferredHeight);

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
        Component corner = tabPane.getCorner();

        int width = getWidth();
        int height = getHeight();

        int tabX = 0;
        int tabY = 0;
        int tabWidth = 0;
        int tabHeight = 0;

        Dimensions buttonPanoramaPreferredSize = buttonPanorama.getPreferredSize();

        switch (tabPane.getTabOrientation()) {
            case HORIZONTAL: {
                int buttonPanoramaWidth = Math.min(width,
                    buttonPanoramaPreferredSize.width);
                int buttonPanoramaHeight = buttonPanoramaPreferredSize.height;
                int buttonPanoramaX = 0;
                int buttonPanoramaY = 0;

                if (corner != null) {
                    if (corner.isDisplayable()) {
                        int cornerWidth = width - buttonPanoramaWidth;
                        int cornerHeight = corner.getPreferredHeight(-1);
                        int cornerX = buttonPanoramaWidth;
                        int cornerY = Math.max(buttonPanoramaHeight - cornerHeight - 1, 0);

                        buttonPanoramaY = Math.max(cornerHeight - buttonPanoramaHeight + 1, 0);

                        corner.setVisible(true);
                        corner.setLocation(cornerX, cornerY);
                        corner.setSize(cornerWidth, cornerHeight);
                    } else {
                        corner.setVisible(false);
                    }
                }

                buttonPanorama.setLocation(buttonPanoramaX, buttonPanoramaY);
                buttonPanorama.setSize(buttonPanoramaWidth, buttonPanoramaHeight);

                tabX = padding.left + 1;
                tabY = padding.top + buttonPanoramaY + buttonPanoramaHeight;
                tabWidth = Math.max(width - (padding.left + padding.right + 2), 0);
                tabHeight = Math.max(height - (padding.top + padding.bottom
                    + buttonPanoramaY + buttonPanoramaHeight + 1), 0);

                break;
            }

            case VERTICAL: {
                int buttonPanoramaWidth = buttonPanoramaPreferredSize.width;
                int buttonPanoramaHeight = Math.min(height,
                    buttonPanoramaPreferredSize.height);
                int buttonPanoramaX = 0;
                int buttonPanoramaY = 0;

                if (corner != null) {
                    if (corner.isDisplayable()) {
                        int cornerWidth = corner.getPreferredWidth(-1);
                        int cornerHeight = height - buttonPanoramaHeight;
                        int cornerX = Math.max(buttonPanoramaWidth - cornerWidth - 1, 0);
                        int cornerY = buttonPanoramaHeight;

                        buttonPanoramaX = Math.max(cornerWidth - buttonPanoramaWidth + 1, 0);

                        corner.setVisible(true);
                        corner.setLocation(cornerX, cornerY);
                        corner.setSize(cornerWidth, cornerHeight);
                    } else {
                        corner.setVisible(false);
                    }
                }

                buttonPanorama.setLocation(buttonPanoramaX, buttonPanoramaY);
                buttonPanorama.setSize(buttonPanoramaWidth, buttonPanoramaHeight);

                tabX = padding.left + buttonPanoramaX + buttonPanoramaWidth;
                tabY = padding.top + 1;
                tabWidth = Math.max(width - (padding.left + padding.right
                    + buttonPanoramaX + buttonPanoramaWidth + 1), 0);
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

    @Override
    public void paint(Graphics2D graphics) {
        TabPane tabPane = (TabPane)getComponent();

        Bounds tabPaneBounds = tabPane.getBounds();

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
                y = Math.max(buttonPanorama.getY() + buttonPanorama.getHeight() - 1, 0);
                width = tabPaneBounds.width;
                height = Math.max(tabPaneBounds.height - y, 0);

                break;
            }

            case VERTICAL: {
                x = Math.max(buttonPanorama.getX() + buttonPanorama.getWidth() - 1, 0);
                y = 0;
                width = Math.max(tabPaneBounds.width - x, 0);
                height = tabPaneBounds.height;

                break;
            }
        }

        Bounds contentBounds = new Bounds(x, y, width, height);

        if (!contentBounds.isEmpty()) {
            // If a tab is selected, paint the active background color; otherwise,
            // paint the inactive background color
            int selectedIndex = tabPane.getSelectedIndex();
            graphics.setPaint((selectedIndex == -1) ? inactiveTabColor : activeTabColor);
            graphics.fillRect(contentBounds.x, contentBounds.y,
                contentBounds.width, contentBounds.height);

            // Draw the border
            graphics.setPaint(borderColor);
            graphics.drawRect(contentBounds.x, contentBounds.y,
                Math.max(contentBounds.width - 1, 0),
                Math.max(contentBounds.height - 1, 0));

            // Draw the bevel for vertical tabs
            if (tabOrientation == Orientation.VERTICAL) {
                graphics.setPaint(buttonBevelColor);
                graphics.drawLine(contentBounds.x + 1, contentBounds.y + 1,
                    contentBounds.x + contentBounds.width - 1,
                    contentBounds.y + 1);
            }
        }
    }

    public Color getActiveTabColor() {
        return activeTabColor;
    }

    public void setActiveTabColor(Color activeTabColor) {
        if (activeTabColor == null) {
            throw new IllegalArgumentException("activeTabColor is null.");
        }

        this.activeTabColor = activeTabColor;

        repaintComponent();
    }

    public final void setActiveTabColor(String activeTabColor) {
        if (activeTabColor == null) {
            throw new IllegalArgumentException("activeTabColor is null.");
        }

        setActiveTabColor(Color.decode(activeTabColor));
    }

    public Color getInactiveTabColor() {
        return inactiveTabColor;
    }

    public void setInactiveTabColor(Color inactiveTabColor) {
        if (inactiveTabColor == null) {
            throw new IllegalArgumentException("inactiveTabColor is null.");
        }

        this.inactiveTabColor = inactiveTabColor;
        repaintComponent();
    }

    public final void setInactiveTabColor(String inactiveTabColor) {
        if (inactiveTabColor == null) {
            throw new IllegalArgumentException("inactiveTabColor is null.");
        }

        setInactiveTabColor(Color.decode(inactiveTabColor));
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        this.borderColor = borderColor;
        buttonPanorama.getStyles().put("buttonBackgroundColor", borderColor);
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        setBorderColor(Color.decode(borderColor));
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

    public Font getButtonFont() {
        return buttonFont;
    }

    public void setButtonFont(Font buttonFont) {
        if (buttonFont == null) {
            throw new IllegalArgumentException("buttonFont is null.");
        }

        this.buttonFont = buttonFont;
        invalidateComponent();
    }

    public final void setButtonFont(String buttonFont) {
        if (buttonFont == null) {
            throw new IllegalArgumentException("buttonFont is null.");
        }

        setButtonFont(Font.decode(buttonFont));
    }

    public Color getButtonColor() {
        return buttonColor;
    }

    public void setButtonColor(Color buttonColor) {
        if (buttonColor == null) {
            throw new IllegalArgumentException("buttonColor is null.");
        }

        this.buttonColor = buttonColor;
        repaintComponent();
    }

    public final void setButtonColor(String buttonColor) {
        if (buttonColor == null) {
            throw new IllegalArgumentException("buttonColor is null.");
        }

        setButtonColor(Color.decode(buttonColor));
    }

    public Color getButtonBevelColor() {
        return buttonBevelColor;
    }

    public void setButtonBevelColor(Color buttonBevelColor) {
        if (buttonBevelColor == null) {
            throw new IllegalArgumentException("buttonBevelColor is null.");
        }

        this.buttonBevelColor = buttonBevelColor;
        repaintComponent();
    }

    public final void setButtonBevelColor(String buttonBevelColor) {
        if (buttonBevelColor == null) {
            throw new IllegalArgumentException("buttonBevelColor is null.");
        }

        setButtonBevelColor(Color.decode(buttonBevelColor));
    }

    public Color getPressedButtonBevelColor() {
        return pressedButtonBevelColor;
    }

    public void setPressedButtonBevelColor(Color pressedButtonBevelColor) {
        if (pressedButtonBevelColor == null) {
            throw new IllegalArgumentException("pressedButtonBevelColor is null.");
        }

        this.pressedButtonBevelColor = pressedButtonBevelColor;
        repaintComponent();
    }

    public final void setPressedButtonBevelColor(String pressedButtonBevelColor) {
        if (pressedButtonBevelColor == null) {
            throw new IllegalArgumentException("pressedButtonBevelColor is null.");
        }

        setPressedButtonBevelColor(Color.decode(pressedButtonBevelColor));
    }

    public Insets getButtonPadding() {
        return buttonPadding;
    }

    public void setButtonPadding(Insets buttonPadding) {
        if (buttonPadding == null) {
            throw new IllegalArgumentException("buttonPadding is null.");
        }

        this.buttonPadding = buttonPadding;
        invalidateComponent();
    }

    public final void setButtonPadding(int buttonPadding) {
        setButtonPadding(new Insets(buttonPadding));
    }

    public int getButtonSpacing() {
        return (Integer)buttonFlowPane.getStyles().get("spacing");
    }

    public void setButtonSpacing(int buttonSpacing) {
        buttonFlowPane.getStyles().put("spacing", buttonSpacing);
    }

    protected void updateButtonData(Component tab) {
        TabPane tabPane = (TabPane)getComponent();
        int tabIndex = tabPane.getTabs().indexOf(tab);

        if (tabIndex != -1) {
            TabButton tabButton =
                (TabButton)buttonFlowPane.get(tabIndex);

            tabButton.setButtonData(new ButtonData(TabPane.getIcon(tab),
                TabPane.getName(tab)));
        }
    }

    // TabPaneListener methods

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
        TabButton tabButton = new TabButton(new ButtonData(TabPane.getIcon(tab),
            TabPane.getName(tab)));
        tabButton.setGroup(tabButtonGroup);

        buttonFlowPane.insert(tabButton, index);
    }

    public void tabsRemoved(TabPane tabPane, int index, Sequence<Component> tabs) {
        // Remove the buttons
        Sequence<Component> removed = buttonFlowPane.remove(index, tabs.getLength());

        for (int i = 0, n = removed.getLength(); i < n; i++) {
            TabButton tabButton = (TabButton)removed.get(i);
            tabButton.setGroup((Group)null);
        }
    }

    public void cornerChanged(TabPane tabPane, Component previousCorner) {
        invalidateComponent();
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
            Button button = (Button)buttonFlowPane.get(selectedIndex);
            button.setSelected(true);
        }

        invalidateComponent();
    }

    // Tab pane attribute events
    public void nameChanged(TabPane tabPane, Component component, String previousName) {
        updateButtonData(component);
    }

    public void iconChanged(TabPane tabPane, Component component, Image previousIcon) {
        updateButtonData(component);
    }

    // Button group events
    public void selectionChanged(Group group, Button previousSelection) {
        Button button = tabButtonGroup.getSelection();
        int index = (button == null) ? -1 : buttonFlowPane.indexOf(button);

        TabPane tabPane = (TabPane)getComponent();
        tabPane.setSelectedIndex(index);
    }
}
