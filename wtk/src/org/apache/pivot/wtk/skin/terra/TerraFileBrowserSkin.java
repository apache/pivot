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

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.io.Folder;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.FileBrowser;
import org.apache.pivot.wtk.skin.FileBrowserSkin;
import org.apache.pivot.wtkx.WTKXSerializer;

/**
 * Terra file browser skin.
 *
 * @author gbrown
 */
public class TerraFileBrowserSkin extends FileBrowserSkin {
    private Component content = null;

    @Override
    public void install(Component component) {
        super.install(component);

        FileBrowser fileBrowser = (FileBrowser)component;

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
        // TODO
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
