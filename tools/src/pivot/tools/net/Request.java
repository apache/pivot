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
package pivot.tools.net;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.io.IOTask;
import pivot.util.ListenerList;
import pivot.util.concurrent.SynchronizedListenerList;
import pivot.util.concurrent.TaskExecutionException;

/**
 * An asynchronous operation that executes an HTTP request.
 *
 * @author tvolkert
 */
public class Request extends IOTask<Response> {
    /**
     * Request headers dictionary implementation.
     */
    public final class RequestHeadersDictionary
        implements Dictionary<String, String>, Iterable<String> {
        public String get(String key) {
            return requestHeaders.get(key);
        }

        public String put(String key, String value) {
            return requestHeaders.put(key, value);
        }

        public String remove(String key) {
            return requestHeaders.remove(key);
        }

        public boolean containsKey(String key) {
            return requestHeaders.containsKey(key);
        }

        public boolean isEmpty() {
            return requestHeaders.isEmpty();
        }

        public Iterator<String> iterator() {
            return requestHeaders.iterator();
        }
    }

    /**
     * HTTP request listener list.
     *
     * @author tvolkert
     */
    private static class RequestListenerList extends SynchronizedListenerList<RequestListener>
        implements RequestListener {
        public synchronized void connected(Request httpRequest) {
            for (RequestListener listener : this) {
                listener.connected(httpRequest);
            }
        }

        public synchronized void requestSent(Request httpRequest) {
            for (RequestListener listener : this) {
                listener.requestSent(httpRequest);
            }
        }

        public synchronized void responseReceived(Request httpRequest) {
            for (RequestListener listener : this) {
                listener.responseReceived(httpRequest);
            }
        }

        public synchronized void failed(Request httpRequest) {
            for (RequestListener listener : this) {
                listener.failed(httpRequest);
            }
        }
    }

    private String method;
    private URL location = null;
    private byte[] body = null;
    private HostnameVerifier hostnameVerifier = null;

    private HashMap<String, String> requestHeaders = new HashMap<String, String>();

    private RequestHeadersDictionary requestHeadersDictionary = new RequestHeadersDictionary();

    private volatile long bytesExpected = -1;

    private RequestListenerList httpRequestListeners = new RequestListenerList();

    /**
     *
     */
    public Request(String method, String protocol, String host, int port, String path) {
        this.method = method;

        try {
            location = new URL(protocol, host, port, path);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Unable to construct URL.", ex);
        }
    }

    public String getMethod() {
        return method;
    }

    public URL getLocation() {
        return location;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    /**
     * Returns the request's request header dictionary.
     */
    public RequestHeadersDictionary getRequestHeaders() {
        return requestHeadersDictionary;
    }

    /**
     * Returns the HTTP body that will be sent to the server when the request is
     * executed.
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * Sets the HTTP body that will be sent to the server when the request is
     * executed.
     */
    public void setBody(byte[] body) {
        this.body = body;
    }

    /**
     * Gets the number of bytes that have been sent in the body of this
     * HTTP request.  This number will increment in between the
     * {@link RequestListener#connected(Request) connected} and
     * {@link RequestListener#requestSent(Request) requestSent} phases
     * of the <tt>RequestListener</tt> lifecycle methods. Interested
     * listeners can poll for this value during that phase.
     */
    public long getBytesSent() {
        return bytesSent;
    }

    /**
     * Gets the number of bytes that have been received from the server in the
     * body of the HTTP response.  This number will increment in between the
     * {@link RequestListener#requestSent(Request) requestSent} and
     * {@link RequestListener#responseReceived(Request) responseReceived}
     * phases of the <tt>RequestListener</tt> lifecycle methods. Interested
     * listeners can poll for this value during that phase.
     */
    public long getBytesReceived() {
        return bytesReceived;
    }

    /**
     * Gets the number of bytes that are expected to be received from the
     * server in the body of the server's HTTP response. This value reflects
     * the <tt>Content-Length</tt> HTTP response header and is thus merely an
     * expectation. The actual total number of bytes that will be received is
     * not known for certain until the full response has been received.
     * <p>
     * If the server did not specify a <tt>Content-Length</tt> HTTP response
     * header, a value of <tt>-1</tt> will be returned to indicate that this
     * value is unknown.
     */
    public long getBytesExpected() {
        return bytesExpected;
    }

    @Override
    public Response execute() throws TaskExecutionException {
        Response httpResponse = null;

        HttpURLConnection connection = null;

        bytesSent = 0;
        bytesReceived = 0;
        bytesExpected = -1;

        int statusCode = -1;
        String statusMessage = null;
        HashMap<String, String> responseHeaders = new HashMap<String, String>();

        try {
            // Open a connection
            connection = (HttpURLConnection)location.openConnection();
            connection.setRequestMethod(method);
            connection.setAllowUserInteraction(false);
            connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false);

            if (connection instanceof HttpsURLConnection
                && hostnameVerifier != null) {
                HttpsURLConnection httpsConnection = (HttpsURLConnection)connection;
                httpsConnection.setHostnameVerifier(hostnameVerifier);
            }

            // Set the request headers
            for (String key : requestHeaders) {
                connection.setRequestProperty(key, requestHeaders.get(key));
            }

            // Set the input/output state
            connection.setDoInput(true);
            connection.setDoOutput(!method.equalsIgnoreCase("GET"));

            // Connect to the server
            connection.connect();
            httpRequestListeners.connected(this);

            // Write the request body
            if (body != null && !method.equalsIgnoreCase("GET")) {
                OutputStream outputStream = null;
                try {
                    outputStream = new MonitoredOutputStream(connection.getOutputStream());
                    outputStream.write(body);
                } finally {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            }

            // Notify listeners that the request has been sent
            httpRequestListeners.requestSent(this);

            // Set the response info
            statusCode = connection.getResponseCode();
            statusMessage = connection.getResponseMessage();

            // NOTE Header indexes start at 1, not 0
            int i = 1;
            for (String key = connection.getHeaderFieldKey(i);
                key != null;
                key = connection.getHeaderFieldKey(++i)) {
                responseHeaders.put(key, connection.getHeaderField(i));
            }

            int statusPrefix = statusCode / 100;
            if (statusPrefix != 2) {
                // WE only retrieve the body for HTTP 200 (OK)
                httpResponse = new Response(statusCode, statusMessage, responseHeaders);
            } else {
                // Record the content length
                bytesExpected = connection.getContentLength();

                // Read the response body
                ByteArrayOutputStream byteArrayOutputStream =
                    new ByteArrayOutputStream(bytesExpected >= 0 ? (int)bytesExpected : 256);
                InputStream inputStream = null;
                try {
                    inputStream = new MonitoredInputStream(connection.getInputStream());

                    byte[] buf = new byte[256];
                    for (int n = inputStream.read(buf); n != -1; n = inputStream.read(buf)) {
                        byteArrayOutputStream.write(buf, 0, n);
                    }
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }

                httpResponse = new Response(statusCode, statusMessage,
                    responseHeaders, byteArrayOutputStream.toByteArray());
            }

            // Notify listeners that the response has been received
            httpRequestListeners.responseReceived(this);
        } catch (Exception ex) {
            httpRequestListeners.failed(this);
            throw new TaskExecutionException(ex);
        }

        return httpResponse;
    }

    /**
     * Returns the HTTP request listener list.
     */
    public ListenerList<RequestListener> getRequestListeners() {
        return httpRequestListeners;
    }

    /**
     * Adds a listener to the HTTP request listener list.
     *
     * @param listener
     */
    public void setRequestListener(RequestListener listener) {
        httpRequestListeners.add(listener);
    }
}
