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

import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.FileBrowserSheetListener;

/**
 * Terra file browser sheet skin.
 *
 * @author gbrown
 */
public class TerraFileBrowserSheetSkin extends TerraSheetSkin
    implements FileBrowserSheetListener {
    @Override
    public void install(Component component) {
        super.install(component);

        FileBrowserSheet fileBrowserSheet = (FileBrowserSheet)component;
        fileBrowserSheet.getFileBrowserSheetListeners().add(this);

        // TODO Add components
    }

    @Override
    public void uninstall() {
        FileBrowserSheet fileBrowserSheet = (FileBrowserSheet)getComponent();
        fileBrowserSheet.getFileBrowserSheetListeners().remove(this);

        // TODO Remove components

        super.uninstall();
    }

    @Override
    public void selectedFileChanged(FileBrowserSheet fileBrowserSheet, File previousSelectedFile) {
        // TODO
    }
}
