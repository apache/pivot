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
import org.apache.pivot.beans.BeanSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Resources;
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
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputTextListener;
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
        fileBrowserSheet.setMinimumPreferredWidth(360);
        fileBrowserSheet.setMinimumPreferredHeight(180);

        // Load the sheet content
        Resources resources;
        try {
            resources = new Resources(getClass().getName());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        BeanSerializer beanSerializer = new BeanSerializer(resources);

        Component content;
        try {
            content = (Component)beanSerializer.readObject(this, "terra_file_browser_sheet_skin.bxml");
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        fileBrowserSheet.setContent(content);

        beanSerializer.bind(this, TerraFileBrowserSheetSkin.class);

        saveAsTextInput.getTextInputTextListeners().add(new TextInputTextListener() {
            @Override
            public void textChanged(TextInput textInput) {
                updateOKButtonState();
            }
        });

        fileBrowser.getFileBrowserListeners().add(new FileBrowserListener.Adapter() {
            @Override
            public void rootDirectoryChanged(FileBrowser fileBrowser,
                File previousRootDirectory) {
                updatingSelection = true;

                fileBrowserSheet.setRootDirectory(fileBrowser.getRootDirectory());

                updatingSelection = false;

                selectedDirectoryCount = 0;
                updateOKButtonState();
            }

            @Override
            public void selectedFileAdded(FileBrowser fileBrowser, File file) {
                if (file.isDirectory()) {
                    selectedDirectoryCount++;
                }

                updateOKButtonState();
            }

            @Override
            public void selectedFileRemoved(FileBrowser fileBrowser, File file) {
                if (file.isDirectory()) {
                    selectedDirectoryCount--;
                }

                updateOKButtonState();
            }

            @Override
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
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
                boolean consumed = super.mouseClick(component, button, x, y, count);

                FileBrowserSheet.Mode mode = fileBrowserSheet.getMode();

                if (count == 1) {
                    file = fileBrowser.getFileAt(x, y);
                } else if (count == 2) {
                    File file = fileBrowser.getFileAt(x, y);

                    if (file != null
                        && this.file != null
                        && file.equals(this.file)
                        && fileBrowser.isFileSelected(file)) {
                        if (mode == FileBrowserSheet.Mode.OPEN
                            || mode == FileBrowserSheet.Mode.OPEN_MULTIPLE) {
                            if (!file.isDirectory()) {
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
                    File selectedFile = new File(fileBrowser.getRootDirectory(), fileName);
                    fileBrowserSheet.setSelectedFiles(new ArrayList<File>(selectedFile));
                    break;
                }
            }

            updatingSelection = false;
        }

        return vote;
    }

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
