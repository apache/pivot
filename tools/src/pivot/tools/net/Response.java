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
package pivot.tools.net;

import java.util.Iterator;
import java.util.NoSuchElementException;

import pivot.collections.Dictionary;
import pivot.collections.Map;

/**
 * An HTTP response from a web server.
 *
 * @author tvolkert
 */
public class Response {
    /**
     * Response headers dictionary implementation.
     */
    public final class ResponseHeadersDictionary
        implements Dictionary<String, String>, Iterable<String> {
        public String get(String key) {
            return (responseHeaders == null ? null : responseHeaders.get(key));
        }

        public String put(String key, String value) {
            throw new UnsupportedOperationException();
        }

        public String remove(String key) {
            throw new UnsupportedOperationException();
        }

        public boolean containsKey(String key) {
            return (responseHeaders == null ? false : responseHeaders.containsKey(key));
        }

        public boolean isEmpty() {
            return (responseHeaders == null ? true : responseHeaders.isEmpty());
        }

        public Iterator<String> iterator() {
            return (responseHeaders != null ? responseHeaders.iterator() : new Iterator<String>() {
                public boolean hasNext() {
                    return false;
                }

                public String next() {
                    throw new NoSuchElementException();
                }

                public void remove() {
                    throw new NoSuchElementException();
                }
            });
        }
    }

    private int statusCode;
    private String statusMessage;
    private Map<String, String> responseHeaders;
    private byte[] body;

    private ResponseHeadersDictionary responseHeadersDictionary = new ResponseHeadersDictionary();

    /**
     * Creates a new <tt>Response</tt>.
     */
    Response(int statusCode, String statusMessage, Map<String, String> responseHeaders) {
        this(statusCode, statusMessage, responseHeaders, null);
    }

    /**
     * Creates a new <tt>Response</tt>.
     */
    Response(int statusCode, String statusMessage, Map<String, String> responseHeaders, byte[] body) {
        if (statusMessage == null) {
            throw new IllegalArgumentException("statusMessage is null.");
        }

        if (responseHeaders == null) {
            throw new IllegalArgumentException("responseHeaders is null.");
        }

        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.responseHeaders = responseHeaders;
        this.body = body;
    }

    /**
     * Gets the HTTP response code.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Gets the human-readable HTTP status message.
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Returns the HTTP response headers as a read-only dictionary.
     */
    public ResponseHeadersDictionary getResponseHeaders() {
        return responseHeadersDictionary;
    }

    /**
     * Gets the HTTP response body as a byte array, or <tt>null</tt> if no HTTP
     * body was received.
     */
    public byte[] getBody() {
        return body;
    }

    @Override
    public String toString() {
        return statusCode + " " + statusMessage;
    }
}
