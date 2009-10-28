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
package org.apache.pivot.tools.wtk;

import java.util.Comparator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.EnumList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonStateListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentListener;
import org.apache.pivot.wtk.ComponentStateListener;
import org.apache.pivot.wtk.CornerRadii;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.FlowPane;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Limits;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.skin.ContainerSkin;
import org.apache.pivot.wtk.text.validation.IntValidator;
import org.apache.pivot.wtk.text.validation.FloatValidator;

class ComponentStyleInspectorSkin extends ContainerSkin implements ComponentInspectorListener {
    private static class StyleKeyComparator implements Comparator<String> {
        @Override
        public int compare(String styleKey1, String styleKey2) {
            return styleKey1.compareTo(styleKey2);
        }
    }

    private ComponentListener componentHandler = new ComponentListener.Adapter() {
        @Override
        public void styleUpdated(Component component, String styleKey, Object previousValue) {
            Class<?> styleType = styles.getType(styleKey);

            if (styleType == Boolean.TYPE) {
                updateBooleanControl(styleKey);
            } else if (styleType == Integer.TYPE) {
                updateIntControl(styleKey);
            } else if (styleType == Float.TYPE) {
                updateFloatControl(styleKey);
            } else if (styleType == String.class) {
                updateStringControl(styleKey);
            } else if (styleType.isEnum()) {
                updateEnumControl(styleKey);
            } else if (styleType == Point.class) {
                updatePointControl(styleKey);
            } else if (styleType == Dimensions.class) {
                updateDimensionsControl(styleKey);
            } else if (styleType == Limits.class) {
                updateLimitsControl(styleKey);
            }
        }
    };

    private Form form = new Form();
    private Form.Section formSection = new Form.Section();

    private Component.StyleDictionary styles = null;

    private HashMap<String, Component> inspectorComponents = new HashMap<String, Component>();

    private static StyleKeyComparator styleKeyComparator = new StyleKeyComparator();

    public ComponentStyleInspectorSkin() {
        form.getStyles().put("rightAlignLabels", true);
        form.getSections().add(formSection);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        ComponentStyleInspector componentStyleInspector = (ComponentStyleInspector)component;

        componentStyleInspector.getComponentInspectorListeners().add(this);
        componentStyleInspector.add(form);

        sourceChanged(componentStyleInspector, null);
    }

    @Override
    public int getPreferredWidth(int height) {
        return form.getPreferredWidth(height);
    }

    @Override
    public int getPreferredHeight(int width) {
        return form.getPreferredHeight(width);
    }

    @Override
    public Dimensions getPreferredSize() {
        return form.getPreferredSize();
    }

    @Override
    public void layout() {
        form.setLocation(0, 0);
        form.setSize(getWidth(), getHeight());
    }

    @Override
    public void sourceChanged(ComponentInspector componentInspector, Component previousSource) {
        formSection.remove(0, formSection.getLength());
        styles = null;

        Component source = componentInspector.getSource();

        if (previousSource != null) {
            previousSource.getComponentListeners().remove(componentHandler);
        }

        if (source != null) {
            source.getComponentListeners().add(componentHandler);

            styles = source.getStyles();

            ArrayList<String> styleKeys = new ArrayList<String>(styleKeyComparator);
            for (String styleKey : styles) {
                if (!styles.isReadOnly(styleKey)) {
                    styleKeys.add(styleKey);
                }
            }

            for (String styleKey : styleKeys) {
                addStyleControl(styleKey);
            }
        }
    }

    private void addStyleControl(String styleKey) {
        Class<?> styleType = styles.getType(styleKey);

        Component inspectorComponent = null;

        if (styleType == Boolean.TYPE) {
            inspectorComponent = addBooleanControl(styleKey);
        } else if (styleType == Integer.TYPE) {
            inspectorComponent = addIntControl(styleKey);
        } else if (styleType == Float.TYPE) {
            inspectorComponent = addFloatControl(styleKey);
        } else if (styleType == String.class) {
            inspectorComponent = addStringControl(styleKey);
        } else if (styleType.isEnum()) {
            inspectorComponent = addEnumControl(styleKey);
        } else if (styleType == Point.class) {
            inspectorComponent = addPointControl(styleKey);
        } else if (styleType == Dimensions.class) {
            inspectorComponent = addDimensionsControl(styleKey);
        } else if (styleType == Limits.class) {
            inspectorComponent = addLimitsControl(styleKey);
        } else if (styleType == Insets.class) {
            inspectorComponent = addInsetsControl(styleKey);
        } else if (styleType == CornerRadii.class) {
            inspectorComponent = addCornerRadiiControl(styleKey);
        }

        if (inspectorComponent != null) {
            inspectorComponents.put(styleKey, inspectorComponent);
        }
    }

    private Component addBooleanControl(final String styleKey) {
        boolean styleValue = (Boolean)styles.get(styleKey);

        Checkbox checkbox = new Checkbox();
        checkbox.setSelected(styleValue);
        formSection.add(checkbox);
        Form.setLabel(checkbox, styleKey);

        checkbox.getButtonStateListeners().add(new ButtonStateListener() {
            private boolean updating = false;

            @Override
            public void stateChanged(Button button, Button.State previousState) {
                if (!updating) {
                    updating = true;
                    try {
                        styles.put(styleKey, button.isSelected());
                    } catch (Exception exception) {
                        button.setState(previousState);
                    } finally {
                        updating = false;
                    }
                }
            }
        });

        return checkbox;
    }

    private void updateBooleanControl(String styleKey) {
        Checkbox checkbox = (Checkbox)inspectorComponents.get(styleKey);

        if (checkbox != null) {
            boolean styleValue = (Boolean)styles.get(styleKey);
            checkbox.setSelected(styleValue);
        }
    }

    private Component addIntControl(final String styleKey) {
        int styleValue = (Integer)styles.get(styleKey);

        TextInput textInput = new TextInput();
        textInput.setTextSize(10);
        textInput.setMaximumLength(10);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(styleValue));
        formSection.add(textInput);
        Form.setLabel(textInput, styleKey);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;

                    try {
                        styles.put(styleKey, Integer.parseInt(textInput.getText()));
                    } catch (Exception exception) {
                        Object styleValue = styles.get(styleKey);
                        textInput.setText(String.valueOf(styleValue));
                    }
                }
            }
        });

        return textInput;
    }

    private void updateIntControl(String styleKey) {
        TextInput textInput = (TextInput)inspectorComponents.get(styleKey);

        if (textInput != null) {
            int styleValue = (Integer)styles.get(styleKey);
            textInput.setText(String.valueOf(styleValue));
        }
    }

    private Component addFloatControl(final String styleKey) {
        float styleValue = (Float)styles.get(styleKey);

        TextInput textInput = new TextInput();
        textInput.setTextSize(10);
        textInput.setValidator(new FloatValidator());
        textInput.setText(String.valueOf(styleValue));
        formSection.add(textInput);
        Form.setLabel(textInput, styleKey);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;

                    try {
                        styles.put(styleKey, Float.parseFloat(textInput.getText()));
                    } catch (Exception exception) {
                        Object styleValue = styles.get(styleKey);
                        textInput.setText(String.valueOf(styleValue));
                    }
                }
            }
        });

        return textInput;
    }

    private void updateFloatControl(String styleKey) {
        TextInput textInput = (TextInput)inspectorComponents.get(styleKey);

        if (textInput != null) {
            float styleValue = (Float)styles.get(styleKey);
            textInput.setText(String.valueOf(styleValue));
        }
    }

    private Component addStringControl(final String styleKey) {
        String styleValue = (String)styles.get(styleKey);

        TextInput textInput = new TextInput();
        textInput.setText(styleValue == null ? "" : styleValue);
        formSection.add(textInput);
        Form.setLabel(textInput, styleKey);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;

                    try {
                        styles.put(styleKey, textInput.getText());
                    } catch (Exception exception) {
                        String styleValue = (String)styles.get(styleKey);
                        textInput.setText(styleValue == null ? "" : styleValue);
                    }
                }
            }
        });

        return textInput;
    }

    private void updateStringControl(String styleKey) {
        TextInput textInput = (TextInput)inspectorComponents.get(styleKey);

        if (textInput != null) {
            String styleValue = (String)styles.get(styleKey);
            textInput.setText(styleValue == null ? "" : styleValue);
        }
    }

    @SuppressWarnings("unchecked")
    private Component addEnumControl(final String styleKey) {
        Class<?> styleType = styles.getType(styleKey);
        Enum<?> styleValue = (Enum<?>)styles.get(styleKey);

        ListButton listButton = new ListButton();
        listButton.setListData(new EnumList(styleType));
        listButton.setSelectedItem(styleValue);
        formSection.add(listButton);
        Form.setLabel(listButton, styleKey);

        listButton.getListButtonSelectionListeners().add(new ListButtonSelectionListener() {
            private boolean updating = false;

            @Override
            public void selectedIndexChanged(ListButton listButton, int previousSelectedIndex) {
                if (!updating) {
                    updating = true;
                    try {
                        styles.put(styleKey, listButton.getSelectedItem());
                    } catch (Exception exception) {
                        listButton.setSelectedIndex(previousSelectedIndex);
                    } finally {
                        updating = false;
                    }
                }
            }
        });

        return listButton;
    }

    private void updateEnumControl(String styleKey) {
        ListButton listButton = (ListButton)inspectorComponents.get(styleKey);

        if (listButton != null) {
            Enum<?> styleValue = (Enum<?>)styles.get(styleKey);
            listButton.setSelectedItem(styleValue);
        }
    }

    private Component addPointControl(final String styleKey) {
        Point point = (Point)styles.get(styleKey);

        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);
        formSection.add(boxPane);
        Form.setLabel(boxPane, styleKey);

        FlowPane flowPane = new FlowPane();
        flowPane.getStyles().put("alignToBaseline", true);
        flowPane.getStyles().put("horizontalSpacing", 5);
        boxPane.add(flowPane);

        TextInput textInput = new TextInput();
        textInput.setTextSize(3);
        textInput.setMaximumLength(4);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(point.x));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    Point point = (Point)styles.get(styleKey);

                    try {
                        int x = Integer.parseInt(textInput.getText());
                        styles.put(styleKey, new Point(x, point.y));
                    } catch (Exception exception) {
                        textInput.setText(String.valueOf(point.x));
                    }
                }
            }
        });

        Label label = new Label("x");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);

        flowPane = new FlowPane();
        flowPane.getStyles().put("alignToBaseline", true);
        flowPane.getStyles().put("horizontalSpacing", 5);
        boxPane.add(flowPane);

        textInput = new TextInput();
        textInput.setTextSize(3);
        textInput.setMaximumLength(4);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(point.y));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    Point point = (Point)styles.get(styleKey);

                    try {
                        int y = Integer.parseInt(textInput.getText());
                        styles.put(styleKey, new Point(point.x, y));
                    } catch (Exception exception) {
                        textInput.setText(String.valueOf(point.y));
                    }
                }
            }
        });

        label = new Label("y");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);

        return boxPane;
    }

    private void updatePointControl(String styleKey) {
        BoxPane boxPane = (BoxPane)inspectorComponents.get(styleKey);

        if (boxPane != null) {
            Point point = (Point)styles.get(styleKey);

            TextInput xTextInput = (TextInput)((FlowPane)boxPane.get(0)).get(0);
            TextInput yTextInput = (TextInput)((FlowPane)boxPane.get(1)).get(0);

            xTextInput.setText(String.valueOf(point.x));
            yTextInput.setText(String.valueOf(point.y));
        }
    }

    private Component addDimensionsControl(final String styleKey) {
        Dimensions dimensions = (Dimensions)styles.get(styleKey);

        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);
        formSection.add(boxPane);
        Form.setLabel(boxPane, styleKey);

        FlowPane flowPane = new FlowPane();
        flowPane.getStyles().put("alignToBaseline", true);
        flowPane.getStyles().put("horizontalSpacing", 5);
        boxPane.add(flowPane);

        TextInput textInput = new TextInput();
        textInput.setTextSize(3);
        textInput.setMaximumLength(4);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(dimensions.width));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    Dimensions dimensions = (Dimensions)styles.get(styleKey);

                    try {
                        int width = Integer.parseInt(textInput.getText());
                        styles.put(styleKey, new Dimensions(width, dimensions.height));
                    } catch (Exception exception) {
                        textInput.setText(String.valueOf(dimensions.width));
                    }
                }
            }
        });

        Label label = new Label("width");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);

        flowPane = new FlowPane();
        flowPane.getStyles().put("alignToBaseline", true);
        flowPane.getStyles().put("horizontalSpacing", 5);
        boxPane.add(flowPane);

        textInput = new TextInput();
        textInput.setTextSize(3);
        textInput.setMaximumLength(4);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(dimensions.height));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    Dimensions dimensions = (Dimensions)styles.get(styleKey);

                    try {
                        int height = Integer.parseInt(textInput.getText());
                        styles.put(styleKey, new Dimensions(dimensions.width, height));
                    } catch (Exception exception) {
                        textInput.setText(String.valueOf(dimensions.height));
                    }
                }
            }
        });

        label = new Label("height");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);

        return boxPane;
    }

    private void updateDimensionsControl(String styleKey) {
        BoxPane boxPane = (BoxPane)inspectorComponents.get(styleKey);

        if (boxPane != null) {
            Dimensions dimensions = (Dimensions)styles.get(styleKey);

            TextInput widthTextInput = (TextInput)((FlowPane)boxPane.get(0)).get(0);
            TextInput heightTextInput = (TextInput)((FlowPane)boxPane.get(1)).get(0);

            widthTextInput.setText(String.valueOf(dimensions.width));
            heightTextInput.setText(String.valueOf(dimensions.height));
        }
    }

    private Component addLimitsControl(final String styleKey) {
        Limits limits = (Limits)styles.get(styleKey);

        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);
        formSection.add(boxPane);
        Form.setLabel(boxPane, styleKey);

        FlowPane flowPane = new FlowPane();
        flowPane.getStyles().put("alignToBaseline", true);
        flowPane.getStyles().put("horizontalSpacing", 5);
        boxPane.add(flowPane);

        TextInput textInput = new TextInput();
        textInput.setTextSize(10);
        textInput.setMaximumLength(10);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(limits.min));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    Limits limits = (Limits)styles.get(styleKey);

                    try {
                        int min = Integer.parseInt(textInput.getText());
                        styles.put(styleKey, new Limits(min, limits.max));
                    } catch (Exception exception) {
                        textInput.setText(String.valueOf(limits.min));
                    }
                }
            }
        });

        Label label = new Label("min");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);

        flowPane = new FlowPane();
        flowPane.getStyles().put("alignToBaseline", true);
        flowPane.getStyles().put("horizontalSpacing", 5);
        boxPane.add(flowPane);

        textInput = new TextInput();
        textInput.setTextSize(10);
        textInput.setMaximumLength(10);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(limits.max));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    Limits limits = (Limits)styles.get(styleKey);

                    try {
                        int max = Integer.parseInt(textInput.getText());
                        styles.put(styleKey, new Limits(limits.min, max));
                    } catch (Exception exception) {
                        textInput.setText(String.valueOf(limits.max));
                    }
                }
            }
        });

        label = new Label("max");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);

        return boxPane;
    }

    private void updateLimitsControl(String styleKey) {
        BoxPane boxPane = (BoxPane)inspectorComponents.get(styleKey);

        if (boxPane != null) {
            Limits limits = (Limits)styles.get(styleKey);

            TextInput minTextInput = (TextInput)((FlowPane)boxPane.get(0)).get(0);
            TextInput maxTextInput = (TextInput)((FlowPane)boxPane.get(1)).get(0);

            minTextInput.setText(String.valueOf(limits.min));
            maxTextInput.setText(String.valueOf(limits.max));
        }
    }

    private Component addInsetsControl(final String styleKey) {
        Insets insets = (Insets)styles.get(styleKey);

        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);
        formSection.add(boxPane);
        Form.setLabel(boxPane, styleKey);

        FlowPane flowPane = new FlowPane();
        flowPane.getStyles().put("alignToBaseline", true);
        flowPane.getStyles().put("horizontalSpacing", 5);
        boxPane.add(flowPane);

        TextInput textInput = new TextInput();
        textInput.setTextSize(4);
        textInput.setMaximumLength(4);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(insets.top));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    Insets insets = (Insets)styles.get(styleKey);

                    try {
                        int top = Integer.parseInt(textInput.getText());
                        styles.put(styleKey, new Insets(top, insets.left, insets.bottom,
                            insets.right));
                    } catch (Exception exception) {
                        textInput.setText(String.valueOf(insets.top));
                    }
                }
            }
        });

        Label label = new Label("top");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);

        flowPane = new FlowPane();
        flowPane.getStyles().put("alignToBaseline", true);
        flowPane.getStyles().put("horizontalSpacing", 5);
        boxPane.add(flowPane);

        textInput = new TextInput();
        textInput.setTextSize(4);
        textInput.setMaximumLength(4);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(insets.left));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    Insets insets = (Insets)styles.get(styleKey);

                    try {
                        int left = Integer.parseInt(textInput.getText());
                        styles.put(styleKey, new Insets(insets.top, left, insets.bottom,
                            insets.right));
                    } catch (Exception exception) {
                        textInput.setText(String.valueOf(insets.left));
                    }
                }
            }
        });

        label = new Label("left");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);

        flowPane = new FlowPane();
        flowPane.getStyles().put("alignToBaseline", true);
        flowPane.getStyles().put("horizontalSpacing", 5);
        boxPane.add(flowPane);

        textInput = new TextInput();
        textInput.setTextSize(4);
        textInput.setMaximumLength(4);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(insets.bottom));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    Insets insets = (Insets)styles.get(styleKey);

                    try {
                        int bottom = Integer.parseInt(textInput.getText());
                        styles.put(styleKey, new Insets(insets.top, insets.left, bottom,
                            insets.right));
                    } catch (Exception exception) {
                        textInput.setText(String.valueOf(insets.bottom));
                    }
                }
            }
        });

        label = new Label("bottom");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);

        flowPane = new FlowPane();
        flowPane.getStyles().put("alignToBaseline", true);
        flowPane.getStyles().put("horizontalSpacing", 5);
        boxPane.add(flowPane);

        textInput = new TextInput();
        textInput.setTextSize(4);
        textInput.setMaximumLength(4);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(insets.right));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    Insets insets = (Insets)styles.get(styleKey);

                    try {
                        int right = Integer.parseInt(textInput.getText());
                        styles.put(styleKey, new Insets(insets.top, insets.left, insets.bottom,
                            right));
                    } catch (Exception exception) {
                        textInput.setText(String.valueOf(insets.right));
                    }
                }
            }
        });

        label = new Label("right");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);

        return boxPane;
    }

    private Component addCornerRadiiControl(final String styleKey) {
        CornerRadii cornerRadii = (CornerRadii)styles.get(styleKey);

        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);
        formSection.add(boxPane);
        Form.setLabel(boxPane, styleKey);

        FlowPane flowPane = new FlowPane();
        flowPane.getStyles().put("alignToBaseline", true);
        flowPane.getStyles().put("horizontalSpacing", 5);
        boxPane.add(flowPane);

        TextInput textInput = new TextInput();
        textInput.setTextSize(4);
        textInput.setMaximumLength(4);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(cornerRadii.topLeft));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    CornerRadii cornerRadii = (CornerRadii)styles.get(styleKey);

                    try {
                        int topLeft = Integer.parseInt(textInput.getText());
                        styles.put(styleKey, new CornerRadii(topLeft, cornerRadii.topRight,
                            cornerRadii.bottomLeft, cornerRadii.bottomRight));
                    } catch (Exception exception) {
                        textInput.setText(String.valueOf(cornerRadii.topLeft));
                    }
                }
            }
        });

        Label label = new Label("topLeft");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);

        flowPane = new FlowPane();
        flowPane.getStyles().put("alignToBaseline", true);
        flowPane.getStyles().put("horizontalSpacing", 5);
        boxPane.add(flowPane);

        textInput = new TextInput();
        textInput.setTextSize(4);
        textInput.setMaximumLength(4);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(cornerRadii.topRight));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    CornerRadii cornerRadii = (CornerRadii)styles.get(styleKey);

                    try {
                        int topRight = Integer.parseInt(textInput.getText());
                        styles.put(styleKey, new CornerRadii(cornerRadii.topLeft, topRight,
                            cornerRadii.bottomLeft, cornerRadii.bottomRight));
                    } catch (Exception exception) {
                        textInput.setText(String.valueOf(cornerRadii.topRight));
                    }
                }
            }
        });

        label = new Label("topRight");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);

        flowPane = new FlowPane();
        flowPane.getStyles().put("alignToBaseline", true);
        flowPane.getStyles().put("horizontalSpacing", 5);
        boxPane.add(flowPane);

        textInput = new TextInput();
        textInput.setTextSize(4);
        textInput.setMaximumLength(4);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(cornerRadii.bottomLeft));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    CornerRadii cornerRadii = (CornerRadii)styles.get(styleKey);

                    try {
                        int bottomLeft = Integer.parseInt(textInput.getText());
                        styles.put(styleKey, new CornerRadii(cornerRadii.topLeft,
                            cornerRadii.topRight, bottomLeft, cornerRadii.bottomRight));
                    } catch (Exception exception) {
                        textInput.setText(String.valueOf(cornerRadii.bottomLeft));
                    }
                }
            }
        });

        label = new Label("bottomLeft");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);

        flowPane = new FlowPane();
        flowPane.getStyles().put("alignToBaseline", true);
        flowPane.getStyles().put("horizontalSpacing", 5);
        boxPane.add(flowPane);

        textInput = new TextInput();
        textInput.setTextSize(4);
        textInput.setMaximumLength(4);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(cornerRadii.bottomRight));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    CornerRadii cornerRadii = (CornerRadii)styles.get(styleKey);

                    try {
                        int bottomRight = Integer.parseInt(textInput.getText());
                        styles.put(styleKey, new CornerRadii(cornerRadii.topLeft,
                            cornerRadii.topRight, cornerRadii.bottomLeft, bottomRight));
                    } catch (Exception exception) {
                        textInput.setText(String.valueOf(cornerRadii.bottomRight));
                    }
                }
            }
        });

        label = new Label("bottomRight");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);

        return boxPane;
    }
}
