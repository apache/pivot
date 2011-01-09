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
package org.apache.pivot.scene.media;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.pivot.scene.Extents;
import org.apache.pivot.scene.Graphics;
import org.apache.pivot.scene.Node;
import org.apache.pivot.scene.Platform;

/**
 * Node encapsulating a raster.
 */
public class Image extends Node {
    private Raster raster;

    public Image() {
        this(null);
    }

    public Image(Raster raster) {
        this.raster = raster;
    }

    public Raster getRaster() {
        return raster;
    }

    public void setRaster(Raster raster) {
        this.raster = raster;
    }

    public void setRaster(URL location) throws IOException {
        setRaster(location.openStream());
    }

    public void setRaster(InputStream inputStream) throws IOException {
        setRaster(Platform.getPlatform().readRaster(inputStream));
    }

    @Override
    public Extents getExtents() {
        // TODO
        return null;
    }

    @Override
    public boolean contains(int x, int y) {
        // TODO
        return false;
    }

    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public int getPreferredWidth(int height) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getPreferredHeight(int width) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getBaseline(int width, int height) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void layout() {
        // TODO Auto-generated method stub

    }

    @Override
    public void paint(Graphics graphics) {
        // TODO Auto-generated method stub
    }

    // TODO Add resample() methods here as well as in Platform
}
