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

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.FileBrowser;
import org.apache.pivot.wtk.FileBrowserListener;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.FileBrowserSheetListener;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.Window;

/**
 * Terra file browser sheet skin.
 */
public class TerraFileBrowserSheetSkin extends TerraSheetSkin implements FileBrowserSheetListener {

    private static class SaveToFileFilter implements Filter<File> {
        public final Filter<File> sourceFilter;

        public SaveToFileFilter(Filter<File> sourceFilter) {
            this.sourceFilter = sourceFilter;
        }

        @Override
        public boolean include(File file) {
            return (!file.isDirectory()
                || (sourceFilter != null
                    && sourceFilter.include(file)));
        }
    }

    @BXML private TablePane tablePane = null;
    @BXML private BoxPane saveAsBoxPane = null;
    @BXML private TextInput saveAsTextInput = null;
    @BXML private FileBrowser fileBrowser = null;
    @BXML private PushButton okButton = null;
    @BXML private PushButton cancelButton = null;

    private boolean updatingSelection = false;
    private int selectedDirectoryCount = 0;

    public TerraFileBrowserSheetSkin() {
        setResizable(true);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        final FileBrowserSheet fileBrowserSheet = (FileBrowserSheet)component;
        fileBrowserSheet.setMinimumWidth(360);
        fileBrowserSheet.setMinimumHeight(180);

        // Load the sheet content
        BXMLSerializer bxmlSerializer = new BXMLSerializer();

        Component content;
        try {
            content = (Component)bxmlSerializer.readObject(TerraFileBrowserSheetSkin.class,
                "terra_file_browser_sheet_skin.bxml", true);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        fileBrowserSheet.setContent(content);

        bxmlSerializer.bind(this, TerraFileBrowserSheetSkin.class);

        // set the same rootDirectory to fileBrowser
        fileBrowser.setRootDirectory(fileBrowserSheet.getRootDirectory());

        saveAsTextInput.getTextInputContentListeners().add(new TextInputContentListener.Adapter() {
            @Override
            public void textChanged(TextInput textInput) {
                Form.clearFlag(saveAsBoxPane);
                updateOKButtonState();
            }
        });

        fileBrowser.getFileBrowserListeners().add(new FileBrowserListener.Adapter() {
            @Override
            public void rootDirectoryChanged(FileBrowser fileBrowserArgument,
                File previousRootDirectory) {
                updatingSelection = true;

                fileBrowserSheet.setRootDirectory(fileBrowserArgument.getRootDirectory());

                updatingSelection = false;

                selectedDirectoryCount = 0;
                updateOKButtonState();
            }

            @Override
            public void selectedFileAdded(FileBrowser fileBrowserArgument, File file) {
                if (file.isDirectory()) {
                    selectedDirectoryCount++;
                }

                updateOKButtonState();
            }

            @Override
            public void selectedFileRemoved(FileBrowser fileBrowserArgument, File file) {
                if (file.isDirectory()) {
                    selectedDirectoryCount--;
                }

                updateOKButtonState();
            }

            @Override
            public void selectedFilesChanged(FileBrowser fileBrowserArgument,
                Sequence<File> previousSelectedFiles) {
                selectedDirectoryCount = 0;

                Sequence<File> selectedFiles = fileBrowserArgument.getSelectedFiles();
                for (int i = 0, n = selectedFiles.getLength(); i < n; i++) {
                    File selectedFile = selectedFiles.get(i);

                    if (selectedFile.isDirectory()) {
                        selectedDirectoryCount++;
                    }
                }

                if (!fileBrowserArgument.isMultiSelect()) {
                    File selectedFile = fileBrowserArgument.getSelectedFile();

                    if (selectedFile != null
                        && !selectedFile.isDirectory()) {
                        saveAsTextInput.setText(selectedFile.getName());
                    }
                }

                updateOKButtonState();
            }
        });

        fileBrowser.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter() {
            private File file = null;

            @Override
            public boolean mouseClick(Component componentArgument, Mouse.Button button, int x, int y, int count) {
                boolean consumed = super.mouseClick(componentArgument, button, x, y, count);

                FileBrowserSheet.Mode mode = fileBrowserSheet.getMode();

                if (count == 1) {
                    file = fileBrowser.getFileAt(x, y);
                } else if (count == 2) {
                    File fileLocal = fileBrowser.getFileAt(x, y);

                    if (fileLocal != null
                        && this.file != null
                        && fileLocal.equals(this.file)
                        && fileBrowser.isFileSelected(fileLocal)) {
                        if (mode == FileBrowserSheet.Mode.OPEN
                            || mode == FileBrowserSheet.Mode.OPEN_MULTIPLE) {
                            if (!fileLocal.isDirectory()) {
                                fileBrowserSheet.close(true);
                                consumed = true;
                            }
                        }
                    }
                }

                return consumed;
            }
        });

        okButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                fileBrowserSheet.close(true);
            }
        });

        cancelButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                fileBrowserSheet.close(false);
            }
        });

        // Add this as a file browser sheet listener
        fileBrowserSheet.getFileBrowserSheetListeners().add(this);

        modeChanged(fileBrowserSheet, null);
        rootDirectoryChanged(fileBrowserSheet, null);
        selectedFilesChanged(fileBrowserSheet, null);
    }

    public boolean isHideDisabledFiles() {
        return (Boolean)fileBrowser.getStyles().get("hideDisabledFiles");
    }

    public void setHideDisabledFiles(boolean hideDisabledFiles) {
        fileBrowser.getStyles().put("hideDisabledFiles", hideDisabledFiles);
    }

    public boolean getShowOKButtonFirst() {
        Container parent = okButton.getParent();
        return parent.indexOf(okButton) < parent.indexOf(cancelButton);
    }

    public void setShowOKButtonFirst(boolean showOKButtonFirst) {
        if (showOKButtonFirst != getShowOKButtonFirst()) {
            Container parent = okButton.getParent();
            parent.remove(okButton);
            parent.remove(cancelButton);

            if (showOKButtonFirst) {
                parent.add(okButton);
                parent.add(cancelButton);
            } else {
                parent.add(cancelButton);
                parent.add(okButton);
            }
        }
    }

    @Override
    public void windowOpened(Window window) {
        super.windowOpened(window);
        window.requestFocus();
    }

    @Override
    public Vote previewSheetClose(final Sheet sheet, final boolean result) {
        Vote vote = null;

        if (result
            && !okButton.isEnabled()) {
            vote = Vote.DENY;
        } else {
            if (result) {
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
                        // Contents of the entry field could be:
                        // 1. Just a new file name in the current root directory
                        // 2. A relative or absolute path that is an existing directory
                        //    to navigate to
                        // 3. A relative or absolute path including the new file name
                        //    in an existing directory
                        // So, first make it an absolute path
                        File selectedFile = new File(fileName);
                        if (!selectedFile.isAbsolute() && !fileName.startsWith(File.separator)) {
                            selectedFile = new File(fileBrowser.getRootDirectory(), fileName);
                        } else {
                            selectedFile = selectedFile.getAbsoluteFile();
                        }
                        if (selectedFile.exists() && selectedFile.isDirectory()) {
                            try {
                                File root = selectedFile.getCanonicalFile();
                                fileBrowserSheet.setRootDirectory(root);
                                fileBrowser.setRootDirectory(root);
                                saveAsTextInput.setText("");
                            } catch (IOException ioe) {
                                Form.setFlag(saveAsBoxPane, new Form.Flag());
                            }
                            selectedFile = null;
                            vote = Vote.DENY;
                        } else {
                            File root = selectedFile.getParentFile();
                            if (root != null && root.exists() && root.isDirectory()) {
                                try {
                                    fileBrowserSheet.setRootDirectory(root.getCanonicalFile());
                                    selectedFile = new File(selectedFile.getName());
                                }
                                catch (IOException ioe) {
                                    Form.setFlag(saveAsBoxPane, new Form.Flag());
                                    selectedFile = null;
                                    vote = Vote.DENY;
                                }
                            } else {
                                // Could be an error message here ("Directory does not exist")
                                Form.setFlag(saveAsBoxPane, new Form.Flag());
                                selectedFile = null;
                                vote = Vote.DENY;
                            }
                        }
                        if (selectedFile != null) {
                            fileBrowserSheet.setSelectedFiles(new ArrayList<File>(selectedFile));
                        }
                        break;
                    }

                    default: {
                        break;
                    }
                }

                updatingSelection = false;
            }
            if (vote == null) {
                vote = super.previewSheetClose(sheet, result);
            }
        }

        return vote;
    }

    @Override
    public void modeChanged(FileBrowserSheet fileBrowserSheet,
        FileBrowserSheet.Mode previousMode) {
        FileBrowserSheet.Mode mode = fileBrowserSheet.getMode();

        fileBrowser.getStyles().put("keyboardFolderTraversalEnabled",
            (mode != FileBrowserSheet.Mode.SAVE_TO));

        switch (mode) {
            case OPEN: {
                saveAsBoxPane.setVisible(false);
                fileBrowser.setMultiSelect(false);
                break;
            }

            case OPEN_MULTIPLE: {
                saveAsBoxPane.setVisible(false);
                fileBrowser.setMultiSelect(true);
                break;
            }

            case SAVE_AS: {
                saveAsBoxPane.setVisible(true);
                fileBrowser.setMultiSelect(false);
                break;
            }

            case SAVE_TO: {
                saveAsBoxPane.setVisible(false);
                fileBrowser.setMultiSelect(false);
                break;
            }

            default: {
                break;
            }
        }

        updateDisabledFileFilter();
        updateOKButtonState();
    }

    @Override
    public void rootDirectoryChanged(FileBrowserSheet fileBrowserSheet,
        File previousRootDirectory) {
        if (!updatingSelection) {
            fileBrowser.setRootDirectory(fileBrowserSheet.getRootDirectory());
        }
    }

    @Override
    public void selectedFilesChanged(FileBrowserSheet fileBrowserSheet,
        Sequence<File> previousSelectedFiles) {
        if (!updatingSelection) {
            Sequence<File> selectedFiles = fileBrowserSheet.getSelectedFiles();
            fileBrowser.setSelectedFiles(selectedFiles);

            if (fileBrowser.getSelectedFiles().getLength() == 0
                && selectedFiles.getLength() == 1) {
                // The file does not currently exist; set the file name in the
                // text input if the parent directory is the same as the root
                // directory
                File selectedFile = selectedFiles.get(0);

                File rootDirectory = fileBrowser.getRootDirectory();
                if (rootDirectory.equals(selectedFile.getParentFile())) {
                    saveAsTextInput.setText(selectedFile.getName());
                }
            }
        }
    }

    @Override
    public void disabledFileFilterChanged(FileBrowserSheet fileBrowserSheet,
        Filter<File> previousDisabledFileFilter) {
        updateDisabledFileFilter();
    }

    private void updateDisabledFileFilter() {
        FileBrowserSheet fileBrowserSheet = (FileBrowserSheet)getComponent();
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
                okButton.setEnabled(saveAsTextInput.getCharacterCount() > 0);
                break;
            }

            case SAVE_TO: {
                okButton.setEnabled(selectedDirectoryCount > 0);
                break;
            }

            default: {
                break;
            }
        }
    }

    public void addComponent(Component component) {
        TablePane.Row row = new TablePane.Row(-1);
        row.add(component);
        Sequence<TablePane.Row> rows = tablePane.getRows();
        rows.insert(row, rows.getLength() - 1);
    }
}
