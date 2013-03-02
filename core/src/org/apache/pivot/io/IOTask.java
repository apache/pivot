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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;

import org.apache.pivot.util.concurrent.AbortException;
import org.apache.pivot.util.concurrent.Task;

/**
 * Abstract base class for input/output tasks.
 */
public abstract class IOTask<V> extends Task<V> {
    /**
     * Input stream that monitors the bytes that are read from it by
     * incrementing the <tt>bytesReceived</tt> member variable.
     */
    protected class MonitoredInputStream extends InputStream {
        private InputStream inputStream;

        long mark = 0;

        public MonitoredInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            if (abort) {
                throw new AbortException();
            }

            int result = inputStream.read();

            if (result != -1) {
                bytesReceived++;
            }

            return result;
        }

        @Override
        public int read(byte b[]) throws IOException {
            if (abort) {
                throw new AbortException();
            }

            int count = inputStream.read(b);

            if (count != -1) {
                bytesReceived += count;
            }

            return count;
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException {
            if (abort) {
                throw new AbortException();
            }

            int count = inputStream.read(b, off, len);

            if (count != -1) {
                bytesReceived += count;
            }

            return count;
        }

        @Override
        public long skip(long n) throws IOException {
            if (abort) {
                throw new AbortException();
            }

            long count = inputStream.skip(n);
            bytesReceived += count;
            return count;
        }

        @Override
        public int available() throws IOException {
            if (abort) {
                throw new AbortException();
            }

            return inputStream.available();
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
        }

        @Override
        public synchronized void mark(int readLimit) {
            if (abort) {
                throw new AbortException();
            }

            inputStream.mark(readLimit);
            mark = bytesReceived;
        }

        @Override
        public synchronized void reset() throws IOException {
            if (abort) {
                throw new AbortException();
            }

            inputStream.reset();
            bytesReceived = mark;
        }

        @Override
        public boolean markSupported() {
            return inputStream.markSupported();
        }
    }

    /**
     * Output stream that monitors the bytes that are written to it by
     * incrementing the <tt>bytesSent</tt> member variable.
     */
    protected class MonitoredOutputStream extends OutputStream {
        private OutputStream outputStream;

        public MonitoredOutputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void close() throws IOException {
            outputStream.close();
        }

        @Override
        public void flush() throws IOException {
            if (abort) {
                throw new AbortException();
            }

            outputStream.flush();
        }

        @Override
        public void write(byte[] b) throws IOException {
            if (abort) {
                throw new AbortException();
            }

            outputStream.write(b);
            bytesSent += b.length;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (abort) {
                throw new AbortException();
            }

            outputStream.write(b, off, len);
            bytesSent += len;
        }

        @Override
        public void write(int b) throws IOException {
            if (abort) {
                throw new AbortException();
            }

            outputStream.write(b);
            bytesSent++;
        }
    }

    // TODO: use AtomicInteger instead of volatile ...
    protected volatile long bytesSent = 0;
    protected volatile long bytesReceived = 0;

    public IOTask() {
        super();
    }

    public IOTask(ExecutorService executorService) {
        super(executorService);
    }
}
