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
package org.apache.pivot.wtk.skin.terra;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Expander;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.content.ButtonDataRenderer;
import org.apache.pivot.wtk.effects.ClipDecorator;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtk.effects.easing.Easing;
import org.apache.pivot.wtk.effects.easing.Quadratic;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.ExpanderSkin;

/**
 * Terra expander skin.
 */
public class TerraExpanderSkin extends ExpanderSkin
    implements ButtonPressListener {
    /**
     * Expand/collapse transition.
     */
    public class ExpandTransition extends Transition {
        private Easing easing = new Quadratic();

        public ExpandTransition(boolean reversed) {
            super(expandDuration, expandRate, false, reversed);
        }

        public float getScale() {
            int elapsedTime = getElapsedTime();
            int duration = getDuration();

            float scale;
            if (isReversed()) {
                scale = easing.easeIn(elapsedTime, 0, 1, duration);
            } else {
                scale = easing.easeOut(elapsedTime, 0, 1, duration);
            }

            return scale;
        }

        @Override
        public void start(TransitionListener transitionListener) {
            Expander expander = (Expander)getComponent();
            Component content = expander.getContent();
            content.getDecorators().add(clipDecorator);

            expander.setEnabled(false);

            super.start(transitionListener);
        }

        @Override
        public void stop() {
            Expander expander = (Expander)getComponent();
            Component content = expander.getContent();
            content.getDecorators().remove(clipDecorator);

            expander.setEnabled(true);

            super.stop();
        }

        @Override
        protected void update() {
            invalidateComponent();
        }
    }

    /**
     * Expander shade button component.
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
     */
    public class ShadeButtonSkin extends TerraLinkButtonSkin {
        @Override
        public boolean isFocusable() {
            return false;
        }
    }

    protected abstract class ButtonImage extends Image {
        @Override
        public int getWidth() {
            return 11;
        }

        @Override
        public int getHeight() {
            return 11;
        }
    }

    protected class CollapseImage extends ButtonImage {
        @Override
        public void paint(Graphics2D graphics) {
            Expander expander = (Expander)TerraExpanderSkin.this.getComponent();

            graphics.setStroke(new BasicStroke(0));
            if (expander.isEnabled()) {
                graphics.setPaint(shadeButtonColor);
            } else {
                graphics.setPaint(disabledShadeButtonColor);
            }
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            int[] xPoints = {3, 6, 9};
            int[] yPoints = {9, 3, 9};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    protected class ExpandImage extends ButtonImage {
        @Override
        public void paint(Graphics2D graphics) {
            Expander expander = (Expander)TerraExpanderSkin.this.getComponent();

            graphics.setStroke(new BasicStroke(0));
            if (expander.isEnabled()) {
                graphics.setPaint(shadeButtonColor);
            } else {
                graphics.setPaint(disabledShadeButtonColor);
            }
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            int[] xPoints = {3, 6, 9};
            int[] yPoints = {3, 9, 3};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    private Image collapseImage = new CollapseImage();
    private Image expandImage = new ExpandImage();

    private TablePane titleBarTablePane = null;
    private BoxPane titleBoxPane = null;
    private BoxPane buttonBoxPane = null;

    private Label titleLabel = new Label();
    private ShadeButton shadeButton = null;

    private Color titleBarBackgroundColor;
    private Color titleBarBorderColor;
    private Color shadeButtonColor;
    private Color disabledShadeButtonColor;
    private Color borderColor;
    private Insets padding;

    private int expandDuration = DEFAULT_EXPAND_DURATION;
    private int expandRate = DEFAULT_EXPAND_RATE;

    private Color titleBarBevelColor;

    private ExpandTransition expandTransition = null;
    private ClipDecorator clipDecorator = new ClipDecorator();

    private ComponentMouseButtonListener titleBarMouseListener = new ComponentMouseButtonListener.Adapter() {
        @Override
        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            boolean consumed = false;

            if (count == 2) {
                Expander expander = (Expander)getComponent();

                if (expander.isCollapsible()) {
                    expander.setExpanded(!expander.isExpanded());
                    consumed = true;
                }
            }

            return consumed;
        }
    };

    private static final int DEFAULT_EXPAND_DURATION = 250;
    private static final int DEFAULT_EXPAND_RATE = 30;

    public TerraExpanderSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(4));

        titleBarBackgroundColor = theme.getColor(10);
        titleBarBorderColor = theme.getColor(7);
        shadeButtonColor = theme.getColor(12);
        disabledShadeButtonColor = theme.getColor(7);
        borderColor = theme.getColor(7);
        padding = new Insets(4);

        // Set the derived colors
        titleBarBevelColor = TerraTheme.brighten(titleBarBackgroundColor);

        // Create the title bar components
        titleBarTablePane = new TablePane();
        titleBarTablePane.getColumns().add(new TablePane.Column(1, true));
        titleBarTablePane.getColumns().add(new TablePane.Column(-1));

        titleBarTablePane.getStyles().put("padding", new Insets(3));
        titleBarTablePane.getStyles().put("horizontalSpacing", 3);

        TablePane.Row titleRow = new TablePane.Row(-1);
        titleBarTablePane.getRows().add(titleRow);

        titleBoxPane = new BoxPane(Orientation.HORIZONTAL);
        titleBoxPane.getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);

        buttonBoxPane = new BoxPane(Orientation.HORIZONTAL);
        buttonBoxPane.getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);
        buttonBoxPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

        titleRow.add(titleBoxPane);
        titleRow.add(buttonBoxPane);

        titleLabel.getStyles().put("color", shadeButtonColor);

        Font titleFont = theme.getFont().deriveFont(Font.BOLD);
        titleLabel.getStyles().put("font", titleFont);
        titleBoxPane.add(titleLabel);

        // Listen for click events on the title bar
        titleBarTablePane.getComponentMouseButtonListeners().add(titleBarMouseListener);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Expander expander = (Expander)component;
        expander.add(titleBarTablePane);

        Image buttonData = expander.isExpanded() ? collapseImage : expandImage;
        shadeButton = new ShadeButton(buttonData);
        buttonBoxPane.add(shadeButton);

        shadeButton.getButtonPressListeners().add(this);

        titleChanged(expander, null);
        collapsibleChanged(expander);
        enabledChanged(expander);
    }

    @Override
    public int getPreferredWidth(int height) {
        Expander expander = (Expander)getComponent();
        Component content = expander.getContent();

        int preferredWidth = titleBarTablePane.getPreferredWidth(-1);

        if (content != null) {
            int contentHeight = -1;

            if (height >= 0) {
                int reservedHeight = 2 + padding.top + padding.bottom
                    + titleBarTablePane.getPreferredHeight(-1);

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

    @Override
    public int getPreferredHeight(int width) {
        Expander expander = (Expander)getComponent();
        Component content = expander.getContent();

        int preferredHeight = titleBarTablePane.getPreferredHeight(-1);

        if (content != null
            && (expander.isExpanded()
                || expandTransition != null)) {
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
                preferredHeight += (int)(scale * (padding.top + padding.bottom
                    + content.getPreferredHeight(contentWidth)));
            }
        }

        preferredHeight += 2;

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        Expander expander = (Expander)getComponent();
        Component content = expander.getContent();

        Dimensions titleBarSize = titleBarTablePane.getPreferredSize();

        int preferredWidth = titleBarSize.width;
        int preferredHeight = titleBarSize.height;

        if (content != null) {
            Dimensions contentSize = content.getPreferredSize();

            preferredWidth = Math.max(contentSize.width + (padding.left + padding.right),
                preferredWidth);

            if (expander.isExpanded()
                || expandTransition != null) {
                // Title bar border is only drawn when expander is expanded
                // or expanding
                preferredHeight += 1;

                if (expandTransition == null) {
                    preferredHeight += (padding.top + padding.bottom + contentSize.height);
                } else {
                    float scale = expandTransition.getScale();
                    preferredHeight += (int)(scale * (padding.top + padding.bottom
                        + contentSize.height));
                }
            }
        }

        preferredWidth += 2;
        preferredHeight += 2;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        Expander expander = (Expander)getComponent();
        Component content = expander.getContent();

        int baseline = -1;

        if (content != null) {
            int titleBarWidth = Math.max(width - 2, 0);
            int titleBarHeight = titleBarTablePane.getPreferredHeight(-1);

            baseline = titleBarTablePane.getBaseline(titleBarWidth, titleBarHeight);

            if (baseline != -1) {
                // Account for top border
                baseline += 1;
            }
        }

        return baseline;
    }

    @Override
    public void layout() {
        Expander expander = (Expander)getComponent();
        Component content = expander.getContent();

        int width = getWidth();
        int height = getHeight();

        int titleBarHeight = titleBarTablePane.getPreferredHeight(-1);
        titleBarTablePane.setSize(Math.max(width - 2, 0), titleBarHeight);
        titleBarTablePane.setLocation(1, 1);

        if (content != null) {
            int contentWidth = Math.max(width - (2 + padding.left + padding.right), 0);
            int contentHeight = Math.max(height - (3 + padding.top + padding.bottom + titleBarHeight), 0);

            clipDecorator.setSize(contentWidth, contentHeight);
            content.setSize(contentWidth, content.getPreferredHeight(contentWidth));

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

        int titleBarX = titleBarTablePane.getX();
        int titleBarY = titleBarTablePane.getY();
        int titleBarWidth = titleBarTablePane.getWidth();
        int titleBarHeight = titleBarTablePane.getHeight();

        graphics.setPaint(titleBarBorderColor);
        GraphicsUtilities.drawLine(graphics, 0, 1 + titleBarHeight, width, Orientation.HORIZONTAL);

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

        setTitleBarFont(decodeFont(titleBarFont));
    }

    public final void setTitleBarFont(Dictionary<String, ?> titleBarFont) {
        if (titleBarFont == null) {
            throw new IllegalArgumentException("titleBarFont is null.");
        }

        setTitleBarFont(Theme.deriveFont(titleBarFont));
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

    public Color getDisabledShadeButtonColor() {
        return disabledShadeButtonColor;
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

    public final void setPadding(String padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(Insets.decode(padding));
    }

    public int getExpandDuration() {
        return expandDuration;
    }

    public void setExpandDuration(int expandDuration) {
        this.expandDuration = expandDuration;
    }

    public int getExpandRate() {
        return expandRate;
    }

    public void setExpandRate(int expandRate) {
        this.expandRate = expandRate;
    }

    // ButtonPressListener methods

    /**
     * Listener for expander button events.
     *
     * @param button
     *     The source of the button event.
     */
    @Override
    public void buttonPressed(Button button) {
        Expander expander = (Expander)getComponent();

        if (expander.isCollapsible()) {
            expander.setExpanded(!expander.isExpanded());
        }
    }

    // ComponentStateListener methods

    /**
     * {@inheritDoc}
     */
    @Override
    public void enabledChanged(Component component) {
        if (component.isEnabled()) {
            setTitleBarColor(shadeButtonColor);
        } else {
            setTitleBarColor(disabledShadeButtonColor);
        }
    }

    // ExpanderListener methods

    /**
     * {@inheritDoc}
     */
    @Override
    public void titleChanged(Expander expander, String previousTitle) {
        String title = expander.getTitle();
        titleLabel.setVisible(title != null);
        titleLabel.setText(title != null ? title : "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void collapsibleChanged(Expander expander) {
        buttonBoxPane.setVisible(expander.isCollapsible());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vote previewExpandedChange(final Expander expander) {
        Vote vote;

        if (expander.isShowing()
            && expandTransition == null
            && expander.getContent() != null) {
            final boolean expanded = expander.isExpanded();
            shadeButton.setButtonData(expanded ? collapseImage : expandImage);
            expandTransition = new ExpandTransition(expanded);

            expandTransition.start(new TransitionListener() {
                @Override
                public void transitionCompleted(Transition transition) {
                    expander.setExpanded(!expanded);
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

    /**
     * {@inheritDoc}
     */
    @Override
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void expandedChanged(final Expander expander) {
        shadeButton.setButtonData(expander.isExpanded() ? collapseImage : expandImage);
        invalidateComponent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contentChanged(Expander expander, Component previousContent) {
        if (expandTransition != null) {
            expandTransition.end();
        }

        invalidateComponent();
    }
}
