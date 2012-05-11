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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Rollup;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtk.effects.easing.Easing;
import org.apache.pivot.wtk.effects.easing.Quadratic;
import org.apache.pivot.wtk.skin.ComponentSkin;
import org.apache.pivot.wtk.skin.RollupSkin;

/**
 * Terra rollup skin.
 */
public class TerraRollupSkin extends RollupSkin {
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
     * Component that allows the user to expand and collapse the Rollup.
     */
    protected class RollupButton extends Component {
        public RollupButton() {
            setSkin(new RollupButtonSkin());
        }
    }

    /**
     * Skin for the rollup button.
     */
    protected class RollupButtonSkin extends ComponentSkin {
        @Override
        public boolean isFocusable() {
            return false;
        }

        @Override
        public int getPreferredWidth(int height) {
            return 7;
        }

        @Override
        public int getPreferredHeight(int width) {
            return 7;
        }

        @Override
        public Dimensions getPreferredSize() {
            return new Dimensions(7, 7);
        }

        @Override
        public void layout() {
            // No-op
        }

        @Override
        public void paint(Graphics2D graphics) {
            Rollup rollup = (Rollup)TerraRollupSkin.this.getComponent();

            graphics.setStroke(new BasicStroke(0));
            if (rollup.isEnabled()) {
                graphics.setPaint(buttonColor);
            } else {
                graphics.setPaint(disabledButtonColor);
            }
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            if (rollup.isCollapsible()) {
                if (rollup.isExpanded()) {
                    // Paint the collapse image
                    int[] xPoints = {0, 3, 6};
                    int[] yPoints = {0, 6, 0};
                    graphics.fillPolygon(xPoints, yPoints, 3);
                    graphics.drawPolygon(xPoints, yPoints, 3);
                } else {
                    // Paint the expand image
                    int[] xPoints = {0, 6, 0};
                    int[] yPoints = {0, 3, 6};
                    graphics.fillPolygon(xPoints, yPoints, 3);
                    graphics.drawPolygon(xPoints, yPoints, 3);
                }
            } else {
                // Paint the bullet
                RoundRectangle2D.Double shape = new RoundRectangle2D.Double(1, 1, 4, 4, 2, 2);
                graphics.draw(shape);
                graphics.fill(shape);
            }
        }

        @Override
        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            Rollup rollup = (Rollup)TerraRollupSkin.this.getComponent();
            rollup.setExpanded(!rollup.isExpanded());
            return true;
        }
    }

    private RollupButton rollupButton = null;

    // Styles
    private Color buttonColor;
    private Color disabledButtonColor;
    private int spacing;
    private int buffer;
    private boolean fill;
    private boolean headingToggles;

    private int expandDuration = DEFAULT_EXPAND_DURATION;
    private int expandRate = DEFAULT_EXPAND_RATE;

    private ExpandTransition expandTransition = null;

    private ComponentMouseButtonListener headingMouseButtonListener = new ComponentMouseButtonListener.Adapter() {
        @Override
        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            boolean consumed = false;

            Rollup rollup = (Rollup)getComponent();
            if (headingToggles
                && rollup.isCollapsible()) {
                rollup.setExpanded(!rollup.isExpanded());
                consumed = true;
            }

            return consumed;
        }
    };

    private static final int DEFAULT_EXPAND_DURATION = 250;
    private static final int DEFAULT_EXPAND_RATE = 30;

    public TerraRollupSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();

        buttonColor = theme.getColor(1);
        disabledButtonColor = theme.getColor(7);
        spacing = 4;
        buffer = 4;
        fill = false;
        headingToggles = true;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Rollup rollup = (Rollup)component;

        // Add the rollup button
        rollupButton = new RollupButton();
        rollup.add(rollupButton);

        // Initialize state
        headingChanged(rollup, null);
        contentChanged(rollup, null);
        collapsibleChanged(rollup);
    }

    @Override
    public int getPreferredWidth(int height) {
        Rollup rollup = (Rollup)getComponent();

        Component heading = rollup.getHeading();
        Component content = rollup.getContent();

        int preferredWidth = 0;

        if (heading != null) {
            preferredWidth = heading.getPreferredWidth(-1);
        }

        if (content != null
            && (rollup.isExpanded()
                || (expandTransition != null
                    && !expandTransition.isReversed()))) {
            preferredWidth = Math.max(preferredWidth, content.getPreferredWidth(-1));
        }

        preferredWidth += rollupButton.getPreferredWidth(-1) + buffer;

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        Rollup rollup = (Rollup)getComponent();

        Component heading = rollup.getHeading();
        Component content = rollup.getContent();

        int preferredHeight = 0;

        // Calculate our internal width constraint
        if (fill && width >= 0) {
            width = Math.max(width - rollupButton.getPreferredWidth(-1) - buffer, 0);
        } else {
            width = -1;
        }

        if (heading != null) {
            preferredHeight += heading.getPreferredHeight(width);
        }

        if (content != null) {
            if (expandTransition == null) {
                if (rollup.isExpanded()) {
                    preferredHeight += spacing + content.getPreferredHeight(width);
                }
            } else {
                float scale = expandTransition.getScale();
                preferredHeight += (int)(scale * (spacing + content.getPreferredHeight(width)));
            }
        }

        preferredHeight = Math.max(preferredHeight, rollupButton.getPreferredHeight(-1));

        return preferredHeight;
    }

    @Override
    public int getBaseline(int width, int height) {
        Rollup rollup = (Rollup)getComponent();
        Component heading = rollup.getHeading();

        int baseline = -1;

        if (heading != null) {
            int headingWidth, headingHeight;
            if (fill) {
                headingWidth = Math.max(width - rollupButton.getPreferredWidth(-1) - buffer, 0);
                headingHeight = heading.getPreferredHeight(headingWidth);
            } else {
                Dimensions headingPreferredSize = heading.getPreferredSize();
                headingWidth = headingPreferredSize.width;
                headingHeight = headingPreferredSize.height;
            }

            baseline = heading.getBaseline(headingWidth, headingHeight);
        }

        return baseline;
    }

    @Override
    public void layout() {
        Rollup rollup = (Rollup)getComponent();

        Component heading = rollup.getHeading();
        Component content = rollup.getContent();

        Dimensions rollupButtonSize = rollupButton.getPreferredSize();
        rollupButton.setSize(rollupButtonSize.width, rollupButtonSize.height);

        int x = rollupButtonSize.width + buffer;
        int y = 0;
        int justifiedWidth = Math.max(getWidth() - rollupButtonSize.width - buffer, 0);

        if (heading != null) {
            int headingWidth, headingHeight;
            if (fill) {
                headingWidth = justifiedWidth;
                headingHeight = heading.getPreferredHeight(headingWidth);
            } else {
                Dimensions headingPreferredSize = heading.getPreferredSize();
                headingWidth = headingPreferredSize.width;
                headingHeight = headingPreferredSize.height;
            }

            heading.setLocation(x, y);
            heading.setSize(headingWidth, headingHeight);

            y += headingHeight + spacing;
        }

        if (content != null) {
            if (rollup.isExpanded()
                || (expandTransition != null
                && !expandTransition.isReversed())) {
                int contentWidth, contentHeight;
                if (fill) {
                    contentWidth = justifiedWidth;
                    contentHeight = content.getPreferredHeight(contentWidth);
                } else {
                    Dimensions contentPreferredSize = content.getPreferredSize();
                    contentWidth = contentPreferredSize.width;
                    contentHeight = contentPreferredSize.height;
                }

                content.setLocation(x, y);
                content.setSize(contentWidth, contentHeight);

                content.setVisible(true);
            } else {
                content.setVisible(false);
            }

        }

        y = (heading == null ? 0 : (heading.getHeight() - rollupButtonSize.height) / 2 + 1);

        rollupButton.setLocation(0, y);
    }

    public Color getButtonColor() {
        return buttonColor;
    }

    public final void setButtonColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setButtonColor(theme.getColor(color));
    }

    public void setButtonColor(Color buttonColor) {
        if (buttonColor == null) {
            throw new IllegalArgumentException("buttonColor is null.");
        }

        this.buttonColor = buttonColor;
        rollupButton.repaint();
    }

    public final void setButtonColor(String buttonColor) {
        if (buttonColor == null) {
            throw new IllegalArgumentException("buttonColor is null.");
        }

        setButtonColor(GraphicsUtilities.decodeColor(buttonColor));
    }

    public Color getDisabledButtonColor() {
        return disabledButtonColor;
    }

    public final void setDisabledButtonColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setDisabledButtonColor(theme.getColor(color));
    }

    public void setDisabledButtonColor(Color buttonColor) {
        if (buttonColor == null) {
            throw new IllegalArgumentException("buttonColor is null.");
        }

        this.disabledButtonColor = buttonColor;
        rollupButton.repaint();
    }

    public final void setDisabledButtonColor(String buttonColor) {
        if (buttonColor == null) {
            throw new IllegalArgumentException("buttonColor is null.");
        }

        setDisabledButtonColor(GraphicsUtilities.decodeColor(buttonColor));
    }

    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(int spacing) {
        if (spacing < 0) {
            throw new IllegalArgumentException("spacing is negative.");
        }
        this.spacing = spacing;

        Rollup rollup = (Rollup)getComponent();
        if (rollup.isExpanded()) {
            invalidateComponent();
        }
    }

    public int getBuffer() {
        return buffer;
    }

    public void setBuffer(int buffer) {
        this.buffer = buffer;
        invalidateComponent();
    }

    public boolean getFill() {
        return fill;
    }

    public void setFill(boolean fill) {
        this.fill = fill;
        invalidateComponent();
    }

    public boolean getHeadingToggles() {
        return headingToggles;
    }

    public void setHeadingToggles(boolean headingToggles) {
        this.headingToggles = headingToggles;
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

    @Override
    public void headingChanged(Rollup rollup, Component previousHeading) {
        if (previousHeading != null) {
            previousHeading.getComponentMouseButtonListeners().remove(headingMouseButtonListener);
        }

        Component heading = rollup.getHeading();

        if (heading != null) {
            heading.getComponentMouseButtonListeners().add(headingMouseButtonListener);
        }

        invalidateComponent();
    }

    @Override
    public void contentChanged(Rollup rollup, Component previousContent) {
        invalidateComponent();
    }

    // Rollup state events
    @Override
    public Vote previewExpandedChange(final Rollup rollup) {
        Vote vote;

        if (rollup.isShowing()
            && expandTransition == null
            && rollup.getContent() != null) {
            final boolean expanded = rollup.isExpanded();
            expandTransition = new ExpandTransition(expanded);

            expandTransition.start(new TransitionListener() {
                @Override
                public void transitionCompleted(Transition transition) {
                    rollup.setExpanded(!expanded);
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

    @Override
    public void expandedChangeVetoed(Rollup rollup, Vote reason) {
        if (reason == Vote.DENY
            && expandTransition != null) {
            // NOTE We stop, rather than end, the transition so the completion
            // event isn't fired; if the event fires, the listener will set
            // the expanded state
            expandTransition.stop();
            expandTransition = null;

            invalidateComponent();
        }
    }

    @Override
    public void expandedChanged(final Rollup rollup) {
        invalidateComponent();
    }

    @Override
    public void collapsibleChanged(Rollup rollup) {
        if (rollup.isCollapsible()) {
            rollupButton.setCursor(Cursor.HAND);
        } else {
            rollupButton.setCursor(Cursor.DEFAULT);
        }

        invalidateComponent();
    }

}
