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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.serialization.JSONSerializer;
import pivot.serialization.Serializer;
import pivot.util.concurrent.Dispatcher;
import pivot.util.concurrent.Task;

public abstract class Query<V> extends Task<V> {
    protected enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }

    public final class ArgumentsDictionary implements Dictionary<String, String> {
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
    }

    public final class RequestPropertiesDictionary implements Dictionary<String, String> {
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
    }

    public final class ResponsePropertiesDictionary implements Dictionary<String, String> {
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
    }

    private URL locationContext = null;

    private HashMap<String, String> arguments = new HashMap<String, String>();
    private HashMap<String, String> requestProperties = new HashMap<String, String>();
    private HashMap<String, String> responseProperties = new HashMap<String, String>();

    private ArgumentsDictionary argumentsDictionary = new ArgumentsDictionary();
    private RequestPropertiesDictionary requestPropertiesDictionary = new RequestPropertiesDictionary();
    private ResponsePropertiesDictionary responsePropertiesDictionary = new ResponsePropertiesDictionary();

    private Serializer serializer = new JSONSerializer();

    private static Dispatcher DEFAULT_DISPATCHER = new Dispatcher();

    public static final int DEFAULT_PORT = -1;

    private static final String HTTP_PROTOCOL = "http";
    private static final String HTTPS_PROTOCOL = "https";
    private static final String URL_ENCODING = "UTF-8";

    public Query(String hostname, int port, String path, boolean secure) {
        super(DEFAULT_DISPATCHER);

        try {
            locationContext = new URL(secure ? HTTPS_PROTOCOL : HTTP_PROTOCOL,
                hostname, port, path);
        } catch(MalformedURLException exception) {
            throw new IllegalArgumentException("Unable to construct path URL.",
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
            } catch(UnsupportedEncodingException exception) {
                throw new IllegalStateException("Unable to construct query URL.", exception);
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
        } catch(MalformedURLException exception) {
        }

        assert (location != null) : "Error constructing location.";

        return location;
    }

    public ArgumentsDictionary getArguments() {
        return argumentsDictionary;
    }

    public RequestPropertiesDictionary getRequestProperties() {
        return requestPropertiesDictionary;
    }

    public ResponsePropertiesDictionary getResponseProperties() {
        return responsePropertiesDictionary;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public void setSerializer(Serializer serializer) {
        if (serializer == null) {
            throw new IllegalArgumentException("serializer is null.");
        }

        this.serializer = serializer;
    }

    protected Object execute(Method method, Object value)
        throws QueryException {
        URL location = getLocation();
        HttpURLConnection connection = null;

        int status = -1;
        String message = null;

        try {
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

            // Write the request body
            if (method == Method.POST || method == Method.PUT) {
                OutputStream outputStream = null;
                try {
                    outputStream = connection.getOutputStream();
                    serializer.writeObject(value, outputStream);
                } finally {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            }

            // Set the response info
            status = connection.getResponseCode();
            message = connection.getResponseMessage();

            responseProperties.clear();

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
                    value = serializer.readObject(inputStream);
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }
        } catch(Exception exception) {
            throw new QueryException(exception);
        }

        // If the response was anything other than 2xx, throw an exception
        if (status / 100 != 2) {
            throw new QueryException(status, message);
        }

        return value;
    }
}
