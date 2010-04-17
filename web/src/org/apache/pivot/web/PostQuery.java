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
package org.apache.pivot.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;

/**
 * Executes an HTTP POST operation.
 */
public class PostQuery extends Query<URL> {
    private Object value = null;

    public static final Method METHOD = Method.POST;

    public PostQuery(String hostname, String path) {
        this(hostname, DEFAULT_PORT, path, false);
    }

    public PostQuery(String hostname, int port, String path, boolean secure) {
        this(hostname, port, path, secure, DEFAULT_EXECUTOR_SERVICE);
    }

    public PostQuery(String hostname, int port, String path, boolean secure,
        ExecutorService executorService) {
        super(hostname, port, path, secure, executorService);
    }

    @Override
    public Method getMethod() {
        return METHOD;
    }

    /**
     * Returns the value that will be POSTed to the server when the query is
     * executed.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value that will be POSTed to the server when the query is
     * executed.
     *
     * @param value
     * The value to POST to the server.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Synchronously executes the POST operation.
     *
     * @return
     * A URL that uniquely identifies the location of the resource created
     * on the server by the operation, or <tt>null</tt> if the server did
     * not return a location.
     */
    @Override
    public URL execute() throws QueryException {
        URL valueLocation = null;

        execute(METHOD, value);

        if (getStatus() == Status.CREATED) {
            String location = getResponseHeaders().get("Location");
            if (location != null) {
                try {
                    valueLocation = new URL(getLocation(), location);
                } catch(MalformedURLException exception) {
                    throw new RuntimeException(exception);
                }
            }
        }

        return valueLocation;
    }
}
