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
package org.apache.pivot.tutorials.filebrowsing;

import java.io.File;

import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.CardPane;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FileBrowser;
import org.apache.pivot.wtk.FileBrowserListener;
import org.apache.pivot.wtk.Panel;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

public class FileBrowsers implements Application {
    private Window window = null;

    @WTKX private CardPane cardPane = null;
    @WTKX private TabPane tabPane = null;
    @WTKX private PushButton openFileButton = null;
    @WTKX private FileBrowser fileBrowser = null;
    @WTKX private PushButton okButton = null;

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(getClass().getResource("file_browsers.wtkx"));
        wtkxSerializer.bind(this, FileBrowsers.class);

        openFileButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                cardPane.setSelectedIndex(1);
            }
        });

        fileBrowser.getFileBrowserListeners().add(new FileBrowserListener.Adapter() {
            @Override
            public void selectedFilesChanged(FileBrowser fileBrowser, Sequence<File> previousSelectedFiles) {
                File selectedFile = fileBrowser.getSelectedFile();
                okButton.setEnabled(selectedFile != null
                    && !selectedFile.isDirectory());
            }
        });

        okButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                File selectedFile = fileBrowser.getSelectedFile();
                String fileName = selectedFile.getName();

                Panel panel = new Panel();
                tabPane.getTabs().add(panel);

                TabPane.setLabel(panel, fileName);

                cardPane.setSelectedIndex(0);
            }
        });

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(FileBrowsers.class, args);
    }
}
