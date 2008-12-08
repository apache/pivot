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
 * Transport backed by a byte array. The object is written to an in-memory
 * output stream immediately by the constructor; an input stream on the byte
 * array is created on-demand.
 * <p>
 * TODO Should the byte array be created on demand?
 *
 * @author gbrown
 */
public class ByteArrayTransport extends Transport {
    public ByteArrayTransport(Object object, Serializer serializer) {
        super(object, serializer);
    }

    @Override
    public InputStream getInputStream() {
        // TODO Auto-generated method stub
        return null;
    }
}
