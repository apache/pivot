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
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.TableViewRowEditor;
import org.apache.pivot.wtk.skin.CardPaneSkin;

public final class TableViewTest2 implements Application {
    private Window window = null;
    private TableView tableView = null;
    private Window menu = null;

    @Override
    public void startup(final Display display, final Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();

        System.out.println("Double Click on Table elements to open the Row Editor");

        window = (Window) bxmlSerializer.readObject(TableViewTest2.class, "table_view_test2.bxml");
        tableView = (TableView) bxmlSerializer.getNamespace().get("tableView");
        menu = (Window) bxmlSerializer.readObject(TableViewTest2.class, "context_menus.bxml");

        tableView.setMenuHandler(new ContextMenusSampleMenuHandlerAdapter());
        System.out.println("Right  Click on Table elements to display Contextual Menu: " + menu);

        TableViewRowEditor tableViewRowEditor = new TableViewRowEditor();
        tableViewRowEditor.setEditEffect(CardPaneSkin.SelectionChangeEffect.HORIZONTAL_SLIDE);
        tableView.setRowEditor(tableViewRowEditor);

        TextArea textArea = new TextArea();
        textArea.setTextKey("value");
        tableViewRowEditor.getCellEditors().put("value", textArea);

        window.open(display);
    }

    @Override
    public boolean shutdown(final boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public static void main(final String[] args) {
        DesktopApplicationContext.main(TableViewTest2.class, args);
    }
}
