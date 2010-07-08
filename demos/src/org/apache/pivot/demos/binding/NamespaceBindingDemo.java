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
package org.apache.pivot.demos.binding;

import java.net.URL;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.beans.NamespaceBinding;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Window;

public class NamespaceBindingDemo extends Window implements Bindable {
    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        // One-way binding
        NamespaceBinding textInputToLabelBinding = new NamespaceBinding(namespace,
            "textInput.text", "label.text");
        textInputToLabelBinding.bind();

        // Two-way binding
        NamespaceBinding textInput1ToTextInput2Binding = new NamespaceBinding(namespace,
            "textInput1.text", "textInput2.text");
        textInput1ToTextInput2Binding.bind();

        NamespaceBinding textInput2ToTextInput1Binding = new NamespaceBinding(namespace,
            "textInput2.text", "textInput1.text");
        textInput2ToTextInput1Binding.bind();

        // Style binding
        NamespaceBinding colorChooserButtonToLabelTextBinding = new NamespaceBinding(namespace,
            "colorChooserButton.selectedColor", "colorLabel.text");
        colorChooserButtonToLabelTextBinding.bind();

        NamespaceBinding colorChooserButtonToLabelColorBinding = new NamespaceBinding(namespace,
            "colorChooserButton.selectedColor", "colorLabel.styles['color']");
        colorChooserButtonToLabelColorBinding.bind();
    }
}
