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
package org.apache.pivot.tests.issues;

import java.io.File;
import java.io.FileFilter;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;

public class Pivot800 implements Application {
    private FileBrowserSheet sheet;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        Window window = new Window();
        window.setMaximized(true);
        sheet = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO);
        sheet.getWindowStateListeners().add(new SelectFileListener());
        window.open(display);
        sheet.open(window);
    }

    private class SelectFileListener implements WindowStateListener {
        @Override
        public void windowOpened(Window window) {
            File homeFolder = new File(System.getProperty("user.home"));
            File firstFolderInHome = homeFolder.listFiles(new FolderFilter())[0];
            sheet.setRootDirectory(homeFolder);
            System.out.println("selecting file " + firstFolderInHome);
            sheet.setSelectedFile(firstFolderInHome);
            System.out.println("Selected file is " + sheet.getSelectedFile()
                + ", but button state is not updated!");
        }
    }

    private class FolderFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(new String[] {Pivot800.class.getName()});
    }

}
