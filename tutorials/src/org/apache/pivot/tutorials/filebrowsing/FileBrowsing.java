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
package org.apache.pivot.tutorials.filebrowsing;

import java.io.File;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

public class FileBrowsing implements Application {
    private Window window = null;

    @WTKX private ButtonGroup fileBrowserSheetModeGroup = null;
    @WTKX private PushButton openSheetButton = null;

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();

        window = (Window)wtkxSerializer.readObject(getClass().getResource("file_browsing.wtkx"));
        wtkxSerializer.bind(this, FileBrowsing.class);

        openSheetButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                Button selection = fileBrowserSheetModeGroup.getSelection();

                String mode = (String)selection.getUserData().get("mode");
                final FileBrowserSheet fileBrowserSheet =
                    new FileBrowserSheet(FileBrowserSheet.Mode.valueOf(mode.toUpperCase()));

                fileBrowserSheet.open(window, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            Sequence<File> selectedFiles = fileBrowserSheet.getSelectedFiles();

                            ListView listView = new ListView();
                            listView.setListData(new ArrayList<File>(selectedFiles));
                            listView.setSelectMode(ListView.SelectMode.NONE);
                            listView.getStyles().put("backgroundColor", null);

                            Alert.alert(MessageType.INFO, "You selected:", listView, window);
                        } else {
                            Alert.alert(MessageType.INFO, "You didn't select anything.", window);
                        }
                    }
                });
            }
        });

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
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
        DesktopApplicationContext.main(FileBrowsing.class, args);
    }
}
