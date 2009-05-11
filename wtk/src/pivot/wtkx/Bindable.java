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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.serialization.SerializationException;
import pivot.util.Resources;

/**
 * Base class for objects that wish to leverage WTKX binding. By extending this
 * class, subclasses may use the {@link Load @Load} and {@link Bind @Bind}
 * annotations to automate WTKX loading and binding within their class.
 *
 * @see
 * BindProcessor
 *
 * @author gbrown
 * @author tvolkert
 */
public abstract class Bindable {
    /**
     * Annotation that causes the annotated field to be loaded via WTKX and
     * bound to the field. This annotation is the entry point into WTKX binding
     * and a prerequisite to using the <tt>@Bind</tt> annotation.
     *
     * @see
     * WTKXSerializer#readObject(URL)
     *
     * @author gbrown
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    protected static @interface Load {
        /**
         * A path name that identifies the WTKX resource to be loaded. The root
         * WTKX element will be stored in the annotated field. The path name
         * should be of the form defined by {@link Class#getResource(String)}.
         */
        public String name();

        /**
         * The base name of the resources to associate with the WTKX load.
         * The base name should be of the form defined by the {@link Resources}
         * class. If unspecified, the WTKX load will be assumed to not use
         * resource strings.
         */
        public String resources() default "\0";

        /**
         * The locale with which to load the WTKX. This should be a lowercase
         * two-letter ISO-639 code. If unspecified, the user's default locale
         * will be used.
         */
        public String locale() default "\0";

        /**
         * Indicates whether the loaded WTKX should be compiled into the class
         * or if it should be loaded at runtime via the <tt>WTKXSerializer</tt>
         * class. If unspecified, the WTKX loading will be done at runtime.
         * <p>
         * <b>Note</b>: This option only has meaning when the annotations are
         * processed during compilation using {@link BindProcessor}. Callers
         * who choose to skip the annotation processing will always be using a
         * runtime implementation of WTKX loading, and in such cases, the
         * <tt>compile</tt> flag will be ignored.
         * <p>
         * Also note that if the loaded WTKX is compiled into the class, the
         * WTKX resource may not be needed at runtime; in such cases, the
         * caller may wish to exclude it from their JAR file.
         */
        public boolean compile() default false;
    }

    /**
     * Annotation that causes a loaded WTKX element to be bound to the
     * annotated field. This annotation necessitates the prior use of a
     * <tt>@Load</tt> annotation and references the loaded field via the
     * <tt>property</tt> attribute.
     *
     * @author gbrown
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    protected static @interface Bind {
        /**
         * The name of the property that was loaded via the <tt>@Load</tt>
         * annotation.
         *
         * @see
         * Load
         */
        public String property();

        /**
         * The name of the WTKX variable that references the element to bind.
         * It should be a valid <tt>wtkx:id</tt> from the loaded
         * WTKX resource. If unspecified, the name of the annotated field will
         * be used.
         *
         * @see
         * WTKXSerializer#getObjectByName(String)
         */
        public String name() default "\0";
    }

    /**
     * Creates a new <tt>Bindable</tt> object.
     */
    protected Bindable() {
    }

    /**
     * Applies WTKX binding annotations to this object. Subclasses should call
     * this before they access their bound variables. Calling this more than
     * once is legal; it will cause the annotated fields to be re-loaded and
     * re-bound.
     * <p>
     * Note that by default, this method uses reflection to perform the
     * binding. This may necessitate a call to <tt>setAccessible(true)</tt> on
     * the bound field. If there is a security manager, its checkPermission
     * method will correspondingly be called with a
     * <tt>ReflectPermission("suppressAccessChecks")</tt> permission. This
     * permission is not typically granted to un-trusted applets, meaning that
     * this method of binding is not available to un-signed applets. To
     * mitigate this, a compile-time annotation processor,
     * {@link BindProcessor}, is available and will cause this method to use
     * compiled code to perform the binding as opposed to reflection. This in
     * turn should eliminate any security issues with the binding process.
     *
     * @throws BindException
     * If an error occurs during binding
     */
    protected final void bind() throws BindException {
        ArrayList<Class<?>> typeHierarchy = new ArrayList<Class<?>>();
        Class<?> type = getClass();
        while (type != Bindable.class) {
            typeHierarchy.add(type);
            type = type.getSuperclass();
        }

        Method bindOverload = null;
        for (int i = 0, n = typeHierarchy.getLength(); i < n; i++) {
            type = typeHierarchy.get(i);
            try {
                bindOverload = type.getDeclaredMethod("bind", new Class<?>[] {Dictionary.class});
                break;
            } catch(NoSuchMethodException exception) {
                // No-op
            }
        }

        if (bindOverload == null) {
            // Maps field name to the serializer that loaded the property; public
            // and protected serializers are retained for sub-types, but private
            // serializers are removed at the end of the block
            HashMap<String, WTKXSerializer> wtkxSerializers = new HashMap<String, WTKXSerializer>();

            // Walk fields and resolve annotations
            for (int i = typeHierarchy.getLength() - 1; i >= 0; i--) {
                type = typeHierarchy.get(i);
                Field[] fields = type.getDeclaredFields();

                ArrayList<String> privateFieldNames = new ArrayList<String>();

                for (int j = 0, n = fields.length; j < n; j++) {
                    Field field = fields[j];
                    String fieldName = field.getName();
                    int fieldModifiers = field.getModifiers();

                    Load loadAnnotation = field.getAnnotation(Load.class);
                    if (loadAnnotation != null) {
                        // Ensure that we can write to the field
                        if ((fieldModifiers & Modifier.FINAL) > 0) {
                            throw new BindException(fieldName + " is final.");
                        }

                        if ((fieldModifiers & Modifier.PUBLIC) == 0) {
                            try {
                                field.setAccessible(true);
                            } catch(SecurityException exception) {
                                throw new BindException(fieldName + " is not accessible.");
                            }
                        }

                        assert(!wtkxSerializers.containsKey(fieldName));

                        if ((fieldModifiers & Modifier.PRIVATE) > 0) {
                            privateFieldNames.add(fieldName);
                        }

                        // Get the name of the resource file to use
                        Resources resources = null;
                        boolean defaultResources = false;

                        String baseName = loadAnnotation.resources();
                        if (baseName.equals("\0")) {
                            baseName = type.getName();
                            defaultResources = true;
                        }

                        // Get the resource locale
                        Locale locale;
                        String language = loadAnnotation.locale();
                        if (language.equals("\0")) {
                            locale = Locale.getDefault();
                        } else {
                            locale = new Locale(language);
                        }

                        // Attmpt to load the resources
                        try {
                            resources = new Resources(baseName, locale, "UTF8");
                        } catch(IOException exception) {
                            throw new BindException(exception);
                        } catch(SerializationException exception) {
                            throw new BindException(exception);
                        } catch(MissingResourceException exception) {
                            if (!defaultResources) {
                                throw new BindException(baseName + " not found.");
                            }
                        }

                        // Deserialize the value
                        WTKXSerializer wtkxSerializer = new WTKXSerializer(resources);
                        wtkxSerializers.put(fieldName, wtkxSerializer);

                        URL location = type.getResource(loadAnnotation.name());
                        Object resource;
                        try {
                            resource = wtkxSerializer.readObject(location);
                        } catch(IOException exception) {
                            throw new BindException(exception);
                        } catch (SerializationException exception) {
                            throw new BindException(exception);
                        }

                        // Set the deserialized value into the field
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

                        // Ensure that we can write to the field
                        if ((fieldModifiers & Modifier.FINAL) > 0) {
                            throw new BindException(fieldName + " is final.");
                        }

                        if ((fieldModifiers & Modifier.PUBLIC) == 0) {
                            try {
                                field.setAccessible(true);
                            } catch(SecurityException exception) {
                                throw new BindException(fieldName + " is not accessible.");
                            }
                        }

                        // Bind to the value loaded by the property's serializer
                        String property = bindAnnotation.property();
                        WTKXSerializer wtkxSerializer = wtkxSerializers.get(property);
                        if (wtkxSerializer == null) {
                            throw new BindException("Property \"" + property + "\" has not been loaded.");
                        }

                        String id = bindAnnotation.name();
                        if (id.equals("\0")) {
                            id = field.getName();
                        }

                        Object value = wtkxSerializer.getObjectByName(id);
                        if (value == null) {
                            throw new BindException("\"" + id + "\" does not exist.");
                        }

                        // Set the value into the field
                        try {
                            field.set(this, value);
                        } catch (IllegalAccessException exception) {
                            throw new BindException(exception);
                        }
                    }
                }

                // Remove the private field serializers
                for (String privateFieldName : privateFieldNames) {
                    wtkxSerializers.remove(privateFieldName);
                }
            }
        } else {
            // Invoke the bind overload
            HashMap<String, Dictionary<String, Object>> namedObjectDictionaries =
                new HashMap<String, Dictionary<String, Object>>();
            bind(namedObjectDictionaries);
        }
    }

    /**
     * This is an internal method that callers should neither invoke nor
     * override. It exists to support {@link BindProcessor}. Dealing directly
     * with this method in any way may yield unpredictable behavior.
     */
    protected void bind(Dictionary<String, Dictionary<String, Object>> namedObjectDictionaries) {
        // No-op
    }
}
