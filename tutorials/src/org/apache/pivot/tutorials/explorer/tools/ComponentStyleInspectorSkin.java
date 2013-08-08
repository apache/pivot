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
package org.apache.pivot.tutorials.explorer.tools;

import java.util.Comparator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentStyleListener;
import org.apache.pivot.wtk.Form;

class ComponentStyleInspectorSkin extends ComponentInspectorSkin {
    private ComponentStyleListener componentStyleHandler = new ComponentStyleListener() {
        @Override
        public void styleUpdated(Component component, String key, Object previousValue) {
            Component.StyleDictionary styles = component.getStyles();
            updateControl(styles, key, styles.getType(key));
        }
    };

    private Form.Section stylesSection = new Form.Section();

    public ComponentStyleInspectorSkin() {
        form.getSections().add(stylesSection);
    }

    @Override
    public void sourceChanged(ComponentInspector componentInspector, Component previousSource) {
        Component source = componentInspector.getSource();

        clearControls();

        if (previousSource != null) {
            previousSource.getComponentStyleListeners().remove(componentStyleHandler);
        }

        if (source != null) {
            source.getComponentStyleListeners().add(componentStyleHandler);

            Component.StyleDictionary styles = source.getStyles();

            ArrayList<String> keys = new ArrayList<>(new Comparator<String>() {
                @Override
                public int compare(String key1, String key2) {
                    return key1.compareTo(key2);
                }
            });

            // Filter (exclude read-only) and sort the keys
            for (String key : styles) {
                if (!styles.isReadOnly(key)) {
                    keys.add(key);
                }
            }

            // Add the controls
            for (String key : keys) {
                addControl(styles, key, styles.getType(key), stylesSection);
            }
        }
    }
}
