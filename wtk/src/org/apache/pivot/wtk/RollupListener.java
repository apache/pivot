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
 * Defines event listener methods that pertain to rollups. Developers
 * register for such events by adding themselves to a rollup's list of "rollup
 * listeners" (see {@link Rollup#getRollupListeners()}).
 */
public interface RollupListener {
    /**
     * Rollup listener adapter.
     */
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
     * @param rollup
     * @param previousHeading
     */
    public void headingChanged(Rollup rollup, Component previousHeading);

    /**
     * Called when a rollup's content component changed.
     *
     * @param rollup
     * @param previousContent
     */
    public void contentChanged(Rollup rollup, Component previousContent);

    /**
     * Called when a rollup's collapsible flag has changed.
     *
     * @param rollup
     */
    public void collapsibleChanged(Rollup rollup);
}
