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
 * <p>
 * <h3>A Practical Example:</h3>
 * As an example, consider the following WTKX file, named <tt>example.wtkx</tt>:
 * <p>
 * <pre>
 * &lt;Border xmlns:wtkx="http://incubator.apache.org/pivot/wtkx/1.1"
 *         xmlns="pivot.wtk"&gt;
 *     &lt;content&gt;
 *         &lt;FlowPane orientation="vertical"&gt;
 *             &lt;Slider wtkx:id="redSlider" bounds="{minimum:0, maximum:255}" value="0"/&gt;
 *             &lt;Slider wtkx:id="greenSlider" bounds="{minimum:0, maximum:255}" value="0"/&gt;
 *             &lt;Slider wtkx:id="blueSlider" bounds="{minimum:0, maximum:255}" value="0"/&gt;
 *             &lt;Border wtkx:id="colorBorder" preferredWidth="120" preferredHeight="30"/&gt;
 *         &lt;/FlowPane&gt;
 *     &lt;/content&gt;
 * &lt;/Border&gt;
 * </pre>
 * You could leverage WTKX binding by subclassing <tt>Bindable</tt>, like so:
 * <p>
 * <pre>
 * public class Example extends Bindable {
 *     &#64;Load(resourceName="example.wtkx") private Border border;
 *
 *     &#64;Bind(fieldName="border") private Slider redSlider;
 *     &#64;Bind(fieldName="border") private Slider greenSlider;
 *     &#64;Bind(fieldName="border") private Slider blueSlider;
 *     &#64;Bind(fieldName="border", id="colorBorder") private Border colorSample;
 *
 *     public Example() {
 *         // Your annotated variables will be null until you call bind()
 *         bind();
 *     }
 * }
 * </pre>
 * <h3>Binding implementations:</h3>
 * WTKX binding can be performed using one of two methods. It is important
 * for callers to understand these methods so that they may decide which is
 * appropriate for them. The methods are as follows:
 * <ol>
 *   <li>
 *     <b>Runtime / Reflection</b>
 *     <br/><br/>
 *     The default binding process loads the WTKX at runtime and uses
 *     reflection to bind the values to the variables. This method requires
 *     security privileges; it is suitable for callers that are deploying to a
 *     trusted application, such as a signed applet or a desktop application.
 *     <br/><br/>
 *   </li>
 *   <li>
 *     <b>Runtime / No Reflection</b>
 *     <br/><br/>
 *     For those callers that are deploying to an unsigned applet, a
 *     compile-time annotation processor, {@link BindProcessor}, is available
 *     and will cause the binding process to load the WTKX at runtime and bind
 *     the values to the variables without the use of reflection. This
 *     method requires the use of a Sun <tt>javac</tt> compiler; it is suitable
 *     for callers that are willing to adopt a dependency on Sun's compiler in
 *     order to function in an untrusted environment.
 *     <br/><br/>
 *     Note that it is possible to use the default binding method during
 *     development and deploy using the annotation processor.
 *     <br/><br/>
 *   </li>
 * </ol>
 *
 * @author gbrown
 * @author tvolkert
 * @see BindProcessor
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
         * should be of the form defined by {@link Class#getResource(String)}
         * and is relative to the <tt>Bindable</tt> subclass.
         */
        public String resourceName();

        /**
         * The base name of the resources to associate with the WTKX load.
         * The base name should be of the form defined by the {@link Resources}
         * class. If unspecified, the WTKX load will be assumed to not use
         * resource strings.
         */
        public String resources() default "\0";

        /**
         * The locale with which to load WTKX resources. This should be a
         * lowercase two-letter ISO-639 code. If unspecified, the user's
         * default locale will be used.
         */
        public String locale() default "\0";
    }

    /**
     * Annotation that causes a loaded WTKX element to be bound to the
     * annotated field. This annotation necessitates the prior use of a
     * <tt>@Load</tt> annotation and references the loaded field via the
     * <tt>fieldName</tt> attribute.
     *
     * @author gbrown
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    protected static @interface Bind {
        /**
         * The name of the field that was loaded via the <tt>@Load</tt>
         * annotation.
         *
         * @see
         * Load
         */
        public String fieldName();

        /**
         * The ID of the WTKX variable that references the element to bind.
         * It should be a valid <tt>wtkx:id</tt> from the loaded
         * WTKX resource. If unspecified, the name of the annotated field
         * will be used.
         *
         * @see
         * WTKXSerializer#getObjectByID(String)
         */
        public String id() default "\0";
    }

    /**
     * Interface denoting a WTKX object hierarchy to which we can bind values.
     *
     * @author tvolkert
     */
    public static interface ObjectHierarchy {
        /**
         * Gets the root of the object hierarchy.
         */
        <T> T getRootObject();

        /**
         * Gets a named object by its ID.
         */
        <T> T getObjectByID(String id);
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
     * permission is not granted to un-trusted applets, meaning that this
     * method of binding is not available to un-signed applets.
     * <p>
     * To mitigate this problem, a compile-time annotation processor,
     * {@link BindProcessor}, is available and will cause this method to work
     * without requiring the use of reflection. This in turn will eliminate any
     * security issues with the binding process.
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
            // Maps field name to the serializer that loaded the field; public
            // and protected serializers are retained for sub-types, but private
            // serializers are removed at the end of the block
            HashMap<String, WTKXSerializer> wtkxSerializers = new HashMap<String, WTKXSerializer>();

            // Walk fields and resolve annotations
            for (int i = typeHierarchy.getLength() - 1; i >= 0; i--) {
                type = typeHierarchy.get(i);
                Field[] fields = type.getDeclaredFields();

                ArrayList<String> privateFieldNames = new ArrayList<String>();

                // Process load annotations
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

                        String loadResourceName = loadAnnotation.resourceName();
                        URL location = type.getResource(loadResourceName);
                        if (location == null) {
                            throw new BindException(loadResourceName + " not found.");
                        }

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

                }

                // Process bind annotations
                for (int j = 0, n = fields.length; j < n; j++) {
                    Field field = fields[j];
                    String fieldName = field.getName();
                    int fieldModifiers = field.getModifiers();

                    Bind bindAnnotation = field.getAnnotation(Bind.class);
                    if (bindAnnotation != null) {
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

                        // Bind to the value loaded by the field's serializer
                        String loadFieldName = bindAnnotation.fieldName();
                        WTKXSerializer wtkxSerializer = wtkxSerializers.get(loadFieldName);
                        if (wtkxSerializer == null) {
                            throw new BindException("Field \"" + loadFieldName + "\" has not been loaded.");
                        }

                        String id = bindAnnotation.id();
                        if (id.equals("\0")) {
                            id = field.getName();
                        }

                        Object value = wtkxSerializer.getObjectByID(id);
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
            HashMap<String, ObjectHierarchy> objectHierarchies = new HashMap<String, ObjectHierarchy>();
            bind(objectHierarchies);
        }
    }

    /**
     * This is an internal method that callers should neither invoke nor
     * override. It exists to support {@link BindProcessor}. Dealing directly
     * with this method in any way may yield unpredictable behavior.
     */
    protected void bind(Dictionary<String, ObjectHierarchy> objectHierarchies) {
        // No-op
    }
}
