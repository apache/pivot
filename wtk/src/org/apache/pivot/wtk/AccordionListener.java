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
 * Accordion listener interface.
 */
public interface AccordionListener {
    /**
     * Accordion listener adapter.
     */
    public static class Adapter implements AccordionListener {
        @Override
        public void panelInserted(Accordion accordion, int index) {
            // empty block
        }

        @Override
        public void panelsRemoved(Accordion accordion, int index, Sequence<Component> removed) {
            // empty block
        }

        @Override
        public void headerDataRendererChanged(Accordion accordion, Button.DataRenderer previousHeaderDataRenderer) {
            // empty block
        }
    }

    /**
     * Called when a panel has been inserted into a accordion's panel sequence.
     *
     * @param accordion
     * @param index
     */
    public void panelInserted(Accordion accordion, int index);

    /**
     * Called when a panel has been removed from an accordion's panel sequence.
     *
     * @param accordion
     * @param index
     * @param removed
     */
    public void panelsRemoved(Accordion accordion, int index, Sequence<Component> removed);

    /**
     * Called when an accordion's header data renderer has changed.
     *
     * @param accordion
     * @param previousHeaderDataRenderer
     */
    public void headerDataRendererChanged(Accordion accordion, Button.DataRenderer previousHeaderDataRenderer);
}
