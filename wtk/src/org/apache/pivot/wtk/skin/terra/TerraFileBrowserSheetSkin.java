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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.io.Folder;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.FileBrowser;
import org.apache.pivot.wtk.FileBrowserListener;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.FileBrowserSheetListener;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputTextListener;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

/**
 * Terra file browser sheet skin.
 */
public class TerraFileBrowserSheetSkin extends TerraSheetSkin implements FileBrowserSheetListener {
    private static class SaveToFileFilter implements Filter<File> {
        public final Filter<File> sourceFilter;

        public SaveToFileFilter(Filter<File> sourceFilter) {
            this.sourceFilter = sourceFilter;
        }

        public boolean include(File file) {
            return (!file.isDirectory()
                || (sourceFilter != null
                    && sourceFilter.include(file)));
        }
    };

    @WTKX private TablePane tablePane = null;
    @WTKX private TablePane.Row saveAsRow = null;
    @WTKX private TextInput saveAsTextInput = null;
    @WTKX private FileBrowser fileBrowser = null;
    @WTKX private PushButton okButton = null;
    @WTKX private PushButton cancelButton = null;

    private boolean updatingSelection = false;
    private int selectedDirectoryCount = 0;

    @Override
    public void install(Component component) {
        super.install(component);

        final FileBrowserSheet fileBrowserSheet = (FileBrowserSheet)component;
        final FileBrowserSheet.Mode mode = fileBrowserSheet.getMode();

        // Load the sheet content
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

        saveAsTextInput.getTextInputTextListeners().add(new TextInputTextListener() {
            public void textChanged(TextInput textInput) {
                updateOKButtonState();
            }
        });

        fileBrowser.getStyles().put("keyboardFolderTraversalEnabled",
            (mode != FileBrowserSheet.Mode.SAVE_TO));

        fileBrowser.getFileBrowserListeners().add(new FileBrowserListener.Adapter() {
            public void selectedFileAdded(FileBrowser fileBrowser, File file) {
                if (file.isDirectory()) {
                    selectedDirectoryCount++;
                }

                updateOKButtonState();
            }

            public void selectedFileRemoved(FileBrowser fileBrowser, File file) {
                if (file.isDirectory()) {
                    selectedDirectoryCount--;
                }

                updateOKButtonState();
            }

            public void selectedFilesChanged(FileBrowser fileBrowser,
                Sequence<File> previousSelectedFiles) {
                selectedDirectoryCount = 0;

                Sequence<File> selectedFiles = fileBrowser.getSelectedFiles();
                for (int i = 0, n = selectedFiles.getLength(); i < n; i++) {
                    File selectedFile = selectedFiles.get(i);

                    if (selectedFile.isDirectory()) {
                        selectedDirectoryCount++;
                    }
                }

                if (!fileBrowser.isMultiSelect()) {
                    File selectedFile = fileBrowser.getSelectedFile();

                    if (selectedFile == null) {
                        saveAsTextInput.setText("");
                    } else if (!selectedFile.isDirectory()) {
                        saveAsTextInput.setText(selectedFile.getName());
                    }
                }

                updateOKButtonState();
            }
        });

        okButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                fileBrowserSheet.close(true);
            }
        });

        cancelButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                fileBrowserSheet.close(false);
            }
        });


        // Add this as a file browser sheet listener
        fileBrowserSheet.getFileBrowserSheetListeners().add(this);

        // Initialize layout and file browser selection state
        switch (mode) {
            case OPEN: {
                if (saveAsRow.getTablePane() != null) {
                    tablePane.getRows().remove(saveAsRow);
                }

                fileBrowser.setMultiSelect(false);
                break;
            }

            case OPEN_MULTIPLE: {
                if (saveAsRow.getTablePane() != null) {
                    tablePane.getRows().remove(saveAsRow);
                }

                fileBrowser.setMultiSelect(true);
                break;
            }

            case SAVE_AS: {
                if (saveAsRow.getTablePane() == null) {
                    tablePane.getRows().insert(saveAsRow, 0);
                }

                fileBrowser.setMultiSelect(false);
                break;
            }

            case SAVE_TO: {
                if (saveAsRow.getTablePane() != null) {
                    tablePane.getRows().remove(saveAsRow);
                }

                fileBrowser.setMultiSelect(false);
                break;
            }
        }

        selectedFolderChanged(fileBrowserSheet, null);
        selectedFilesChanged(fileBrowserSheet, null);
        disabledFileFilterChanged(fileBrowserSheet, null);
    }

    @Override
    public void uninstall() {
        FileBrowserSheet fileBrowserSheet = (FileBrowserSheet)getComponent();

        fileBrowserSheet.setContent(null);
        fileBrowserSheet.getFileBrowserSheetListeners().remove(this);

        super.uninstall();
    }

    @Override
    public Vote previewSheetClose(final Sheet sheet, final boolean result) {
        Vote vote;

        if (result
            && !okButton.isEnabled()) {
            vote = Vote.DENY;
        } else {
            vote = super.previewSheetClose(sheet, result);
        }

        if (vote == Vote.APPROVE) {
            updatingSelection = true;

            FileBrowserSheet fileBrowserSheet = (FileBrowserSheet)sheet;
            FileBrowserSheet.Mode mode = fileBrowserSheet.getMode();

            switch (mode) {
                case OPEN:
                case OPEN_MULTIPLE:
                case SAVE_TO: {
                    fileBrowserSheet.setSelectedFiles(fileBrowser.getSelectedFiles());
                    break;
                }

                case SAVE_AS: {
                    String fileName = saveAsTextInput.getText();
                    File selectedFile = new File(fileBrowser.getSelectedFolder(), fileName);
                    fileBrowserSheet.setSelectedFiles(new ArrayList<File>(selectedFile));
                    break;
                }
            }

            updatingSelection = false;
        }

        return vote;
    }

    public void selectedFolderChanged(FileBrowserSheet fileBrowserSheet, Folder previousSelectedFolder) {
        fileBrowser.setSelectedFolder(fileBrowserSheet.getSelectedFolder());
    }

    public void selectedFilesChanged(FileBrowserSheet fileBrowserSheet, Sequence<File> previousSelectedFiles) {
        if (!updatingSelection) {
            fileBrowser.setSelectedFiles(fileBrowserSheet.getSelectedFiles());
        }
    }

    public void disabledFileFilterChanged(FileBrowserSheet fileBrowserSheet, Filter<File> previousDisabledFileFilter) {

        Filter<File> disabledFileFilter = fileBrowserSheet.getDisabledFileFilter();

        FileBrowserSheet.Mode mode = fileBrowserSheet.getMode();
        if (mode == FileBrowserSheet.Mode.SAVE_TO) {
            disabledFileFilter = new SaveToFileFilter(disabledFileFilter);
        }

        fileBrowser.setDisabledFileFilter(disabledFileFilter);
    }

    private void updateOKButtonState() {
        FileBrowserSheet fileBrowserSheet = (FileBrowserSheet)getComponent();

        FileBrowserSheet.Mode mode = fileBrowserSheet.getMode();
        Sequence<File> selectedFiles = fileBrowser.getSelectedFiles();

        switch (mode) {
            case OPEN:
            case OPEN_MULTIPLE: {
                okButton.setEnabled(selectedFiles.getLength() > 0
                    && selectedDirectoryCount == 0);
                break;
            }

            case SAVE_AS: {
                okButton.setEnabled(saveAsTextInput.getTextLength() > 0);
                break;
            }

            case SAVE_TO: {
                okButton.setEnabled(selectedDirectoryCount > 0);
                break;
            }
        }

    }
}
