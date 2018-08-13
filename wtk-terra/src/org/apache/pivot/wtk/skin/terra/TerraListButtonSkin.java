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
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Utils;
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
import org.apache.pivot.wtk.Style;
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
    private WindowStateListener listViewPopupStateListener = new WindowStateListener() {
        @Override
        public void windowOpened(Window window) {
            // Adjust for list size
            ListButton listButton = (ListButton) getComponent();

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
            if (popupHeight > maximumHeight && buttonLocation.y > maximumHeight) {
                window.setMaximumHeight(buttonLocation.y);
                window.setY(buttonLocation.y - window.getPreferredHeight() + 1);
            } else {
                window.setMaximumHeight(maximumHeight);
            }

            repaintComponent();

            ApplicationContext.queueCallback(() -> {
                int selectedIndex = listView.getSelectedIndex();

                if (selectedIndex >= 0) {
                    Bounds itemBounds = listView.getItemBounds(selectedIndex);
                    listView.scrollAreaToVisible(itemBounds);
                }
            });
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

        listViewPopup.getWindowStateListeners().add(listViewPopupStateListener);

        // Create the panorama and border
        listViewPanorama = new Panorama(listView);
        listViewPanorama.getStyles().put(Style.buttonBackgroundColor,
            listView.getStyles().get(Style.backgroundColor));
        listViewPanorama.getStyles().put(Style.alwaysShowScrollButtons, true);

        listViewBorder = new Border(listViewPanorama);
        listViewBorder.getStyles().put(Style.padding, 0);
        listViewBorder.getStyles().put(Style.color, borderColor);

        // Set the popup content
        listViewPopup.setContent(listViewBorder);

        // Attach the drop-shadow decorator
        if (!themeIsFlat()) {
            dropShadowDecorator = new DropShadowDecorator();
            listViewPopup.getDecorators().add(dropShadowDecorator);
        }
    }

    @Override
    public int getPreferredWidth(int height) {
        ListButton listButton = (ListButton) getComponent();
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

        preferredWidth += TRIGGER_WIDTH + padding.getWidth() + 2;

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        ListButton listButton = (ListButton) getComponent();

        Button.DataRenderer dataRenderer = listButton.getDataRenderer();
        dataRenderer.render(listButton.getButtonData(), listButton, false);

        int preferredHeight = dataRenderer.getPreferredHeight(-1) + padding.getHeight() + 2;

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        ListButton listButton = (ListButton) getComponent();
        Button.DataRenderer dataRenderer = listButton.getDataRenderer();

        // Determine the preferred width and height of the current button data
        dataRenderer.render(listButton.getButtonData(), listButton, false);
        Dimensions contentSize = dataRenderer.getPreferredSize();
        int preferredWidth = contentSize.width;
        int preferredHeight = contentSize.height + padding.getHeight() + 2;

        // The preferred width of the button is the max. width of the rendered
        // content plus padding and the trigger width
        List<?> listData = listButton.getListData();
        for (Object item : listData) {
            dataRenderer.render(item, listButton, false);
            preferredWidth = Math.max(preferredWidth, dataRenderer.getPreferredWidth(-1));
        }

        preferredWidth += TRIGGER_WIDTH + padding.getWidth() + 2;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        ListButton listButton = (ListButton) getComponent();

        Button.DataRenderer dataRenderer = listButton.getDataRenderer();
        dataRenderer.render(listButton.getButtonData(), listButton, false);

        int clientWidth = Math.max(width - (TRIGGER_WIDTH + padding.getWidth() + 2), 0);
        int clientHeight = Math.max(height - (padding.getHeight() + 2), 0);

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
        ListButton listButton = (ListButton) getComponent();

        int width = getWidth();
        int height = getHeight();

        Color colorLocal = null;
        Color backgroundColorLocal = null;
        Color bevelColorLocal = null;
        Color borderColorLocal = null;

        if (listButton.isEnabled()) {
            colorLocal = this.color;
            backgroundColorLocal = this.backgroundColor;
            bevelColorLocal = (pressed || (listViewPopup.isOpen() && !listViewPopup.isClosing())) ? pressedBevelColor
                : this.bevelColor;
            borderColorLocal = this.borderColor;
        } else {
            colorLocal = disabledColor;
            backgroundColorLocal = disabledBackgroundColor;
            bevelColorLocal = disabledBevelColor;
            borderColorLocal = disabledBorderColor;
        }

        graphics.setStroke(new BasicStroke());

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

        Bounds contentBounds = new Bounds(0, 0, Math.max(width - TRIGGER_WIDTH - 1, 0),
            Math.max(height - 1, 0));
        Button.DataRenderer dataRenderer = listButton.getDataRenderer();
        dataRenderer.render(listButton.getButtonData(), listButton, false);
        dataRenderer.setSize(
            Math.max(contentBounds.width - (padding.getWidth() + 2) + 1, 0),
            Math.max(contentBounds.height - (padding.getHeight() + 2) + 1, 0));

        Graphics2D contentGraphics = (Graphics2D) graphics.create();
        contentGraphics.translate(padding.left + 1, padding.top + 1);
        contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
        dataRenderer.paint(contentGraphics);
        contentGraphics.dispose();

        GraphicsUtilities.setAntialiasingOn(graphics);

        // Paint the border
        if (!themeIsFlat()) {
            graphics.setPaint(borderColorLocal);
            graphics.setStroke(new BasicStroke(1));
            graphics.draw(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1,
                CORNER_RADIUS, CORNER_RADIUS));
            graphics.draw(new Line2D.Double(contentBounds.x + contentBounds.width, 0.5,
                contentBounds.x + contentBounds.width, contentBounds.height));
        }

        // Paint the focus state
        if (listButton.isFocused()) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

            graphics.setStroke(dashStroke);
            graphics.setColor(borderColorLocal);

            graphics.draw(new RoundRectangle2D.Double(2.5, 2.5,
                Math.max(contentBounds.width - 4, 0), Math.max(contentBounds.height - 4, 0),
                CORNER_RADIUS / 2, CORNER_RADIUS / 2));
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
        triggerGraphics.setPaint(colorLocal);

        Bounds triggerBounds = getTriggerBounds();
        int tx = triggerBounds.x
            + Math.round((triggerBounds.width - triggerIconShape.getBounds().width) / 2f) - 1;
        int ty = triggerBounds.y
            + Math.round((triggerBounds.height - triggerIconShape.getBounds().height) / 2f) - 1;
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

                GraphicsUtilities.setAntialiasingOn(graphics);

                graphics.fill(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1,
                    CORNER_RADIUS, CORNER_RADIUS));

                GraphicsUtilities.setAntialiasingOff(graphics);
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

    public final void setColor(int color) {
        Theme theme = currentTheme();
        setColor(theme.getColor(color));
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

    public final void setDisabledColor(int disabledColor) {
        Theme theme = currentTheme();
        setDisabledColor(theme.getColor(disabledColor));
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

    public final void setBackgroundColor(int backgroundColor) {
        Theme theme = currentTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
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
        setDisabledBackgroundColor(GraphicsUtilities.decodeColor(disabledBackgroundColor,
            "disabledBackgroundColor"));
    }

    public final void setDisabledBackgroundColor(int disabledBackgroundColor) {
        Theme theme = currentTheme();
        setDisabledBackgroundColor(theme.getColor(disabledBackgroundColor));
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        Utils.checkNull(borderColor, "borderColor");

        this.borderColor = borderColor;
        listViewBorder.getStyles().put(Style.color, borderColor);
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        setBorderColor(GraphicsUtilities.decodeColor(borderColor, "borderColor"));
    }

    public final void setBorderColor(int borderColor) {
        Theme theme = currentTheme();
        setBorderColor(theme.getColor(borderColor));
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
        setDisabledBorderColor(GraphicsUtilities.decodeColor(disabledBorderColor,
            "disabledBorderColor"));
    }

    public final void setDisabledBorderColor(int disabledBorderColor) {
        Theme theme = currentTheme();
        setDisabledBorderColor(theme.getColor(disabledBorderColor));
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

    public Object getListFont() {
        return listView.getStyles().get(Style.font);
    }

    public void setListFont(Object listFont) {
        listView.getStyles().put(Style.font, listFont);
    }

    public Object getListColor() {
        return listView.getStyles().get(Style.color);
    }

    public void setListColor(Object listColor) {
        listView.getStyles().put(Style.color, listColor);
    }

    public Object getListDisabledColor() {
        return listView.getStyles().get(Style.disabledColor);
    }

    public void setListDisabledColor(Object listDisabledColor) {
        listView.getStyles().put(Style.disabledColor, listDisabledColor);
    }

    public Object getListBackgroundColor() {
        return listView.getStyles().get(Style.backgroundColor);
    }

    public void setListBackgroundColor(Object listBackgroundColor) {
        listView.getStyles().put(Style.backgroundColor, listBackgroundColor);
        listViewPanorama.getStyles().put(Style.buttonBackgroundColor, listBackgroundColor);
    }

    public Object getListSelectionColor() {
        return listView.getStyles().get(Style.selectionColor);
    }

    public void setListSelectionColor(Object listSelectionColor) {
        listView.getStyles().put(Style.selectionColor, listSelectionColor);
    }

    public Object getListSelectionBackgroundColor() {
        return listView.getStyles().get(Style.selectionBackgroundColor);
    }

    public void setListSelectionBackgroundColor(Object listSelectionBackgroundColor) {
        listView.getStyles().put(Style.selectionBackgroundColor, listSelectionBackgroundColor);
    }

    public Object getListInactiveSelectionColor() {
        return listView.getStyles().get(Style.inactiveSelectionColor);
    }

    public void setListInactiveSelectionColor(Object listInactiveSelectionColor) {
        listView.getStyles().put(Style.inactiveSelectionColor, listInactiveSelectionColor);
    }

    public Object getListInactiveSelectionBackgroundColor() {
        return listView.getStyles().get(Style.inactiveSelectionBackgroundColor);
    }

    public void setListInactiveSelectionBackgroundColor(Object listInactiveSelectionBackgroundColor) {
        listView.getStyles().put(Style.inactiveSelectionBackgroundColor,
            listInactiveSelectionBackgroundColor);
    }

    public Object getListHighlightColor() {
        return listView.getStyles().get(Style.highlightColor);
    }

    public void setListHighlightColor(Object listHighlightColor) {
        listView.getStyles().put(Style.highlightColor, listHighlightColor);
    }

    public Object getListHighlightBackgroundColor() {
        return listView.getStyles().get(Style.highlightBackgroundColor);
    }

    public void setListHighlightBackgroundColor(Object listHighlightBackgroundColor) {
        listView.getStyles().put(Style.highlightBackgroundColor, listHighlightBackgroundColor);
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

        ListButton listButton = (ListButton) component;
        if (listButton.isRepeatable()) {
            repaintComponent();
        }

        return consumed;
    }
}
