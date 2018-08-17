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
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Utils;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ColorChooserButton;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;
import org.apache.pivot.wtk.effects.DropShadowDecorator;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtk.skin.ColorChooserButtonSkin;

/**
 * Terra color chooser button skin.
 */
public class TerraColorChooserButtonSkin extends ColorChooserButtonSkin {
    private WindowStateListener colorChooserPopupStateListener = new WindowStateListener() {
        @Override
        public void windowOpened(Window window) {
            ColorChooserButton colorChooserButton = (ColorChooserButton) getComponent();
            colorChooser.setSelectedColor(colorChooserButton.getSelectedColor());

            // Size and position the popup
            Display display = colorChooserButton.getDisplay();
            Dimensions displaySize = display.getSize();

            Point buttonLocation = colorChooserButton.mapPointToAncestor(display, 0, 0);
            window.setLocation(buttonLocation.x, buttonLocation.y + getHeight() - 1);

            int width = getWidth();
            window.setMinimumWidth(width - TRIGGER_WIDTH - 1);

            int popupWidth = window.getPreferredWidth();
            if (buttonLocation.x + popupWidth > displaySize.width) {
                window.setX(buttonLocation.x + width - popupWidth);
            }

            int popupHeight = window.getPreferredHeight();
            int maximumHeight = displaySize.height - window.getY();
            if (popupHeight > maximumHeight && buttonLocation.y > maximumHeight) {
                window.setY(buttonLocation.y - window.getPreferredHeight() + 1);
            }

            repaintComponent();
        }

        @Override
        public Vote previewWindowClose(final Window window) {
            Vote vote = Vote.APPROVE;

            if (closeTransition == null) {
                closeTransition = new FadeWindowTransition(window, closeTransitionDuration,
                    closeTransitionRate, dropShadowDecorator);

                closeTransition.start(new TransitionListener() {
                    @Override
                    public void transitionCompleted(Transition transition) {
                        window.close();
                    }
                });

                vote = Vote.DEFER;
            } else {
                vote = (closeTransition.isRunning()) ? Vote.DEFER : Vote.APPROVE;
            }

            return vote;
        }

        @Override
        public void windowCloseVetoed(Window window, Vote reason) {
            if (reason == Vote.DENY && closeTransition != null) {
                closeTransition.stop();
                closeTransition = null;
            }

            repaintComponent();
        }

        @Override
        public void windowClosed(Window window, Display display, Window owner) {
            closeTransition = null;
            repaintComponent();
        }
    };

    private Border colorChooserBorder;

    private Font font;
    private Color color;
    private Color disabledColor;
    private Color backgroundColor;
    private Color disabledBackgroundColor;
    private Color borderColor;
    private Color disabledBorderColor;
    private Insets padding;

    private int closeTransitionDuration = DEFAULT_CLOSE_TRANSITION_DURATION;
    private int closeTransitionRate = DEFAULT_CLOSE_TRANSITION_RATE;

    private Color bevelColor;
    private Color pressedBevelColor;
    private Color disabledBevelColor;

    private Transition closeTransition = null;
    private DropShadowDecorator dropShadowDecorator = null;

    private static final int CORNER_RADIUS = 4;
    private static final int TRIGGER_WIDTH = 10;

    private static final int DEFAULT_CLOSE_TRANSITION_DURATION = 250;
    private static final int DEFAULT_CLOSE_TRANSITION_RATE = 30;

    public TerraColorChooserButtonSkin() {
        Theme theme = currentTheme();

        font = theme.getFont();
        color = theme.getColor(1);
        disabledColor = theme.getColor(7);
        backgroundColor = theme.getColor(10);
        disabledBackgroundColor = theme.getColor(10);
        borderColor = theme.getColor(7);
        disabledBorderColor = theme.getColor(7);
        padding = new Insets(2, 3, 2, 3);

        // Set the derived colors
        bevelColor = TerraTheme.brighten(backgroundColor);
        pressedBevelColor = TerraTheme.darken(backgroundColor);
        disabledBevelColor = disabledBackgroundColor;

        colorChooserPopup.getWindowStateListeners().add(colorChooserPopupStateListener);

        // Create the border
        colorChooserBorder = new Border(colorChooser);
        colorChooserBorder.getStyles().put(Style.color, borderColor);
        colorChooserBorder.getStyles().put(Style.padding, 2);

        // Set the popup content
        colorChooserPopup.setContent(colorChooserBorder);

        // Attach the drop-shadow decorator
        dropShadowDecorator = new DropShadowDecorator();
        colorChooserPopup.getDecorators().add(dropShadowDecorator);
    }

    private int paddingWidth() {
        return TRIGGER_WIDTH + padding.getWidth() + 2;
    }

    private int paddingHeight() {
        return padding.getHeight() + 2;
    }

    @Override
    public int getPreferredWidth(int height) {
        ColorChooserButton colorChooserButton = (ColorChooserButton) getComponent();
        Button.DataRenderer dataRenderer = colorChooserButton.getDataRenderer();

        dataRenderer.render(colorChooserButton.getButtonData(), colorChooserButton, false);

        int preferredWidth = dataRenderer.getPreferredWidth(-1) + paddingWidth();

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        ColorChooserButton colorChooserButton = (ColorChooserButton) getComponent();
        Button.DataRenderer dataRenderer = colorChooserButton.getDataRenderer();

        dataRenderer.render(colorChooserButton.getButtonData(), colorChooserButton, false);

        int preferredHeight = dataRenderer.getPreferredHeight(-1) + paddingHeight();

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        ColorChooserButton colorChooserButton = (ColorChooserButton) getComponent();

        Button.DataRenderer dataRenderer = colorChooserButton.getDataRenderer();
        dataRenderer.render(colorChooserButton.getButtonData(), colorChooserButton, false);

        Dimensions contentSize = dataRenderer.getPreferredSize();
        int preferredWidth = contentSize.width + paddingWidth();
        int preferredHeight = contentSize.height + paddingHeight();

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        ColorChooserButton colorChooserButton = (ColorChooserButton) getComponent();

        Button.DataRenderer dataRenderer = colorChooserButton.getDataRenderer();
        dataRenderer.render(colorChooserButton.getButtonData(), colorChooserButton, false);

        int clientWidth = Math.max(width - paddingWidth(), 0);
        int clientHeight = Math.max(height - paddingHeight(), 0);

        int baseline = dataRenderer.getBaseline(clientWidth, clientHeight);

        if (baseline != -1) {
            baseline += padding.top + 1;
        }

        return baseline;
    }

    @Override
    public void layout() {
        // No-op
    }

    @Override
    public void paint(Graphics2D graphics) {
        ColorChooserButton colorChooserButton = (ColorChooserButton) getComponent();

        int width = getWidth();
        int height = getHeight();

        Color backgroundColorLocal = null;
        Color bevelColorLocal = null;
        Color borderColorLocal = null;

        if (colorChooserButton.isEnabled()) {
            backgroundColorLocal = this.backgroundColor;
            bevelColorLocal = (pressed || (colorChooserPopup.isOpen() && !colorChooserPopup.isClosing()))
                ? pressedBevelColor : this.bevelColor;
            borderColorLocal = this.borderColor;
        } else {
            backgroundColorLocal = disabledBackgroundColor;
            bevelColorLocal = disabledBevelColor;
            borderColorLocal = disabledBorderColor;
        }

        // Paint the background
        GraphicsUtilities.setAntialiasingOn(graphics);

        if (!themeIsFlat()) {
            graphics.setPaint(new GradientPaint(width / 2f, 0, bevelColorLocal, width / 2f,
                height / 2f, backgroundColorLocal));
        } else {
            graphics.setPaint(backgroundColorLocal);
        }
        graphics.fill(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1, CORNER_RADIUS,
            CORNER_RADIUS));

        // Paint the content
        GraphicsUtilities.setAntialiasingOff(graphics);

        Bounds contentBounds = new Bounds(0, 0,
            Math.max(width - TRIGGER_WIDTH - 1, 0),
            Math.max(height - 1, 0));
        Button.DataRenderer dataRenderer = colorChooserButton.getDataRenderer();
        dataRenderer.render(colorChooserButton.getButtonData(), colorChooserButton, false);
        dataRenderer.setSize(
            Math.max(contentBounds.width - (padding.getWidth() + 2) + 1, 0),
            Math.max(contentBounds.height - (padding.getHeight() + 2) + 1, 0));

        Graphics2D contentGraphics = (Graphics2D) graphics.create();
        contentGraphics.translate(padding.left + 1, padding.top + 1);
        contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
        dataRenderer.paint(contentGraphics);
        contentGraphics.dispose();

        GraphicsUtilities.setAntialiasingOn(graphics);

        if (!themeIsFlat()) {
            // Paint the border
            if (borderColorLocal != null) {
                graphics.setPaint(borderColorLocal);
                graphics.setStroke(new BasicStroke(1));
                graphics.draw(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1,
                    CORNER_RADIUS, CORNER_RADIUS));
            }
        }

        // Paint the focus state
        if (colorChooserButton.isFocused()) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);
            graphics.setStroke(dashStroke);
            graphics.setColor(this.borderColor);
            graphics.draw(new RoundRectangle2D.Double(2.5, 2.5, Math.max(width - 5, 0), Math.max(
                height - 5, 0), CORNER_RADIUS / 2, CORNER_RADIUS / 2));
        }

        GraphicsUtilities.setAntialiasingOff(graphics);

        // Paint the trigger
        GeneralPath triggerIconShape = new GeneralPath(Path2D.WIND_EVEN_ODD);
        triggerIconShape.moveTo(0, 0);
        triggerIconShape.lineTo(3, 3);
        triggerIconShape.lineTo(6, 0);
        triggerIconShape.closePath();

        Graphics2D triggerGraphics = (Graphics2D) graphics.create();
        triggerGraphics.setStroke(new BasicStroke(0));
        triggerGraphics.setPaint(color);

        Bounds triggerBounds = new Bounds(Math.max(width - (padding.right + TRIGGER_WIDTH), 0), 0,
            TRIGGER_WIDTH, Math.max(height - padding.getHeight(), 0));
        int tx = triggerBounds.x + (triggerBounds.width - triggerIconShape.getBounds().width) / 2;
        int ty = triggerBounds.y + (triggerBounds.height - triggerIconShape.getBounds().height) / 2;
        triggerGraphics.translate(tx, ty);

        triggerGraphics.draw(triggerIconShape);
        triggerGraphics.fill(triggerIconShape);

        triggerGraphics.dispose();
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        Utils.checkNull(font, "font");

        this.font = font;
        invalidateComponent();
    }

    public final void setFont(String font) {
        setFont(decodeFont(font));
    }

    public final void setFont(Dictionary<String, ?> font) {
        setFont(Theme.deriveFont(font));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        Utils.checkNull(color, "color");

        this.color = color;
        repaintComponent();
    }

    public final void setColor(String color) {
        setColor(GraphicsUtilities.decodeColor(color, "color"));
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(Color disabledColor) {
        Utils.checkNull(disabledColor, "disabledColor");

        this.disabledColor = disabledColor;
        repaintComponent();
    }

    public final void setDisabledColor(String disabledColor) {
        setDisabledColor(GraphicsUtilities.decodeColor(disabledColor, "disabledColor"));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        Utils.checkNull(backgroundColor, "backgroundColor");

        this.backgroundColor = backgroundColor;
        bevelColor = TerraTheme.brighten(backgroundColor);
        pressedBevelColor = TerraTheme.darken(backgroundColor);
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor, "backgroundColor"));
    }

    public Color getDisabledBackgroundColor() {
        return disabledBackgroundColor;
    }

    public void setDisabledBackgroundColor(Color disabledBackgroundColor) {
        Utils.checkNull(disabledBackgroundColor, "disabledBackgroundColor");

        this.disabledBackgroundColor = disabledBackgroundColor;
        disabledBevelColor = disabledBackgroundColor;
        repaintComponent();
    }

    public final void setDisabledBackgroundColor(String disabledBackgroundColor) {
        setDisabledBackgroundColor(GraphicsUtilities.decodeColor(disabledBackgroundColor, "disabledBackgroundColor"));
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        Utils.checkNull(borderColor, "borderColor");

        this.borderColor = borderColor;
        colorChooserBorder.getStyles().put(Style.color, borderColor);
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        setBorderColor(GraphicsUtilities.decodeColor(borderColor, "borderColor"));
    }

    public Color getDisabledBorderColor() {
        return disabledBorderColor;
    }

    public void setDisabledBorderColor(Color disabledBorderColor) {
        Utils.checkNull(disabledBorderColor, "disabledBorderColor");

        this.disabledBorderColor = disabledBorderColor;
        repaintComponent();
    }

    public final void setDisabledBorderColor(String disabledBorderColor) {
        setDisabledBorderColor(GraphicsUtilities.decodeColor(disabledBorderColor, "disabledBorderColor"));
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
        Utils.checkNull(padding, "padding");

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(Dictionary<String, ?> padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(Sequence<?> padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(Number padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(String padding) {
        setPadding(Insets.decode(padding));
    }

    public int getCloseTransitionDuration() {
        return closeTransitionDuration;
    }

    public void setCloseTransitionDuration(int closeTransitionDuration) {
        this.closeTransitionDuration = closeTransitionDuration;
    }

    public int getCloseTransitionRate() {
        return closeTransitionRate;
    }

    public void setCloseTransitionRate(int closeTransitionRate) {
        this.closeTransitionRate = closeTransitionRate;
    }
}
