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
package org.apache.pivot.examples.sheets;

import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Window;

/**
 * Edit greeting sheet.
 */
public class EditGreetingSheet extends Sheet implements Bindable {
    @BXML
    private Form form = null;
    @BXML
    private TextInput greetingTextInput = null;
    @BXML
    private PushButton cancelButton = null;
    @BXML
    private PushButton okButton = null;

    private Resources resources = null;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resourcesArgument) {
        this.resources = resourcesArgument;

        cancelButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                close(false);
            }
        });

        okButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                close(true);
            }
        });
    }

    @Override
    public void open(Display display, Window owner, SheetCloseListener sheetCloseListener) {
        super.open(display, owner, sheetCloseListener);

        greetingTextInput.selectAll();
    }

    @Override
    public void close(boolean result) {
        form.clearFlags();

        if (result) {
            if (greetingTextInput.getCharacterCount() == 0) {
                Form.setFlag(greetingTextInput,
                    new Form.Flag(MessageType.ERROR, (String) resources.get("greetingRequired")));
            }
        }

        if (form.getFlaggedFieldCount(MessageType.ERROR) == 0) {
            super.close(result);
        }
    }

}
