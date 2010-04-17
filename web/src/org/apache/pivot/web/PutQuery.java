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

import java.util.concurrent.ExecutorService;

/**
 * Executes an HTTP PUT operation.
 */
public class PutQuery extends Query<Boolean> {
    private Object value = null;

    public static final Method METHOD = Method.PUT;

    public PutQuery(String hostname, String path) {
        this(hostname, DEFAULT_PORT, path, false);
    }

    public PutQuery(String hostname, int port, String path, boolean secure) {
        this(hostname, port, path, secure, DEFAULT_EXECUTOR_SERVICE);
    }

    public PutQuery(String hostname, int port, String path, boolean secure,
        ExecutorService executorService) {
        super(hostname, port, path, secure, executorService);
    }

    @Override
    public Method getMethod() {
        return METHOD;
    }

    /**
     * Returns the value that will be PUT to the server when the query is
     * executed.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value that will be PUT to the server when the query is
     * executed.
     *
     * @param value
     * The value to PUT to the server.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Synchronously executes the PUT operation.
     *
     * @return
     * <tt>true</tt> if the operation resulted in the creation of a server
     * resource; <tt>false</tt>, otherwise.
     */
    @Override
    public Boolean execute() throws QueryException {
        execute(METHOD, value);

        return (getStatus() == Status.CREATED);
    }
}
