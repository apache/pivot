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
package pivot.wtk.test;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.PushButton;
import pivot.wtk.Window;

public class PushButtonTest implements Application {
    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        window = new Window();
        FlowPane flowPane = new FlowPane();
        flowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);

        PushButton uploadButton = new PushButton("Upload");
        uploadButton.getStyles().put("preferredAspectRatio", 3.0f);
        flowPane.add(uploadButton);

        window.setContent(flowPane);
        window.setMaximized(true);

        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(PushButtonTest.class, args);
    }
}
