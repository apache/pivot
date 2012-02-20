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
package org.apache.pivot.tests.issues;

import java.io.File;
import java.io.IOException;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.Window;

public class Pivot832 implements Application
{
    private Window window = null;
    private String selectedFolder = null;

    @BXML
    private PushButton selectFolderButton = null;

    @BXML
    private PushButton openFileButton = null;


    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(getClass().getResource("pivot_832.bxml"));
        bxmlSerializer.bind(this, Pivot832.class);

        selectFolderButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet();

                fileBrowserSheet.setMode(FileBrowserSheet.Mode.SAVE_TO);  // to be able to select a folder
                fileBrowserSheet.open(window, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            File loadedFile = fileBrowserSheet.getSelectedFile();
                            try {
                                selectedFolder = loadedFile.getCanonicalPath();

                                openFileButton.setEnabled(true);
                            } catch (IOException e) {
                                e.printStackTrace();
                                openFileButton.setEnabled(false);
                            }
                            window.setTitle("Selected folder: " + selectedFolder);
                        }
                    }
                });
            }
        });

        openFileButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet();

                try {
                    fileBrowserSheet.setRootDirectory(new File(selectedFolder));
                    System.out.println("Root folder set to '" + fileBrowserSheet.getRootDirectory().getCanonicalPath() + "'");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                fileBrowserSheet.setMode(FileBrowserSheet.Mode.OPEN);  // open it, so I can verify that it starts from the right folder ...
                fileBrowserSheet.open(window, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                    }
                });
            }
        });

        window.open(display);
    }

    public boolean shutdown(boolean b) throws Exception {
        return false;
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Pivot832.class, args);
    }

}
