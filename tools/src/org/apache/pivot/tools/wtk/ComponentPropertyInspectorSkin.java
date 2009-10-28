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
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Form;

class ComponentPropertyInspectorSkin extends ComponentInspectorSkin {
    private static class NameComparator implements Comparator<String> {
        @Override
        public int compare(String propertyName1, String propertyName2) {
            return propertyName1.compareTo(propertyName2);
        }
    }

    private static class ClassComparator implements Comparator<Class<?>> {
        @Override
        public int compare(Class<?> class1, Class<?> class2) {
            int result = 0;

            if (class1.isAssignableFrom(class2)) {
                result = 1;
            } else if (class2.isAssignableFrom(class1)) {
                result = -1;
            } else {
                result = class1.getName().compareTo(class2.getName());
            }

            return result;
        }
    }

    private BeanDictionary beanDictionary = new BeanDictionary();

    private static NameComparator nameComparator = new NameComparator();
    private static ClassComparator classComparator = new ClassComparator();

    public ComponentPropertyInspectorSkin() {
        beanDictionary.getBeanDictionaryListeners().add(new BeanDictionaryListener.Adapter() {
            @Override
            public void propertyChanged(BeanDictionary beanDictionary, String propertyName) {
                Class<?> type = beanDictionary.getType(propertyName);
                updateControl(beanDictionary, propertyName, type);
            }
        });

        form.getStyles().put("showFirstSectionHeading", true);
    }

    @Override
    public void sourceChanged(ComponentInspector componentInspector, Component previousSource) {
        Component source = componentInspector.getSource();

        clearControls();
        Form.SectionSequence sections = form.getSections();
        sections.remove(0, sections.getLength());

        beanDictionary.setBean(source);

        if (source != null) {
            Class<?> sourceType = source.getClass();
            HashMap<Class<?>, List<String>> declaringClassPartitions =
                new HashMap<Class<?>, List<String>>(classComparator);

            // Partition the properties by their declaring class
            for (String propertyName : beanDictionary) {
                if (beanDictionary.isNotifying(propertyName)
                    && !beanDictionary.isReadOnly(propertyName)) {
                    Method method = BeanDictionary.getGetterMethod(sourceType, propertyName);
                    Class<?> declaringClass = method.getDeclaringClass();

                    List<String> propertyNames = declaringClassPartitions.get(declaringClass);
                    if (propertyNames == null) {
                        propertyNames = new ArrayList<String>(nameComparator);
                        declaringClassPartitions.put(declaringClass, propertyNames);
                    }

                    propertyNames.add(propertyName);
                }
            }

            // Add the controls
            for (Class<?> declaringClass : declaringClassPartitions) {
                Form.Section section = new Form.Section();
                section.setHeading(declaringClass.getSimpleName());
                sections.add(section);

                List<String> propertyNames = declaringClassPartitions.get(declaringClass);
                for (String propertyName : propertyNames) {
                    Class<?> type = beanDictionary.getType(propertyName);
                    addControl(beanDictionary, propertyName, type, section);
                }
            }
        }
    }
}
