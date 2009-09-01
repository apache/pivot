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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Implementation of the {@link Serializer} interface that uses Java's
 * internal serialization mechanism to read and write values. All values in the
 * object hierarchy are required to implement {@link java.io.Serializable}.
 */
public class BinarySerializer implements Serializer<Object> {
    public static final String MIME_TYPE = "application/x-java-serialized-object";
    public static final String CLASS_PARAMETER = "class";

    /**
     * Reads a graph of serialized objects from an input stream.
     */
    @Override
    public Object readObject(InputStream inputStream) throws IOException,
        SerializationException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is null.");
        }

        Object object = null;

        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            object = objectInputStream.readObject();
        } catch(ClassNotFoundException exception) {
            throw new SerializationException(exception);
        }

        return object;
    }

    /**
     * Writes a graph of serializable objects to an output stream.
     */
    @Override
    public void writeObject(Object object, OutputStream outputStream)
        throws IOException, SerializationException {
        if (object == null) {
            throw new IllegalArgumentException("object is null.");
        }

        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream is null.");
        }

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(object);
    }

    @Override
    public String getMIMEType(Object object) {
        String mimeType = MIME_TYPE;
        if (object != null) {
            mimeType += "; " + CLASS_PARAMETER + "=" + object.getClass().getName();
        }

        return mimeType;
    }
}
