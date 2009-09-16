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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.media.Picture;

public class SheetTest implements Application {
    private Frame frame = null;
    private Sheet sheet = null;

    @Override
    public void startup(final Display display, Map<String, String> properties)
        throws Exception {
        Picture picture = (Picture)Image.load(getClass().getResource("IMG_0767_2.jpg"));
        picture.resample(120);

        PushButton windowContent = new PushButton(picture);
        windowContent.setPreferredSize(480, 360);

        frame = new Frame(windowContent);
        frame.getStyles().put("padding", 0);
        frame.open(display);

        final TablePane tablePane = new TablePane();
        tablePane.setPreferredSize(320, 240);
        tablePane.getColumns().add(new TablePane.Column(1, true));
        tablePane.getRows().add(new TablePane.Row(1, true));
        tablePane.getRows().add(new TablePane.Row(-1));

        final Label sheetContent = new Label("Sheet Content");
        sheetContent.getStyles().put("wrapText", true);
        sheetContent.getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
        sheetContent.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

        tablePane.getRows().get(0).add(sheetContent);

        Label promptBody = new Label("Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.");
        promptBody.getStyles().put("wrapText", true);

        final Prompt prompt = new Prompt(MessageType.INFO, "Prompt", new ArrayList<String>("OK"), promptBody);
        prompt.setTitle("Prompt");
        prompt.setOwner(frame);

        Label alertBody = new Label("Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.");
        alertBody.getStyles().put("wrapText", true);

        final Alert alert = new Alert(MessageType.INFO, "Alert", new ArrayList<String>("OK"), alertBody);
        alert.setTitle("Alert");

        BoxPane boxPane = new BoxPane();
        tablePane.getRows().get(1).add(boxPane);

        boxPane.getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);

        final PushButton closeButton = new PushButton("Close");
        closeButton.getStyles().put("minimumAspectRatio", 3);
        boxPane.add(closeButton);

        sheet = new Sheet(tablePane);

        closeButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                button.getWindow().close();
            }
        });

        windowContent.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                prompt.open(display);
            }
        });

        sheet.getWindowStateListeners().add(new WindowStateListener() {
            @Override
            public Vote previewWindowOpen(Window window, Display display) {
                return Vote.APPROVE;
            }

            @Override
            public void windowOpenVetoed(Window window, Vote reason) {
            }

            @Override
            public void windowOpened(Window window) {
                closeButton.requestFocus();
            }

            @Override
            public Vote previewWindowClose(Window window) {
                return Vote.APPROVE;
            }

            @Override
            public void windowCloseVetoed(Window window, Vote reason) {
            }

            @Override
            public void windowClosed(Window window, Display display) {
            }
        });
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (frame != null) {
            frame.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(SheetTest.class, args);
    }
}
