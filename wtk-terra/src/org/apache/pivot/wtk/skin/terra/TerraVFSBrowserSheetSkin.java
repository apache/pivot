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

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
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
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.VFSBrowser;
import org.apache.pivot.wtk.VFSBrowserListener;
import org.apache.pivot.wtk.VFSBrowserSheet;
import org.apache.pivot.wtk.VFSBrowserSheetListener;
import org.apache.pivot.wtk.Window;

/**
 * Terra Commons VFS browser sheet skin.
 */
public class TerraVFSBrowserSheetSkin extends TerraSheetSkin implements VFSBrowserSheetListener {

    private static class SaveToFileFilter implements Filter<FileObject> {
        public final Filter<FileObject> sourceFilter;

        public SaveToFileFilter(Filter<FileObject> sourceFilter) {
            this.sourceFilter = sourceFilter;
        }

        @Override
        public boolean include(FileObject file) {
            return (file.getName().getType() != FileType.FOLDER || (sourceFilter != null && sourceFilter.include(file)));
        }
    }

    @BXML
    private TablePane tablePane = null;
    @BXML
    private BoxPane hostNameBoxPane = null;
    @BXML
    private Label hostNameLabel = null;
    @BXML
    private BoxPane saveAsBoxPane = null;
    @BXML
    private TextInput saveAsTextInput = null;
    @BXML
    private VFSBrowser fileBrowser = null;
    @BXML
    private PushButton okButton = null;
    @BXML
    private PushButton cancelButton = null;

    private FileSystem fileSystem = null;
    private boolean updatingSelection = false;
    private int selectedDirectoryCount = 0;
    private static final Pattern HOST_PATTERN = Pattern.compile("[a-zA-Z]+://([a-zA-Z0-9\\-_\\.]+)(\\\\[a-zA-Z0-9\\-\\.]+)?:\\d+/.*");


    private void setHostLabel(FileObject rootDir) {
        try {
            if (rootDir != null) {
                hostNameBoxPane.setVisible(true);
                FileSystem localFileSystem = rootDir.getFileSystem();
                if (!localFileSystem.equals(fileSystem)) {
                    fileSystem = localFileSystem;
                    FileObject root = fileSystem.getRoot();
                    String rootURL = root.getURL().toString();
                    // Parse out the host name with some special considerations
                    Matcher m = HOST_PATTERN.matcher(rootURL);
                    if (m.matches())
                        hostNameLabel.setText(m.group(1));
                    else
                        hostNameLabel.setText(rootURL);
                }
            } else {
                hostNameBoxPane.setVisible(false);
            }
        }
        catch (FileSystemException ex) {
            throw new RuntimeException(ex);
        }
    }

    public TerraVFSBrowserSheetSkin() {
        setResizable(true);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        final VFSBrowserSheet fileBrowserSheet = (VFSBrowserSheet) component;
        fileBrowserSheet.setMinimumWidth(360);
        fileBrowserSheet.setMinimumHeight(180);

        // Load the sheet content
        BXMLSerializer bxmlSerializer = new BXMLSerializer();

        Component content;
        try {
            content = (Component) bxmlSerializer.readObject(TerraVFSBrowserSheetSkin.class,
                "terra_vfs_browser_sheet_skin.bxml", true);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        fileBrowserSheet.setContent(content);

        bxmlSerializer.bind(this, TerraVFSBrowserSheetSkin.class);

        // set the same rootDirectory as the component
        try {
            FileObject rootDirectory = fileBrowserSheet.getRootDirectory();
            fileBrowser.setRootDirectory(rootDirectory);
            setHostLabel(rootDirectory);
        } catch (FileSystemException fse) {
            throw new RuntimeException(fse);
        }

        // set the same homeDirectory as the component
        try {
            fileBrowser.setHomeDirectory(fileBrowserSheet.getHomeDirectory());
        } catch (FileSystemException fse) {
            throw new RuntimeException(fse);
        }

        saveAsTextInput.getTextInputContentListeners().add(new TextInputContentListener.Adapter() {
            @Override
            public void textChanged(TextInput textInput) {
                Form.clearFlag(saveAsBoxPane);
                updateOKButtonState();
            }
        });

        fileBrowser.getFileBrowserListeners().add(new VFSBrowserListener.Adapter() {
            @Override
            public void rootDirectoryChanged(VFSBrowser fileBrowserArgument,
                FileObject previousRootDirectory) {
                updatingSelection = true;

                try {
                    FileObject rootDirectory = fileBrowserArgument.getRootDirectory();
                    fileBrowserSheet.setRootDirectory(rootDirectory);
                    setHostLabel(rootDirectory);
                } catch (FileSystemException fse) {
                    throw new RuntimeException(fse);
                }

                updatingSelection = false;

                selectedDirectoryCount = 0;
                updateOKButtonState();
            }

            @Override
            public void homeDirectoryChanged(VFSBrowser fileBrowserArgument,
                FileObject previousHomeDirectory) {
                updatingSelection = true;

                try {
                    fileBrowserSheet.setHomeDirectory(fileBrowserArgument.getHomeDirectory());
                } catch (FileSystemException fse) {
                    throw new RuntimeException(fse);
                }
                updatingSelection = false;
            }

            @Override
            public void selectedFileAdded(VFSBrowser fileBrowserArgument, FileObject file) {
                if (file.getName().getType() == FileType.FOLDER) {
                    selectedDirectoryCount++;
                }

                updateOKButtonState();
            }

            @Override
            public void selectedFileRemoved(VFSBrowser fileBrowserArgument, FileObject file) {
                if (file.getName().getType() == FileType.FOLDER) {
                    selectedDirectoryCount--;
                }

                updateOKButtonState();
            }

            @Override
            public void selectedFilesChanged(VFSBrowser fileBrowserArgument,
                Sequence<FileObject> previousSelectedFiles) {
                selectedDirectoryCount = 0;

                Sequence<FileObject> selectedFiles = fileBrowserArgument.getSelectedFiles();
                for (int i = 0, n = selectedFiles.getLength(); i < n; i++) {
                    FileObject selectedFile = selectedFiles.get(i);

                    if (selectedFile.getName().getType() == FileType.FOLDER) {
                        selectedDirectoryCount++;
                    }
                }

                if (!fileBrowserArgument.isMultiSelect()) {
                    FileObject selectedFile = fileBrowserArgument.getSelectedFile();

                    if (selectedFile != null && selectedFile.getName().getType() != FileType.FOLDER) {
                        saveAsTextInput.setText(selectedFile.getName().getPath());
                    }
                }

                updateOKButtonState();
            }
        });

        fileBrowser.getComponentMouseButtonListeners().add(
            new ComponentMouseButtonListener.Adapter() {
                private FileObject file = null;

                @Override
                public boolean mouseClick(Component componentArgument, Mouse.Button button, int x,
                    int y, int count) {
                    boolean consumed = super.mouseClick(componentArgument, button, x, y, count);

                    VFSBrowserSheet.Mode mode = fileBrowserSheet.getMode();

                    if (count == 1) {
                        file = fileBrowser.getFileAt(x, y);
                    } else if (count == 2) {
                        FileObject fileLocal = fileBrowser.getFileAt(x, y);

                        if (fileLocal != null && this.file != null && fileLocal.equals(this.file)
                            && fileBrowser.isFileSelected(fileLocal)) {
                            if (mode == VFSBrowserSheet.Mode.OPEN
                                || mode == VFSBrowserSheet.Mode.OPEN_MULTIPLE) {
                                if (fileLocal.getName().getType() != FileType.FOLDER) {
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
        homeDirectoryChanged(fileBrowserSheet, null);
        rootDirectoryChanged(fileBrowserSheet, null);
        selectedFilesChanged(fileBrowserSheet, null);
    }

    public boolean isHideDisabledFiles() {
        return (Boolean) fileBrowser.getStyles().get("hideDisabledFiles");
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

        if (result && !okButton.isEnabled()) {
            vote = Vote.DENY;
        } else {
            if (result) {
                updatingSelection = true;

                VFSBrowserSheet fileBrowserSheet = (VFSBrowserSheet) sheet;
                VFSBrowserSheet.Mode mode = fileBrowserSheet.getMode();
                FileSystemManager manager = fileBrowserSheet.getManager();
                FileName baseFileName = fileBrowserSheet.getBaseFileName();

                switch (mode) {
                    case OPEN:
                    case OPEN_MULTIPLE:
                    case SAVE_TO: {
                        try {
                            fileBrowserSheet.setSelectedFiles(fileBrowser.getSelectedFiles());
                        } catch (FileSystemException fse) {
                            throw new RuntimeException(fse);
                        }
                        break;
                    }

                    case SAVE_AS: {
                        String fileName = saveAsTextInput.getText();
                        // Contents of the entry field could be:
                        // 1. Just a new file name in the current root directory
                        // 2. A relative or absolute path that is an existing
                        // directory
                        // to navigate to
                        // 3. A relative or absolute path including the new file
                        // name
                        // in an existing directory
                        // So, first make it an absolute path
                        // TODO: all this logic needs changing (not sure how)
                        // with VFS
                        // because you could type in a whole new URI and have to
                        // change
                        // managers
                        try {
                            FileObject selectedFile = manager.resolveFile(fileName);
                            // if (!selectedFile.isAbsolute() &&
                            // !fileName.startsWith(File.separator)) {
                            if (baseFileName == null
                                || !baseFileName.isAncestor(selectedFile.getName())) {
                                selectedFile = manager.resolveFile(fileBrowser.getRootDirectory(),
                                    fileName);
                            } else {
                                // TODO: is there really anything to do here?
                                // selectedFile =
                                // selectedFile.getAbsoluteFile();
                            }
                            if (selectedFile.exists() && selectedFile.getType() == FileType.FOLDER) {
                                try {
                                    // TODO: what to do about canonical file
                                    // representations?
                                    FileObject root = /*
                                                       * selectedFile.
                                                       * getCanonicalFile();
                                                       */selectedFile;
                                    fileBrowserSheet.setRootDirectory(root);
                                    fileBrowser.setRootDirectory(root);
                                    setHostLabel(root);
                                    saveAsTextInput.setText("");
                                } catch (IOException ioe) {
                                    Form.setFlag(saveAsBoxPane, new Form.Flag());
                                }
                                selectedFile = null;
                                vote = Vote.DENY;
                            } else {
                                FileObject root = selectedFile.getParent();
                                if (root != null && root.exists()
                                    && root.getType() == FileType.FOLDER) {
                                    try {
                                        // TODO: canonical file again
                                        // fileBrowserSheet.setRootDirectory(root.getCanonicalFile());
                                        fileBrowserSheet.setRootDirectory(root);
                                        setHostLabel(root);
                                        selectedFile = manager.resolveFile(selectedFile.getName().getURI());
                                    } catch (IOException ioe) {
                                        Form.setFlag(saveAsBoxPane, new Form.Flag());
                                        selectedFile = null;
                                        vote = Vote.DENY;
                                    }
                                } else {
                                    // Could be an error message here
                                    // ("Directory does not exist")
                                    Form.setFlag(saveAsBoxPane, new Form.Flag());
                                    selectedFile = null;
                                    vote = Vote.DENY;
                                }
                            }
                            if (selectedFile != null) {
                                fileBrowserSheet.setSelectedFiles(new ArrayList<>(selectedFile));
                            }
                        } catch (FileSystemException fse) {
                            Form.setFlag(saveAsBoxPane, new Form.Flag());
                            vote = Vote.DENY;
                        }
                        break;
                    }
                    default:
                        break;
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
    public void managerChanged(VFSBrowserSheet fileBrowserSheet, FileSystemManager previousManager) {
        // TODO: what to do here?
    }

    @Override
    public void modeChanged(VFSBrowserSheet fileBrowserSheet, VFSBrowserSheet.Mode previousMode) {
        VFSBrowserSheet.Mode mode = fileBrowserSheet.getMode();

        fileBrowser.getStyles().put("keyboardFolderTraversalEnabled",
            (mode != VFSBrowserSheet.Mode.SAVE_TO));

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
            default:
                break;
        }

        updateDisabledFileFilter();
        updateOKButtonState();
    }

    @Override
    public void rootDirectoryChanged(VFSBrowserSheet fileBrowserSheet,
        FileObject previousRootDirectory) {
        if (!updatingSelection) {
            try {
                FileObject rootDirectory = fileBrowserSheet.getRootDirectory();
                fileBrowser.setRootDirectory(rootDirectory);
                setHostLabel(rootDirectory);
            } catch (FileSystemException fse) {
                throw new RuntimeException(fse);
            }
        }
    }

    @Override
    public void homeDirectoryChanged(VFSBrowserSheet fileBrowserSheet,
        FileObject previousHomeDirectory) {
        if (!updatingSelection) {
            try {
                fileBrowser.setHomeDirectory(fileBrowserSheet.getHomeDirectory());
            } catch (FileSystemException fse) {
                throw new RuntimeException(fse);
            }
        }
    }

    @Override
    public void selectedFilesChanged(VFSBrowserSheet fileBrowserSheet,
        Sequence<FileObject> previousSelectedFiles) {
        if (!updatingSelection) {
            Sequence<FileObject> selectedFiles = fileBrowserSheet.getSelectedFiles();
            try {
                fileBrowser.setSelectedFiles(selectedFiles);
            } catch (FileSystemException fse) {
                throw new RuntimeException(fse);
            }

            if (fileBrowser.getSelectedFiles().getLength() == 0 && selectedFiles.getLength() == 1) {
                // The file does not currently exist; set the file name in the
                // text input if the parent directory is the same as the root
                // directory
                FileObject selectedFile = selectedFiles.get(0);

                try {
                    FileObject rootDirectory = fileBrowser.getRootDirectory();
                    if (rootDirectory.equals(selectedFile.getParent())) {
                        saveAsTextInput.setText(selectedFile.getName().getPath());
                    }
                } catch (FileSystemException fse) {
                    throw new RuntimeException(fse);
                }
            }
        }
    }

    @Override
    public void disabledFileFilterChanged(VFSBrowserSheet fileBrowserSheet,
        Filter<FileObject> previousDisabledFileFilter) {
        updateDisabledFileFilter();
    }

    private void updateDisabledFileFilter() {
        VFSBrowserSheet fileBrowserSheet = (VFSBrowserSheet) getComponent();
        Filter<FileObject> disabledFileFilter = fileBrowserSheet.getDisabledFileFilter();

        VFSBrowserSheet.Mode mode = fileBrowserSheet.getMode();
        if (mode == VFSBrowserSheet.Mode.SAVE_TO) {
            disabledFileFilter = new SaveToFileFilter(disabledFileFilter);
        }

        fileBrowser.setDisabledFileFilter(disabledFileFilter);
    }

    private void updateOKButtonState() {
        VFSBrowserSheet fileBrowserSheet = (VFSBrowserSheet) getComponent();

        VFSBrowserSheet.Mode mode = fileBrowserSheet.getMode();
        Sequence<FileObject> selectedFiles = fileBrowser.getSelectedFiles();

        switch (mode) {
            case OPEN:
            case OPEN_MULTIPLE: {
                okButton.setEnabled(selectedFiles.getLength() > 0 && selectedDirectoryCount == 0);
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
            default:
                break;
        }
    }

    public void addComponent(Component component) {
        TablePane.Row row = new TablePane.Row(-1);
        row.add(component);
        Sequence<TablePane.Row> rows = tablePane.getRows();
        rows.insert(row, rows.getLength() - 1);
    }
}
