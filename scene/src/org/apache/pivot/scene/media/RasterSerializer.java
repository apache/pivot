package org.apache.pivot.scene.media;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.pivot.io.SerializationException;
import org.apache.pivot.io.Serializer;
import org.apache.pivot.scene.Platform;

public class RasterSerializer implements Serializer<Raster> {
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

    public RasterSerializer() {
        this(Format.PNG);
    }

    public RasterSerializer(Format outputFormat) {
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

    @Override
    public Raster readObject(InputStream inputStream) throws IOException, SerializationException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is null.");
        }

        return Platform.getPlatform().readRaster(inputStream);
    }

    @Override
    public void writeObject(Raster raster, OutputStream outputStream) throws IOException,
        SerializationException {
        if (raster == null) {
            throw new IllegalArgumentException("bufferedImage is null.");
        }

        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream is null.");
        }

        Platform.getPlatform().writeRaster(raster, outputFormat.getName(), outputStream);
    }

    @Override
    public String getMIMEType(Raster object) {
        return outputFormat.getMIMEType();
    }
}
