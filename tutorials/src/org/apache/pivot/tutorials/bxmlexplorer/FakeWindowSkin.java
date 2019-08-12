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
package org.apache.pivot.tutorials.bxmlexplorer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.effects.DropShadowDecorator;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.ContainerSkin;
import org.apache.pivot.wtk.skin.terra.TerraFrameSkin.FrameButton;
import org.apache.pivot.wtk.skin.terra.TerraTheme;
import org.apache.pivot.wtk.util.ColorUtilities;

public class FakeWindowSkin extends ContainerSkin implements FakeWindowListener {

    /**
     * Abstract base class for frame button images.
     */
    protected abstract class ButtonImage extends Image {
        @Override
        public int getWidth() {
            return 8;
        }

        @Override
        public int getHeight() {
            return 8;
        }
    }

    /**
     * Minimize button image.
     */
    protected class MinimizeImage extends ButtonImage {
        @Override
        public void paint(Graphics2D graphics) {
            graphics.setPaint(titleBarColor);
            graphics.fillRect(0, 6, 8, 2);
        }
    }

    /**
     * Maximize button image.
     */
    protected class MaximizeImage extends ButtonImage {
        @Override
        public void paint(Graphics2D graphics) {
            graphics.setPaint(titleBarColor);
            graphics.fillRect(0, 0, 8, 8);

            graphics.setPaint(titleBarBackgroundColor);
            graphics.fillRect(2, 2, 4, 4);
        }
    }

    /**
     * Restore button image.
     */
    protected class RestoreImage extends ButtonImage {
        @Override
        public void paint(Graphics2D graphics) {
            graphics.setPaint(titleBarColor);
            graphics.fillRect(1, 1, 6, 6);

            graphics.setPaint(titleBarBackgroundColor);
            graphics.fillRect(3, 3, 2, 2);
        }
    }

    /**
     * Close button image.
     */
    protected class CloseImage extends ButtonImage {
        @Override
        public void paint(Graphics2D graphics) {
            graphics.setPaint(titleBarColor);
            graphics.setStroke(new BasicStroke(2));

            GraphicsUtilities.setAntialiasingOn(graphics);

            graphics.draw(new Line2D.Double(0.5, 0.5, 7.5, 7.5));
            graphics.draw(new Line2D.Double(0.5, 7.5, 7.5, 0.5));
        }
    }

    /**
     * Resize button image.
     */
    protected class ResizeImage extends Image {
        public static final int ALPHA = 64;

        @Override
        public int getWidth() {
            return 5;
        }

        @Override
        public int getHeight() {
            return 5;
        }

        @Override
        public void paint(Graphics2D graphics) {
            graphics.setPaint(new Color(0, 0, 0, ALPHA));
            graphics.fillRect(3, 0, 2, 1);
            graphics.fillRect(0, 3, 2, 1);
            graphics.fillRect(3, 3, 2, 1);

            graphics.setPaint(ColorUtilities.toTransparentColor(contentBorderColor, ALPHA));

            graphics.fillRect(3, 1, 2, 1);
            graphics.fillRect(0, 4, 2, 1);
            graphics.fillRect(3, 4, 2, 1);
        }
    }

    private Image minimizeImage = new MinimizeImage();
    private Image maximizeImage = new MaximizeImage();
    private Image closeImage = new CloseImage();

    private TablePane titleBarTablePane = new TablePane();
    private BoxPane titleBoxPane = new BoxPane();
    private BoxPane buttonBoxPane = new BoxPane();

    private ImageView iconImageView = new ImageView();
    private Label titleLabel = new Label();

    private FrameButton minimizeButton = null;
    private FrameButton maximizeButton = null;
    private FrameButton closeButton = null;

    private DropShadowDecorator dropShadowDecorator = null;

    private Color titleBarColor;
    private Color titleBarBackgroundColor;
    private Color titleBarBorderColor;
    private Color contentBorderColor;
    private Insets padding;

    // Derived colors
    private Color titleBarBevelColor;

    // private Color contentBevelColor; // TODO: future use

    @SuppressWarnings("unused")
    public FakeWindowSkin() {
        Theme theme = currentTheme();
        setBackgroundColor(10);

        titleBarColor = theme.getColor(4);
        titleBarBackgroundColor = theme.getColor(14);
        titleBarBorderColor = theme.getColor(12);
        contentBorderColor = theme.getColor(7);
        padding = new Insets(8);

        // Set the derived colors
        titleBarBevelColor = TerraTheme.brighten(titleBarBackgroundColor);

        // The title bar table pane contains two nested box panes: one for
        // the title contents and the other for the buttons
        new TablePane.Column(titleBarTablePane, 1, true);
        new TablePane.Column(titleBarTablePane, -1);

        TablePane.Row titleRow = new TablePane.Row(titleBarTablePane, -1);

        titleRow.add(titleBoxPane);
        titleRow.add(buttonBoxPane);

        titleBarTablePane.getStyles().put(Style.padding, new Insets(2));

        // Initialize the title box pane
        titleBoxPane.add(iconImageView);
        titleBoxPane.add(titleLabel);
        titleBoxPane.getStyles().put(Style.verticalAlignment, VerticalAlignment.CENTER);
        titleBoxPane.getStyles().put(Style.padding, new Insets(0, 0, 0, 2));

        Font titleFont = theme.getFont().deriveFont(Font.BOLD);
        titleLabel.getStyles().put(Style.font, titleFont);

        iconImageView.setPreferredSize(16, 16);
        iconImageView.getStyles().put(Style.fill, true);
        iconImageView.getStyles().put(Style.backgroundColor, null);

        // Initialize the button box pane
        buttonBoxPane.getStyles().put(Style.horizontalAlignment, HorizontalAlignment.RIGHT);
        buttonBoxPane.getStyles().put(Style.verticalAlignment, VerticalAlignment.CENTER);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        FakeWindow window = (FakeWindow) component;
        window.getWindowListeners().add(this);

        FakeWindow frame = (FakeWindow) getComponent();

        // Attach the drop-shadow decorator
        dropShadowDecorator = new DropShadowDecorator();
        frame.getDecorators().add(dropShadowDecorator);

        frame.add(titleBarTablePane);

        // Create the frame buttons
        minimizeButton = new FrameButton(minimizeImage);
        maximizeButton = new FrameButton(maximizeImage);
        closeButton = new FrameButton(closeImage);

        buttonBoxPane.add(minimizeButton);
        buttonBoxPane.add(maximizeButton);
        buttonBoxPane.add(closeButton);

        iconAdded(frame, null);
        titleChanged(frame, null);
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;
        int heightMutable = height;

        FakeWindow frame = (FakeWindow) getComponent();

        // Include title bar width plus left/right title bar borders
        Dimensions titleBarSize = titleBarTablePane.getPreferredSize();
        preferredWidth = Math.max(titleBarSize.width + 2, preferredWidth);

        if (heightMutable != -1) {
            // Subtract title bar height and top/bottom title bar borders
            // from height constraint
            heightMutable -= titleBarSize.height + 2;
        }

        Component content = frame.getContent();
        if (content != null) {
            if (heightMutable != -1) {
                // Subtract padding, top/bottom content borders, and content
                // bevel from height constraint
                heightMutable -= padding.getHeight() + (1) + 2;
                heightMutable = Math.max(heightMutable, 0);
            }

            preferredWidth = Math.max(preferredWidth, content.getPreferredWidth(heightMutable));
        }

        // Add padding and left/right content borders
        preferredWidth += padding.getWidth() + 2;

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;
        int widthMutable = width;

        FakeWindow frame = (FakeWindow) getComponent();

        // Include title bar height plus top/bottom title bar borders
        preferredHeight += titleBarTablePane.getPreferredHeight() + 2;

        Component content = frame.getContent();
        if (content != null) {
            if (widthMutable != -1) {
                // Subtract padding and left/right content borders from
                // constraint
                widthMutable -= padding.getWidth() + 2;
                widthMutable = Math.max(widthMutable, 0);
            }

            preferredHeight += content.getPreferredHeight(widthMutable);
        }

        // Add padding, top/bottom content borders, and content bevel
        preferredHeight += padding.getHeight() + (1) + 2;

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        FakeWindow frame = (FakeWindow) getComponent();

        // Include title bar width plus left/right title bar borders
        Dimensions titleBarSize = titleBarTablePane.getPreferredSize();
        preferredWidth = Math.max(preferredWidth, titleBarSize.width + 2);

        // Include title bar height plus top/bottom title bar borders
        preferredHeight += titleBarSize.height + 2;

        Component content = frame.getContent();
        if (content != null) {
            Dimensions preferredContentSize = content.getPreferredSize();

            preferredWidth = Math.max(preferredWidth, preferredContentSize.width);
            preferredHeight += preferredContentSize.height;
        }

        // Add padding, borders, and content bevel
        preferredWidth += padding.getWidth() + 2;
        preferredHeight += padding.getHeight() + (1) + 2;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public void layout() {
        FakeWindow frame = (FakeWindow) getComponent();

        int width = getWidth();
        int height = getHeight();

        int clientX = 1;
        int clientY = 1;
        int clientWidth = Math.max(width - 2, 0);
        int clientHeight = Math.max(height - 2, 0);

        // Size/position title bar
        titleBarTablePane.setLocation(clientX, clientY);
        titleBarTablePane.setSize(clientWidth, titleBarTablePane.getPreferredHeight());
        titleBarTablePane.setVisible(true);

        // Add bottom title bar border, top content border, and content bevel
        clientY += titleBarTablePane.getHeight() + (1) + 2;

        // Size/position content
        Component content = frame.getContent();
        if (content != null) {
            int contentX = clientX + padding.left;
            int contentY = clientY + padding.top;
            int contentWidth = Math.max(clientWidth - padding.getWidth(), 0);
            int contentHeight = Math.max(clientHeight - (clientY + padding.getHeight()) + (1), 0);

            content.setLocation(contentX, contentY);
            content.setSize(contentWidth, contentHeight);
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        // Call the base class to paint the background
        super.paint(graphics);

        int width = getWidth();
        int height = getHeight();

        int titleBarHeight = titleBarTablePane.getHeight();

        // Draw the title area
        Color titleBarBackgroundColorLocal = this.titleBarBackgroundColor;
        Color titleBarBorderColorLocal = this.titleBarBorderColor;
        Color titleBarBevelColorLocal = this.titleBarBevelColor;

        graphics.setPaint(new GradientPaint(width / 2f, 0, titleBarBevelColorLocal, width / 2f,
            titleBarHeight + 1, titleBarBackgroundColorLocal));
        graphics.fillRect(0, 0, width, titleBarHeight + 1);

        // Draw the border
        graphics.setPaint(titleBarBorderColorLocal);
        GraphicsUtilities.drawRect(graphics, 0, 0, width, titleBarHeight + 2);

        // Draw the content area
        Bounds contentAreaRectangle = new Bounds(0, titleBarHeight + 2, width, height
            - (titleBarHeight + 2));
        graphics.setPaint(contentBorderColor);
        GraphicsUtilities.drawRect(graphics, contentAreaRectangle.x, contentAreaRectangle.y,
            contentAreaRectangle.width, contentAreaRectangle.height);

        // graphics.setPaint(contentBevelColor);
        GraphicsUtilities.drawLine(graphics, contentAreaRectangle.x + 1,
            contentAreaRectangle.y + 1, contentAreaRectangle.width - 2, Orientation.HORIZONTAL);
    }

    @Override
    public void titleChanged(FakeWindow window, String previousTitle) {
        String title = window.getTitle();
        titleLabel.setVisible(title != null);
        titleLabel.setText(title != null ? title : "");
    }

    @Override
    public void iconInserted(FakeWindow window, Image addedIcon, int index) {
        // No-op
    }

    @Override
    public void iconAdded(FakeWindow window, Image addedIcon) {
        if (window.getIcons().getLength() > 0) {
            iconImageView.setVisible(true);
            iconImageView.setImage(window.getIcons().get(0));
        } else {
            iconImageView.setVisible(false);
            iconImageView.setImage((Image) null);
        }
    }

    @Override
    public void iconsRemoved(FakeWindow window, int index, Sequence<Image> removed) {
        if (window.getIcons().getLength() > 0) {
            iconImageView.setVisible(true);
            iconImageView.setImage(window.getIcons().get(0));
        } else {
            iconImageView.setVisible(false);
            iconImageView.setImage((Image) null);
        }
    }

    @Override
    public void contentChanged(FakeWindow window, Component previousContent) {
        invalidateComponent();
    }

}
