/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk.media;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import pivot.serialization.SerializationException;
import pivot.serialization.Serializer;

/**
 * Implementation of the {@link Serializer} interface that reads and writes
 * instances of {@link java.awt.image.BufferedImage}.
 *
 * @author tvolkert
 */
public class BufferedImageSerializer implements Serializer {
    /**
     * Supported image formats.
     *
     * @author tvolkert
     */
    public static enum Format {
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
    public Object readObject(InputStream inputStream) throws IOException,
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
    public void writeObject(Object object, OutputStream outputStream)
        throws IOException, SerializationException {
        if (object == null) {
            throw new IllegalArgumentException("object is null.");
        }

        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream is null.");
        }

        BufferedImage bufferedImage = (BufferedImage)object;
        ImageIO.write(bufferedImage, outputFormat.getName(), outputStream);
    }

    public String getMIMEType() {
        return outputFormat.getMIMEType();
    }
}
