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
package pivot.serialization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * Implementation of the {@link Serializer} interface that reads and writes
 * E4X data.
 *
 * @author gbrown
 */
public class E4XSerializer implements Serializer<Object> {
    public static final String MIME_TYPE = "text/xml; type=e4x";
    public static final int BUFFER_SIZE = 2048;

    public Object readObject(InputStream inputStream)
        throws IOException, SerializationException {
        Reader reader = new BufferedReader(new InputStreamReader(inputStream), BUFFER_SIZE);
        Object object = readObject(reader);

        return object;
    }

    public Object readObject(Reader reader)
        throws IOException, SerializationException {
        // TODO
        return null;
    }

    public void writeObject(Object object, OutputStream outputStream)
        throws IOException, SerializationException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream), BUFFER_SIZE);
        writeObject(object, writer);
    }

    public void writeObject(Object object, Writer writer)
        throws IOException, SerializationException {
        // TODO
    }

    public String getMIMEType(Object object) {
        // TODO Auto-generated method stub
        return null;
    }
}
