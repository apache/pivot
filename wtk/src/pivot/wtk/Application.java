/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk;

import pivot.collections.Dictionary;

/**
 * <p>Represents the entry point into a WTK application.</p>
 *
 * @author gbrown
 */
public interface Application {
    /**
     * Called when the application is starting up.
     *
     * @param display
     * The display on which this application was started.
     *
     * @param properties
     * Initialization properties passed to the application.
     */
    public void startup(Display display, Dictionary<String, String> properties) throws Exception;

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
