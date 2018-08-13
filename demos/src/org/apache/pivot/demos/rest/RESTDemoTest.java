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
package org.apache.pivot.demos.rest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;

import org.apache.pivot.json.JSON;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.web.DeleteQuery;
import org.apache.pivot.web.GetQuery;
import org.apache.pivot.web.PostQuery;
import org.apache.pivot.web.PutQuery;
import org.apache.pivot.web.Query;
import org.apache.pivot.web.QueryException;
import org.junit.BeforeClass;
import org.junit.Test;

public class RESTDemoTest {
    private static String hostname = null;
    private static int port = -1;
    private static boolean secure = false;

    @BeforeClass
    public static void oneTimeSetUp() {
        hostname = System.getProperty("org.apache.pivot.demos.rest.hostname", "localhost");
        port = Integer.parseInt(System.getProperty("org.apache.pivot.demos.rest.port", "-1"));
        secure = Boolean.parseBoolean(System.getProperty("org.apache.pivot.demos.rest.secure",
            "false"));
    }

    @Test
    public void testCRUD() throws IOException, SerializationException, QueryException {
        JSONSerializer jsonSerializer = new JSONSerializer();
        Object contact = jsonSerializer.readObject(getClass().getResourceAsStream("contact.json"));

        // Create
        PostQuery postQuery = new PostQuery(hostname, port, "/pivot-demos/rest_demo", secure);
        postQuery.setValue(contact);
        URL location = postQuery.execute();

        assertNotNull(location);

        String path = location.getPath();

        // Read
        GetQuery getQuery = new GetQuery(hostname, port, path, secure);

        Object result = getQuery.execute();
        assertArrayEquals((Object[]) JSON.get(contact, "address.street"),
            (Object[]) JSON.get(result, "address.street"));
        assertEquals(contact, result);

        // Update
        JSON.put(contact, "name", "Joseph User");
        PutQuery putQuery = new PutQuery(hostname, port, path, secure);
        putQuery.setValue(contact);
        boolean created = putQuery.execute();

        assertFalse(created);
        assertEquals(contact, getQuery.execute());

        // Delete
        DeleteQuery deleteQuery = new DeleteQuery(hostname, port, path, secure);
        deleteQuery.execute();

        assertEquals(deleteQuery.getStatus(), Query.Status.NO_CONTENT);
    }

    @Test(expected = QueryException.class)
    public void testException() throws QueryException {
        GetQuery getQuery = new GetQuery(hostname, port, "/pivot-demos/rest_demo/foo", secure);
        getQuery.execute();
    }
}
