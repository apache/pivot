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
package pivot.tutorials.databinding;

import java.io.InputStream;

import pivot.collections.Dictionary;
import pivot.collections.Map;
import pivot.serialization.JSONSerializer;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.Form;
import pivot.wtk.Label;
import pivot.wtk.PushButton;
import pivot.wtk.Window;
import pivot.wtkx.Bindable;

public class DataBinding extends Bindable implements Application {
    @Load(resourceName="data_binding.wtkx") private Window window;
    @Bind(fieldName="window") private Form form;
    @Bind(fieldName="window") private PushButton loadJavaButton;
    @Bind(fieldName="window") private PushButton loadJSONButton;
    @Bind(fieldName="window") private PushButton clearButton;
    @Bind(fieldName="window") private Label sourceLabel;

    private static final Contact CONTACT = new Contact("101", "Joe Smith",
        new Address("123 Main St.", "Cambridge", "MA", "02142"),
        "(617) 555-1234", "joe_smith@foo.com",
        new IMAccount("jsmith1234", "AIM"));

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        bind();

        loadJavaButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                form.load(CONTACT);
                sourceLabel.setText("Java");
            }
        });

        loadJSONButton.getButtonPressListeners().add(new ButtonPressListener() {
            @SuppressWarnings("unchecked")
            public void buttonPressed(Button button) {
                JSONSerializer serializer = new JSONSerializer();
                InputStream inputStream = getClass().getResourceAsStream("contact.json");

                try {
                    form.load((Map<String, Object>)serializer.readObject(inputStream));
                    sourceLabel.setText("JSON");
                } catch(Exception exception) {
                    System.out.println(exception);
                }

                button.setEnabled(true);
            }
        });

        clearButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                form.load(new Contact());
                sourceLabel.setText(null);
            }
        });

        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }


    public void suspend() {
    }

    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(DataBinding.class, args);
    }
}
