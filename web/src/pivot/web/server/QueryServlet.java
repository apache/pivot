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
package pivot.web.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Iterator;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.serialization.JSONSerializer;
import pivot.serialization.SerializationException;
import pivot.serialization.Serializer;
import pivot.util.Base64;

/**
 * Abstract base class for web query servlets. It is the server counterpart to
 * {@link pivot.web.Query pivot.web.Query}.
 *
 * @author tvolkert
 */
public abstract class QueryServlet extends HttpServlet {
    static final long serialVersionUID = -646654620936816287L;

    /**
     * The supported HTTP methods.
     *
     * @author tvolkert
     */
    protected enum Method {
        GET,
        POST,
        PUT,
        DELETE;

        public static Method decode(String value) {
            return valueOf(value.toUpperCase());
        }
    }

    /**
     * User credentials, which will be made availale if the servlet's
     * <tt>authenticationRequired</tt> flag is set to <tt>true</tt>.
     *
     * @author tvolkert
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

    /**
     * Arguments dictionary implementation.
     *
     * @author tvolkert
     */
    public final class ArgumentsDictionary
        implements Dictionary<String, String>, Iterable<String> {
        public String get(String key) {
            return arguments.get(key);
        }

        public String put(String key, String value) {
            throw new UnsupportedOperationException();
        }

        public String remove(String key) {
            throw new UnsupportedOperationException();
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
     *
     * @author tvolkert
     */
    public final class RequestPropertiesDictionary
        implements Dictionary<String, String>, Iterable<String> {
        public String get(String key) {
            return requestProperties.get(key);
        }

        public String put(String key, String value) {
            throw new UnsupportedOperationException();
        }

        public String remove(String key) {
            throw new UnsupportedOperationException();
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
     *
     * @author tvolkert
     */
    public final class ResponsePropertiesDictionary
        implements Dictionary<String, String>, Iterable<String> {
        public String get(String key) {
            return responseProperties.get(key);
        }

        public String put(String key, String value) {
            return responseProperties.put(key, value);
        }

        public String remove(String key) {
            return responseProperties.remove(key);
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

    private boolean authenticationRequired = false;
    private Credentials credentials = null;

    private String hostname;
    private String contextPath;
    private String queryPath;
    private int port;
    private boolean secure;
    private Method method;

    private boolean determineContentLength = false;

    private HashMap<String, String> arguments = new HashMap<String, String>();
    private HashMap<String, String> requestProperties = new HashMap<String, String>();
    private HashMap<String, String> responseProperties = new HashMap<String, String>();

    private ArgumentsDictionary argumentsDictionary = new ArgumentsDictionary();
    private RequestPropertiesDictionary requestPropertiesDictionary = new RequestPropertiesDictionary();
    private ResponsePropertiesDictionary responsePropertiesDictionary = new ResponsePropertiesDictionary();

    private Serializer serializer = new JSONSerializer();

    private static final String BASIC_AUTHENTICATION_TAG = "Basic";
    private static final String HTTP_PROTOCOL = "http";
    private static final String HTTPS_PROTOCOL = "https";
    private static final String URL_ENCODING = "UTF-8";

    /**
     * Gets the host name that was requested.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Returns the portion of the request URI that indicates the context of the
     * request. The context path always comes first in a request URI. The path
     * starts with a "/" character but does not end with a "/" character. For
     * servlets in the default (root) context, this method returns "".
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Returns the portion of the request URI that occurs after the context
     * path but preceding the query string. It will start with a "/" character.
     * For servlets in the default (root) context, this method returns the full
     * path.
     */
    public String getQueryPath() {
        return queryPath;
    }

    /**
     * Returns the Internet Protocol (IP) port number of the interface on which
     * the request was received.
     */
    public int getPort() {
        return port;
    }

    /**
     * Tells whether the request has been ecrypted over HTTPS.
     */
    public boolean isSecure() {
        return secure;
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
        return method;
    }

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
            credentials = null;
        }
    }

    /**
     * Gets the authentication credentials that were extracted from the
     * request. These are only available if the <tt>authenticationRequired</tt>
     * flag is set to <tt>true</tt>.
     */
    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * Returns the servlet's arguments dictionary, which holds the values
     * passed in the HTTP request query string.
     */
    public ArgumentsDictionary getArguments() {
        return argumentsDictionary;
    }

    /**
     * Returns the servlet's request property dictionary, which holds the HTTP
     * request headers.
     */
    public RequestPropertiesDictionary getRequestProperties() {
        return requestPropertiesDictionary;
    }

    /**
     * Returns the servlet's response property dictionary, which holds the HTTP
     * response headers that will be sent back to the client.
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
     *
     */
    protected Object doGet() throws ServletException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    protected URL doPost(Object value) throws ServletException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    protected void doPut(Object value) throws ServletException {
        throw new UnsupportedOperationException();
    }

    /**
     *
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
     * subclasses that wish to authenticate the request but not authorization
     * it may simply not override this method.
     * <p>
     * This method is guaranteed to be called after the arguments and request
     * properties have been made available.
     *
     * @throws LoginException
     * Thrown if the request is not authorized
     */
    protected void authorize() throws LoginException {
        // No-op
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void service(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
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

                if (credentials == null) {
                    credentials = new Credentials(username, password);
                } else {
                    credentials.username = username;
                    credentials.password = password;
                }
            }
        }

        if (proceed) {
            // Extract our location context
            try {
                URL url = new URL(request.getRequestURL().toString());

                hostname = url.getHost();
                contextPath = request.getContextPath();
                queryPath = request.getRequestURI();
                port = request.getLocalPort();
                secure = url.getProtocol().equalsIgnoreCase(HTTPS_PROTOCOL);
                method = Method.decode(request.getMethod());

                if (queryPath.startsWith(contextPath)) {
                    queryPath = queryPath.substring(contextPath.length());
                }
            } catch (MalformedURLException ex) {
                throw new ServletException(ex);
            }

            // Clear out any remnants of the previous service
            arguments.clear();
            requestProperties.clear();
            responseProperties.clear();

            // Copy the query string into our arguments dictionary
            String queryString = request.getQueryString();
            if (queryString != null) {
                String[] pairs = queryString.split("&");

                for (int i = 0, n = pairs.length; i < n; i++) {
                    String[] pair = pairs[i].split("=");

                    String key = URLDecoder.decode(pair[0], URL_ENCODING);
                    String value = URLDecoder.decode((pair.length > 1) ? pair[1] : "", URL_ENCODING);

                    arguments.put(key, value);
                }
            }

            // Copy the request headers into our request properties dictionary
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);

                requestProperties.put(headerName, headerValue);
            }

            if (authenticationRequired) {
                try {
                    authorize();
                } catch (LoginException ex) {
                    proceed = false;
                    doUnauthorized(request, response);
                }
            }
        }

        if (proceed) {
            super.service(request, response);
        }
    }

    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        try {
            Object result = doGet();

            response.setStatus(200);
            setResponseHeaders(response);
            response.setContentType(serializer.getMIMEType());

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
        } catch (UnsupportedOperationException ex) {
            doMethodNotAllowed(response);
        } catch (SerializationException ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected final void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        try {
            Object value = serializer.readObject(request.getInputStream());

            URL url = doPost(value);

            response.setStatus(201);
            setResponseHeaders(response);
            response.setHeader("Location", url.toString());
            response.setContentLength(0);
            response.flushBuffer();
        } catch (UnsupportedOperationException ex) {
            doMethodNotAllowed(response);
        } catch (SerializationException ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected final void doPut(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        try {
            Object value = serializer.readObject(request.getInputStream());

            doPut(value);

            response.setStatus(204);
            setResponseHeaders(response);
            response.setContentLength(0);
            response.flushBuffer();
        } catch (UnsupportedOperationException ex) {
            doMethodNotAllowed(response);
        } catch (SerializationException ex) {
            throw new ServletException(ex);
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
        } catch (UnsupportedOperationException ex) {
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
    private void doMethodNotAllowed(HttpServletResponse response) throws IOException {
        response.setStatus(405);
        response.setContentLength(0);
        response.flushBuffer();
    }

    /**
     *
     */
    private void setResponseHeaders(HttpServletResponse response) {
        for (String key : responseProperties) {
            response.setHeader(key, responseProperties.get(key));
        }
    }
}
