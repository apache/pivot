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
package org.apache.pivot.wtk.skin.terra;

import java.io.File;
import java.io.IOException;

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.io.Folder;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.FileBrowser;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.FileBrowserSheetListener;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

/**
 * Terra file browser sheet skin.
 */
public class TerraFileBrowserSheetSkin extends TerraSheetSkin implements FileBrowserSheetListener {
    @WTKX private TextInput saveAsTextInput = null;
    @WTKX private FileBrowser fileBrowser = null;
    @WTKX private PushButton okButton = null;
    @WTKX private PushButton cancelButton = null;

    private boolean updatingSelection = false;

    @Override
    public void install(Component component) {
        super.install(component);

        final FileBrowserSheet fileBrowserSheet = (FileBrowserSheet)component;

        Resources resources;
        try {
            resources = new Resources(this);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        WTKXSerializer wtkxSerializer = new WTKXSerializer(resources);

        Component content;
        try {
            content = (Component)wtkxSerializer.readObject(this, "terra_file_browser_sheet_skin.wtkx");
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        fileBrowserSheet.setContent(content);

        wtkxSerializer.bind(this, TerraFileBrowserSheetSkin.class);

        okButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                updatingSelection = true;

                // TODO If SAVE_AS, get value from saveAsTextInput
                saveAsTextInput.getText();

                fileBrowserSheet.setSelectedFiles(fileBrowser.getSelectedFiles());

                updatingSelection = false;

                fileBrowserSheet.close(true);
            }
        });

        cancelButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                fileBrowserSheet.close(false);
            }
        });

        fileBrowserSheet.getFileBrowserSheetListeners().add(this);
    }

    @Override
    public void uninstall() {
        FileBrowserSheet fileBrowserSheet = (FileBrowserSheet)getComponent();
        fileBrowserSheet.setContent(null);

        fileBrowserSheet.getFileBrowserSheetListeners().remove(this);

        super.uninstall();
    }

    public void multiSelectChanged(FileBrowserSheet fileBrowserSheet) {
        // TODO
    }

    public void selectedFolderChanged(FileBrowserSheet fileBrowserSheet, Folder previousSelectedFolder) {
        // TODO
    }

    public void selectedFilesChanged(FileBrowserSheet fileBrowserSheet, Sequence<File> previousSelectedFiles) {
        if (!updatingSelection) {
            fileBrowser.setSelectedFiles(fileBrowserSheet.getSelectedFiles());
        }
    }

    public void fileFilterChanged(FileBrowserSheet fileBrowserSheet, Filter<File> previousFileFilter) {
        // TODO
    }
}
