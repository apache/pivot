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

import java.awt.Graphics2D;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.FormAttributeListener;
import org.apache.pivot.wtk.FormListener;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Separator;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.ContainerSkin;

/**
 * Terra form skin.
 */
public class TerraFormSkin extends ContainerSkin
    implements FormListener, FormAttributeListener {
    private ArrayList<Separator> separators = new ArrayList<Separator>();
    private ArrayList<ArrayList<BoxPane>> rowHeaders = new ArrayList<ArrayList<BoxPane>>();
    // TODO
    // private ArrayList<ArrayList<Label>> flagMessages = new ArrayList<ArrayList<Label>>();

    private int horizontalSpacing = 4;
    private int verticalSpacing = 4;
    private int flagImageOffset = 4;
    private boolean showFirstSectionHeading = false;
    private boolean showFlagMessagesInline = false;
    private boolean fill = false;
    private boolean leftAlignLabels = false;
    private String delimiter = DEFAULT_DELIMITER;

    private static final int FLAG_IMAGE_SIZE = 16;
    private static final int FLAG_HIGHLIGHT_PADDING = 2;

    private static final String DEFAULT_DELIMITER = ":";

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
        // TODO Respect showFlagMessagesInline

        int preferredWidth = 0;

        int maximumRowHeaderWidth = 0;
        int maximumFieldWidth = 0;
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
                }
            }
        }

        preferredWidth = Math.max(maximumRowHeaderWidth + horizontalSpacing + maximumFieldWidth
            + FLAG_HIGHLIGHT_PADDING * 2, maximumSeparatorWidth);

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        // TODO Respect showFlagMessagesInline

        int preferredHeight = 0;

        Form form = (Form)getComponent();
        Form.SectionSequence sections = form.getSections();

        // If justified and constrained, determine field width constraint
        int fieldWidth = -1;

        if (fill && width != -1) {
            int maximumRowHeaderWidth = 0;

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
                    }
                }
            }

            fieldWidth = Math.max(0, width - (maximumRowHeaderWidth + horizontalSpacing
                + FLAG_HIGHLIGHT_PADDING * 2));
        }

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
                    BoxPane rowHeader = rowHeaders.get(sectionIndex).get(fieldIndex);

                    // Determine the row header size and baseline
                    Dimensions rowHeaderSize = rowHeader.getPreferredSize();
                    int rowHeaderAscent = rowHeader.getBaseline(rowHeaderSize.width,
                        rowHeaderSize.height);
                    if (rowHeaderAscent == -1) {
                        rowHeaderAscent = rowHeaderSize.height;
                    }

                    int rowHeaderDescent = rowHeaderSize.height - rowHeaderAscent;

                    // Determine the field size and baseline
                    Dimensions fieldSize;
                    if (fill
                        && fieldWidth != -1) {
                        fieldSize = new Dimensions(fieldWidth, field.getPreferredHeight(fieldWidth));
                    } else {
                        fieldSize = field.getPreferredSize();
                    }

                    int fieldAscent = field.getBaseline(fieldSize.width, fieldSize.height);
                    if (fieldAscent == -1) {
                        fieldAscent = fieldSize.height;
                    }

                    int fieldDescent = fieldSize.height - fieldAscent;

                    // Determine the baseline and row height
                    int baseline = Math.max(rowHeaderAscent, fieldAscent);
                    int rowHeight = baseline + Math.max(rowHeaderDescent, fieldDescent);

                    preferredHeight += rowHeight;

                    if (fieldIndex > 0) {
                        preferredHeight += verticalSpacing;
                    }
                }
            }
        }

        preferredHeight += FLAG_HIGHLIGHT_PADDING * 2;

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        // TODO Optimize
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    @Override
    public int getBaseline(int width, int height) {
        // TODO Respect showFlagMessagesInline

        Form form = (Form)getComponent();
        Form.SectionSequence sections = form.getSections();

        // Determine the field width
        int fieldWidth = -1;

        if (fill) {
            int maximumRowHeaderWidth = 0;

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
                    }
                }
            }

            fieldWidth = Math.max(0, width - (maximumRowHeaderWidth + horizontalSpacing
                + FLAG_HIGHLIGHT_PADDING * 2));
        }

        int baseline = -1;

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
                    int rowHeaderAscent = rowHeader.getBaseline(rowHeaderSize.width,
                        rowHeaderSize.height);
                    if (rowHeaderAscent == -1) {
                        rowHeaderAscent = rowHeaderSize.height;
                    }

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

                    // Determine the baseline
                    baseline = rowY + Math.max(rowHeaderAscent, fieldAscent);
                }

                fieldIndex++;
            }

            sectionIndex++;
        }

        baseline += FLAG_HIGHLIGHT_PADDING;

        return baseline;
    }

    @Override
    public void layout() {
        // TODO Respect showFlagMessagesInline

        Form form = (Form)getComponent();
        Form.SectionSequence sections = form.getSections();

        // Determine the maximum row header width
        int maximumRowHeaderWidth = 0;

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
                }
            }
        }

        // Determine the field width
        int width = getWidth();
        int fieldWidth = Math.max(0, width - (maximumRowHeaderWidth + horizontalSpacing
            + FLAG_HIGHLIGHT_PADDING * 2));

        // Lay out the components
        int rowY = FLAG_HIGHLIGHT_PADDING;

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
                separator.setLocation(FLAG_HIGHLIGHT_PADDING, rowY);
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
                    int rowHeaderAscent = rowHeader.getBaseline(rowHeaderSize.width,
                        rowHeaderSize.height);
                    if (rowHeaderAscent == -1) {
                        rowHeaderAscent = rowHeaderSize.height;
                    }

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
                        fieldAscent = fieldSize.height;
                    }

                    int fieldDescent = fieldSize.height - fieldAscent;

                    // Determine the baseline and row height
                    int baseline = Math.max(rowHeaderAscent, fieldAscent);
                    int rowHeight = baseline + Math.max(rowHeaderDescent, fieldDescent);

                    // Position the row header
                    int rowHeaderX = FLAG_HIGHLIGHT_PADDING;
                    int rowHeaderY = rowY + (baseline - rowHeaderAscent);
                    rowHeader.setLocation(rowHeaderX, rowHeaderY);

                    // Position the field
                    int fieldX = FLAG_HIGHLIGHT_PADDING + maximumRowHeaderWidth + horizontalSpacing;
                    int fieldY = rowY + (baseline - fieldAscent);
                    field.setLocation(fieldX, fieldY);

                    // Update the row y-coordinate
                    rowY += rowHeight + verticalSpacing;
                } else {
                    // Hide the row header
                    rowHeader.setVisible(false);
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
                // TODO
                // Component field = section.get(fieldIndex);
            }
        }
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

        // TODO Set spacing style of all existing row headers to flagImageOffset

        invalidateComponent();
    }

    public final void setFlagImageOffset(Number flagImageOffset) {
        if (flagImageOffset == null) {
            throw new IllegalArgumentException("flagImageOffset is null.");
        }

        setFlagImageOffset(flagImageOffset.intValue());
    }

    public boolean getShowFirstSectionHeading() {
        return showFirstSectionHeading;
    }

    public void setShowFirstSectionHeading(boolean showFirstSectionHeading) {
        this.showFirstSectionHeading = showFirstSectionHeading;
        invalidateComponent();
    }

    public boolean getShowFlagMessagesInline() {
        return showFlagMessagesInline;
    }

    public void setShowFlagMessagesInline(boolean showFlagMessagesInline) {
        this.showFlagMessagesInline = showFlagMessagesInline;

        // TODO?

        invalidateComponent();
    }

    public boolean getFill() {
        return fill;
    }

    public void setFill(boolean fill) {
        this.fill = fill;
        invalidateComponent();
    }

    public boolean getLeftAlignLabels() {
        return leftAlignLabels;
    }

    public void setLeftAlignLabels(boolean leftAlignLabels) {
        this.leftAlignLabels = leftAlignLabels;

        // TODO Set horizontal alignment style of all existing row headers to left or right

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

        // TODO Insert flag message list, if inline

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

        // TODO Remove flag message list, if inline

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
        flagImageView.setPreferredSize(FLAG_IMAGE_SIZE, FLAG_IMAGE_SIZE);
        rowHeader.add(flagImageView);

        Label label = new Label();
        rowHeader.add(label);

        rowHeaders.get(sectionIndex).insert(rowHeader, index);
        form.add(rowHeader);

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
        for (int i = 0; i < count; i++) {
            form.remove(removedRowHeaders.get(i));
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
        Form.Flag flag = Form.getFlag(field);

        Image flagImage = null;
        String flagMessage = null;

        if (flag != null) {
            TerraTheme theme = (TerraTheme) Theme.getTheme();
            MessageType flagMessageType = flag.getMessageType();
            flagImage = theme.getSmallMessageIcon(flagMessageType);
            flagMessage = flag.getMessage();
        }

        flagImageView.setImage(flagImage);
        field.setTooltipText(flagMessage);
    }
}
