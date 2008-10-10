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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

import pivot.collections.Sequence;
import pivot.util.Vote;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Container;
import pivot.wtk.Cursor;
import pivot.wtk.Dimensions;
import pivot.wtk.Mouse;
import pivot.wtk.PushButton;
import pivot.wtk.Rollup;
import pivot.wtk.RollupListener;
import pivot.wtk.Theme;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;
import pivot.wtk.effects.easing.Easing;
import pivot.wtk.effects.easing.Quadratic;
import pivot.wtk.media.Image;
import pivot.wtk.skin.ButtonSkin;
import pivot.wtk.skin.ContainerSkin;

/**
 * Rollup skin.
 * <p>
 * TODO Optimize this class by performing preferred size calculation in one
 * pass.
 *
 * @author tvolkert
 */
public class TerraRollupSkin extends ContainerSkin
    implements RollupListener, ButtonPressListener {
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

                Rollup rollup = (Rollup)getComponent();
                rollup.setPreferredHeight(height);
            }
        }
    }

    protected class RollupButton extends PushButton {
        public RollupButton() {
            super(null);
            setSkin(new RollupButtonSkin());
        }
    }

    protected class RollupButtonSkin extends ButtonSkin {
        @Override
        public boolean isFocusable() {
            return false;
        }

        public int getPreferredWidth(int height) {
            RollupButton rollupButton = (RollupButton)getComponent();
            Button.DataRenderer dataRenderer = rollupButton.getDataRenderer();
            dataRenderer.render(rollupButton.getButtonData(), rollupButton, false);
            return dataRenderer.getPreferredWidth(height);
        }

        public int getPreferredHeight(int width) {
            RollupButton rollupButton = (RollupButton)getComponent();
            Button.DataRenderer dataRenderer = rollupButton.getDataRenderer();
            dataRenderer.render(rollupButton.getButtonData(), rollupButton, false);
            return dataRenderer.getPreferredHeight(width);
        }

        public Dimensions getPreferredSize() {
            RollupButton rollupButton = (RollupButton)getComponent();
            Button.DataRenderer dataRenderer = rollupButton.getDataRenderer();
            dataRenderer.render(rollupButton.getButtonData(), rollupButton, false);
            Dimensions contentSize = dataRenderer.getPreferredSize();
            return new Dimensions(contentSize.width, contentSize.height);
        }

        public void paint(Graphics2D graphics) {
            RollupButton rollupButton = (RollupButton)getComponent();

            // Paint the content
            Button.DataRenderer dataRenderer = rollupButton.getDataRenderer();
            dataRenderer.render(rollupButton.getButtonData(), rollupButton, false);

            Dimensions contentSize = dataRenderer.getPreferredSize();
            dataRenderer.setSize(contentSize.width, contentSize.height);
            dataRenderer.paint(graphics);
        }

        @Override
        public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            PushButton pushButton = (PushButton)getComponent();
            pushButton.press();
        }
    }

    protected abstract class ButtonImage extends Image {
        public int getWidth() {
            return 7;
        }

        public int getHeight() {
            return 7;
        }
    }

    protected class ExpandImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            graphics.setStroke(new BasicStroke(0));
            graphics.setPaint(buttonColor);

            GeneralPath shape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
            shape.moveTo(0, 0);
            shape.lineTo(6, 3);
            shape.lineTo(0, 6);
            shape.closePath();

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.draw(shape);
            graphics.fill(shape);
        }
    }

    protected class CollapseImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            graphics.setStroke(new BasicStroke(0));
            graphics.setPaint(buttonColor);

            GeneralPath shape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
            shape.moveTo(0, 0);
            shape.lineTo(3, 6);
            shape.lineTo(6, 0);
            shape.closePath();

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.draw(shape);
            graphics.fill(shape);
        }
    }

    protected class BulletImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            graphics.setStroke(new BasicStroke(0));
            graphics.setPaint(buttonColor);

            RoundRectangle2D.Double shape = new RoundRectangle2D.Double(1, 1, 4, 4, 2, 2);

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.draw(shape);
            graphics.fill(shape);
        }
    }

    private class ToggleComponentMouseHandler
        implements ComponentMouseButtonListener {
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            Rollup rollup = (Rollup)getComponent();
            rollup.setExpanded(!rollup.isExpanded());
        }
    }

    private RollupButton rollupButton = null;
    private Component toggleComponent = null;
    private ToggleComponentMouseHandler toggleComponentMouseHandler =
        new ToggleComponentMouseHandler();
    private ExpandImage expandImage = new ExpandImage();
    private CollapseImage collapseImage = new CollapseImage();
    private BulletImage bulletImage = new BulletImage();

    private ExpansionTransition expandTransition = null;
    private ExpansionTransition collapseTransition = null;

    private Color buttonColor;
    private int spacing;
    private int buffer;
    private boolean justify;
    private boolean firstChildToggles;

    private static final int EXPANSION_DURATION = 250;
    private static final int EXPANSION_RATE = 30;

    public TerraRollupSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        buttonColor = theme.getColor(4);
        spacing = 4;
        buffer = 4;
        justify = false;
        firstChildToggles = true;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Rollup rollup = (Rollup)component;
        rollup.getRollupListeners().add(this);

        updateToggleComponent();

        rollupButton = new RollupButton();
        updateRollupButton();
        rollup.add(rollupButton);
        rollupButton.getButtonPressListeners().add(this);
    }

    @Override
    public void uninstall() {
        Rollup rollup = (Rollup)getComponent();
        rollup.getRollupListeners().remove(this);

        rollupButton.getButtonPressListeners().remove(this);
        rollup.remove(rollupButton);
        rollupButton = null;

        if (toggleComponent != null) {
            toggleComponent.getComponentMouseButtonListeners().remove(toggleComponentMouseHandler);
            toggleComponent = null;
        }

        super.uninstall();
    }

    @Override
    public int getPreferredWidth(int height) {
        Rollup rollup = (Rollup)getComponent();

        int preferredWidth = 0;

        // Preferred width is the max of our childrens' preferred widths, plus
        // the button width, buffer, and padding. If we're collapsed, we only
        // look at the first child.
        for (int i = 0, n = rollup.getLength(); i < n; i++) {
            Component component = rollup.get(i);

            if (component == rollupButton) {
                // Ignore "private" component
                continue;
            }

            if (component.isDisplayable()) {
                int componentPreferredWidth = component.getPreferredWidth(-1);
                preferredWidth = Math.max(preferredWidth, componentPreferredWidth);
            }

            if (!rollup.isExpanded()) {
                // If we're collapsed, we only look at the first child.
                break;
            }
        }

        preferredWidth += rollupButton.getPreferredWidth(-1) + buffer;

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        Rollup rollup = (Rollup)getComponent();
        return getPreferredHeight(width, rollup.isExpanded());
    }

    private int getPreferredHeight(int width, boolean expanded) {
        Rollup rollup = (Rollup)getComponent();

        // Preferred height is the sum of our childrens' preferred heights,
        // plus spacing and padding.
        Dimensions rollupButtonPreferredSize = rollupButton.getPreferredSize();

        if (justify
            && width != -1) {
            width = Math.max(width - rollupButtonPreferredSize.width - buffer, 0);
        } else {
            width = -1;
        }

        int preferredHeight = 0;

        int displayableComponentCount = 0;
        for (int i = 0, n = rollup.getLength(); i < n; i++) {
            Component component = rollup.get(i);

            if (component == rollupButton) {
                // Ignore "private" component
                continue;
            }

            if (component.isDisplayable()) {
                preferredHeight += component.getPreferredHeight(width);
                displayableComponentCount++;
            }

            if (!expanded) {
                // If we're collapsed, we only look at the first child.
                break;
            }
        }

        if (displayableComponentCount > 0) {
            preferredHeight += (displayableComponentCount - 1) * spacing;
        }

        preferredHeight = Math.max(preferredHeight,
            rollupButtonPreferredSize.height);

        return preferredHeight;
    }

    public void layout() {
        Rollup rollup = (Rollup)getComponent();
        Dimensions rollupButtonSize = rollupButton.getPreferredSize();
        rollupButton.setSize(rollupButtonSize);

        int x = rollupButtonSize.width + buffer;
        int y = 0;
        int justifiedWidth = Math.max(getWidth() - rollupButtonSize.width - buffer, 0);

        Component firstComponent = null;

        for (int i = 0, n = rollup.getLength(); i < n; i++) {
            Component component = rollup.get(i);

            if (component == rollupButton) {
                // Ignore "private" component
                continue;
            }

            if (firstComponent == null) {
                firstComponent = component;
            }

            if ((component == firstComponent
                || rollup.isExpanded())
                && component.isDisplayable()) {
                // We lay this child out and make sure it's painted.
                component.setVisible(true);

                int componentWidth, componentHeight;
                if (justify) {
                    componentWidth = justifiedWidth;
                    componentHeight = component.getPreferredHeight(componentWidth);
                } else {
                    Dimensions componentPreferredSize = component.getPreferredSize();
                    componentWidth = componentPreferredSize.width;
                    componentHeight = componentPreferredSize.height;
                }

                component.setLocation(x, y);
                component.setSize(componentWidth, componentHeight);

                y += componentHeight + spacing;
            } else {
                // We make sure this child doesn't get painted.  There's also
                // no need to lay the child out.
                component.setVisible(false);
            }
        }

        int rollupButtonY = (firstComponent == null) ?
            0 : (firstComponent.getHeight() - rollupButtonSize.height) / 2 + 1;

        rollupButton.setLocation(0, rollupButtonY);
    }

    public Color getButtonColor() {
        return buttonColor;
    }

    public void setButtonColor(Color buttonColor) {
        this.buttonColor = buttonColor;
        repaintComponent();
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
        invalidateComponent();
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

    public boolean getFirstChildToggles() {
        return firstChildToggles;
    }

    public void setFirstChildToggles(boolean firstChildToggles) {
        this.firstChildToggles = firstChildToggles;
        updateToggleComponent();
    }

    private void updateRollupButton() {
        Rollup rollup = (Rollup)getComponent();

        Image buttonData = null;
        Cursor cursor = Cursor.HAND;

        // Make sure to account for rollupButton
        if (rollup.getLength() == 2) {
            buttonData = bulletImage;
            cursor = Cursor.DEFAULT;
        } else if (rollup.isExpanded()) {
            buttonData = collapseImage;
        } else {
            buttonData = expandImage;
        }

        rollupButton.setButtonData(buttonData);
        rollupButton.setCursor(cursor);
    }

    private void updateToggleComponent() {
        Rollup rollup = (Rollup)getComponent();
        Component previousToggleComponent = toggleComponent;

        toggleComponent = null;
        if (firstChildToggles) {
            for (int i = 0, n = rollup.getLength(); i < n; i++) {
                Component child = rollup.get(i);
                if (child != rollupButton) {
                    toggleComponent = child;
                    break;
                }
            }
        }

        if (toggleComponent != null
            && rollup.getLength() > 2) {
            // TODO Record original cursor
            toggleComponent.setCursor(Cursor.HAND);
        }

        if (toggleComponent != previousToggleComponent) {
            if (previousToggleComponent != null) {
                // TODO Restore original cursor
                previousToggleComponent.setCursor(Cursor.DEFAULT);

                previousToggleComponent.getComponentMouseButtonListeners().remove(toggleComponentMouseHandler);
            }

            if (toggleComponent != null) {
                toggleComponent.getComponentMouseButtonListeners().add(toggleComponentMouseHandler);
            }
        }
    }

    // Container events
    @Override
    public void componentInserted(Container container, int index) {
        super.componentInserted(container, index);

        updateRollupButton();
        updateToggleComponent();
    }

    @Override
    public void componentsRemoved(Container container, int index, Sequence<Component> components) {
        super.componentsRemoved(container, index, components);

        updateRollupButton();
        updateToggleComponent();
    }

    // RollupListener methods

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

    public void expandedChangeVetoed(Rollup rollup, Vote reason) {
        if (reason == Vote.DENY
            && collapseTransition != null) {
            collapseTransition.stop();
            collapseTransition = null;
        }
    }

    public void expandedChanged(Rollup rollup) {
        updateRollupButton();
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

    // ButtonPressListener methods

    public void buttonPressed(Button button) {
        Rollup rollup = (Rollup)getComponent();
        rollup.setExpanded(!rollup.isExpanded());
    }
}
