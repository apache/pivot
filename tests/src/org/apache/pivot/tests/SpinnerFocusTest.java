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
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.Spinner;

public final class SpinnerFocusTest implements Application {
    private Frame frame = null;
    private Spinner spinner = null;

    @Override
    public void startup(final Display display, final Map<String, String> properties) throws Exception {
        Action action = new Action() {
            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(final Component source) {
                String msg = "Selected: " + spinner.getSelectedItem().toString();
                Alert.alert(msg, frame);

                spinner.requestFocus();
                System.out.println("Focus transferred to spinner");
            }
        };

        Action.getNamedActions().put("buttonAction", action);

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        frame = new Frame((Component) bxmlSerializer.readObject(getClass().getResource(
            "spinner_focus_test.bxml")));
        frame.setTitle("Spinner Focus Test");
        frame.open(display);

        spinner = (Spinner) bxmlSerializer.getNamespace().get("spinner");
        spinner.requestFocus();

        // action.setEnabled(false);
    }

    @Override
    public boolean shutdown(final boolean optional) {
        if (frame != null) {
            frame.close();
        }

        return false;
    }

    public static void main(final String[] args) {
        DesktopApplicationContext.main(SpinnerFocusTest.class, args);
    }
}
