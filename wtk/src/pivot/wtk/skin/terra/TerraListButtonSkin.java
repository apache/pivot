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
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.util.Vote;
import pivot.wtk.Border;
import pivot.wtk.Button;
import pivot.wtk.Dimensions;
import pivot.wtk.Display;
import pivot.wtk.Insets;
import pivot.wtk.ListButton;
import pivot.wtk.ListView;
import pivot.wtk.Panorama;
import pivot.wtk.Bounds;
import pivot.wtk.Theme;
import pivot.wtk.Window;
import pivot.wtk.WindowStateListener;
import pivot.wtk.effects.FadeTransition;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;
import pivot.wtk.skin.ListButtonSkin;

/**
 * Terra list button skin.
 *
 * @author gbrown
 */
public class TerraListButtonSkin extends ListButtonSkin {
    private Panorama listViewPanorama;
    private Border listViewBorder;

    private WindowStateListener listViewPopupStateListener = new WindowStateListener() {
        private boolean focusButtonOnClose = true;

        public Vote previewWindowOpen(Window window, Display display) {
            return Vote.APPROVE;
        }

        public void windowOpenVetoed(Window window, Vote reason) {
            // No-op
        }

        public void windowOpened(Window window) {
            focusButtonOnClose = true;
        }

        public Vote previewWindowClose(final Window window) {
            Vote vote = Vote.APPROVE;

            if (closeTransition == null) {
                closeTransition = new FadeTransition(window,
                    CLOSE_TRANSITION_DURATION, CLOSE_TRANSITION_RATE);

                closeTransition.start(new TransitionListener() {
                    public void transitionCompleted(Transition transition) {
                        focusButtonOnClose = window.containsFocus();
                        window.close();
                    }
                });

                vote = Vote.DEFER;
            } else {
                vote = (closeTransition.isRunning()) ? Vote.DEFER : Vote.APPROVE;
            }

            return vote;
        }

        public void windowCloseVetoed(Window window, Vote reason) {
            if (reason == Vote.DENY
                && closeTransition != null) {
                closeTransition.stop();
                closeTransition = null;
            }
        }

        public void windowClosed(Window window, Display display) {
            closeTransition = null;
            if (focusButtonOnClose) {
                ListButton listButton = (ListButton)getComponent();
                if (listButton.isShowing()) {
                    listButton.requestFocus();
                }
            }
        }
    };

    private Font font;
    private Color color;
    private Color disabledColor;
    private Color backgroundColor;
    private Color disabledBackgroundColor;
    private Color borderColor;
    private Color disabledBorderColor;
    private Insets padding;
    private int listSize = -1;

    // Derived colors
    private Color bevelColor;
    private Color pressedBevelColor;
    private Color disabledBevelColor;

    private Transition closeTransition = null;

    private static final int TRIGGER_WIDTH = 14;

    private static final int CLOSE_TRANSITION_DURATION = 250;
    private static final int CLOSE_TRANSITION_RATE = 30;

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
    }

    public int getPreferredWidth(int height) {
        ListButton listButton = (ListButton)getComponent();
        List<?> listData = listButton.getListData();

        Button.DataRenderer dataRenderer = listButton.getDataRenderer();

        // Include padding in constraint
        if (height != -1) {
            height = Math.max(height - (padding.top + padding.bottom + 2), 0);
        }

        // Determine the preferred width of the current button data
        dataRenderer.render(listButton.getButtonData(),
            listButton, false);
        int preferredWidth = dataRenderer.getPreferredWidth(-1);

        // The preferred width of the button is the max. width of the rendered
        // content plus padding and the trigger width
        for (Object item : listData) {
            dataRenderer.render(item, listButton, false);
            preferredWidth = Math.max(preferredWidth, dataRenderer.getPreferredWidth(-1));
        }

        preferredWidth += TRIGGER_WIDTH + padding.left + padding.right + 2;

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        ListButton listButton = (ListButton)getComponent();
        Button.DataRenderer dataRenderer = listButton.getDataRenderer();

        dataRenderer.render(listButton.getButtonData(), listButton, false);

        int preferredHeight = dataRenderer.getPreferredHeight(-1)
            + padding.top + padding.bottom + 2;

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        // TODO Optimize by performing calcuations locally
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        ListButton listButton = (ListButton)getComponent();

        int width = getWidth();
        int height = getHeight();

        Color backgroundColor = null;
        Color bevelColor = null;
        Color borderColor = null;

        if (listButton.isEnabled()) {
            backgroundColor = this.backgroundColor;
            bevelColor = (pressed
        		|| (listViewPopup.isOpen() && closeTransition == null)) ? pressedBevelColor : this.bevelColor;
            borderColor = this.borderColor;
        } else {
            backgroundColor = disabledBackgroundColor;
            bevelColor = disabledBevelColor;
            borderColor = disabledBorderColor;
        }

        graphics.setStroke(new BasicStroke());

        // Paint the background
        graphics.setPaint(new GradientPaint(width / 2, 0, bevelColor,
            width / 2, height / 2, backgroundColor));
        graphics.fillRect(0, 0, width, height);

        // Paint the border
        graphics.setPaint(borderColor);

        Bounds contentBounds = new Bounds(0, 0,
            Math.max(width - TRIGGER_WIDTH - 1, 0), Math.max(height - 1, 0));
        graphics.draw(new Rectangle2D.Double(contentBounds.x + 0.5, contentBounds.y + 0.5,
            contentBounds.width, contentBounds.height));

        Bounds triggerBounds = new Bounds(Math.max(width - TRIGGER_WIDTH - 1, 0), 0,
            TRIGGER_WIDTH, Math.max(height - 1, 0));
        graphics.draw(new Rectangle2D.Double(triggerBounds.x + 0.5, triggerBounds.y + 0.5,
            triggerBounds.width, triggerBounds.height));

        // Paint the content
        Button.DataRenderer dataRenderer = listButton.getDataRenderer();
        dataRenderer.render(listButton.getButtonData(), listButton, false);
        dataRenderer.setSize(Math.max(contentBounds.width - (padding.left + padding.right + 2) + 1, 0),
            Math.max(contentBounds.height - (padding.top + padding.bottom + 2) + 1, 0));

        Graphics2D contentGraphics = (Graphics2D)graphics.create();
        contentGraphics.translate(padding.left + 1, padding.top + 1);
        contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
        dataRenderer.paint(contentGraphics);
        contentGraphics.dispose();

        // Paint the focus state
        if (listButton.isFocused()) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

            graphics.setStroke(dashStroke);
            graphics.setColor(borderColor);

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.draw(new Rectangle2D.Double(2.5, 2.5, Math.max(contentBounds.width - 4, 0),
                Math.max(contentBounds.height - 4, 0)));
        }

        // Paint the trigger
        GeneralPath triggerIconShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        triggerIconShape.moveTo(0, 0);
        triggerIconShape.lineTo(3, 3);
        triggerIconShape.lineTo(6, 0);
        triggerIconShape.closePath();

        Graphics2D triggerGraphics = (Graphics2D)graphics.create();
        triggerGraphics.setStroke(new BasicStroke(0));
        triggerGraphics.setPaint(color);

        int tx = triggerBounds.x + Math.round((triggerBounds.width
            - triggerIconShape.getBounds().width) / 2f);
        int ty = triggerBounds.y + Math.round((triggerBounds.height
            - triggerIconShape.getBounds().height) / 2f);
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

        setFont(Font.decode(font));
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

        setColor(decodeColor(color));
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

        setDisabledColor(decodeColor(disabledColor));
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

        setBackgroundColor(decodeColor(backgroundColor));
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

        setDisabledBackgroundColor(decodeColor(disabledBackgroundColor));
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

        setBorderColor(decodeColor(borderColor));
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

        setDisabledBorderColor(decodeColor(disabledBorderColor));
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

    public int getListSize() {
    	return listSize;
    }

    public void setListSize(int listSize) {
    	if (listSize < -1) {
    		throw new IllegalArgumentException("Invalid list size.");
    	}

    	this.listSize = listSize;
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
    public void buttonPressed(Button button) {
    	if (!listViewPopup.isOpen()) {
        	if (listSize == -1) {
        		listViewBorder.setPreferredHeight(-1);
        	} else {
        		if (!listViewBorder.isPreferredHeightSet()) {
            		ListView.ItemRenderer itemRenderer = listView.getItemRenderer();
            		int height = itemRenderer.getPreferredHeight(-1) * listSize + 2;

            		if (listViewBorder.getPreferredHeight() > height) {
            			listViewBorder.setPreferredHeight(height);
            		} else {
            			listViewBorder.setPreferredHeight(-1);
            		}
        		}
        	}
    	}

    	super.buttonPressed(button);
    }
}
