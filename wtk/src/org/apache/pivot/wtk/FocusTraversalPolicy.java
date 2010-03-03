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

/**
 * Defines the order in which components will receive focus during focus
 * traversal.
 */
public interface FocusTraversalPolicy {
    /**
     * Returns the next focus destination according to this traversal policy.
     *
     * @param container
     * The container to which the traversal policy applies.
     *
     * @param component
     * The component from which focus is being transferred. If <tt>null</tt>,
     * implementations should return the first component for a forward
     * traversal and the last component for a backward traversal.
     *
     * @param direction
     * The direction in which to transfer focus.
     *
     * @return
     * The component to focus, or <tt>null</tt> if there are no more components
     * in the given direction or next component cannot be determined.
     */
    Component getNextComponent(Container container, Component component, FocusTraversalDirection direction);
}
