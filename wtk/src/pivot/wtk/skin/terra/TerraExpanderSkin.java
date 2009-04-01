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

import pivot.collections.Dictionary;
import pivot.util.Vote;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Expander;
import pivot.wtk.ExpanderListener;
import pivot.wtk.Dimensions;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Insets;
import pivot.wtk.Label;
import pivot.wtk.LinkButton;
import pivot.wtk.Mouse;
import pivot.wtk.Orientation;
import pivot.wtk.FlowPane;
import pivot.wtk.Theme;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.content.ButtonDataRenderer;
import pivot.wtk.effects.ClipDecorator;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;
import pivot.wtk.effects.easing.Easing;
import pivot.wtk.effects.easing.Quadratic;
import pivot.wtk.media.Image;
import pivot.wtk.skin.ContainerSkin;

/**
 * Expander skin.
 *
 * @author gbrown
 */
public class TerraExpanderSkin extends ContainerSkin
    implements ButtonPressListener, ExpanderListener {
    public class ExpandTransition extends Transition {
        private boolean collapse;
        private Easing easing = new Quadratic();
        private ClipDecorator clipDecorator = new ClipDecorator();

        public ExpandTransition(boolean collapse, int duration, int rate) {
            super(duration, rate, false);
            this.collapse = collapse;
        }

        public float getScale() {
            int elapsedTime = getElapsedTime();
            int duration = getDuration();

            float scale;
            if (collapse) {
                scale = easing.easeIn(elapsedTime, 1, -1, duration);
            } else {
                scale = easing.easeOut(elapsedTime, 0, 1, duration);
            }

            return scale;
        }

        @Override
        public void start(TransitionListener transitionListener) {
        	Expander expander = (Expander)getComponent();
        	Component content = expander.getContent();
        	content.getDecorators().add(clipDecorator);

        	getComponent().setEnabled(false);

            super.start(transitionListener);
        }

        @Override
        public void stop() {
        	Expander expander = (Expander)getComponent();
        	Component content = expander.getContent();
        	content.getDecorators().remove(clipDecorator);

        	getComponent().setEnabled(true);

        	super.stop();
        }

        @Override
        protected void update() {
            invalidateComponent();
        }
    }

    /**
     * Expander shade button component.
     *
     * @author tvolkert
     */
    public class ShadeButton extends LinkButton {
        public ShadeButton() {
            this(null);
        }

        public ShadeButton(Object buttonData) {
            super(buttonData);

            setSkin(new ShadeButtonSkin());
            setDataRenderer(new ButtonDataRenderer());
        }
    }

    /**
     * Expander shade button component skin.
     *
     * @author tvolkert
     */
    public class ShadeButtonSkin extends TerraLinkButtonSkin {
        @Override
        public boolean isFocusable() {
            return false;
        }
    }

    protected abstract class ButtonImage extends Image {
        public int getWidth() {
            return 11;
        }

        public int getHeight() {
            return 11;
        }
    }

    protected class CollapseImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            graphics.setStroke(new BasicStroke(0));
            graphics.setPaint(shadeButtonColor);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            int[] xPoints = {3, 6, 9};
            int[] yPoints = {9, 3, 9};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    protected class ExpandImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            graphics.setStroke(new BasicStroke(0));
            graphics.setPaint(shadeButtonColor);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            int[] xPoints = {3, 6, 9};
            int[] yPoints = {3, 9, 3};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    private class TitleBarMouseHandler
        implements ComponentMouseButtonListener {
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            boolean consumed = false;

            if (count == 2) {
                Expander expander = (Expander)getComponent();
                expander.setExpanded(!expander.isExpanded());
                consumed = true;
            }

            return consumed;
        }
    }

    private Image collapseImage = new CollapseImage();
    private Image expandImage = new ExpandImage();

    private FlowPane titleBarFlowPane = null;
    private FlowPane titleFlowPane = null;
    private FlowPane buttonFlowPane = null;

    private Label titleLabel = new Label();
    private ShadeButton shadeButton = null;

    private TitleBarMouseHandler titleBarMouseHandler = new TitleBarMouseHandler();

    private Color titleBarBackgroundColor;
    private Color titleBarBorderColor;
    private Color shadeButtonColor;
    private Color shadeButtonBackgroundColor;
    private Color borderColor;
    private Insets padding;

    // Derived colors
    private Color titleBarBevelColor;

    private ExpandTransition expandTransition = null;

    private static final int EXPAND_DURATION = 250;
    private static final int EXPAND_RATE = 30;

    public TerraExpanderSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(4));

        titleBarBackgroundColor = theme.getColor(10);
        titleBarBorderColor = theme.getColor(7);
        shadeButtonColor = theme.getColor(7);
        shadeButtonBackgroundColor = theme.getColor(4);
        borderColor = theme.getColor(7);
        padding = new Insets(4);

        // Set the derived colors
        titleBarBevelColor = TerraTheme.brighten(titleBarBackgroundColor);

        titleBarFlowPane = new FlowPane(Orientation.HORIZONTAL);
        titleBarFlowPane.getComponentMouseButtonListeners().add(titleBarMouseHandler);

        titleBarFlowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.JUSTIFY);
        titleBarFlowPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        titleBarFlowPane.getStyles().put("padding", new Insets(3));
        titleBarFlowPane.getStyles().put("spacing", 3);

        titleFlowPane = new FlowPane(Orientation.HORIZONTAL);
        titleFlowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);

        buttonFlowPane = new FlowPane(Orientation.HORIZONTAL);
        buttonFlowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);

        titleBarFlowPane.add(titleFlowPane);
        titleBarFlowPane.add(buttonFlowPane);

        titleLabel.getStyles().put("color", theme.getColor(15));
        titleLabel.getStyles().put("fontBold", true);
        titleFlowPane.add(titleLabel);
    }

	@Override
	public void setSize(int width, int height) {
		if (expandTransition != null
			&& width != getWidth()) {
			expandTransition.end();
			expandTransition = null;
		}

		super.setSize(width, height);
	}

    @Override
    public void install(Component component) {
        super.install(component);

        Expander expander = (Expander)component;
        expander.getExpanderListeners().add(this);
        expander.add(titleBarFlowPane);

        Image buttonData = expander.isExpanded() ? collapseImage : expandImage;
        shadeButton = new ShadeButton(buttonData);
        buttonFlowPane.add(shadeButton);

        shadeButton.getButtonPressListeners().add(this);

        titleChanged(expander, null);
    }

    public void uninstall() {
        Expander expander = (Expander)getComponent();
        expander.getExpanderListeners().remove(this);
        expander.remove(titleBarFlowPane);

        shadeButton.getButtonPressListeners().remove(this);
        buttonFlowPane.remove(shadeButton);
        shadeButton = null;

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth;

        if (expandTransition == null) {
        	preferredWidth = 2;

            Expander expander = (Expander)getComponent();
            Component content = expander.getContent();

            int titleBarPreferredWidth = titleBarFlowPane.getPreferredWidth(-1);
            int contentPreferredWidth = 0;

            if (content != null) {
                int contentHeightConstraint = -1;

                if (height >= 0) {
                    int reservedHeight = 2 + padding.top + padding.bottom
                        + titleBarFlowPane.getPreferredHeight(-1);

                    if (expander.isExpanded()) {
                        // Title bar border is only drawn when expander is expanded
                        reservedHeight += 1;
                    }

                    contentHeightConstraint = Math.max(height - reservedHeight, 0);
                }

                contentPreferredWidth = padding.left + padding.right
                    + content.getPreferredWidth(contentHeightConstraint);
            }

            preferredWidth += Math.max(titleBarPreferredWidth, contentPreferredWidth);
        } else {
        	preferredWidth = getWidth();
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 2 + titleBarFlowPane.getPreferredHeight(-1);

        Expander expander = (Expander)getComponent();
        if (expander.isExpanded()) {
            // Title bar border is only drawn when expander is expanded
            preferredHeight += 1;

            Component content = expander.getContent();
            if (content != null) {
                int contentWidthConstraint = -1;

                if (width >= 0) {
                    int reservedWidth = 2 + padding.left + padding.right;
                    contentWidthConstraint = Math.max(width - reservedWidth, 0);
                }

                if (expandTransition == null) {
                    preferredHeight += (padding.top + padding.bottom
                        + content.getPreferredHeight(contentWidthConstraint));
                } else {
                    float scale = expandTransition.getScale();
                    preferredHeight += (int)(scale * (float)(padding.top + padding.bottom
                		+ content.getHeight()));
                }
            }
        }

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        // TODO Optimize
        return new Dimensions(this.getPreferredWidth(-1),
            this.getPreferredHeight(-1));
    }

    public void layout() {
        Expander expander = (Expander)getComponent();
        Component content = expander.getContent();

        int width = getWidth();
        int height = getHeight();

        int titleBarHeight;
        if (expandTransition == null) {
            titleBarHeight = titleBarFlowPane.getPreferredHeight(-1);
            titleBarFlowPane.setSize(Math.max(width - 2, 0), titleBarHeight);
            titleBarFlowPane.setLocation(1, 1);
        } else {
        	titleBarHeight = titleBarFlowPane.getHeight();
        }

        if (content != null) {
            if (expander.isExpanded()) {
                content.setVisible(true);

                int reservedWidth = 2 + padding.left + padding.right;
                int contentWidth = Math.max(width - reservedWidth, 0);

                int reservedHeight = 3 + padding.top + padding.bottom + titleBarHeight;
                int contentHeight = Math.max(height - reservedHeight, 0);

                if (expandTransition == null) {
                	content.setSize(contentWidth, contentHeight);
                } else {
                	if (!expandTransition.isRunning()) {
                    	content.setSize(contentWidth, content.getPreferredHeight(contentWidth));
                	}

                	expandTransition.clipDecorator.setWidth(contentWidth);
                	expandTransition.clipDecorator.setHeight(contentHeight);
                }

                int contentX = 1 + padding.left;
                int contentY = 2 + padding.top + titleBarHeight;

                content.setLocation(contentX, contentY);
            } else {
                content.setVisible(false);
            }
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        int width = getWidth();
        int height = getHeight();

        graphics.setStroke(new BasicStroke());

        Expander expander = (Expander)getComponent();
        if (expander.isExpanded()) {
            int titleBarHeight = titleBarFlowPane.getPreferredHeight(-1);

            graphics.setPaint(titleBarBorderColor);
            graphics.drawLine(0, 1 + titleBarHeight, width - 1, 1 + titleBarHeight);
        }

        int titleBarX = titleBarFlowPane.getX();
        int titleBarY = titleBarFlowPane.getY();
        int titleBarWidth = titleBarFlowPane.getWidth();
        int titleBarHeight = titleBarFlowPane.getHeight();

        graphics.setPaint(new GradientPaint(titleBarX + titleBarWidth / 2, titleBarY, titleBarBevelColor,
    		titleBarX + titleBarWidth / 2, titleBarY + titleBarHeight, titleBarBackgroundColor));
        graphics.fillRect(titleBarX, titleBarY, titleBarWidth, titleBarHeight);

        graphics.setPaint(borderColor);
        graphics.drawRect(0, 0, width - 1, height - 1);
    }

    public Font getTitleBarFont() {
        return (Font)titleLabel.getStyles().get("font");
    }

    public void setTitleBarFont(Font titleBarFont) {
        titleLabel.getStyles().put("font", titleBarFont);
    }

    public final void setTitleBarFont(String titleBarFont) {
        if (titleBarFont == null) {
            throw new IllegalArgumentException("titleBarFont is null.");
        }

        setTitleBarFont(Font.decode(titleBarFont));
    }

    public Color getTitleBarColor() {
        return (Color)titleLabel.getStyles().get("color");
    }

    public void setTitleBarColor(Color titleBarColor) {
        titleLabel.getStyles().put("color", titleBarColor);
    }

    public final void setTitleBarColor(String titleBarColor) {
        if (titleBarColor == null) {
            throw new IllegalArgumentException("titleBarColor is null.");
        }

        setTitleBarColor(decodeColor(titleBarColor));
    }

    public Color getTitleBarBackgroundColor() {
        return titleBarBackgroundColor;
    }

    public void setTitleBarBackgroundColor(Color titleBarBackgroundColor) {
        this.titleBarBackgroundColor = titleBarBackgroundColor;
        titleBarBevelColor = TerraTheme.brighten(titleBarBackgroundColor);
        repaintComponent();
    }

    public final void setTitleBarBackgroundColor(String titleBarBackgroundColor) {
        if (titleBarBackgroundColor == null) {
            throw new IllegalArgumentException("titleBarBackgroundColor is null.");
        }

        setTitleBarBackgroundColor(decodeColor(titleBarBackgroundColor));
    }

    public Color getTitleBarBorderColor() {
        return titleBarBorderColor;
    }

    public void setTitleBarBorderColor(Color titleBarBorderColor) {
        this.titleBarBorderColor = titleBarBorderColor;
        repaintComponent();
    }

    public final void setTitleBarBorderColor(String titleBarBorderColor) {
        if (titleBarBorderColor == null) {
            throw new IllegalArgumentException("titleBarBorderColor is null.");
        }

        setTitleBarBorderColor(decodeColor(titleBarBorderColor));
    }

    public Color getShadeButtonColor() {
        return shadeButtonColor;
    }

    public void setShadeButtonColor(Color shadeButtonColor) {
        this.shadeButtonColor = shadeButtonColor;
        repaintComponent();
    }

    public final void setShadeButtonColor(String shadeButtonColor) {
        if (shadeButtonColor == null) {
            throw new IllegalArgumentException("shadeButtonColor is null.");
        }

        setShadeButtonColor(decodeColor(shadeButtonColor));
    }

    public Color getShadeButtonBackgroundColor() {
        return shadeButtonBackgroundColor;
    }

    public void setShadeButtonBackgroundColor(Color shadeButtonBackgroundColor) {
        this.shadeButtonBackgroundColor = shadeButtonBackgroundColor;
        shadeButton.getStyles().put("shadeButtonBackgroundColor", shadeButtonBackgroundColor);
        repaintComponent();
    }

    public final void setShadeButtonBackgroundColor(String shadeButtonBackgroundColor) {
        if (shadeButtonBackgroundColor == null) {
            throw new IllegalArgumentException("shadeButtonBackgroundColor is null.");
        }

        setShadeButtonBackgroundColor(decodeColor(shadeButtonBackgroundColor));
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        setBorderColor(decodeColor(borderColor));
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
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

    /**
     * Listener for expander button events.
     *
     * @param button
     *     The source of the button event.
     */
    public void buttonPressed(Button button) {
        Expander expander = (Expander)getComponent();

        expander.setExpanded(!expander.isExpanded());
    }

    // Expander events
    public void titleChanged(Expander expander, String previousTitle) {
        String title = expander.getTitle();
        titleLabel.setDisplayable(title != null);
        titleLabel.setText(title);
    }

    public Vote previewExpandedChange(final Expander expander) {
        Vote vote = Vote.APPROVE;

        if (expander.isShowing()) {
            if (expandTransition == null) {
                if (expander.isExpanded()
            		&& expander.getContent() != null) {
                    expandTransition = new ExpandTransition(true, EXPAND_DURATION, EXPAND_RATE);

                    layout();
                    expandTransition.start(new TransitionListener() {
                        public void transitionCompleted(Transition transition) {
                            expander.setExpanded(false);
                            expandTransition = null;
                        }
                    });

                    vote = Vote.DEFER;
                }
            } else {
            	if (expandTransition.isRunning()) {
                	vote = Vote.DEFER;
            	}
            }
        }

        return vote;
    }

    public void expandedChangeVetoed(Expander expander, Vote reason) {
        if (reason == Vote.DENY
            && expandTransition != null) {
            expandTransition.stop();
            expandTransition = null;
            invalidateComponent();
        }
    }

    public void expandedChanged(Expander expander) {
        if (expander.isShowing()) {
            if (expander.isExpanded()
        		&& expander.getContent() != null) {
                expandTransition = new ExpandTransition(false, EXPAND_DURATION, EXPAND_RATE);

                layout();
                expandTransition.start(new TransitionListener() {
                    public void transitionCompleted(Transition transition) {
                        expandTransition = null;
                    }
                });
            }
        }

        Image buttonData = expander.isExpanded() ? collapseImage : expandImage;
        shadeButton.setButtonData(buttonData);

        invalidateComponent();
    }

    public void contentChanged(Expander expander, Component previousContent) {
    	if (expandTransition != null) {
    		expandTransition.stop();
    		expandTransition = null;
    	}

        invalidateComponent();
    }
}
