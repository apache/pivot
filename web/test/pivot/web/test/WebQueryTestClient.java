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
package pivot.web.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pivot.collections.Dictionary;
import pivot.serialization.BinarySerializer;
import pivot.serialization.JSONSerializer;
import pivot.util.concurrent.Task;
import pivot.util.concurrent.TaskGroup;
import pivot.util.concurrent.TaskListener;
import pivot.web.BasicAuthentication;
import pivot.web.GetQuery;
import pivot.web.DeleteQuery;
import pivot.web.PostQuery;
import pivot.web.PutQuery;
import pivot.web.QueryDictionary;

public class WebQueryTestClient {
    final static boolean useProxy = true;

    final static String HOSTNAME = "localhost";
    final static String PATH = (useProxy ? "/pivot_web_test/proxy" : "/pivot_web_test/webquery") + "/bar/quux";
    final static int PORT = 8080;
    final static boolean SECURE = false;

    @Test
    public void basicTest() {
        final BasicAuthentication authentication = new BasicAuthentication("foo", "bar");

        TaskGroup queryGroup = new TaskGroup();

        // GET
        final GetQuery getQuery = new GetQuery(HOSTNAME, PORT, PATH, SECURE);
        getQuery.getParameters().put("a", "b");
        getQuery.setSerializer(new BinarySerializer());
        getQuery.getRequestHeaders().add("bar", "hello");
        getQuery.getRequestHeaders().add("bar", "world");
        authentication.authenticate(getQuery);
        queryGroup.add(getQuery);

        // POST
        final PostQuery postQuery = new PostQuery(HOSTNAME, PORT, PATH, SECURE);
        authentication.authenticate(postQuery);
        postQuery.setValue(JSONSerializer.parseList("[1, 2, 3]"));
        queryGroup.add(postQuery);

        // PUT
        final PutQuery putQuery = new PutQuery(HOSTNAME, PORT, PATH, SECURE);
        authentication.authenticate(putQuery);
        putQuery.setValue(JSONSerializer.parseMap("{a:100, b:200, c:300}"));
        queryGroup.add(putQuery);

        // POST
        final DeleteQuery deleteQuery = new DeleteQuery(HOSTNAME, PORT, PATH, SECURE);
        authentication.authenticate(deleteQuery);
        queryGroup.add(deleteQuery);

        queryGroup.execute(new TaskListener<Void>() {
            @SuppressWarnings("unchecked")
            public synchronized void taskExecuted(Task<Void> task) {
                Dictionary<String, Object> result = (Dictionary<String, Object>)getQuery.getResult();

                System.out.println("GET result: "
                    + "username: " + result.get("username") + ", "
                    + "pathInfo: " + result.get("pathInfo") + ", "
                    + "queryString: " + result.get("queryString") + ", "
                    + "status: " + getQuery.getStatus());

                QueryDictionary responseHeaders = getQuery.getResponseHeaders();
                for (String headerName : responseHeaders) {
                    System.out.print(headerName + "=");

                    for (int i = 0, n = responseHeaders.getLength(headerName); i < n; i++) {
                        System.out.print(responseHeaders.get(headerName, i) + ";");
                    }

                    System.out.print("\n");
                }
                System.out.println("GET fault: " + getQuery.getFault());

                System.out.println("POST result: " + task.getResult());
                System.out.println("POST fault: " + postQuery.getFault());

                System.out.println("PUT fault: " + putQuery.getFault());
                System.out.println("DELETE fault: " + deleteQuery.getFault());
            }

            public synchronized void executeFailed(Task<Void> task) {
                // No-op; task groups don't fail
            }
        });

        synchronized(queryGroup) {
            if (queryGroup.isPending()) {
                try {
                    queryGroup.wait(10000);
                } catch(InterruptedException exception) {
                    throw new RuntimeException(exception);
                }
            }
        }

        assertTrue(getQuery.getFault() == null
            && postQuery.getFault() == null
            && putQuery.getFault() == null
            && deleteQuery.getFault() == null);
    }
}
