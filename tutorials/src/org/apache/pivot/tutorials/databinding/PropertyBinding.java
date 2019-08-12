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
package org.apache.pivot.tutorials.databinding;

import java.awt.Color;
import java.net.URL;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.beans.NamespaceBinding;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.util.ColorUtilities;

public class PropertyBinding extends Window implements Bindable {
    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        // Bind list button selection to label text
        NamespaceBinding namespaceBinding1 = new NamespaceBinding(namespace,
            "listButton.selectedItem", "listButtonLabel1.text");

        namespaceBinding1.bind();

        // Bind list button selection to label text with bind mapping
        NamespaceBinding namespaceBinding2 = new NamespaceBinding(namespace,
            "listButton.selectedItem", "listButtonLabel2.text", new NamespaceBinding.BindMapping() {
                @Override
                public Object evaluate(Object value) {
                    return value.toString().toUpperCase();
                }
            });

        namespaceBinding2.bind();
    }

    public static String toHex(Color color) {
        return ColorUtilities.toStringValue(color);
    }
}
