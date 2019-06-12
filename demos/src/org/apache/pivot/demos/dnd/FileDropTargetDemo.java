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
package org.apache.pivot.demos.dnd;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.io.FileList;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.DropAction;
import org.apache.pivot.wtk.DropTarget;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Manifest;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.Window;

public class FileDropTargetDemo extends Window implements Bindable {
    @BXML private TableView fileTableView;
    @BXML private PushButton uploadButton;

    /** Flag whether to expand directories recursively when dropped. TODO: implement in GUI. */
    private static boolean expandDirectories = false;

    private FileList fileList = null;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        fileList = new FileList();
        fileTableView.setTableData(fileList);

        fileList.getListListeners().add(new ListListener<File>() {
            @Override
            public void itemInserted(List<File> list, int index) {
                uploadButton.setEnabled(list.getLength() > 0);
            }

            @Override
            public void itemsRemoved(List<File> list, int index, Sequence<File> files) {
                uploadButton.setEnabled(list.getLength() > 0);

                if (fileTableView.isFocused() && index < list.getLength()) {
                    fileTableView.setSelectedIndex(index);
                }
            }
        });

        fileTableView.getComponentKeyListeners().add(new ComponentKeyListener() {
            @Override
            public boolean keyPressed(Component component, int keyCode,
                Keyboard.KeyLocation keyLocation) {
                if (keyCode == Keyboard.KeyCode.DELETE || keyCode == Keyboard.KeyCode.BACKSPACE) {
                    Sequence<Span> selectedRanges = fileTableView.getSelectedRanges();

                    for (int i = selectedRanges.getLength() - 1; i >= 0; i--) {
                        Span range = selectedRanges.get(i);
                        int index = range.start;
                        int count = range.end - index + 1;
                        fileList.remove(index, count);
                    }
                }

                return false;
            }
        });

        fileTableView.setDropTarget(new DropTarget() {
            @Override
            public DropAction dragEnter(Component component, Manifest dragContent,
                int supportedDropActions, DropAction userDropAction) {
                DropAction dropAction = null;

                if (dragContent.containsFileList()
                    && DropAction.COPY.isSelected(supportedDropActions)) {
                    dropAction = DropAction.COPY;
                }

                return dropAction;
            }

            @Override
            public void dragExit(Component component) {
                // empty block
            }

            @Override
            public DropAction dragMove(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (dragContent.containsFileList() ? DropAction.COPY : null);
            }

            @Override
            public DropAction userDropActionChange(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (dragContent.containsFileList() ? DropAction.COPY : null);
            }

            private void addFile(File file, FileList tableData) {
                if (file.isDirectory()) {
                    tableData.add(file);
                    if (expandDirectories) {
                        for (File subdirFile : file.listFiles()) {
                            addFile(subdirFile, tableData);
                        }
                    }
                } else {
                    tableData.add(file);
                }
            }

            @Override
            public DropAction drop(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                DropAction dropAction = null;

                if (dragContent.containsFileList()) {
                    try {
                        FileList tableData = (FileList) fileTableView.getTableData();
                        for (File file : dragContent.getFileList()) {
                            addFile(file, tableData);
                        }

                        dropAction = DropAction.COPY;
                    } catch (IOException exception) {
                        System.err.println(exception);
                    }
                }

                dragExit(component);

                return dropAction;
            }
        });

        uploadButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                Prompt.prompt(MessageType.INFO, "Pretending to upload...", FileDropTargetDemo.this);
            }
        });
    }
}
