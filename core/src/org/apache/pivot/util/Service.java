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
package org.apache.pivot.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utility class for locating and instantiating service providers.
 */
public class Service {
    /**
     * Attempts to load a service provider.
     *
     * @param providerName
     * The name of the provider to load. The method first looks for a system
     * property with this name. The value of the property is expected to be the
     * name of a class that implements the expected provider interface.
     * <p>
     * If the system property does not exist, the method then attempts to load
     * a resource with this name from the META-INF/services directory. The
     * resource is expected to be a text file containing a single line that is
     * the name of the provider class.
     */
    public static Object getProvider(String providerName) {
        String providerClassName = null;

        // First look for a system property
        try {
            providerClassName = System.getProperty(providerName);
        } catch(SecurityException exception) {
            // No-op
        }

        // Next look for a service descriptor on the classpath
        if (providerClassName == null) {
            String serviceName = "META-INF/services/" + providerName;

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream serviceInputStream = classLoader.getResourceAsStream(serviceName);

            if (serviceInputStream != null) {
                try {
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader(serviceInputStream, "UTF-8"));
                        String line = reader.readLine();
                        while (line != null
                            && (line.length() == 0
                                || line.startsWith("#"))) {
                            line = reader.readLine();
                        }

                        providerClassName = line;
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                    }
                } catch(IOException exception) {
                    // No-op
                }
            }
        }

        // Try to load the provider class
        Class<?> providerClass = null;

        if (providerClassName != null) {
            try {
                providerClass = Class.forName(providerClassName);
            } catch(ClassNotFoundException exception) {
                // The specified class could not be found
            }
        }

        Object provider = null;
        if (providerClass != null) {
            try {
                provider = providerClass.newInstance();
            } catch(InstantiationException exception) {
                // No-op
            } catch(IllegalAccessException exception) {
                // No-op
            }
        }

        return provider;
    }
}
