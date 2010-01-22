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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.FormAttributeListener;
import org.apache.pivot.wtk.FormListener;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Separator;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;
import org.apache.pivot.wtk.effects.Decorator;
import org.apache.pivot.wtk.effects.DropShadowDecorator;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.ContainerSkin;

/**
 * Terra form skin.
 * <p>
 * TODO Create a section info structure
 * <p>
 * TODO Drop use of BoxPane for headers and move image views out into the section info structure
 * <p>
 * TODO Dynamically calculate field identifier decorator size based on component size (both types)
 * <p>
 * TODO Animate preferred size calculations when flags change (make this configurable via
 * a style flag)
 * <p>
 * TODO Create message objects as needed depending on showFlagMessagesInline flag?
 */
public class TerraFormSkin extends ContainerSkin
    implements FormListener, FormAttributeListener {
    private static class PopupFieldIdentifierDecorator implements Decorator {
        private Component component = null;
        private Graphics2D graphics = null;

        @Override
        public Graphics2D prepare(Component component, Graphics2D graphics) {
            this.component = component;
            this.graphics = graphics;
            return graphics;
        }

        @Override
        public void update() {
            GeneralPath arrow = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
            arrow.moveTo(POPUP_FIELD_INDICATOR_OFFSET, 0);
            arrow.lineTo(POPUP_FIELD_INDICATOR_OFFSET + POPUP_FIELD_INDICATOR_WIDTH / 2, -POPUP_FIELD_INDICATOR_HEIGHT);
            arrow.lineTo(POPUP_FIELD_INDICATOR_OFFSET + POPUP_FIELD_INDICATOR_WIDTH, 0);
            arrow.closePath();

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.setStroke(new BasicStroke(0));
            graphics.setColor((Color)component.getStyles().get("backgroundColor"));

            graphics.draw(arrow);
            graphics.fill(arrow);

            component = null;
            graphics = null;
        }

        @Override
        public Bounds getBounds(Component component) {
            return new Bounds(POPUP_FIELD_INDICATOR_OFFSET, -POPUP_FIELD_INDICATOR_HEIGHT,
                POPUP_FIELD_INDICATOR_WIDTH, POPUP_FIELD_INDICATOR_HEIGHT);
        }

        @Override
        public AffineTransform getTransform(Component component) {
            return new AffineTransform();
        }
    }

    private static class InlineFieldIdentifierDecorator implements Decorator {
        private Component component = null;
        private Graphics2D graphics = null;

        @Override
        public Graphics2D prepare(Component component, Graphics2D graphics) {
            this.component = component;
            this.graphics = graphics;
            return graphics;
        }

        @Override
        public void update() {
            Label label = (Label)component;
            if (label.getText() != null) {
                int height = component.getHeight();

                GeneralPath arrow = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
                arrow.moveTo(0, 0);
                arrow.lineTo(-height / 2, height / 2);
                arrow.lineTo(0, height);
                arrow.closePath();

                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                graphics.setColor((Color)component.getStyles().get("backgroundColor"));
                graphics.fill(arrow);
            }

            component = null;
            graphics = null;
        }

        @Override
        public Bounds getBounds(Component component) {
            int height = component.getHeight();
            return new Bounds(-height / 2, 0, height / 2, height);
        }

        @Override
        public AffineTransform getTransform(Component component) {
            return new AffineTransform();
        }
    }

    private ArrayList<Separator> separators = new ArrayList<Separator>();
    private ArrayList<ArrayList<BoxPane>> rowHeaders = new ArrayList<ArrayList<BoxPane>>();

    private Label flagMessageLabel = new Label();
    private Window flagMessageWindow = new Window(flagMessageLabel);
    private ArrayList<ArrayList<Label>> flagMessageLabels = new ArrayList<ArrayList<Label>>();

    private Insets padding;
    private int horizontalSpacing;
    private int verticalSpacing;
    private int flagImageOffset;
    private boolean fill;
    private boolean showFirstSectionHeading;
    private boolean showFlagIcons;
    private boolean showFlagHighlight;
    private boolean showFlagMessagesInline;
    private boolean leftAlignLabels;
    private String delimiter;
    private Image errorMessageIcon = null;
    private Image warningMessageIcon = null;
    private Image questionMessageIcon = null;
    private Image infoMessageIcon = null;

    // TODO Add values for flag message colors; expose both icons and colors as styles

    private int flagImageWidth = 0;

    private ComponentMouseListener fieldMouseListener = new ComponentMouseListener.Adapter() {
        @Override
        public void mouseOver(Component component) {
            if (!showFlagMessagesInline) {
                Form.Flag flag = Form.getFlag(component);

                if (flag != null) {
                    String message = flag.getMessage();

                    if (message != null) {
                        flagMessageLabel.setText(message);

                        MessageType messageType = flag.getMessageType();
                        TerraTheme theme = (TerraTheme)Theme.getTheme();

                        Color color = null;
                        Color backgroundColor = null;

                        switch (messageType) {
                            case ERROR: {
                                color = theme.getColor(4);
                                backgroundColor = theme.getColor(22);
                                break;
                            }

                            case WARNING: {
                                color = theme.getColor(1);
                                backgroundColor = theme.getColor(19);
                                break;
                            }

                            case QUESTION: {
                                color = theme.getColor(4);
                                backgroundColor = theme.getColor(16);
                                break;
                            }

                            case INFO: {
                                color = theme.getColor(1);
                                backgroundColor = theme.getColor(10);
                                break;
                            }
                        }

                        flagMessageLabel.getStyles().put("color", color);
                        flagMessageWindow.getStyles().put("backgroundColor", backgroundColor);

                        // Open the window
                        Point location = component.mapPointToAncestor(component.getDisplay(), 0,
                            component.getHeight());

                        int y = location.y + POPUP_FIELD_INDICATOR_HEIGHT - 4;
                        if (showFlagHighlight) {
                            y += FLAG_HIGHLIGHT_PADDING;
                        }

                        flagMessageWindow.setLocation(location.x, y);
                        flagMessageWindow.open(component.getWindow());
                    }
                }
            }
        }

        @Override
        public void mouseOut(Component component) {
            flagMessageWindow.close();
        }
    };

    private static final int FLAG_HIGHLIGHT_PADDING = 2;

    private static final int POPUP_FIELD_INDICATOR_WIDTH = 13;
    private static final int POPUP_FIELD_INDICATOR_HEIGHT = 6;
    private static final int POPUP_FIELD_INDICATOR_OFFSET = 10;
    private static final int HIDE_POPUP_MESSAGE_DELAY = 3500;

    private static final String DEFAULT_DELIMITER = ":";

    public TerraFormSkin() {
        padding = new Insets(4);
        horizontalSpacing = 6;
        verticalSpacing = 6;
        flagImageOffset = 4;
        fill = false;
        showFirstSectionHeading = false;
        showFlagIcons = true;
        showFlagHighlight = true;
        showFlagMessagesInline = false;
        leftAlignLabels = false;
        delimiter = DEFAULT_DELIMITER;

        TerraTheme terraTheme = (TerraTheme)Theme.getTheme();
        errorMessageIcon = terraTheme.getSmallMessageIcon(MessageType.ERROR);
        warningMessageIcon = terraTheme.getSmallMessageIcon(MessageType.WARNING);
        questionMessageIcon = terraTheme.getSmallMessageIcon(MessageType.QUESTION);
        infoMessageIcon = terraTheme.getSmallMessageIcon(MessageType.INFO);

        // Determine maximum icon size
        flagImageWidth = Math.max(flagImageWidth, errorMessageIcon.getWidth());
        flagImageWidth = Math.max(flagImageWidth, warningMessageIcon.getWidth());
        flagImageWidth = Math.max(flagImageWidth, questionMessageIcon.getWidth());
        flagImageWidth = Math.max(flagImageWidth, infoMessageIcon.getWidth());

        // Create the flag message popup
        flagMessageLabel.getStyles().put("padding", new Insets(3, 4, 3, 4));

        flagMessageWindow.getDecorators().add(new DropShadowDecorator(3, 3, 3));
        flagMessageWindow.getDecorators().add(new PopupFieldIdentifierDecorator());

        flagMessageWindow.getWindowStateListeners().add(new WindowStateListener.Adapter() {
            private ApplicationContext.ScheduledCallback scheduledHideFlagMessageCallback = null;

            @Override
            public void windowOpened(Window window) {
                // Set a timer to hide the message
                Runnable hideFlagMessageCallback = new Runnable() {
                    public void run() {
                        flagMessageWindow.close();
                    }
                };

                scheduledHideFlagMessageCallback =
                    ApplicationContext.scheduleCallback(hideFlagMessageCallback,
                        HIDE_POPUP_MESSAGE_DELAY);
            }

            @Override
            public void windowClosed(Window window, Display display, Window owner) {
                scheduledHideFlagMessageCallback.cancel();
            }
        });
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Form form = (Form) component;
        form.getFormListeners().add(this);
        form.getFormAttributeListeners().add(this);

        Form.SectionSequence sections = form.getSections();
        for (int i = 0, n = sections.getLength(); i < n; i++) {
            insertSection(sections.get(i), i);
        }
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        int maximumRowHeaderWidth = 0;
        int maximumFieldWidth = 0;
        int maximumFlagMessageWidth = 0;
        int maximumSeparatorWidth = 0;

        Form form = (Form)getComponent();
        Form.SectionSequence sections = form.getSections();

        for (int sectionIndex = 0, sectionCount = sections.getLength();
            sectionIndex < sectionCount; sectionIndex++) {
            Form.Section section = sections.get(sectionIndex);

            if (showFirstSectionHeading
                || sectionIndex > 0) {
                Separator separator = separators.get(sectionIndex);
                maximumSeparatorWidth = Math.max(maximumSeparatorWidth,
                    separator.getPreferredWidth());
            }

            for (int fieldIndex = 0, fieldCount = section.getLength();
                fieldIndex < fieldCount; fieldIndex++) {
                Component field = section.get(fieldIndex);

                if (field.isVisible()) {
                    BoxPane rowHeader = rowHeaders.get(sectionIndex).get(fieldIndex);
                    maximumRowHeaderWidth = Math.max(maximumRowHeaderWidth,
                        rowHeader.getPreferredWidth(-1));
                    maximumFieldWidth = Math.max(maximumFieldWidth,
                        field.getPreferredWidth(-1));

                    if (showFlagMessagesInline) {
                        Label flagMessageLabel = flagMessageLabels.get(sectionIndex).get(fieldIndex);
                        maximumFlagMessageWidth = Math.max(maximumFlagMessageWidth,
                            flagMessageLabel.getPreferredWidth(-1));
                    }
                }
            }
        }

        preferredWidth = maximumRowHeaderWidth + horizontalSpacing + maximumFieldWidth;

        if (showFlagMessagesInline) {
            preferredWidth += horizontalSpacing + maximumFlagMessageWidth;
        }

        preferredWidth = Math.max(preferredWidth + padding.left + padding.right,
            maximumSeparatorWidth);

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        Form form = (Form)getComponent();
        Form.SectionSequence sections = form.getSections();

        // Determine the field width constraint
        int fieldWidth = (fill && width != -1) ? getFieldWidth(width) : -1;

        for (int sectionIndex = 0, sectionCount = sections.getLength();
            sectionIndex < sectionCount; sectionIndex++) {
            Form.Section section = sections.get(sectionIndex);

            if (showFirstSectionHeading
                || sectionIndex > 0) {
                Separator separator = separators.get(sectionIndex);
                preferredHeight += separator.getPreferredHeight(width);
                preferredHeight += verticalSpacing;
            }

            for (int fieldIndex = 0, fieldCount = section.getLength();
                fieldIndex < fieldCount; fieldIndex++) {
                Component field = section.get(fieldIndex);

                if (field.isVisible()) {
                    // Determine the row header size and baseline
                    BoxPane rowHeader = rowHeaders.get(sectionIndex).get(fieldIndex);

                    Dimensions rowHeaderSize = rowHeader.getPreferredSize();
                    int rowHeaderAscent = rowHeader.getBaseline(rowHeaderSize.width, rowHeaderSize.height);
                    int rowHeaderDescent = rowHeaderSize.height - rowHeaderAscent;

                    // Determine the field size and baseline
                    Dimensions fieldSize;
                    if (fill && fieldWidth != -1) {
                        fieldSize = new Dimensions(fieldWidth, field.getPreferredHeight(fieldWidth));
                    } else {
                        fieldSize = field.getPreferredSize();
                    }

                    int fieldAscent = field.getBaseline(fieldSize.width, fieldSize.height);
                    if (fieldAscent == -1) {
                        fieldAscent = fieldSize.height;
                    }

                    int fieldDescent = fieldSize.height - fieldAscent;

                    // Determine the message label size and baseline
                    int flagMessageLabelAscent = 0;
                    int flagMessageLabelDescent = 0;

                    if (showFlagMessagesInline) {
                        Label flagMessageLabel = flagMessageLabels.get(sectionIndex).get(fieldIndex);

                        Dimensions flagMessageLabelSize = flagMessageLabel.getPreferredSize();
                        flagMessageLabelAscent = flagMessageLabel.getBaseline(flagMessageLabelSize.width,
                            flagMessageLabelSize.height);
                        flagMessageLabelDescent = flagMessageLabelSize.height - flagMessageLabelAscent;
                    }

                    // Determine the row height
                    int maximumAscent = Math.max(rowHeaderAscent, Math.max(fieldAscent, flagMessageLabelAscent));
                    int maximumDescent = Math.max(rowHeaderDescent, Math.max(fieldDescent, flagMessageLabelDescent));

                    int rowHeight = maximumAscent + maximumDescent;

                    preferredHeight += rowHeight;

                    if (fieldIndex > 0) {
                        preferredHeight += verticalSpacing;
                    }
                }
            }
        }

        preferredHeight += (padding.top + padding.bottom);

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        // TODO Optimize
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    @Override
    public int getBaseline(int width, int height) {
        int baseline = -1;

        Form form = (Form)getComponent();
        Form.SectionSequence sections = form.getSections();

        // Determine the field width constraint
        int fieldWidth = (fill) ? getFieldWidth(width) : -1;

        int sectionCount = sections.getLength();
        int sectionIndex = 0;

        int rowY = 0;
        while (sectionIndex < sectionCount
            && baseline == -1) {
            Form.Section section = sections.get(sectionIndex);

            if (showFirstSectionHeading
                || sectionIndex > 0) {
                Separator separator = separators.get(sectionIndex);
                rowY += separator.getPreferredHeight(width);
                rowY += verticalSpacing;
            }

            int fieldCount = section.getLength();
            int fieldIndex = 0;

            while (fieldIndex < fieldCount
                && baseline == -1) {
                Component field = section.get(fieldIndex);

                if (field.isVisible()) {
                    BoxPane rowHeader = rowHeaders.get(sectionIndex).get(fieldIndex);

                    // Determine the row header size and baseline
                    Dimensions rowHeaderSize = rowHeader.getPreferredSize();
                    int rowHeaderAscent = rowHeader.getBaseline(rowHeaderSize.width, rowHeaderSize.height);

                    // Determine the field size and baseline
                    Dimensions fieldSize;
                    if (fill && fieldWidth != -1) {
                        fieldSize = new Dimensions(fieldWidth, field.getPreferredHeight(fieldWidth));
                    } else {
                        fieldSize = field.getPreferredSize();
                    }

                    int fieldAscent = field.getBaseline(fieldSize.width, fieldSize.height);
                    if (fieldAscent == -1) {
                        fieldAscent = rowHeaderAscent;
                    }

                    // Determine the message label size and baseline
                    int flagMessageLabelAscent = 0;

                    if (showFlagMessagesInline) {
                        Label flagMessageLabel = flagMessageLabels.get(sectionIndex).get(fieldIndex);

                        Dimensions flagMessageLabelSize = flagMessageLabel.getPreferredSize();
                        flagMessageLabelAscent = flagMessageLabel.getBaseline(flagMessageLabelSize.width,
                            flagMessageLabelSize.height);
                    }

                    // Determine the baseline
                    int maximumAscent = Math.max(rowHeaderAscent, Math.max(fieldAscent, flagMessageLabelAscent));
                    baseline = rowY + maximumAscent;
                }

                fieldIndex++;
            }

            sectionIndex++;
        }

        baseline += padding.top;

        return baseline;
    }

    private int getFieldWidth(int width) {
        int maximumRowHeaderWidth = 0;
        int maximumFlagMessageWidth = 0;

        Form form = (Form)getComponent();
        Form.SectionSequence sections = form.getSections();

        for (int sectionIndex = 0, sectionCount = sections.getLength();
            sectionIndex < sectionCount; sectionIndex++) {
            Form.Section section = sections.get(sectionIndex);

            for (int fieldIndex = 0, fieldCount = section.getLength();
                fieldIndex < fieldCount; fieldIndex++) {
                Component field = section.get(fieldIndex);

                if (field.isVisible()) {
                    BoxPane rowHeader = rowHeaders.get(sectionIndex).get(fieldIndex);
                    maximumRowHeaderWidth = Math.max(maximumRowHeaderWidth,
                        rowHeader.getPreferredWidth(-1));

                    if (showFlagMessagesInline) {
                        Label flagMessageLabel = flagMessageLabels.get(sectionIndex).get(fieldIndex);

                        maximumFlagMessageWidth = Math.max(maximumFlagMessageWidth,
                            flagMessageLabel.getPreferredWidth(-1));
                    }
                }
            }
        }

        int fieldWidth = Math.max(0, width - (maximumRowHeaderWidth + horizontalSpacing));

        if (showFlagMessagesInline) {
            fieldWidth = Math.max(0, fieldWidth - (horizontalSpacing + maximumFlagMessageWidth));
        }

        fieldWidth = Math.max(0, fieldWidth - (padding.left + padding.right));

        return fieldWidth;
    }

    @Override
    public void layout() {
        Form form = (Form)getComponent();
        Form.SectionSequence sections = form.getSections();

        // Determine the maximum row header and message flag widths
        int maximumRowHeaderWidth = 0;
        int maximumFlagMessageWidth = 0;

        for (int sectionIndex = 0, sectionCount = sections.getLength();
            sectionIndex < sectionCount; sectionIndex++) {
            Form.Section section = sections.get(sectionIndex);

            for (int fieldIndex = 0, fieldCount = section.getLength();
                fieldIndex < fieldCount; fieldIndex++) {
                Component field = section.get(fieldIndex);

                if (field.isVisible()) {
                    BoxPane rowHeader = rowHeaders.get(sectionIndex).get(fieldIndex);
                    maximumRowHeaderWidth = Math.max(maximumRowHeaderWidth,
                        rowHeader.getPreferredWidth(-1));

                    if (showFlagMessagesInline) {
                        Label flagMessageLabel = flagMessageLabels.get(sectionIndex).get(fieldIndex);

                        maximumFlagMessageWidth = Math.max(maximumFlagMessageWidth,
                            flagMessageLabel.getPreferredWidth(-1));
                    }
                }
            }
        }

        // Determine the field width
        int width = getWidth();
        int fieldWidth = Math.max(0, width - (maximumRowHeaderWidth + horizontalSpacing));

        if (showFlagMessagesInline) {
            fieldWidth = Math.max(0, fieldWidth - (horizontalSpacing + maximumFlagMessageWidth));
        }

        fieldWidth = Math.max(0, fieldWidth - (padding.left + padding.right));

        // Lay out the components
        int rowX = padding.left;
        int rowY = padding.top;

        for (int sectionIndex = 0, sectionCount = sections.getLength();
            sectionIndex < sectionCount; sectionIndex++) {
            Form.Section section = sections.get(sectionIndex);

            Separator separator = separators.get(sectionIndex);
            if (sectionIndex == 0
                && !showFirstSectionHeading) {
                separator.setVisible(false);
            } else {
                separator.setVisible(true);
                separator.setSize(width, separator.getPreferredHeight(width));
                separator.setLocation(rowX, rowY);
                rowY += separator.getHeight();
            }

            for (int fieldIndex = 0, fieldCount = section.getLength();
                fieldIndex < fieldCount; fieldIndex++) {
                Component field = section.get(fieldIndex);

                BoxPane rowHeader = rowHeaders.get(sectionIndex).get(fieldIndex);

                if (field.isVisible()) {
                    // Show the row header
                    rowHeader.setVisible(true);

                    // Determine the row header size and baseline
                    Dimensions rowHeaderSize = new Dimensions(maximumRowHeaderWidth,
                        rowHeader.getPreferredHeight());
                    rowHeader.setSize(rowHeaderSize);
                    int rowHeaderAscent = rowHeader.getBaseline(rowHeaderSize.width, rowHeaderSize.height);
                    int rowHeaderDescent = rowHeaderSize.height - rowHeaderAscent;

                    // Determine the field size and baseline
                    Dimensions fieldSize;
                    if (fill) {
                        fieldSize = new Dimensions(fieldWidth, field.getPreferredHeight(fieldWidth));
                    } else {
                        fieldSize = field.getPreferredSize();
                    }

                    field.setSize(fieldSize);

                    int fieldAscent = field.getBaseline(fieldSize.width, fieldSize.height);
                    if (fieldAscent == -1) {
                        fieldAscent = rowHeaderAscent;
                    }

                    int fieldDescent = fieldSize.height - fieldAscent;

                    // Determine the message label size and baseline
                    int flagMessageLabelAscent = 0;
                    int flagMessageLabelDescent = 0;

                    if (showFlagMessagesInline) {
                        Label flagMessageLabel = flagMessageLabels.get(sectionIndex).get(fieldIndex);

                        Dimensions flagMessageLabelSize = flagMessageLabel.getPreferredSize();
                        flagMessageLabel.setSize(flagMessageLabelSize);

                        flagMessageLabelAscent = flagMessageLabel.getBaseline(flagMessageLabelSize.width,
                            flagMessageLabelSize.height);
                        flagMessageLabelDescent = flagMessageLabelSize.height - flagMessageLabelAscent;
                    }

                    // Determine the baseline and row height
                    int maximumAscent = Math.max(rowHeaderAscent, Math.max(fieldAscent, flagMessageLabelAscent));
                    int maximumDescent = Math.max(rowHeaderDescent, Math.max(fieldDescent, flagMessageLabelDescent));

                    int baseline = maximumAscent;
                    int rowHeight = maximumAscent + maximumDescent;

                    // Position the row header
                    int rowHeaderX = padding.left;
                    int rowHeaderY = rowY + (baseline - rowHeaderAscent);
                    rowHeader.setLocation(rowHeaderX, rowHeaderY);

                    // Position the field
                    int fieldX = maximumRowHeaderWidth + horizontalSpacing + padding.left;
                    int fieldY = rowY + (baseline - fieldAscent);
                    field.setLocation(fieldX, fieldY);

                    // Position the message label
                    if (showFlagMessagesInline) {
                        Label flagMessageLabel = flagMessageLabels.get(sectionIndex).get(fieldIndex);

                        int flagMessageLabelX = maximumRowHeaderWidth + horizontalSpacing
                            + field.getWidth() + horizontalSpacing;
                        int flagMessageLabelY = rowY + (baseline - flagMessageLabelAscent);

                        flagMessageLabel.setLocation(flagMessageLabelX, flagMessageLabelY);
                    }

                    // Update the row y-coordinate
                    rowY += rowHeight + verticalSpacing;
                } else {
                    // Hide the row header
                    rowHeader.setVisible(false);

                    // Hide the flag message label
                    if (showFlagMessagesInline) {
                        Label flagMessageLabel = flagMessageLabels.get(sectionIndex).get(fieldIndex);
                        flagMessageLabel.setVisible(false);
                    }
                }
            }
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        Form form = (Form)getComponent();
        Form.SectionSequence sections = form.getSections();

        for (int sectionIndex = 0, sectionCount = sections.getLength();
            sectionIndex < sectionCount; sectionIndex++) {
            Form.Section section = sections.get(sectionIndex);

            for (int fieldIndex = 0, fieldCount = section.getLength();
                fieldIndex < fieldCount; fieldIndex++) {
                Component field = section.get(fieldIndex);

                Form.Flag flag = Form.getFlag(field);
                if (flag != null && showFlagHighlight) {
                    TerraTheme theme = (TerraTheme)Theme.getTheme();
                    MessageType messageType = flag.getMessageType();

                    Color highlightColor = null;

                    switch (messageType) {
                        case ERROR: {
                            highlightColor = theme.getColor(21);
                            break;
                        }

                        case WARNING: {
                            highlightColor = theme.getColor(18);
                            break;
                        }

                        case QUESTION: {
                            highlightColor = theme.getColor(15);
                            break;
                        }

                        case INFO: {
                            highlightColor = theme.getColor(9);
                            break;
                        }
                    }

                    if (highlightColor != null) {
                        Bounds fieldBounds = field.getBounds();

                        graphics.setColor(highlightColor);
                        graphics.setStroke(new BasicStroke(1));
                        graphics.drawRect(fieldBounds.x - FLAG_HIGHLIGHT_PADDING,
                            fieldBounds.y - FLAG_HIGHLIGHT_PADDING,
                            fieldBounds.width + FLAG_HIGHLIGHT_PADDING * 2 - 1,
                            fieldBounds.height + FLAG_HIGHLIGHT_PADDING * 2 - 1);
                    }
                }
            }
        }
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

    public int getHorizontalSpacing() {
        return horizontalSpacing;
    }

    public void setHorizontalSpacing(int horizontalSpacing) {
        if (horizontalSpacing < 0) {
            throw new IllegalArgumentException("horizontalSpacing is negative.");
        }

        this.horizontalSpacing = horizontalSpacing;
        invalidateComponent();
    }

    public final void setHorizontalSpacing(Number horizontalSpacing) {
        if (horizontalSpacing == null) {
            throw new IllegalArgumentException("horizontalSpacing is null.");
        }

        setHorizontalSpacing(horizontalSpacing.intValue());
    }

    public int getVerticalSpacing() {
        return verticalSpacing;
    }

    public void setVerticalSpacing(int verticalSpacing) {
        if (verticalSpacing < 0) {
            throw new IllegalArgumentException("verticalSpacing is negative.");
        }

        this.verticalSpacing = verticalSpacing;
        invalidateComponent();
    }

    public final void setVerticalSpacing(Number verticalSpacing) {
        if (verticalSpacing == null) {
            throw new IllegalArgumentException("verticalSpacing is null.");
        }

        setVerticalSpacing(verticalSpacing.intValue());
    }

    public int getFlagImageOffset() {
        return flagImageOffset;
    }

    public void setFlagImageOffset(int flagImageOffset) {
        if (flagImageOffset < 0) {
            throw new IllegalArgumentException("flagImageOffset is negative.");
        }

        this.flagImageOffset = flagImageOffset;

        // Set spacing style of existing row headers to flagImageOffset
        Form form = (Form)getComponent();
        Form.SectionSequence sections = form.getSections();

        for (int sectionIndex = 0, sectionCount = sections.getLength();
            sectionIndex < sectionCount; sectionIndex++) {
            Form.Section section = sections.get(sectionIndex);

            for (int fieldIndex = 0, fieldCount = section.getLength();
                fieldIndex < fieldCount; fieldIndex++) {
                BoxPane rowHeader = rowHeaders.get(sectionIndex).get(fieldIndex);
                rowHeader.getStyles().put("spacing", flagImageOffset);
            }
        }

        invalidateComponent();
    }

    public final void setFlagImageOffset(Number flagImageOffset) {
        if (flagImageOffset == null) {
            throw new IllegalArgumentException("flagImageOffset is null.");
        }

        setFlagImageOffset(flagImageOffset.intValue());
    }

    public boolean getFill() {
        return fill;
    }

    public void setFill(boolean fill) {
        this.fill = fill;
        invalidateComponent();
    }

    public boolean getShowFirstSectionHeading() {
        return showFirstSectionHeading;
    }

    public void setShowFirstSectionHeading(boolean showFirstSectionHeading) {
        this.showFirstSectionHeading = showFirstSectionHeading;
        invalidateComponent();
    }

    public boolean getShowFlagIcons() {
        return showFlagIcons;
    }

    public void setShowFlagIcons(boolean showFlagIcons) {
        this.showFlagIcons = showFlagIcons;

        // Set visibility of existing flag image views to false
        Form form = (Form)getComponent();
        Form.SectionSequence sections = form.getSections();

        for (int sectionIndex = 0, sectionCount = sections.getLength();
            sectionIndex < sectionCount; sectionIndex++) {
            Form.Section section = sections.get(sectionIndex);

            for (int fieldIndex = 0, fieldCount = section.getLength();
                fieldIndex < fieldCount; fieldIndex++) {
                BoxPane rowHeader = rowHeaders.get(sectionIndex).get(fieldIndex);
                ImageView flagImageView = (ImageView)rowHeader.get(0);
                flagImageView.setVisible(showFlagIcons);
            }
        }

        invalidateComponent();
    }

    public boolean getShowFlagHighlight() {
        return showFlagHighlight;
    }

    public void setShowFlagHighlight(boolean showFlagHighlight) {
        this.showFlagHighlight = showFlagHighlight;
        invalidateComponent();
    }

    public boolean getShowFlagMessagesInline() {
        return showFlagMessagesInline;
    }

    public void setShowFlagMessagesInline(boolean showFlagMessagesInline) {
        this.showFlagMessagesInline = showFlagMessagesInline;
        invalidateComponent();
    }

    public boolean getLeftAlignLabels() {
        return leftAlignLabels;
    }

    public void setLeftAlignLabels(boolean leftAlignLabels) {
        this.leftAlignLabels = leftAlignLabels;

        // Set horizontal alignment style of existing row headers to left or right
        Form form = (Form)getComponent();
        Form.SectionSequence sections = form.getSections();

        for (int sectionIndex = 0, sectionCount = sections.getLength();
            sectionIndex < sectionCount; sectionIndex++) {
            Form.Section section = sections.get(sectionIndex);

            for (int fieldIndex = 0, fieldCount = section.getLength();
                fieldIndex < fieldCount; fieldIndex++) {
                BoxPane rowHeader = rowHeaders.get(sectionIndex).get(fieldIndex);
                rowHeader.getStyles().put("horizontalAlignment", leftAlignLabels ?
                    HorizontalAlignment.LEFT : HorizontalAlignment.RIGHT);
            }
        }

        invalidateComponent();
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        if (delimiter == null) {
            throw new IllegalArgumentException("delimiter is null.");
        }

        this.delimiter = delimiter;

        Form form = (Form)getComponent();
        Form.SectionSequence sections = form.getSections();

        for (int i = 0, n = sections.getLength(); i < n; i++) {
            Form.Section section = sections.get(i);

            for (int j = 0, m = section.getLength(); j < m; j++) {
                updateFieldLabel(section, j);
            }
        }

        invalidateComponent();
    }

    // Form events
    @Override
    public void sectionInserted(Form form, int index) {
        insertSection(form.getSections().get(index), index);
    }

    @Override
    public void sectionsRemoved(Form form, int index, Sequence<Form.Section> removed) {
        removeSections(index, removed);
    }

    @Override
    public void sectionHeadingChanged(Form.Section section) {
        updateSectionHeading(section);
    }

    @Override
    public void fieldInserted(Form.Section section, int index) {
        insertField(section, section.get(index), index);
    }

    @Override
    public void fieldsRemoved(Form.Section section, int index, Sequence<Component> fields) {
        Form form = (Form)getComponent();
        removeFields(form.getSections().indexOf(section), index, fields);
    }

    // Form attribute events
    @Override
    public void labelChanged(Form form, Component field, String previousLabel) {
        Form.Section section = Form.getSection(field);
        updateFieldLabel(section, section.indexOf(field));
    }

    @Override
    public void requiredChanged(Form form, Component field) {
        // No-op
    }

    @Override
    public void flagChanged(Form form, Component field, Form.Flag previousFlag) {
        Form.Section section = Form.getSection(field);
        updateFieldFlag(section, section.indexOf(field));
    }

    private void insertSection(Form.Section section, int index) {
        Form form = (Form)getComponent();

        // Insert separator
        Separator separator = new Separator(section.getHeading());
        separators.insert(separator, index);
        form.add(separator);

        // Insert row header list
        ArrayList<BoxPane> sectionRowHeaders = new ArrayList<BoxPane>();
        rowHeaders.insert(sectionRowHeaders, index);

        // Insert flag message list
        flagMessageLabels.insert(new ArrayList<Label>(), index);

        // Insert fields
        for (int i = 0, n = section.getLength(); i < n; i++) {
            insertField(section, section.get(i), i);
        }

        invalidateComponent();
    }

    private void removeSections(int index, Sequence<Form.Section> removed) {
        Form form = (Form)getComponent();
        int count = removed.getLength();

        // Remove fields
        for (int i = 0; i < count; i++) {
            removeFields(index + i, 0, removed.get(i));
        }

        // Remove row header list
        rowHeaders.remove(index, count);

        // Remove flag message list
        flagMessageLabels.remove(index, count);

        // Remove separators
        Sequence<Separator> removedSeparators = separators.remove(index, count);
        for (int i = 0; i < count; i++) {
            form.remove(removedSeparators.get(i));
        }

        invalidateComponent();
    }

    private void insertField(Form.Section section, Component field, int index) {
        Form form = (Form)getComponent();
        int sectionIndex = form.getSections().indexOf(section);

        // Create the row header
        BoxPane rowHeader = new BoxPane();
        rowHeader.getStyles().put("horizontalAlignment", leftAlignLabels ?
            HorizontalAlignment.LEFT : HorizontalAlignment.RIGHT);
        rowHeader.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

        ImageView flagImageView = new ImageView();
        flagImageView.setPreferredWidth(flagImageWidth);
        flagImageView.setVisible(showFlagIcons);

        rowHeader.add(flagImageView);

        Label label = new Label();
        rowHeader.add(label);

        rowHeaders.get(sectionIndex).insert(rowHeader, index);
        form.add(rowHeader);

        // Add mouse listener
        field.getComponentMouseListeners().add(fieldMouseListener);

        // Add flag message label
        Label flagMessageLabel = new Label();
        flagMessageLabel.getStyles().put("padding", new Insets(3, 4, 3, 4));
        flagMessageLabel.getDecorators().add(new InlineFieldIdentifierDecorator());

        flagMessageLabels.get(sectionIndex).insert(flagMessageLabel, index);
        form.add(flagMessageLabel);

        // Update the field label and flag
        updateFieldLabel(section, index);
        updateFieldFlag(section, index);

        invalidateComponent();
    }

    private void removeFields(int sectionIndex, int index, Sequence<Component> removed) {
        Form form = (Form)getComponent();
        int count = removed.getLength();

        // Remove the row headers
        Sequence<BoxPane> removedRowHeaders = rowHeaders.get(sectionIndex).remove(index, count);

        // Remove flag message labels
        Sequence<Label> removedFlagMessageLabels = flagMessageLabels.get(sectionIndex).remove(index, count);

        for (int i = 0; i < count; i++) {
            form.remove(removedRowHeaders.get(i));
            form.remove(removedFlagMessageLabels.get(i));

            // Remove mouse listener
            Component field = removed.get(i);
            field.getComponentMouseListeners().remove(fieldMouseListener);
        }

        invalidateComponent();
    }

    private void updateSectionHeading(Form.Section section) {
        Form form = (Form)getComponent();
        int sectionIndex = form.getSections().indexOf(section);

        Separator separator = separators.get(sectionIndex);
        separator.setHeading(section.getHeading());
    }

    private void updateFieldLabel(Form.Section section, int fieldIndex) {
        Form form = (Form)getComponent();
        Component field = section.get(fieldIndex);

        int sectionIndex = form.getSections().indexOf(section);
        BoxPane rowHeader = rowHeaders.get(sectionIndex).get(fieldIndex);
        Label label = (Label)rowHeader.get(1);
        String labelText = Form.getLabel(field);
        label.setText((labelText == null) ? "" : labelText + delimiter);
    }

    private void updateFieldFlag(Form.Section section, int fieldIndex) {
        Form form = (Form)getComponent();
        Component field = section.get(fieldIndex);

        int sectionIndex = form.getSections().indexOf(section);
        BoxPane rowHeader = rowHeaders.get(sectionIndex).get(fieldIndex);
        ImageView flagImageView = (ImageView)rowHeader.get(0);

        Label flagMessageLabel = flagMessageLabels.get(sectionIndex).get(fieldIndex);

        Form.Flag flag = Form.getFlag(field);

        if (flag == null) {
            flagImageView.setImage((Image)null);

            flagMessageLabel.setText(null);
            flagMessageLabel.getStyles().put("color", 1);
            flagMessageLabel.getStyles().put("backgroundColor", null);
        } else {
            MessageType messageType = flag.getMessageType();
            TerraTheme theme = (TerraTheme)Theme.getTheme();

            Image messageIcon = null;
            Color color = null;
            Color backgroundColor = null;

            switch (messageType) {
                case ERROR: {
                    messageIcon = errorMessageIcon;
                    color = theme.getColor(4);
                    backgroundColor = theme.getColor(22);
                    break;
                }

                case WARNING: {
                    messageIcon = warningMessageIcon;
                    color = theme.getColor(1);
                    backgroundColor = theme.getColor(19);
                    break;
                }

                case QUESTION: {
                    messageIcon = questionMessageIcon;
                    color = theme.getColor(4);
                    backgroundColor = theme.getColor(16);
                    break;
                }

                case INFO: {
                    messageIcon = infoMessageIcon;
                    color = theme.getColor(1);
                    backgroundColor = theme.getColor(10);
                    break;
                }
            }

            flagImageView.setImage(messageIcon);

            flagMessageLabel.setText(flag.getMessage());
            flagMessageLabel.getStyles().put("color", color);
            flagMessageLabel.getStyles().put("backgroundColor", backgroundColor);
        }

        if (showFlagHighlight) {
            Bounds fieldBounds = field.getBounds();
            repaintComponent(fieldBounds.x - FLAG_HIGHLIGHT_PADDING,
                fieldBounds.y - FLAG_HIGHLIGHT_PADDING,
                fieldBounds.width + FLAG_HIGHLIGHT_PADDING * 2 - 1,
                fieldBounds.height + FLAG_HIGHLIGHT_PADDING * 2 - 1);
        }
    }
}
