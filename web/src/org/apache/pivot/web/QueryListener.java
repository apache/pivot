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
package org.apache.pivot.web;

import org.apache.pivot.util.ListenerList;

/**
 * Query listener interface.
 */
public interface QueryListener<V> {
    /**
     * Query listener listeners list.
     */
    public static class Listeners<V> extends ListenerList<QueryListener<V>>
        implements QueryListener<V> {
        @Override
        public synchronized void connected(Query<V> query) {
            forEach(listener -> listener.connected(query));
        }

        @Override
        public synchronized void requestSent(Query<V> query) {
            forEach(listener -> listener.requestSent(query));
        }

        @Override
        public synchronized void responseReceived(Query<V> query) {
            forEach(listener -> listener.responseReceived(query));
        }

        @Override
        public synchronized void failed(Query<V> query) {
            forEach(listener -> listener.failed(query));
        }
    }

    /**
     * Query listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter<V> implements QueryListener<V> {
        @Override
        public void connected(Query<V> query) {
            // empty block
        }

        @Override
        public void requestSent(Query<V> query) {
            // empty block
        }

        @Override
        public void responseReceived(Query<V> query) {
            // empty block
        }

        @Override
        public void failed(Query<V> query) {
            // empty block
        }
    }

    /**
     * Called when a query has connected to the server but the request has not
     * yet been sent.
     *
     * @param query The query that has just connected.
     */
    default void connected(Query<V> query) {
    }

    /**
     * Called when the request has been sent to the server but the response has
     * not yet been received.
     *
     * @param query The query whose request has been sent.
     */
    default void requestSent(Query<V> query) {
    }

    /**
     * Called when a response has been received from the server.
     *
     * @param query The query whose response has just been received.
     */
    default void responseReceived(Query<V> query) {
    }

    /**
     * Called when an error has occurred.
     *
     * @param query The query that has failed.
     */
    default void failed(Query<V> query) {
    }
}
