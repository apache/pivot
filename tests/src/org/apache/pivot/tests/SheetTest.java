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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.media.Picture;

public final class SheetTest implements Application {
    private Frame frame = null;
    private Sheet sheet = null;

    private static final String LICENSE_TEXT =
        "Unless required by applicable law or agreed to in writing, software distributed under the License "
      + "is distributed on an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express "
      + "or implied.";

    @SuppressWarnings("unused")
    @Override
    public void startup(final Display display, final Map<String, String> properties) throws Exception {
        Picture picture = (Picture) Image.load(getClass().getResource("IMG_0767_2.jpg"));
        picture.resample(120);

        BoxPane windowContent = new BoxPane();
        PushButton button = new PushButton(picture);
        button.getStyles().put(Style.toolbar, true);

        windowContent.add(button);

        frame = new Frame(windowContent);
        frame.setPreferredSize(480, 360);
        frame.getStyles().put(Style.padding, 0);
        frame.open(display);

        final TablePane tablePane = new TablePane();
        tablePane.setPreferredSize(320, 240);
        new TablePane.Column(tablePane, 1, true);
        TablePane.Row row0 = new TablePane.Row(tablePane, 1, true);
        TablePane.Row row1 = new TablePane.Row(tablePane, -1);

        final Label sheetContent = new Label("Sheet Content");
        sheetContent.getStyles().put(Style.wrapText, true);
        sheetContent.getStyles().put(Style.horizontalAlignment, HorizontalAlignment.CENTER);
        sheetContent.getStyles().put(Style.verticalAlignment, VerticalAlignment.CENTER);

        row0.add(sheetContent);

        Label promptBody = new Label(LICENSE_TEXT);
        promptBody.getStyles().put(Style.wrapText, true);

        final Prompt prompt = new Prompt(MessageType.INFO, "Prompt", new ArrayList<>("OK"), promptBody);
        prompt.setTitle("Prompt");
        prompt.getStyles().put(Style.resizable, true);

        prompt.getComponentMouseListeners().add(new ComponentMouseListener() {
            @Override
            public void mouseOver(final Component component) {
                System.out.println("Mouse Over");
            }

            @Override
            public void mouseOut(final Component component) {
                System.out.println("Mouse out");
            }
        });

        Label alertBody = new Label(LICENSE_TEXT);
        alertBody.getStyles().put(Style.wrapText, true);

        final Alert alert = new Alert(MessageType.INFO, "Alert", new ArrayList<>("OK"), alertBody);
        alert.setTitle("Alert");

        BoxPane boxPane = new BoxPane();
        row1.add(boxPane);

        boxPane.getStyles().put(Style.horizontalAlignment, HorizontalAlignment.RIGHT);

        final PushButton closeButton = new PushButton("Close");
        closeButton.getStyles().put(Style.minimumAspectRatio, 3);
        boxPane.add(closeButton);

        sheet = new Sheet(tablePane);

        closeButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(final Button buttonArgument) {
                buttonArgument.getWindow().close();
            }
        });

        button.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(final Button buttonArgument) {
                prompt.open(frame);

                Display displayLocal = DesktopApplicationContext.createDisplay(640, 480, 100, 100,
                    true, true, false, buttonArgument.getDisplay().getHostWindow(), null);

                Window window = new Window();
                window.setTitle("New Secondary Window");
                window.setMaximized(true);
                window.setContent(new Label("I am a secondary window!"));
                window.open(displayLocal);
            }
        });

        sheet.getWindowStateListeners().add(new WindowStateListener() {
            @Override
            public void windowOpened(final Window window) {
                closeButton.requestFocus();
            }
        });

        DesktopApplicationContext.sizeHostToFit(frame);
    }

    @Override
    public boolean shutdown(final boolean optional) {
        if (frame != null) {
            frame.close();
        }

        return false;
    }

    public static void main(final String[] args) {
        DesktopApplicationContext.main(SheetTest.class, args);
    }
}
