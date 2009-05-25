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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.Tool;
import javax.tools.ToolProvider;

/**
 * Provides the facility by which WTKX resources can be compiled into Java
 * classes. This enables callers to leverage compile-time checking of their
 * WTKX files as well as a performance improvement over runtime WTKX parsing
 * (via {@link WTKXSerializer}).
 *
 * @author tvolkert
 */
public class Compiler implements Tool {
    /**
     * Creates a new <tt>Compiler</tt>.
     */
    public Compiler() {
    }

    /**
     * Compiles a WTKX resource into a Java class file.
     *
     * @param referenceClass
     * The class relative to which the WTKX resource can be found.
     *
     * @param resourceName
     * A path name that identifies the WTKX resource. The path name should be
     * of the form defined by {@link Class#getResource(String)} and is relative
     * to the base package. Note that this is the same form as is defined in
     * {@link Bindable.Load#resourceName()}.
     *
     * @param options
     * Compiler options, or <tt>null</tt> for no options.
     *
     * @return
     * <tt>true</tt> if the resource compiled without errors; <tt>false</tt>
     * otherwise.
     */
    public boolean compile(Class<?> referenceClass, String resourceName, Iterable<String> options)
        throws IOException {
        boolean success = false;

        Translator translator = new Translator();
        JavaFileObject javaFileObject = translator.translate(referenceClass, resourceName);
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            JavaCompiler.CompilationTask task = compiler.getTask(null, null, null,
                options, null, Collections.singletonList(javaFileObject));

            success = task.call();
        } finally {
            javaFileObject.delete();
        }

        return success;
    }

    /**
     * Returns the preferred class name for a compiled WTKX resource. The WTKX
     * binding process will automatically attempt to discover and use compiled
     * versions of WTKX resources (as opposed to parsing WTKX at runtime using
     * {@link WTKXSerializer}) based on their preferred class names.
     *
     * @param referenceClass
     * The class relative to which the WTKX resource will be considered.
     * Use <tt>null</tt> to specify that the WTKX resource is relative to no
     * class.
     *
     * @param resourceName
     * A path name that identifies the WTKX resource. The path name should be
     * of the form defined by {@link Class#getResource(String)} and is relative
     * to the reference class. Note that this is the same form as is defined in
     * {@link Bindable.Load#resourceName()}.
     *
     * @return
     * The preferred class name for a compiled version of the specified WTKX
     * resource.
     */
    public static String getPreferredClassName(Class<?> referenceClass, String resourceName) {
        String name = resourceName;

        if (resourceName.startsWith("/")) {
            name = name.substring(1);

            name = name.replace('.', '_');
            name = name.replace('/', '.');
        } else {
            while (referenceClass.isArray()) {
                referenceClass = referenceClass.getComponentType();
            }

            name = name.replace('.', '_');
            name = name.replace('/', '.');

            String baseName = referenceClass.getName();
            int index = baseName.lastIndexOf('.');
            if (index != -1) {
                name = baseName.substring(0, index) + "." + name;
            }
        }

        return name;
    }

    /**
     * Gets the compiled class created from a WTKX resource. This assumes that
     * the class is named according to the {@linkplain
     * #getPreferredClassName(Class,String) preferred class name} for
     * compiled WTKX resources. If a compiled version of the resource exists
     * but is named differently, this method will not discover it.
     *
     * @param referenceClass
     * The class relative to which the WTKX resource will be considered.
     * Use <tt>null</tt> to specify that the WTKX resource is relative to no
     * class.
     *
     * @param resourceName
     * A path name that identifies the WTKX resource. The path name should be
     * of the form defined by {@link Class#getResource(String)} and is relative
     * to the reference class. Note that this is the same form as is defined in
     * {@link Bindable.Load#resourceName()}.
     *
     * @return
     * The compiled WTKX class, or <tt>null</tt> if no such class was
     * discovered.
     */
    @SuppressWarnings("unchecked")
    public static Class<Bindable.ObjectHierarchy> getClass(Class<?> referenceClass,
        String resourceName) {
        Class<Bindable.ObjectHierarchy> result = null;

        String className = getPreferredClassName(referenceClass, resourceName);
        try {
            result = (Class<Bindable.ObjectHierarchy>)Class.forName(className);
        } catch (ClassNotFoundException ex) {
            // No-op
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    public int run(InputStream in, OutputStream out, OutputStream err, String... arguments) {
        // TODO
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public Set<SourceVersion> getSourceVersions() {
        return Collections.unmodifiableSet(EnumSet.range
            (SourceVersion.RELEASE_5, SourceVersion.latest()));
    }
}
