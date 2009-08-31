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
package org.apache.pivot.wtk.media;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;


/**
 * Implementation of the {@link Serializer} interface that reads and writes
 * instances of {@link java.awt.image.BufferedImage}.
 */
public class BufferedImageSerializer implements Serializer<BufferedImage> {
    /**
     * Supported image formats.
     */
    public enum Format {
        PNG("png", "image/png"),
        JPEG("jpeg", "image/jpeg"),
        BMP("bmp", "image/bmp"),
        WBMP("wbmp", "image/vnd.wap.wbmp"),
        GIF("gif", "image/gif");

        private String name;
        private String mimeType;

        private Format(String name, String mimeType) {
            this.name = name;
            this.mimeType = mimeType;
        }

        public String getName() {
            return name;
        }

        public String getMIMEType() {
            return mimeType;
        }
    }

    private Format outputFormat;

    public BufferedImageSerializer() {
        this(Format.PNG);
    }

    public BufferedImageSerializer(Format outputFormat) {
        setOutputFormat(outputFormat);
    }

    /**
     * Gets the image format that this serializer is using for output.
     */
    public Format getOutputFormat() {
        return outputFormat;
    }

    /**
     * Sets the image format that this serializer should use for output.
     */
    public void setOutputFormat(Format outputFormat) {
        if (outputFormat == null) {
            throw new IllegalArgumentException("Output format is null.");
        }

        this.outputFormat = outputFormat;
    }

    /**
     * Reads a serialized image from an input stream.
     *
     * @return
     * A <tt>BufferedImage</tt> object
     */
    @Override
    public BufferedImage readObject(InputStream inputStream) throws IOException,
        SerializationException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is null.");
        }

        BufferedImage bufferedImage = ImageIO.read(inputStream);
        return bufferedImage;
    }


    /**
     * Writes a buffered image to an output stream.
     */
    @Override
    public void writeObject(BufferedImage bufferedImage, OutputStream outputStream)
        throws IOException, SerializationException {
        if (bufferedImage == null) {
            throw new IllegalArgumentException("bufferedImage is null.");
        }

        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream is null.");
        }

        ImageIO.write(bufferedImage, outputFormat.getName(), outputStream);
    }

    @Override
    public String getMIMEType(BufferedImage bufferedImage) {
        return outputFormat.getMIMEType();
    }
}
