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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.pivot.serialization.ByteArraySerializer;
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
 * Integration test of Client-side Authentication with the Basic method.
 * <br/>
 * This is a JUnit 4 Test, but should be excluded from usual (Unit) Test Suite.
 * <br/>
 * Before Run these tests with local instance of Tomcat, some steps are required:
 *   - configure role and users in Tomcat users file, under its conf folder,
 *     but note that for the digest authentication, passwords have to be encoded
 *     (see Tomcat docs for this) 
 *   - start the web application pivot-test, containing the 
 *     required resources (dir /public , dir /protected protected with 
 *     basic authentication) and files.
 * 
 * Then, before to run these tests, ensure that all has been successfully setup 
 * (the webapp has started), trying to ask the same URLs from a Web Browser.
 *
 * TODO:
 *   - test other HTTP methods ...
 *
 */
public class WebQueryTestClientBasicOnTomcat {
    final static String HOSTNAME = "localhost";
    final static String PATH = null;
    final static int PORT = 8080;
    final static boolean SECURE = false;

    final static String WEBAPP = "/pivot-test";

    final static String PATH_PUBLIC = WEBAPP + "/public/";
    final static String PATH_PROTECTED_BASIC = WEBAPP + "/protected/";

    final static String SAMPLE_FILE_BINARY = "test.jpg";
    final static String SAMPLE_FILE_TEXT = "test.txt";

    final static String USER_NAME = "test";
    final static String USER_PASSWORD = "test0";

    final static long TIMEOUT = 5000l; // default timeout for WebQuery tests
                                       // here: 5 sec

    String host = null;
    int port = 0;
    String path = null;

    Authentication authentication = null;

    Object result = null;

    public void log(String msg) {
        System.out.println(msg);
    }

    @BeforeClass
    public static void runBeforeClass() {
        // run for one time before all test cases
    }

    @AfterClass
    public static void runAfterClass() {
        // run for one time after all test cases
    }

    @Before
    public void runBeforeEveryTest() {
        // run before any single test case
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

    @Test(timeout = 10000, expected = QueryException.class)
    public void publicOnApache_noauth_NotExistingHost() throws QueryException {
        log("publicOnApache_noauth_NotExistingHost()");

        host = "non_existing_host";
        port = PORT;
        path = PATH_PUBLIC;

        GetQuery query = new GetQuery(host, port, path, SECURE);
        query.setTimeout(TIMEOUT);
        log("GET Query to " + query.getLocation());

        result = query.execute();
        assertNull(result);

        log("Query result: \n" + result);
    }

    @Test(timeout = 10000, expected = QueryException.class)
    public void publicOnApache_noauth_localhost_NotExistingResource() throws QueryException {
        log("publicOnApache_noauth_localhost_NotExistingResource()");

        host = HOSTNAME;
        port = PORT;
        path = PATH_PUBLIC + "non_existing_resource";

        GetQuery query = new GetQuery(host, port, path, SECURE);
        query.setTimeout(TIMEOUT);
        log("GET Query to " + query.getLocation());

        result = query.execute();
        assertNull(result);

        log("Query result: \n" + result);
    }

    @Test(timeout = 10000)
    public void publicOnApache_noauth_localhost_testFile() throws QueryException {
        log("publicOnApache_noauth_localhost_testFile()");

        host = HOSTNAME;
        port = PORT;
        path = PATH_PUBLIC + SAMPLE_FILE_TEXT;

        GetQuery query = new GetQuery(host, port, path, SECURE);

        // attention, don't use BinarySerializer here, but instead use the
        // generic ByteArraySerializer
        query.setSerializer(new ByteArraySerializer());
        query.setTimeout(TIMEOUT);
        log("GET Query to " + query.getLocation());

        result = query.execute();
        assertNotNull(result);

        // dump content, but useful only for text resources ...
        String dump = // result.toString()
        // Arrays.toString((byte []) result);
        new String((byte[]) result);
        log("Query result: " + (dump.getBytes().length) + " bytes \n" + dump);
    }

    @Test(timeout = 10000)
    public void publicOnApache_basic_localhost_forceUnnecessaryAuthentication()
        throws QueryException {
        log("publicOnApache_basic_localhost_forceUnnecessaryAuthentication()");

        host = HOSTNAME;
        port = PORT;
        path = PATH_PUBLIC + SAMPLE_FILE_TEXT;

        GetQuery query = new GetQuery(host, port, path, SECURE);

        // attention, don't use BinarySerializer here, but instead use the
        // generic ByteArraySerializer
        query.setSerializer(new ByteArraySerializer());
        query.setTimeout(TIMEOUT);
        log("GET Query to " + query.getLocation());

        authentication = new BasicAuthentication(USER_NAME, USER_PASSWORD);
        authentication.authenticate(query);

        result = query.execute();
        assertNotNull(result);

        // dump content, but useful only for text resources ...
        String dump = // result.toString()
        // Arrays.toString((byte []) result);
        new String((byte[]) result);
        log("Query result: " + (dump.getBytes().length) + " bytes \n" + dump);
    }

    // @Test(timeout = 10000, expected = QueryException.class)
    @Test(timeout = 1000000, expected = QueryException.class)
    // for debugging the execution
    public void protectedOnApache_basic_localhostWithoutAuthenticate() throws QueryException {
        log("protectedOnApache_basic_localhostWithoutAuthenticate()");

        host = HOSTNAME;
        port = PORT;
        path = PATH_PROTECTED_BASIC + SAMPLE_FILE_TEXT;

        GetQuery query = new GetQuery(host, port, path, SECURE);
        query.setSerializer(new ByteArraySerializer());
        query.setTimeout(TIMEOUT);
        log("GET Query to " + query.getLocation());

        result = query.execute();
        log("Query result: \n" + result);

        assertNull(result);
    }

    @Test(timeout = 10000, expected = QueryException.class)
    public void protectedOnApache_basic_localhostWithWrongCredentials() throws QueryException {
        log("protectedOnApache_basic_localhostWithWrongCredentials()");

        host = HOSTNAME;
        port = PORT;
        path = PATH_PROTECTED_BASIC + SAMPLE_FILE_TEXT;

        GetQuery query = new GetQuery(host, port, path, SECURE);
        query.setSerializer(new ByteArraySerializer());
        query.setTimeout(TIMEOUT);
        log("GET Query to " + query.getLocation());

        authentication = new BasicAuthentication("wrongUsername", "wrongPassword");
        authentication.authenticate(query);

        result = query.execute();
        log("Query result: \n" + result);

        assertNull(result);
    }

    @Test(timeout = 10000)
    public void protectedOnApache_basic_localhost() throws QueryException {
        log("protectedOnApache_basic_localhost()");

        host = HOSTNAME;
        port = PORT;
        // path = PATH_PROTECTED_BASIC + SAMPLE_FILE_TEXT;
        // path = PATH_PROTECTED_BASIC + SAMPLE_FILE_BINARY;
        path = PATH_PROTECTED_BASIC + SAMPLE_FILE_TEXT;

        GetQuery query = new GetQuery(host, port, path, SECURE);

        // attention, don't use BinarySerializer here, but instead use the
        // generic ByteArraySerializer
        query.setSerializer(new ByteArraySerializer());
        query.setTimeout(TIMEOUT);
        log("GET Query to " + query.getLocation());

        authentication = new BasicAuthentication(USER_NAME, USER_PASSWORD);
        authentication.authenticate(query);

        result = query.execute();
        assertNotNull(result);

        // int status = query.getStatus(); // method missing at the moment ...
        // log("Query: status = " + status + ", result: \n" + result);
        // assertEquals(401, status);

        // dump content, but useful only for text resources ...
        String dump = // result.toString()
        // Arrays.toString((byte []) result);
        new String((byte[]) result);
        log("Query result: " + (dump.getBytes().length) + " bytes \n" + dump);
    }

}
