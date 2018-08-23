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
import java.nio.charset.StandardCharsets;

import org.apache.pivot.util.Constants;
import org.apache.pivot.util.Utils;

/**
 * Implementation of the {@link Serializer} interface that reads data from and
 * writes data to Java Strings.  The text data is interpreted using either the
 * default <code>UTF-8</code> {@link Charset} or a <code>Charset</code> supplied
 * in the constructor.
 * <p> Instances of this class are reusable (and thread-safe) because no mutable
 * instance data is used in the {@link #readObject} and {@link #writeObject}
 * methods.
 */
public class StringSerializer implements Serializer<String> {
    private final Charset charset;

    public static final String TEXT_EXTENSION = "txt";
    public static final String MIME_TYPE = "text/plain";

    public StringSerializer() {
        this(StandardCharsets.UTF_8);
    }

    public StringSerializer(Charset charset) {
        Utils.checkNull(charset, "charset");

        this.charset = charset;
    }

    public Charset getCharset() {
        return charset;
    }

    /**
     * Reads plain text data from an input stream, interpreted by the given {@link Charset}.
     *
     * @param inputStream The input stream from which data will be read.
     * @return An instance of {@link String} containing the text read from the
     * input stream.
     * @see #getCharset
     */
    @Override
    public String readObject(InputStream inputStream) throws IOException, SerializationException {
        Utils.checkNull(inputStream, "inputStream");

        String result = null;

        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, Constants.BUFFER_SIZE);
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[Constants.BUFFER_SIZE];
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
     * Writes plain text data to an output stream, encoded in the given {@link Charset}.
     *
     * @param text The text to be written to the output stream.
     * @param outputStream The output stream to which data will be written.
     * @see #getCharset
     */
    @Override
    public void writeObject(String text, OutputStream outputStream) throws IOException,
        SerializationException {
        Utils.checkNull(text, "text");
        Utils.checkNull(outputStream, "outputStream");

        try {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, Constants.BUFFER_SIZE);
            bufferedOutputStream.write(text.getBytes(charset));
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
