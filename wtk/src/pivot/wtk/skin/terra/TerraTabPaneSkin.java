/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
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

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

import pivot.collections.Dictionary;
import pivot.collections.Sequence;
import pivot.util.Vote;
import pivot.wtk.Button;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.FlowPane;
import pivot.wtk.GraphicsUtilities;
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
import pivot.wtk.Theme;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.Button.Group;
import pivot.wtk.content.ButtonData;
import pivot.wtk.content.ButtonDataRenderer;
import pivot.wtk.effects.ClipDecorator;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;
import pivot.wtk.effects.easing.Easing;
import pivot.wtk.effects.easing.Quadratic;
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
 * TODO Add showCloseButton style.
 *
 * @author gbrown
 */
public class TerraTabPaneSkin extends ContainerSkin
    implements TabPaneListener, TabPaneSelectionListener, TabPaneAttributeListener,
        Button.GroupListener {
    protected class TabButton extends Button {
    	private boolean active = false;

    	public TabButton() {
            this(null);
        }

        public TabButton(Object buttonData) {
            super(buttonData);

            super.setToggleButton(true);
            setDataRenderer(DEFAULT_DATA_RENDERER);

            setSkin(new TabButtonSkin());
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
            setSelected(collapsible ? !isSelected() : true);
            super.press();
        }
    }

    protected class TabButtonSkin extends ButtonSkin {
        public int getPreferredWidth(int height) {
            TabButton tabButton = (TabButton)getComponent();

            Button.DataRenderer dataRenderer = tabButton.getDataRenderer();
            dataRenderer.render(tabButton.getButtonData(), tabButton, false);

            // Include padding in constraint
            if (height != -1) {
                height = Math.max(height - (buttonPadding.top + buttonPadding.bottom + 2), 0);
            }

            int preferredWidth = 0;
            switch (tabOrientation) {
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

            Button.DataRenderer dataRenderer = tabButton.getDataRenderer();
            dataRenderer.render(tabButton.getButtonData(), tabButton, false);

            // Include padding in constraint
            if (width != -1) {
                width = Math.max(width - (buttonPadding.left + buttonPadding.right + 2), 0);
            }

            int preferredHeight = 0;
            switch (tabOrientation) {
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

            Button.DataRenderer dataRenderer = tabButton.getDataRenderer();
            dataRenderer.render(tabButton.getButtonData(), tabButton, false);

            Dimensions preferredContentSize = dataRenderer.getPreferredSize();

            int preferredWidth = 0;
            int preferredHeight = 0;
            switch (tabOrientation) {
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

            Color backgroundColor = (tabButton.isSelected()
                || tabButton.active) ?
                activeTabColor : inactiveTabColor;

            int width = getWidth();
            int height = getHeight();

            // Draw the background
            graphics.setPaint(backgroundColor);
            graphics.fillRect(0, 0, width, height);

            // Draw the bevel
            graphics.setPaint(new GradientPaint(width / 2, 1, buttonBevelColor,
                width / 2, GRADIENT_BEVEL_THICKNESS, backgroundColor));

            switch(tabOrientation) {
                case HORIZONTAL: {
                    graphics.fillRect(1, 1, width - 2, GRADIENT_BEVEL_THICKNESS);
                    break;
                }

                case VERTICAL: {
                    graphics.fillRect(1, 1, width - 1, GRADIENT_BEVEL_THICKNESS);
                    break;
                }
            }

            // Draw the border
            graphics.setPaint(borderColor);

            if (tabButton.isSelected()
                || tabButton.active) {
                switch(tabOrientation) {
                    case HORIZONTAL: {
                        GraphicsUtilities.drawLine(graphics, 0, 0, height, Orientation.VERTICAL);
                        GraphicsUtilities.drawLine(graphics, 0, 0, width, Orientation.HORIZONTAL);
                        GraphicsUtilities.drawLine(graphics, width - 1, 0, height, Orientation.VERTICAL);
                        break;
                    }

                    case VERTICAL: {
                        GraphicsUtilities.drawLine(graphics, 0, 0, width, Orientation.HORIZONTAL);
                        GraphicsUtilities.drawLine(graphics, 0, 0, height, Orientation.VERTICAL);
                        GraphicsUtilities.drawLine(graphics, 0, height - 1, width, Orientation.HORIZONTAL);
                        break;
                    }
                }
            } else {
                GraphicsUtilities.drawRect(graphics, 0, 0, width, height);
            }

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
        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            boolean consumed = super.mouseClick(component, button, x, y, count);

            TabButton tabButton = (TabButton)getComponent();
            tabButton.press();

            return consumed;
        }

        public Font getFont() {
        	return buttonFont;
        }

        public Color getColor() {
        	return buttonColor;
        }

        public Color getDisabledColor() {
        	return buttonColor;
        }
    }

    public class ExpandTransition extends Transition {
    	boolean collapse;
        private Easing easing = new Quadratic();
        private ClipDecorator clipDecorator = new ClipDecorator();

        public ExpandTransition(boolean collapse, int duration, int rate) {
            super(duration, rate, false);
            this.collapse = collapse;
        }

        public float getScale() {
            int elapsedTime = getElapsedTime();
            int duration = getDuration();

            float scale;
            if (collapse) {
                scale = easing.easeIn(elapsedTime, 1, -1, duration);
            } else {
                scale = easing.easeOut(elapsedTime, 0, 1, duration);
            }

            return scale;
        }

        @Override
        public void start(TransitionListener transitionListener) {
        	TabPane tabPane = (TabPane)getComponent();
        	Component selectedTab = tabPane.getSelectedTab();
        	selectedTab.getDecorators().add(clipDecorator);

        	int selectedIndex = tabPane.getSelectedIndex();
        	TabButton tabButton = (TabButton)buttonFlowPane.get(selectedIndex);
        	tabButton.active = true;

        	getComponent().setEnabled(false);

            super.start(transitionListener);
        }

        @Override
        public void stop() {
        	TabPane tabPane = (TabPane)getComponent();
        	Component selectedTab = tabPane.getSelectedTab();
        	selectedTab.getDecorators().remove(clipDecorator);

        	int selectedIndex = tabPane.getSelectedIndex();
        	TabButton tabButton = (TabButton)buttonFlowPane.get(selectedIndex);
        	tabButton.active = false;

        	getComponent().setEnabled(true);

        	super.stop();
        }

        @Override
        protected void update() {
            invalidateComponent();
        }
    }

    protected Panorama buttonPanorama = new Panorama();
    protected FlowPane buttonFlowPane = new FlowPane();
    private Button.Group tabButtonGroup = new Button.Group();

    private Color activeTabColor;
    private Color inactiveTabColor;
    private Color borderColor;
    private Insets padding;
    private Font buttonFont;
    private Color buttonColor;
    private Insets buttonPadding;

    // Derived colors
    private Color buttonBevelColor;

    private boolean collapsible = false;
    private Orientation tabOrientation = Orientation.HORIZONTAL;

    private ExpandTransition expandTransition = null;

    private static final int EXPAND_DURATION = 250;
    private static final int EXPAND_RATE = 30;

	public static final int GRADIENT_BEVEL_THICKNESS = 4;
	private static final Button.DataRenderer DEFAULT_DATA_RENDERER = new ButtonDataRenderer();

    public TerraTabPaneSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        activeTabColor = theme.getColor(11);
        inactiveTabColor = theme.getColor(9);
        borderColor = theme.getColor(7);
        padding = new Insets(6);
        buttonFont = theme.getFont();
        buttonColor = theme.getColor(1);
        buttonPadding = new Insets(3, 4, 3, 4);

        // Set the derived colors
        buttonBevelColor = TerraTheme.brighten(inactiveTabColor);

        tabButtonGroup.getGroupListeners().add(this);

        buttonFlowPane.getStyles().put("spacing", 2);
    }

	@Override
	public void setSize(int width, int height) {
		if (expandTransition != null) {
			if ((tabOrientation == Orientation.HORIZONTAL && width != getWidth())
				|| (tabOrientation == Orientation.VERTICAL && height != getHeight())) {
				expandTransition.end();
				expandTransition = null;
			}
		}

		super.setSize(width, height);
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

        // Add buttons for all existing tabs
        for (Component tab : tabPane.getTabs()) {
            TabButton tabButton = new TabButton(new ButtonData(TabPane.getIcon(tab),
                TabPane.getName(tab)));
            tabButton.setGroup(tabButtonGroup);

            buttonFlowPane.add(tabButton);
        }

        selectedIndexChanged(tabPane, -1);
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
        int preferredWidth;

        TabPane tabPane = (TabPane)getComponent();
        if (expandTransition == null
    		|| tabOrientation == Orientation.VERTICAL) {
        	preferredWidth = 0;

            Component selectedTab = tabPane.getSelectedTab();
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

                    if (selectedTab != null) {
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

                    if (selectedTab != null) {
                    	if (expandTransition == null) {
                        	int contentWidth = 0;
                            for (Component tab : tabPane.getTabs()) {
                                contentWidth = Math.max(contentWidth,
                                    tab.getPreferredWidth(height));
                            }

                            preferredWidth += (padding.left + padding.right + contentWidth);
                    	} else {
                            float scale = expandTransition.getScale();
                            preferredWidth += (int)(scale * (float)(padding.left + padding.right
                        		+ selectedTab.getWidth()));
                    	}

                        preferredWidth += 2;
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
        } else {
        	preferredWidth = getWidth();
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight;

        TabPane tabPane = (TabPane)getComponent();
        if (expandTransition == null
    		|| tabOrientation == Orientation.HORIZONTAL) {
        	preferredHeight = 0;
            Component selectedTab = tabPane.getSelectedTab();
            Component corner = tabPane.getCorner();

            switch (tabOrientation) {
                case HORIZONTAL: {
                    if (width != -1) {
                        width = Math.max(width - (padding.left + padding.right + 2), 0);
                    }

                    if (selectedTab != null) {
                    	if (expandTransition == null) {
                    		int contentHeight = 0;
                            for (Component tab : tabPane.getTabs()) {
                            	contentHeight = Math.max(contentHeight,
                                    tab.getPreferredHeight(width));
                            }

                            preferredHeight += (padding.top + padding.bottom + contentHeight);
                    	} else {
                    		float scale = expandTransition.getScale();
                            preferredHeight += (int)(scale * (float)(padding.top + padding.bottom
                        		+ selectedTab.getHeight()));
                    	}

                        preferredHeight += 2;
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

                    if (selectedTab != null) {
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
        } else {
        	preferredHeight = getHeight();
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

        int tabX = 0;
        int tabY = 0;
        int tabWidth = 0;
        int tabHeight = 0;

        Component selectedTab = tabPane.getSelectedTab();
        Component corner = tabPane.getCorner();

        Dimensions buttonPanoramaSize;
        if (expandTransition == null) {
        	buttonPanoramaSize = buttonPanorama.getPreferredSize();
        } else {
        	buttonPanoramaSize = buttonPanorama.getSize();
        }

        switch (tabOrientation) {
            case HORIZONTAL: {
                int buttonPanoramaWidth = Math.min(width,
                    buttonPanoramaSize.width);
                int buttonPanoramaHeight = buttonPanoramaSize.height;
                int buttonPanoramaX = 0;
                int buttonPanoramaY = 0;

                if (corner != null) {
                    if (corner.isDisplayable()) {
                        int cornerWidth = width - buttonPanoramaWidth;
                        int cornerHeight = Math.max(corner.getPreferredHeight(-1), buttonPanoramaSize.height - 1);
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
                int buttonPanoramaWidth = buttonPanoramaSize.width;
                int buttonPanoramaHeight = Math.min(height,
                    buttonPanoramaSize.height);
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

        if (expandTransition == null) {
            for (Component tab : tabPane.getTabs()) {
                if (tab == selectedTab) {
                    // Show the selected tab
                    tab.setVisible(true);

                    // Set the tab's size and location
                    tab.setLocation(tabX, tabY);
                    tab.setSize(tabWidth, tabHeight);
                } else {
                    tab.setVisible(false);
                }
            }
        } else {
        	if (expandTransition.isRunning()) {
            	// Update the clip
            	expandTransition.clipDecorator.setWidth(tabWidth);
            	expandTransition.clipDecorator.setHeight(tabHeight);
        	} else {
            	switch (tabOrientation) {
            		case HORIZONTAL: {
            			tabHeight = 0;
            			for (Component tab : tabPane.getTabs()) {
            				tabHeight = Math.max(tab.getPreferredHeight(tabWidth), tabHeight);
            			}

            			break;
            		}

            		case VERTICAL: {
            			tabWidth = 0;
            			for (Component tab : tabPane.getTabs()) {
            				tabWidth = Math.max(tab.getPreferredWidth(tabHeight), tabWidth);
            			}

            			break;
            		}
            	}

                selectedTab.setLocation(tabX, tabY);
                selectedTab.setSize(tabWidth, tabHeight);

            	selectedTab.setVisible(true);
        	}

        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        TabPane tabPane = (TabPane)getComponent();

        Bounds tabPaneBounds = tabPane.getBounds();

        // Call the base class to paint the background
        super.paint(graphics);

        // Paint the content background and border
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;

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
            GraphicsUtilities.drawRect(graphics, contentBounds.x, contentBounds.y,
                contentBounds.width, contentBounds.height);

            // Draw the bevel for vertical tabs
            if (tabOrientation == Orientation.VERTICAL) {
	            graphics.setPaint(new GradientPaint(width / 2, contentBounds.y + 1, buttonBevelColor,
	                width / 2, contentBounds.y + 1 + GRADIENT_BEVEL_THICKNESS, activeTabColor));
	            graphics.fillRect(contentBounds.x + 1, contentBounds.y + 1,
                    contentBounds.width - 2, GRADIENT_BEVEL_THICKNESS);
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

        setActiveTabColor(GraphicsUtilities.decodeColor(activeTabColor));
    }

    public Color getInactiveTabColor() {
        return inactiveTabColor;
    }

    public void setInactiveTabColor(Color inactiveTabColor) {
        if (inactiveTabColor == null) {
            throw new IllegalArgumentException("inactiveTabColor is null.");
        }

        this.inactiveTabColor = inactiveTabColor;
        buttonBevelColor = TerraTheme.brighten(inactiveTabColor);
        repaintComponent();
    }

    public final void setInactiveTabColor(String inactiveTabColor) {
        if (inactiveTabColor == null) {
            throw new IllegalArgumentException("inactiveTabColor is null.");
        }

        setInactiveTabColor(GraphicsUtilities.decodeColor(inactiveTabColor));
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

        setBorderColor(GraphicsUtilities.decodeColor(borderColor));
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

        setButtonColor(GraphicsUtilities.decodeColor(buttonColor));
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

    public Orientation getTabOrientation() {
        return tabOrientation;
    }

    public void setTabOrientation(Orientation tabOrientation) {
        if (tabOrientation == null) {
            throw new IllegalArgumentException("tabOrientation is null.");
        }

        this.tabOrientation = tabOrientation;

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

    public void setTabOrientation(String tabOrientation) {
        if (tabOrientation == null) {
            throw new IllegalArgumentException("tabOrientation is null.");
        }

        setTabOrientation(Orientation.decode(tabOrientation));
    }

    public boolean isCollapsible() {
        return collapsible;
    }

    public void setCollapsible(boolean collapsible) {
        this.collapsible = collapsible;
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

    // Tab pane events
    public void tabInserted(TabPane tabPane, int index) {
    	if (expandTransition != null) {
    		expandTransition.stop();
    		expandTransition = null;
    	}

        // Create a new button for the tab
        Component tab = tabPane.getTabs().get(index);
        TabButton tabButton = new TabButton(new ButtonData(TabPane.getIcon(tab),
            TabPane.getName(tab)));
        tabButton.setGroup(tabButtonGroup);

        buttonFlowPane.insert(tabButton, index);
    }

    public void tabsRemoved(TabPane tabPane, int index, Sequence<Component> tabs) {
    	if (expandTransition != null) {
    		expandTransition.stop();
    		expandTransition = null;
    	}

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
	public Vote previewSelectedIndexChange(final TabPane tabPane, final int selectedIndex) {
        Vote vote = Vote.APPROVE;

        if (tabPane.isShowing()) {
            if (expandTransition == null) {
                if (selectedIndex == -1) {
                    expandTransition = new ExpandTransition(true, EXPAND_DURATION, EXPAND_RATE);

                    layout();
                    expandTransition.start(new TransitionListener() {
                        public void transitionCompleted(Transition transition) {
                            tabPane.setSelectedIndex(-1);
                            expandTransition = null;
                        }
                    });

                    vote = Vote.DEFER;
                }
            } else {
            	if (expandTransition.isRunning()) {
                	vote = Vote.DEFER;
            	}
            }
        }

        return vote;
	}

	public void selectedIndexChangeVetoed(TabPane tabPane, Vote reason) {
        if (reason == Vote.DENY
            && expandTransition != null) {
            expandTransition.stop();
            expandTransition = null;
            invalidateComponent();
        }
	}

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

            if (previousSelectedIndex == -1) {
                if (tabPane.isShowing()) {
                    expandTransition = new ExpandTransition(false, EXPAND_DURATION, EXPAND_RATE);

                    layout();
                    expandTransition.start(new TransitionListener() {
                        public void transitionCompleted(Transition transition) {
                            expandTransition = null;
                        }
                    });
                }
            }
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

    public void closeableChanged(TabPane tabPane, Component component) {
        // TODO
    }

    // Button group events
    public void selectionChanged(Group group, Button previousSelection) {
        Button button = tabButtonGroup.getSelection();
        int index = (button == null) ? -1 : buttonFlowPane.indexOf(button);

        TabPane tabPane = (TabPane)getComponent();
        tabPane.setSelectedIndex(index);
    }
}
