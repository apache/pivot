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

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Keyboard.KeyLocation;

/**
 * Represents the entry point into a WTK application.
 * <p>
 * These methods are called by the application context. In general, they should
 * not be invoked directly by the application.
 */
public interface Application {
    /**
     * Application adapter.
     */
    public static class Adapter implements Application {
        @Override
        public void startup(Display display, Map<String, String> properties) throws Exception {
            // empty block
        }

        @Override
        public boolean shutdown(boolean optional) throws Exception {
            return false;
        }

        @Override
        public void suspend() throws Exception {
            // empty block
        }

        @Override
        public void resume() throws Exception {
            // empty block
        }
    }

    /**
     * Optional interface that allows an application to present information
     * about itself.
     */
    public interface AboutHandler {
        /**
         * Called to notify the application that it should present its
         * "about" information.
         */
        public void aboutRequested();
    }

    /**
     * Optional interface that allows an application to handle unprocessed
     * key events (keystrokes that are processed when no component has the
     * input focus).
     */
    public interface UnprocessedKeyHandler {
        /**
         * UnprocessedKeyHandler adapter.
         */
        public static class Adapter implements UnprocessedKeyHandler {
            @Override
            public void keyTyped(char character) {
                // empty block
            }

            @Override
            public void keyPressed(int keyCode, KeyLocation keyLocation) {
                // empty block
            }

            @Override
            public void keyReleased(int keyCode, KeyLocation keyLocation) {
                // empty block
            }
        }

        public void keyTyped(char character);
        public void keyPressed(int keyCode, Keyboard.KeyLocation keyLocation);
        public void keyReleased(int keyCode, Keyboard.KeyLocation keyLocation);
    }

    /**
     * Optional interface that allows an application to handle uncaught
     * exceptions thrown during a user input event.
     */
    public interface UncaughtExceptionHandler {
        public void uncaughtExceptionThrown(Exception exception);
    }

    /**
     * Called when the application is starting up.
     *
     * @param display
     * The display on which this application was started.
     *
     * @param properties
     * Initialization properties passed to the application.
     */
    public void startup(Display display, Map<String, String> properties) throws Exception;

    /**
     * Called when the application is being shut down.
     *
     * @param optional
     * If <tt>true</tt>, the shutdown may be cancelled by returning a value of
     * <tt>true</tt>.
     *
     * @return
     * <tt>true</tt> to cancel shutdown, <tt>false</tt> to continue.
     */
    public boolean shutdown(boolean optional) throws Exception;

    /**
     * Called to notify the application that it is being suspended.
     */
    public void suspend() throws Exception;

    /**
     * Called when a suspended application has been resumed.
     */
    public void resume() throws Exception;
}
