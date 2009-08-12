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
package org.apache.pivot.wtkx;

import java.io.IOException;
import java.util.Collections;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

/**
 * Provides the facility by which WTKX resources can be compiled into Java
 * classes. This enables callers to leverage compile-time checking of their
 * WTKX files as well as a performance improvement over runtime WTKX parsing
 * (via {@link WTKXSerializer}).
 * <p>
 * <b>NOTE</b>: This class contains experimental functionality and should be
 * considered a "preview" until a future release.
 *
 * @author tvolkert
 */
public class Compiler {
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
     * to the base package.
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
     * to the reference class.
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
}
