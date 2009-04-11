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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import pivot.util.Vote;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Cursor;
import pivot.wtk.Dimensions;
import pivot.wtk.Mouse;
import pivot.wtk.Rollup;
import pivot.wtk.Theme;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;
import pivot.wtk.effects.easing.Easing;
import pivot.wtk.effects.easing.Quadratic;
import pivot.wtk.skin.ComponentSkin;
import pivot.wtk.skin.RollupSkin;

/**
 * Terra theme's rollup skin.
 *
 * @author tvolkert
 */
public class TerraRollupSkin extends RollupSkin {
    /**
     * Provides expand/collapse animation.
     *
     * @author tvolkert
     */
    private class ExpansionTransition extends Transition {
        private int height1;
        private int height2;
        private boolean reverse;

        private int originalPreferredHeight;
        private int height;

        private Easing easing = new Quadratic();

        public ExpansionTransition(int height1, int height2, boolean reverse, int duration, int rate) {
            super(duration, rate, false);

            this.height1 = height1;
            this.height2 = height2;
            this.reverse = reverse;
        }

        public int getHeight() {
            return height;
        }

        @Override
        public void start(TransitionListener transitionListener) {
            Rollup rollup = (Rollup)getComponent();
            originalPreferredHeight = rollup.isPreferredHeightSet() ?
                rollup.getPreferredHeight() : -1;

            super.start(transitionListener);
        }

        @Override
        public void stop() {
            Rollup rollup = (Rollup)getComponent();
            rollup.setPreferredHeight(originalPreferredHeight);

            super.stop();
        }

        @Override
        protected void update() {
            float percentComplete = getPercentComplete();

            if (percentComplete < 1f) {
                int elapsedTime = getElapsedTime();
                int duration = getDuration();

                height = (int)(height1 + (height2 - height1) * percentComplete);
                if (reverse) {
                    height = (int)easing.easeIn(elapsedTime, height1, height2 - height1, duration);
                } else {
                    height = (int)easing.easeOut(elapsedTime, height1, height2 - height1, duration);
                }

                getComponent().setPreferredHeight(height);
            }
        }
    }

    /**
     * Component that allows the user to expand and collapse the Rollup.
     *
     * @author tvolkert
     */
    protected class RollupButton extends Component {
        public RollupButton() {
            setSkin(new RollupButtonSkin());
        }
    }

    /**
     * Skin for the rollup button.
     *
     * @author tvolkert
     */
    protected class RollupButtonSkin extends ComponentSkin {
        @Override
        public boolean isFocusable() {
            return false;
        }

        public int getPreferredWidth(int height) {
            return 7;
        }

        public int getPreferredHeight(int width) {
            return 7;
        }

        public Dimensions getPreferredSize() {
            return new Dimensions(7, 7);
        }

        public void layout() {
            // No-op
        }

        public void paint(Graphics2D graphics) {
            Rollup rollup = (Rollup)TerraRollupSkin.this.getComponent();

            graphics.setStroke(new BasicStroke(0));
            graphics.setPaint(buttonColor);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            if (rollup.getContent() == null && useBullet) {
                // Paint the bullet
                RoundRectangle2D.Double shape = new RoundRectangle2D.Double(1, 1, 4, 4, 2, 2);
                graphics.draw(shape);
                graphics.fill(shape);
            } else if (rollup.isExpanded()) {
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
        }

        @Override
        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            Rollup rollup = (Rollup)TerraRollupSkin.this.getComponent();
            rollup.setExpanded(!rollup.isExpanded());
            return true;
        }
    }

    /**
     * Responsible for expanding and collapsing the rollup when the user clicks
     * on the heading component. This only applies if <tt>headingToggles</tt>
     * is <tt>true</tt>.
     *
     * @author tvolkert
     */
    private class HeadingMouseButtonHandler implements ComponentMouseButtonListener {
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            boolean consumed = false;

            if (headingToggles) {
                Rollup rollup = (Rollup)getComponent();
                rollup.setExpanded(!rollup.isExpanded());
                consumed = true;
            }

            return consumed;
        }
    }

    // Skin components
    private RollupButton rollupButton = null;

    // Internal handlers
    private HeadingMouseButtonHandler headingMouseButtonHandler = new HeadingMouseButtonHandler();

    // Animation support
    private ExpansionTransition expandTransition = null;
    private ExpansionTransition collapseTransition = null;

    // Styles
    private Color buttonColor;
    private int spacing;
    private int buffer;
    private boolean justify;
    private boolean headingToggles;
    private boolean useBullet;

    private static final int EXPANSION_DURATION = 250;
    private static final int EXPANSION_RATE = 30;

    public TerraRollupSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();

        buttonColor = theme.getColor(9);
        spacing = 4;
        buffer = 4;
        justify = false;
        headingToggles = true;
        useBullet = false;
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
    }

    @Override
    public void uninstall() {
        Rollup rollup = (Rollup)getComponent();

        // Uninitialize state
        Component heading = rollup.getHeading();
        if (heading != null) {
            heading.getComponentMouseButtonListeners().remove(headingMouseButtonHandler);
        }

        // Remove the rollup button
        rollup.remove(rollupButton);
        rollupButton = null;

        super.uninstall();
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

        if (rollup.isExpanded() && content != null) {
            preferredWidth = Math.max(preferredWidth, content.getPreferredWidth(-1));
        }

        preferredWidth += rollupButton.getPreferredWidth(-1) + buffer;

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        Rollup rollup = (Rollup)getComponent();
        return getPreferredHeight(width, rollup.isExpanded());
    }

    /**
     * Gets the preferred height of the rollup assuming the specified expansion
     * state. We use this to transition from one expansion state to another
     * because it allows us to have foreknowledge of the height value to which
     * we are transitioning.
     *
     * @param width
     * The width constraint.
     *
     * @param expanded
     * The supposed expansion state.
     */
    private int getPreferredHeight(int width, boolean expanded) {
        Rollup rollup = (Rollup)getComponent();

        Component heading = rollup.getHeading();
        Component content = rollup.getContent();

        int preferredHeight = 0;

        // Calculate our internal width constraint
        if (justify && width >= 0) {
            width = Math.max(width - rollupButton.getPreferredWidth(-1) - buffer, 0);
        } else {
            width = -1;
        }

        if (heading != null) {
            preferredHeight += heading.getPreferredHeight(width);
        }

        if (expanded && content != null) {
            preferredHeight += spacing + content.getPreferredHeight(width);
        }

        preferredHeight = Math.max(preferredHeight, rollupButton.getPreferredHeight(-1));

        return preferredHeight;
    }

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
            if (justify) {
                headingWidth = justifiedWidth;
                headingHeight = heading.getPreferredHeight(headingWidth);
            } else {
                Dimensions headingPreferredSize = heading.getPreferredSize();
                headingWidth = headingPreferredSize.width;
                headingHeight = headingPreferredSize.height;
            }

            heading.setVisible(true);
            heading.setLocation(x, y);
            heading.setSize(headingWidth, headingHeight);

            y += headingHeight + spacing;
        }

        if (content != null) {
            if (rollup.isExpanded()) {
                int contentWidth, contentHeight;
                if (justify) {
                    contentWidth = justifiedWidth;
                    contentHeight = content.getPreferredHeight(contentWidth);
                } else {
                    Dimensions contentPreferredSize = content.getPreferredSize();
                    contentWidth = contentPreferredSize.width;
                    contentHeight = contentPreferredSize.height;
                }

                content.setVisible(true);
                content.setLocation(x, y);
                content.setSize(contentWidth, contentHeight);
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

        setButtonColor(decodeColor(buttonColor));
    }

    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(int spacing) {
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

    public boolean getJustify() {
        return justify;
    }

    public void setJustify(boolean justify) {
        this.justify = justify;
        invalidateComponent();
    }

    public boolean getHeadingToggles() {
        return headingToggles;
    }

    public void setHeadingToggles(boolean headingToggles) {
        this.headingToggles = headingToggles;
    }

    public boolean getUseBullet() {
        return useBullet;
    }

    public void setUseBullet(boolean useBullet) {
        this.useBullet = useBullet;

        Rollup rollup = (Rollup)getComponent();
        if (rollup.getContent() == null) {
            rollupButton.repaint();
        }
    }

    // RollupListener methods

    @Override
    public void headingChanged(Rollup rollup, Component previousHeading) {
        if (previousHeading != null) {
            previousHeading.getComponentMouseButtonListeners().remove(headingMouseButtonHandler);
        }

        Component heading = rollup.getHeading();

        if (heading != null) {
            heading.getComponentMouseButtonListeners().add(headingMouseButtonHandler);
        }

        invalidateComponent();
    }

    @Override
    public void contentChanged(Rollup rollup, Component previousContent) {
        if (rollup.getContent() == null && useBullet) {
            rollupButton.setCursor(Cursor.DEFAULT);
        } else {
            rollupButton.setCursor(Cursor.HAND);
        }

        rollupButton.repaint();

        if (rollup.isExpanded()) {
            invalidateComponent();
        }
    }

    // RollupStateListener methods

    @Override
    public Vote previewExpandedChange(final Rollup rollup) {
        Vote vote = Vote.APPROVE;

        if (rollup.getDisplay() != null) {
            if (rollup.isExpanded()) {
                // Start a collapse transition, return false, and set the
                // expanded state when the transition is complete
                if (collapseTransition == null) {
                    int duration = EXPANSION_DURATION;
                    int height1 = getHeight();

                    if (expandTransition != null) {
                        // Stop the expand transition
                        expandTransition.stop();

                        // Record its progress so we can reverse it at the right point
                        duration = expandTransition.getElapsedTime();
                        height1 = expandTransition.getHeight();

                        expandTransition = null;
                    }

                    if (duration > 0) {
                        int height2 = getPreferredHeight(-1, false);

                        collapseTransition = new ExpansionTransition(height1, height2,
                            true, duration, EXPANSION_RATE);
                        collapseTransition.start(new TransitionListener() {
                            public void transitionCompleted(Transition transition) {
                                rollup.setExpanded(false);
                                collapseTransition = null;
                            }
                        });

                        vote = Vote.DEFER;
                    }
                } else {
                    vote = collapseTransition.isRunning() ? Vote.DEFER : Vote.APPROVE;
                }
            }
        }

        return vote;
    }

    @Override
    public void expandedChangeVetoed(Rollup rollup, Vote reason) {
        if (reason == Vote.DENY
            && collapseTransition != null) {
            collapseTransition.stop();
            collapseTransition = null;
        }
    }

    @Override
    public void expandedChanged(Rollup rollup) {
        invalidateComponent();

        if (rollup.getDisplay() != null) {
            if (rollup.isExpanded()) {
                // Start an expansion transition
                int height1 = getHeight();
                int height2 = getPreferredHeight(-1, true);

                expandTransition = new ExpansionTransition(height1, height2,
                    false, EXPANSION_DURATION, EXPANSION_RATE);
                expandTransition.start(new TransitionListener() {
                    public void transitionCompleted(Transition transition) {
                        expandTransition = null;
                    }
                });
            }
        }
    }
}
