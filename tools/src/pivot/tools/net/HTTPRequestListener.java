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
package pivot.tools.net;

/**
 * Defines event listener methods that pertain to HTTP requests. Developers
 * register for such events by adding themselves to a request's list of "HTTP
 * request listeners" (see {@link HTTPRequest#getHTTPRequestListeners()}).
 * <p>
 * Note that, like {@link pivot.util.concurrent.TaskListener task listeners},
 * query listeners will be notified on the query's worker thread, not the thread
 * that executed the query.
 *
 * @author tvolkert
 */
public interface HTTPRequestListener {
    /**
     * Called when an <tt>HTTPRequest</tt> has connected to the server but the
     * request has not yet been sent.
     *
     * @param httpRequest
     */
    public void connected(HTTPRequest httpRequest);

    /**
     * Called when the request has been sent to the server but the response has
     * not yet been received.
     *
     * @param httpRequest
     */
    public void requestSent(HTTPRequest httpRequest);

    /**
     * Called when a response has been received from the server.
     *
     * @param httpRequest
     */
    public void responseReceived(HTTPRequest httpRequest);

    /**
     * Called when an error has occurred
     *
     * @param httpRequest
     */
    public void failed(HTTPRequest httpRequest);
}
