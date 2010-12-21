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
import java.nio.charset.Charset;

/**
 * Implementation of the {@link Serializer} interface that reads data from
 * and writes data to Java Strings.
 */
public class StringSerializer implements Serializer<String> {
    private final Charset charset;

    public static final String DEFAULT_CHARSET_NAME = "UTF-8";
    public static final String TEXT_EXTENSION = "txt";
    public static final String MIME_TYPE = "text/plain";
    public static final int BUFFER_SIZE = 2048;

    public StringSerializer() {
        this(Charset.forName(DEFAULT_CHARSET_NAME));
    }

    public StringSerializer(Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("charset is null.");
        }

        this.charset = charset;
    }

    public Charset getCharset() {
        return charset;
    }

    /**
     * Reads plain text data from an input stream.
     *
     * @param inputStream
     * The input stream from which data will be read.
     *
     * @return
     * An instance of {@link String} containing the text read from the input stream.
     */
    @Override
    public String readObject(InputStream inputStream) throws IOException, SerializationException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is null.");
        }

        String result = null;

        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = bufferedInputStream.read(buffer)) != -1) {
                byteOutputStream.write(buffer, 0, read);
            }

            byteOutputStream.flush();

            result = new String(byteOutputStream.toByteArray(), charset);
        } catch (IOException exception) {
            throw new SerializationException(exception);
        }

        return result;
    }

    /**
     * Writes plain text data to an output stream.
     *
     * @param text
     * The text to be written to the output stream.
     *
     * @param outputStream
     * The output stream to which data will be written.
     */
    @Override
    public void writeObject(String text, OutputStream outputStream)
        throws IOException, SerializationException {
        if (text == null) {
            throw new IllegalArgumentException("text is null.");
        }

        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream is null.");
        }

        try {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            bufferedOutputStream.write(text.getBytes());
            bufferedOutputStream.flush();
        } catch (IOException exception) {
            throw new SerializationException(exception);
        }
    }

    @Override
    public String getMIMEType(String object) {
        return MIME_TYPE;
    }
}
