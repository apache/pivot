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
package pivot.wtkx;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.net.URL;

import pivot.collections.HashMap;
import pivot.serialization.SerializationException;

/**
 * Base class for objects that wish to leverage WTKX binding annotations.
 *
 * @author tvolkert
 */
public abstract class Bindable {
    /**
     * WTKX binding annotation.
     *
     * @author gbrown
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    protected static @interface Load {
        public String value();
    }

    /**
     * WTKX binding annotation.
     *
     * @author gbrown
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    protected static @interface Bind {
        public String resource();
        public String id() default "\0";
    }

    /**
     * Applies WTKX binding annotations to this bindable object.
     */
    protected void bind() throws IOException, BindException {
        // Maps resource field name to the serializer that loaded the resource
        HashMap<String, WTKXSerializer> wtkxSerializers = new HashMap<String, WTKXSerializer>();

        // Walk field lists and resolve WTKX annotations
        Class<?> type = getClass();
        Field[] fields = type.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            Load loadAnnotation = field.getAnnotation(Load.class);

            if (loadAnnotation != null) {
                // Create a serializer for the resource
                String fieldName = field.getName();
                assert(!wtkxSerializers.containsKey(fieldName));

                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                wtkxSerializers.put(fieldName, wtkxSerializer);

                // Load the resource
                URL location = type.getResource(loadAnnotation.value());
                Object resource;
                try {
                    resource = wtkxSerializer.readObject(location);
                } catch (SerializationException exception) {
                    throw new BindException(exception);
                }

                // Set the resource into the field
                if (!field.isAccessible()) {
                    try {
                        field.setAccessible(true);
                    } catch (Exception ex) {
                        // No-op; the callers might have used public fields, in
                        // which case we don't need to make them accessible
                    }
                }

                try {
                    field.set(this, resource);
                } catch (IllegalAccessException exception) {
                    throw new BindException(exception);
                }
            }

            Bind bindAnnotation = field.getAnnotation(Bind.class);
            if (bindAnnotation != null) {
                if (loadAnnotation != null) {
                    throw new BindException("Cannot combine " + Load.class.getName()
                        + " and " + Bind.class.getName() + " annotations.");
                }

                // Bind to the value loaded by the field's serializer
                String fieldName = bindAnnotation.resource();
                WTKXSerializer wtkxSerializer = wtkxSerializers.get(fieldName);
                if (wtkxSerializer == null) {
                    throw new BindException("\"" + fieldName + "\" is not a valid resource name.");
                }

                String id = bindAnnotation.id();
                if ("\0".equals(id)) {
                    id = field.getName();
                }

                Object value = wtkxSerializer.getObjectByName(id);
                if (value == null) {
                    throw new BindException("\"" + id + "\" does not exist.");
                }

                // Set the value into the field
                if (!field.isAccessible()) {
                    try {
                        field.setAccessible(true);
                    } catch (Exception ex) {
                        // No-op; the callers might have used public fields, in
                        // which case we don't need to make them accessible
                    }
                }

                try {
                    field.set(this, value);
                } catch (IllegalAccessException exception) {
                    throw new BindException(exception);
                }
            }
        }
    }
}
