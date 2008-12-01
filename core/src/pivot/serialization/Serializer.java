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
package pivot.serialization;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import pivot.util.concurrent.Task;
import pivot.util.concurrent.TaskExecutionException;

/**
 * Defines an interface for writing objects to and reading objects from a data
 * stream.
 *
 * @author gbrown
 */
public interface Serializer {
    /**
     * Task that reads an object from an input stream.
     *
     * @author gbrown
     */
    public class ReadTask extends Task<Object> {
        private Serializer serializer;
        private InputStream inputStream;

        public ReadTask(Serializer serializer, InputStream inputStream) {
            if (serializer == null) {
                throw new IllegalArgumentException("serializer is null.");
            }

            if (inputStream == null) {
                throw new IllegalArgumentException("inputStream is null.");
            }

            this.serializer = serializer;
            this.inputStream = inputStream;
        }

        public Serializer getSerializer() {
            return serializer;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        @Override
        public Object execute() throws TaskExecutionException {
            // TODO Auto-generated method stub
            return null;
        }
    }

    /**
     * Task that writes an object to an output stream.
     *
     * @author gbrown
     */
    public class WriteTask extends Task<Void> {
        private Object object;
        private Serializer serializer;
        private OutputStream outputStream;

        public WriteTask(Object object, Serializer serializer, OutputStream outputStream) {
            if (object == null) {
                throw new IllegalArgumentException("object is null.");
            }

            if (serializer == null) {
                throw new IllegalArgumentException("serializer is null.");
            }

            if (outputStream == null) {
                throw new IllegalArgumentException("outputStream is null.");
            }

            this.object = object;
            this.serializer = serializer;
            this.outputStream = outputStream;
        }

        public Object getObject() {
            return object;
        }

        public Serializer getSerializer() {
            return serializer;
        }

        public OutputStream getOutputStream() {
            return outputStream;
        }

        @Override
        public Void execute() throws TaskExecutionException {
            // TODO Auto-generated method stub
            return null;
        }

    }

    /**
     * Reads an object from an input stream.
     *
     * @param inputStream
     * The data stream from which the object will be read.
     *
     * @return
     * The deserialized object.
     */
    public Object readObject(InputStream inputStream) throws IOException, SerializationException;

    /**
     * Writes an object to an output stream.
     *
     * @param object
     * The object to serialize.
     *
     * @param outputStream
     * The data stream to which the object will be written.
     */
    public void writeObject(Object object, OutputStream outputStream) throws IOException, SerializationException;

    /**
     * Returns the MIME type of the data read and written by this serializer.
     *
     * @param object
     * If provided, allows the serializer to attach parameters to the returned
     * MIME type containing more detailed information about the data. If
     * <tt>null</tt>, the base MIME type is returned.
     */
    public String getMIMEType(Object object);
}
