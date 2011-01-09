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
package org.apache.pivot.scene;

/**
 * Node mouse listener interface. Node mouse events are "bubbling" events;
 * consuming a node mouse event prevents it from propagating up the node
 * hierarchy.
 */
public interface NodeMouseListener {
    /**
     * Node mouse listener adapter.
     */
    public static class Adapter implements NodeMouseListener {
        @Override
        public void mouseEntered(Node node) {
        }

        @Override
        public void mouseExited(Node node) {
        }

        @Override
        public boolean mouseMoved(Node node, int x, int y, boolean captured) {
            return false;
        }
    }

    /**
     * Called when the mouse enters a node.
     *
     * @param node
     */
    public void mouseEntered(Node node);

    /**
     * Called when the mouse exits a node.
     *
     * @param node
     */
    public void mouseExited(Node node);

    /**
     * Called as the mouse is moved over a node.
     *
     * @param node
     * @param x
     * @param y
     * @param captured
     *
     * @return
     * <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    public boolean mouseMoved(Node node, int x, int y, boolean captured);
}
