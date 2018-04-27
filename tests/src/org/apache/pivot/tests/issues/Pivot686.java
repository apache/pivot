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
package org.apache.pivot.tests.issues;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Window;

public class Pivot686 implements Application {
    private Window window = null;
    private TextInput textInput = null;
    private PushButton pushButton = null;

    public static final String FORCE_FOCUS_KEY = "forceFocus";

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        System.out.println("startup: start");

        String forceFocusParameter = properties.get(FORCE_FOCUS_KEY);
        boolean forceFocus = false;
        forceFocus = Boolean.parseBoolean(forceFocusParameter);

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(this.getClass(), "pivot_686.bxml");
        initializeFields(bxmlSerializer);
        window.open(display);

        if (forceFocus) {
            System.out.println("force focus on textInput now");
            textInput.requestFocus(); // force focus on TextInput, Ok when run
                                      // as a Java Application, and even as
                                      // Applet
        }
        System.out.println("textInput has focus: " + textInput.isFocused());

        System.out.println("startup: end");
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    private void initializeFields(BXMLSerializer serializer) {
        System.out.println("initializeFields: start");

        textInput = (TextInput) serializer.getNamespace().get("textInput");
        textInput.requestFocus(); // note that this has no effect here
        System.out.println("textInput has focus: " + textInput.isFocused());

        pushButton = (PushButton) serializer.getNamespace().get("pushButton");
        pushButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                String msg = "You clicked me!";
                System.out.println(msg);
                // Alert.alert(MessageType.INFO, msg, window);

                textInput.setText("");
                textInput.requestFocus();
            }
        });

        System.out.println("initializeFields: end");
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Pivot686.class, args);
    }

}
