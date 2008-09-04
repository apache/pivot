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
import pivot.wtk.media.Image;
import pivot.wtk.skin.ButtonSkin;
import pivot.wtk.skin.ContainerSkin;

/**
 * Rollup skin.
 *
 * @author tvolkert
 */
public class RollupSkin extends ContainerSkin
    implements RollupListener, ButtonPressListener {
    public static class RollupButton extends PushButton {
        private Rollup rollup;

        public RollupButton(Rollup rollup) {
            this(rollup, null);
        }

        public RollupButton(Rollup rollup, Object buttonData) {
            super(buttonData);

            this.rollup = rollup;

            installSkin(RollupButton.class);
        }

        public Rollup getRollup() {
            return rollup;
        }
    }

    public static class RollupButtonSkin extends ButtonSkin {
        @Override
        public void install(Component component) {
            validateComponentType(component, RollupButton.class);

            super.install(component);
        }

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
        public void mouseClick(Mouse.Button button, int x, int y, int count) {
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
        public void mouseDown(Component component, Mouse.Button button, int x, int y) {
        }

        public void mouseUp(Component component, Mouse.Button button, int x, int y) {
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

    private Color buttonColor = new Color(0xcc, 0xca, 0xc2);
    private int spacing = 4;
    private int buffer = 4;
    private boolean firstChildToggles = true;

    @Override
    public void install(Component component) {
        validateComponentType(component, Rollup.class);

        super.install(component);

        Rollup rollup = (Rollup)component;
        rollup.getRollupListeners().add(this);

        updateToggleComponent();

        rollupButton = new RollupButton(rollup);
        updateRollupButton();
        rollup.add(rollupButton);
        rollupButton.getButtonPressListeners().add(this);
    }

    @Override
    public void uninstall() {
        Rollup rollup = (Rollup)getComponent();
        rollup.getRollupListeners().remove(this);

        toggleComponent = null;

        rollupButton.getButtonPressListeners().remove(this);
        rollup.remove(rollupButton);
        rollupButton = null;

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

        // Preferred height is the sum of our childrens' preferred heights,
        // plus spacing and padding.
        Dimensions rollupButtonSize = rollupButton.getPreferredSize();

        if (width != -1) {
            width -= rollupButtonSize.width + buffer;
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

            if (!rollup.isExpanded()) {
                // If we're collapsed, we only look at the first child.
                break;
            }
        }

        if (displayableComponentCount > 0) {
            preferredHeight += (displayableComponentCount - 1) * spacing;
        }

        preferredHeight = Math.max(preferredHeight, rollupButtonSize.height);

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        // TODO Optimize
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        Rollup rollup = (Rollup)getComponent();
        Dimensions rollupButtonSize = rollupButton.getPreferredSize();
        rollupButton.setSize(rollupButtonSize);

        int x = rollupButtonSize.width + buffer;
        int y = 0;
        int componentWidth = Math.max(getWidth() - rollupButtonSize.width - buffer, 0);

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

                int componentHeight = component.getPreferredHeight(componentWidth);

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

        setButtonColor(Color.decode(buttonColor));
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

    // Rollup events
    public void expandedChanged(Rollup rollup) {
        updateRollupButton();

        invalidateComponent();
    }

    // Button press event
    public void buttonPressed(Button button) {
        Rollup rollup = (Rollup)getComponent();
        rollup.setExpanded(!rollup.isExpanded());
    }
}
