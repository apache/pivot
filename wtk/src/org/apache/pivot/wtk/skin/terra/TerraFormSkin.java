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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.FormAttributeListener;
import org.apache.pivot.wtk.FormListener;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Separator;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.ContainerSkin;

/**
 * Form skin.
 */
public class TerraFormSkin extends ContainerSkin
    implements FormListener, FormAttributeListener {
    private ArrayList<Separator> separators = new ArrayList<Separator>();
    private ArrayList<ArrayList<Label>> labels = new ArrayList<ArrayList<Label>>();
    private ArrayList<ArrayList<ImageView>> flagImageViews = new ArrayList<ArrayList<ImageView>>();

    private boolean rightAlignLabels = false;

    // Make the field fill the width of the form
    private boolean fill = false;
    private int horizontalSpacing = 6;
    private int verticalSpacing = 6;
    private int flagImageOffset = 4;
    private boolean showFirstSectionHeading = false;

    // Align field and label so that their baselines line up
    private boolean alignToBaseline = true;
    private String delimiter = DEFAULT_DELIMITER;

    private static final int FLAG_IMAGE_SIZE = 16;
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
        int preferredWidth = 0;

        // Preferred width is maximum of either the sum of the maximum label
        // width, maximum field width, horizontal spacing, flag image offset,
        // and flag image size values or the maximum separator width
        int maximumLabelWidth = 0;
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
                    Label label = labels.get(sectionIndex).get(fieldIndex);
                    maximumLabelWidth = Math.max(maximumLabelWidth,
                        label.getPreferredWidth(-1));
                    maximumFieldWidth = Math.max(maximumFieldWidth,
                        field.getPreferredWidth(-1));
                }
            }
        }

        preferredWidth = Math.max(maximumLabelWidth + horizontalSpacing + maximumFieldWidth
            + flagImageOffset + FLAG_IMAGE_SIZE, maximumSeparatorWidth);

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        Form form = (Form)getComponent();
        Form.SectionSequence sections = form.getSections();

        // If justified and constrained, determine field width constraint
        int fieldWidth = -1;

        if (fill
            && width != -1) {
            int maximumLabelWidth = 0;

            for (int sectionIndex = 0, sectionCount = sections.getLength();
                sectionIndex < sectionCount; sectionIndex++) {
                Form.Section section = sections.get(sectionIndex);

                for (int fieldIndex = 0, fieldCount = section.getLength();
                    fieldIndex < fieldCount; fieldIndex++) {
                    Component field = section.get(fieldIndex);

                    if (field.isVisible()) {
                        Label label = labels.get(sectionIndex).get(fieldIndex);
                        maximumLabelWidth = Math.max(maximumLabelWidth,
                            label.getPreferredWidth(-1));
                    }
                }
            }

            fieldWidth = Math.max(0, width - (maximumLabelWidth
                + horizontalSpacing + flagImageOffset + FLAG_IMAGE_SIZE));
        }

        // Preferred height is the sum of the maximum value of the label,
        // field, and flag image for each row, plus vertical spacing and
        // preferred separator heights
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
                    Label label = labels.get(sectionIndex).get(fieldIndex);
                    int preferredRowHeight;
                    if (alignToBaseline) {
                        int labelBaseLine = label.getBaseline(-1);
                        int fieldBaseLine = field.getBaseline(-1);
                        if (labelBaseLine != -1 && fieldBaseLine != -1) {
                            int labelPreferredHeight = label.getPreferredHeight(-1);
                            int fieldPreferredHeight = field.getPreferredHeight(fieldWidth);
                            int baseline = Math.max(labelBaseLine, fieldBaseLine);
                            int belowBaseline = Math.max(fieldPreferredHeight - fieldBaseLine,
                                labelPreferredHeight - labelBaseLine);
                            preferredRowHeight = baseline + belowBaseline;
                        } else {
                            // if they don't both have baselines, default to
                            // non-baseline behaviour
                            preferredRowHeight = Math.max(label.getPreferredHeight(-1),
                                field.getPreferredHeight(fieldWidth));
                        }
                    } else {
                        preferredRowHeight = Math.max(label.getPreferredHeight(-1),
                            field.getPreferredHeight(fieldWidth));
                    }
                    preferredRowHeight = Math.max(preferredRowHeight, FLAG_IMAGE_SIZE);
                    preferredHeight += preferredRowHeight;

                    if (fieldIndex > 0) {
                        preferredHeight += verticalSpacing;
                    }
                }
            }
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        // TODO Optimize
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    @Override
    public void layout() {
        Form form = (Form)getComponent();
        Form.SectionSequence sections = form.getSections();

        // Determine the maximum label and field widths
        int maximumLabelWidth = 0;
        int maximumFieldWidth = 0;

        for (int sectionIndex = 0, sectionCount = sections.getLength();
            sectionIndex < sectionCount; sectionIndex++) {
            Form.Section section = sections.get(sectionIndex);

            for (int fieldIndex = 0, fieldCount = section.getLength();
                fieldIndex < fieldCount; fieldIndex++) {
                Component field = section.get(fieldIndex);

                if (field.isVisible()) {
                    Label label = labels.get(sectionIndex).get(fieldIndex);
                    maximumLabelWidth = Math.max(maximumLabelWidth,
                        label.getPreferredWidth(-1));
                    maximumFieldWidth = Math.max(maximumFieldWidth,
                        field.getPreferredWidth(-1));
                }
            }
        }

        // Determine the maximum field width
        int width = getWidth();
        int availableFieldWidth = Math.max(0, width - (maximumLabelWidth
            + horizontalSpacing + flagImageOffset + FLAG_IMAGE_SIZE));

        // Lay out the components
        int rowY = 0;

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
                separator.setLocation(0, rowY);
                rowY += separator.getHeight();
            }

            for (int fieldIndex = 0, fieldCount = section.getLength();
                fieldIndex < fieldCount; fieldIndex++) {
                Component field = section.get(fieldIndex);

                Label label = labels.get(sectionIndex).get(fieldIndex);
                ImageView flagImageView = flagImageViews.get(sectionIndex).get(fieldIndex);

                if (field.isVisible()) {
                    // Show the row components
                    label.setVisible(true);
                    flagImageView.setVisible(true);

                    // Set the row component sizes
                    label.setSize(label.getPreferredSize());

                    Dimensions fieldSize = null;
                    if (fill) {
                        fieldSize = new Dimensions(availableFieldWidth,
                            field.getPreferredHeight(availableFieldWidth));
                    } else {
                        fieldSize = field.getPreferredSize();
                    }

                    field.setSize(fieldSize);
                    flagImageView.setSize(flagImageView.getPreferredSize());

                    int rowHeight;
                    int fieldY;
                    int labelY;
                    int flagImageY;
                    if (alignToBaseline) {
                        int labelBaseLine = label.getBaseline(label.getWidth());
                        int fieldBaseLine = field.getBaseline(fieldSize.width);

                        if (labelBaseLine != -1 && fieldBaseLine != -1) {
                            int baseline = Math.max(labelBaseLine, fieldBaseLine);
                            int belowBaseline = Math.max(fieldSize.height - fieldBaseLine,
                                label.getHeight() - labelBaseLine);
                            rowHeight = baseline + belowBaseline;
                            labelY = rowY + (baseline - labelBaseLine);
                            fieldY = rowY + (baseline - fieldBaseLine);
                        } else {
                            // if they don't both have baselines, default to
                            // non-baseline behaviour
                            rowHeight = Math.max(label.getHeight(), Math.max(field.getHeight(),
                                FLAG_IMAGE_SIZE));
                            fieldY = rowY;
                            labelY = rowY;
                        }

                        // Vertically center the flag on the row
                        flagImageY = rowY + (rowHeight - flagImageView.getHeight()) / 2;
                    } else {
                        rowHeight = Math.max(label.getHeight(), Math.max(field.getHeight(),
                            FLAG_IMAGE_SIZE));
                        fieldY = rowY;
                        labelY = rowY;
                        flagImageY = rowY + (rowHeight - flagImageView.getHeight()) / 2;
                    }

                    // Set the row component locations
                    int labelX = rightAlignLabels ? maximumLabelWidth - label.getWidth() : 0;
                    label.setLocation(labelX, labelY);

                    int fieldX = maximumLabelWidth + horizontalSpacing;

                    field.setLocation(fieldX, fieldY);
                    flagImageView.setLocation(fieldX + field.getWidth() + flagImageOffset, flagImageY);

                    // Update the row y-coordinate
                    rowY += rowHeight + verticalSpacing;
                } else {
                    // Hide the row components
                    label.setVisible(false);
                    flagImageView.setVisible(false);
                }
            }
        }
    }

    public boolean getRightAlignLabels() {
        return rightAlignLabels;
    }

    public void setRightAlignLabels(boolean rightAlignLabels) {
        this.rightAlignLabels = rightAlignLabels;
        invalidateComponent();
    }

    public boolean getFill() {
        return fill;
    }

    public void setFill(boolean fill) {
        this.fill = fill;
        invalidateComponent();
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
        this.flagImageOffset = flagImageOffset;
        invalidateComponent();
    }

    public final void setFlagImageOffset(Number flagImageOffset) {
        if (flagImageOffset == null) {
            throw new IllegalArgumentException("flagImageOffset is null.");
        }

        setFlagImageOffset(flagImageOffset.intValue());
    }

    public boolean isShowFirstSectionHeading() {
        return showFirstSectionHeading;
    }

    public void setShowFirstSectionHeading(boolean showFirstSectionHeading) {
        this.showFirstSectionHeading = showFirstSectionHeading;
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

    public boolean getAlignToBaseline() {
        return alignToBaseline;
    }

    public void setAlignToBaseline(boolean alignToBaseline) {
        this.alignToBaseline = alignToBaseline;
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
        removeFields(section, index, fields.getLength());
    }

    // Form attribute events
    @Override
    public void labelChanged(Form form, Component field, String previousLabel) {
        Form.Section section = Form.getSection(field);
        updateFieldLabel(section, section.indexOf(field));
    }

    @Override
    public void flagChanged(Form form, Component field, Form.Flag previousFlag) {
        Form.Section section = Form.getSection(field);
        updateFieldFlag(section, section.indexOf(field));
    }

    // Implementation methods
    private void insertSection(Form.Section section, int index) {
        Form form = (Form)getComponent();

        // Insert separator
        Separator separator = new Separator(section.getHeading());
        separators.insert(separator, index);
        form.add(separator);

        // Insert field label and flag image view lists
        ArrayList<Label> sectionLabels = new ArrayList<Label>();
        labels.insert(sectionLabels, index);

        ArrayList<ImageView> sectionFlagImageViews = new ArrayList<ImageView>();
        flagImageViews.insert(sectionFlagImageViews, index);

        // Insert fields
        for (int i = 0, n = section.getLength(); i < n; i++) {
            insertField(section, section.get(i), i);
        }

        invalidateComponent();
    }

    private void removeSections(int index, Sequence<Form.Section> removed) {
        Form form = (Form)getComponent();

        for (int i = 0, n = removed.getLength(); i < n; i++) {
            // Remove fields
            Form.Section section = removed.get(i);
            for (int j = 0; j < n; j++) {
                removeFields(section, 0, section.getLength());
            }

            // Remove field label and flag image view lists
            labels.remove(index, n);
            flagImageViews.remove(index, n);

            // Remove separators
            Sequence<Separator> removedSeparators = separators.remove(index, n);
            for (int j = 0; j < n; j++) {
                form.remove(removedSeparators.get(j));
            }
        }

        invalidateComponent();
    }

    private void insertField(Form.Section section, Component field, int index) {
        Form form = (Form)getComponent();
        int sectionIndex = form.getSections().indexOf(section);

        // Create the label
        Label label = new Label();
        labels.get(sectionIndex).insert(label, index);
        form.add(label);
        updateFieldLabel(section, index);

        // Create the flag image view
        ImageView flagImageView = new ImageView();
        flagImageViews.get(sectionIndex).insert(flagImageView, index);
        form.add(flagImageView);
        updateFieldFlag(section, index);

        invalidateComponent();
    }

    private void removeFields(Form.Section section, int index, int count) {
        Form form = (Form)getComponent();
        int sectionIndex = form.getSections().indexOf(section);

        // Remove the labels
        Sequence<Label> removedLabels = labels.get(sectionIndex).remove(index, count);
        for (int i = 0; i < count; i++) {
            form.remove(removedLabels.get(i));
        }

        // Remove the flag image views
        Sequence<ImageView> removedFlagImageViews = flagImageViews.get(sectionIndex).remove(index, count);
        for (int i = 0; i < count; i++) {
            form.remove(removedFlagImageViews.get(i));
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
        Label label = labels.get(sectionIndex).get(fieldIndex);
        String labelText = Form.getLabel(field);
        label.setText((labelText == null) ? "" : labelText + delimiter);
    }

    private void updateFieldFlag(Form.Section section, int fieldIndex) {
        Form form = (Form)getComponent();
        Component field = section.get(fieldIndex);

        int sectionIndex = form.getSections().indexOf(section);
        ImageView flagImageView = flagImageViews.get(sectionIndex).get(fieldIndex);
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
        flagImageView.setTooltipText(flagMessage);
    }
}
