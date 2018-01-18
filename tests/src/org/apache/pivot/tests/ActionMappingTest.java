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
package org.apache.pivot.tests;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Window;

public class ActionMappingTest implements Application {
    private Window window = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        Action.getNamedActions().put("action1", new Action() {
            @Override
            public void perform(Component source) {
                Alert.alert(MessageType.INFO, "Action 1 performed.", window);
            }
        });

        Action.getNamedActions().put("action2", new Action() {
            @Override
            public void perform(Component source) {
                Alert.alert(MessageType.INFO, "Action 2 performed.", window);
            }
        });

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(ActionMappingTest.class,
            "action_mapping_test.bxml");
        window.getActionMappings().add(
            new Window.ActionMapping(new Keyboard.KeyStroke(Keyboard.KeyCode.B,
                Keyboard.Modifier.SHIFT.getMask()), "action2"));

        window.open(display);
        window.requestFocus();
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(ActionMappingTest.class, args);
    }

}
