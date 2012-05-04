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

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
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
        public int add(Component panel) {
            int index = getLength();
            insert(panel, index);

            return index;
        }

        @Override
        public void insert(Component panel, int index) {
            if (panel == null) {
                throw new IllegalArgumentException("panel is null.");
            }

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
        public Component update(int index, Component panel) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(Component panel) {
            int index = indexOf(panel);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<Component> remove(int index, int count) {
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
        public Component get(int index) {
            return panels.get(index);
        }

        @Override
        public int indexOf(Component panel) {
            return panels.indexOf(panel);
        }

        @Override
        public int getLength() {
            return panels.getLength();
        }

        @Override
        public Iterator<Component> iterator() {
            return new ImmutableIterator<Component>(panels.iterator());
        }
    }

    private enum Attribute {
        HEADER_DATA,
        TOOLTIP_TEXT;
    }

    private static class AccordionListenerList extends WTKListenerList<AccordionListener>
        implements AccordionListener {
        @Override
        public void panelInserted(Accordion accordion, int index) {
            for (AccordionListener listener : this) {
                listener.panelInserted(accordion, index);
            }
        }

        @Override
        public void panelsRemoved(Accordion accordion, int index, Sequence<Component> panels) {
            for (AccordionListener listener : this) {
                listener.panelsRemoved(accordion, index, panels);
            }
        }

        @Override
        public void headerDataRendererChanged(Accordion accordion, Button.DataRenderer previousHeaderDataRenderer) {
            for (AccordionListener listener : this) {
                listener.headerDataRendererChanged(accordion, previousHeaderDataRenderer);
            }
        }
    }

    private static class AccordionSelectionListenerList extends WTKListenerList<AccordionSelectionListener>
        implements AccordionSelectionListener {
        @Override
        public Vote previewSelectedIndexChange(Accordion accordion, int selectedIndex) {
            Vote vote = Vote.APPROVE;

            for (AccordionSelectionListener listener : this) {
                vote = vote.tally(listener.previewSelectedIndexChange(accordion, selectedIndex));
            }

            return vote;
        }

        @Override
        public void selectedIndexChangeVetoed(Accordion accordion, Vote reason) {
            for (AccordionSelectionListener listener : this) {
                listener.selectedIndexChangeVetoed(accordion, reason);
            }
        }

        @Override
        public void selectedIndexChanged(Accordion accordion, int previousSelectedIndex) {
            for (AccordionSelectionListener listener : this) {
                listener.selectedIndexChanged(accordion, previousSelectedIndex);
            }
        }
    }

    private static class AccordionAttributeListenerList extends WTKListenerList<AccordionAttributeListener>
        implements AccordionAttributeListener {
        @Override
        public void headerDataChanged(Accordion accordion, Component component, Object previousHeaderData) {
            for (AccordionAttributeListener listener : this) {
                listener.headerDataChanged(accordion, component, previousHeaderData);
            }
        }

        @Override
        public void tooltipTextChanged(Accordion accordion, Component component, String previousTooltipText) {
            for (AccordionAttributeListener listener : this) {
                listener.tooltipTextChanged(accordion, component, previousTooltipText);
            }
        }
    }

    private ArrayList<Component> panels = new ArrayList<Component>();
    private PanelSequence panelSequence = new PanelSequence();
    private int selectedIndex = -1;
    private Button.DataRenderer headerDataRenderer = DEFAULT_HEADER_DATA_RENDERER;

    private AccordionListenerList accordionListeners = new AccordionListenerList();
    private AccordionSelectionListenerList accordionSelectionListeners = new AccordionSelectionListenerList();
    private AccordionAttributeListenerList accordionAttributeListeners = new AccordionAttributeListenerList();

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

    public void setSelectedIndex(int selectedIndex) {
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

    public void setHeaderDataRenderer(Button.DataRenderer headerDataRenderer) {
        if (headerDataRenderer == null) {
            throw new IllegalArgumentException();
        }

        Button.DataRenderer previousHeaderDataRenderer = this.headerDataRenderer;
        if (previousHeaderDataRenderer != headerDataRenderer) {
            this.headerDataRenderer = headerDataRenderer;
            accordionListeners.headerDataRendererChanged(this, previousHeaderDataRenderer);
        }
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);

            if (panels.indexOf(component) >= 0) {
                throw new UnsupportedOperationException();
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

    public static Object getHeaderData(Component component) {
        return component.getAttribute(Attribute.HEADER_DATA);
    }

    public static void setHeaderData(Component component, Object headerData) {
        Object previousHeaderData = component.setAttribute(Attribute.HEADER_DATA, headerData);

        if (previousHeaderData != headerData) {
            Container parent = component.getParent();

            if (parent instanceof Accordion) {
                Accordion accordion = (Accordion)parent;
                accordion.accordionAttributeListeners.headerDataChanged(accordion, component,
                    previousHeaderData);
            }
        }
    }

    public static String getTooltipText(Component component) {
        return (String)component.getAttribute(Attribute.TOOLTIP_TEXT);
    }

    public static void setTooltipText(Component component, String tooltipText) {
        String previousTooltipText = (String)component.setAttribute(Attribute.TOOLTIP_TEXT, tooltipText);

        if (previousTooltipText != tooltipText) {
            Container parent = component.getParent();

            if (parent instanceof Accordion) {
                Accordion accordion = (Accordion)parent;
                accordion.accordionAttributeListeners.tooltipTextChanged(accordion, component,
                    previousTooltipText);
            }
        }
    }
}
