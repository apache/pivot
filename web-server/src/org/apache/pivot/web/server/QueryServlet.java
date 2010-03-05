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
package org.apache.pivot.web.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.serialization.CSVSerializer;
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;
import org.apache.pivot.web.Query;
import org.apache.pivot.web.QueryDictionary;
import org.apache.pivot.web.QueryException;

/**
 * Abstract base class for query servlets.
 */
public abstract class QueryServlet extends HttpServlet {
    private static final long serialVersionUID = 4881638232902478092L;

    private boolean determineContentLength = false;
    private HashMap<String, Class<? extends Serializer<?>>> serializerTypes
        = new HashMap<String, Class<? extends Serializer<?>>>();

    private transient ThreadLocal<String> hostname = new ThreadLocal<String>();
    private transient ThreadLocal<Integer> port = new ThreadLocal<Integer>();
    private transient ThreadLocal<String> servletPath = new ThreadLocal<String>();
    private transient ThreadLocal<Boolean> secure = new ThreadLocal<Boolean>();

    private transient ThreadLocal<QueryDictionary> parameters = new ThreadLocal<QueryDictionary>();
    private transient ThreadLocal<QueryDictionary> requestHeaders = new ThreadLocal<QueryDictionary>();
    private transient ThreadLocal<QueryDictionary> responseHeaders = new ThreadLocal<QueryDictionary>();

    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String URL_ENCODING = "UTF-8";

    public static final String ACTION_HEADER = "Action";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String CONTENT_LENGTH_HEADER = "Content-Length";
    public static final String LOCATION_HEADER = "Location";

    @Override
    public void init() throws ServletException {
        super.init();

        // TODO Read determineContentLength and MIME type-serializer class mapping
        // from init params

        serializerTypes.put(JSONSerializer.MIME_TYPE, JSONSerializer.class);
        serializerTypes.put(CSVSerializer.MIME_TYPE, CSVSerializer.class);
    }

    /**
     * Gets the host name that was requested.
     */
    public String getHostname() {
        return hostname.get();
    }

    /**
     * Returns the Internet Protocol (IP) port number of the interface on which
     * the request was received.
     */
    public int getPort() {
        return port.get();
    }

    /**
     * Returns the portion of the request URL representing the servlet path.
     */
    public String getServletPath() {
        return servletPath.get();
    }

    /**
     * Tells whether the request has been ecrypted over HTTPS.
     */
    public boolean isSecure() {
        return secure.get();
    }

    /**
     * Returns the name of the HTTP protocol that the request is using.
     */
    public String getProtocol() {
        return isSecure() ? HTTPS_PROTOCOL : HTTP_PROTOCOL;
    }

    /**
     * Returns the servlet's parameter dictionary, which holds the values
     * passed in the HTTP request query string.
     */
    public QueryDictionary getParameters() {
        return parameters.get();
    }

    /**
     * Returns the servlet's request header dictionary, which holds the HTTP
     * request headers.
     */
    public QueryDictionary getRequestHeaders() {
        return requestHeaders.get();
    }

    /**
     * Returns the servlet's response header dictionary, which holds the HTTP
     * response headers that will be sent back to the client.
     */
    public QueryDictionary getResponseHeaders() {
        return responseHeaders.get();
    }

    /**
     * Prepares a servlet for request execution. This method is called immediately
     * prior to the {@link #validate(String)} method.
     * <p>
     * The default implementation is a no-op.
     *
     * @throws ServletException
     */
    public void prepare() throws ServletException {
    }

    /**
     * Disposes any resources allocated in {@link #prepare()}. This method is
     * guaranteed to be called even if the HTTP handler method throws.
     * <p>
     * The default implementation is a no-op.
     *
     * @throws ServletException
     */
    public void dispose() throws ServletException {
    }

    /**
     * Validates a servlet for request execution. This method is called immediately
     * prior to the HTTP handler method.
     * <p>
     * The default implementation is a no-op.
     *
     * @throws QueryException
     */
    public void validate(String path) throws QueryException {
    }

    /**
     * Allows a servlet to configure a serializer
     *
     * @param serializer
     */
    public void configureSerializer(Serializer<Object> serializer, String path) {
    }

    /**
     * Handles an HTTP GET request. The default implementation throws an HTTP
     * 405 query exception.
     *
     * @param path
     *
     * @return
     * The result of the GET.
     *
     * @throws QueryException
     */
    public Object doGet(String path) throws QueryException {
        throw new QueryException(Query.Status.METHOD_NOT_ALLOWED);
    }

    /**
     * Handles an HTTP POST request. The default implementation throws an HTTP
     * 405 query exception.
     *
     * @param path
     * @param value
     *
     * @return
     * A URL containing the location of the created resource.
     *
     * @throws QueryException
     */
    public URL doPost(String path, Object value) throws QueryException {
        throw new QueryException(Query.Status.METHOD_NOT_ALLOWED);
    }

    /**
     * Handles an HTTP POST/Action request. The default implementation throws an HTTP
     * 405 query exception.
     *
     * @param path
     * @param action
     *
     * @throws QueryException
     */
    public void doPostAction(String path, String action) throws QueryException {
        throw new QueryException(Query.Status.METHOD_NOT_ALLOWED);
    }

    /**
     * Handles an HTTP GET request. The default implementation throws an HTTP
     * 405 query exception.
     *
     * @param path
     * @param value
     *
     * @throws QueryException
     */
    public void doPut(String path, Object value) throws QueryException {
        throw new QueryException(Query.Status.METHOD_NOT_ALLOWED);
    }

    /**
     * Handles an HTTP GET request. The default implementation throws an HTTP
     * 405 query exception.
     *
     * @param path
     *
     * @throws QueryException
     */
    public void doDelete(String path) throws QueryException {
        throw new QueryException(Query.Status.METHOD_NOT_ALLOWED);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void service(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        try {
            try {
                URL url = new URL(request.getRequestURL().toString());
                hostname.set(url.getHost());
                port.set(request.getLocalPort());
                servletPath.set(request.getServletPath());
                secure.set(url.getProtocol().equalsIgnoreCase(HTTPS_PROTOCOL));
            } catch (MalformedURLException exception) {
                throw new ServletException(exception);
            }

            parameters.set(new QueryDictionary(true));
            requestHeaders.set(new QueryDictionary(false));
            responseHeaders.set(new QueryDictionary(false));

            // Copy the query string into the arguments dictionary
            String queryString = request.getQueryString();
            if (queryString != null) {
                QueryDictionary parametersDictionary = parameters.get();
                String[] pairs = queryString.split("&");

                for (int i = 0, n = pairs.length; i < n; i++) {
                    String[] pair = pairs[i].split("=");

                    String key = URLDecoder.decode(pair[0], URL_ENCODING);
                    String value = URLDecoder.decode((pair.length > 1) ? pair[1] : "", URL_ENCODING);

                    parametersDictionary.add(key, value);
                }
            }

            // Copy the request headers into the request properties dictionary
            QueryDictionary requestHeaderDictionary = requestHeaders.get();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);

                requestHeaderDictionary.add(headerName, headerValue);
            }

            // Prepare the servlet for request processing
            prepare();

            // Process the request
            super.service(request, response);
        } catch (IOException exception) {
            System.err.println(exception);
            throw exception;
        } catch (RuntimeException exception) {
            System.err.println(exception);
            throw exception;
        } finally {
            // Clean up thread local variables
            hostname.remove();
            port.remove();
            servletPath.remove();
            secure.remove();
            parameters.remove();
            requestHeaders.remove();
            responseHeaders.remove();

            // Clean up any allocated resources
            dispose();
        }
    }

    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        Serializer<Object> serializer = createSerializer(request.getHeader(CONTENT_TYPE_HEADER));

        Object result = null;
        try {
            String path = request.getPathInfo();
            validate(path);
            configureSerializer(serializer, path);
            result = doGet(path);
        } catch (QueryException exception) {
            response.setStatus(exception.getStatus());
            response.flushBuffer();
        }

        if (!response.isCommitted()) {
            response.setStatus(200);
            setResponseHeaders(response);
            response.setContentType(serializer.getMIMEType(result));

            OutputStream responseOutputStream = response.getOutputStream();

            if (determineContentLength) {
                File tempFile = File.createTempFile(getClass().getName(), null);

                // Serialize the result to an intermediary file
                FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                try {
                    serializer.writeObject(result, fileOutputStream);
                } catch (SerializationException exception) {
                    throw new ServletException(exception);
                } finally {
                    fileOutputStream.close();
                }

                // Set the content length header
                response.setHeader(CONTENT_LENGTH_HEADER, String.valueOf(tempFile.length()));

                // Write the contents of the file out to the response
                FileInputStream fileInputStream = new FileInputStream(tempFile);
                try {
                    byte[] buffer = new byte[1024];
                    int nBytes;
                    do {
                        nBytes = fileInputStream.read(buffer);
                        if (nBytes > 0) {
                            responseOutputStream.write(buffer, 0, nBytes);
                        }
                    } while (nBytes != -1);
                } finally {
                    fileInputStream.close();
                }
            } else {
                try {
                    serializer.writeObject(result, responseOutputStream);
                } catch (SerializationException exception) {
                    throw new ServletException(exception);
                }
            }

            response.flushBuffer();
        }
    }

    @Override
    protected final void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        Serializer<Object> serializer = createSerializer(request.getHeader(CONTENT_TYPE_HEADER));
        String action = request.getHeader(ACTION_HEADER);

        if (action == null) {
            Object value = null;
            try {
                value = serializer.readObject(request.getInputStream());
            } catch (SerializationException exception) {
                throw new ServletException(exception);
            }

            URL location = null;
            try {
                String path = request.getPathInfo();
                validate(path);
                configureSerializer(serializer, path);
                location = doPost(path, value);
            } catch (QueryException exception) {
                response.setStatus(exception.getStatus());
                response.flushBuffer();
            }

            if (!response.isCommitted()) {
                response.setStatus(201);
                setResponseHeaders(response);
                response.setHeader(LOCATION_HEADER, location.toString());
                response.setContentLength(0);
            }
        } else {
            try {
                String path = request.getPathInfo();
                validate(path);
                doPostAction(path, action);
            } catch (QueryException exception) {
                response.setStatus(exception.getStatus());
                response.flushBuffer();
            }

            if (!response.isCommitted()) {
                response.setStatus(204);
                setResponseHeaders(response);
                response.setContentLength(0);
            }

            response.flushBuffer();
        }
    }

    @Override
    protected final void doPut(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        Serializer<Object> serializer = createSerializer(request.getHeader(CONTENT_TYPE_HEADER));

        Object value = null;
        try {
            value = serializer.readObject(request.getInputStream());
        } catch (SerializationException exception) {
            throw new ServletException(exception);
        }

        try {
            String path = request.getPathInfo();
            validate(path);
            configureSerializer(serializer, path);
            doPut(path, value);
        } catch (QueryException exception) {
            response.setStatus(exception.getStatus());
            response.flushBuffer();
        }

        if (!response.isCommitted()) {
            response.setStatus(204);
            setResponseHeaders(response);
            response.setContentLength(0);
            response.flushBuffer();
        }
    }

    @Override
    protected final void doDelete(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        try {
            String path = request.getPathInfo();
            validate(path);
            doDelete(path);
        } catch (QueryException exception) {
            response.setStatus(exception.getStatus());
            response.flushBuffer();
        }

        if (!response.isCommitted()) {
            response.setStatus(204);
            setResponseHeaders(response);
            response.setContentLength(0);
            response.flushBuffer();
        }
    }

    @Override
    protected final void doHead(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.flushBuffer();
    }

    @Override
    protected final void doOptions(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.flushBuffer();
    }

    @Override
    protected final void doTrace(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.flushBuffer();
    }

    @SuppressWarnings("unchecked")
    protected Serializer<Object> createSerializer(String mimeType)
        throws ServletException {
        if (mimeType == null) {
            mimeType = JSONSerializer.MIME_TYPE;
        } else {
            mimeType = mimeType.substring(0, mimeType.indexOf(';'));
            mimeType = mimeType.trim();
        }

        Class<? extends Serializer<?>> serializerType = serializerTypes.get(mimeType);
        if (serializerType == null) {
            throw new ServletException("A serializer for " + mimeType + " not found.");
        }

        Serializer<Object> serializer = null;
        try {
            serializer = (Serializer<Object>)serializerType.newInstance();
        } catch (InstantiationException exception) {
            throw new ServletException(exception);
        } catch (IllegalAccessException exception) {
            throw new ServletException(exception);
        }

        return serializer;
    }

    private void setResponseHeaders(HttpServletResponse response) {
        QueryDictionary responseHeaderDictionary = responseHeaders.get();

        for (String key : responseHeaderDictionary) {
            for (int i = 0, n = responseHeaderDictionary.getLength(key); i < n; i++) {
                response.addHeader(key, responseHeaderDictionary.get(key, i));
            }
        }
    }
}
