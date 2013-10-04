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

import org.apache.pivot.util.Vote;

/**
 * Expander listener interface..
 */
public interface ExpanderListener {
    /**
     * Expander listener adapter.
     */
    public static class Adapter implements ExpanderListener {
        @Override
        public void titleChanged(Expander expander, String previousTitle) {
            // empty block
        }

        @Override
        public void collapsibleChanged(Expander expander) {
            // empty block
        }

        @Override
        public Vote previewExpandedChange(Expander expander) {
            return Vote.APPROVE;
        }

        @Override
        public void expandedChangeVetoed(Expander expander, Vote reason) {
            // empty block
        }

        @Override
        public void expandedChanged(Expander expander) {
            // empty block
        }

        @Override
        public void contentChanged(Expander expander, Component previousContent) {
            // empty block
        }
    }

    /**
     * Called when an expander's title has changed.
     *
     * @param expander
     * @param previousTitle
     */
    public void titleChanged(Expander expander, String previousTitle);

    /**
     * Called when an expander's collapsible flag has changed.
     *
     * @param expander
     */
    public void collapsibleChanged(Expander expander);

    /**
     * Called to preview an expanded change event.
     *
     * @param expander
     */
    public Vote previewExpandedChange(Expander expander);

    /**
     * Called when an expanded change event has been vetoed.
     *
     * @param expander
     * @param reason
     */
    public void expandedChangeVetoed(Expander expander, Vote reason);

    /**
     * Called when an expander's expanded state has changed.
     *
     * @param expander
     */
    public void expandedChanged(Expander expander);

    /**
     * Called when an expander's content component has changed.
     *
     * @param expander
     * @param previousContent
     */
    public void contentChanged(Expander expander, Component previousContent);
}
