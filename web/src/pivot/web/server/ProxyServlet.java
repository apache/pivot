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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ProxyServlet extends HttpServlet {
    private String hostname = null;
    private int port = -1;
    private String path = null;

    public static final long serialVersionUID = 0;

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";

    public static final String HOSTNAME_PARAM = "hostname";
    public static final String PORT_PARAM = "port";
    public static final String PATH_PARAM = "path";

    public static final int BUFFER_SIZE = 1024;

    @Override
    public void init(ServletConfig config)
        throws ServletException {
        super.init();

        hostname = config.getInitParameter(HOSTNAME_PARAM);
        if (hostname == null) {
            throw new ServletException("Hostname is required.");
        }

        String portHeader = config.getInitParameter(PORT_PARAM);
        port = (portHeader == null) ? -1 : Integer.parseInt(portHeader);

        path = config.getInitParameter(PATH_PARAM);
        if (path == null) {
            throw new ServletException("Path is required.");
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void service(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        // Construct the URL
        String path = this.path;

        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            path += "/" + pathInfo;
        }

        String queryString = request.getQueryString();
        if (queryString != null) {
            path += "?" + queryString;
        }

        URL url = null;
        try {
            url = new URL(request.getScheme(), hostname, port, path);
        } catch(MalformedURLException exception) {
            throw new ServletException("Unable to construct URL.", exception);
        }

        String method = request.getMethod();

        // Open a connection to the URL
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod(method);
        connection.setAllowUserInteraction(false);
        connection.setInstanceFollowRedirects(false);
        connection.setUseCaches(false);

        // Write request headers to connection
        System.out.println("[Request Headers]");
        Enumeration<String> headerNames = (Enumeration<String>)request.getHeaderNames();

        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();

                Enumeration<String> headerValues =
                    (Enumeration<String>)request.getHeaders(headerName);

                while (headerValues.hasMoreElements()) {
                    String headerValue = headerValues.nextElement();
                    System.out.println(headerName + ": " + headerValue);

                    if (connection.getRequestProperty(headerName) == null) {
                        connection.setRequestProperty(headerName, headerValue);
                    } else {
                        connection.addRequestProperty(headerName, headerValue);
                    }
                }
            }
        }

        // Set the input/output state
        connection.setDoInput(true);
        connection.setDoOutput(method.equalsIgnoreCase(METHOD_POST)
            || method.equalsIgnoreCase(METHOD_PUT));

        // Connect to the server
        connection.connect();

        // Write the request body
        if (connection.getDoOutput()) {
            OutputStream outputStream = null;

            try {
                InputStream inputStream = request.getInputStream();

                outputStream = connection.getOutputStream();
                outputStream = new BufferedOutputStream(outputStream, BUFFER_SIZE);
                for (int data = inputStream.read(); data != -1; data = inputStream.read()) {
                    outputStream.write((byte)data);
                }
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        }

        // Set the response status
        int status = connection.getResponseCode();
        System.out.println("[Status] " + status);

        int statusPrefix = status / 100;

        if (statusPrefix == 1
            || statusPrefix == 3) {
            throw new ServletException("Unexpected server response: " + status);
        }

        response.setStatus(status);

        // Write response headers
        // NOTE Header indexes start at 1, not 0
        System.out.println("[Response Headers]");

        int i = 1;
        for (String key = connection.getHeaderFieldKey(i);
            key != null;
            key = connection.getHeaderFieldKey(++i)) {
            if (key != null) {
                String value = connection.getHeaderField(i);
                System.out.println(key + ": " + value);

                if (response.containsHeader(key)) {
                    response.addHeader(key, value);
                } else {
                    response.setHeader(key, value);
                }
            }
        }

        // Read the response body
        if (method.equalsIgnoreCase(METHOD_GET)) {
            InputStream inputStream = null;

            try {
                try {
                    // Response returned on input stream
                    inputStream = connection.getInputStream();
                } catch(Exception exception) {
                    // Response returned on error stream
                    inputStream = connection.getErrorStream();
                }

                inputStream = new BufferedInputStream(inputStream, BUFFER_SIZE);

                OutputStream outputStream = response.getOutputStream();
                for (int data = inputStream.read(); data != -1; data = inputStream.read()) {
                    outputStream.write((byte)data);
                }

                response.flushBuffer();
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
    }
}
