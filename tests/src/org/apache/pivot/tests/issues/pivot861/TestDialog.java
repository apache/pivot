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
package org.apache.pivot.tests.issues.pivot861;

import java.io.IOException;
import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.PushButton;

public class TestDialog extends Dialog implements Bindable {

    private static final String DIALOG_MARKUP_FILE = "/org/apache/pivot/tests/issues/pivot861/test_dialog.bxml";

    @BXML
    private PushButton okButton;

    public static TestDialog create() {
        System.out.println("TestDialog create()");
        TestDialog dialog = null;
        try {
            BXMLSerializer bxmlSerializer = new BXMLSerializer();
            dialog = (TestDialog) bxmlSerializer.readObject(TestDialog.class, DIALOG_MARKUP_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SerializationException e) {
            e.printStackTrace();
        }
        return dialog;
    }

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        System.out.println("TestDialog initialize(...)");

        this.okButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                TestDialog.this.close(true);
            }
        });

    }

}
