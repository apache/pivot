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
package org.apache.pivot.wtk.test;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FlowPane;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.TextInput;

public class WindowFocusTest implements Application {
    private Frame frame1;
    private Frame frame2;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        FlowPane flowPane1 = new FlowPane();
        flowPane1.add(new TextInput());
        frame1 = new Frame(flowPane1);
        frame1.setPreferredSize(320, 240);
        frame1.open(display);

        FlowPane flowPane2 = new FlowPane();
        flowPane2.add(new TextInput());
        frame2 = new Frame(flowPane2);
        frame2.setPreferredSize(320, 240);
        frame2.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) throws Exception {
        if (frame1 != null) {
            frame1.close();
        }

        frame1 = null;

        if (frame2 != null) {
            frame2.close();
        }

        frame2 = null;

        return true;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(WindowFocusTest.class, args);
    }
}
