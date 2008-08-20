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
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

import pivot.collections.List;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.Rectangle;
import pivot.wtk.Spinner;
import pivot.wtk.SpinnerListener;
import pivot.wtk.SpinnerSelectionListener;
import pivot.wtk.media.Image;
import pivot.wtk.skin.ComponentSkin;
import pivot.wtk.skin.ContainerSkin;

/**
 * Spinner skin
 *
 * @author tvolkert
 */
public class SpinnerSkin extends ContainerSkin implements Spinner.Skin,
    SpinnerListener, SpinnerSelectionListener {

    /**
     * Encapsulates the code needed to perform timer-controlled spinning.
     */
    private static class AutomaticSpinner {
        public Spinner spinner;
        public int direction;

        private int timeoutID = -1;
        private int intervalID = -1;

        /**
         * Starts spinning the specified spinner.
         *
         * @param spinner
         * The spinner to spin
         *
         * @param direction
         * <tt>1</tt> to adjust the spinner's selected index larger;
         * <tt>-1</tt> to adjust it smaller
         *
         * @exception IllegalStateException
         * If automatic spinner of any spinner is already in progress.
         * Only one spinner may be automatically spun at one time
         */
        public void start(Spinner spinner, int direction) {
            assert(direction != 0) : "Direction must be positive or negative";

            if (timeoutID != -1
                || intervalID != -1) {
                throw new IllegalStateException("Already running");
            }

            this.spinner = spinner;
            this.direction = direction;

            // Wait a timeout period, then begin repidly spinning
            timeoutID = ApplicationContext.setTimeout(new Runnable() {
                public void run() {
                    intervalID = ApplicationContext.setInterval(new Runnable() {
                        public void run() {
                            spin();
                        }
                    }, 30);

                    timeoutID = -1;
                }
            }, 400);

            // We initially spin once to register that we've started
            spin();
        }

        private void spin() {
            boolean circular = spinner.isCircular();
            int selectedIndex = spinner.getSelectedIndex();
            int count = spinner.getSpinnerData().getLength();

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
            if (timeoutID != -1) {
                ApplicationContext.clearTimeout(timeoutID);
                timeoutID = -1;
            }

            if (intervalID != -1) {
                ApplicationContext.clearInterval(intervalID);
                intervalID = -1;
            }
        }
    }

    /**
     * Component that holds the content of a spinner. It is the focusable part
     * of a spinner.
     *
     * @author tvolkert
     */
    private class SpinnerContent extends Component {
        public SpinnerContent() {
            setSkin(new SpinnerContentSkin());
        }
    }

    /**
     * SpinnerContent skin.
     *
     * @author tvolkert
     */
    private class SpinnerContentSkin extends ComponentSkin {
        public int getPreferredWidth(int height) {
            Spinner spinner = (Spinner)SpinnerSkin.this.getComponent();
            Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();

            itemRenderer.render(spinner.getSelectedValue(), spinner);
            return itemRenderer.getPreferredWidth(height);
        }

        public int getPreferredHeight(int width) {
            Spinner spinner = (Spinner)SpinnerSkin.this.getComponent();
            Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();

            itemRenderer.render(spinner.getSelectedValue(), spinner);
            return itemRenderer.getPreferredHeight(width);
        }

        public Dimensions getPreferredSize() {
            Spinner spinner = (Spinner)SpinnerSkin.this.getComponent();
            Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();

            itemRenderer.render(spinner.getSelectedValue(), spinner);
            return itemRenderer.getPreferredSize();
        }

        public void layout() {
            // No-op
        }

        public void paint(Graphics2D graphics) {
            SpinnerContent spinnerContent = (SpinnerContent)getComponent();
            Spinner spinner = (Spinner)SpinnerSkin.this.getComponent();

            int width = getWidth();
            int height = getHeight();

            // Paint the content
            Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();
            itemRenderer.render(spinner.getSelectedValue(), spinner);

            Graphics2D contentGraphics = (Graphics2D)graphics.create();
            Dimensions rendererSize = itemRenderer.getPreferredSize();
            itemRenderer.setSize(rendererSize.width, rendererSize.height);
            itemRenderer.paint(contentGraphics);
            contentGraphics.dispose();

            // Paint the focus state
            if (spinnerContent.isFocused()) {
                BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

                graphics.setStroke(dashStroke);
                graphics.setColor(borderColor);

                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                graphics.drawRect(2, 2, Math.max(width - 5, 0),
                    Math.max(height - 5, 0));
            }
        }

        @Override
        public void enabledChanged(Component component) {
            super.enabledChanged(component);

            repaintComponent();
        }

        @Override
        public void focusedChanged(Component component, boolean temporary) {
            super.focusedChanged(component, temporary);

            repaintComponent();
        }

        @Override
        public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
            boolean consumed = true;

            Spinner spinner = (Spinner)SpinnerSkin.this.getComponent();

            boolean circular = spinner.isCircular();
            int selectedIndex = spinner.getSelectedIndex();
            int count = spinner.getSpinnerData().getLength();

            if (keyCode == Keyboard.KeyCode.UP) {
                if (selectedIndex < count - 1) {
                    spinner.setSelectedIndex(selectedIndex + 1);
                } else if (circular) {
                    spinner.setSelectedIndex(0);
                }
            } else if (keyCode == Keyboard.KeyCode.DOWN) {
                if (selectedIndex > 0) {
                    spinner.setSelectedIndex(selectedIndex - 1);
                } else if (circular) {
                    spinner.setSelectedIndex(count - 1);
                }
            } else {
                consumed = super.keyPressed(keyCode, keyLocation);
            }

            return consumed;
        }
    }

    /**
     *
     *
     * @author tvolkert
     */
    private class SpinButton extends Component {
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
     *
     *
     * @author tvolkert
     */
    private class SpinButtonSkin extends ComponentSkin {
        private boolean highlighted = false;
        private boolean pressed = false;

        @Override
        public boolean isFocusable() {
            return false;
        }

        public int getPreferredWidth(int height) {
            return 13;
        }

        public int getPreferredHeight(int width) {
            return 6;
        }

        public Dimensions getPreferredSize() {
            return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
        }

        public void layout() {
            // No-op
        }

        public void paint(Graphics2D graphics) {
            // Apply spinner styles to the button
            SpinButton spinButton = (SpinButton)getComponent();
            Spinner spinner = (Spinner)SpinnerSkin.this.getComponent();

            Color backgroundColor = null;

            if (spinner.isEnabled()) {
                if (pressed) {
                    backgroundColor = buttonPressedBackgroundColor;
                } else if (highlighted) {
                    backgroundColor = buttonHighlightedBackgroundColor;
                } else {
                    backgroundColor = buttonBackgroundColor;
                }
            } else {
                backgroundColor = buttonDisabledBackgroundColor;
            }

            int width = getWidth();
            int height = getHeight();

            // Paint the background
            graphics.setPaint(backgroundColor);
            Rectangle rectangle = new Rectangle(0, 0, width, height);
            graphics.fill(rectangle);

            // Size the image to be proportional to our size
            int buttonImageWidth = (int)Math.floor((float)width / 2f);
            int buttonImageHeight = (int)((float)height / 3f);
            Image buttonImage = spinButton.getButtonImage();
            buttonImage.setSize(buttonImageWidth, buttonImageHeight);

            // Paint the image
            Graphics2D imageGraphics = (Graphics2D)graphics.create();
            int buttonImageX = (width - buttonImageWidth) / 2;
            int buttonImageY = (height - buttonImageHeight) / 2;
            imageGraphics.translate(buttonImageX, buttonImageY);
            imageGraphics.clipRect(0, 0, buttonImageWidth, buttonImageHeight);
            buttonImage.paint(imageGraphics);
            imageGraphics.dispose();
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
        public void mouseOver() {
            super.mouseOver();

            highlighted = true;
            repaintComponent();
        }

        @Override
        public void mouseOut() {
            super.mouseOut();

            automaticSpinner.stop();

            pressed = false;
            highlighted = false;
            repaintComponent();
        }

        @Override
        public boolean mouseDown(Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseDown(button, x, y);

            if (button == Mouse.Button.LEFT) {
                SpinButton spinButton = (SpinButton)getComponent();
                Spinner spinner = (Spinner)SpinnerSkin.this.getComponent();

                // Start the automatic spinner. It'll be stopped when we
                // mouse up or mouse out
                automaticSpinner.start(spinner, spinButton.getDirection());

                pressed = true;
                repaintComponent();

                consumed = true;
            }

            return consumed;
        }

        @Override
        public boolean mouseUp(Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseUp(button, x, y);

            if (button == Mouse.Button.LEFT) {
                automaticSpinner.stop();

                pressed = false;
                repaintComponent();
            }

            return consumed;
        }
    }

    protected abstract class SpinButtonImage extends ImageAsset {
        public int getPreferredWidth(int height) {
            // The image never gets consulted for its preferred size
            return 0;
        }

        public int getPreferredHeight(int width) {
            // The image never gets consulted for its preferred size
            return 0;
        }

        public Dimensions getPreferredSize() {
            // The image never gets consulted for its preferred size
            return null;
        }
    }

    protected class SpinUpImage extends SpinButtonImage {
        public void paint(Graphics2D graphics) {
            int width = getWidth();
            int height = getHeight();

            graphics.setPaint(buttonImageColor);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            GeneralPath arrow = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

            arrow.moveTo(0, (float)height + 0.5f);
            arrow.lineTo((float)width / 2.0f, 0);
            arrow.lineTo(width, (float)height + 0.5f);

            arrow.closePath();
            graphics.fill(arrow);
        }
    }

    protected class SpinDownImage extends SpinButtonImage {
        public void paint(Graphics2D graphics) {
            int width = getWidth();
            int height = getHeight();

            graphics.setPaint(buttonImageColor);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            GeneralPath arrow = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

            arrow.moveTo(0, 0);
            arrow.lineTo((float)width / 2.0f, (float)height + 0.5f);
            arrow.lineTo(width, 0);

            arrow.closePath();
            graphics.fill(arrow);
        }
    }

    private SpinnerContent spinnerContent = new SpinnerContent();
    private SpinButton upButton = new SpinButton(1, new SpinUpImage());
    private SpinButton downButton = new SpinButton(-1, new SpinDownImage());

    private Color color = Color.BLACK;
    private Color disabledColor = new Color(0x99, 0x99, 0x99);
    private Color borderColor = DEFAULT_BORDER_COLOR;
    private Color buttonImageColor = Color.BLACK;
    private Color buttonBackgroundColor = new Color(0xF0, 0xEC, 0xE7);
    private Color buttonDisabledBackgroundColor = new Color(0xF0, 0xEC, 0xE7);
    private Color buttonPressedBackgroundColor = new Color(0xDF, 0xD7, 0xCD);
    private Color buttonHighlightedBackgroundColor = new Color(0xFB, 0xFA, 0xF8);
    private Font font = new Font("Verdana", Font.PLAIN, 11);

    private static AutomaticSpinner automaticSpinner = new AutomaticSpinner();

    private static final Color DEFAULT_BORDER_COLOR = new Color(0x99, 0x99, 0x99);

    @Override
    public void install(Component component) {
        validateComponentType(component, Spinner.class);

        super.install(component);

        Spinner spinner = (Spinner)component;
        spinner.getSpinnerListeners().add(this);
        spinner.getSpinnerSelectionListeners().add(this);

        spinner.add(spinnerContent);
        spinner.add(upButton);
        spinner.add(downButton);
    }

    @Override
    public void uninstall() {
        Spinner spinner = (Spinner)getComponent();
        spinner.getSpinnerListeners().remove(this);
        spinner.getSpinnerSelectionListeners().remove(this);

        spinner.remove(spinnerContent);
        spinner.remove(upButton);
        spinner.remove(downButton);

        super.uninstall();
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
            spinnerContent.getPreferredHeight(width));

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        // TODO Optimize
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        int width = getWidth();
        int height = getHeight();

        int buttonHeight = (height - 2) / 2;
        int buttonWidth = Math.max(upButton.getPreferredWidth(buttonHeight),
            downButton.getPreferredWidth(buttonHeight));

        spinnerContent.setSize(width - buttonWidth - 3, height - 2);
        spinnerContent.setLocation(1, 1);

        upButton.setSize(buttonWidth, buttonHeight);
        upButton.setLocation(width - buttonWidth - 1, 1);

        downButton.setSize(buttonWidth, buttonHeight);
        downButton.setLocation(width - buttonWidth - 1, height - buttonHeight - 1);
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        int width = getWidth();
        int height = getHeight();

        int buttonWidth = upButton.getWidth();
        int buttonHeight = upButton.getHeight();

        graphics.setStroke(new BasicStroke(0));
        graphics.setPaint(borderColor);
        graphics.drawRect(0, 0, width - 1, height - 1);
        graphics.drawLine(width - buttonWidth - 2, 0,
            width - buttonWidth - 2, height - 1);
        graphics.drawLine(width - buttonWidth - 2, buttonHeight + 1,
            width - 1, buttonHeight + 1);
    }

    @Override
    public void mouseClick(Mouse.Button button, int x, int y, int count) {
        Component.setFocusedComponent(spinnerContent);
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

        setColor(Color.decode(color));
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

        setDisabledColor(Color.decode(disabledColor));
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

        setBorderColor(Color.decode(borderColor));
    }

    public Color getButtonImageColor() {
        return buttonImageColor;
    }

    public void setButtonImageColor(Color buttonImageColor) {
        this.buttonImageColor = buttonImageColor;
        repaintComponent();
    }

    public final void setButtonImageColor(String buttonImageColor) {
        if (buttonImageColor == null) {
            throw new IllegalArgumentException("buttonImageColor is null");
        }

        setButtonImageColor(Color.decode(buttonImageColor));
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

        setButtonBackgroundColor(Color.decode(buttonBackgroundColor));
    }

    public Color getButtonDisabledBackgroundColor() {
        return buttonDisabledBackgroundColor;
    }

    public void setButtonDisabledBackgroundColor(Color buttonDisabledBackgroundColor) {
        this.buttonDisabledBackgroundColor = buttonDisabledBackgroundColor;
        repaintComponent();
    }

    public final void setButtonDisabledBackgroundColor(String buttonDisabledBackgroundColor) {
        if (buttonDisabledBackgroundColor == null) {
            throw new IllegalArgumentException("buttonDisabledBackgroundColor is null");
        }

        setButtonDisabledBackgroundColor(Color.decode(buttonDisabledBackgroundColor));
    }

    public Color getButtonPressedBackgroundColor() {
        return buttonPressedBackgroundColor;
    }

    public void setButtonPressedBackgroundColor(Color buttonPressedBackgroundColor) {
        this.buttonPressedBackgroundColor = buttonPressedBackgroundColor;
        repaintComponent();
    }

    public final void setButtonPressedBackgroundColor(String buttonPressedBackgroundColor) {
        if (buttonPressedBackgroundColor == null) {
            throw new IllegalArgumentException("buttonPressedBackgroundColor is null");
        }

        setButtonPressedBackgroundColor(Color.decode(buttonPressedBackgroundColor));
    }

    public Color getButtonHighlightedBackgroundColor() {
        return buttonHighlightedBackgroundColor;
    }

    public void setButtonHighlightedBackgroundColor(Color buttonHighlightedBackgroundColor) {
        this.buttonHighlightedBackgroundColor = buttonHighlightedBackgroundColor;
        repaintComponent();
    }

    public final void setButtonHighlightedBackgroundColor(String buttonHighlightedBackgroundColor) {
        if (buttonHighlightedBackgroundColor == null) {
            throw new IllegalArgumentException("buttonHighlightedBackgroundColor is null");
        }

        setButtonHighlightedBackgroundColor(Color.decode(buttonHighlightedBackgroundColor));
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        this.font = font;

        invalidateComponent();
    }

    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Font.decode(font));
    }

    // Spinner.Skin methods

    public Rectangle getContentBounds() {
        return spinnerContent.getBounds();
    }

    // SpinnerListener methods

    public void spinnerDataChanged(Spinner spinner, List<?> previousSpinnerData) {
        invalidateComponent();
    }

    public void itemRendererChanged(Spinner spinner,
        Spinner.ItemRenderer previousItemRenderer) {
        invalidateComponent();
    }

    public void circularChanged(Spinner spinner) {
        // No-op
    }

    public void selectedValueKeyChanged(Spinner spinner,
        String previousSelectedValueKey) {
        // No-op
    }

    public void valueMappingChanged(Spinner spinner,
        Spinner.ValueMapping previousValueMapping) {
        invalidateComponent();
    }

    // SpinnerSelectionListener methods

    public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex) {
        invalidateComponent();
    }
}
