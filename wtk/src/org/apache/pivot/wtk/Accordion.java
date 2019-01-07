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

import java.util.Iterator;

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.content.AccordionHeaderDataRenderer;

/**
 * Component that provides access to a set of components via selectable headers.
 * Only one component is visible at a time.
 */
@DefaultProperty("panels")
public class Accordion extends Container {
    /**
     * Panel sequence implementation.
     */
    public final class PanelSequence implements Sequence<Component>, Iterable<Component> {
        private PanelSequence() {
        }

        @Override
        public int add(final Component panel) {
            int index = getLength();
            insert(panel, index);

            return index;
        }

        @Override
        public void insert(final Component panel, final int index) {
            Utils.checkNull(panel, "panel");

            // Add the panel to the component sequence
            Accordion.this.add(panel);
            panels.insert(panel, index);

            // Update the selection
            int previousSelectedIndex = selectedIndex;

            if (selectedIndex >= index) {
                selectedIndex++;
            }

            // Fire insert event
            accordionListeners.panelInserted(Accordion.this, index);

            // Fire selection change event, if necessary
            if (selectedIndex != previousSelectedIndex && previousSelectedIndex > -1) {
                accordionSelectionListeners.selectedIndexChanged(Accordion.this, selectedIndex);
            }
        }

        @Override
        @UnsupportedOperation
        public Component update(final int index, final Component panel) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(final Component panel) {
            int index = indexOf(panel);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<Component> remove(final int index, final int count) {
            // Remove the panels from the panel list
            Sequence<Component> removed = panels.remove(index, count);

            // Update the selection
            int previousSelectedIndex = selectedIndex;

            if (selectedIndex >= index) {
                if (selectedIndex < index + count) {
                    selectedIndex = -1;
                } else {
                    selectedIndex -= count;
                }
            }

            // Fire remove event
            accordionListeners.panelsRemoved(Accordion.this, index, removed);

            // Remove the panels from the component list
            for (int i = 0, n = removed.getLength(); i < n; i++) {
                Component panel = removed.get(i);
                Accordion.this.remove(panel);
            }

            // Fire selection change event, if necessary
            if (selectedIndex != previousSelectedIndex && previousSelectedIndex > -1) {
                accordionSelectionListeners.selectedIndexChanged(Accordion.this, selectedIndex);
            }

            return removed;
        }

        @Override
        public Component get(final int index) {
            return panels.get(index);
        }

        @Override
        public int indexOf(final Component panel) {
            return panels.indexOf(panel);
        }

        @Override
        public int getLength() {
            return panels.getLength();
        }

        @Override
        public Iterator<Component> iterator() {
            return new ImmutableIterator<>(panels.iterator());
        }
    }

    private enum Attribute {
        HEADER_DATA,
        TOOLTIP_TEXT;
    }

    private ArrayList<Component> panels = new ArrayList<>();
    private PanelSequence panelSequence = new PanelSequence();
    private int selectedIndex = -1;
    private Button.DataRenderer headerDataRenderer = DEFAULT_HEADER_DATA_RENDERER;

    private AccordionListener.Listeners accordionListeners = new AccordionListener.Listeners();
    private AccordionSelectionListener.Listeners accordionSelectionListeners =
        new AccordionSelectionListener.Listeners();
    private AccordionAttributeListener.Listeners accordionAttributeListeners =
        new AccordionAttributeListener.Listeners();

    private static final Button.DataRenderer DEFAULT_HEADER_DATA_RENDERER = new AccordionHeaderDataRenderer();

    public Accordion() {
        installSkin(Accordion.class);
    }

    public PanelSequence getPanels() {
        return panelSequence;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(final int selectedIndex) {
        indexBoundsCheck("selectedIndex", selectedIndex, -1, panels.getLength() - 1);

        int previousSelectedIndex = this.selectedIndex;

        if (previousSelectedIndex != selectedIndex) {
            Vote vote = accordionSelectionListeners.previewSelectedIndexChange(this, selectedIndex);

            if (vote == Vote.APPROVE) {
                this.selectedIndex = selectedIndex;
                accordionSelectionListeners.selectedIndexChanged(this, previousSelectedIndex);
            } else {
                accordionSelectionListeners.selectedIndexChangeVetoed(this, vote);
            }
        }
    }

    public Component getSelectedPanel() {
        return (selectedIndex == -1) ? null : panels.get(selectedIndex);
    }

    public Button.DataRenderer getHeaderDataRenderer() {
        return headerDataRenderer;
    }

    public void setHeaderDataRenderer(final Button.DataRenderer headerDataRenderer) {
        Utils.checkNull(headerDataRenderer, "headerDataRenderer");

        Button.DataRenderer previousHeaderDataRenderer = this.headerDataRenderer;
        if (previousHeaderDataRenderer != headerDataRenderer) {
            this.headerDataRenderer = headerDataRenderer;
            accordionListeners.headerDataRendererChanged(this, previousHeaderDataRenderer);
        }
    }

    @Override
    public Sequence<Component> remove(final int index, final int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);

            if (panels.indexOf(component) >= 0) {
                throw new UnsupportedOperationException("Cannot remove a panel directly from the Accordion.");
            }
        }

        // Call the base method to remove the components
        return super.remove(index, count);
    }

    public ListenerList<AccordionListener> getAccordionListeners() {
        return accordionListeners;
    }

    public ListenerList<AccordionSelectionListener> getAccordionSelectionListeners() {
        return accordionSelectionListeners;
    }

    public ListenerList<AccordionAttributeListener> getAccordionAttributeListeners() {
        return accordionAttributeListeners;
    }

    public static Object getHeaderData(final Component component) {
        return component.getAttribute(Attribute.HEADER_DATA);
    }

    public static void setHeaderData(final Component component, final Object headerData) {
        Object previousHeaderData = component.setAttribute(Attribute.HEADER_DATA, headerData);

        if (previousHeaderData != headerData) {
            Container parent = component.getParent();

            if (parent instanceof Accordion) {
                Accordion accordion = (Accordion) parent;
                accordion.accordionAttributeListeners.headerDataChanged(accordion, component,
                    previousHeaderData);
            }
        }
    }

    public static String getTooltipText(final Component component) {
        return (String) component.getAttribute(Attribute.TOOLTIP_TEXT);
    }

    public static void setTooltipText(final Component component, final String tooltipText) {
        String previousTooltipText = (String) component.setAttribute(Attribute.TOOLTIP_TEXT,
            tooltipText);

        if (previousTooltipText != tooltipText) {
            Container parent = component.getParent();

            if (parent instanceof Accordion) {
                Accordion accordion = (Accordion) parent;
                accordion.accordionAttributeListeners.tooltipTextChanged(accordion, component,
                    previousTooltipText);
            }
        }
    }
}
