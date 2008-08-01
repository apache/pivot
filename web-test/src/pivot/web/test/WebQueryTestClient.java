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
package pivot.web.test;

import java.net.URL;

import pivot.serialization.BinarySerializer;
import pivot.serialization.JSONSerializer;
import pivot.util.concurrent.Task;
import pivot.util.concurrent.TaskListener;
import pivot.web.BasicAuthentication;
import pivot.web.GetQuery;
import pivot.web.DeleteQuery;
import pivot.web.PostQuery;
import pivot.web.PutQuery;

public class WebQueryTestClient {
    public static void main(String[] args) {
        final boolean useProxy = true;

        final String HOSTNAME = "localhost";
        final String PATH = useProxy ? "/pivot_web_test/proxy" : "/pivot_web_test/webquery/bar";
        final int PORT = 8080;
        final boolean SECURE = false;

        BasicAuthentication authentication = new BasicAuthentication("foo", "bar");

        // GET
        GetQuery getQuery = new GetQuery(HOSTNAME, PORT, PATH, SECURE);
        getQuery.getArguments().put("a", "b");
        getQuery.setSerializer(new BinarySerializer());
        authentication.authenticate(getQuery);

        getQuery.execute(new TaskListener<Object>() {
            public void taskExecuted(Task<Object> task) {
                System.out.println("GET result: " + task.getResult());
            }

            public void executeFailed(Task<Object> task) {
                System.out.println("GET fault: " + task.getFault());
            }
        });

        // POST
        PostQuery postQuery = new PostQuery(HOSTNAME, PORT, PATH, SECURE);
        authentication.authenticate(postQuery);
        postQuery.setValue(JSONSerializer.parseList("[1, 2, 3]"));

        postQuery.execute(new TaskListener<URL>() {
            public void taskExecuted(Task<URL> task) {
                System.out.println("POST result: " + task.getResult());
            }

            public void executeFailed(Task<URL> task) {
                System.out.println("POST fault: " + task.getFault());
            }
        });

        // PUT
        PutQuery putQuery = new PutQuery(HOSTNAME, PORT, PATH, SECURE);
        authentication.authenticate(putQuery);
        putQuery.setValue(JSONSerializer.parseMap("{a:100, b:200, c:300}"));

        putQuery.execute(new TaskListener<Void>() {
            public void taskExecuted(Task<Void> task) {
                System.out.println("PUT result");
            }

            public void executeFailed(Task<Void> task) {
                System.out.println("PUT fault: " + task.getFault());
            }
        });

        // POST
        DeleteQuery deleteQuery = new DeleteQuery(HOSTNAME, PORT, PATH, SECURE);
        authentication.authenticate(deleteQuery);

        deleteQuery.execute(new TaskListener<Void>() {
            public void taskExecuted(Task<Void> task) {
                System.out.println("DELETE result");
            }

            public void executeFailed(Task<Void> task) {
                System.out.println("DELETE fault: " + task.getFault());
            }
        });

        // Wait for the async. calls to complete
        try {
            Thread.sleep(10000);
        } catch(InterruptedException exception) {
        }
    }
}
