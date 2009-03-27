/*
 * Copyright (c) 2009 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.demos.dnd;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.io.FileList;
import pivot.io.Folder;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.Display;
import pivot.wtk.DropAction;
import pivot.wtk.DropTarget;
import pivot.wtk.Keyboard;
import pivot.wtk.Manifest;
import pivot.wtk.MessageType;
import pivot.wtk.Prompt;
import pivot.wtk.PushButton;
import pivot.wtk.Span;
import pivot.wtk.TableView;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class FileDropTargetDemo implements Application {
    private Window window = null;

    private FileList fileList = null;
    private TableView fileTableView = null;
    private PushButton uploadButton = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = new Window((Component)wtkxSerializer.readObject(getClass().getResource("file_drop_target_demo.wtkx")));

        fileTableView = (TableView)wtkxSerializer.getObjectByName("fileTableView");

        fileList = new FileList();
        fileTableView.setTableData(fileList);

        fileList.getListListeners().add(new ListListener<File>() {
            public void itemInserted(List<File> list, int index) {
                uploadButton.setEnabled(list.getLength() > 0);
            }

            public void itemsRemoved(List<File> list, int index, Sequence<File> files) {
                uploadButton.setEnabled(list.getLength() > 0);

                if (fileTableView.isFocused()
                    && index < list.getLength()) {
                    fileTableView.setSelectedIndex(index);
                }
            }

            public void itemUpdated(List<File> list, int index, File previousFile) {
                // No-op
            }

            public void comparatorChanged(List<File> fileList, Comparator<File> previousComparator) {
                // No-op
            }
        });

        fileTableView.getComponentKeyListeners().add(new ComponentKeyListener() {
            public boolean keyTyped(Component component, char character) {
                return false;
            }

            public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                if (keyCode == Keyboard.KeyCode.DELETE
                    || keyCode == Keyboard.KeyCode.BACKSPACE) {
                    Sequence<Span> selectedRanges = fileTableView.getSelectedRanges();

                    for (int i = selectedRanges.getLength() - 1; i >= 0; i--) {
                        Span range = selectedRanges.get(i);
                        int index = range.getStart();
                        int count = range.getEnd() - index + 1;
                        fileList.remove(index, count);
                    }
                }

                return false;
            }

            public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                return false;
            }
        });

        fileTableView.setDropTarget(new DropTarget() {
            public DropAction dragEnter(Component component, Manifest dragContent,
                int supportedDropActions, DropAction userDropAction) {
                DropAction dropAction = null;

                if (dragContent.containsFileList()
                    && DropAction.COPY.isSelected(supportedDropActions)) {
                    dropAction = DropAction.COPY;
                }

                return dropAction;
            }

            public void dragExit(Component component) {
            }

            public DropAction dragMove(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (dragContent.containsFileList() ? DropAction.COPY : null);
            }

            public DropAction userDropActionChange(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (dragContent.containsFileList() ? DropAction.COPY : null);
            }

            public DropAction drop(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                DropAction dropAction = null;

                if (dragContent.containsFileList()) {
                    try {
                        FileList tableData = (FileList)fileTableView.getTableData();
                        FileList fileList = dragContent.getFileList();
                        for (File file : fileList) {
                            if (file instanceof Folder) {
                                tableData.add((Folder)file);
                            } else {
                                tableData.add(file);
                            }
                        }

                        dropAction = DropAction.COPY;
                    } catch(IOException exception) {
                        System.err.println(exception);
                    }
                }

                dragExit(component);

                return dropAction;
            }
        });

        uploadButton = (PushButton)wtkxSerializer.getObjectByName("uploadButton");
        uploadButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Prompt.prompt(MessageType.INFO, "Pretending to upload...", window);
            }
        });

        window.setTitle("File Drop Target Demo");
        window.setMaximized(true);
        window.open(display);
    }

    public boolean shutdown(boolean optional) throws Exception {
        if (window != null) {
            window.close();
        }

        window = null;

        return false;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
