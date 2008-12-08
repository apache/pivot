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
 * Transport backed by a "pipe". The object is written to a piped output stream
 * by a separate thread created when the input stream is requested. The returned
 * input stream reads data from the pipe.
 * <p>
 * TODO Should the piped output stream be created on demand, and then re-used if
 * requested again?
 *
 * @author gbrown
 */
public class PipedTransport extends Transport {
    public PipedTransport(Object object, Serializer serializer) {
        super(object, serializer);
    }

    @Override
    public InputStream getInputStream() {
        // TODO Auto-generated method stub
        return null;
    }

}
