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
import java.io.OutputStream;
import java.util.Properties;

import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.adapter.MapAdapter;


/**
 * Implementation of the {@link Serializer} interface that reads data from
 * and writes data to the Java properties file format.
 *
 */
public class PropertiesSerializer implements Serializer<Map<?, ?>> {
    public static final String PROPERTIES_EXTENSION = "properties";
    public static final String MIME_TYPE = "text/plain";

    /**
     * Reads data from a properties stream.
     *
     * @param inputStream
     * The input stream from which data will be read.
     *
     * @return
     * An instance of {@link Map} containing the data read from the properties
     * file. Both keys and values are strings.
     */
    @Override
    public Map<?, ?> readObject(InputStream inputStream) throws IOException,
        SerializationException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is null.");
        }

        Properties properties = new Properties();
        properties.load(inputStream);

        return new MapAdapter<Object, Object>(properties);
    }

    /**
     * Writes data to a properties stream.
     *
     * @param object
     * An instance of {@link Map} containing the data to be written to the
     * properties file. Keys must be strings, and values will be converted to
     * strings.
     *
     * @param outputStream
     * The output stream to which data will be written.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void writeObject(Map<?, ?> object, OutputStream outputStream) throws IOException,
        SerializationException {
        if (object == null) {
            throw new IllegalArgumentException("object is null.");
        }

        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream is null.");
        }

        Map<Object, Object> map = (Map<Object, Object>)object;

        Properties properties = new Properties();

        for (Object key : map) {
            Object value = map.get(key);
            if (key != null && value != null) {
                properties.put(key, value.toString());
            }
        }

        properties.store(outputStream, null);
    }

    @Override
    public String getMIMEType(Map<?, ?> object) {
        return MIME_TYPE;
    }
}
