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
package org.apache.pivot.xml;

import org.apache.pivot.util.ListenerList;

/**
 * Abstract base class for XML nodes.
 */
public abstract class Node {
    private Element parent = null;

    private NodeListener.Listeners nodeListeners = new NodeListener.Listeners();

    /**
     * @return The parent element of the node.
     */
    public Element getParent() {
        return parent;
    }

    /**
     * Sets the parent element of the node.
     *
     * @param parent New parent for this node.
     */
    protected void setParent(Element parent) {
        Element previousParent = this.parent;
        this.parent = parent;

        nodeListeners.parentChanged(this, previousParent);
    }

    /**
     * @return The node listener list.
     */
    public ListenerList<NodeListener> getNodeListeners() {
        return nodeListeners;
    }
}
