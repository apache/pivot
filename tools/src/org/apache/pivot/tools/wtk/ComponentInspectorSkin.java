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

import java.awt.Color;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonStateListener;
import org.apache.pivot.wtk.CalendarButton;
import org.apache.pivot.wtk.CalendarButtonSelectionListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.ColorChooserButton;
import org.apache.pivot.wtk.ColorChooserButtonSelectionListener;
import org.apache.pivot.wtk.Component;
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
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.ScrollBar.Scope;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.skin.ContainerSkin;
import org.apache.pivot.wtk.text.validation.DoubleValidator;
import org.apache.pivot.wtk.text.validation.IntValidator;
import org.apache.pivot.wtk.text.validation.FloatValidator;

abstract class ComponentInspectorSkin extends ContainerSkin implements ComponentInspectorListener {
    // Container for the control components
    protected Form form = new Form();

    // Maps key to corresponding control component
    private HashMap<String, Component> controls = new HashMap<String, Component>();

    public ComponentInspectorSkin() {
        form.getStyles().put("rightAlignLabels", true);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        ComponentInspector componentInspector = (ComponentInspector)component;

        componentInspector.getComponentInspectorListeners().add(this);
        componentInspector.add(form);

        sourceChanged(componentInspector, null);
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
        // No-op
    }

    /**
     * Adds a new control component to the specified form section. The component
     * will control the specified property.
     *
     * @param dictionary
     * The property dictionary
     *
     * @param key
     * The property key
     *
     * @param type
     * The type of the property
     *
     * @param section
     * The form section
     *
     * @throws IllegalArgumentException
     * If the form section does not belong to this skin's form
     */
    @SuppressWarnings("unchecked")
    protected void addControl(Dictionary<String, Object> dictionary, String key,
        Class<?> type, Form.Section section) {
        if (section.getForm() != form) {
            throw new IllegalArgumentException("section does not belong to form.");
        }

        Component control = null;

        if (type == Boolean.TYPE) {
            control = addBooleanControl(dictionary, key, section);
        } else if (type == Integer.TYPE) {
            control = addIntControl(dictionary, key, section);
        } else if (type == Float.TYPE) {
            control = addFloatControl(dictionary, key, section);
        } else if (type == Double.TYPE) {
            control = addDoubleControl(dictionary, key, section);
        } else if (type == String.class) {
            control = addStringControl(dictionary, key, section);
        } else if (type.isEnum()) {
            control = addEnumControl(dictionary, key, (Class<? extends Enum<?>>)type, section);
        } else if (type == Point.class) {
            control = addPointControl(dictionary, key, section);
        } else if (type == Dimensions.class) {
            control = addDimensionsControl(dictionary, key, section);
        } else if (type == Limits.class) {
            control = addLimitsControl(dictionary, key, section);
        } else if (type == Insets.class) {
            control = addInsetsControl(dictionary, key, section);
        } else if (type == Span.class) {
            control = addSpanControl(dictionary, key, section);
        } else if (type == CornerRadii.class) {
            control = addCornerRadiiControl(dictionary, key, section);
        } else if (type == Scope.class) {
            control = addScopeControl(dictionary, key, section);
        } else if (type == Color.class) {
            control = addColorControl(dictionary, key, section);
        } else if (type == CalendarDate.class) {
            control = addCalendarDateControl(dictionary, key, section);
        }

        if (control != null) {
            controls.put(key, control);
        }
    }

    /**
     * Removes all control components from this skin's form.
     */
    protected void clearControls() {
        for (Form.Section section : form.getSections()) {
            section.remove(0, section.getLength());
        }

        controls.clear();
    }

    /**
     * Updates the control component associated with the specified property to
     * the appropriate state based on the property value.
     *
     * @param dictionary
     * The property dictionary
     *
     * @param key
     * The property key
     *
     * @param type
     * The type of the property
     */
    protected void updateControl(Dictionary<String, Object> dictionary, String key, Class<?> type) {
        if (type == Boolean.TYPE) {
            updateBooleanControl(dictionary, key);
        } else if (type == Integer.TYPE) {
            updateIntControl(dictionary, key);
        } else if (type == Float.TYPE) {
            updateFloatControl(dictionary, key);
        } else if (type == Double.TYPE) {
            updateDoubleControl(dictionary, key);
        } else if (type == String.class) {
            updateStringControl(dictionary, key);
        } else if (type.isEnum()) {
            updateEnumControl(dictionary, key);
        } else if (type == Point.class) {
            updatePointControl(dictionary, key);
        } else if (type == Dimensions.class) {
            updateDimensionsControl(dictionary, key);
        } else if (type == Limits.class) {
            updateLimitsControl(dictionary, key);
        } else if (type == Span.class) {
            updateSpanControl(dictionary, key);
        } else if (type == Scope.class) {
            updateScopeControl(dictionary, key);
        } else if (type == Color.class) {
            updateColorControl(dictionary, key);
        } else if (type == CalendarDate.class) {
            updateCalendarDateControl(dictionary, key);
        }
    }

    private Component addBooleanControl(final Dictionary<String, Object> dictionary,
        final String key, Form.Section section) {
        boolean value = (Boolean)dictionary.get(key);

        Checkbox checkbox = new Checkbox();
        checkbox.setSelected(value);
        section.add(checkbox);
        Form.setLabel(checkbox, key);

        checkbox.getButtonStateListeners().add(new ButtonStateListener() {
            private boolean updating = false;

            @Override
            public void stateChanged(Button button, Button.State previousState) {
                if (!updating) {
                    updating = true;
                    try {
                        dictionary.put(key, button.isSelected());
                    } catch (Exception exception) {
                        displayErrorMessage(exception, button.getWindow());
                        button.setState(previousState);
                    } finally {
                        updating = false;
                    }
                }
            }
        });

        return checkbox;
    }

    private void updateBooleanControl(Dictionary<String, Object> dictionary, String key) {
        Checkbox checkbox = (Checkbox)controls.get(key);

        if (checkbox != null) {
            boolean value = (Boolean)dictionary.get(key);
            checkbox.setSelected(value);
        }
    }

    private Component addIntControl(final Dictionary<String, Object> dictionary,
        final String key, Form.Section section) {
        int value = (Integer)dictionary.get(key);

        TextInput textInput = new TextInput();
        textInput.setTextSize(10);
        textInput.setMaximumLength(10);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(value));
        section.add(textInput);
        Form.setLabel(textInput, key);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;

                    try {
                        dictionary.put(key, Integer.parseInt(textInput.getText()));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
                        int value = (Integer)dictionary.get(key);
                        textInput.setText(String.valueOf(value));
                    }
                }
            }
        });

        return textInput;
    }

    private void updateIntControl(Dictionary<String, Object> dictionary, String key) {
        TextInput textInput = (TextInput)controls.get(key);

        if (textInput != null) {
            int value = (Integer)dictionary.get(key);
            textInput.setText(String.valueOf(value));
        }
    }

    private Component addFloatControl(final Dictionary<String, Object> dictionary,
        final String key, Form.Section section) {
        float value = (Float)dictionary.get(key);

        TextInput textInput = new TextInput();
        textInput.setTextSize(10);
        textInput.setValidator(new FloatValidator());
        textInput.setText(String.valueOf(value));
        section.add(textInput);
        Form.setLabel(textInput, key);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;

                    try {
                        dictionary.put(key, Float.parseFloat(textInput.getText()));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
                        float value = (Float)dictionary.get(key);
                        textInput.setText(String.valueOf(value));
                    }
                }
            }
        });

        return textInput;
    }

    private void updateFloatControl(Dictionary<String, Object> dictionary, String key) {
        TextInput textInput = (TextInput)controls.get(key);

        if (textInput != null) {
            float value = (Float)dictionary.get(key);
            textInput.setText(String.valueOf(value));
        }
    }

    private Component addDoubleControl(final Dictionary<String, Object> dictionary,
        final String key, Form.Section section) {
        double value = (Double)dictionary.get(key);

        TextInput textInput = new TextInput();
        textInput.setTextSize(14);
        textInput.setValidator(new DoubleValidator());
        textInput.setText(String.valueOf(value));
        section.add(textInput);
        Form.setLabel(textInput, key);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;

                    try {
                        dictionary.put(key, Double.parseDouble(textInput.getText()));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
                        double value = (Double)dictionary.get(key);
                        textInput.setText(String.valueOf(value));
                    }
                }
            }
        });

        return textInput;
    }

    private void updateDoubleControl(Dictionary<String, Object> dictionary, String key) {
        TextInput textInput = (TextInput)controls.get(key);

        if (textInput != null) {
            double value = (Double)dictionary.get(key);
            textInput.setText(String.valueOf(value));
        }
    }

    private Component addStringControl(final Dictionary<String, Object> dictionary,
        final String key, Form.Section section) {
        String value = (String)dictionary.get(key);

        TextInput textInput = new TextInput();
        textInput.setText(value == null ? "" : value);
        section.add(textInput);
        Form.setLabel(textInput, key);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;

                    try {
                        dictionary.put(key, textInput.getText());
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
                        String value = (String)dictionary.get(key);
                        textInput.setText(value == null ? "" : value);
                    }
                }
            }
        });

        return textInput;
    }

    private void updateStringControl(Dictionary<String, Object> dictionary, String key) {
        TextInput textInput = (TextInput)controls.get(key);

        if (textInput != null) {
            String value = (String)dictionary.get(key);
            textInput.setText(value == null ? "" : value);
        }
    }

    private Component addEnumControl(final Dictionary<String, Object> dictionary,
        final String key, Class<? extends Enum<?>> type, Form.Section section) {
        Enum<?> value = (Enum<?>)dictionary.get(key);

        ArrayList<Object> listData = new ArrayList<Object>();
        listData.add(null);

        Enum<?>[] enumConstants = type.getEnumConstants();
        for (int i = 0; i < enumConstants.length; i++) {
            listData.add(enumConstants[i]);
        }

        ListButton listButton = new ListButton();
        listButton.setListData(listData);
        listButton.setSelectedItem(value);
        section.add(listButton);
        Form.setLabel(listButton, key);

        listButton.getListButtonSelectionListeners().add(new ListButtonSelectionListener() {
            private boolean updating = false;

            @Override
            public void selectedIndexChanged(ListButton listButton, int previousSelectedIndex) {
                if (!updating) {
                    updating = true;
                    try {
                        dictionary.put(key, listButton.getSelectedItem());
                    } catch (Exception exception) {
                        displayErrorMessage(exception, listButton.getWindow());
                        listButton.setSelectedIndex(previousSelectedIndex);
                    } finally {
                        updating = false;
                    }
                }
            }
        });

        return listButton;
    }

    private void updateEnumControl(Dictionary<String, Object> dictionary, String key) {
        ListButton listButton = (ListButton)controls.get(key);

        if (listButton != null) {
            Enum<?> value = (Enum<?>)dictionary.get(key);
            listButton.setSelectedItem(value);
        }
    }

    private Component addPointControl(final Dictionary<String, Object> dictionary,
        final String key, Form.Section section) {
        Point point = (Point)dictionary.get(key);

        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);
        section.add(boxPane);
        Form.setLabel(boxPane, key);

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
                    Point point = (Point)dictionary.get(key);

                    try {
                        int x = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new Point(x, point.y));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
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
                    Point point = (Point)dictionary.get(key);

                    try {
                        int y = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new Point(point.x, y));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
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

    private void updatePointControl(Dictionary<String, Object> dictionary, String key) {
        BoxPane boxPane = (BoxPane)controls.get(key);

        if (boxPane != null) {
            Point point = (Point)dictionary.get(key);

            TextInput xTextInput = (TextInput)((FlowPane)boxPane.get(0)).get(0);
            TextInput yTextInput = (TextInput)((FlowPane)boxPane.get(1)).get(0);

            xTextInput.setText(String.valueOf(point.x));
            yTextInput.setText(String.valueOf(point.y));
        }
    }

    private Component addDimensionsControl(final Dictionary<String, Object> dictionary,
        final String key, Form.Section section) {
        Dimensions dimensions = (Dimensions)dictionary.get(key);

        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);
        section.add(boxPane);
        Form.setLabel(boxPane, key);

        FlowPane flowPane = new FlowPane();
        flowPane.getStyles().put("alignToBaseline", true);
        flowPane.getStyles().put("horizontalSpacing", 5);
        boxPane.add(flowPane);

        TextInput textInput = new TextInput();
        textInput.setTextSize(4);
        textInput.setMaximumLength(5);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(dimensions.width));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    Dimensions dimensions = (Dimensions)dictionary.get(key);

                    try {
                        int width = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new Dimensions(width, dimensions.height));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
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
        textInput.setTextSize(4);
        textInput.setMaximumLength(5);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(dimensions.height));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    Dimensions dimensions = (Dimensions)dictionary.get(key);

                    try {
                        int height = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new Dimensions(dimensions.width, height));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
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

    private void updateDimensionsControl(Dictionary<String, Object> dictionary, String key) {
        BoxPane boxPane = (BoxPane)controls.get(key);

        if (boxPane != null) {
            Dimensions dimensions = (Dimensions)dictionary.get(key);

            TextInput widthTextInput = (TextInput)((FlowPane)boxPane.get(0)).get(0);
            TextInput heightTextInput = (TextInput)((FlowPane)boxPane.get(1)).get(0);

            widthTextInput.setText(String.valueOf(dimensions.width));
            heightTextInput.setText(String.valueOf(dimensions.height));
        }
    }

    private Component addLimitsControl(final Dictionary<String, Object> dictionary,
        final String key, Form.Section section) {
        Limits limits = (Limits)dictionary.get(key);

        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);
        section.add(boxPane);
        Form.setLabel(boxPane, key);

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
                    Limits limits = (Limits)dictionary.get(key);

                    try {
                        int min = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new Limits(min, limits.max));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
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
                    Limits limits = (Limits)dictionary.get(key);

                    try {
                        int max = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new Limits(limits.min, max));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
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

    private void updateLimitsControl(Dictionary<String, Object> dictionary, String key) {
        BoxPane boxPane = (BoxPane)controls.get(key);

        if (boxPane != null) {
            Limits limits = (Limits)dictionary.get(key);

            TextInput minTextInput = (TextInput)((FlowPane)boxPane.get(0)).get(0);
            TextInput maxTextInput = (TextInput)((FlowPane)boxPane.get(1)).get(0);

            minTextInput.setText(String.valueOf(limits.min));
            maxTextInput.setText(String.valueOf(limits.max));
        }
    }

    private Component addInsetsControl(final Dictionary<String, Object> dictionary,
        final String key, Form.Section section) {
        Insets insets = (Insets)dictionary.get(key);

        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);
        section.add(boxPane);
        Form.setLabel(boxPane, key);

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
                    Insets insets = (Insets)dictionary.get(key);

                    try {
                        int top = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new Insets(top, insets.left, insets.bottom,
                            insets.right));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
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
                    Insets insets = (Insets)dictionary.get(key);

                    try {
                        int left = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new Insets(insets.top, left, insets.bottom,
                            insets.right));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
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
                    Insets insets = (Insets)dictionary.get(key);

                    try {
                        int bottom = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new Insets(insets.top, insets.left, bottom,
                            insets.right));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
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
                    Insets insets = (Insets)dictionary.get(key);

                    try {
                        int right = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new Insets(insets.top, insets.left, insets.bottom,
                            right));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
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

    private Component addSpanControl(final Dictionary<String, Object> dictionary,
        final String key, Form.Section section) {
        Span span = (Span)dictionary.get(key);

        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);
        section.add(boxPane);
        Form.setLabel(boxPane, key);

        FlowPane flowPane = new FlowPane();
        flowPane.getStyles().put("alignToBaseline", true);
        flowPane.getStyles().put("horizontalSpacing", 5);
        boxPane.add(flowPane);

        TextInput textInput = new TextInput();
        textInput.setTextSize(10);
        textInput.setMaximumLength(10);
        textInput.setValidator(new IntValidator());
        textInput.setText(span == null ? "" : String.valueOf(span.start));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    Span span = (Span)dictionary.get(key);

                    try {
                        int start = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new Span(start, span == null ? start : span.end));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
                        textInput.setText(span == null ? "" : String.valueOf(span.start));
                    }
                }
            }
        });

        Label label = new Label("start");
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
        textInput.setText(span == null ? "" : String.valueOf(span.end));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    Span span = (Span)dictionary.get(key);

                    try {
                        int end = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new Span(span == null ? end : span.start, end));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
                        textInput.setText(span == null ? "" : String.valueOf(span.end));
                    }
                }
            }
        });

        label = new Label("end");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);

        return boxPane;
    }

    private void updateSpanControl(Dictionary<String, Object> dictionary, String key) {
        BoxPane boxPane = (BoxPane)controls.get(key);

        if (boxPane != null) {
            Span span = (Span)dictionary.get(key);

            TextInput startTextInput = (TextInput)((FlowPane)boxPane.get(0)).get(0);
            TextInput endTextInput = (TextInput)((FlowPane)boxPane.get(1)).get(0);

            startTextInput.setText(span == null ? "" : String.valueOf(span.start));
            endTextInput.setText(span == null ? "" : String.valueOf(span.end));
        }
    }

    private Component addCornerRadiiControl(final Dictionary<String, Object> dictionary,
        final String key, Form.Section section) {
        CornerRadii cornerRadii = (CornerRadii)dictionary.get(key);

        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);
        section.add(boxPane);
        Form.setLabel(boxPane, key);

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
                    CornerRadii cornerRadii = (CornerRadii)dictionary.get(key);

                    try {
                        int topLeft = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new CornerRadii(topLeft, cornerRadii.topRight,
                            cornerRadii.bottomLeft, cornerRadii.bottomRight));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
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
                    CornerRadii cornerRadii = (CornerRadii)dictionary.get(key);

                    try {
                        int topRight = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new CornerRadii(cornerRadii.topLeft, topRight,
                            cornerRadii.bottomLeft, cornerRadii.bottomRight));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
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
                    CornerRadii cornerRadii = (CornerRadii)dictionary.get(key);

                    try {
                        int bottomLeft = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new CornerRadii(cornerRadii.topLeft,
                            cornerRadii.topRight, bottomLeft, cornerRadii.bottomRight));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
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
                    CornerRadii cornerRadii = (CornerRadii)dictionary.get(key);

                    try {
                        int bottomRight = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new CornerRadii(cornerRadii.topLeft,
                            cornerRadii.topRight, cornerRadii.bottomLeft, bottomRight));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
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

    private Component addScopeControl(final Dictionary<String, Object> dictionary,
        final String key, Form.Section section) {
        Scope scope = (Scope)dictionary.get(key);

        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);
        section.add(boxPane);
        Form.setLabel(boxPane, key);

        FlowPane flowPane = new FlowPane();
        flowPane.getStyles().put("alignToBaseline", true);
        flowPane.getStyles().put("horizontalSpacing", 5);
        boxPane.add(flowPane);

        TextInput textInput = new TextInput();
        textInput.setTextSize(10);
        textInput.setMaximumLength(10);
        textInput.setValidator(new IntValidator());
        textInput.setText(scope == null ? "" : String.valueOf(scope.start));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    Scope scope = (Scope)dictionary.get(key);

                    try {
                        int start = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new Scope(start, scope == null ? start : scope.end,
                            scope == null ? start : scope.extent));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
                        textInput.setText(scope == null ? "" : String.valueOf(scope.start));
                    }
                }
            }
        });

        Label label = new Label("start");
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
        textInput.setText(scope == null ? "" : String.valueOf(scope.end));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    Scope scope = (Scope)dictionary.get(key);

                    try {
                        int end = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new Scope(scope == null ? end : scope.start, end,
                            scope == null ? end : scope.extent));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
                        textInput.setText(scope == null ? "" : String.valueOf(scope.end));
                    }
                }
            }
        });

        label = new Label("end");
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
        textInput.setText(scope == null ? "" : String.valueOf(scope.extent));
        flowPane.add(textInput);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;
                    Scope scope = (Scope)dictionary.get(key);

                    try {
                        int extent = Integer.parseInt(textInput.getText());
                        dictionary.put(key, new Scope(scope == null ? extent : scope.start,
                            scope == null ? extent : scope.end, extent));
                    } catch (Exception exception) {
                        displayErrorMessage(exception, component.getWindow());
                        textInput.setText(scope == null ? "" : String.valueOf(scope.extent));
                    }
                }
            }
        });

        label = new Label("extent");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);

        return boxPane;
    }

    private void updateScopeControl(Dictionary<String, Object> dictionary, String key) {
        BoxPane boxPane = (BoxPane)controls.get(key);

        if (boxPane != null) {
            Scope scope = (Scope)dictionary.get(key);

            TextInput startTextInput = (TextInput)((FlowPane)boxPane.get(0)).get(0);
            TextInput endTextInput = (TextInput)((FlowPane)boxPane.get(1)).get(0);
            TextInput extentTextInput = (TextInput)((FlowPane)boxPane.get(2)).get(0);

            startTextInput.setText(scope == null ? "" : String.valueOf(scope.start));
            endTextInput.setText(scope == null ? "" : String.valueOf(scope.end));
            extentTextInput.setText(scope == null ? "" : String.valueOf(scope.extent));
        }
    }

    private Component addColorControl(final Dictionary<String, Object> dictionary,
        final String key, Form.Section section) {
        Color color = (Color)dictionary.get(key);

        ColorChooserButton colorChooserButton = new ColorChooserButton();
        colorChooserButton.setSelectedColor(color);
        section.add(colorChooserButton);
        Form.setLabel(colorChooserButton, key);

        colorChooserButton.getColorChooserButtonSelectionListeners().add
            (new ColorChooserButtonSelectionListener() {
            @Override
            public void selectedColorChanged(ColorChooserButton colorChooserButton,
                Color previousSelectedColor) {
                try {
                    dictionary.put(key, colorChooserButton.getSelectedColor());
                } catch (Exception exception) {
                    displayErrorMessage(exception, colorChooserButton.getWindow());
                    dictionary.put(key, previousSelectedColor);
                }
            }
        });

        return colorChooserButton;
    }

    private void updateColorControl(Dictionary<String, Object> dictionary, String key) {
        ColorChooserButton colorChooserButton = (ColorChooserButton)controls.get(key);

        if (colorChooserButton != null) {
            Color value = (Color)dictionary.get(key);
            colorChooserButton.setSelectedColor(value);
        }
    }

    private Component addCalendarDateControl(final Dictionary<String, Object> dictionary,
        final String key, Form.Section section) {
        CalendarDate calendarDate = (CalendarDate)dictionary.get(key);

        CalendarButton calendarButton = new CalendarButton();
        calendarButton.setMinimumPreferredWidth(75);
        calendarButton.setSelectedDate(calendarDate);
        section.add(calendarButton);
        Form.setLabel(calendarButton, key);

        calendarButton.getCalendarButtonSelectionListeners().add
            (new CalendarButtonSelectionListener() {
            @Override
            public void selectedDateChanged(CalendarButton calendarButton,
                CalendarDate previousSelectedDate) {
                try {
                    dictionary.put(key, calendarButton.getSelectedDate());
                } catch (Exception exception) {
                    displayErrorMessage(exception, calendarButton.getWindow());
                    dictionary.put(key, previousSelectedDate);
                }
            }
        });

        return calendarButton;
    }

    private void updateCalendarDateControl(Dictionary<String, Object> dictionary, String key) {
        CalendarButton calendarButton = (CalendarButton)controls.get(key);

        if (calendarButton != null) {
            CalendarDate value = (CalendarDate)dictionary.get(key);
            calendarButton.setSelectedDate(value);
        }
    }

    private void displayErrorMessage(Exception exception, Window window) {
        String message = exception.getLocalizedMessage();

        if (message == null) {
            message = exception.getClass().getSimpleName();
        }

        Prompt.prompt(MessageType.ERROR, message, window);
    }
}
