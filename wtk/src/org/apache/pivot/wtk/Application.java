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

/**
 * Represents the entry point into a WTK application.
 *
 * @author gbrown
 */
public interface Application {
    /**
     * Optional interface that allows an application to present information
     * about itself.
     *
     * @author gbrown
     */
    public interface About {
        /**
         * Called to notify the application that it should present it's
         * "about" information.
         */
        public void handleAbout();
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
     * If <tt>true</tt>, the shutdown may be canceled by returning a value of
     * <tt>false</tt>.
     *
     * @return
     * <tt>true</tt> to continue shutdown, <tt>false</tt> to cancel.
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
