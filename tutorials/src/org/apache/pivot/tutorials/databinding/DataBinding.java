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

import java.io.InputStream;
import java.net.URL;

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Window;

public class DataBinding extends Window implements Bindable {
    private Form form = null;
    private PushButton loadJavaButton = null;
    private PushButton loadJSONButton = null;
    private PushButton clearButton = null;
    private Label sourceLabel = null;

    private static final Contact CONTACT = new Contact("101", "Joe User", new Address(
        "123 Main St.", "Cambridge", "MA", "02142"), "(617) 555-1234", "joe_user@foo.com",
        new IMAccount("juser1234", "AIM"));

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        form = (Form) namespace.get("form");
        loadJavaButton = (PushButton) namespace.get("loadJavaButton");
        loadJSONButton = (PushButton) namespace.get("loadJSONButton");
        clearButton = (PushButton) namespace.get("clearButton");
        sourceLabel = (Label) namespace.get("sourceLabel");

        loadJavaButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                form.load(new BeanAdapter(CONTACT));
                sourceLabel.setText("Java");
            }
        });

        loadJSONButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                JSONSerializer serializer = new JSONSerializer();

                try (InputStream inputStream = getClass().getResourceAsStream("contact.json")) {
                    form.load(serializer.readObject(inputStream));
                    sourceLabel.setText("JSON");
                } catch (Exception exception) {
                    System.err.println(exception);
                }

                button.setEnabled(true);
            }
        });

        clearButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                form.clear();
                sourceLabel.setText("");
            }
        });
    }
}
