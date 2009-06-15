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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.util.Vote;

import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Expander;
import pivot.wtk.ExpanderListener;
import pivot.wtk.Dimensions;
import pivot.wtk.GraphicsUtilities;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Insets;
import pivot.wtk.Label;
import pivot.wtk.LinkButton;
import pivot.wtk.Mouse;
import pivot.wtk.Orientation;
import pivot.wtk.FlowPane;
import pivot.wtk.Theme;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.content.ButtonDataRenderer;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;
import pivot.wtk.effects.easing.Easing;
import pivot.wtk.effects.easing.Quadratic;
import pivot.wtk.media.Image;
import pivot.wtk.skin.ContainerSkin;

/**
 * Expander skin.
 *
 * @author gbrown
 */
public class TerraExpanderSkin extends ContainerSkin
    implements ButtonPressListener, ExpanderListener {
    public class ExpandTransition extends Transition {
        private boolean expand;
        private Easing easing = new Quadratic();

        public ExpandTransition(boolean expand, int duration, int rate) {
            super(duration, rate, false);
            this.expand = expand;
        }

        public float getScale() {
            int elapsedTime = getElapsedTime();
            int duration = getDuration();

            float scale;
            if (expand) {
                scale = easing.easeOut(elapsedTime, 0, 1, duration);
            } else {
                scale = easing.easeIn(elapsedTime, 1, -1, duration);
            }

            return scale;
        }

        @Override
        public void start(TransitionListener transitionListener) {
            getComponent().setEnabled(false);
            super.start(transitionListener);
        }

        @Override
        public void stop() {
            getComponent().setEnabled(true);
            super.stop();
        }

        @Override
        protected void update() {
            invalidateComponent();
        }
    }

    /**
     * Expander shade button component.
     *
     * @author tvolkert
     */
    public class ShadeButton extends LinkButton {
        public ShadeButton() {
            this(null);
        }

        public ShadeButton(Object buttonData) {
            super(buttonData);

            setSkin(new ShadeButtonSkin());
            setDataRenderer(new ButtonDataRenderer());
        }
    }

    /**
     * Expander shade button component skin.
     *
     * @author tvolkert
     */
    public class ShadeButtonSkin extends TerraLinkButtonSkin {
        @Override
        public boolean isFocusable() {
            return false;
        }
    }

    protected abstract class ButtonImage extends Image {
        public int getWidth() {
            return 11;
        }

        public int getHeight() {
            return 11;
        }
    }

    protected class CollapseImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            graphics.setStroke(new BasicStroke(0));
            graphics.setPaint(shadeButtonColor);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            int[] xPoints = {3, 6, 9};
            int[] yPoints = {9, 3, 9};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    protected class ExpandImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            graphics.setStroke(new BasicStroke(0));
            graphics.setPaint(shadeButtonColor);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            int[] xPoints = {3, 6, 9};
            int[] yPoints = {3, 9, 3};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
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

        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            boolean consumed = false;

            if (count == 2) {
                Expander expander = (Expander)getComponent();
                expander.setExpanded(!expander.isExpanded());
                consumed = true;
            }

            return consumed;
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
    private Color shadeButtonColor;
    private Color shadeButtonBackgroundColor;
    private Color borderColor;
    private Insets padding;

    // Derived colors
    private Color titleBarBevelColor;

    private ExpandTransition expandTransition = null;

    private static final int EXPAND_DURATION = 250;
    private static final int EXPAND_RATE = 30;

    public TerraExpanderSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(4));

        titleBarBackgroundColor = theme.getColor(10);
        titleBarBorderColor = theme.getColor(7);
        shadeButtonColor = theme.getColor(7);
        shadeButtonBackgroundColor = theme.getColor(4);
        borderColor = theme.getColor(7);
        padding = new Insets(4);

        // Set the derived colors
        titleBarBevelColor = TerraTheme.brighten(titleBarBackgroundColor);

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

        titleLabel.getStyles().put("color", theme.getColor(15));
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
        shadeButton = new ShadeButton(buttonData);
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
        Expander expander = (Expander)getComponent();
        Component content = expander.getContent();

        int preferredWidth = titleBarFlowPane.getPreferredWidth(-1);

        if (content != null) {
            int contentHeight = -1;

            if (height >= 0) {
                int reservedHeight = 2 + padding.top + padding.bottom
                    + titleBarFlowPane.getPreferredHeight(-1);

                if (expander.isExpanded()) {
                    // Title bar border is only drawn when expander is expanded
                    reservedHeight += 1;
                }

                contentHeight = Math.max(height - reservedHeight, 0);
            }

            preferredWidth = Math.max(content.getPreferredWidth(contentHeight)
                + (padding.left + padding.right), preferredWidth);
        }

        preferredWidth += 2;

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        Expander expander = (Expander)getComponent();
        Component content = expander.getContent();

        int preferredHeight = titleBarFlowPane.getPreferredHeight(-1);

        if (content != null
            && (expander.isExpanded()
                || (expandTransition != null
                    && expandTransition.expand))) {
            // Title bar border is only drawn when content is non-null and
            // expander is expanded or expanding
            preferredHeight += 1;

            int contentWidth = -1;
            if (width >= 0) {
                contentWidth = Math.max(width - (2 + padding.left + padding.right), 0);
            }

            if (expandTransition == null) {
                preferredHeight += (padding.top + padding.bottom
                    + content.getPreferredHeight(contentWidth));
            } else {
                float scale = expandTransition.getScale();
                preferredHeight += (int)(scale * (float)(padding.top + padding.bottom
                    + content.getPreferredHeight(contentWidth)));
            }
        }

        preferredHeight += 2;

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        Expander expander = (Expander)getComponent();
        Component content = expander.getContent();

        Dimensions titleBarSize = titleBarFlowPane.getPreferredSize();

        int preferredWidth = titleBarSize.width;
        int preferredHeight = titleBarSize.height;

        if (content != null) {
            Dimensions contentSize = content.getPreferredSize();

            preferredWidth = Math.max(contentSize.width + (padding.left + padding.right),
                preferredWidth);

            if (expander.isExpanded()
                || (expandTransition != null
                    && expandTransition.expand)) {
                // Title bar border is only drawn when expander is expanded
                // or expanding
                preferredHeight += 1;

                if (expandTransition == null) {
                    preferredHeight += (padding.top + padding.bottom + contentSize.height);
                } else {
                    float scale = expandTransition.getScale();
                    preferredHeight += (int)(scale * (float)(padding.top + padding.bottom
                        + contentSize.height));
                }
            }
        }

        preferredWidth += 2;
        preferredHeight += 2;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    public void layout() {
        Expander expander = (Expander)getComponent();
        Component content = expander.getContent();

        int width = getWidth();
        int height = getHeight();

        int titleBarHeight = titleBarFlowPane.getPreferredHeight(-1);
        titleBarFlowPane.setSize(Math.max(width - 2, 0), titleBarHeight);
        titleBarFlowPane.setLocation(1, 1);

        if ((expander.isExpanded()
            || (expandTransition != null
                && expandTransition.expand))
            && content != null) {
            int contentWidth = Math.max(width - (2 + padding.left + padding.right), 0);
            int contentHeight = Math.max(height - (3 + padding.top + padding.bottom + titleBarHeight), 0);
            content.setSize(contentWidth, contentHeight);

            int contentX = 1 + padding.left;
            int contentY = 2 + padding.top + titleBarHeight;
            content.setLocation(contentX, contentY);
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        int width = getWidth();
        int height = getHeight();

        Expander expander = (Expander)getComponent();
        if (expander.isExpanded()
            || (expandTransition != null
                && expandTransition.expand)) {
            int titleBarHeight = titleBarFlowPane.getPreferredHeight(-1);
            graphics.setPaint(titleBarBorderColor);
            GraphicsUtilities.drawLine(graphics, 0, 1 + titleBarHeight, width, Orientation.HORIZONTAL);
        }

        int titleBarX = titleBarFlowPane.getX();
        int titleBarY = titleBarFlowPane.getY();
        int titleBarWidth = titleBarFlowPane.getWidth();
        int titleBarHeight = titleBarFlowPane.getHeight();

        graphics.setPaint(new GradientPaint(titleBarX + titleBarWidth / 2, titleBarY, titleBarBevelColor,
            titleBarX + titleBarWidth / 2, titleBarY + titleBarHeight, titleBarBackgroundColor));
        graphics.fillRect(titleBarX, titleBarY, titleBarWidth, titleBarHeight);

        graphics.setPaint(borderColor);
        GraphicsUtilities.drawRect(graphics, 0, 0, width, height);
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

        setTitleBarColor(GraphicsUtilities.decodeColor(titleBarColor));
    }

    public Color getTitleBarBackgroundColor() {
        return titleBarBackgroundColor;
    }

    public void setTitleBarBackgroundColor(Color titleBarBackgroundColor) {
        this.titleBarBackgroundColor = titleBarBackgroundColor;
        titleBarBevelColor = TerraTheme.brighten(titleBarBackgroundColor);
        repaintComponent();
    }

    public final void setTitleBarBackgroundColor(String titleBarBackgroundColor) {
        if (titleBarBackgroundColor == null) {
            throw new IllegalArgumentException("titleBarBackgroundColor is null.");
        }

        setTitleBarBackgroundColor(GraphicsUtilities.decodeColor(titleBarBackgroundColor));
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

        setTitleBarBorderColor(GraphicsUtilities.decodeColor(titleBarBorderColor));
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

        setShadeButtonColor(GraphicsUtilities.decodeColor(shadeButtonColor));
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

        setShadeButtonBackgroundColor(GraphicsUtilities.decodeColor(shadeButtonBackgroundColor));
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

        setBorderColor(GraphicsUtilities.decodeColor(borderColor));
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

    public Vote previewExpandedChange(final Expander expander) {
        Vote vote;

        if (expander.isShowing()
            && expandTransition == null
            && expander.getContent() != null) {
            final boolean expand = !expander.isExpanded();
            shadeButton.setButtonData(expand ? collapseImage : expandImage);
            expandTransition = new ExpandTransition(expand, EXPAND_DURATION, EXPAND_RATE);

            expandTransition.start(new TransitionListener() {
                public void transitionCompleted(Transition transition) {
                    expander.setExpanded(expand);
                    expandTransition = null;
                }
            });
        }

        if (expandTransition == null
            || !expandTransition.isRunning()) {
            vote = Vote.APPROVE;
        } else {
            vote = Vote.DEFER;
        }

        return vote;
    }

    public void expandedChangeVetoed(Expander expander, Vote reason) {
        if (reason == Vote.DENY
            && expandTransition != null) {
            // NOTE We stop, rather than end, the transition so the completion
            // event isn't fired; if the event fires, the listener will set
            // the expanded state
            expandTransition.stop();
            expandTransition = null;

            shadeButton.setButtonData(expander.isExpanded() ? collapseImage : expandImage);

            invalidateComponent();
        }
    }

    public void expandedChanged(final Expander expander) {
        shadeButton.setButtonData(expander.isExpanded() ? collapseImage : expandImage);
        invalidateComponent();
    }

    public void contentChanged(Expander expander, Component previousContent) {
        if (expandTransition != null) {
            expandTransition.end();
        }

        invalidateComponent();
    }
}
