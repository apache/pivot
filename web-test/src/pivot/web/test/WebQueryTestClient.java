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

import pivot.collections.Dictionary;
import pivot.serialization.BinarySerializer;
import pivot.serialization.JSONSerializer;
import pivot.util.concurrent.Task;
import pivot.util.concurrent.TaskListener;
import pivot.web.BasicAuthentication;
import pivot.web.GetQuery;
import pivot.web.DeleteQuery;
import pivot.web.PostQuery;
import pivot.web.PutQuery;
import pivot.wtk.Application;
import pivot.wtk.Display;

public class WebQueryTestClient implements Application {
    final static boolean useProxy = true;

    final static String HOSTNAME = "localhost";
    final static String PATH = (useProxy ? "/pivot_web_test/proxy" : "/pivot_web_test/webquery") + "/bar/quux";
    final static int PORT = 8080;
    final static boolean SECURE = false;

    public void startup(Display display, Dictionary<String, String> properties)
    	throws Exception {
        final BasicAuthentication authentication = new BasicAuthentication("foo", "bar");

        // GET
        final GetQuery getQuery = new GetQuery(HOSTNAME, PORT, PATH, SECURE);
        getQuery.getArguments().put("a", "b");
        getQuery.setSerializer(new BinarySerializer());
        authentication.authenticate(getQuery);

        getQuery.execute(new TaskListener<Object>() {
            @SuppressWarnings("unchecked")
            public void taskExecuted(Task<Object> task) {
                Dictionary<String, Object> result = (Dictionary<String, Object>)task.getResult();

                System.out.println("GET result: "
                    + "username: " + result.get("username") + ", "
                    + "pathInfo: " + result.get("pathInfo") + ", "
                    + "queryString: " + result.get("queryString"));
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
    }

    public boolean shutdown(boolean optional) {
    	return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
