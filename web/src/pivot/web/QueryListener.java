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
package pivot.web;

/**
 * Defines event listener methods that pertain to queries. Developers register
 * for such events by adding themselves to a query's list of "query
 * listeners" (see {@link Query#getQueryListeners()}).
 * <p>
 * Note that, like {@link pivot.util.concurrent.TaskListener task listeners},
 * query listeners will be notified on the query's worker thread, not the thread
 * that executed the query.
 *
 * @author tvolkert
 */
public interface QueryListener<V> {
    /**
     * Adapts the <tt>QueryListener</tt> interface.
     *
     * @author tvolkert
     */
    public static class Adapter<V> implements QueryListener<V> {
        public void connected(Query<V> query) {
        }

        public void requestSent(Query<V> query) {
        }

        public void responseReceived(Query<V> query) {
        }

        public void failed(Query<V> query) {
        }
    }

    /**
     * Called when a query has connected to the server but the request has not
     * yet been sent.
     *
     * @param query
     */
    public void connected(Query<V> query);

    /**
     * Called when the request has been sent to the server but the response has
     * not yet been received.
     *
     * @param query
     */
    public void requestSent(Query<V> query);

    /**
     * Called when a response has been received from the server.
     *
     * @param query
     */
    public void responseReceived(Query<V> query);

    /**
     * Called when an error has occurred
     *
     * @param query
     */
    public void failed(Query<V> query);
}
