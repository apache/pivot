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
 *
 * @author gbrown
 */
public class TerraFormSkin extends ContainerSkin
    implements FormListener, FormAttributeListener {
    private ArrayList<Separator> separators = new ArrayList<Separator>();
    private ArrayList<ArrayList<Label>> labels = new ArrayList<ArrayList<Label>>();
    private ArrayList<ArrayList<ImageView>> flagImageViews = new ArrayList<ArrayList<ImageView>>();

    private boolean rightAlignLabels = false;
    private boolean fill = false;
    private int horizontalSpacing = 6;
    private int verticalSpacing = 6;
    private int flagImageOffset = 4;
    private boolean showFirstSectionHeading = false;

    private static final int FLAG_IMAGE_SIZE = 16;

    @Override
    public void install(Component component) {
        super.install(component);

        Form form = (Form)component;
        form.getFormListeners().add(this);
        form.getFormAttributeListeners().add(this);

        Form.SectionSequence sections = form.getSections();
        for (int i = 0, n = sections.getLength(); i < n; i++) {
            insertSection(sections.get(i), i);
        }
    }

    @Override
    public void uninstall() {
        Form form = (Form)getComponent();
        form.getFormListeners().remove(this);
        form.getFormAttributeListeners().remove(this);

        removeSections(0, form.getSections());

        super.uninstall();
    }

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

                if (field.isDisplayable()) {
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

                    if (field.isDisplayable()) {
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

                if (field.isDisplayable()) {
                    Label label = labels.get(sectionIndex).get(fieldIndex);

                    int preferredRowHeight = Math.max(label.getPreferredHeight(-1),
                        Math.max(field.getPreferredHeight(fieldWidth), FLAG_IMAGE_SIZE));
                    preferredHeight += preferredRowHeight;

                    if (fieldIndex > 0) {
                        preferredHeight += verticalSpacing;
                    }
                }
            }
        }

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        // TODO Optimize
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

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

                if (field.isDisplayable()) {
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

                if (field.isDisplayable()) {
                    // Show the row components
                    label.setVisible(true);
                    field.setVisible(true);
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

                    int rowHeight = Math.max(label.getHeight(),
                        Math.max(field.getHeight(), FLAG_IMAGE_SIZE));

                    // Set the row component locations
                    int labelX = rightAlignLabels ? maximumLabelWidth - label.getWidth() : 0;
                    label.setLocation(labelX, rowY);

                    int fieldX = maximumLabelWidth + horizontalSpacing;

                    field.setLocation(fieldX, rowY);
                    flagImageView.setLocation(fieldX + field.getWidth() + flagImageOffset,
                        rowY + (rowHeight - flagImageView.getHeight()) / 2);

                    // Update the row y-coordinate
                    rowY += rowHeight + verticalSpacing;
                } else {
                    // Hide the row components
                    label.setVisible(false);
                    field.setVisible(false);
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

    // Form events
    public void sectionInserted(Form form, int index) {
        insertSection(form.getSections().get(index), index);
    }

    public void sectionsRemoved(Form form, int index, Sequence<Form.Section> removed) {
        removeSections(index, removed);
    }

    public void sectionHeadingChanged(Form.Section section) {
        updateSectionHeading(section);
    }

    public void fieldInserted(Form.Section section, int index) {
        insertField(section, section.get(index), index);
    }

    public void fieldsRemoved(Form.Section section, int index, Sequence<Component> fields) {
        removeFields(section, index, fields.getLength());
    }

    // Form attribute events
    public void nameChanged(Form form, Component field, String previousName) {
        Form.Section section = Form.getSection(field);
        updateFieldName(section, section.indexOf(field));
    }

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
        updateFieldName(section, index);

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

    private void updateFieldName(Form.Section section, int fieldIndex) {
        Form form = (Form)getComponent();
        Component field = section.get(fieldIndex);

        int sectionIndex = form.getSections().indexOf(section);
        Label label = labels.get(sectionIndex).get(fieldIndex);
        String name = Form.getName(field);
        label.setText((name == null) ? "" : name + ":");
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
            TerraTheme theme = (TerraTheme)Theme.getTheme();
            MessageType flagMessageType = flag.getMessageType();
            flagImage = theme.getSmallMessageIcon(flagMessageType);
            flagMessage = flag.getMessage();
        }

        flagImageView.setImage(flagImage);
        flagImageView.setTooltipText(flagMessage);
    }
}
