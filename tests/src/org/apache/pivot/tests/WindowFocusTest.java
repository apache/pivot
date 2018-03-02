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

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.TextInput;

public final class WindowFocusTest implements Application {
    private Frame frame1;
    private Frame frame2;

    @Override
    public void startup(final Display display, final Map<String, String> properties) throws Exception {
        BoxPane boxPane1 = new BoxPane(Orientation.VERTICAL);
        TextInput textInput1 = new TextInput();
        textInput1.setText("ABCD");
        boxPane1.add(textInput1);
        boxPane1.add(new TextInput());
        boxPane1.add(new TextInput());
        frame1 = new Frame(boxPane1);
        frame1.setPreferredSize(320, 240);
        frame1.open(display);

        BoxPane boxPane2 = new BoxPane(Orientation.VERTICAL);
        TextInput textInput2 = new TextInput();
        textInput2.setText("1234");
        boxPane2.add(textInput2);
        boxPane2.add(new TextInput());
        boxPane2.add(new TextInput());
        frame2 = new Frame(boxPane2);
        frame2.setPreferredSize(320, 240);
        frame2.open(display);

        frame2.requestFocus();
    }

    @Override
    public boolean shutdown(final boolean optional) throws Exception {
        if (frame1 != null) {
            frame1.close();
        }

        if (frame2 != null) {
            frame2.close();
        }

        return false;
    }

    public static void main(final String[] args) {
        DesktopApplicationContext.main(WindowFocusTest.class, args);
    }

}
