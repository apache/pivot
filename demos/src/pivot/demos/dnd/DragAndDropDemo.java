/*
 * Copyright (c) 2008 VMware, Inc.
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

import java.io.IOException;

import pivot.collections.Dictionary;
import pivot.io.FileList;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Clipboard;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.DragSource;
import pivot.wtk.DropAction;
import pivot.wtk.DropTarget;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.ListView;
import pivot.wtk.LocalManifest;
import pivot.wtk.Manifest;
import pivot.wtk.Point;
import pivot.wtk.PushButton;
import pivot.wtk.Visual;
import pivot.wtk.Window;
import pivot.wtk.media.Image;
import pivot.wtkx.WTKXSerializer;

public class DragAndDropDemo implements Application {
    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = new Window((Component)wtkxSerializer.readObject(getClass().getResource("drag_and_drop.wtkx")));

        // Text
        final Label label = (Label)wtkxSerializer.getObjectByName("label");
        label.setDragSource(new DragSource() {
            private LocalManifest content = null;

            public boolean beginDrag(Component component, int x, int y) {
                String text = label.getText();
                if (text != null) {
                    content = new LocalManifest();
                    content.putText(label.getText());
                }

                return (content != null);
            }

            public void endDrag(Component component, DropAction dropAction) {
                content = null;
            }

            public boolean isNative() {
                return true;
            }

            public LocalManifest getContent() {
                return content;
            }

            public Visual getRepresentation() {
                return null;
            }

            public Point getOffset() {
                return null;
            }

            public int getSupportedDropActions() {
                return DropAction.COPY.getMask();
            }
        });

        label.setDropTarget(new DropTarget() {
            public DropAction dragEnter(Component component, Manifest dragContent,
                int supportedDropActions, DropAction userDropAction) {
                DropAction dropAction = null;

                if (dragContent.containsText()
                    && DropAction.COPY.isSelected(supportedDropActions)) {
                    dropAction = DropAction.COPY;
                }

                return dropAction;
            }

            public void dragExit(Component component) {
            }

            public DropAction dragMove(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (dragContent.containsText() ? DropAction.COPY : null);
            }

            public DropAction userDropActionChange(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (dragContent.containsText() ? DropAction.COPY : null);
            }

            public DropAction drop(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                DropAction dropAction = null;

                if (dragContent.containsText()) {
                    try {
                        label.setText(dragContent.getText());
                        dropAction = DropAction.COPY;
                    } catch(IOException exception) {
                        System.err.println(exception);
                    }
                }

                dragExit(component);

                return dropAction;
            }
        });

        PushButton copyTextButton = (PushButton)wtkxSerializer.getObjectByName("copyTextButton");
        copyTextButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                String text = label.getText();
                LocalManifest clipboardContent = new LocalManifest();
                clipboardContent.putText(text);
                Clipboard.setContent(clipboardContent);
            }
        });

        PushButton pasteTextButton = (PushButton)wtkxSerializer.getObjectByName("pasteTextButton");
        pasteTextButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Manifest clipboardContent = Clipboard.getContent();

                if (clipboardContent != null
                    && clipboardContent.containsText()) {
                    try {
                        label.setText(clipboardContent.getText());
                    } catch(IOException exception) {
                        System.err.println(exception);
                    }
                }
            }
        });

        // Images
        final ImageView imageView = (ImageView)wtkxSerializer.getObjectByName("imageView");
        imageView.setDragSource(new DragSource() {
            private LocalManifest content = null;

            public boolean beginDrag(Component component, int x, int y) {
                Image image = imageView.getImage();

                if (image != null) {
                    content = new LocalManifest();
                    content.putImage(image);
                }

                return (content != null);
            }

            public void endDrag(Component component, DropAction dropAction) {
                content = null;
            }

            public boolean isNative() {
                return true;
            }

            public LocalManifest getContent() {
                return content;
            }

            public Visual getRepresentation() {
                return null;
            }

            public Point getOffset() {
                return null;
            }

            public int getSupportedDropActions() {
                return DropAction.COPY.getMask();
            }
        });

        imageView.setDropTarget(new DropTarget() {
            public DropAction dragEnter(Component component, Manifest dragContent,
                int supportedDropActions, DropAction userDropAction) {
                DropAction dropAction = null;

                if (dragContent.containsImage()
                    && DropAction.COPY.isSelected(supportedDropActions)) {
                    dropAction = DropAction.COPY;
                }

                return dropAction;
            }

            public void dragExit(Component component) {
            }

            public DropAction dragMove(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (dragContent.containsImage() ? DropAction.COPY : null);
            }

            public DropAction userDropActionChange(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (dragContent.containsImage() ? DropAction.COPY : null);
            }

            public DropAction drop(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                DropAction dropAction = null;

                if (dragContent.containsImage()) {
                    try {
                        imageView.setImage(dragContent.getImage());
                        dropAction = DropAction.COPY;
                    } catch(IOException exception) {
                        System.err.println(exception);
                    }
                }

                dragExit(component);

                return dropAction;
            }
        });

        PushButton copyImageButton = (PushButton)wtkxSerializer.getObjectByName("copyImageButton");
        copyImageButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Image image = imageView.getImage();
                if (image != null) {
                    LocalManifest clipboardContent = new LocalManifest();
                    clipboardContent.putImage(image);
                    Clipboard.setContent(clipboardContent);
                }
            }
        });

        PushButton pasteImageButton = (PushButton)wtkxSerializer.getObjectByName("pasteImageButton");
        pasteImageButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Manifest clipboardContent = Clipboard.getContent();

                if (clipboardContent != null
                    && clipboardContent.containsImage()) {
                    try {
                        imageView.setImage(clipboardContent.getImage());
                    } catch(IOException exception) {
                        System.err.println(exception);
                    }
                }
            }
        });

        // Files
        final ListView listView = (ListView)wtkxSerializer.getObjectByName("listView");
        listView.setListData(new FileList());

        listView.setDragSource(new DragSource() {
            private LocalManifest content = null;

            public boolean beginDrag(Component component, int x, int y) {
                ListView listView = (ListView)component;
                FileList fileList = (FileList)listView.getListData();

                if (fileList.getLength() > 0) {
                    content = new LocalManifest();
                    content.putFileList(fileList);
                }

                return (content != null);
            }

            public void endDrag(Component component, DropAction dropAction) {
                content = null;
            }

            public boolean isNative() {
                return true;
            }

            public LocalManifest getContent() {
                return content;
            }

            public Visual getRepresentation() {
                return null;
            }

            public Point getOffset() {
                return null;
            }

            public int getSupportedDropActions() {
                return DropAction.COPY.getMask();
            }
        });

        listView.setDropTarget(new DropTarget() {
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
                        listView.setListData(dragContent.getFileList());
                        dropAction = DropAction.COPY;
                    } catch(IOException exception) {
                        System.err.println(exception);
                    }
                }

                dragExit(component);

                return dropAction;
            }
        });

        PushButton copyFilesButton = (PushButton)wtkxSerializer.getObjectByName("copyFilesButton");
        copyFilesButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                // TODO
            }
        });

        PushButton pasteFilesButton = (PushButton)wtkxSerializer.getObjectByName("pasteFilesButton");
        pasteFilesButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                // TODO
            }
        });

        // Open the window
        window.setTitle("Drag and Drop Demo");
        window.setMaximized(true);
        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        window.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
