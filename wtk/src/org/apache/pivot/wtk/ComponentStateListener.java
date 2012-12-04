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
 * Component state listener interface.
 */
public interface ComponentStateListener {
    /**
     * Component state listener adapter.
     */
    public static class Adapter implements ComponentStateListener {
        @Override
        public void enabledChanged(Component component) {
            // empty block
        }

        @Override
        public void focusedChanged(Component component, Component obverseComponent) {
            // empty block
        }
    }

    /**
     * Called when a component's enabled state has changed.
     * <p> Called both when the component is enabled and when it is disabled.
     * The component's <code>enabled</code> flag has already been set when this
     * method is called so the new state can be determined by calling the
     * {@link Component#isEnabled} method.
     *
     * @param component The component whose enabled state is changing.
     */
    public void enabledChanged(Component component);

    /**
     * Called when a component's focused state has changed.
     * <p> This will be called both when a component gains focus and when it loses
     * focus. The currently focused component has already been set when this method
     * is called, so that the new state of the component can be determined by calling
     * the {@link Component#isFocused} method.
     *
     * @param component The component that is either gaining focus or the one that
     *                  previously had focus and is now losing it.
     * @param obverseComponent If the component is gaining focus, this is the component
     *                         that is losing focus. If the component is losing focus
     *                         this is the component that is gaining the focus instead.
     */
    public void focusedChanged(Component component, Component obverseComponent);
}
