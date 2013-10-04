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
import java.util.Date;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Window;

/**
 * Simple test window which continuously presses a button which opens a dialog
 * box and then closes it immediately. Watch the heap usage in a profiler and
 * compare: - when TestDialog has an icon (specified in ok_dialog.bxml) - when
 * TestDialog has no icon and see that heap usage will grow to almost the
 * maximum when an icon is used in TestDialog. The icon contains an
 * ImageListenerList which will retain a reference to every dialog created, thus
 * preventing them being garbage collected.
 */
public class LeakTestWindow extends Window implements Bindable {

    private static final String MARKUP_FILE = "/org/apache/pivot/tests/issues/pivot861/leak_test_window.bxml";

    @BXML
    PushButton button;

    int dialogTest = 0;

    public static LeakTestWindow create() throws IOException, SerializationException {
        System.out.println("LeakTestWindow create()");
        return (LeakTestWindow) new BXMLSerializer().readObject(LeakTestWindow.class, MARKUP_FILE);
    }

    @Override
    public void initialize(Map<String, Object> arg0, URL arg1, Resources arg2) {
        System.out.println("LeakTestWindow initialize(...)\n");

        button.setAction(new Action() {
            @Override
            public void perform(Component component) {
                dialogTest++;
                System.out.println("Dialog test number " + dialogTest + " at " + new Date());
                TestDialog dialog = TestDialog.create();

                System.out.println("Opening the dialog");
                dialog.open(LeakTestWindow.this);

                // Close the dialog straight away
                System.out.println("Closing the dialog");
                dialog.close();

                System.out.println("End of perform()\n");
            }
        });
    }

    @Override
    public void open(Display display, Window owner) {
        super.open(display, owner);

        ApplicationContext.scheduleCallback(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    button.press();
                }
            }
        }, 1000 // add a little delay (instead of 0) to show the TestDialog
        );
    }

}
