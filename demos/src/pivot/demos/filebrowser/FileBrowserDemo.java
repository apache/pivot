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
package pivot.demos.filebrowser;

import java.io.File;
import java.net.MalformedURLException;

import pivot.collections.Dictionary;
import pivot.io.Folder;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Display;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.TreeView;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class FileBrowserDemo implements Application {
    private Window window = null;
    private TreeView folderTreeView = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = new Window((Component)wtkxSerializer.readObject(getClass().getResource("file_browser_demo.wtkx")));

        folderTreeView = (TreeView)wtkxSerializer.getObjectByName("folderTreeView");

        String pathname = System.getProperty("user.home");
        folderTreeView.setTreeData(new Folder(pathname));

        folderTreeView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener() {
            public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
                return false;
            }

            public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
                return false;
            }

            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
                if (count == 2) {
                    openSelectedFile();
                }

                return false;
            }
        });

        folderTreeView.getComponentKeyListeners().add(new ComponentKeyListener() {
            public boolean keyTyped(Component component, char character) {
                return false;
            }

            public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                if (keyCode == Keyboard.KeyCode.ENTER) {
                    openSelectedFile();
                }

                return false;
            }

            public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                return false;
            }
        });

        window.setTitle("File Browser Demo");
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

    private void openSelectedFile() {
        File file = (File)folderTreeView.getSelectedNode();
        try {
            ApplicationContext.open(file.toURI().toURL());
        } catch(MalformedURLException exception) {
            // No-op
        }
    }
}
