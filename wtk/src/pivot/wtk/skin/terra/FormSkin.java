/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import pivot.collections.ArrayList;
import pivot.collections.Sequence;

import pivot.wtk.Alert;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Form;
import pivot.wtk.FormAttributeListener;
import pivot.wtk.FormListener;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.media.Image;
import pivot.wtk.skin.ContainerSkin;

public class FormSkin extends ContainerSkin
    implements FormListener, FormAttributeListener {
    private ArrayList<Label> labels = new ArrayList<Label>();
    private ArrayList<ImageView> flagImageViews = new ArrayList<ImageView>();

    private static Image informationImage = null;
    private static Image warningImage = null;
    private static Image errorImage = null;
    private static Image questionImage = null;

    private boolean rightAlignLabels = false;
    private HorizontalAlignment fieldAlignment = HorizontalAlignment.LEFT;
    private int horizontalSpacing = 12;
    private int verticalSpacing = 6;
    private int flagImageOffset = 4;

    private static final int FLAG_IMAGE_SIZE = 16;

    @Override
    public void install(Component component) {
        validateComponentType(component, Form.class);

        super.install(component);

        Form form = (Form)component;
        form.getFormListeners().add(this);
        form.getFormAttributeListeners().add(this);

        // Initialize for existing fields
        for (int i = 0, n = form.getLength(); i < n; i++) {
            updateLabel(i);
            updateFlag(i);
        }
    }

    @Override
    public void uninstall() {
        Form form = (Form)getComponent();
        form.getFormListeners().remove(this);
        form.getFormAttributeListeners().remove(this);

        // Remove all added labels and flag image views
        for (int i = 0, n = form.getLength(); i < n; i++) {
            form.remove(labels.get(i));
            form.remove(flagImageViews.get(i));
        }

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        // Preferred width is the sum of the maximum label width, maximum field
        // width, horizontal spacing, flag image offset, and flag image size
        // values
        Form form = (Form)getComponent();
        Form.FieldSequence fields = form.getFields();

        int maximumLabelWidth = 0;
        int maximumFieldWidth = 0;

        for (int i = 0, n = fields.getLength(); i < n; i++) {
            Component field = fields.get(i);

            if (field.isDisplayable()) {
                Label label = labels.get(i);
                maximumLabelWidth = Math.max(maximumLabelWidth,
                    label.getPreferredWidth(-1));
                maximumFieldWidth = Math.max(maximumFieldWidth,
                    field.getPreferredWidth(-1));
            }
        }

        preferredWidth = maximumLabelWidth + horizontalSpacing + maximumFieldWidth
            + flagImageOffset + FLAG_IMAGE_SIZE;

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        Form form = (Form)getComponent();
        Form.FieldSequence fields = form.getFields();

        int fieldWidth = -1;

        // If justified and constrained, determine field width constraint
        if (fieldAlignment == HorizontalAlignment.JUSTIFY
            && width != -1) {
            int maximumLabelWidth = 0;

            for (int i = 0, n = fields.getLength(); i < n; i++) {
                Component field = fields.get(i);

                if (field.isDisplayable()) {
                    Label label = labels.get(i);
                    maximumLabelWidth = Math.max(maximumLabelWidth,
                        label.getPreferredWidth(-1));
                }
            }

            fieldWidth = Math.max(0, width - (maximumLabelWidth
                + horizontalSpacing + flagImageOffset + FLAG_IMAGE_SIZE));
        }

        // Preferred height is the sum of the maximum value of the label,
        // field, and flag image for each row, plus vertical spacing
        for (int i = 0, n = fields.getLength(); i < n; i++) {
            Component field = fields.get(i);

            if (field.isDisplayable()) {
                Label label = labels.get(i);

                int preferredRowHeight = Math.max(label.getPreferredHeight(-1),
                    Math.max(field.getPreferredHeight(fieldWidth), FLAG_IMAGE_SIZE));
                preferredHeight += preferredRowHeight;

                if (i > 0) {
                    preferredHeight += verticalSpacing;
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
        Form.FieldSequence fields = form.getFields();

        int n = fields.getLength();

        // Determine the maximum label and field widths
        int maximumLabelWidth = 0;
        int maximumFieldWidth = 0;

        for (int i = 0; i < n; i++) {
            Component field = fields.get(i);

            if (field.isDisplayable()) {
                Label label = labels.get(i);
                maximumLabelWidth = Math.max(maximumLabelWidth,
                    label.getPreferredWidth(-1));
                maximumFieldWidth = Math.max(maximumFieldWidth,
                    field.getPreferredWidth(-1));
            }
        }

        // Determine the maximum field width
        int width = getWidth();
        int availableFieldWidth = Math.max(0, width - (maximumLabelWidth
            + horizontalSpacing + flagImageOffset + FLAG_IMAGE_SIZE));

        // Lay out the components
        int rowY = 0;

        for (int i = 0; i < n; i++) {
            Label label = labels.get(i);
            Component field = fields.get(i);
            ImageView flagImageView = flagImageViews.get(i);

            if (field.isDisplayable()) {
                // Show the row components
                label.setVisible(true);
                field.setVisible(true);
                flagImageView.setVisible(true);

                // Set the row component sizes
                label.setSize(label.getPreferredSize());

                Dimensions fieldSize = null;
                if (fieldAlignment == HorizontalAlignment.JUSTIFY) {
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

                int fieldX = 0;
                switch(fieldAlignment) {
                    case LEFT:
                    case JUSTIFY: {
                        fieldX = maximumLabelWidth + horizontalSpacing;
                        break;
                    }

                    case RIGHT: {
                        fieldX = maximumLabelWidth + horizontalSpacing
                            + Math.max(0, Math.max(availableFieldWidth, maximumFieldWidth)
                                - field.getWidth());
                        break;
                    }

                    case CENTER: {
                        fieldX = maximumLabelWidth + horizontalSpacing
                            + Math.max(0, (Math.max(availableFieldWidth, maximumFieldWidth)
                                - field.getWidth()) / 2);
                        break;
                    }
                }

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

    public boolean getRightAlignLabels() {
        return rightAlignLabels;
    }

    public void setRightAlignLabels(boolean rightAlignLabels) {
        this.rightAlignLabels = rightAlignLabels;
    }

    public HorizontalAlignment getFieldAlignment() {
        return fieldAlignment;
    }

    public void setFieldAlignment(HorizontalAlignment fieldAlignment) {
        this.fieldAlignment = fieldAlignment;
        repaintComponent();
    }

    public final void setFieldAlignment(String fieldAlignment) {
        if (fieldAlignment == null) {
            throw new IllegalArgumentException("fieldAlignment is null.");
        }

        setFieldAlignment(HorizontalAlignment.decode(fieldAlignment));
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

    // Form events
    public void fieldInserted(Form form, int index) {
        // Create the label
        Label label = new Label();
        labels.insert(label, index);
        form.add(label);
        updateLabel(index);

        // Create the image view
        ImageView flagImageView = new ImageView();
        flagImageViews.insert(flagImageView, index);
        form.add(flagImageView);
        updateFlag(index);

        invalidateComponent();
    }

    public void fieldsRemoved(Form form, int index, Sequence<Component> fields) {
        int count = fields.getLength();

        Sequence<Label> removedLabels = labels.remove(index, count);
        for (int i = 0, n = removedLabels.getLength(); i < n; i++) {
            form.remove(removedLabels.get(i));
        }

        Sequence<ImageView> removedImageViews = flagImageViews.remove(index, count);
        for (int i = 0, n = removedImageViews.getLength(); i < n; i++) {
            form.remove(removedImageViews.get(i));
        }

        invalidateComponent();
    }

    // Form attribute events
    public void labelChanged(Form form, Component component, String previousLabel) {
        updateLabel(form.getFields().indexOf(component));
    }

    public void flagChanged(Form form, Component component, Form.Flag previousFlag) {
        updateFlag(form.getFields().indexOf(component));
    }

    private void updateLabel(int index) {
        Form form = (Form)getComponent();
        Component field = form.getFields().get(index);

        Label label = labels.get(index);
        String labelText = Form.getLabel(field);
        label.setText((labelText == null) ? "" : labelText + ":");
    }

    private void updateFlag(int index) {
        Form form = (Form)getComponent();
        Component field = form.getFields().get(index);

        ImageView flagImageView = flagImageViews.get(index);
        Form.Flag flag = Form.getFlag(field);

        Image flagImage = null;
        String flagMessage = null;

        if (flag != null) {
            Alert.Type flagAlertType = flag.getAlertType();

            if (flagAlertType != null) {
                switch (flagAlertType) {
                    case INFO: {
                        if (informationImage == null) {
                            informationImage =
                                Image.load(getClass().getResource("FormSkin-Information-16x16.png"));
                        }

                        flagImage = informationImage;
                        break;
                    }

                    case WARNING: {
                        if (warningImage == null) {
                            warningImage =
                                Image.load(getClass().getResource("FormSkin-Warning-16x16.png"));
                        }

                        flagImage = warningImage;
                        break;
                    }

                    case ERROR: {
                        if (errorImage == null) {
                            errorImage =
                                Image.load(getClass().getResource("FormSkin-Error-16x16.png"));
                        }

                        flagImage = errorImage;
                        break;
                    }

                    case QUESTION: {
                        if (questionImage == null) {
                            questionImage =
                                Image.load(getClass().getResource("FormSkin-Question-16x16.png"));
                        }

                        flagImage = questionImage;
                        break;
                    }
                }

                flagMessage = flag.getMessage();
            }
        }

        flagImageView.setImage(flagImage);
        flagImageView.setTooltipText(flagMessage);
    }
}
