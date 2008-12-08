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
 * Abstract base class for "transports". A transport is a vehicle for exporting
 * an object to a data stream.
 *
 * @author gbrown
 */
public abstract class Transport {
    private Object object;
    private Serializer serializer;

    public Transport(Object object, Serializer serializer) {
        if (object == null) {
            throw new IllegalArgumentException("object is null.");
        }

        if (serializer == null) {
            throw new IllegalArgumentException("serializer is null.");
        }
    }

    public Object getObject() {
        return object;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    /**
     * Returns an input stream containing the object as written to an output
     * stream using the serializer.
     */
    public abstract InputStream getInputStream();
}
