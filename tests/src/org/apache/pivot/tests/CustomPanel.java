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

import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Window;

/**
 * Test Component to ensure that BXMLSerializer will use public setter methods
 * instead of setting private fields directly (possible in trusted code when run
 * in a sandboxed environment, like in not trusted applets).
 */
public class CustomPanel extends Window implements Bindable {

    @BXML
    private String field;
    @BXML
    private TextInput textInput = null;

    public CustomPanel() {
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public void setTextInput(TextInput textInput) {
        this.textInput = textInput;
    }

    public TextInput getTextInput() {
        return textInput;
    }

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        System.out.println("initialize - start");

        System.out.println("Testing Java Security ...");
        TestUtils.testJavaSecurity();

        System.out.println("field = \"" + getField() + "\"");
        System.out.println("textInput.text = \"" + getTextInput().getText() + "\"");

        System.out.println("initialize - end");
    }

}
