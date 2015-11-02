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
package org.apache.pivot.wtk;

import org.apache.pivot.collections.Sequence;

/**
 * Container listener interface.
 */
public interface ContainerListener {
    /**
     * Container listener adapter.
     */
    public static class Adapter implements ContainerListener {
        @Override
        public void componentInserted(Container container, int index) {
            // empty block
        }

        @Override
        public void componentsRemoved(Container container, int index, Sequence<Component> removed) {
            // empty block
        }

        @Override
        public void componentMoved(Container container, int from, int to) {
            // empty block
        }

        @Override
        public void focusTraversalPolicyChanged(Container container,
            FocusTraversalPolicy previousFocusTraversalPolicy) {
            // empty block
        }
    }

    /**
     * Called when a component has been inserted into a container's component
     * sequence.
     *
     * @param container The container that has changed.
     * @param index     The index where the new component has been inserted.
     */
    public void componentInserted(Container container, int index);

    /**
     * Called when components have been removed from a container's component
     * sequence.
     *
     * @param container The container that has changed.
     * @param index     The starting index of the components that were removed.
     * @param removed   The complete sequence of removed components.
     */
    public void componentsRemoved(Container container, int index, Sequence<Component> removed);

    /**
     * Called when a component has moved from one z-index to another within a
     * container.
     *
     * @param container The container that has changed.
     * @param from      The starting index of the component (in Z-order).
     * @param to        The place in the Z-order where this component ended up.
     */
    public void componentMoved(Container container, int from, int to);

    /**
     * Called when a container's focus traversal policy has changed.
     *
     * @param container                    The container that has changed.
     * @param previousFocusTraversalPolicy The previous value of the focus traversal policy for this container.
     */
    public void focusTraversalPolicyChanged(Container container,
        FocusTraversalPolicy previousFocusTraversalPolicy);
}
