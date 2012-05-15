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
 * Accordion attribute listener interface.
 */
public interface AccordionAttributeListener {
    /**
     * Accordion attribute listener adapter.
     */
    public static class Adapter implements AccordionAttributeListener {
        @Override
        public void headerDataChanged(Accordion accordion, Component component, Object previousHeaderData) {
            // empty block
        }

        @Override
        public void tooltipTextChanged(Accordion accordion, Component component, String previousTooltipText) {
            // empty block
        }
    }

    /**
     * Called when a panel's header data attribute has changed.
     *
     * @param accordion
     * @param component
     * @param previousHeaderData
     */
    public void headerDataChanged(Accordion accordion, Component component, Object previousHeaderData);

    /**
     * Called when a panel's tooltip text has changed.
     *
     * @param accordion
     * @param component
     * @param previousTooltipText
     */
    public void tooltipTextChanged(Accordion accordion, Component component, String previousTooltipText);
}
