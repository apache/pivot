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
package org.apache.pivot.wtk;

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Map;

/**
 * Represents the entry point for a WTK application that has a container for
 * application-scope properties. In general, all methods derived from
 * Application should not be invoked directly by the application.
 *
 * @see Application
 * @see Application.Adapter
 */
public interface ApplicationWithProperties extends Application {
    /**
     * ApplicationWithProperties adapter.
     */
    public static class Adapter extends Application.Adapter implements ApplicationWithProperties {
        // TODO: verify if change with something (but general) like
        // ResourceCacheDictionary, and then synchronize its methods ...
        private Map<String, Object> properties = new HashMap<>();

        @Override
        public Map<String, Object> getProperties() {
            return properties;
        }
    }

    /**
     * Optional interface that allows an application to present information
     * about itself.
     */
    public interface AboutHandler extends Application.AboutHandler {
        // empty block
    }

    /**
     * Optional interface that allows an application to handle unprocessed key
     * events (keystrokes that are processed when no component has the input
     * focus).
     */
    public interface UnprocessedKeyHandler extends Application.UnprocessedKeyHandler {
        // empty block
    }

    /**
     * Optional interface that allows an application to handle uncaught
     * exceptions thrown during a user input event.
     */
    public interface UncaughtExceptionHandler extends Application.UncaughtExceptionHandler {
        // empty block
    }

    /**
     * Get the property container at application-level scope.
     *
     * @return the container, with all values inside (if any).
     */
    public Map<String, Object> getProperties();

}
