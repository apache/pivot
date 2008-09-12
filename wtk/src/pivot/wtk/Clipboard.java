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
package pivot.wtk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.serialization.SerializationException;
import pivot.serialization.Serializer;
import pivot.serialization.JSONSerializer;

/**
 * Singleton class providing a means of sharing data between components and
 * applications.
 *
 * <p>TODO Copy data to system clipboard when allowed (and the data format is
 * supported).</p>
 *
 * <p>TODO Use a list of Class:Serializer mapping, so we can prioritize? Using
 * this approach, we could walk the list and use the first matching serializer
 * (e.g. to use BinarySerializer for classes that implement Serializable).
 * Alternatively, this would let us serialize to all matching types (though
 * this could be a peformance issue).</p>
 *
 * <p>Another alternative is to require the caller to specify the serializer(s)
 * at put() time.</p>
 */
public class Clipboard {
    /**
     * Serializer dictionary implementation.
     *
     * @author gbrown
     */
    public static final class SerializerDictionary
        implements Dictionary<Class<?>, Class<? extends Serializer>> {
        public Class<? extends Serializer> get(Class<?> type) {
            // TODO Walk class hierarchy to find a match

            return serializers.get(type);
        }

        public Class<? extends Serializer> put(Class<?> type,
            Class<? extends Serializer> serializerType) {
            if (serializerType == null) {
                throw new IllegalArgumentException("serializerType is null.");
            }

            return serializers.put(type, serializerType);
        }

        public Class<? extends Serializer> remove(Class<?> type) {
            return serializers.remove(type);
        }

        public boolean containsKey(Class<?> type) {
            return serializers.containsKey(type);
        }

        public boolean isEmpty() {
            return serializers.isEmpty();
        }
    }

    private Class<?> type = null;
    private byte[] buffer = null;

    private static Clipboard instance = new Clipboard();

    private static HashMap<Class<?>, Class<? extends Serializer>> serializers = new HashMap<Class<?>, Class<? extends Serializer>>();
    private static SerializerDictionary serializerDictionary = new SerializerDictionary();

    public void put(Object contents) {
        if (contents == null) {
            throw new IllegalArgumentException("contents is null");
        }

        Class<?> type = contents.getClass();

        if (!serializers.containsKey(type)) {
            throw new IllegalArgumentException("No serializer found for "
                + type.getName());
        }

        Class<? extends Serializer> serializerType = serializers.get(type);

        Serializer serializer = null;
        try {
            serializer = serializerType.newInstance();
        } catch(IllegalAccessException exception) {
            throw new RuntimeException(exception);
        } catch(InstantiationException exception) {
            throw new RuntimeException(exception);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            serializer.writeObject(contents, byteArrayOutputStream);
        } catch(IOException exception) {
            throw new RuntimeException(exception);
        } catch(SerializationException exception) {
            throw new RuntimeException(exception);
        }

        this.type = type;
        buffer = byteArrayOutputStream.toByteArray();
    }

    public Object get() {
        Object contents = null;

        if (buffer != null) {
            if (!serializers.containsKey(type)) {
                throw new IllegalStateException("No serializer found for "
                    + type.getName());
            }

            Class<? extends Serializer> serializerType = serializers.get(type);

            Serializer serializer = null;
            try {
                serializer = serializerType.newInstance();
            } catch(IllegalAccessException exception) {
                throw new RuntimeException(exception);
            } catch(InstantiationException exception) {
                throw new RuntimeException(exception);
            }

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);

            try {
                contents = serializer.readObject(byteArrayInputStream);
            } catch(IOException exception) {
                throw new RuntimeException(exception);
            } catch(SerializationException exception) {
                throw new RuntimeException(exception);
            }
        }

        return contents;
    }

    public void clear() {
        type = null;
        buffer = null;
    }

    public static Clipboard getInstance() {
        return instance;
    }

    public static SerializerDictionary getSerializers() {
        return serializerDictionary;
    }

    static {
        serializers.put(String.class, JSONSerializer.class);
        serializers.put(Number.class, JSONSerializer.class);
        serializers.put(Boolean.class, JSONSerializer.class);
    }
}
