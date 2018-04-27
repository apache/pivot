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

import org.apache.pivot.util.ListenerList;

/**
 * Defines event listener methods that pertain to rollups. Developers register
 * for such events by adding themselves to a rollup's list of "rollup listeners"
 * (see {@link Rollup#getRollupListeners()}).
 */
public interface RollupListener {
    /**
     * Rollup listeners.
     */
    public static class Listeners extends ListenerList<RollupListener> implements RollupListener {
        @Override
        public void headingChanged(Rollup rollup, Component previousHeading) {
            forEach(listener -> listener.headingChanged(rollup, previousHeading));
        }

        @Override
        public void contentChanged(Rollup rollup, Component previousContent) {
            forEach(listener -> listener.contentChanged(rollup, previousContent));
        }

        @Override
        public void collapsibleChanged(Rollup rollup) {
            forEach(listener -> listener.collapsibleChanged(rollup));
        }
    }

    /**
     * Rollup listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements RollupListener {
        @Override
        public void headingChanged(Rollup rollup, Component previousHeading) {
            // empty block
        }

        @Override
        public void contentChanged(Rollup rollup, Component previousContent) {
            // empty block
        }

        @Override
        public void collapsibleChanged(Rollup rollup) {
            // empty block
        }
    }

    /**
     * Called when a rollup's heading component changed.
     *
     * @param rollup The rollup whose heading changed.
     * @param previousHeading What the heading used to be.
     */
    default void headingChanged(Rollup rollup, Component previousHeading) {
    }

    /**
     * Called when a rollup's content component changed.
     *
     * @param rollup The rollup that has new content.
     * @param previousContent What the content used to be.
     */
    default void contentChanged(Rollup rollup, Component previousContent) {
    }

    /**
     * Called when a rollup's collapsible flag has changed.
     *
     * @param rollup The rollup that changed.
     */
    default void collapsibleChanged(Rollup rollup) {
    }
}
