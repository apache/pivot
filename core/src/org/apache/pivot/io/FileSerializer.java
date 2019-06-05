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
package org.apache.pivot.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import javax.activation.MimetypesFileTypeMap;

import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;
import org.apache.pivot.util.Constants;

/**
 * Implementation of the {@link Serializer} interface that reads and writes
 * {@link java.io.File} objects.
 */
public class FileSerializer implements Serializer<File> {
    private File tempFileDirectory;

    private static final MimetypesFileTypeMap MIME_TYPES_FILE_MAP = new MimetypesFileTypeMap();

    /**
     * Creates a new file serializer that will store temporary files in the
     * default temporary file directory.
     */
    public FileSerializer() {
        this(null);
    }

    /**
     * Creates a new file serializer that will store temporary files in a
     * specific directory.
     *
     * @param tempFileDirectory The directory in which to store temporary
     * files (can be {@code null} to use the system default location).
     */
    public FileSerializer(final File tempFileDirectory) {
        if (tempFileDirectory != null && !tempFileDirectory.isDirectory()) {
            throw new IllegalArgumentException("Temp file directory '" + tempFileDirectory + "' is not a directory.");
        }

        this.tempFileDirectory = tempFileDirectory;
    }

    /**
     * Reads a file from an input stream. The returned file is a temporary file
     * and must be deleted by the caller.
     */
    @Override
    public File readObject(final InputStream inputStream) throws IOException, SerializationException {
        File file = File.createTempFile(getClass().getName(), null, tempFileDirectory);

        try (OutputStream outputStream =
                 new BufferedOutputStream(Files.newOutputStream(file.toPath()), Constants.BUFFER_SIZE)) {
            for (int data = inputStream.read(); data != -1; data = inputStream.read()) {
                outputStream.write((byte) data);
            }
        }

        return file;
    }

    /**
     * Writes a file to an output stream.
     */
    @Override
    public void writeObject(final File file, final OutputStream outputStream) throws IOException,
            SerializationException {
        try (InputStream inputStream =
                new BufferedInputStream(Files.newInputStream(file.toPath()), Constants.BUFFER_SIZE)) {
            for (int data = inputStream.read(); data != -1; data = inputStream.read()) {
                outputStream.write((byte) data);
            }
        }
    }

    @Override
    public String getMIMEType(final File file) {
        return MIME_TYPES_FILE_MAP.getContentType(file);
    }
}
