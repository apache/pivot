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

import java.util.Date;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.CalendarButton;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.Window;

public class Pivot694 implements Application {
    private Window window = null;

    // TODO: for release 2.1 ... maybe generalizing some test here
    // private ButtonGroup pushButtons = null;
    // private ButtonGroup toggleButtons = null;
    // private ButtonGroup radioButtons = null;
    // private ButtonGroup checkButtons = null;
    // private ButtonGroup checkTriButtons = null;

    private CalendarButton calendarButton1 = null;
    private CalendarButton calendarButton2 = null;
    private Spinner spinner1 = null;
    private Spinner spinner2 = null;

    private ListView listView1 = null;
    private ListView listView2 = null;
    private TableView tableView1 = null;
    private TableView tableView2 = null;
    private TreeView treeView1 = null;
    private TreeView treeView2 = null;

    private ListButton listButton1 = null;
    private ListButton listButton2 = null;

    private PushButton clearSelectionButton = null;
    private PushButton clearButton = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        System.out.println("startup: start");

        System.out.println("Test for clearing (selection) in many Components");

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(this.getClass(), "pivot_694.bxml");
        initializeFields(bxmlSerializer);
        window.open(display);

        System.out.println("Note that elements to clear selection, "
            + " are to improve Pivot on this feature in a next release, so now only some components will support it");
        System.out.println("Note that elements to clear (data) inside, will be emptied"
            + " only if they have a buttonDataKey/listDataKey/tableDataKey/treeDataKey/spinnerDataKey/xxxDataKey"
            + " property set at creation time");

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

        // TODO: for release 2.1 ... maybe generalizing some test here
        // pushButtons =
        // (ButtonGroup)serializer.getNamespace().get("pushButtons");
        // toggleButtons =
        // (ButtonGroup)serializer.getNamespace().get("toggleButtons");
        // radioButtons =
        // (ButtonGroup)serializer.getNamespace().get("radioButtons");
        // checkButtons =
        // (ButtonGroup)serializer.getNamespace().get("checkButtons");
        // checkTriButtons =
        // (ButtonGroup)serializer.getNamespace().get("checkTriButtons");

        calendarButton1 = (CalendarButton) serializer.getNamespace().get("calendarButton1");
        calendarButton2 = (CalendarButton) serializer.getNamespace().get("calendarButton2");
        spinner1 = (Spinner) serializer.getNamespace().get("spinner1");
        spinner2 = (Spinner) serializer.getNamespace().get("spinner2");

        listView1 = (ListView) serializer.getNamespace().get("listView1");
        listView2 = (ListView) serializer.getNamespace().get("listView2");
        tableView1 = (TableView) serializer.getNamespace().get("tableView1");
        tableView2 = (TableView) serializer.getNamespace().get("tableView2");
        treeView1 = (TreeView) serializer.getNamespace().get("treeView1");
        treeView2 = (TreeView) serializer.getNamespace().get("treeView2");

        listButton1 = (ListButton) serializer.getNamespace().get("listButton1");
        listButton2 = (ListButton) serializer.getNamespace().get("listButton2");

        clearSelectionButton = (PushButton) serializer.getNamespace().get("clearSelectionButton");
        clearSelectionButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                System.out.println("Clearing selection from " + button.getName() + " at "
                    + new Date());

                // TODO: for release 2.1 ... maybe generalizing some test here
                // pushButtons.clearSelection();
                // toggleButtons.clearSelection();
                // radioButtons.clearSelection();
                // checkButtons.clearSelection();
                // checkTriButtons.clearSelection();

                calendarButton1.clearSelection();
                calendarButton2.clearSelection();

                spinner1.clearSelection();
                spinner2.clearSelection();

                listButton1.clearSelection();
                listButton2.clearSelection();

                listView1.clearSelection();
                listView2.clearSelection();
                tableView1.clearSelection();
                tableView2.clearSelection();
                treeView1.clearSelection();
                treeView2.clearSelection();

            }
        });

        clearButton = (PushButton) serializer.getNamespace().get("clearButton");
        clearButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                System.out.println("Clearing data from " + button.getName() + " at " + new Date());

                // TODO: verify for release 2.1 if implement a method to empty
                // components inside ...
                // pushButtons.remove(button);
                // toggleButtons.remove(button);
                // radioButtons.remove(button);
                // checkButtons.remove(button);
                // checkTriButtons.remove(button);

                // force clear of elements data ...
                clearComponent(calendarButton1);
                clearComponent(calendarButton2);
                clearComponent(spinner1);
                clearComponent(spinner2);
                clearComponent(listButton1);
                clearComponent(listButton2);

                clearComponent(listView1);
                clearComponent(listView2);
                clearComponent(tableView1);
                clearComponent(tableView2);
                clearComponent(treeView1);
                clearComponent(treeView2);

                // TODO: put (in bxml) the two tableView in a SplitPane, and see
                // some strange moving mouse over ...

                // TODO: add clear/clearSelection methods even to some type of
                // buttons (all types in the first row displayed) ...

            }
        });

        System.out.println("initializeFields: end");
    }

    protected static final void clearComponent(Component component) {
        Utils.checkNull(component, "component");

        component.clear();
        component.repaint();

        System.out.println("Component " + component + " with name \"" + component.getName() + "\""
            + " cleared, and forced a repaint on it");
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Pivot694.class, args);
    }

}
