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
package org.apache.pivot.web.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.apache.pivot.serialization.ByteArraySerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.web.Authentication;
import org.apache.pivot.web.BasicAuthentication;
import org.apache.pivot.web.GetQuery;
import org.apache.pivot.web.QueryException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Integration test of Client-side Authentication with the Basic method. <br/>
 * This is a JUnit 4 Test, but should be excluded from usual (Unit) Test Suite.
 * <br/>
 * To Run these tests, a local instance of Apache must be started and
 * configured with the required resources (by default folders /public , and /protected
 * protected with basic authentication) and files.
 * Then, before to run these tests, ensure basic authentication has been successfully setup
 * asking the same URLs from a Web Browser. <br/>
 * Note that now this class loads (in a standard Pivot way) some test parameters
 * from a json file that must be in the same folder of this class.
 * For example, to run this Class on another Server (for example on a local Tomcat instance),
 * some parameters have to be changed  inside the json file, then rerun the test.
 * <br/>
 * Note that at the end of some tests, the downloaded content is written (dump) to the console,
 * but it's useful only for text resources, so don't do it for binary resources.
 * Here a sample (including commented code for other ways to do this):
 * <pre>
        String dump = // result.toString()
            // Arrays.toString((byte []) result);
            new String((byte[]) result);
 * <pre/>
 *
 * TODO:
 *   - test other HTTP methods ...
 *
 */
public class WebQueryTestClientBasic {
    static Resources resources = null;  // parametric resources, using the Pivot-way

    String host = null;
    int port = 0;
    String path = null;
    boolean secure = false;

    Authentication authentication = null;

    Object result = null;

    public static final void log(String msg) {
        System.out.println(msg);
    }

    @BeforeClass
    public static void runBeforeClass() {
        // run for one time before all test cases

        // load Test Case parametric values
        try {
            resources = new Resources(WebQueryTestClientBasic.class.getName());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SerializationException e) {
            e.printStackTrace();
        }

        log("Loaded " + resources + " resources");
    }

    @AfterClass
    public static void runAfterClass() {
        // run for one time after all test cases
    }

    @Before
    public void runBeforeEveryTest() {
        // run before any single test case
        host = resources.getString("hostname");
        port = resources.getInteger("port");
    }

    @After
    public void runAfterEveryTest() {
        // run after any single test case
        host = null;
        port = 0;
        path = null;

        authentication = null;

        result = null;
    }

    protected GetQuery createGetQuery(final String host, final int port, final String path, final boolean secure) {
        GetQuery query = new GetQuery(host, port, path, secure);
        query.setTimeout(resources.getLong("timeout"));
        log("GET Query to " + query.getLocation());

        // to download generic content don't use BinarySerializer,
        // but instead use the ByteArraySerializer
        query.setSerializer(new ByteArraySerializer());

        return query;
    }

    protected void dumpResource(Object result) {
        String dump = new String((byte[]) result);
        log("Query result: " + (dump.getBytes().length) + " bytes \n" + dump);
    }

    @Test(timeout = 10000, expected = QueryException.class)
    public void public_noauth_NotExistingHost() throws QueryException {
        log("public_noauth_NotExistingHost()");

        host = "non_existing_host";
        path = resources.getString("folder_public");

        GetQuery query = createGetQuery(host, port, path, secure);
        result = query.execute();
        assertNull(result);

        log("Query result: \n" + result);
    }

    @Test(timeout = 10000, expected = QueryException.class)
    public void public_noauth_localhost_NotExistingResource() throws QueryException {
        log("public_noauth_localhost_NotExistingResource()");

        path = resources.getString("folder_public") + "non_existing_resource";

        GetQuery query = createGetQuery(host, port, path, secure);
        result = query.execute();
        assertNull(result);

        log("Query result: \n" + result);
    }

    @Test(timeout = 10000)
    public void public_noauth_localhost_testFile() throws QueryException {
        log("public_noauth_localhost_testFile()");

        path = resources.getString("folder_public") + resources.getString("file_text");

        GetQuery query = createGetQuery(host, port, path, secure);
        result = query.execute();
        assertNotNull(result);

        dumpResource(result);
    }

    @Test(timeout = 10000)
    public void public_basic_localhost_forceUnnecessaryAuthentication()
        throws QueryException {
        log("public_basic_localhost_forceUnnecessaryAuthentication()");

        path = resources.getString("folder_public") + resources.getString("file_text");

        GetQuery query = createGetQuery(host, port, path, secure);

        authentication = new BasicAuthentication(resources.getString("user_name"),
            resources.getString("user_pass"));
        authentication.authenticate(query);

        result = query.execute();
        assertNotNull(result);

        dumpResource(result);
    }

    // @Test(timeout = 10000, expected = QueryException.class)
    @Test(timeout = 1000000, expected = QueryException.class)  // for debugging the execution
    public void protected_basic_localhostWithoutAuthenticate() throws QueryException {
        log("protected_basic_localhostWithoutAuthenticate()");

        path = resources.getString("folder_protected") + resources.getString("file_text");

        GetQuery query = createGetQuery(host, port, path, secure);
        result = query.execute();
        log("Query result: \n" + result);
        assertNull(result);

        int status = query.getStatus();
        log("Query: status = " + status + ", result: \n" + result);
        assertEquals(401, status);
    }

    @Test(timeout = 10000, expected = QueryException.class)
    public void protected_basic_localhostWithWrongCredentials() throws QueryException {
        log("protected_basic_localhostWithWrongCredentials()");

        path = resources.getString("folder_protected") + resources.getString("file_text");

        GetQuery query = createGetQuery(host, port, path, secure);

        authentication = new BasicAuthentication("wrongUsername", "wrongPassword");
        authentication.authenticate(query);

        result = query.execute();
        log("Query result: \n" + result);
        assertNull(result);

        int status = query.getStatus();
        log("Query: status = " + status + ", result: \n" + result);
        assertEquals(401, status);
    }

    @Test(timeout = 10000)
    public void protected_basic_localhost() throws QueryException {
        log("protected_basic_localhost()");

        path = resources.getString("folder_protected") + resources.getString("file_text");
        // path = resources.getString("folder_protected") + resources.getString("file_binary");

        GetQuery query = createGetQuery(host, port, path, secure);

        authentication = new BasicAuthentication(resources.getString("user_name"),
            resources.getString("user_pass"));
        authentication.authenticate(query);

        result = query.execute();
        assertNotNull(result);

        int status = query.getStatus();
        log("Query: status = " + status + ", result: \n" + result);
        assertEquals(200, status);

        dumpResource(result);
    }

}
