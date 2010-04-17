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
 * Executes an HTTP DELETE operation.
 */
public class DeleteQuery extends Query<Void> {
    public static final Method METHOD = Method.DELETE;

    public DeleteQuery(String hostname, String path) {
        this(hostname, DEFAULT_PORT, path, false);
    }

    public DeleteQuery(String hostname, int port, String path, boolean secure) {
        this(hostname, port, path, secure, DEFAULT_EXECUTOR_SERVICE);
    }

    public DeleteQuery(String hostname, int port, String path, boolean secure,
        ExecutorService executorService) {
        super(hostname, port, path, secure, executorService);
    }

    @Override
    public Method getMethod() {
        return METHOD;
    }

    /**
     * Synchronously executes the DELETE operation.
     */
    @Override
    public Void execute() throws QueryException {
        execute(METHOD, null);
        return null;
    }
}
