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
 * Node listener interface.
 */
public interface NodeListener {
    /**
     * Node listener adapter.
     */
    public static class Adapter implements NodeListener {
        @Override
        public void locationChanged(Node node, int previousX, int previousY) {
        }

        @Override
        public void sizeChanged(Node node, int previousWidth, int previousHeight) {
        }

        @Override
        public void visibleChanged(Node node) {
        }

        @Override
        public void clipChanged(Node node) {
        }
    }

    /**
     * Called when a node's location has changed.
     *
     * @param node
     * @param previousX
     * @param previousY
     */
    public void locationChanged(Node node, int previousX, int previousY);

    /**
     * Called when a node's size has changed.
     *
     * @param node
     * @param previousWidth
     * @param previousHeight
     */
    public void sizeChanged(Node node, int previousWidth, int previousHeight);

    /**
     * Called when a node's visible flag has changed.
     *
     * @param node
     */
    public void visibleChanged(Node node);

    /**
     * Called when a node's clip flag has changed.
     */
    public void clipChanged(Node node);
}
