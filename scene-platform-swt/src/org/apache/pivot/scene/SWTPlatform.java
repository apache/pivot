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
package org.apache.pivot.scene;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.pivot.scene.Font;
import org.apache.pivot.scene.LinearGradientPaint;
import org.apache.pivot.scene.PathGeometry;
import org.apache.pivot.scene.Platform;
import org.apache.pivot.scene.RadialGradientPaint;
import org.apache.pivot.scene.SolidColorPaint;
import org.apache.pivot.scene.Stroke;
import org.apache.pivot.scene.media.Raster;
import org.eclipse.swt.graphics.Pattern;

/**
 * AWT platform implementation.
 */
public class SWTPlatform extends Platform {
    @Override
    public Font.Metrics getFontMetrics(Font font) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public float measureText(Font font, CharSequence text, int start, int length) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Raster readRaster(InputStream inputStream) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void writeRaster(Raster raster, String mimeType,  OutputStream outputStream)
        throws IOException {
    }

    @Override
    protected org.eclipse.swt.graphics.Font getNativeFont(Font font) {
        // TODO Return a FontReference; this will be a wrapper class that
        // contains both a reference to the Font itself as well as the
        // native font instance. In the FontReference finalizer(), we'll
        // remove the entry from the reference-counted map and dispose
        // the native font, if necessary. We'll also dispose() all
        // undisposed resources when the stage host component is disposed.

        return null;
    }

    @Override
    protected org.eclipse.swt.graphics.Color getNativePaint(SolidColorPaint solidColorPaint) {
        // TODO Return a ColorReference
        return null;
    }

    @Override
    protected Pattern getNativePaint(LinearGradientPaint linearGradientPaint) {
        // TODO Return a PatternReference
        return null;
    }

    @Override
    protected Pattern getNativePaint(RadialGradientPaint radialGradientPaint) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object getNativeStroke(Stroke stroke) {
        return null;
    }

    @Override
    protected Object getNativePathGeometry(PathGeometry pathGeometry) {
        // TODO
        return null;
    }
}
