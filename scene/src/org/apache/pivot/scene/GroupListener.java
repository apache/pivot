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
 * Group listener interface.
 */
public interface GroupListener {
    /**
     * Group listener adapter.
     */
    public static class Adapter implements GroupListener {
        @Override
        public void preferredSizeChanged(Group group, int previousPreferredWidth,
            int previousPreferredHeight) {
        }

        @Override
        public void widthLimitsChanged(Group group, int previousMinimumWidth,
            int previousMaximumWidth) {
        }

        @Override
        public void heightLimitsChanged(Group group, int previousMinimumHeight,
            int previousMaximumHeight) {
        }


        @Override
        public void layoutChanged(Group group, Layout previousLayout) {
        }

        @Override
        public void focusTraversalPolicyChanged(Group group,
            FocusTraversalPolicy previousFocusTraversalPolicy) {
        }
    }

    /**
     * Called when a group's preferred size has changed.
     *
     * @param group
     * @param previousPreferredWidth
     * @param previousPreferredHeight
     */
    public void preferredSizeChanged(Group group, int previousPreferredWidth,
        int previousPreferredHeight);

    /**
     * Called when a group's preferred width limits have changed.
     *
     * @param group
     * @param previousMinimumWidth
     * @param previousMaximumWidth
     */
    public void widthLimitsChanged(Group group, int previousMinimumWidth,
        int previousMaximumWidth);

    /**
     * Called when a group's preferred height limits have changed.
     *
     * @param group
     * @param previousMinimumHeight
     * @param previousMaximumHeight
     */
    public void heightLimitsChanged(Group group, int previousMinimumHeight,
        int previousMaximumHeight);

    /**
     * Called when a group's layout has changed.
     *
     * @param group
     * @param previousLayout
     */
    public void layoutChanged(Group group, Layout previousLayout);

    /**
     * Called when a group's focus traversal policy has changed.
     *
     * @param group
     * @param previousFocusTraversalPolicy
     */
    public void focusTraversalPolicyChanged(Group group,
        FocusTraversalPolicy previousFocusTraversalPolicy);
}
