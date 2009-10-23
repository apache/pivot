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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Comparator;

import org.apache.pivot.beans.BeanDictionary;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.EnumList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonStateListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.FlowPane;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Limits;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Rollup;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.skin.ContainerSkin;
import org.apache.pivot.wtk.text.validation.IntValidator;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

class ComponentInspectorSkin extends ContainerSkin implements ComponentInspectorListener {
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

    private Component content = null;

    @WTKX private BoxPane propertiesPane = null;
    @WTKX private BoxPane stylesPane = null;

    private static PropertyNameComparator propertyNameComparator = new PropertyNameComparator();
    private static PropertySourceTypeComparator propertySourceTypeComparator =
        new PropertySourceTypeComparator();

    @Override
    public void install(Component component) {
        super.install(component);

        ComponentInspector componentInspector = (ComponentInspector)component;

        componentInspector.getComponentInspectorListeners().add(this);

        Resources resources;
        try {
            resources = new Resources(getClass().getName());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        WTKXSerializer wtkxSerializer = new WTKXSerializer(resources);
        try {
            content = (Component)wtkxSerializer.readObject(this, "component_inspector_skin.wtkx");
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        componentInspector.add(content);

        wtkxSerializer.bind(this, ComponentInspectorSkin.class);

        sourceChanged(componentInspector, null);
    }

    @Override
    public int getPreferredWidth(int height) {
        return content.getPreferredWidth(height);
    }

    @Override
    public int getPreferredHeight(int width) {
        return content.getPreferredHeight(width);
    }

    @Override
    public Dimensions getPreferredSize() {
        return content.getPreferredSize();
    }

    @Override
    public void layout() {
        content.setLocation(0, 0);
        content.setSize(getWidth(), getHeight());
    }

    @Override
    public void sourceChanged(ComponentInspector componentInspector, Component previousSource) {
        propertiesPane.remove(0, propertiesPane.getLength());
        stylesPane.remove(0, stylesPane.getLength());

        Component source = componentInspector.getSource();

        if (source != null) {
            Class<?> sourceType = source.getClass();
            BeanDictionary beanDictionary = new BeanDictionary(source);

            Map<Class<?>, List<String>> propertyBuckets =
                new HashMap<Class<?>, List<String>>(propertySourceTypeComparator);

            for (String propertyName : beanDictionary) {
                boolean readOnly = beanDictionary.isReadOnly(propertyName);

                if (!readOnly) {
                    // TODO?
                    // Class<?> propertyType = beanDictionary.getType(propertyName);

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
                Rollup rollup = new Rollup();
                propertiesPane.add(rollup);
                Label label = new Label(declaringClass.getSimpleName());
                label.getStyles().put("color", 16);
                label.getStyles().put("font", "{bold:true}");
                rollup.setHeading(label);

                Form form = new Form();
                form.getStyles().put("rightAlignLabels", true);
                Form.Section section = new Form.Section();
                form.getSections().add(section);
                rollup.setContent(form);

                for (String propertyName : propertyBuckets.get(declaringClass)) {
                    addPropertyControl(propertyName, section, beanDictionary);
                }
            }
        }
    }

    private void addPropertyControl(String propertyName, Form.Section section,
        BeanDictionary beanDictionary) {
        Class<?> propertyType = beanDictionary.getType(propertyName);

        if (propertyType == Boolean.TYPE) {
            addBooleanControl(propertyName, section, beanDictionary);
        } else if (propertyType == Integer.TYPE) {
            addIntControl(propertyName, section, beanDictionary);
        } else if (propertyType.isEnum()) {
            addEnumControl(propertyName, section, beanDictionary);
        } else if (propertyType == Point.class) {
            addPointControl(propertyName, section, beanDictionary);
        } else if (propertyType == Dimensions.class) {
            addDimensionsControl(propertyName, section, beanDictionary);
        } else if (propertyType == Limits.class) {
            addLimitsControl(propertyName, section, beanDictionary);
        }
    }

    private void addBooleanControl(final String propertyName, Form.Section section,
        final BeanDictionary beanDictionary) {
        boolean propertyValue = (Boolean)beanDictionary.get(propertyName);

        Checkbox checkbox = new Checkbox();
        checkbox.setSelected(propertyValue);
        section.add(checkbox);
        Form.setLabel(checkbox, propertyName);

        checkbox.getButtonStateListeners().add(new ButtonStateListener() {
            @Override
            public void stateChanged(Button button, Button.State previousState) {
                beanDictionary.put(propertyName, button.isSelected());
            }
        });
    }

    private void addIntControl(final String propertyName, Form.Section section,
        final BeanDictionary beanDictionary) {
        /*
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
                TextInput textInput = (TextInput)component;
                try {
                    beanDictionary.put(propertyName, Integer.parseInt(textInput.getText()));
                } catch (Exception exception) {
                    Object propertyValue = beanDictionary.get(propertyName);
                    textInput.setText(String.valueOf(propertyValue));
                }
            }
        });
        */
    }

    @SuppressWarnings("unchecked")
    private void addEnumControl(final String propertyName, Form.Section section,
        final BeanDictionary beanDictionary) {
        Class<?> propertyType = beanDictionary.getType(propertyName);
        Enum<?> propertyValue = (Enum<?>)beanDictionary.get(propertyName);

        ListButton listButton = new ListButton();
        listButton.setListData(new EnumList(propertyType));
        listButton.setSelectedItem(propertyValue);
        section.add(listButton);
        Form.setLabel(listButton, propertyName);

        listButton.getListButtonSelectionListeners().add(new ListButtonSelectionListener() {
            @Override
            public void selectedIndexChanged(ListButton listButton, int previousSelectedIndex) {
                beanDictionary.put(propertyName, listButton.getSelectedItem());
            }
        });
    }

    private void addPointControl(final String propertyName, Form.Section section,
        final BeanDictionary beanDictionary) {
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

        label = new Label("y");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);
    }

    private void addDimensionsControl(final String propertyName, Form.Section section,
        final BeanDictionary beanDictionary) {
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

        label = new Label("height");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);
    }

    private void addLimitsControl(final String propertyName, Form.Section section,
        final BeanDictionary beanDictionary) {
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

        label = new Label("max");
        label.getStyles().put("font", "{italic:true}");
        flowPane.add(label);
    }
}
