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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.MimetypesFileTypeMap;

import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;

/**
 * Implementation of the {@link Serializer} interface that reads and
 * writes {@link java.io.File} objects.
 */
public class FileSerializer implements Serializer<File> {
    private File tempFileDirectory;

    public static final int BUFFER_SIZE = 1024;

    private static final MimetypesFileTypeMap MIME_TYPES_FILE_MAP = new MimetypesFileTypeMap();

    /**
     * Creates a new file serializer that will store temporary files in the default
     * temporary file directory.
     */
    public FileSerializer() {
        this(null);
    }

    /**
     * Creates a new file serializer that will store temporary files in a specific
     * directory.
     *
     * @param tempFileDirectory
     * The directory in which to store temporary folders.
     */
    public FileSerializer(File tempFileDirectory) {
        if (tempFileDirectory != null
            && !tempFileDirectory.isDirectory()) {
            throw new IllegalArgumentException();
        }

        this.tempFileDirectory = tempFileDirectory;
    }

    /**
     * Reads a file from an input stream. The returned file is a temporary file and must be
     * deleted by the caller.
     */
    @Override
    public File readObject(InputStream inputStream) throws IOException, SerializationException {
        File file = File.createTempFile(getClass().getName(), null, tempFileDirectory);
        OutputStream outputStream = null;

        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE);
            for (int data = inputStream.read(); data != -1; data = inputStream.read()) {
                outputStream.write((byte)data);
            }
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }

        return file;
    }

    /**
     * Writes a file to an output stream.
     */
    @Override
    public void writeObject(File file, OutputStream outputStream) throws IOException,
        SerializationException {
        InputStream inputStream = null;

        try {
            inputStream = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
            for (int data = inputStream.read(); data != -1; data = inputStream.read()) {
                outputStream.write((byte)data);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    @Override
    public String getMIMEType(File file) {
        return MIME_TYPES_FILE_MAP.getContentType(file);
    }
}
