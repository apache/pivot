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
import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.Window;

public class FileBrowsing extends Window implements Bindable {
    @BXML private ButtonGroup fileBrowserSheetModeGroup = null;
    @BXML private PushButton openSheetButton = null;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        openSheetButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                Button selection = fileBrowserSheetModeGroup.getSelection();

                String mode = (String) selection.getUserData().get("mode");
                FileBrowserSheet.Mode fileBrowserSheetMode = FileBrowserSheet.Mode.valueOf(mode.toUpperCase());
                final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet();

                if (fileBrowserSheetMode == FileBrowserSheet.Mode.SAVE_AS) {
                    fileBrowserSheet.setSelectedFile(new File(fileBrowserSheet.getRootDirectory(),
                        "New File"));
                }

                fileBrowserSheet.setMode(fileBrowserSheetMode);
                fileBrowserSheet.open(FileBrowsing.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            Sequence<File> selectedFiles = fileBrowserSheet.getSelectedFiles();

                            ListView listView = new ListView();
                            listView.setListData(new ArrayList<>(selectedFiles));
                            listView.setSelectMode(ListView.SelectMode.NONE);
                            listView.getStyles().put(Style.backgroundColor, null);

                            Alert.alert(MessageType.INFO, "You selected:", listView,
                                FileBrowsing.this);
                        } else {
                            Alert.alert(MessageType.INFO, "You didn't select anything.",
                                FileBrowsing.this);
                        }
                    }
                });
            }
        });
    }
}
