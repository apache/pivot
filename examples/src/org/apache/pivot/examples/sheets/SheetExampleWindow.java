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
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.Window;

/**
 * Main window.
 */
public class SheetExampleWindow extends Window implements Bindable {
    @BXML
    EditGreetingSheet editGreetingSheet = null;
    @BXML
    private PushButton editGreetingButton = null;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        editGreetingButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                HashMap<String, Object> context = new HashMap<>();
                store(context);
                editGreetingSheet.load(context);

                editGreetingSheet.open(SheetExampleWindow.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            HashMap<String, Object> contextLocal = new HashMap<>();
                            editGreetingSheet.store(contextLocal);
                            load(contextLocal);
                        }
                    }
                });
            }
        });
    }

}
