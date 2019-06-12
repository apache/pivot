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
import org.apache.pivot.util.Utils;

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
        BMP("bmp", "image/bmp"),
        GIF("gif", "image/gif"),
        JPEG("jpeg", "image/jpeg"),
        WBMP("wbmp", "image/vnd.wap.wbmp");

        private String name;
        private String mimeType;

        Format(final String name, final String mimeType) {
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

    /**
     * Construct an image serializer for the default {@link Format#PNG PNG} format.
     */
    public BufferedImageSerializer() {
        this(Format.PNG);
    }

    /**
     * Construct an image serializer for the given format.
     *
     * @param outputFormat The output format for this serializer.
     */
    public BufferedImageSerializer(final Format outputFormat) {
        setOutputFormat(outputFormat);
    }

    /**
     * @return The image format that this serializer is using for output.
     */
    public Format getOutputFormat() {
        return outputFormat;
    }

    /**
     * Sets the image format that this serializer should use for output.
     *
     * @param outputFormat The new format to use for output.
     */
    public void setOutputFormat(final Format outputFormat) {
        Utils.checkNull(outputFormat, "outputFormat");

        this.outputFormat = outputFormat;
    }

    /**
     * Reads a serialized image from an input stream.
     *
     * @param inputStream The stream to read the image from.
     * @return A <tt>BufferedImage</tt> object
     */
    @Override
    public BufferedImage readObject(final InputStream inputStream) throws IOException,
        SerializationException {
        Utils.checkNull(inputStream, "inputStream");

        BufferedImage bufferedImage = ImageIO.read(inputStream);
        return bufferedImage;
    }

    /**
     * Writes a buffered image to an output stream.
     *
     * @param bufferedImage The image to write out to the stream.
     * @param outputStream The stream to write the image out to.
     */
    @Override
    public void writeObject(final BufferedImage bufferedImage, final OutputStream outputStream)
        throws IOException, SerializationException {
        Utils.checkNull(bufferedImage, "bufferedImage");
        Utils.checkNull(outputStream, "outputStream");

        ImageIO.write(bufferedImage, outputFormat.getName(), outputStream);
    }

    @Override
    public final String getMIMEType(final BufferedImage bufferedImage) {
        return outputFormat.getMIMEType();
    }
}
