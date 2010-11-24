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
package org.apache.pivot.serialization;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementation of the {@link Serializer} interface that reads and writes a
 * byte array.
 *
 * @see Serializer
 */
public class ByteArraySerializer implements Serializer<byte[]> {
    public static final String MIME_TYPE = "application/octet-stream";

    public static final int BUFFER_SIZE = 2048;

    /**
     * Reads a byte array from an input stream.
     */
    @Override
    public byte[] readObject(InputStream inputStream) throws IOException,
        SerializationException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is null.");
        }

        byte[] result = null;

        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = bufferedInputStream.read(buffer)) != -1) {
                byteOutputStream.write(buffer, 0, read);
            }

            byteOutputStream.flush();

            result = byteOutputStream.toByteArray();

        } catch (IOException exception) {
            throw new SerializationException(exception);
        }

        return result;
    }

    /**
     * Writes a byte array to an output stream.
     */
    @Override
    public void writeObject(byte[] bytes, OutputStream outputStream) throws IOException,
        SerializationException {
        if (bytes == null) {
            throw new IllegalArgumentException("byte array is null.");
        }

        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream is null.");
        }

        try {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            bufferedOutputStream.write(bytes);
            bufferedOutputStream.flush();
        } catch (IOException exception) {
            throw new SerializationException(exception);
        }

    }

    @Override
    public String getMIMEType(byte[] bytes) {
        return MIME_TYPE;
    }
}
