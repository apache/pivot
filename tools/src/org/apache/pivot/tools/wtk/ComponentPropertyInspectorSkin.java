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

import java.lang.reflect.Method;
import java.util.Comparator;

import org.apache.pivot.beans.BeanDictionary;
import org.apache.pivot.beans.BeanDictionaryListener;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.EnumList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonStateListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentStateListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.FlowPane;
import org.apache.pivot.wtk.Form;
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

class ComponentPropertyInspectorSkin extends ContainerSkin
    implements ComponentInspectorListener {
    private static class PropertyNameComparator implements Comparator<String> {
        @Override
        public int compare(String propertyName1, String propertyName2) {
            return propertyName1.compareTo(propertyName2);
        }
    }

    private static class PropertySourceTypeComparator implements Comparator<Class<?>> {
        @Override
        public int compare(Class<?> sourceType1, Class<?> sourceType2) {
            int result = 0;

            if (sourceType1.isAssignableFrom(sourceType2)) {
                result = 1;
            } else if (sourceType2.isAssignableFrom(sourceType1)) {
                result = -1;
            } else {
                result = sourceType1.getName().compareTo(sourceType2.getName());
            }

            return result;
        }
    }

    private Form form = new Form();

    private BeanDictionary beanDictionary = new BeanDictionary();

    private HashMap<String, Component> inspectorComponents = new HashMap<String, Component>();

    private static PropertyNameComparator propertyNameComparator = new PropertyNameComparator();
    private static PropertySourceTypeComparator propertySourceTypeComparator =
        new PropertySourceTypeComparator();

    public ComponentPropertyInspectorSkin() {
        beanDictionary.getBeanDictionaryListeners().add(new BeanDictionaryListener.Adapter() {
            @Override
            public void propertyChanged(BeanDictionary beanDictionary, String propertyName) {
                Class<?> propertyType = beanDictionary.getType(propertyName);

                if (propertyType == Boolean.TYPE) {
                    updateBooleanControl(propertyName);
                } else if (propertyType == Integer.TYPE) {
                    updateIntControl(propertyName);
                } else if (propertyType == Float.TYPE) {
                    updateFloatControl(propertyName);
                } else if (propertyType == String.class) {
                    updateStringControl(propertyName);
                } else if (propertyType.isEnum()) {
                    updateEnumControl(propertyName);
                } else if (propertyType == Point.class) {
                    updatePointControl(propertyName);
                } else if (propertyType == Dimensions.class) {
                    updateDimensionsControl(propertyName);
                } else if (propertyType == Limits.class) {
                    updateLimitsControl(propertyName);
                }
            }
        });

        form.getStyles().put("rightAlignLabels", true);
        form.getStyles().put("showFirstSectionHeading", true);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        ComponentPropertyInspector componentPropertyInspector =
            (ComponentPropertyInspector)component;

        componentPropertyInspector.getComponentInspectorListeners().add(this);
        componentPropertyInspector.add(form);

        sourceChanged(componentPropertyInspector, null);
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
        Form.SectionSequence sections = form.getSections();
        sections.remove(0, sections.getLength());

        Component source = componentInspector.getSource();

        beanDictionary.setBean(source);

        if (source != null) {
            Class<?> sourceType = source.getClass();
            HashMap<Class<?>, List<String>> propertyBuckets =
                new HashMap<Class<?>, List<String>>(propertySourceTypeComparator);

            for (String propertyName : beanDictionary) {
                if (beanDictionary.isNotifying(propertyName)
                    && !beanDictionary.isReadOnly(propertyName)) {
                    Method method = BeanDictionary.getGetterMethod(sourceType, propertyName);
                    Class<?> declaringClass = method.getDeclaringClass();

                    List<String> propertyNames = propertyBuckets.get(declaringClass);
                    if (propertyNames == null) {
                        propertyNames = new ArrayList<String>(propertyNameComparator);
                        propertyBuckets.put(declaringClass, propertyNames);
                    }

                    propertyNames.add(propertyName);
                }
            }

            for (Class<?> declaringClass : propertyBuckets) {
                Form.Section section = new Form.Section();
                section.setHeading(declaringClass.getSimpleName());
                sections.add(section);

                for (String propertyName : propertyBuckets.get(declaringClass)) {
                    addPropertyControl(propertyName, section);
                }
            }
        }
    }

    private void addPropertyControl(String propertyName, Form.Section section) {
        Class<?> propertyType = beanDictionary.getType(propertyName);

        Component inspectorComponent = null;

        if (propertyType == Boolean.TYPE) {
            inspectorComponent = addBooleanControl(propertyName, section);
        } else if (propertyType == Integer.TYPE) {
            inspectorComponent = addIntControl(propertyName, section);
        } else if (propertyType == Float.TYPE) {
            inspectorComponent = addFloatControl(propertyName, section);
        } else if (propertyType == String.class) {
            inspectorComponent = addStringControl(propertyName, section);
        } else if (propertyType.isEnum()) {
            inspectorComponent = addEnumControl(propertyName, section);
        } else if (propertyType == Point.class) {
            inspectorComponent = addPointControl(propertyName, section);
        } else if (propertyType == Dimensions.class) {
            inspectorComponent = addDimensionsControl(propertyName, section);
        } else if (propertyType == Limits.class) {
            inspectorComponent = addLimitsControl(propertyName, section);
        }

        if (inspectorComponent != null) {
            inspectorComponents.put(propertyName, inspectorComponent);
        }
    }

    private Component addBooleanControl(final String propertyName, Form.Section section) {
        boolean propertyValue = (Boolean)beanDictionary.get(propertyName);

        Checkbox checkbox = new Checkbox();
        checkbox.setSelected(propertyValue);
        section.add(checkbox);
        Form.setLabel(checkbox, propertyName);

        checkbox.getButtonStateListeners().add(new ButtonStateListener() {
            private boolean updating = false;

            @Override
            public void stateChanged(Button button, Button.State previousState) {
                if (!updating) {
                    updating = true;
                    try {
                        beanDictionary.put(propertyName, button.isSelected());
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

    private void updateBooleanControl(String propertyName) {
        Checkbox checkbox = (Checkbox)inspectorComponents.get(propertyName);

        if (checkbox != null) {
            boolean propertyValue = (Boolean)beanDictionary.get(propertyName);
            checkbox.setSelected(propertyValue);
        }
    }

    private Component addIntControl(final String propertyName, Form.Section section) {
        int propertyValue = (Integer)beanDictionary.get(propertyName);

        TextInput textInput = new TextInput();
        textInput.setTextSize(10);
        textInput.setMaximumLength(10);
        textInput.setValidator(new IntValidator());
        textInput.setText(String.valueOf(propertyValue));
        section.add(textInput);
        Form.setLabel(textInput, propertyName);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;

                    try {
                        beanDictionary.put(propertyName, Integer.parseInt(textInput.getText()));
                    } catch (Exception exception) {
                        Object propertyValue = beanDictionary.get(propertyName);
                        textInput.setText(String.valueOf(propertyValue));
                    }
                }
            }
        });

        return textInput;
    }

    private void updateIntControl(String propertyName) {
        TextInput textInput = (TextInput)inspectorComponents.get(propertyName);

        if (textInput != null) {
            int propertyValue = (Integer)beanDictionary.get(propertyName);
            textInput.setText(String.valueOf(propertyValue));
        }
    }

    private Component addFloatControl(final String propertyName, Form.Section section) {
        float propertyValue = (Float)beanDictionary.get(propertyName);

        TextInput textInput = new TextInput();
        textInput.setTextSize(10);
        textInput.setValidator(new FloatValidator());
        textInput.setText(String.valueOf(propertyValue));
        section.add(textInput);
        Form.setLabel(textInput, propertyName);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;

                    try {
                        beanDictionary.put(propertyName, Float.parseFloat(textInput.getText()));
                    } catch (Exception exception) {
                        Object propertyValue = beanDictionary.get(propertyName);
                        textInput.setText(String.valueOf(propertyValue));
                    }
                }
            }
        });

        return textInput;
    }

    private void updateFloatControl(String propertyName) {
        TextInput textInput = (TextInput)inspectorComponents.get(propertyName);

        if (textInput != null) {
            float propertyValue = (Float)beanDictionary.get(propertyName);
            textInput.setText(String.valueOf(propertyValue));
        }
    }

    private Component addStringControl(final String propertyName, Form.Section section) {
        String propertyValue = (String)beanDictionary.get(propertyName);

        TextInput textInput = new TextInput();
        textInput.setText(propertyValue == null ? "" : propertyValue);
        section.add(textInput);
        Form.setLabel(textInput, propertyName);

        textInput.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (!component.isFocused()) {
                    TextInput textInput = (TextInput)component;

                    try {
                        beanDictionary.put(propertyName, textInput.getText());
                    } catch (Exception exception) {
                        String propertyValue = (String)beanDictionary.get(propertyName);
                        textInput.setText(propertyValue == null ? "" : propertyValue);
                    }
                }
            }
        });

        return textInput;
    }

    private void updateStringControl(String propertyName) {
        TextInput textInput = (TextInput)inspectorComponents.get(propertyName);

        if (textInput != null) {
            String propertyValue = (String)beanDictionary.get(propertyName);
            textInput.setText(propertyValue == null ? "" : propertyValue);
        }
    }

    @SuppressWarnings("unchecked")
    private Component addEnumControl(final String propertyName, Form.Section section) {
        Class<?> propertyType = beanDictionary.getType(propertyName);
        Enum<?> propertyValue = (Enum<?>)beanDictionary.get(propertyName);

        ListButton listButton = new ListButton();
        listButton.setListData(new EnumList(propertyType));
        listButton.setSelectedItem(propertyValue);
        section.add(listButton);
        Form.setLabel(listButton, propertyName);

        listButton.getListButtonSelectionListeners().add(new ListButtonSelectionListener() {
            private boolean updating = false;

            @Override
            public void selectedIndexChanged(ListButton listButton, int previousSelectedIndex) {
                if (!updating) {
                    updating = true;
                    try {
                        beanDictionary.put(propertyName, listButton.getSelectedItem());
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

    private void updateEnumControl(String propertyName) {
        ListButton listButton = (ListButton)inspectorComponents.get(propertyName);

        if (listButton != null) {
            Enum<?> propertyValue = (Enum<?>)beanDictionary.get(propertyName);
            listButton.setSelectedItem(propertyValue);
        }
    }

    private Component addPointControl(final String propertyName, Form.Section section) {
        Point point = (Point)beanDictionary.get(propertyName);

        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);
        section.add(boxPane);
        Form.setLabel(boxPane, propertyName);

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
                    Point point = (Point)beanDictionary.get(propertyName);

                    try {
                        int x = Integer.parseInt(textInput.getText());
                        beanDictionary.put(propertyName, new Point(x, point.y));
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
                    Point point = (Point)beanDictionary.get(propertyName);

                    try {
                        int y = Integer.parseInt(textInput.getText());
                        beanDictionary.put(propertyName, new Point(point.x, y));
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

    private void updatePointControl(String propertyName) {
        BoxPane boxPane = (BoxPane)inspectorComponents.get(propertyName);

        if (boxPane != null) {
            Point point = (Point)beanDictionary.get(propertyName);

            TextInput xTextInput = (TextInput)((FlowPane)boxPane.get(0)).get(0);
            TextInput yTextInput = (TextInput)((FlowPane)boxPane.get(1)).get(0);

            xTextInput.setText(String.valueOf(point.x));
            yTextInput.setText(String.valueOf(point.y));
        }
    }

    private Component addDimensionsControl(final String propertyName, Form.Section section) {
        Dimensions dimensions = (Dimensions)beanDictionary.get(propertyName);

        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);
        section.add(boxPane);
        Form.setLabel(boxPane, propertyName);

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
                    Dimensions dimensions = (Dimensions)beanDictionary.get(propertyName);

                    try {
                        int width = Integer.parseInt(textInput.getText());
                        beanDictionary.put(propertyName, new Dimensions(width, dimensions.height));
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
                    Dimensions dimensions = (Dimensions)beanDictionary.get(propertyName);

                    try {
                        int height = Integer.parseInt(textInput.getText());
                        beanDictionary.put(propertyName, new Dimensions(dimensions.width, height));
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

    private void updateDimensionsControl(String propertyName) {
        BoxPane boxPane = (BoxPane)inspectorComponents.get(propertyName);

        if (boxPane != null) {
            Dimensions dimensions = (Dimensions)beanDictionary.get(propertyName);

            TextInput widthTextInput = (TextInput)((FlowPane)boxPane.get(0)).get(0);
            TextInput heightTextInput = (TextInput)((FlowPane)boxPane.get(1)).get(0);

            widthTextInput.setText(String.valueOf(dimensions.width));
            heightTextInput.setText(String.valueOf(dimensions.height));
        }
    }

    private Component addLimitsControl(final String propertyName, Form.Section section) {
        Limits limits = (Limits)beanDictionary.get(propertyName);

        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);
        section.add(boxPane);
        Form.setLabel(boxPane, propertyName);

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
                    Limits limits = (Limits)beanDictionary.get(propertyName);

                    try {
                        int min = Integer.parseInt(textInput.getText());
                        beanDictionary.put(propertyName, new Limits(min, limits.max));
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
                    Limits limits = (Limits)beanDictionary.get(propertyName);

                    try {
                        int max = Integer.parseInt(textInput.getText());
                        beanDictionary.put(propertyName, new Limits(limits.min, max));
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

    private void updateLimitsControl(String propertyName) {
        BoxPane boxPane = (BoxPane)inspectorComponents.get(propertyName);

        if (boxPane != null) {
            Limits limits = (Limits)beanDictionary.get(propertyName);

            TextInput minTextInput = (TextInput)((FlowPane)boxPane.get(0)).get(0);
            TextInput maxTextInput = (TextInput)((FlowPane)boxPane.get(1)).get(0);

            minTextInput.setText(String.valueOf(limits.min));
            maxTextInput.setText(String.valueOf(limits.max));
        }
    }
}
