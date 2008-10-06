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

import pivot.collections.Dictionary;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Expander;
import pivot.wtk.ExpanderListener;
import pivot.wtk.Dimensions;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Insets;
import pivot.wtk.Label;
import pivot.wtk.Mouse;
import pivot.wtk.Orientation;
import pivot.wtk.PushButton;
import pivot.wtk.FlowPane;
import pivot.wtk.Theme;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.media.Image;
import pivot.wtk.skin.ContainerSkin;

/**
 * Expander skin.
 *
 * @author gbrown
 */
public class TerraExpanderSkin extends ContainerSkin
    implements ButtonPressListener, ExpanderListener {
    /**
     * Expander shade button component.
     *
     * @author tvolkert
     */
    public static class ShadeButton extends PushButton {
        private Expander expander = null;

        public ShadeButton(Expander expander) {
            this(expander, null);
        }

        public ShadeButton(Expander expander, Object buttonData) {
            super(buttonData);

            this.expander = expander;

            installSkin(ShadeButton.class);
        }

        public Expander getExpander() {
            return expander;
        }
    }

    /**
     * Expander shade button component skin.
     *
     * @author tvolkert
     */
    public static class ShadeButtonSkin extends TerraPushButtonSkin {
        public ShadeButtonSkin() {
            super();

            setPadding(new Insets(2));
        }

        @Override
        public void install(Component component) {
            super.install(component);

            ShadeButton shadeButton = (ShadeButton)component;
            Expander expander = shadeButton.getExpander();

            Color shadeButtonBackgroundColor = (Color)expander.getStyles().get("shadeButtonBackgroundColor");
            setBackgroundColor(shadeButtonBackgroundColor);
            setDisabledBackgroundColor(shadeButtonBackgroundColor);

            setBevelColor(shadeButtonBackgroundColor);
            setPressedBevelColor(shadeButtonBackgroundColor);
            setDisabledBevelColor(shadeButtonBackgroundColor);

            Color borderColor = (Color)expander.getStyles().get("borderColor");
            setBorderColor(borderColor);
        }

        @Override
        public boolean isFocusable() {
            return false;
        }
    }

    protected abstract class ButtonImage extends Image {
        public int getWidth() {
            return 6;
        }

        public int getHeight() {
            return 6;
        }
    }

    protected class CollapseImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            graphics.setPaint(shadeButtonColor);
            graphics.fillRect(0, 0, 6, 2);
        }
    }

    protected class ExpandImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            graphics.setPaint(shadeButtonColor);
            graphics.drawRect(0, 0, 5, 5);
        }
    }

    private class TitleBarMouseHandler
        implements ComponentMouseButtonListener {
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            if (count == 2) {
                Expander expander = (Expander)getComponent();
                expander.setExpanded(!expander.isExpanded());
            }
        }
    }

    private Image collapseImage = new CollapseImage();
    private Image expandImage = new ExpandImage();

    private FlowPane titleBarFlowPane = null;
    private FlowPane titleFlowPane = null;
    private FlowPane buttonFlowPane = null;

    private Label titleLabel = new Label();
    private ShadeButton shadeButton = null;

    private TitleBarMouseHandler titleBarMouseHandler = new TitleBarMouseHandler();

    private Color titleBarBackgroundColor;
    private Color titleBarBorderColor;
    private Color titleBarBevelColor;
    private Color shadeButtonColor;
    private Color shadeButtonBackgroundColor;
    private Color borderColor;
    private Insets padding;

    public TerraExpanderSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(1));

        titleBarBackgroundColor = theme.getColor(5);
        titleBarBorderColor = theme.getColor(3);
        titleBarBevelColor = theme.getColor(6);
        shadeButtonColor = theme.getColor(10);
        shadeButtonBackgroundColor = theme.getColor(1);
        borderColor = theme.getColor(4);
        padding = new Insets(4);

        titleBarFlowPane = new FlowPane(Orientation.HORIZONTAL);
        titleBarFlowPane.getComponentMouseButtonListeners().add(titleBarMouseHandler);

        titleBarFlowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.JUSTIFY);
        titleBarFlowPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        titleBarFlowPane.getStyles().put("padding", new Insets(3));
        titleBarFlowPane.getStyles().put("spacing", 3);

        titleFlowPane = new FlowPane(Orientation.HORIZONTAL);
        titleFlowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);

        buttonFlowPane = new FlowPane(Orientation.HORIZONTAL);
        buttonFlowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);

        titleBarFlowPane.add(titleFlowPane);
        titleBarFlowPane.add(buttonFlowPane);

        titleLabel.getStyles().put("color", theme.getColor(7));
        titleLabel.getStyles().put("fontBold", true);
        titleFlowPane.add(titleLabel);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Expander expander = (Expander)component;
        expander.getExpanderListeners().add(this);
        expander.add(titleBarFlowPane);

        Image buttonData = expander.isExpanded() ? collapseImage : expandImage;
        shadeButton = new ShadeButton(expander, buttonData);
        buttonFlowPane.add(shadeButton);

        shadeButton.getButtonPressListeners().add(this);

        titleChanged(expander, null);
    }

    public void uninstall() {
        Expander expander = (Expander)getComponent();
        expander.getExpanderListeners().remove(this);
        expander.remove(titleBarFlowPane);

        shadeButton.getButtonPressListeners().remove(this);
        buttonFlowPane.remove(shadeButton);
        shadeButton = null;

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 2;

        Expander expander = (Expander)getComponent();
        Component contentComponent = expander.getContent();

        int titleBarPreferredWidth = titleBarFlowPane.getPreferredWidth(-1);
        int contentPreferredWidth = 0;

        if (contentComponent != null) {
            int contentHeightConstraint = -1;

            if (height >= 0) {
                int reservedHeight = 2 + padding.top + padding.bottom
                    + titleBarFlowPane.getPreferredHeight(-1);

                if (expander.isExpanded()) {
                    // Title bar border is only drawn when expander is expanded
                    reservedHeight += 1;
                }

                contentHeightConstraint = Math.max(height - reservedHeight, 0);
            }

            contentPreferredWidth = padding.left + padding.right
                + contentComponent.getPreferredWidth(contentHeightConstraint);
        }

        preferredWidth += Math.max(titleBarPreferredWidth, contentPreferredWidth);

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 2 + titleBarFlowPane.getPreferredHeight(-1);

        Expander expander = (Expander)getComponent();

        if (expander.isExpanded()) {
            // Title bar border is only drawn when expander is expanded
            preferredHeight += 1;

            Component contentComponent = expander.getContent();
            if (contentComponent != null) {
                int contentWidthConstraint = -1;

                if (width >= 0) {
                    int reservedWidth = 2 + padding.left + padding.right;
                    contentWidthConstraint = Math.max(width - reservedWidth, 0);
                }

                preferredHeight += padding.top + padding.bottom
                    + contentComponent.getPreferredHeight(contentWidthConstraint);
            }
        }

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        // TODO Optimize
        return new Dimensions(this.getPreferredWidth(-1),
            this.getPreferredHeight(-1));
    }

    public void layout() {
        Expander expander = (Expander)getComponent();
        Component contentComponent = expander.getContent();

        int width = getWidth();
        int height = getHeight();
        int titleBarHeight = titleBarFlowPane.getPreferredHeight(-1);

        titleBarFlowPane.setSize(Math.max(width - 2, 0), titleBarHeight);
        titleBarFlowPane.setLocation(1, 1);

        if (contentComponent != null) {
            if (expander.isExpanded()) {
                contentComponent.setVisible(true);

                int reservedWidth = 2 + padding.left + padding.right;
                int contentWidth = Math.max(width - reservedWidth, 0);

                int reservedHeight = 3 + padding.top + padding.bottom + titleBarHeight;
                int contentHeight = Math.max(height - reservedHeight, 0);

                contentComponent.setSize(contentWidth, contentHeight);

                int contentX = 1 + padding.left;
                int contentY = 2 + padding.top + titleBarHeight;

                contentComponent.setLocation(contentX, contentY);
            } else {
                contentComponent.setVisible(false);
            }
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        int width = getWidth();
        int height = getHeight();

        graphics.setStroke(new BasicStroke());

        Expander expander = (Expander)getComponent();
        if (expander.isExpanded()) {
            int titleBarHeight = titleBarFlowPane.getPreferredHeight(-1);

            graphics.setPaint(titleBarBorderColor);
            graphics.drawLine(0, 1 + titleBarHeight, width - 1, 1 + titleBarHeight);
        }

        graphics.setPaint(titleBarBackgroundColor);
        graphics.fillRect(titleBarFlowPane.getX(), titleBarFlowPane.getY(),
            titleBarFlowPane.getWidth(), titleBarFlowPane.getHeight());

        graphics.setPaint(borderColor);
        graphics.drawRect(0, 0, width - 1, height - 1);

        graphics.setPaint(titleBarBevelColor);
        graphics.drawLine(titleBarFlowPane.getX(), titleBarFlowPane.getY(),
            titleBarFlowPane.getWidth(), titleBarFlowPane.getY());
    }

    public Font getTitleBarFont() {
        return (Font)titleLabel.getStyles().get("font");
    }

    public void setTitleBarFont(Font titleBarFont) {
        titleLabel.getStyles().put("font", titleBarFont);
    }

    public final void setTitleBarFont(String titleBarFont) {
        if (titleBarFont == null) {
            throw new IllegalArgumentException("titleBarFont is null.");
        }

        setTitleBarFont(Font.decode(titleBarFont));
    }

    public Color getTitleBarColor() {
        return (Color)titleLabel.getStyles().get("color");
    }

    public void setTitleBarColor(Color titleBarColor) {
        titleLabel.getStyles().put("color", titleBarColor);
    }

    public final void setTitleBarColor(String titleBarColor) {
        if (titleBarColor == null) {
            throw new IllegalArgumentException("titleBarColor is null.");
        }

        setTitleBarColor(Color.decode(titleBarColor));
    }

    public Color getTitleBarBackgroundColor() {
        return titleBarBackgroundColor;
    }

    public void setTitleBarBackgroundColor(Color titleBarBackgroundColor) {
        this.titleBarBackgroundColor = titleBarBackgroundColor;
    }

    public final void setTitleBarBackgroundColor(String titleBarBackgroundColor) {
        if (titleBarBackgroundColor == null) {
            throw new IllegalArgumentException("titleBarBackgroundColor is null.");
        }

        setTitleBarBackgroundColor(Color.decode(titleBarBackgroundColor));
    }

    public Color getTitleBarBorderColor() {
        return titleBarBorderColor;
    }

    public void setTitleBarBorderColor(Color titleBarBorderColor) {
        this.titleBarBorderColor = titleBarBorderColor;
        repaintComponent();
    }

    public final void setTitleBarBorderColor(String titleBarBorderColor) {
        if (titleBarBorderColor == null) {
            throw new IllegalArgumentException("titleBarBorderColor is null.");
        }

        setTitleBarBorderColor(Color.decode(titleBarBorderColor));
    }

    public Color getShadeButtonColor() {
        return shadeButtonColor;
    }

    public void setShadeButtonColor(Color shadeButtonColor) {
        this.shadeButtonColor = shadeButtonColor;
        repaintComponent();
    }

    public final void setShadeButtonColor(String shadeButtonColor) {
        if (shadeButtonColor == null) {
            throw new IllegalArgumentException("shadeButtonColor is null.");
        }

        setShadeButtonColor(Color.decode(shadeButtonColor));
    }

    public Color getShadeButtonBackgroundColor() {
        return shadeButtonBackgroundColor;
    }

    public void setShadeButtonBackgroundColor(Color shadeButtonBackgroundColor) {
        this.shadeButtonBackgroundColor = shadeButtonBackgroundColor;
        shadeButton.getStyles().put("shadeButtonBackgroundColor", shadeButtonBackgroundColor);
        repaintComponent();
    }

    public final void setShadeButtonBackgroundColor(String shadeButtonBackgroundColor) {
        if (shadeButtonBackgroundColor == null) {
            throw new IllegalArgumentException("shadeButtonBackgroundColor is null.");
        }

        setShadeButtonBackgroundColor(Color.decode(shadeButtonBackgroundColor));
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
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

    /**
     * Listener for expander button events.
     *
     * @param button
     *     The source of the button event.
     */
    public void buttonPressed(Button button) {
        Expander expander = (Expander)getComponent();

        expander.setExpanded(!expander.isExpanded());
    }

    // Expander events
    public void titleChanged(Expander expander, String previousTitle) {
        String title = expander.getTitle();
        titleLabel.setDisplayable(title != null);
        titleLabel.setText(title);
    }

    public void expandedChanged(Expander expander) {
        Image buttonData = expander.isExpanded() ? collapseImage : expandImage;
        shadeButton.setButtonData(buttonData);

        invalidateComponent();
    }

    public void contentChanged(Expander expander, Component previousContent) {
        invalidateComponent();
    }
}
