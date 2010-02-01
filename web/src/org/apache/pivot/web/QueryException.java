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

import org.apache.pivot.util.concurrent.TaskExecutionException;

/**
 * Thrown when an error occurs while executing a web query.
 */
public class QueryException extends TaskExecutionException {
    private static final long serialVersionUID = -4949157889229298652L;

    private int status;

    public QueryException(int status) {
        this(status, null);
    }

    public QueryException(int status, String message) {
        super(message);
        this.status = status;
    }

    public QueryException(Throwable cause) {
        super(cause);
        status = Query.Status.INTERNAL_SERVER_ERROR;
    }

    /**
     * Returns the HTTP status code corresponding to the exception.
     *
     * @return
     * An HTTP status code reflecting the nature of the exception, or
     * <tt>0</tt> if the HTTP status is not known.
     */
    public int getStatus() {
        return status;
    }

    @Override
    public String getLocalizedMessage() {
        String message = super.getLocalizedMessage();
        return (message != null ? (status + " " + message) : String.valueOf(status));
    }
}
