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
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.ColorChooserButton;
import org.apache.pivot.wtk.Point;
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
    private WindowStateListener colorChooserPopupStateListener = new WindowStateListener.Adapter() {
        @Override
        public void windowOpened(Window window) {
            ColorChooserButton colorChooserButton = (ColorChooserButton)getComponent();

            // Determine the popup's location and preferred size, relative
            // to the button
            Display display = colorChooserButton.getDisplay();

            if (display != null) {
                int width = getWidth();
                int height = getHeight();

                // Ensure that the popup remains within the bounds of the display
                Point buttonLocation = colorChooserButton.mapPointToAncestor(display, 0, 0);

                Dimensions displaySize = display.getSize();

                colorChooserPopup.setPreferredSize(-1, -1);
                Dimensions popupSize = colorChooserPopup.getPreferredSize();
                int popupWidth = Math.max(popupSize.width,
                    colorChooserButton.getWidth() - TRIGGER_WIDTH - 1);
                int popupHeight = popupSize.height;

                int x = buttonLocation.x;
                if (popupWidth > width
                    && x + popupWidth > displaySize.width) {
                    x = buttonLocation.x + width - popupWidth;
                }

                int y = buttonLocation.y + height - 1;
                if (y + popupSize.height > displaySize.height) {
                    y = buttonLocation.y - popupSize.height + 1;
                }

                colorChooser.setSelectedColor(colorChooserButton.getSelectedColor());

                colorChooserPopup.setLocation(x, y);
                colorChooserPopup.setPreferredSize(popupWidth, popupHeight);
                colorChooserPopup.open(colorChooserButton.getWindow());
                colorChooserPopup.requestFocus();
            }
        }

        @Override
        public Vote previewWindowClose(final Window window) {
            Vote vote = Vote.APPROVE;

            if (closeTransition == null) {
                closeTransition = new FadeWindowTransition(window, CLOSE_TRANSITION_DURATION,
                    CLOSE_TRANSITION_RATE, dropShadowDecorator);

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
        }

        @Override
        public void windowClosed(Window window, Display display, Window owner) {
            closeTransition = null;

            repaintComponent();
            getComponent().requestFocus();
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

    private Color bevelColor;
    private Color pressedBevelColor;
    private Color disabledBevelColor;

    private Transition closeTransition = null;
    private DropShadowDecorator dropShadowDecorator = null;

    private static final int CORNER_RADIUS = 4;
    private static final int TRIGGER_WIDTH = 10;

    private static final int CLOSE_TRANSITION_DURATION = 250;
    private static final int CLOSE_TRANSITION_RATE = 30;

    public TerraColorChooserButtonSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();

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
        colorChooserBorder.getStyles().put("color", borderColor);
        colorChooserBorder.getStyles().put("padding", 2);

        // Set the popup content
        colorChooserPopup.setContent(colorChooserBorder);

        // Attach the drop-shadow decorator
        dropShadowDecorator = new DropShadowDecorator(3, 3, 3);
        colorChooserPopup.getDecorators().add(dropShadowDecorator);
    }

    @Override
    public int getPreferredWidth(int height) {
        ColorChooserButton colorChooserButton = (ColorChooserButton)getComponent();
        Button.DataRenderer dataRenderer = colorChooserButton.getDataRenderer();

        dataRenderer.render(colorChooserButton.getButtonData(), colorChooserButton, false);

        int preferredWidth = dataRenderer.getPreferredWidth(-1) + TRIGGER_WIDTH
            + padding.left + padding.right + 2;

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        ColorChooserButton colorChooserButton = (ColorChooserButton)getComponent();
        Button.DataRenderer dataRenderer = colorChooserButton.getDataRenderer();

        dataRenderer.render(colorChooserButton.getButtonData(), colorChooserButton, false);

        int preferredHeight = dataRenderer.getPreferredHeight(-1)
            + padding.top + padding.bottom + 2;

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        ColorChooserButton colorChooserButton = (ColorChooserButton)getComponent();

        Button.DataRenderer dataRenderer = colorChooserButton.getDataRenderer();
        dataRenderer.render(colorChooserButton.getButtonData(), colorChooserButton, false);

        Dimensions contentSize = dataRenderer.getPreferredSize();
        int preferredWidth = contentSize.width + TRIGGER_WIDTH + padding.left + padding.right + 2;
        int preferredHeight = contentSize.height + padding.top + padding.bottom + 2;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        ColorChooserButton colorChooserButton = (ColorChooserButton)getComponent();

        Button.DataRenderer dataRenderer = colorChooserButton.getDataRenderer();
        dataRenderer.render(colorChooserButton.getButtonData(), colorChooserButton, false);

        int clientWidth = Math.max(width - (TRIGGER_WIDTH + padding.left + padding.right + 2), 0);
        int clientHeight = Math.max(height - (padding.top + padding.bottom + 2), 0);

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
        ColorChooserButton colorChooserButton = (ColorChooserButton)getComponent();

        int width = getWidth();
        int height = getHeight();

        Color backgroundColor = null;
        Color bevelColor = null;
        Color borderColor = null;

        if (colorChooserButton.isEnabled()) {
            backgroundColor = this.backgroundColor;
            bevelColor = (pressed || (colorChooserPopup.isOpen() && closeTransition == null)) ?
                pressedBevelColor : this.bevelColor;
            borderColor = this.borderColor;
        } else {
            backgroundColor = disabledBackgroundColor;
            bevelColor = disabledBevelColor;
            borderColor = disabledBorderColor;
        }

        // Paint the background
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setPaint(new GradientPaint(width / 2f, 0, bevelColor,
            width / 2f, height / 2f, backgroundColor));
        graphics.fill(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1,
            CORNER_RADIUS, CORNER_RADIUS));

        // Paint the content
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_OFF);

        Bounds contentBounds = new Bounds(0, 0,
            Math.max(width - TRIGGER_WIDTH - 1, 0), Math.max(height - 1, 0));
        Button.DataRenderer dataRenderer = colorChooserButton.getDataRenderer();
        dataRenderer.render(colorChooserButton.getButtonData(), colorChooserButton, false);
        dataRenderer.setSize(Math.max(contentBounds.width - (padding.left + padding.right + 2) + 1, 0),
            Math.max(contentBounds.height - (padding.top + padding.bottom + 2) + 1, 0));

        Graphics2D contentGraphics = (Graphics2D)graphics.create();
        contentGraphics.translate(padding.left + 1, padding.top + 1);
        contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
        dataRenderer.paint(contentGraphics);
        contentGraphics.dispose();

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        // Paint the border
        if (borderColor != null) {
            graphics.setPaint(borderColor);
            graphics.setStroke(new BasicStroke(1));
            graphics.draw(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1,
                CORNER_RADIUS, CORNER_RADIUS));
        }

        // Paint the focus state
        if (colorChooserButton.isFocused()) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);
            graphics.setStroke(dashStroke);
            graphics.setColor(this.borderColor);
            graphics.draw(new RoundRectangle2D.Double(2.5, 2.5, Math.max(width - 5, 0),
                Math.max(height - 5, 0), CORNER_RADIUS / 2, CORNER_RADIUS / 2));
        }

        // Paint the focus state
        if (colorChooserButton.isFocused()) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);
            graphics.setStroke(dashStroke);
            graphics.setColor(this.borderColor);
            graphics.draw(new RoundRectangle2D.Double(2.5, 2.5, Math.max(width - 5, 0),
                Math.max(height - 5, 0), CORNER_RADIUS / 2, CORNER_RADIUS / 2));
        }

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_OFF);

        // Paint the trigger
        GeneralPath triggerIconShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        triggerIconShape.moveTo(0, 0);
        triggerIconShape.lineTo(3, 3);
        triggerIconShape.lineTo(6, 0);
        triggerIconShape.closePath();

        Graphics2D triggerGraphics = (Graphics2D)graphics.create();
        triggerGraphics.setStroke(new BasicStroke(0));
        triggerGraphics.setPaint(color);

        Bounds triggerBounds = new Bounds(Math.max(width - (padding.right + TRIGGER_WIDTH), 0),
            0, TRIGGER_WIDTH, Math.max(height - (padding.top - padding.bottom), 0));
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

        setFont(decodeFont(font));
    }

    public final void setFont(Dictionary<String, ?> font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Theme.deriveFont(font));
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

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        this.backgroundColor = backgroundColor;
        bevelColor = TerraTheme.brighten(backgroundColor);
        pressedBevelColor = TerraTheme.darken(backgroundColor);
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
    }

    public Color getDisabledBackgroundColor() {
        return disabledBackgroundColor;
    }

    public void setDisabledBackgroundColor(Color disabledBackgroundColor) {
        if (disabledBackgroundColor == null) {
            throw new IllegalArgumentException("disabledBackgroundColor is null.");
        }

        this.disabledBackgroundColor = disabledBackgroundColor;
        disabledBevelColor = disabledBackgroundColor;
        repaintComponent();
    }

    public final void setDisabledBackgroundColor(String disabledBackgroundColor) {
        if (disabledBackgroundColor == null) {
            throw new IllegalArgumentException("disabledBackgroundColor is null.");
        }

        setDisabledBackgroundColor(GraphicsUtilities.decodeColor(disabledBackgroundColor));
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        this.borderColor = borderColor;
        colorChooserBorder.getStyles().put("color", borderColor);
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        setBorderColor(GraphicsUtilities.decodeColor(borderColor));
    }

    public Color getDisabledBorderColor() {
        return disabledBorderColor;
    }

    public void setDisabledBorderColor(Color disabledBorderColor) {
        if (disabledBorderColor == null) {
            throw new IllegalArgumentException("disabledBorderColor is null.");
        }

        this.disabledBorderColor = disabledBorderColor;
        repaintComponent();
    }

    public final void setDisabledBorderColor(String disabledBorderColor) {
        if (disabledBorderColor == null) {
            throw new IllegalArgumentException("disabledBorderColor is null.");
        }

        setDisabledBorderColor(GraphicsUtilities.decodeColor(disabledBorderColor));
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

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
}
