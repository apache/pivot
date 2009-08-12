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
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Direction;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.skin.ContainerSkin;

public class FocusTest implements Application {
    private Frame frame = null;

    @Override
    public void startup(Display display, Map<String, String> properties) {
        frame = new Frame();

        BoxPane boxPane = new BoxPane();
        boxPane.add(new Label("Hello, World!"));
        boxPane.setFocusTraversalPolicy(new ContainerSkin.IndexFocusTraversalPolicy(true));

        frame.setContent(boxPane);

        frame.setPreferredSize(320, 240);
        frame.open(display);

        frame.requestFocus();
        frame.transferFocus(null, Direction.FORWARD);
    }

    @Override
    public boolean shutdown(boolean optional) {
        return false;
    }

    @Override
    public void suspend(){
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(FocusTest.class, args);
    }
}
