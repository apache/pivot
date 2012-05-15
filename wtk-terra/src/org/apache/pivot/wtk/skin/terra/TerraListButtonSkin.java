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
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Panorama;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;
import org.apache.pivot.wtk.effects.DropShadowDecorator;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtk.skin.ListButtonSkin;

/**
 * Terra list button skin.
 */
public class TerraListButtonSkin extends ListButtonSkin {
    private WindowStateListener listViewPopupStateListener = new WindowStateListener.Adapter() {
        @Override
        public void windowOpened(Window window) {
            // Adjust for list size
            ListButton listButton = (ListButton)getComponent();

            int listSize = listButton.getListSize();
            if (listSize == -1) {
                listViewBorder.setPreferredHeight(-1);
            } else {
                if (!listViewBorder.isPreferredHeightSet()) {
                    ListView.ItemRenderer itemRenderer = listView.getItemRenderer();
                    int borderHeight = itemRenderer.getPreferredHeight(-1) * listSize + 2;

                    if (listViewBorder.getPreferredHeight() > borderHeight) {
                        listViewBorder.setPreferredHeight(borderHeight);
                    } else {
                        listViewBorder.setPreferredHeight(-1);
                    }
                }
            }

            // Size and position the popup
            Display display = listButton.getDisplay();
            Dimensions displaySize = display.getSize();

            Point buttonLocation = listButton.mapPointToAncestor(display, 0, 0);
            window.setLocation(buttonLocation.x, buttonLocation.y + getHeight() - 1);

            int width = getWidth();
            window.setMinimumWidth(width - TRIGGER_WIDTH - 1);

            int popupWidth = window.getPreferredWidth();
            if (buttonLocation.x + popupWidth > displaySize.width) {
                window.setX(buttonLocation.x + width - popupWidth);
            }

            window.setMaximumHeight(Integer.MAX_VALUE);
            int popupHeight = window.getPreferredHeight();
            int maximumHeight = displaySize.height - window.getY();
            if (popupHeight > maximumHeight
                && buttonLocation.y > maximumHeight) {
                window.setMaximumHeight(buttonLocation.y);
                window.setY(buttonLocation.y - window.getPreferredHeight() + 1);
            } else {
                window.setMaximumHeight(maximumHeight);
            }

            repaintComponent();

            ApplicationContext.queueCallback(new Runnable() {
                @Override
                public void run() {
                    int selectedIndex = listView.getSelectedIndex();

                    if (selectedIndex >= 0) {
                        Bounds itemBounds = listView.getItemBounds(selectedIndex);
                        listView.scrollAreaToVisible(itemBounds);
                    }
                }
            });
        }

        @Override
        public Vote previewWindowClose(final Window window) {
            Vote vote = Vote.APPROVE;

            if (closeTransition == null) {
                closeTransition = new FadeWindowTransition(window,
                    closeTransitionDuration, closeTransitionRate,
                    dropShadowDecorator);

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
            if (reason == Vote.DENY
                && closeTransition != null) {
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

    private Panorama listViewPanorama;
    private Border listViewBorder;

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
    private static final int TRIGGER_WIDTH = 14;

    private static final int DEFAULT_CLOSE_TRANSITION_DURATION = 250;
    private static final int DEFAULT_CLOSE_TRANSITION_RATE = 30;

    public TerraListButtonSkin() {
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

        listViewPopup.getWindowStateListeners().add(listViewPopupStateListener);

        // Create the panorama and border
        listViewPanorama = new Panorama(listView);
        listViewPanorama.getStyles().put("buttonBackgroundColor",
            listView.getStyles().get("backgroundColor"));
        listViewPanorama.getStyles().put("alwaysShowScrollButtons", true);

        listViewBorder = new Border(listViewPanorama);
        listViewBorder.getStyles().put("padding", 0);
        listViewBorder.getStyles().put("color", borderColor);

        // Set the popup content
        listViewPopup.setContent(listViewBorder);

        // Attach the drop-shadow decorator
        dropShadowDecorator = new DropShadowDecorator(3, 3, 3);
        listViewPopup.getDecorators().add(dropShadowDecorator);
    }

    @Override
    public int getPreferredWidth(int height) {
        ListButton listButton = (ListButton)getComponent();
        Button.DataRenderer dataRenderer = listButton.getDataRenderer();

        // Determine the preferred width of the current button data
        dataRenderer.render(listButton.getButtonData(), listButton, false);
        int preferredWidth = dataRenderer.getPreferredWidth(-1);

        // The preferred width of the button is the max. width of the rendered
        // content plus padding and the trigger width
        List<?> listData = listButton.getListData();
        for (Object item : listData) {
            dataRenderer.render(item, listButton, false);
            preferredWidth = Math.max(preferredWidth, dataRenderer.getPreferredWidth(-1));
        }

        preferredWidth += TRIGGER_WIDTH + padding.left + padding.right + 2;

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        ListButton listButton = (ListButton)getComponent();

        Button.DataRenderer dataRenderer = listButton.getDataRenderer();
        dataRenderer.render(listButton.getButtonData(), listButton, false);

        int preferredHeight = dataRenderer.getPreferredHeight(-1)
            + padding.top + padding.bottom + 2;

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        ListButton listButton = (ListButton)getComponent();
        Button.DataRenderer dataRenderer = listButton.getDataRenderer();

        // Determine the preferred width and height of the current button data
        dataRenderer.render(listButton.getButtonData(), listButton, false);
        Dimensions contentSize = dataRenderer.getPreferredSize();
        int preferredWidth = contentSize.width;
        int preferredHeight = contentSize.height + padding.top + padding.bottom + 2;

        // The preferred width of the button is the max. width of the rendered
        // content plus padding and the trigger width
        List<?> listData = listButton.getListData();
        for (Object item : listData) {
            dataRenderer.render(item, listButton, false);
            preferredWidth = Math.max(preferredWidth, dataRenderer.getPreferredWidth(-1));
        }

        preferredWidth += TRIGGER_WIDTH + padding.left + padding.right + 2;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        ListButton listButton = (ListButton)getComponent();

        Button.DataRenderer dataRenderer = listButton.getDataRenderer();
        dataRenderer.render(listButton.getButtonData(), listButton, false);

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
        ListButton listButton = (ListButton)getComponent();

        int width = getWidth();
        int height = getHeight();

        Color colorLocal = null;
        Color backgroundColorLocal = null;
        Color bevelColorLocal = null;
        Color borderColorLocal = null;

        if (listButton.isEnabled()) {
            colorLocal = this.color;
            backgroundColorLocal = this.backgroundColor;
            bevelColorLocal = (pressed || (listViewPopup.isOpen() && !listViewPopup.isClosing())) ?
                pressedBevelColor : this.bevelColor;
            borderColorLocal = this.borderColor;
        } else {
            colorLocal = disabledColor;
            backgroundColorLocal = disabledBackgroundColor;
            bevelColorLocal = disabledBevelColor;
            borderColorLocal = disabledBorderColor;
        }

        graphics.setStroke(new BasicStroke());

        // Paint the background
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setPaint(new GradientPaint(width / 2f, 0, bevelColorLocal,
            width / 2f, height / 2f, backgroundColorLocal));
        graphics.fill(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1,
            CORNER_RADIUS, CORNER_RADIUS));

        // Paint the content
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_OFF);

        Bounds contentBounds = new Bounds(0, 0,
            Math.max(width - TRIGGER_WIDTH - 1, 0), Math.max(height - 1, 0));
        Button.DataRenderer dataRenderer = listButton.getDataRenderer();
        dataRenderer.render(listButton.getButtonData(), listButton, false);
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
        graphics.setPaint(borderColorLocal);
        graphics.setStroke(new BasicStroke(1));
        graphics.draw(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1,
            CORNER_RADIUS, CORNER_RADIUS));
        graphics.draw(new Line2D.Double(contentBounds.x + contentBounds.width, 0.5,
            contentBounds.x + contentBounds.width, contentBounds.height));

        // Paint the focus state
        if (listButton.isFocused()) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

            graphics.setStroke(dashStroke);
            graphics.setColor(borderColorLocal);

            graphics.draw(new RoundRectangle2D.Double(2.5, 2.5, Math.max(contentBounds.width - 4, 0),
                Math.max(contentBounds.height - 4, 0), CORNER_RADIUS / 2, CORNER_RADIUS / 2));
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
        triggerGraphics.setPaint(colorLocal);

        Bounds triggerBounds = getTriggerBounds();
        int tx = triggerBounds.x + Math.round((triggerBounds.width
            - triggerIconShape.getBounds().width) / 2f) - 1;
        int ty = triggerBounds.y + Math.round((triggerBounds.height
            - triggerIconShape.getBounds().height) / 2f) - 1;
        triggerGraphics.translate(tx, ty);

        triggerGraphics.draw(triggerIconShape);
        triggerGraphics.fill(triggerIconShape);

        triggerGraphics.dispose();

        // Paint the trigger highlight
        if (listButton.isRepeatable()) {
            Point mouseLocation = listButton.getMouseLocation();

            if (mouseLocation != null) {
                graphics.setPaint(new Color(0, 0, 0, 0.25f));

                if (triggerBounds.contains(mouseLocation)) {
                    graphics.clipRect(triggerBounds.x, triggerBounds.y, triggerBounds.width, height);
                } else {
                    graphics.clipRect(0, 0, width - triggerBounds.width, height);
                }

                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                graphics.fill(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1,
                    CORNER_RADIUS, CORNER_RADIUS));

                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);
            }
        }
    }

    @Override
    public Bounds getTriggerBounds() {
        int width = getWidth();
        int height = getHeight();
        return new Bounds(Math.max(width - (TRIGGER_WIDTH + 1), 0), 0,
            TRIGGER_WIDTH + 1, Math.max(height, 0));
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

    public final void setColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setColor(theme.getColor(color));
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

    public final void setDisabledColor(int disabledColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setDisabledColor(theme.getColor(disabledColor));
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

    public final void setBackgroundColor(int backgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
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

    public final void setDisabledBackgroundColor(int disabledBackgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setDisabledBackgroundColor(theme.getColor(disabledBackgroundColor));
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        this.borderColor = borderColor;
        listViewBorder.getStyles().put("color", borderColor);
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        setBorderColor(GraphicsUtilities.decodeColor(borderColor));
    }

    public final void setBorderColor(int borderColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBorderColor(theme.getColor(borderColor));
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

    public final void setDisabledBorderColor(int disabledBorderColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setDisabledBorderColor(theme.getColor(disabledBorderColor));
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

    public Object getListFont() {
        return listView.getStyles().get("font");
    }

    public void setListFont(Object listFont) {
        listView.getStyles().put("font", listFont);
    }

    public Object getListColor() {
        return listView.getStyles().get("color");
    }

    public void setListColor(Object listColor) {
        listView.getStyles().put("color", listColor);
    }

    public Object getListDisabledColor() {
        return listView.getStyles().get("disabledColor");
    }

    public void setListDisabledColor(Object listDisabledColor) {
        listView.getStyles().put("disabledColor", listDisabledColor);
    }

    public Object getListBackgroundColor() {
        return listView.getStyles().get("backgroundColor");
    }

    public void setListBackgroundColor(Object listBackgroundColor) {
        listView.getStyles().put("backgroundColor", listBackgroundColor);
        listViewPanorama.getStyles().put("buttonBackgroundColor", listBackgroundColor);
    }

    public Object getListSelectionColor() {
        return listView.getStyles().get("selectionColor");
    }

    public void setListSelectionColor(Object listSelectionColor) {
        listView.getStyles().put("selectionColor", listSelectionColor);
    }

    public Object getListSelectionBackgroundColor() {
        return listView.getStyles().get("selectionBackgroundColor");
    }

    public void setListSelectionBackgroundColor(Object listSelectionBackgroundColor) {
        listView.getStyles().put("selectionBackgroundColor", listSelectionBackgroundColor);
    }

    public Object getListInactiveSelectionColor() {
        return listView.getStyles().get("inactiveSelectionColor");
    }

    public void setListInactiveSelectionColor(Object listInactiveSelectionColor) {
        listView.getStyles().put("inactiveSelectionColor", listInactiveSelectionColor);
    }

    public Object getListInactiveSelectionBackgroundColor() {
        return listView.getStyles().get("inactiveSelectionBackgroundColor");
    }

    public void setListInactiveSelectionBackgroundColor(Object listInactiveSelectionBackgroundColor) {
        listView.getStyles().put("inactiveSelectionBackgroundColor", listInactiveSelectionBackgroundColor);
    }

    public Object getListHighlightColor() {
        return listView.getStyles().get("highlightColor");
    }

    public void setListHighlightColor(Object listHighlightColor) {
        listView.getStyles().put("highlightColor", listHighlightColor);
    }

    public Object getListHighlightBackgroundColor() {
        return listView.getStyles().get("highlightBackgroundColor");
    }

    public void setListHighlightBackgroundColor(Object listHighlightBackgroundColor) {
        listView.getStyles().put("highlightBackgroundColor", listHighlightBackgroundColor);
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

        ListButton listButton = (ListButton)component;
        if (listButton.isRepeatable()) {
            repaintComponent();
        }

        return consumed;
    }
}
