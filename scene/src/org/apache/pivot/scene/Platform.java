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

import org.apache.pivot.scene.media.Raster;
import org.apache.pivot.util.Service;

/**
 * Abstraction of a scene graph host platform.
 */
public abstract class Platform {
    private static Platform platform = null;

    public static final Font DEFAULT_FONT = new Font("Verdana", 11);

    /**
     * The service provider name (see {@link Service#getProvider(String)}).
     */
    public static final String PROVIDER_NAME = "org.apache.pivot.scene.Platform";

    static {
        platform = (Platform)Service.getProvider(PROVIDER_NAME);

        if (platform == null) {
            throw new PlatformNotFoundException();
        }
    }

    public Font getDefaultFont() {
        return DEFAULT_FONT;
    }

    public abstract Font.Metrics getFontMetrics(Font font);

    public float measureText(Font font, CharSequence text) {
        return measureText(font, text, 0, text.length());
    }

    public abstract float measureText(Font font, CharSequence text, int start, int length);

    public abstract Raster readRaster(InputStream inputStream) throws IOException;
    public abstract void writeRaster(Raster raster, String mimeType, OutputStream outputStream)
        throws IOException;

    protected abstract Object getNativeFont(Font font);
    protected abstract Object getNativePaint(SolidColorPaint solidColorPaint);
    protected abstract Object getNativePaint(LinearGradientPaint linearGradientPaint);
    protected abstract Object getNativePaint(RadialGradientPaint radialGradientPaint);
    protected abstract Object getNativeStroke(Stroke stroke);
    protected abstract Object getNativePathGeometry(PathGeometry pathGeometry);

    // TODO Add methods to get the clipboard and mouse drag content

    public static Platform getPlatform() {
        if (platform == null) {
            throw new IllegalStateException("No installed platform.");
        }

        return platform;
    }
}
