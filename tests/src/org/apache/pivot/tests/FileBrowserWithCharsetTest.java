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

import java.io.File;
import java.nio.charset.Charset;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.skin.terra.TerraFileBrowserSheetSkin;

public class FileBrowserWithCharsetTest extends FileBrowserSheet implements Application {
    private ArrayList<String> choices;
    private ListButton lb;

    private void addCharsetChoice(String name) {
        if (choices.indexOf(name) < 0) {
            choices.add(name);
        }
    }
    public FileBrowserWithCharsetTest() {
        this(Mode.OPEN);
    }

    public FileBrowserWithCharsetTest(Mode mode) {
        super(mode);
        TerraFileBrowserSheetSkin skin = (TerraFileBrowserSheetSkin) getSkin();
        BoxPane box = new BoxPane();
        box.getStyles().put(Style.verticalAlignment, VerticalAlignment.CENTER);
        box.add(new Label("Character set:"));
        choices = new ArrayList<>(String.CASE_INSENSITIVE_ORDER);
        lb = new ListButton();
        Charset defaultCS = Charset.defaultCharset();
        addCharsetChoice(defaultCS.name());
        addCharsetChoice("UTF-8");
        addCharsetChoice("ISO-8859-1");
        addCharsetChoice("US-ASCII");

        lb.setListData(choices);
        lb.setSelectedIndex(1);
        lb.getListButtonSelectionListeners().add(new ListButtonSelectionListener() {
            @Override
            public void selectedItemChanged(ListButton listButton, Object previousSelectedItem) {
                System.out.println(listButton.toString() + "; New character set selection: "
                    + listButton.getSelectedItem());
            }
        });
        box.add(lb);
        skin.addComponent(box);
    }

    public String getCharsetName() {
        return (String) lb.getSelectedItem();
    }

    private Frame frame = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BoxPane windowContent = new BoxPane();
        windowContent.getStyles().put(Style.verticalAlignment, VerticalAlignment.CENTER);
        final Checkbox showHiddenCheckbox = new Checkbox("Show hidden files");
        windowContent.add(showHiddenCheckbox);
        PushButton button = new PushButton("Open File Browser");
        button.getStyles().put(Style.padding, "[2, 4, 2, 4]");
        button.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button buttonArgument) {
                final Window window = Window.getActiveWindow();
                final FileBrowserWithCharsetTest fileBrowserSheet = new FileBrowserWithCharsetTest(
                    FileBrowserSheet.Mode.OPEN);
                fileBrowserSheet.getStyles().put(Style.showHiddenFiles, showHiddenCheckbox.isSelected());

                fileBrowserSheet.open(window, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            Sequence<File> selectedFiles = fileBrowserSheet.getSelectedFiles();

                            ListView listView = new ListView();
                            listView.setListData(new ArrayList<>(selectedFiles));
                            listView.setSelectMode(ListView.SelectMode.NONE);
                            listView.getStyles().put(Style.backgroundColor, null);

                            Alert.alert(MessageType.INFO,
                                "You selected (charset " + fileBrowserSheet.getCharsetName() + "):", listView, window);
                        } else {
                            Alert.alert(MessageType.INFO, "You didn't select anything.", window);
                        }
                    }
                });

            }
        });

        windowContent.add(button);

        frame = new Frame(windowContent);
        frame.setMaximized(true);
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
        DesktopApplicationContext.main(FileBrowserWithCharsetTest.class, args);
    }

}
