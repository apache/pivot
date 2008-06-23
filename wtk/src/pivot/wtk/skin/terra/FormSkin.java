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
import pivot.wtk.Container;
import pivot.wtk.Dimensions;
import pivot.wtk.Form;
import pivot.wtk.FormListener;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.media.Image;
import pivot.wtk.skin.ContainerSkin;

/**
 * TODO Use a white flag for INFO (currently it is green).
 *
 * @author gbrown
 */
public class FormSkin extends ContainerSkin implements FormListener {
    private ArrayList<Label> labels = new ArrayList<Label>();
    private ArrayList<ImageView> flagImageViews = new ArrayList<ImageView>();

    private static Image informationImage = Image.load(FormSkin.class.getResource("FormSkin-Information-16x16.png"));
    private static Image warningImage = Image.load(FormSkin.class.getResource("FormSkin-Warning-16x16.png"));
    private static Image errorImage = Image.load(FormSkin.class.getResource("FormSkin-Error-16x16.png"));
    private static Image questionImage = Image.load(FormSkin.class.getResource("FormSkin-Question-16x16.png"));

    private static final int FLAG_IMAGE_SIZE = 16;

    // Style properties
    protected boolean rightAlignLabels = DEFAULT_RIGHT_ALIGN_LABELS;
    protected HorizontalAlignment fieldAlignment = DEFAULT_FIELD_ALIGNMENT;
    protected int horizontalSpacing = DEFAULT_HORIZONTAL_SPACING;
    protected int verticalSpacing = DEFAULT_VERTICAL_SPACING;
    protected int flagImageOffset = DEFAULT_FLAG_IMAGE_OFFSET;

    // Default style values
    private static final boolean DEFAULT_RIGHT_ALIGN_LABELS = false;
    private static final HorizontalAlignment DEFAULT_FIELD_ALIGNMENT = HorizontalAlignment.LEFT;
    private static final int DEFAULT_HORIZONTAL_SPACING = 12;
    private static final int DEFAULT_VERTICAL_SPACING = 6;
    private static final int DEFAULT_FLAG_IMAGE_OFFSET = 4;

    // Style keys
    protected static final String RIGHT_ALIGN_LABELS_KEY = "rightAlignLabels";
    protected static final String FIELD_ALIGNMENT_KEY = "fieldAlignment";
    protected static final String HORIZONTAL_SPACING_KEY = "horizontalSpacing";
    protected static final String VERTICAL_SPACING_KEY = "verticalSpacing";
    protected static final String FLAG_IMAGE_OFFSET_KEY = "flagImageOffset";

    public FormSkin() {
        super();
    }

    @Override
    public void install(Component component) {
        validateComponentType(component, Form.class);

        super.install(component);

        Form form = (Form)component;
        form.getFormListeners().add(this);

        // Initialize for existing fields
        for (int i = 0, n = form.getComponents().getLength(); i < n; i++) {
            updateLabel(i);
            updateFlag(i);
        }
    }

    @Override
    public void uninstall() {
        Form form = (Form)getComponent();
        form.getFormListeners().remove(this);

        // Remove all added labels and flag image views
        for (int i = 0, n = form.getComponents().getLength(); i < n; i++) {
            form.getComponents().remove(labels.get(i));
            form.getComponents().remove(flagImageViews.get(i));
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
                    Math.max(field.getHeight(), flagImageView.getHeight()));

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

    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        if (key.equals(RIGHT_ALIGN_LABELS_KEY)) {
            value = rightAlignLabels;
        } else if (key.equals(FIELD_ALIGNMENT_KEY)) {
            value = fieldAlignment;
        } else if (key.equals(HORIZONTAL_SPACING_KEY)) {
            value = horizontalSpacing;
        } else if (key.equals(VERTICAL_SPACING_KEY)) {
            value = verticalSpacing;
        } else if (key.equals(FLAG_IMAGE_OFFSET_KEY)) {
            value = flagImageOffset;
        } else {
            value = super.get(key);
        }

        return value;
    }

    public Object put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(RIGHT_ALIGN_LABELS_KEY)) {
            if (value instanceof String) {
                value = Boolean.parseBoolean((String)value);
            }

            validatePropertyType(key, value, Boolean.class, true);

            previousValue = rightAlignLabels;
            rightAlignLabels = (Boolean)value;

            repaintComponent();
        } else if (key.equals(FIELD_ALIGNMENT_KEY)) {
            if (value instanceof String) {
                value = HorizontalAlignment.decode((String)value);
            }

            validatePropertyType(key, value, HorizontalAlignment.class, false);

            previousValue = fieldAlignment;
            fieldAlignment = (HorizontalAlignment)value;

            invalidateComponent();
        } else if (key.equals(HORIZONTAL_SPACING_KEY)) {
            if (value instanceof String) {
                value = Integer.parseInt((String)value);
            } else if (value instanceof Number) {
                value = ((Number)value).intValue();
            }

            validatePropertyType(key, value, Integer.class, true);

            previousValue = horizontalSpacing;
            horizontalSpacing = (Integer)value;

            invalidateComponent();
        } else if (key.equals(VERTICAL_SPACING_KEY)) {
            if (value instanceof String) {
                value = Integer.parseInt((String)value);
            } else if (value instanceof Number) {
                value = ((Number)value).intValue();
            }

            validatePropertyType(key, value, Integer.class, true);

            previousValue = verticalSpacing;
            verticalSpacing = (Integer)value;

            invalidateComponent();
        } else if (key.equals(FLAG_IMAGE_OFFSET_KEY)) {
            if (value instanceof String) {
                value = Integer.parseInt((String)value);
            } else if (value instanceof Number) {
                value = ((Number)value).intValue();
            }

            validatePropertyType(key, value, Integer.class, true);

            previousValue = flagImageOffset;
            flagImageOffset = (Integer)value;

            invalidateComponent();
        } else {
            super.put(key, value);
        }

        return previousValue;
    }

    public Object remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(RIGHT_ALIGN_LABELS_KEY)) {
            previousValue = put(key, DEFAULT_RIGHT_ALIGN_LABELS);
        } else if (key.equals(FIELD_ALIGNMENT_KEY)) {
            previousValue  = put(key, DEFAULT_FIELD_ALIGNMENT);
        } else if (key.equals(HORIZONTAL_SPACING_KEY)) {
            previousValue = put(key, HORIZONTAL_SPACING_KEY);
        } else if (key.equals(VERTICAL_SPACING_KEY)) {
            previousValue = put(key, VERTICAL_SPACING_KEY);
        } else if (key.equals(FLAG_IMAGE_OFFSET_KEY)) {
            previousValue = put(key, FLAG_IMAGE_OFFSET_KEY);
        } else {
            previousValue = super.remove(key);
        }

        return previousValue;
    }

    public boolean containsKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        return (key.equals(RIGHT_ALIGN_LABELS_KEY)
            || key.equals(FIELD_ALIGNMENT_KEY)
            || key.equals(HORIZONTAL_SPACING_KEY)
            || key.equals(VERTICAL_SPACING_KEY)
            || key.equals(FLAG_IMAGE_OFFSET_KEY)
            || super.containsKey(key));
    }

    public boolean isEmpty() {
        return false;
    }

    // Form events
    public void fieldInserted(Form form, int index) {
        // Create the label
        Label label = new Label();
        labels.insert(label, index);
        form.getComponents().add(label);
        updateLabel(index);

        // Create the image view
        ImageView flagImageView = new ImageView();
        flagImageViews.insert(flagImageView, index);
        form.getComponents().add(flagImageView);
        updateFlag(index);

        invalidateComponent();
    }

    public void fieldsRemoved(Form form, int index, Sequence<Component> fields) {
        int count = fields.getLength();

        Sequence<Label> removedLabels = labels.remove(index, count);
        for (int i = 0, n = removedLabels.getLength(); i < n; i++) {
            form.getComponents().remove(removedLabels.get(i));
        }

        Sequence<ImageView> removedImageViews = flagImageViews.remove(index, count);
        for (int i = 0, n = removedImageViews.getLength(); i < n; i++) {
            form.getComponents().remove(removedImageViews.get(i));
        }

        invalidateComponent();
    }

    // Component attribute events
    public void attributeAdded(Component component, Container.Attribute attribute) {
        super.attributeAdded(component, attribute);
        updateField(component, attribute);
    }

    public void attributeUpdated(Component component, Container.Attribute attribute,
        Object previousValue) {
        super.attributeUpdated(component, attribute, previousValue);
        updateField(component, attribute);
    }

    public void attributeRemoved(Component component, Container.Attribute attribute,
        Object value) {
        super.attributeRemoved(component, attribute, value);
        updateField(component, attribute);
    }

    private void updateField(Component component, Container.Attribute attribute) {
        Form form = (Form)getComponent();
        int index = form.getFields().indexOf(component);

        if (index != -1) {
            if (attribute == Form.LABEL_ATTRIBUTE) {
                updateLabel(index);
            } else if (attribute == Form.FLAG_ATTRIBUTE) {
                updateFlag(index);
            } else {
                // No-op
            }
        }
    }

    private void updateLabel(int index) {
        Form form = (Form)getComponent();
        Component field = form.getFields().get(index);

        Label label = labels.get(index);
        String labelText = (String)field.getAttributes().get(Form.LABEL_ATTRIBUTE);
        label.setText((labelText == null) ? "" : labelText + ":");
    }

    private void updateFlag(int index) {
        Form form = (Form)getComponent();
        Component field = form.getFields().get(index);

        ImageView flagImageView = flagImageViews.get(index);
        Form.Flag flag = (Form.Flag)field.getAttributes().get(Form.FLAG_ATTRIBUTE);

        Image flagImage = null;
        String flagMessage = null;

        if (flag != null) {
            Alert.Type flagAlertType = flag.getAlertType();

            if (flagAlertType != null) {
                switch (flagAlertType) {
                    case INFO: {
                        flagImage = informationImage;
                        break;
                    }

                    case WARNING: {
                        flagImage = warningImage;
                        break;
                    }

                    case ERROR: {
                        flagImage = errorImage;
                        break;
                    }

                    case QUESTION: {
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
