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
package org.apache.pivot.ui;

import java.util.List;
import java.util.Map;

import org.apache.pivot.scene.Stage;
import org.apache.pivot.scene.media.Image;

/**
 * Represents the entry point into an application.
 * <p>
 * These methods are called by the application context. In general, they should
 * not be invoked directly by the application.
 */
public interface Application {
    /**
     * Returns the application's title.
     */
    public String getTitle();

    /**
     * Returns the application's icon list.
     */
    public List<Image> getIcons();

    /**
     * Called when the application is starting up.
     *
     * @param stage
     * The stage on which this application was started.
     *
     * @param properties
     * Initialization properties passed to the application.
     */
    public void startup(Stage stage, Map<String, String> properties) throws Exception;

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
