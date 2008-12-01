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
package pivot.web.test.server;

import java.io.IOException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pivot.collections.HashMap;
import pivot.serialization.BinarySerializer;
import pivot.serialization.JSONSerializer;
import pivot.serialization.SerializationException;
import pivot.serialization.Serializer;
import pivot.util.Base64;

public class WebQueryTestServlet extends HttpServlet {
    public static final long serialVersionUID = 0;

    private String username = null;
    private static final String BASIC_AUTHENTICATION_TAG = "Basic";

    @Override
    public void init(ServletConfig config) {
    }

    @Override
    public void destroy() {
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        String authorization = request.getHeader("Authorization");

        if (authorization == null) {
            response.setStatus(401);
            response.setHeader("WWW-Authenticate", "BASIC realm=\""
                + request.getServletPath() +"\"");
            response.setContentLength(0);
            response.flushBuffer();
        } else {
            String encodedCredentials = authorization.substring(BASIC_AUTHENTICATION_TAG.length() + 1);

            String decodedCredentials = new String(Base64.decode(encodedCredentials));
            String[] credentials = decodedCredentials.split(":");
            username = credentials[0];

            // TODO Parse query string

            super.service(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        String pathInfo = request.getPathInfo();
        String queryString = request.getQueryString();

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("username", username);
        map.put("pathInfo", pathInfo);
        map.put("queryString", queryString);

        response.setStatus(200);

        Serializer serializer = new BinarySerializer();
        response.setContentType(serializer.getMIMEType(map));

        try {
            serializer.writeObject(map, response.getOutputStream());
        } catch(SerializationException exception) {
            throw new ServletException(exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        ServletInputStream inputStream = request.getInputStream();
        JSONSerializer jsonSerializer = new JSONSerializer();

        try {
            Object value = jsonSerializer.readObject(inputStream);
            jsonSerializer.writeObject(value, System.out);
            response.setStatus(201);
            response.setHeader("Location", request.getPathInfo() +"#101");
        } catch(SerializationException exception) {
            throw new ServletException(exception);
        }

        response.setContentLength(0);
        response.flushBuffer();
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        ServletInputStream inputStream = request.getInputStream();
        JSONSerializer jsonSerializer = new JSONSerializer();

        try {
            Object value = jsonSerializer.readObject(inputStream);
            jsonSerializer.writeObject(value, System.out);
            response.setStatus(200);
        } catch(SerializationException exception) {
            throw new ServletException(exception);
        }

        response.setContentLength(0);
        response.flushBuffer();
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        response.setStatus(200);
        response.setContentLength(0);
        response.flushBuffer();
    }
}
