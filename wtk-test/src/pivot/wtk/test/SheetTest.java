/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Display;
import pivot.wtk.FlowPane;
import pivot.wtk.Frame;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Label;
import pivot.wtk.PushButton;
import pivot.wtk.Sheet;
import pivot.wtk.TablePane;
import pivot.wtk.VerticalAlignment;

public class SheetTest implements Application {
    private Frame frame = null;

    public void startup(final Display display, Dictionary<String, String> properties)
        throws Exception {
        PushButton windowContent = new PushButton("Window Content");
        windowContent.setPreferredSize(480, 360);

        frame = new Frame(windowContent);
        frame.getStyles().put("padding", 0);
        frame.open(display);

        TablePane tablePane = new TablePane();
        tablePane.setPreferredSize(320, 240);
        tablePane.getColumns().add(new TablePane.Column(1, true));
        tablePane.getRows().add(new TablePane.Row(1, true));
        tablePane.getRows().add(new TablePane.Row(-1));

        Label sheetContent = new Label("Sheet Content");
        sheetContent.getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
        sheetContent.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

        tablePane.getRows().get(0).add(sheetContent);

        FlowPane flowPane = new FlowPane();
        tablePane.getRows().get(1).add(flowPane);

        flowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);

        PushButton closeButton = new PushButton("Close");
        flowPane.add(closeButton);

        closeButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                button.getWindow().close();
                frame.requestFocus();
            }
        });

        Sheet sheet = new Sheet(tablePane);
        sheet.open(frame);
    }

    public boolean shutdown(boolean optional) {
        frame.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
