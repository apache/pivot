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
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.CalendarButton;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.Window;

public class Pivot694 implements Application {
    private Window window = null;
    private ButtonGroup pushButtons = null;
    private ButtonGroup toggleButtons = null;
    private ButtonGroup radioButtons = null;
    private ButtonGroup checkButtons = null;
    private ButtonGroup checkTriButtons = null;

    private CalendarButton calendarButton = null;
    private Spinner spinner = null;

    private TableView tableView = null;
    private TreeView  treeView  = null;

    private ListButton listButton1 = null;
    private ListButton listButton2 = null;

    private PushButton clearSelectionButton = null;
    private PushButton clearButton = null;


    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        System.out.println("startup: start");

        System.out.println("Test for clearing (selection) in many Components");

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window)bxmlSerializer.readObject(this.getClass(), "pivot_694.bxml");
        initializeFields(bxmlSerializer);
        window.open(display);

        System.out.println("startup: end");
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    private void initializeFields(BXMLSerializer serializer) {
        System.out.println("initializeFields: start");

        pushButtons = (ButtonGroup)serializer.getNamespace().get("pushButtons");
        toggleButtons = (ButtonGroup)serializer.getNamespace().get("toggleButtons");
        radioButtons = (ButtonGroup)serializer.getNamespace().get("radioButtons");
        checkButtons = (ButtonGroup)serializer.getNamespace().get("checkButtons");
        checkTriButtons = (ButtonGroup)serializer.getNamespace().get("checkTriButtons");

        calendarButton = (CalendarButton)serializer.getNamespace().get("calendarButton");
        spinner = (Spinner)serializer.getNamespace().get("spinner");

        tableView = (TableView)serializer.getNamespace().get("tableView");
        treeView  = (TreeView)serializer.getNamespace().get("treeView");

        listButton1 = (ListButton)serializer.getNamespace().get("listButton1");
        listButton2 = (ListButton)serializer.getNamespace().get("listButton2");

        clearSelectionButton = (PushButton)serializer.getNamespace().get("clearSelectionButton");
        clearSelectionButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                System.out.println("Clearing selection from " + button.getName() + " at " + new Date());

    //TODO: verify what to do on these ...
//                pushButtons.clearSelection();
//                toggleButtons.clearSelection();
//                radioButtons.clearSelection();
//                checkButtons.clearSelection();
//                checkTriButtons.clearSelection();

//                calendarButton.clearSelection();
//                spinner.clearSelection();

                tableView.clearSelection();
                treeView.clearSelection();

//              listButton1.clearSelection();
//              listButton2.clearSelection();

            }
        });

        clearButton = (PushButton)serializer.getNamespace().get("clearButton");
        clearButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                System.out.println("Clearing data from " + button.getName() + " at " + new Date());

// TODO: verify what to do on these ...
//                pushButtons.remove(button);
//                toggleButtons.remove(button);
//                radioButtons.remove(button);
//                checkButtons.remove(button);
//                checkTriButtons.remove(button);

                calendarButton.clear();
                spinner.clear();

                tableView.clear();
                treeView.clear();

                listButton1.clear();
                listButton2.clear();
            }
        });

        System.out.println("initializeFields: end");
    }


    public static void main(String[] args) {
        DesktopApplicationContext.main(Pivot694.class, args);
    }

}
