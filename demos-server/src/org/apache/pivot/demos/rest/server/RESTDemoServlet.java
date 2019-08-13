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
package org.apache.pivot.demos.rest.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;
import org.apache.pivot.web.Query;
import org.apache.pivot.web.Query.Method;
import org.apache.pivot.web.QueryException;
import org.apache.pivot.web.server.QueryServlet;

public class RESTDemoServlet extends QueryServlet {
    private static final long serialVersionUID = 0;

    @SuppressWarnings("resource")
    @Override
    protected Object doGet(Path path) throws QueryException {
        if (path.getLength() != 1) {
            throw new QueryException(Query.Status.BAD_REQUEST);
        }

        // Read the value from the temp file
        File directory = new File(System.getProperty("java.io.tmpdir"));
        File file = new File(directory, path.get(0));
        if (!file.exists()) {
            throw new QueryException(Query.Status.NOT_FOUND);
        }

        Object value;
        try {
            JSONSerializer jsonSerializer = new JSONSerializer();
            value = jsonSerializer.readObject(new FileInputStream(file));
        } catch (IOException | SerializationException exception) {
            throw new QueryException(Query.Status.INTERNAL_SERVER_ERROR);
        }

        return value;
    }

    @SuppressWarnings("resource")
    @Override
    protected URL doPost(Path path, Object value) throws QueryException {
        if (path.getLength() > 0 || value == null) {
            throw new QueryException(Query.Status.BAD_REQUEST);
        }

        // Write the value to a temp file
        File directory = new File(System.getProperty("java.io.tmpdir"));
        File file;
        try {
            file = File.createTempFile(getClass().getName(), null, directory);

            JSONSerializer jsonSerializer = new JSONSerializer();
            jsonSerializer.writeObject(value, new FileOutputStream(file));
        } catch (IOException | SerializationException exception) {
            throw new QueryException(Query.Status.INTERNAL_SERVER_ERROR);
        }

        // Return the location of the resource
        URL location;
        try {
            location = new URL(getLocation(), file.getName());
        } catch (MalformedURLException exception) {
            throw new QueryException(Query.Status.INTERNAL_SERVER_ERROR);
        }

        return location;
    }

    @SuppressWarnings("resource")
    @Override
    protected boolean doPut(Path path, Object value) throws QueryException {
        if (path.getLength() != 1 || value == null) {
            throw new QueryException(Query.Status.BAD_REQUEST);
        }

        // Write the value to the temp file
        File directory = new File(System.getProperty("java.io.tmpdir"));
        File file = new File(directory, path.get(0));
        if (!file.exists()) {
            throw new QueryException(Query.Status.NOT_FOUND);
        }

        try {
            JSONSerializer jsonSerializer = new JSONSerializer();
            jsonSerializer.writeObject(value, new FileOutputStream(file));
        } catch (IOException | SerializationException exception) {
            throw new QueryException(Query.Status.INTERNAL_SERVER_ERROR);
        }

        return false;
    }

    @Override
    protected void doDelete(Path path) throws QueryException {
        if (path.getLength() != 1) {
            throw new QueryException(Query.Status.BAD_REQUEST);
        }

        // Delete the file
        File directory = new File(System.getProperty("java.io.tmpdir"));
        File file = new File(directory, path.get(0));
        if (!file.exists()) {
            throw new QueryException(Query.Status.NOT_FOUND);
        }

        boolean deleted = file.delete();
        if (!deleted) {
            throw new QueryException(Query.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected Serializer<?> createSerializer(Method method, Path path) throws QueryException {
        return new JSONSerializer();
    }
}
