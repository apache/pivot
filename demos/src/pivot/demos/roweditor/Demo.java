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
package pivot.demos.roweditor;

import pivot.collections.Dictionary;
import pivot.collections.EnumList;
import pivot.wtk.Application;
import pivot.wtk.Display;
import pivot.wtk.ListButton;
import pivot.wtk.Spinner;
import pivot.wtk.TableView;
import pivot.wtk.TextInput;
import pivot.wtk.Window;
import pivot.wtk.content.CalendarDateSpinnerData;
import pivot.wtk.content.TableViewRowEditor;
import pivot.wtkx.WTKXSerializer;

/**
 * Demonstrates a flip transition used to initiate a table view row editor.
 *
 * @author tvolkert
 */
public class Demo implements Application {
    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties) throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(getClass().getResource("demo.wtkx"));
        window.open(display);

        TableView tableView = (TableView)wtkxSerializer.getObjectByName("tableView");
        TableViewRowEditor tableViewRowEditor = new TableViewRowEditor();
        tableView.setRowEditor(tableViewRowEditor);

        // Date uses a Spinner with a CalendarDateSpinnerData model
        Spinner dateSpinner = new Spinner(new CalendarDateSpinnerData());
        dateSpinner.setSelectedItemKey("date");
        tableViewRowEditor.getCellEditors().put("date", dateSpinner);

        // Expense type uses a ListButton that presents the expense types
        ListButton typeListButton = new ListButton(new EnumList<ExpenseType>(ExpenseType.class));
        typeListButton.setSelectedItemKey("type");
        tableViewRowEditor.getCellEditors().put("type", typeListButton);

        // Amount uses a TextInput with strict currency validation
        TextInput amountTextInput = new TextInput();
        amountTextInput.setValidator(new CurrencyValidator());
        amountTextInput.getStyles().put("strictValidation", true);
        amountTextInput.setTextKey("amount");
        tableViewRowEditor.getCellEditors().put("amount", amountTextInput);
    }

    public boolean shutdown(boolean optional) throws Exception {
        if (window != null) {
            window.close();
        }

        window = null;
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
