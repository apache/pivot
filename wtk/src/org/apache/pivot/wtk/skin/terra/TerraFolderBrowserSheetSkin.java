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

import org.apache.pivot.io.Folder;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.FolderBrowserSheet;
import org.apache.pivot.wtk.FolderBrowserSheetListener;

/**
 * Terra folder browser sheet skin.
 *
 * @author gbrown
 */
public class TerraFolderBrowserSheetSkin extends TerraSheetSkin
    implements FolderBrowserSheetListener {
    @Override
    public void install(Component component) {
        super.install(component);

        FolderBrowserSheet folderBrowserSheet = (FolderBrowserSheet)component;
        folderBrowserSheet.getFolderBrowserSheetListeners().add(this);

        // TODO Add components
    }

    @Override
    public void uninstall() {
        FolderBrowserSheet folderBrowserSheet = (FolderBrowserSheet)getComponent();
        folderBrowserSheet.getFolderBrowserSheetListeners().remove(this);

        // TODO Remove components

        super.uninstall();
    }

    @Override
    public void selectedFolderChanged(FolderBrowserSheet folderBrowserSheet, Folder previousSelectedFolder) {
        // TODO
    }
}
