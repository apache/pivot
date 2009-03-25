/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk;

import java.net.URL;
import java.util.Iterator;

import pivot.collections.ArrayList;
import pivot.collections.Sequence;
import pivot.util.ImmutableIterator;
import pivot.util.ListenerList;
import pivot.util.Vote;
import pivot.wtk.media.Image;

/**
 * Component that provides access to a set of components via selectable headers.
 * Only one component is visible at a time.
 * <p>
 * TODO Add a getPanelAt() method that delegates to the skin.
 *
 * @author gbrown
 */
public class Accordion extends Container {
    /**
     * Defines accordion panel attributes.
     *
     * @author gbrown
     */
    protected static class AccordionAttributes extends Attributes {
        private String name = null;
        private Image icon = null;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            String previousName = this.name;
            this.name = name;

            Component component = getComponent();
            Accordion accordion = (Accordion)component.getParent();
            if (accordion != null) {
                accordion.accordionAttributeListeners.nameChanged(accordion, component, previousName);
            }
        }

        public Image getIcon() {
            return icon;
        }

        public void setIcon(Image icon) {
            Image previousIcon = this.icon;
            this.icon = icon;

            Component component = getComponent();
            Accordion accordion = (Accordion)component.getParent();
            if (accordion != null) {
                accordion.accordionAttributeListeners.iconChanged(accordion, component, previousIcon);
            }
        }
    }

    /**
     * Panel sequence implementation.
     *
     * @author gbrown
     */
    public final class PanelSequence implements Sequence<Component>, Iterable<Component> {
        private PanelSequence() {
        }

        public int add(Component panel) {
            int i = getLength();
            insert(panel, i);

            return i;
        }

        public void insert(Component panel, int index) {
            if (panel == null) {
                throw new IllegalArgumentException("panel is null.");
            }

            if (panel.getParent() != null) {
                throw new IllegalArgumentException("Panel already has a parent.");
            }

            // Add the panel to the component sequence
            Accordion.this.add(panel);
            panels.insert(panel, index);

            // Attach the attributes
            panel.setAttributes(new AccordionAttributes());

            // Update the selection
            if (selectedIndex >= index) {
                selectedIndex++;
            }

            accordionListeners.panelInserted(Accordion.this, index);
        }

        public Component update(int index, Component panel) {
            throw new UnsupportedOperationException();
        }

        public int remove(Component panel) {
            int index = indexOf(panel);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        public Sequence<Component> remove(int index, int count) {
            // Remove the panels from the panel list
            Sequence<Component> removed = panels.remove(index, count);

            // Detach the attributes
            for (int i = 0, n = removed.getLength(); i < n; i++) {
                removed.get(i).setAttributes(null);
            }

            // Update the selection
            if (selectedIndex >= index) {
                if (selectedIndex < index + count) {
                    selectedIndex = -1;
                } else {
                    selectedIndex -= count;
                }
            }

            accordionListeners.panelsRemoved(Accordion.this, index, removed);

            // Remove the panels from the component list
            for (int i = 0, n = removed.getLength(); i < n; i++) {
                Component panel = removed.get(i);
                Accordion.this.remove(panel);
            }

            return removed;
        }

        public Component get(int index) {
            return panels.get(index);
        }

        public int indexOf(Component panel) {
            return panels.indexOf(panel);
        }

        public int getLength() {
            return panels.getLength();
        }

        public Iterator<Component> iterator() {
            return new ImmutableIterator<Component>(panels.iterator());
        }
    }

    private static class AccordionListenerList extends ListenerList<AccordionListener>
        implements AccordionListener {
        public void panelInserted(Accordion accordion, int index) {
            for (AccordionListener listener : this) {
                listener.panelInserted(accordion, index);
            }
        }

        public void panelsRemoved(Accordion accordion, int index, Sequence<Component> panels) {
            for (AccordionListener listener : this) {
                listener.panelsRemoved(accordion, index, panels);
            }
        }
    }

    private static class AccordionSelectionListenerList extends ListenerList<AccordionSelectionListener>
        implements AccordionSelectionListener {
    	public Vote previewSelectedIndexChange(Accordion accordion, int selectedIndex) {
            Vote vote = Vote.APPROVE;

            for (AccordionSelectionListener listener : this) {
                vote = vote.tally(listener.previewSelectedIndexChange(accordion, selectedIndex));
            }

            return vote;
    	}

    	public void selectedIndexChangeVetoed(Accordion accordion, Vote reason) {
            for (AccordionSelectionListener listener : this) {
                listener.selectedIndexChangeVetoed(accordion, reason);
            }
    	}

    	public void selectedIndexChanged(Accordion accordion, int previousSelectedIndex) {
            for (AccordionSelectionListener listener : this) {
                listener.selectedIndexChanged(accordion, previousSelectedIndex);
            }
        }
    }

    private static class AccordionAttributeListenerList extends ListenerList<AccordionAttributeListener>
        implements AccordionAttributeListener {
        public void nameChanged(Accordion accordion, Component component, String previousName) {
            for (AccordionAttributeListener listener : this) {
                listener.nameChanged(accordion, component, previousName);
            }
        }

        public void iconChanged(Accordion accordion, Component component, Image previousIcon) {
            for (AccordionAttributeListener listener : this) {
                listener.iconChanged(accordion, component, previousIcon);
            }
        }
    }

    private int selectedIndex = -1;

    private ArrayList<Component> panels = new ArrayList<Component>();
    private PanelSequence panelSequence = new PanelSequence();

    private AccordionListenerList accordionListeners = new AccordionListenerList();
    private AccordionSelectionListenerList accordionSelectionListeners = new AccordionSelectionListenerList();
    private AccordionAttributeListenerList accordionAttributeListeners = new AccordionAttributeListenerList();

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

    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);
            if (component.getAttributes() != null) {
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

    public static String getName(Component component) {
        AccordionAttributes accordionAttributes = (AccordionAttributes)component.getAttributes();
        return (accordionAttributes == null) ? null : accordionAttributes.getName();
    }

    public static void setName(Component component, String name) {
        AccordionAttributes accordionAttributes = (AccordionAttributes)component.getAttributes();
        if (accordionAttributes == null) {
            throw new IllegalStateException();
        }

        accordionAttributes.setName(name);
    }

    public static Image getIcon(Component component) {
        AccordionAttributes accordionAttributes = (AccordionAttributes)component.getAttributes();
        return (accordionAttributes == null) ? null : accordionAttributes.getIcon();
    }

    public static void setIcon(Component component, Image icon) {
        AccordionAttributes accordionAttributes = (AccordionAttributes)component.getAttributes();
        if (accordionAttributes == null) {
            throw new IllegalStateException();
        }

        accordionAttributes.setIcon(icon);
    }

    public static final void setIcon(Component component, URL icon) {
        if (icon == null) {
            throw new IllegalArgumentException("icon is null.");
        }

        Image iconImage = (Image)ApplicationContext.getResourceCache().get(icon);

        if (iconImage == null) {
            iconImage = Image.load(icon);
            ApplicationContext.getResourceCache().put(icon, iconImage);
        }

        setIcon(component, iconImage);
    }

    public static final void setIcon(Component component, String icon) {
        if (icon == null) {
            throw new IllegalArgumentException("icon is null.");
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        setIcon(component, classLoader.getResource(icon));
    }
}
