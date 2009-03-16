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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.serialization.JSONSerializer;
import pivot.serialization.Serializer;
import pivot.util.ListenerList;
import pivot.util.concurrent.AbortException;
import pivot.util.concurrent.Dispatcher;
import pivot.util.concurrent.SynchronizedListenerList;
import pivot.util.concurrent.Task;

/**
 * Abstract base class for web queries. A web query is an asynchronous
 * operation that executes one of the following HTTP methods:
 *
 * <ul>
 * <li>GET</li>
 * <li>POST</li>
 * <li>PUT</li>
 * <li>DELETE</li>
 * </ul>
 *
 * @param <V>
 * The type of the value retrieved or sent via the query. For GET operations,
 * it is {@link Object}; for POST operations, the type is {@link URL}. For PUT
 * and DELETE, it is {@link Void}.
 *
 * @author gbrown
 * @author tvolkert
 */
public abstract class Query<V> extends Task<V> {
    /**
     * The supported HTTP methods.
     *
     * @author gbrown
     */
    protected enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }

    /**
     * Arguments dictionary implementation.
     */
    public final class ArgumentsDictionary
        implements Dictionary<String, String>, Iterable<String> {
        public String get(String key) {
            return arguments.get(key);
        }

        public String put(String key, String value) {
            return arguments.put(key, value);
        }

        public String remove(String key) {
            return arguments.remove(key);
        }

        public boolean containsKey(String key) {
            return arguments.containsKey(key);
        }

        public boolean isEmpty() {
            return arguments.isEmpty();
        }

        public Iterator<String> iterator() {
            return arguments.iterator();
        }
    }

    /**
     * Request properties dictionary implementation.
     */
    public final class RequestPropertiesDictionary
        implements Dictionary<String, String>, Iterable<String> {
        public String get(String key) {
            return requestProperties.get(key);
        }

        public String put(String key, String value) {
            return requestProperties.put(key, value);
        }

        public String remove(String key) {
            return requestProperties.remove(key);
        }

        public boolean containsKey(String key) {
            return requestProperties.containsKey(key);
        }

        public boolean isEmpty() {
            return requestProperties.isEmpty();
        }

        public Iterator<String> iterator() {
            return requestProperties.iterator();
        }
    }

    /**
     * Response properties dictionary implementation.
     */
    public final class ResponsePropertiesDictionary
        implements Dictionary<String, String>, Iterable<String> {
        public String get(String key) {
            return responseProperties.get(key);
        }

        public String put(String key, String value) {
            throw new UnsupportedOperationException();
        }

        public String remove(String key) {
            throw new UnsupportedOperationException();
        }

        public boolean containsKey(String key) {
            return responseProperties.containsKey(key);
        }

        public boolean isEmpty() {
            return responseProperties.isEmpty();
        }

        public Iterator<String> iterator() {
            return responseProperties.iterator();
        }
    }

    /**
     * Output stream that monitors the bytes that are written to it by
     * incrementing the <tt>bytesSent</tt> member variable.
     *
     * @author tvolkert
     */
    private class MonitoredOutputStream extends OutputStream {
        private OutputStream outputStream;

        public MonitoredOutputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        public void close() throws IOException {
            outputStream.close();
        }

        public void flush() throws IOException {
            if (abort) {
                throw new AbortException();
            }

            outputStream.flush();
        }

        public void write(byte[] b) throws IOException {
            if (abort) {
                throw new AbortException();
            }

            outputStream.write(b);
            bytesSent += b.length;
        }

        public void write(byte[] b, int off, int len) throws IOException {
            if (abort) {
                throw new AbortException();
            }

            outputStream.write(b, off, len);
            bytesSent += len;
        }

        public void write(int b) throws IOException {
            if (abort) {
                throw new AbortException();
            }

            outputStream.write(b);
            bytesSent++;
        }
    }

    /**
     * Input stream that monitors the bytes that are read from it by
     * incrementing the <tt>bytesReceived</tt> member variable.
     *
     * @author tvolkert
     */
    private class MonitoredInputStream extends InputStream {
        private InputStream inputStream;

        long mark = 0;

        public MonitoredInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public int read() throws IOException {
            if (abort) {
                throw new AbortException();
            }

            int result = inputStream.read();

            if (result != -1) {
                bytesReceived++;
            }

            return result;
        }

        public int read(byte b[]) throws IOException {
            if (abort) {
                throw new AbortException();
            }

            int count = inputStream.read(b);

            if (count != -1) {
                bytesReceived += count;
            }

            return count;
        }

        public int read(byte b[], int off, int len) throws IOException {
            if (abort) {
                throw new AbortException();
            }

            int count = inputStream.read(b, off, len);

            if (count != -1) {
                bytesReceived += count;
            }

            return count;
        }

        public long skip(long n) throws IOException {
            if (abort) {
                throw new AbortException();
            }

            long count = inputStream.skip(n);
            bytesReceived += count;
            return count;
        }

        public int available() throws IOException {
            if (abort) {
                throw new AbortException();
            }

            return inputStream.available();
        }

        public void close() throws IOException {
            inputStream.close();
        }

        public void mark(int readLimit) {
            if (abort) {
                throw new AbortException();
            }

            inputStream.mark(readLimit);
            mark = bytesReceived;
        }

        public void reset() throws IOException {
            if (abort) {
                throw new AbortException();
            }

            inputStream.reset();
            bytesReceived = mark;
        }

        public boolean markSupported() {
            return inputStream.markSupported();
        }
    }

    /**
     * Query listener list.
     *
     * @author tvolkert
     */
    private static class QueryListenerList<V> extends SynchronizedListenerList<QueryListener<V>>
        implements QueryListener<V> {
        public synchronized void connected(Query<V> query) {
            for (QueryListener<V> listener : this) {
                listener.connected(query);
            }
        }

        public synchronized void requestSent(Query<V> query) {
            for (QueryListener<V> listener : this) {
                listener.requestSent(query);
            }
        }

        public synchronized void responseReceived(Query<V> query) {
            for (QueryListener<V> listener : this) {
                listener.responseReceived(query);
            }
        }

        public synchronized void failed(Query<V> query) {
            for (QueryListener<V> listener : this) {
                listener.failed(query);
            }
        }
    }

    private URL locationContext = null;

    private HashMap<String, String> arguments = new HashMap<String, String>();
    private HashMap<String, String> requestProperties = new HashMap<String, String>();
    private HashMap<String, String> responseProperties = new HashMap<String, String>();

    private ArgumentsDictionary argumentsDictionary = new ArgumentsDictionary();
    private RequestPropertiesDictionary requestPropertiesDictionary = new RequestPropertiesDictionary();
    private ResponsePropertiesDictionary responsePropertiesDictionary = new ResponsePropertiesDictionary();

    private Serializer serializer = new JSONSerializer();

    private volatile long bytesSent = 0;
    private volatile long bytesReceived = 0;
    private volatile long bytesExpected = -1;

    private QueryListenerList<V> queryListeners = new QueryListenerList<V>();

    private static Dispatcher DEFAULT_DISPATCHER = new Dispatcher();

    public static final int DEFAULT_PORT = -1;

    private static final String HTTP_PROTOCOL = "http";
    private static final String HTTPS_PROTOCOL = "https";
    private static final String URL_ENCODING = "UTF-8";

    /**
     * Creates a new web query.
     *
     * @param hostname
     * @param port
     * @param path
     * @param secure
     */
    public Query(String hostname, int port, String path, boolean secure) {
        super(DEFAULT_DISPATCHER);

        try {
            locationContext = new URL(secure ? HTTPS_PROTOCOL : HTTP_PROTOCOL,
                hostname, port, path);
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Unable to construct context URL.",
                exception);
        }
    }

    public String getHostname() {
        return locationContext.getHost();
    }

    public String getPath() {
        return locationContext.getFile();
    }

    public int getPort() {
        return locationContext.getPort();
    }

    public boolean isSecure() {
        String protocol = locationContext.getProtocol();
        return protocol.equalsIgnoreCase(HTTPS_PROTOCOL);
    }

    public URL getLocation() {
        StringBuilder queryStringBuilder = new StringBuilder();

        for (String key : arguments) {
            try {
                if (queryStringBuilder.length() > 0) {
                    queryStringBuilder.append("&");
                }

                queryStringBuilder.append(URLEncoder.encode(key, URL_ENCODING)
                    + "=" + URLEncoder.encode(arguments.get(key), URL_ENCODING));
            } catch (UnsupportedEncodingException exception) {
                throw new IllegalStateException("Unable to construct query string.", exception);
            }
        }

        URL location = null;
        try {
            String queryString = queryStringBuilder.length() > 0 ?
                "?" + queryStringBuilder.toString() : "";

            location = new URL(locationContext.getProtocol(),
                locationContext.getHost(),
                locationContext.getPort(),
                locationContext.getPath() + queryString);
        } catch (MalformedURLException exception) {
            throw new IllegalStateException("Unable to construct query URL.", exception);
        }

        return location;
    }

    /**
     * Returns the web query's arguments dictionary. Arguments are passed via
     * the query string of the web query's URL.
     */
    public ArgumentsDictionary getArguments() {
        return argumentsDictionary;
    }

    /**
     * Returns the web query's request property dictionary. Request properties
     * are passed via HTTP headers when the query is executed.
     */
    public RequestPropertiesDictionary getRequestProperties() {
        return requestPropertiesDictionary;
    }

    /**
     * Returns the web query's response property dictionary. Response properties
     * are returned via HTTP headers when the query is executed.
     */
    public ResponsePropertiesDictionary getResponseProperties() {
        return responsePropertiesDictionary;
    }

    /**
     * Returns the serializer used to stream the value passed to or from the
     * web query. By default, an instance of {@link JSONSerializer} is used.
     */
    public Serializer getSerializer() {
        return serializer;
    }

    /**
     * Sets the serializer used to stream the value passed to or from the
     * web query.
     *
     * @param serializer
     */
    public void setSerializer(Serializer serializer) {
        if (serializer == null) {
            throw new IllegalArgumentException("serializer is null.");
        }

        this.serializer = serializer;
    }

    /**
     * Gets the number of bytes that have been sent in the body of this
     * query's HTTP request. This will only be non-zero for POST and PUT
     * requests, as GET and DELETE requests send no content to the server.
     * <p>
     * For POST and PUT requests, this number will increment in between the
     * {@link QueryListener#connected(Query) connected} and
     * {@link QueryListener#requestSent(Query) requestSent} phases of the
     * <tt>QueryListener</tt> lifecycle methods. Interested listeners can poll
     * for this value during that phase.
     */
    public long getBytesSent() {
        return bytesSent;
    }

    /**
     * Gets the number of bytes that have been received from the server in the
     * body of the server's HTTP response. This will generally only be non-zero
     * for GET requests, as POST, PUT, and DELETE requests generally don't
     * solicit response content from the server.
     * <p>
     * This number will increment in between the
     * {@link QueryListener#requestSent(Query) requestSent} and
     * {@link QueryListener#responseReceived(Query) responseReceived} phases of
     * the <tt>QueryListener</tt> lifecycle methods. Interested listeners can
     * poll for this value during that phase.
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

    protected Object execute(Method method, Object value)
        throws QueryException {
        URL location = getLocation();
        HttpURLConnection connection = null;

        bytesSent = 0;
        bytesReceived = 0;
        bytesExpected = -1;

        int status = -1;
        String message = null;

        try {
            // Clear any properties from a previous response
            responseProperties.clear();

            // Open a connection
            connection = (HttpURLConnection)location.openConnection();
            connection.setRequestMethod(method.toString());
            connection.setAllowUserInteraction(false);
            connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false);

            // Set the request headers
            for (String key : requestProperties) {
                connection.addRequestProperty(key, requestProperties.get(key));
            }

            // Set the input/output state
            connection.setDoInput(true);
            connection.setDoOutput(method == Method.POST || method == Method.PUT);

            // Connect to the server
            connection.connect();
            queryListeners.connected(this);

            // Write the request body
            if (method == Method.POST || method == Method.PUT) {
                OutputStream outputStream = null;
                try {
                    outputStream = connection.getOutputStream();
                    serializer.writeObject(value, new MonitoredOutputStream(outputStream));
                } finally {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            }

            // Notify listeners that the request has been sent
            queryListeners.requestSent(this);

            // Set the response info
            status = connection.getResponseCode();
            message = connection.getResponseMessage();

            // If the response was anything other than 2xx, throw an exception
            int statusPrefix = status / 100;
            if (statusPrefix != 2) {
                throw new QueryException(status, message);
            }

            // Record the content length
            bytesExpected = connection.getContentLength();

            // NOTE Header indexes start at 1, not 0
            int i = 1;
            for (String key = connection.getHeaderFieldKey(i);
                key != null;
                key = connection.getHeaderFieldKey(++i)) {
                responseProperties.put(key, connection.getHeaderField(i));
            }

            // Read the response body
            if (method == Method.GET) {
                InputStream inputStream = null;
                try {
                    inputStream = connection.getInputStream();
                    value = serializer.readObject(new MonitoredInputStream(inputStream));
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }

            // Notify listeners that the response has been received
            queryListeners.responseReceived(this);
        } catch (Exception exception) {
            queryListeners.failed(this);
            throw new QueryException(exception);
        }

        return value;
    }

    /**
     * Gets the query's <tt>QueryListener</tt>s.
     */
    public ListenerList<QueryListener<V>> getQueryListeners() {
        return queryListeners;
    }
}
