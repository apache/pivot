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
import org.apache.pivot.wtk.HyperlinkButton;


public class HyperlinkButtonTest extends Application.Adapter {
    private Frame frame = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        frame = new Frame();
        frame.setTitle("Hyperlink Button Test");
        frame.setPreferredSize(480, 360);

        HyperlinkButton button1 = new HyperlinkButton("http://pivot.apache.org");
        HyperlinkButton button2 = new HyperlinkButton("Apache website", "http://apache.org");
        BoxPane bp = new BoxPane(Orientation.VERTICAL);
        bp.add(button1);
        bp.add(button2);

        frame.setContent(bp);

        frame.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (frame != null) {
            frame.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(HyperlinkButtonTest.class, args);
    }
}
