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
 * Menu handler interface.
 */
public interface MenuHandler {
    /**
     * Menu handler adapter.
     */
    public static class Adapter implements MenuHandler {
        @Override
        public void configureMenuBar(Component component, MenuBar menuBar) {
            // empty block
        }

        @Override
        public void cleanupMenuBar(Component component, MenuBar menuBar) {
            // empty block
        }

        @Override
        public boolean configureContextMenu(Component component, Menu menu, int x, int y) {
            return false;
        }
    }

    /**
     * Called when a component to which this handler is attached gains the focus.
     *
     * @param component
     * @param menuBar
     */
    public void configureMenuBar(Component component, MenuBar menuBar);

    /**
     * Called when a component to which this handler is attached loses the focus.
     *
     * @param component
     * @param menuBar
     */
    public void cleanupMenuBar(Component component, MenuBar menuBar);

    /**
     * Called when the user right-clicks on a component to which this handler is attached.
     *
     * @param component
     * @param menu
     * @param x
     * @param y
     *
     * @return
     * <tt>true</tt> to stop propagation of context menu configuration;
     * <tt>false</tt> to allow it to continue.
     */
    public boolean configureContextMenu(Component component, Menu menu, int x, int y);
}
