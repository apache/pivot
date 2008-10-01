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

/**
 * Executes an HTTP PUT operation.
 *
 * @author gbrown
 */
public class PutQuery extends Query<Void> {
    private Object value = null;

    public PutQuery(String hostname, String path) {
        this(hostname, DEFAULT_PORT, path, false);
    }

    public PutQuery(String hostname, int port, String path, boolean secure) {
        super(hostname, port, path, secure);
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
     */
    @Override
    public Void execute() throws QueryException {
        execute(Method.PUT, value);
        return null;
    }
}
