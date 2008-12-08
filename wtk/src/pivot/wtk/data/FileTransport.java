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

import java.io.InputStream;

import pivot.serialization.Serializer;

/**
 * Transport backed by the file system. A thread is started by the constructor
 * to write the object to a file; an input stream on the file is created
 * on-demand.
 * <p>
 * The created file is a temporary file that will be cleaned up when the
 * transport is finalized or the JVM exits.
 * <p>
 * TODO Should the file be created on demand? If so, what differentiates a
 * file transport from a piped transport? The ability to convey larger data
 * sets?
 *
 * @author gbrown
 */
public class FileTransport extends Transport {
    public FileTransport(Object object, Serializer serializer) {
        super(object, serializer);
    }

    @Override
    public InputStream getInputStream() {
        // TODO Auto-generated method stub
        return null;
    }

}
