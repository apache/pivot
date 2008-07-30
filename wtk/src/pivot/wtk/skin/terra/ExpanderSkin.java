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
import java.awt.geom.Rectangle2D;

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
import pivot.wtk.TitlePane;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.media.Image;
import pivot.wtk.skin.TitlePaneSkin;

public class ExpanderSkin extends TitlePaneSkin
    implements ButtonPressListener, ExpanderListener {
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

    public static class ShadeButtonSkin extends PushButtonSkin {
        public ShadeButtonSkin() {
            super();

            setPadding(new Insets(2));
        }

        @Override
        public void install(Component component) {
            validateComponentType(component, ShadeButton.class);

            super.install(component);
        }

        @Override
        public boolean isFocusable() {
            return false;
        }
    }

    protected abstract class ButtonImage extends ImageAsset {
        public int getPreferredWidth(int height) {
            return 6;
        }

        public int getPreferredHeight(int width) {
            return 6;
        }

        public Dimensions getPreferredSize() {
            return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
        }
    }

    protected class CollapseImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            graphics.setPaint(shadeButtonColor);
            graphics.fill(new Rectangle2D.Double(0, 0, 6, 2));
        }
    }

    protected class ExpandImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            graphics.setPaint(shadeButtonColor);
            graphics.draw(new Rectangle2D.Double(0, 0, 5, 5));
        }
    }

    private class TitleBarMouseHandler
        implements ComponentMouseButtonListener {
        public void mouseDown(Component component, Mouse.Button button, int x, int y) {
        }

        public void mouseUp(Component component, Mouse.Button button, int x, int y) {
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

    private Font titleBarFont = new Font("Verdana", Font.BOLD, 11);
    private Color titleBarColor = Color.BLACK;
    private Color titleBarBackgroundColor = new Color(0xE6, 0xE3, 0xDA);
    private Color titleBarBorderColor = new Color(0xBD, 0xBD, 0xBD);
    private Color shadeButtonColor = new Color(0x66, 0x99, 0xCC);
    private Color shadeButtonBackgroundColor = Color.WHITE;
    private Color borderColor = new Color(0xCC, 0xCC, 0xCC);
    private Insets padding = new Insets(4);

    public ExpanderSkin() {
        setBackgroundColor(Color.WHITE);

        titleBarFlowPane = new FlowPane(Orientation.HORIZONTAL);
        titleBarFlowPane.getComponentMouseButtonListeners().add(titleBarMouseHandler);

        titleBarFlowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.JUSTIFY);
        titleBarFlowPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        titleBarFlowPane.getStyles().put("padding", new Insets(3));
        titleBarFlowPane.getStyles().put("spacing", 3);
        titleBarFlowPane.getStyles().put("backgroundColor", titleBarBackgroundColor);

        titleFlowPane = new FlowPane(Orientation.HORIZONTAL);
        titleFlowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);

        buttonFlowPane = new FlowPane(Orientation.HORIZONTAL);
        buttonFlowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);

        titleBarFlowPane.getComponents().add(titleFlowPane);
        titleBarFlowPane.getComponents().add(buttonFlowPane);

        // TODO Apply default styles to components

        titleFlowPane.getComponents().add(titleLabel);
    }

    @Override
    public void install(Component component) {
        validateComponentType(component, Expander.class);

        super.install(component);

        Expander expander = (Expander)component;
        expander.getExpanderListeners().add(this);
        expander.getComponents().add(titleBarFlowPane);

        Image buttonData = expander.isExpanded() ? collapseImage : expandImage;
        shadeButton = new ShadeButton(expander, buttonData);
        buttonFlowPane.getComponents().add(shadeButton);

        shadeButton.getButtonPressListeners().add(this);

        titleChanged(expander, null);
    }

    public void uninstall() {
        Expander expander = (Expander)getComponent();
        expander.getExpanderListeners().remove(this);
        expander.getComponents().remove(titleBarFlowPane);

        shadeButton.getButtonPressListeners().remove(this);
        buttonFlowPane.getComponents().remove(shadeButton);
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
        // TODO Optimize this by performing the calculations in a single loop here

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

    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        graphics.setStroke(new BasicStroke());

        Expander expander = (Expander)getComponent();
        if (expander.isExpanded()) {
            int titleBarHeight = titleBarFlowPane.getPreferredHeight(-1);

            graphics.setPaint(titleBarBorderColor);
            graphics.draw(new Line2D.Double(0, 1 + titleBarHeight, getWidth() - 1, 1 + titleBarHeight));
        }

        graphics.setPaint(borderColor);
        graphics.draw(new Rectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1));
    }

    @Override
    public void titleChanged(TitlePane expander, String previousTitle) {
        super.titleChanged(expander, previousTitle);

        String title = expander.getTitle();
        titleLabel.setDisplayable(title != null);
        titleLabel.setText(title);
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

    /**
     * Listener for expander events.
     *
     * @param expander
     *    The source of the event.
     */
    public void expandedChanged(Expander expander) {
        Image buttonData = expander.isExpanded() ? collapseImage : expandImage;
        shadeButton.setButtonData(buttonData);

        invalidateComponent();
    }
}
