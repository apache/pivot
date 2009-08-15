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

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;
import org.apache.pivot.util.Base64;
import org.apache.pivot.web.QueryDictionary;

/**
 * Abstract base class for web query servlets. It is the server counterpart to
 * {@link org.apache.pivot.web.Query}.
 *
 */
public abstract class QueryServlet extends HttpServlet {
    static final long serialVersionUID = -646654620936816287L;

    /**
     * Supported HTTP methods.
     *
     */
    public enum Method {
        GET,
        POST,
        PUT,
        DELETE;
    }

    /**
     * User credentials, which will be made availale if the servlet's
     * <tt>authenticationRequired</tt> flag is set to <tt>true</tt>.
     *
     */
    public static final class Credentials {
        private String username;
        private String password;

        private Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

    private boolean authenticationRequired = false;
    private boolean determineContentLength = false;

    private Class<? extends Serializer<?>> serializerClass = JSONSerializer.class;

    private transient ThreadLocal<Credentials> credentials = new ThreadLocal<Credentials>();

    private transient ThreadLocal<String> hostname = new ThreadLocal<String>();
    private transient ThreadLocal<String> contextPath = new ThreadLocal<String>();
    private transient ThreadLocal<String> queryPath = new ThreadLocal<String>();
    private transient ThreadLocal<Integer> port = new ThreadLocal<Integer>();
    private transient ThreadLocal<Boolean> secure = new ThreadLocal<Boolean>();
    private transient ThreadLocal<Method> method = new ThreadLocal<Method>();

    private transient ThreadLocal<QueryDictionary> parameters = new ThreadLocal<QueryDictionary>();
    private transient ThreadLocal<QueryDictionary> requestHeaders = new ThreadLocal<QueryDictionary>();
    private transient ThreadLocal<QueryDictionary> responseHeaders = new ThreadLocal<QueryDictionary>();

    private static final String BASIC_AUTHENTICATION_TAG = "Basic";
    private static final String HTTP_PROTOCOL = "http";
    private static final String HTTPS_PROTOCOL = "https";
    private static final String URL_ENCODING = "UTF-8";

    /**
     * Tells whether this servlet is configured to always determine the content
     * length of outgoing responses and set the <tt>Content-Length</tt> HTTP
     * response header accordingly. If this flag is <tt>false</tt>, it is up to
     * the servlet's discretion as to when to set the <tt>Content-Length</tt>
     * header (it will do so if it is trivially easy). If this is set to
     * <tt>true</tt>, it will force the servlet to always set the header, but
     * doing so will incur a performance penalty, as the servlet will be unable
     * to stream the response directly to the HTTP output stream as it gets
     * serialized.
     */
    public boolean isDetermineContentLength() {
        return determineContentLength;
    }

    /**
     * Sets the value of the <tt>determineContentLength</tt> flag.
     *
     * @see #isDetermineContentLength()
     */
    public void setDetermineContentLength(boolean determineContentLength) {
        this.determineContentLength = determineContentLength;
    }

    /**
     * Tells whether or not this servlet will require authentication data. If
     * set to <tt>true</tt>, and un-authenticated requests are received, the
     * servlet will automatically respond with a request for authentication.
     */
    public boolean isAuthenticationRequired() {
        return authenticationRequired;
    }

    /**
     * Sets whether or not this servlet will require authentication data. If
     * set to <tt>true</tt>, and un-authenticated requests are received, the
     * servlet will automatically respond with a request for authentication.
     */
    public void setAuthenticationRequired(boolean authenticationRequired) {
        this.authenticationRequired = authenticationRequired;

        if (!authenticationRequired) {
            credentials.remove();
        }
    }

    /**
     * Returns the class of serializer that will be used to stream the value
     * passed to or from the web query. By default, {@link JSONSerializer} is
     * used.
     *
     * @return
     * This servlet's serializer class
     */
    public Class<? extends Serializer<?>> getSerializerClass() {
        return serializerClass;
    }

    /**
     * Sets the class of serializer that will be used to stream the value
     * passed to or from the web query.
     *
     * @param serializerClass
     * This servlet's serializer class (must be non-<tt>null</tt>)
     */
    public void setSerializerClass(Class<? extends Serializer<?>> serializerClass) {
        if (serializerClass == null) {
            throw new IllegalArgumentException("serializerClass is null.");
        }

        this.serializerClass = serializerClass;
    }

    /**
     * Returns a newly allocated instance of this servlet's serializer class.
     *
     * @return
     * A new serializer
     */
    private Serializer<?> newSerializer() {
        Serializer<?> serializer;

        try {
            serializer = serializerClass.newInstance();
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        } catch (InstantiationException exception) {
            throw new RuntimeException(exception);
        }

        return serializer;
    }

    /**
     * Gets the host name that was requested.
     */
    public String getHostname() {
        return hostname.get();
    }

    /**
     * Returns the portion of the request URI that indicates the context of the
     * request. The context path always comes first in a request URI. The path
     * starts with a "/" character but does not end with a "/" character. For
     * servlets in the default (root) context, this method returns "".
     */
    public String getContextPath() {
        return contextPath.get();
    }

    /**
     * Returns the portion of the request URI that occurs after the context
     * path but preceding the query string. It will start with a "/" character.
     * For servlets in the default (root) context, this method returns the full
     * path.
     */
    public String getQueryPath() {
        return queryPath.get();
    }

    /**
     * Returns the Internet Protocol (IP) port number of the interface on which
     * the request was received.
     */
    public int getPort() {
        return port.get();
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
     * Gets the HTTP method with which the current request was made.
     */
    public Method getMethod() {
        return method.get();
    }

    /**
     * Gets the authentication credentials that were extracted from the
     * request. These are only available if the <tt>authenticationRequired</tt>
     * flag is set to <tt>true</tt>.
     */
    public Credentials getCredentials() {
        return credentials.get();
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
     * Called when an HTTP GET is received. This base method throws
     * <tt>UnsupportedOperationException</tt>, which will cause an HTTP 405
     * (method not allowed) to be sent in the response. Subclasses should
     * override this method if they wish to support GET requests.
     * <p>
     * Request parameters, and request/response headers are available to
     * subclasses via the corresponding query dictionary.
     *
     * @return
     * The object that was retrieved via the GET request. This object will be
     * serialized by this servlet's serializer before being included in the
     * HTTP response
     *
     * @see #getParameters()
     * @see #getRequestHeaders()
     * @see #getResponseHeaders()
     */
    protected Object doGet() throws ServletException {
        throw new UnsupportedOperationException();
    }

    /**
     * Called when an HTTP POST is received. This base method throws
     * <tt>UnsupportedOperationException</tt>, which will cause an HTTP 405
     * (method not allowed) to be sent in the response. Subclasses should
     * override this method if they wish to support POST requests.
     * <p>
     * Request parameters, and request/response headers are available to
     * subclasses via the corresponding query dictionary.
     *
     * @param value
     * The object that is being posted by the client. This object will have
     * been de-serialized from within the request by this servlet's serializer
     *
     * @return
     * The URL identifying the location of the object that was posted. The
     * semantics of this URL are up to the subclass to define
     *
     * @see #getParameters()
     * @see #getRequestHeaders()
     * @see #getResponseHeaders()
     */
    protected URL doPost(Object value) throws ServletException {
        throw new UnsupportedOperationException();
    }

    /**
     * Called when an HTTP PUT is received. This base method throws
     * <tt>UnsupportedOperationException</tt>, which will cause an HTTP 405
     * (method not allowed) to be sent in the response. Subclasses should
     * override this method if they wish to support PUT requests.
     * <p>
     * Request parameters, and request/response headers are available to
     * subclasses via the corresponding query dictionary.
     *
     * @param value
     * The object that is being updated by the client. This object will have
     * been de-serialized from within the request by this servlet's serializer
     *
     * @see #getParameters()
     * @see #getRequestHeaders()
     * @see #getResponseHeaders()
     */
    protected void doPut(Object value) throws ServletException {
        throw new UnsupportedOperationException();
    }

    /**
     * Called when an HTTP DELETE is received. This base method throws
     * <tt>UnsupportedOperationException</tt>, which will cause an HTTP 405
     * (method not allowed) to be sent in the response. Subclasses should
     * override this method if they wish to support DELETE requests.
     * <p>
     * Request parameters, and request/response headers are available to
     * subclasses via the corresponding query dictionary.
     *
     * @see #getParameters()
     * @see #getRequestHeaders()
     * @see #getResponseHeaders()
     */
    protected void doDelete() throws ServletException {
        throw new UnsupportedOperationException();
    }

    /**
     * Authorizes the current request, and throws a <tt>LoginException</tt> if
     * the request is not authorized. This method will only be called if the
     * <tt>authenticationRequired</tt> flag is set to <tt>true</tt>. Subclasses
     * wishing to authorize the authenticated user credentials may override
     * this method to perform that authorization. On the other hand, the
     * <tt>authorize</tt> method of <tt>QueryServlet</tt> does nothing, so
     * subclasses that wish to authenticate the request but not authorize
     * it may simply not override this method.
     * <p>
     * This method is guaranteed to be called after the arguments and request
     * properties have been made available.
     *
     * @throws LoginException
     * Thrown if the request is not authorized
     */
    protected void authorize() throws ServletException, LoginException {
        // No-op
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void service(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        try {
            parameters.set(new QueryDictionary());
            requestHeaders.set(new QueryDictionary());
            responseHeaders.set(new QueryDictionary());

            boolean proceed = true;

            if (authenticationRequired) {
                String authorization = request.getHeader("Authorization");

                if (authorization == null) {
                    proceed = false;
                    doUnauthorized(request, response);
                } else {
                    String encodedCredentials = authorization.substring
                        (BASIC_AUTHENTICATION_TAG.length() + 1);
                    String decodedCredentials = new String(Base64.decode(encodedCredentials));
                    String[] credentialsPair = decodedCredentials.split(":");

                    String username = credentialsPair.length > 0 ? credentialsPair[0] : "";
                    String password = credentialsPair.length > 1 ? credentialsPair[1] : "";

                    credentials.set(new Credentials(username, password));
                }
            }

            if (proceed) {
                // Extract our location context
                try {
                    URL url = new URL(request.getRequestURL().toString());
                    String requestURI = request.getRequestURI();
                    String requestContext = request.getContextPath();

                    hostname.set(url.getHost());
                    contextPath.set(requestContext);
                    queryPath.set(requestURI);
                    port.set(request.getLocalPort());
                    secure.set(url.getProtocol().equalsIgnoreCase(HTTPS_PROTOCOL));
                    method.set(Method.valueOf(request.getMethod().toUpperCase()));

                    if (requestURI.startsWith(requestContext)) {
                        queryPath.set(requestURI.substring(requestContext.length()));
                    }
                } catch (MalformedURLException exception) {
                    throw new ServletException(exception);
                }

                // Copy the query string into our arguments dictionary
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

                // Copy the request headers into our request properties dictionary
                QueryDictionary requestHeaderDictionary = requestHeaders.get();
                Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    String headerValue = request.getHeader(headerName);

                    requestHeaderDictionary.add(headerName, headerValue);
                }

                if (authenticationRequired) {
                    try {
                        authorize();
                    } catch (LoginException exception) {
                        proceed = false;
                        doForbidden(request, response);
                    }
                }
            }

            if (proceed) {
                super.service(request, response);
            }
        } finally {
            // Clean up our thread local variables
            credentials.remove();
            hostname.remove();
            contextPath.remove();
            queryPath.remove();
            port.remove();
            secure.remove();
            method.remove();
            parameters.remove();
            requestHeaders.remove();
            responseHeaders.remove();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        Serializer<Object> serializer = (Serializer<Object>)newSerializer();

        try {
            Object result = doGet();

            response.setStatus(200);
            setResponseHeaders(response);
            response.setContentType(serializer.getMIMEType(result));

            OutputStream responseOutputStream = response.getOutputStream();

            if (determineContentLength) {
                File tempFile = File.createTempFile("pivot", null);

                // Serialize our result to an intermediary file
                FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                try {
                    serializer.writeObject(result, fileOutputStream);
                } finally {
                    fileOutputStream.close();
                }

                // Our content length is the length of the file in bytes
                response.setHeader("Content-Length", String.valueOf(tempFile.length()));

                // Now write the contents of the file out to our response
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
                serializer.writeObject(result, responseOutputStream);
            }
        } catch (UnsupportedOperationException exception) {
            doMethodNotAllowed(response);
        } catch (SerializationException exception) {
            throw new ServletException(exception);
        }
    }

    @Override
    protected final void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        Serializer<?> serializer = newSerializer();

        try {
            Object value = serializer.readObject(request.getInputStream());

            URL url = doPost(value);

            response.setStatus(201);
            setResponseHeaders(response);
            response.setHeader("Location", url.toString());
            response.setContentLength(0);
            response.flushBuffer();
        } catch (UnsupportedOperationException exception) {
            doMethodNotAllowed(response);
        } catch (SerializationException exception) {
            throw new ServletException(exception);
        }
    }

    @Override
    protected final void doPut(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        Serializer<?> serializer = newSerializer();

        try {
            Object value = serializer.readObject(request.getInputStream());

            doPut(value);

            response.setStatus(204);
            setResponseHeaders(response);
            response.setContentLength(0);
            response.flushBuffer();
        } catch (UnsupportedOperationException exception) {
            doMethodNotAllowed(response);
        } catch (SerializationException exception) {
            throw new ServletException(exception);
        }
    }

    @Override
    protected final void doDelete(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        try {
            doDelete();

            response.setStatus(204);
            setResponseHeaders(response);
            response.setContentLength(0);
            response.flushBuffer();
        } catch (UnsupportedOperationException exception) {
            doMethodNotAllowed(response);
        }
    }

    @Override
    protected final void doHead(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        doMethodNotAllowed(response);
    }

    @Override
    protected final void doOptions(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        doMethodNotAllowed(response);
    }

    @Override
    protected final void doTrace(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        doMethodNotAllowed(response);
    }

    /**
     *
     */
    private void doUnauthorized(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.setHeader("WWW-Authenticate", "BASIC realm=\""
            + request.getServletPath() +"\"");
        response.setStatus(401);
        response.setContentLength(0);
        response.flushBuffer();
    }

    /**
     *
     */
    private void doForbidden(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.setStatus(403);
        response.setContentLength(0);
        response.flushBuffer();
    }

    /**
     *
     */
    private void doMethodNotAllowed(HttpServletResponse response) throws IOException {
        response.setStatus(405);
        response.setContentLength(0);
        response.flushBuffer();
    }

    /**
     *
     */
    private void setResponseHeaders(HttpServletResponse response) {
        QueryDictionary responseHeaderDictionary = responseHeaders.get();

        for (String key : responseHeaderDictionary) {
            for (int i = 0, n = responseHeaderDictionary.getLength(key); i < n; i++) {
                response.addHeader(key, responseHeaderDictionary.get(key, i));
            }
        }
    }
}
