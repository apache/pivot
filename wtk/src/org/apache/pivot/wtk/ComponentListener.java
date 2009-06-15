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
 * Component listener interface.
 *
 * @author gbrown
 */
public interface ComponentListener {
    /**
     * Component listener adapter.
     *
     * @author tvolkert
     */
    public static class Adapter implements ComponentListener {
        public void parentChanged(Component component, Container previousParent) {
        }

        public void sizeChanged(Component component, int previousWidth, int previousHeight) {
        }

        public void locationChanged(Component component, int previousX, int previousY) {
        }

        public void visibleChanged(Component component) {
        }

        public void styleUpdated(Component component, String styleKey, Object previousValue) {
        }

        public void cursorChanged(Component component, Cursor previousCursor) {
        }

        public void tooltipTextChanged(Component component, String previousTooltipText) {
        }
    }

    /**
     * Called when a component's parent has changed (when the component is
     * either added to or removed from a container).
     *
     * @param component
     * @param previousParent
     */
    public void parentChanged(Component component, Container previousParent);

    /**
     * Called when a component's size has changed.
     *
     * @param component
     * @param previousWidth
     * @param previousHeight
     */
    public void sizeChanged(Component component, int previousWidth, int previousHeight);

    /**
     * Called when a component's location has changed.
     *
     * @param component
     * @param previousX
     * @param previousY
     */
    public void locationChanged(Component component, int previousX, int previousY);

    /**
     * Called when a component's visible flag has changed.
     *
     * @param component
     */
    public void visibleChanged(Component component);

    /**
     * Called when a component style has been updated.
     *
     * @param component
     * @param styleKey
     * @param previousValue
     */
    public void styleUpdated(Component component, String styleKey, Object previousValue);

    /**
     * Called when a component's cursor has changed.
     *
     * @param component
     * @param previousCursor
     */
    public void cursorChanged(Component component, Cursor previousCursor);

    /**
     * Called when a component's tooltip text has changed.
     *
     * @param component
     * @param previousTooltipText
     */
    public void tooltipTextChanged(Component component, String previousTooltipText);
}
