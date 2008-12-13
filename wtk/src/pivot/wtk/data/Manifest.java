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
package pivot.wtk.data;

import java.io.IOException;
import java.io.InputStream;

import pivot.serialization.Serializer;
import pivot.util.MIMEType;
import pivot.util.concurrent.Task;
import pivot.util.concurrent.TaskExecutionException;

/**
 * Interface representing the contents of a data source such as the clipboard
 * or a drag/drop operation.
 * <p>
 * NOTE We provide a {@link #getIndex(String)} method so callers can perform
 * partial match lookups on MIME types. For example, a component that is capable
 * of pasting text content from the clipboard may want to look for a match on
 * "text/plain", regardless of the encoding used. Without partial matching,
 * callers would need to enumerate each entry individually and manually perform
 * the match comparisons.
 *
 * @author gbrown
 */
public abstract class Manifest {
    /**
     * Task that reads an object from a manifest input stream.
     *
     * @author gbrown
     */
    public static class ReadTask extends Task<Object> {
        private Manifest manifest;
        private int index;
        private Serializer serializer;

        public ReadTask(Manifest manifest, int index, Serializer serializer) {
            this.manifest = manifest;
            this.index = index;
            this.serializer = serializer;
        }

        @Override
        public Object execute() throws TaskExecutionException {
            Object object = null;

            try {
                InputStream inputStream = null;
                try {
                    inputStream = manifest.getInputStream(index);
                    object = serializer.readObject(inputStream);
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } catch(Exception exception) {
                throw new TaskExecutionException(exception);
            }

            return object;
        }
    }

    /**
     * Returns the MIME type of the content at the given index.
     *
     * @param index
     */
    public abstract String getMIMEType(int index);

    /**
     * Returns the input stream at the given index.
     *
     * @param index
     */
    public abstract InputStream getInputStream(int index) throws IOException;

    /**
     * Returns the index of the first matching MIME type.
     *
     * @param mimeType
     *
     * @return
     * The index of the first matching MIME type, or <tt>-1</tt> if no match
     * is found.
     */
    public int getIndex(String mimeType) {
        if (mimeType == null) {
            throw new IllegalArgumentException("mimeType is null.");
        }

        return getIndex(MIMEType.decode(mimeType));
    }

    /**
     * Returns the index of the first matching MIME type.
     *
     * @param mimeType
     *
     * @return
     * The index of the first matching MIME type, or <tt>-1</tt> if no match
     * is found.
     */
    public int getIndex(MIMEType mimeType) {
        if (mimeType == null) {
            throw new IllegalArgumentException("mimeType is null.");
        }

        int index = -1;

        for (int i = 0, n = getLength(); i < n; i++) {
            MIMEType streamMIMEType = MIMEType.decode(getMIMEType(i));

            if (mimeType.getBaseType().equals(streamMIMEType.getBaseType())) {
                index = i;

                for (String parameter : mimeType) {
                    if (!mimeType.containsKey(parameter)) {
                        index = -1;
                        break;
                    }

                    String value = mimeType.get(parameter);
                    if (!value.equals(mimeType.get(parameter))) {
                        index = -1;
                        break;
                    }
                }
            }

            if (index != -1) {
                break;
            }
        }

        return index;
    }

    /**
     * Returns the number of entries in the manifest.
     */
    public abstract int getLength();

    /**
     * Releases any resources currently being used by the manifest.
     */
    public abstract void dispose();
}
