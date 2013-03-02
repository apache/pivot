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
import java.awt.geom.Rectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.SpinnerListener;
import org.apache.pivot.wtk.SpinnerSelectionListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.ComponentSkin;
import org.apache.pivot.wtk.skin.ContainerSkin;

/**
 * Spinner skin.
 */
public class TerraSpinnerSkin extends ContainerSkin implements Spinner.Skin,
    SpinnerListener, SpinnerSelectionListener {
    /**
     * Encapsulates the code needed to perform timer-controlled spinning.
     */
    private static class AutomaticSpinner {
        public Spinner spinner;
        public int direction;

        private ApplicationContext.ScheduledCallback scheduledSpinnerCallback = null;

        /**
         * Starts spinning the specified spinner.
         *
         * @param spinnerArgument
         * The spinner to spin
         *
         * @param directionArgument
         * <tt>1</tt> to adjust the spinner's selected index larger;
         * <tt>-1</tt> to adjust it smaller
         *
         * @exception IllegalStateException
         * If automatic spinner of any spinner is already in progress.
         * Only one spinner may be automatically spun at one time
         */
        public void start(Spinner spinnerArgument, int directionArgument) {
            assert(directionArgument != 0) : "Direction must be positive or negative";

            if (scheduledSpinnerCallback != null) {
                throw new IllegalStateException("Already running");
            }

            this.spinner = spinnerArgument;
            this.direction = directionArgument;

            // Wait a timeout period, then begin rapidly spinning
            scheduledSpinnerCallback = ApplicationContext.scheduleRecurringCallback(new Runnable() {
                @Override
                public void run() {
                    spin();
                }
            }, 400, 30);

            // We initially spin once to register that we've started
            spin();
        }

        private void spin() {
            boolean circular = spinner.isCircular();
            int selectedIndex = spinner.getSelectedIndex();
            int count = spinner.getSpinnerData().getLength();
            if (count < 1) {
                // empty spinner
                stop();
                return ;
            }

            if (direction > 0) {
                if (selectedIndex < count - 1) {
                    spinner.setSelectedIndex(selectedIndex + 1);
                } else if (circular) {
                    spinner.setSelectedIndex(0);
                } else {
                    stop();
                }
            } else {
                if (selectedIndex > 0) {
                    spinner.setSelectedIndex(selectedIndex - 1);
                } else if (circular) {
                    spinner.setSelectedIndex(count - 1);
                } else {
                    stop();
                }
            }
        }

        /**
         * Stops any automatic spinning in progress.
         */
        public void stop() {
            if (scheduledSpinnerCallback != null) {
                scheduledSpinnerCallback.cancel();
                scheduledSpinnerCallback = null;
            }
        }
    }

    /**
     * Component that holds the content of a spinner. It is the focusable part
     * of a spinner.
     */
    protected class SpinnerContent extends Component {
        public SpinnerContent() {
            setSkin(new SpinnerContentSkin());
        }
    }

    /**
     * SpinnerContent skin.
     */
    protected class SpinnerContentSkin extends ComponentSkin {
        @Override
        public int getPreferredWidth(int height) {
            int preferredWidth = 0;

            Spinner spinner = (Spinner)TerraSpinnerSkin.this.getComponent();
            Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();

            if (sizeToContent) {
                List<?> spinnerData = spinner.getSpinnerData();
                for (Object item : spinnerData) {
                    itemRenderer.render(item, spinner);
                    preferredWidth = Math.max(preferredWidth, itemRenderer.getPreferredWidth(height));
                }
            } else {
                itemRenderer.render(spinner.getSelectedItem(), spinner);
                preferredWidth = itemRenderer.getPreferredWidth(height);
            }

            return preferredWidth;
        }

        @Override
        public int getPreferredHeight(int width) {
            int preferredHeight = 0;

            Spinner spinner = (Spinner)TerraSpinnerSkin.this.getComponent();
            Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();

            itemRenderer.render(spinner.getSelectedItem(), spinner);
            preferredHeight = itemRenderer.getPreferredHeight(width);

            return preferredHeight;
        }

        @Override
        public int getBaseline(int width, int height) {
            Spinner spinner = (Spinner)TerraSpinnerSkin.this.getComponent();

            int baseline = -1;

            Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();
            itemRenderer.render(spinner.getSelectedItem(), spinner);
            baseline = itemRenderer.getBaseline(width, height);

            return baseline;
        }

        @Override
        public Dimensions getPreferredSize() {
            Dimensions preferredSize;

            Spinner spinner = (Spinner)TerraSpinnerSkin.this.getComponent();
            Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();

            if (sizeToContent) {
                preferredSize = new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
            } else {
                itemRenderer.render(spinner.getSelectedItem(), spinner);
                preferredSize = itemRenderer.getPreferredSize();
            }

            return preferredSize;
        }

        @Override
        public void layout() {
            // No-op
        }

        @Override
        public void paint(Graphics2D graphics) {
            SpinnerContent spinnerContentLocal = (SpinnerContent)getComponent();
            Spinner spinner = (Spinner)TerraSpinnerSkin.this.getComponent();

            int width = getWidth();
            int height = getHeight();

            // Paint the content
            Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();
            itemRenderer.render(spinner.getSelectedItem(), spinner);

            Graphics2D contentGraphics = (Graphics2D)graphics.create();
            itemRenderer.setSize(width, height);
            itemRenderer.paint(contentGraphics);
            contentGraphics.dispose();

            // Paint the focus state
            if (spinnerContentLocal.isFocused()) {
                BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

                graphics.setStroke(dashStroke);
                graphics.setColor(borderColor);

                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                graphics.draw(new Rectangle2D.Double(1, 1.5, Math.max(width - 2.5, 0),
                    Math.max(height - 3, 0)));
            }
        }

        @Override
        public boolean isOpaque() {
            return false;
        }

        @Override
        public void focusedChanged(Component component, Component obverseComponent) {
            super.focusedChanged(component, obverseComponent);

            repaintComponent();
        }

        /**
         * {@link KeyCode#UP UP} Select the previous spinner item.<br>
         * {@link KeyCode#DOWN DOWN} Select the next spinner item.
         */
        @Override
        public boolean keyPressed(Component component, int keyCode,
            Keyboard.KeyLocation keyLocation) {
            boolean consumed = false;

            Spinner spinner = (Spinner)TerraSpinnerSkin.this.getComponent();

            boolean circular = spinner.isCircular();
            int count = spinner.getSpinnerData().getLength();

            int selectedIndex = spinner.getSelectedIndex();
            int newSelectedIndex = selectedIndex;

            if (keyCode == Keyboard.KeyCode.UP) {
                if (selectedIndex < count - 1) {
                    newSelectedIndex++;
                } else if (circular) {
                    newSelectedIndex = 0;
                }
            } else if (keyCode == Keyboard.KeyCode.DOWN) {
                if (selectedIndex > 0) {
                    newSelectedIndex--;
                } else if (circular) {
                    newSelectedIndex = count - 1;
                }
            } else {
                consumed = super.keyPressed(component, keyCode, keyLocation);
            }

            if (newSelectedIndex != selectedIndex) {
                spinner.setSelectedIndex(newSelectedIndex);
                consumed = true;
            }

            return consumed;
        }

        /**
         * Select the next spinner item where the first character of the
         * rendered text matches the typed key (case insensitive).
         */
        @Override
        public boolean keyTyped(Component component, char character) {
            boolean consumed = super.keyTyped(component, character);

            Spinner spinner = (Spinner)TerraSpinnerSkin.this.getComponent();
            List<?> spinnerData = spinner.getSpinnerData();
            Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();

            char characterUpper = Character.toUpperCase(character);

            for (int i = spinner.getSelectedIndex() + 1, n = spinnerData.getLength(); i < n; i++) {
                String string = itemRenderer.toString(spinnerData.get(i));

                if (string != null
                    && string.length() > 0) {
                    char first = Character.toUpperCase(string.charAt(0));

                    if (first == characterUpper) {
                        spinner.setSelectedIndex(i);
                        consumed = true;
                        break;
                    }
                }
            }

            return consumed;
        }
    }

    /**
     * Spinner button.
     */
    protected class SpinButton extends Component {
        private int direction;
        private Image buttonImage;

        public SpinButton(int direction, Image buttonImage) {
            this.direction = direction;
            this.buttonImage = buttonImage;

            setSkin(new SpinButtonSkin());
        }

        public int getDirection() {
            return direction;
        }

        public Image getButtonImage() {
            return buttonImage;
        }
    }

    /**
     * Spinner button skin.
     */
    protected class SpinButtonSkin extends ComponentSkin {
        private boolean highlighted = false;
        private boolean pressed = false;

        @Override
        public int getPreferredWidth(int height) {
            return BUTTON_IMAGE_SIZE + 6;
        }

        @Override
        public int getPreferredHeight(int width) {
            return BUTTON_IMAGE_SIZE + 2;
        }

        @Override
        public void layout() {
            // No-op
        }

        @Override
        public void paint(Graphics2D graphics) {
            // Apply spinner styles to the button
            SpinButton spinButton = (SpinButton)getComponent();

            int width = getWidth();
            int height = getHeight();

            // Paint the background
            float alpha = pressed ? 0.5f : highlighted ? 0.25f : 0.0f;
            graphics.setPaint(new Color(0, 0, 0, alpha));
            graphics.fillRect(0, 0, width, height);

            // Paint the image
            SpinButtonImage buttonImage = (SpinButtonImage)spinButton.getButtonImage();
            graphics.translate((width - BUTTON_IMAGE_SIZE) / 2,
                (height - BUTTON_IMAGE_SIZE) / 2);
            buttonImage.paint(graphics);
        }

        @Override
        public boolean isFocusable() {
            return false;
        }

        @Override
        public boolean isOpaque() {
            return false;
        }

        @Override
        public void enabledChanged(Component component) {
            super.enabledChanged(component);

            automaticSpinner.stop();

            pressed = false;
            highlighted = false;
            repaintComponent();
        }

        @Override
        public void mouseOver(Component component) {
            super.mouseOver(component);

            highlighted = true;
            repaintComponent();
        }

        @Override
        public void mouseOut(Component component) {
            super.mouseOut(component);

            automaticSpinner.stop();

            pressed = false;
            highlighted = false;
            repaintComponent();
        }

        @Override
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseDown(component, button, x, y);

            if (button == Mouse.Button.LEFT) {
                SpinButton spinButton = (SpinButton)getComponent();
                Spinner spinner = (Spinner)TerraSpinnerSkin.this.getComponent();

                // Start the automatic spinner. It'll be stopped when we
                // mouse up or mouse out
                automaticSpinner.start(spinner, spinButton.getDirection());

                pressed = true;
                repaintComponent();
            }

            return consumed;
        }

        @Override
        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseUp(component, button, x, y);

            if (button == Mouse.Button.LEFT) {
                automaticSpinner.stop();

                pressed = false;
                repaintComponent();
            }

            return consumed;
        }
    }

    /**
     * Abstract base class for button images.
     */
    protected abstract class SpinButtonImage extends Image {
        @Override
        public int getWidth() {
            return BUTTON_IMAGE_SIZE;
        }

        @Override
        public int getHeight() {
            return BUTTON_IMAGE_SIZE;
        }

        @Override
        public void paint(Graphics2D graphics) {
            graphics.setStroke(new BasicStroke(0));
            graphics.setPaint(buttonColor);
        }
    }

    protected class SpinUpImage extends SpinButtonImage {
        @Override
        public void paint(Graphics2D graphics) {
            super.paint(graphics);

            int[] xPoints = {0, 2, 4};
            int[] yPoints = {3, 1, 3};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    protected class SpinDownImage extends SpinButtonImage {
        @Override
        public void paint(Graphics2D graphics) {
            super.paint(graphics);

            int[] xPoints = {0, 2, 4};
            int[] yPoints = {1, 3, 1};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    private SpinnerContent spinnerContent = new SpinnerContent();
    private SpinButton upButton = new SpinButton(1, new SpinUpImage());
    private SpinButton downButton = new SpinButton(-1, new SpinDownImage());

    private Font font;
    private Color color;
    private Color disabledColor;
    private Color borderColor;
    private Color buttonColor;
    private Color buttonBackgroundColor;
    private boolean sizeToContent = false;

    // Derived colors
    private Color buttonBevelColor;

    private static AutomaticSpinner automaticSpinner = new AutomaticSpinner();

    public static final int BUTTON_IMAGE_SIZE = 5;

    public TerraSpinnerSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(4));

        font = theme.getFont();
        color = theme.getColor(1);
        disabledColor = theme.getColor(7);
        borderColor = theme.getColor(7);
        buttonColor = theme.getColor(1);
        buttonBackgroundColor = theme.getColor(10);

        buttonBevelColor = TerraTheme.brighten(buttonBackgroundColor);
    }

    @Override
    public void setSize(int width, int height) {
        int previousWidth = getWidth();
        int previousHeight = getHeight();

        super.setSize(width, height);

        if (previousWidth != width
            || previousHeight != height) {
            automaticSpinner.stop();
        }
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Spinner spinner = (Spinner)component;
        spinner.getSpinnerListeners().add(this);
        spinner.getSpinnerSelectionListeners().add(this);

        spinner.add(spinnerContent);
        spinner.add(upButton);
        spinner.add(downButton);
    }

    @Override
    public int getPreferredWidth(int height) {
        // Preferred width is the sum of our maximum button width plus the
        // content width, plus the border

        // Border thickness
        int preferredWidth = 2;

        int buttonHeight = (height < 0 ? -1 : height / 2);
        preferredWidth += Math.max(upButton.getPreferredWidth(buttonHeight),
            downButton.getPreferredWidth(buttonHeight));

        if (height >= 0) {
            // Subtract border thickness from height constraint
            height = Math.max(height - 2, 0);
        }

        preferredWidth += spinnerContent.getPreferredWidth(height);

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        // Preferred height is the maximum of the button height and the
        // renderer's preferred height (plus the border), where button
        // height is defined as the larger of the two buttons' preferred
        // height, doubled.

        Dimensions upButtonPreferredSize = upButton.getPreferredSize();
        Dimensions downButtonPreferredSize = downButton.getPreferredSize();

        int preferredHeight = Math.max(upButtonPreferredSize.height,
            downButtonPreferredSize.height) * 2;

        if (width >= 0) {
            // Subtract the button and border width from width constraint
            int buttonWidth = Math.max(upButtonPreferredSize.width,
                downButtonPreferredSize.width);

            width = Math.max(width - buttonWidth - 2, 0);
        }

        preferredHeight = Math.max(preferredHeight,
            spinnerContent.getPreferredHeight(width)) + 1;

        return preferredHeight;
    }

    @Override
    public int getBaseline(int width, int height) {
        Dimensions upButtonPreferredSize = upButton.getPreferredSize();
        Dimensions downButtonPreferredSize = downButton.getPreferredSize();
        int buttonWidth = Math.max(upButtonPreferredSize.width,
            downButtonPreferredSize.width);

        int clientWidth = Math.max(width - buttonWidth - 2, 0);
        int clientHeight = Math.max(height - 2, 0);

        int baseline = spinnerContent.getBaseline(clientWidth, clientHeight);

        if (baseline != -1) {
            baseline += 1;
        }

        return baseline;
    }

    @Override
    public void layout() {
        int width = getWidth();
        int height = getHeight();

        int buttonHeight = Math.max((height - 3) / 2, 0);
        int buttonWidth = Math.max(upButton.getPreferredWidth(buttonHeight),
            downButton.getPreferredWidth(buttonHeight));

        spinnerContent.setSize(Math.max(width - buttonWidth - 3, 0), Math.max(height - 2, 0));
        spinnerContent.setLocation(1, 1);

        upButton.setSize(buttonWidth, buttonHeight);
        upButton.setLocation(width - buttonWidth - 1, 1);

        downButton.setSize(buttonWidth, Math.max(height - buttonHeight - 3, 0));
        downButton.setLocation(width - buttonWidth - 1, buttonHeight + 2);
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        int width = getWidth();
        int height = getHeight();

        int buttonX = upButton.getX();
        int buttonWidth = upButton.getWidth();
        int buttonHeight = upButton.getHeight();

        graphics.setPaint(new GradientPaint(buttonX + buttonWidth / 2, 0, buttonBevelColor,
            buttonX + buttonWidth / 2, buttonHeight, buttonBackgroundColor));
        graphics.fillRect(buttonX, 0, buttonWidth, height);

        graphics.setPaint(borderColor);
        GraphicsUtilities.drawRect(graphics, 0, 0, width, height);
        GraphicsUtilities.drawLine(graphics, width - buttonWidth - 2, 0,
            height, Orientation.VERTICAL);
        GraphicsUtilities.drawLine(graphics, width - buttonWidth - 2, buttonHeight + 1,
            buttonWidth + 1, Orientation.HORIZONTAL);
    }

    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);
        repaintComponent();
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        spinnerContent.requestFocus();
        return false;
    }

    protected void invalidateContent() {
        spinnerContent.invalidate();
        spinnerContent.repaint();
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.color = color;

        repaintComponent();
    }

    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(GraphicsUtilities.decodeColor(color));
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(Color disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        this.disabledColor = disabledColor;

        repaintComponent();
    }

    public final void setDisabledColor(String disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        setDisabledColor(GraphicsUtilities.decodeColor(disabledColor));
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        this.borderColor = borderColor;

        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        setBorderColor(GraphicsUtilities.decodeColor(borderColor));
    }

    public Color getButtonColor() {
        return buttonColor;
    }

    public void setButtonImageColor(Color buttonColor) {
        this.buttonColor = buttonColor;
        repaintComponent();
    }

    public final void setButtonColor(String buttonColor) {
        if (buttonColor == null) {
            throw new IllegalArgumentException("buttonColor is null");
        }

        setButtonImageColor(GraphicsUtilities.decodeColor(buttonColor));
    }

    public Color getButtonBackgroundColor() {
        return buttonBackgroundColor;
    }

    public void setButtonBackgroundColor(Color buttonBackgroundColor) {
        this.buttonBackgroundColor = buttonBackgroundColor;
        repaintComponent();
    }

    public final void setButtonBackgroundColor(String buttonBackgroundColor) {
        if (buttonBackgroundColor == null) {
            throw new IllegalArgumentException("buttonBackgroundColor is null");
        }

        setButtonBackgroundColor(GraphicsUtilities.decodeColor(buttonBackgroundColor));
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        this.font = font;

        invalidateContent();
    }

    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(decodeFont(font));
    }

    public final void setFont(Dictionary<String, ?> font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Theme.deriveFont(font));
    }

    public boolean isSizeToContent() {
        return sizeToContent;
    }

    public void setSizeToContent(boolean sizeToContent) {
        this.sizeToContent = sizeToContent;
        invalidateContent();
    }

    // Spinner.Skin methods

    @Override
    public Bounds getContentBounds() {
        return spinnerContent.getBounds();
    }

    // SpinnerListener methods

    @Override
    public void spinnerDataChanged(Spinner spinner, List<?> previousSpinnerData) {
        invalidateContent();
    }

    @Override
    public void itemRendererChanged(Spinner spinner,
        Spinner.ItemRenderer previousItemRenderer) {
        invalidateContent();
    }

    @Override
    public void circularChanged(Spinner spinner) {
        // No-op
    }

    // SpinnerSelectionListener methods
    @Override
    public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex) {
        // No-op
    }

    @Override
    public void selectedItemChanged(Spinner spinner, Object previousSelectedItem) {
        invalidateContent();
    }
}
