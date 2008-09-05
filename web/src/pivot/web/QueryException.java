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
package pivot.web;

import pivot.util.concurrent.TaskExecutionException;

/**
 * Thrown when an error occurs while executing a web query.
 */
public class QueryException extends TaskExecutionException {
    public static final long serialVersionUID = 0;

    private int status = -1;

    // TODO Define static constants for status codes

    public QueryException(int status) {
        this(status, null);
    }

    public QueryException(int status, String message) {
        super(message);

        this.status = status;
    }

    public QueryException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns the HTTP status code corresponding to the exception.
     *
     * @return
     * An HTTP status code reflecting the nature of the exception.
     */
    public int getStatus() {
        return status;
    }
}
