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
 * Accordion attribute listener interface.
 */
public interface AccordionAttributeListener {
    /**
     * Accordion attribute listeners.
     */
    public static class Listeners extends ListenerList<AccordionAttributeListener>
        implements AccordionAttributeListener {
        @Override
        public void headerDataChanged(Accordion accordion, Component component,
            Object previousHeaderData) {
            forEach(listener -> listener.headerDataChanged(accordion, component, previousHeaderData));
        }

        @Override
        public void tooltipTextChanged(Accordion accordion, Component component,
            String previousTooltipText) {
            forEach(listener -> listener.tooltipTextChanged(accordion, component, previousTooltipText));
        }
    }

    /**
     * Accordion attribute listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements AccordionAttributeListener {
        @Override
        public void headerDataChanged(Accordion accordion, Component component,
            Object previousHeaderData) {
            // empty block
        }

        @Override
        public void tooltipTextChanged(Accordion accordion, Component component,
            String previousTooltipText) {
            // empty block
        }
    }

    /**
     * Called when a panel's header data attribute has changed.
     *
     * @param accordion           The enclosing accordion that has changed.
     * @param component           The child component in question.
     * @param previousHeaderData  The previous header data for this component.
     */
    default void headerDataChanged(Accordion accordion, Component component,
        Object previousHeaderData) {
    }

    /**
     * Called when a panel's tooltip text has changed.
     *
     * @param accordion           The enclosing accordion that has changed.
     * @param component           The child component in question.
     * @param previousTooltipText The previous tooltip text for the component.
     */
    default void tooltipTextChanged(Accordion accordion, Component component,
        String previousTooltipText) {
    }
}
