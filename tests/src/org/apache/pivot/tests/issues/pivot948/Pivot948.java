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
package org.apache.pivot.tests.issues.pivot948;

import org.apache.pivot.beans.*;
import org.apache.pivot.collections.*;
import org.apache.pivot.wtk.*;

public class Pivot948 implements Application, ButtonPressListener, ListView.ItemBindMapping {
    private Window window = null;
    @BXML private ListButton inputList;
    @BXML private PushButton loadButton;
    @BXML private PushButton storeButton;
    @BXML private TextInput outputResult;
    private Integer listIndex = null;

    /** Method of the {@link Application.Adapter} interface. */
    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window)bxmlSerializer.readObject(this.getClass(), "pivot_948.bxml");
        bxmlSerializer.bind(this);

        loadButton.getButtonPressListeners().add(this);
        storeButton.getButtonPressListeners().add(this);

        // Establish a "selected item binding" between the "listIndex" integer and the
        // "inputList" selected item index.  A null integer implies no selection
        // (index of -1).
        inputList.setSelectedItemBindMapping(this);
        inputList.setSelectedItemBindType(BindType.BOTH);
        inputList.setSelectedItemKey("listIndex");

        buttonPressed(storeButton);

        window.open(display);
    }

    /** Method of the {@link Application.Adapter} interface. */
    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    /**
     * @return The list index as an integer (call be {@code null}).
     */
    public Integer getListIndex() {
        return listIndex;
    }

    /**
     * Set the current list index to the new value.
     * @param newIndex The new index value (which can be {@code null}).
     */
    public void setListIndex(Integer newIndex) {
        listIndex = newIndex;
    }

    /** Method of the {@link ListView.ItemBindMapping} interface, called during "store". */
    @Override
    public Object get(List<?> listData, int index) {
        return Integer.valueOf(index);
    }

    /** Method of the {@link ListView.ItemBindMapping} interface, called during "load". */
    @Override
    public int indexOf(List<?> listData, Object value) {
        if (value != null) {
            Integer iVal = (Integer)value;
            return iVal.intValue();
        }
        // Null item implies nothing selected, so return -1 as the index
        return -1;
    }

    /** Method of the {@link ButtonPressListener} interface. */
    @Override
    public void buttonPressed(Button button) {
        if (button == loadButton) {
            String text = outputResult.getText();
            if (text == null || text.trim().isEmpty()) {
                listIndex = null;
            }
            else {
                listIndex = Integer.valueOf(text);
            }
            inputList.load(this);
        }
        else if (button == storeButton) {
            inputList.store(this);
            outputResult.setText(listIndex == null ? "" : listIndex.toString());
        }
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Pivot948.class, args);
    }

}
