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
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.FileBrowser;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.skin.FileBrowserSkin;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

/**
 * Terra file browser skin.
 *
 * @author gbrown
 */
public class TerraFileBrowserSkin extends FileBrowserSkin {
    private Component content = null;

    @WTKX private ListButton pathListButton = null;
    @WTKX private PushButton goUpButton = null;
    @WTKX private PushButton newFolderButton = null;
    @WTKX private PushButton goHomeButton = null;

    @WTKX private ScrollPane fileScrollPane = null;
    @WTKX private TableView fileTableView = null;

    @Override
    public void install(Component component) {
        super.install(component);

        final FileBrowser fileBrowser = (FileBrowser)component;

        Resources resources;
        try {
            resources = new Resources(this);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        WTKXSerializer wtkxSerializer = new WTKXSerializer(resources);
        try {
            content = (Component)wtkxSerializer.readObject(this, "terra_file_browser_skin.wtkx");
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        fileBrowser.add(content);

        wtkxSerializer.bind(this, TerraFileBrowserSkin.class);

        pathListButton.getListButtonSelectionListeners().add(new ListButtonSelectionListener() {
            public void selectedIndexChanged(ListButton listButton, int previousSelectedIndex) {
                File directory = (File)listButton.getSelectedItem();

                if (directory != null) {
                    fileBrowser.setSelectedFolder(new Folder(directory.getPath()));
                }
            }
        });

        goUpButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Folder selectedFolder = fileBrowser.getSelectedFolder();
                File parentDirectory = selectedFolder.getParentFile();
                fileBrowser.setSelectedFolder(new Folder(parentDirectory.getPath()));
            }
        });

        newFolderButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                // TODO
            }
        });

        goHomeButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                fileBrowser.setSelectedFolder(new Folder(System.getProperty("user.home")));
            }
        });

        fileTableView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter() {
            private File selectedDirectory = null;

            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
                // TODO If we're in multi-select mode, we'll need to check
                // selection length
                if (count == 1) {
                    File selectedFile = (File)fileTableView.getSelectedRow();
                    if (selectedFile != null
                        && selectedFile.isDirectory()) {
                        selectedDirectory = selectedFile;
                    }
                } else if (count == 2) {
                    if (selectedDirectory == fileTableView.getSelectedRow()) {
                        fileBrowser.setSelectedFolder(new Folder(selectedDirectory.getPath()));
                    }
                }

                return false;
            }
        });

        selectedFolderChanged(fileBrowser, null);
    }

    @Override
    public void uninstall() {
        FileBrowser fileBrowser = (FileBrowser)getComponent();
        fileBrowser.remove(content);

        content = null;

        super.uninstall();
    }

    @Override
    public int getPreferredWidth(int height) {
        return content.getPreferredWidth(height);
    }

    @Override
    public int getPreferredHeight(int width) {
        return content.getPreferredHeight(width);
    }

    @Override
    public Dimensions getPreferredSize() {
        return content.getPreferredSize();
    }

    @Override
    public void layout() {
        int width = getWidth();
        int height = getHeight();

        content.setLocation(0, 0);
        content.setSize(width, height);
    }

    public void selectedFolderChanged(FileBrowser fileBrowser, Folder previousSelectedFolder) {
        ArrayList<File> path = new ArrayList<File>();

        Folder selectedFolder = fileBrowser.getSelectedFolder();

        File directory = selectedFolder.getParentFile();
        while (directory != null) {
            path.add(directory);
            directory = directory.getParentFile();
        }

        pathListButton.setListData(path);
        pathListButton.setButtonData(selectedFolder);

        goUpButton.setEnabled(selectedFolder.getParentFile() != null);

        File homeDirectory = new File(System.getProperty("user.home"));
        goHomeButton.setEnabled(!selectedFolder.equals(homeDirectory));

        fileScrollPane.setScrollTop(0);
        fileScrollPane.setScrollLeft(0);
        fileTableView.setTableData(selectedFolder);
    }

    public void selectedFileAdded(FileBrowser fileBrowser, File file) {
        // TODO
    }

    public void selectedFileRemoved(FileBrowser fileBrowser, File file) {
        // TODO
    }

    public void selectedFilesChanged(FileBrowser fileBrowser, Sequence<File> previousSelectedFiles) {
        // TODO
    }

    public void fileFilterChanged(FileBrowser fileBrowser, Filter<File> previousFileFilter) {
        // TODO
    }
}
