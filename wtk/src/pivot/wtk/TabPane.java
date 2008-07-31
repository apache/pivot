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
import pivot.util.ListenerList;
import pivot.wtk.media.Image;

/**
 * TODO Add a corner component.
 *
 * @author gbrown
 */
@ComponentInfo(icon="TabPane.png")
public class TabPane extends Container {
    public static class TabPaneAttributes extends Attributes {
        private String label = null;
        private Image icon = null;
        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            String previousLabel = this.label;
            this.label = label;

            Component component = getComponent();
            TabPane tabPane = (TabPane)component.getParent();
            if (tabPane != null) {
                tabPane.tabPaneAttributeListeners.labelChanged(tabPane, component, previousLabel);
            }
        }

        public Image getIcon() {
            return icon;
        }

        public void setIcon(Image icon) {
            Image previousIcon = this.icon;
            this.icon = icon;

            Component component = getComponent();
            TabPane tabPane = (TabPane)component.getParent();
            if (tabPane != null) {
                tabPane.tabPaneAttributeListeners.iconChanged(tabPane, component, previousIcon);
            }
        }
    }

    public final class TabSequence implements Sequence<Component>,
        Iterable<Component> {
        private class TabIterator implements Iterator<Component> {
            Iterator<Component> source = null;

            public TabIterator(Iterator<Component> source) {
                this.source = source;
            }

            public boolean hasNext() {
                return source.hasNext();
            }

            public Component next() {
                return source.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        public int add(Component tab) {
            int i = getLength();
            insert(tab, i);

            return i;
        }

        public void insert(Component tab, int index) {
            insertTab(tab, index);
        }

        public Component update(int index, Component tab) {
            throw new UnsupportedOperationException();
        }

        public int remove(Component tab) {
            int index = indexOf(tab);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        public Sequence<Component> remove(int index, int count) {
            return removeTabs(index, count);
        }

        public Component get(int index) {
            return tabs.get(index);
        }

        public int indexOf(Component tab) {
            return tabs.indexOf(tab);
        }

        public int getLength() {
            return tabs.getLength();
        }

        public Iterator<Component> iterator() {
            return new TabIterator(tabs.iterator());
        }
    }

    private class TabPaneListenerList extends ListenerList<TabPaneListener>
        implements TabPaneListener {
        public void tabOrientationChanged(TabPane tabPane) {
            for (TabPaneListener listener : this) {
                listener.tabOrientationChanged(tabPane);
            }
        }

        public void tabInserted(TabPane tabPane, int index) {
            for (TabPaneListener listener : this) {
                listener.tabInserted(tabPane, index);
            }
        }

        public void tabsRemoved(TabPane tabPane, int index, Sequence<Component> tabs) {
            for (TabPaneListener listener : this) {
                listener.tabsRemoved(tabPane, index, tabs);
            }
        }

        public void collapsibleChanged(TabPane tabPane) {
            for (TabPaneListener listener : this) {
                listener.collapsibleChanged(tabPane);
            }
        }
    }

    private class TabPaneSelectionListenerList extends ListenerList<TabPaneSelectionListener>
        implements TabPaneSelectionListener {
        public void selectedIndexChanged(TabPane tabPane, int previousSelectedIndex) {
            for (TabPaneSelectionListener listener : this) {
                listener.selectedIndexChanged(tabPane, previousSelectedIndex);
            }
        }
    }

    private class TabPaneAttributeListenerList extends ListenerList<TabPaneAttributeListener>
        implements TabPaneAttributeListener {
        public void iconChanged(TabPane tabPane, Component component, Image previousIcon) {
            for (TabPaneAttributeListener listener : this) {
                listener.iconChanged(tabPane, component, previousIcon);
            }
        }

        public void labelChanged(TabPane tabPane, Component component, String previousLabel) {
            for (TabPaneAttributeListener listener : this) {
                listener.labelChanged(tabPane, component, previousLabel);
            }
        }

    }

    private Orientation tabOrientation = Orientation.HORIZONTAL;
    private boolean collapsible = false;
    private int selectedIndex = -1;

    private ArrayList<Component> tabs = new ArrayList<Component>();
    private TabSequence tabSequence = new TabSequence();

    private TabPaneListenerList tabPaneListeners = new TabPaneListenerList();
    private TabPaneSelectionListenerList tabPaneSelectionListeners = new TabPaneSelectionListenerList();
    private TabPaneAttributeListenerList tabPaneAttributeListeners = new TabPaneAttributeListenerList();

    public TabPane() {
        this(false);
    }

    public TabPane(boolean collapsible) {
        super();

        this.collapsible = collapsible;

        installSkin(TabPane.class);
    }

    public Orientation getTabOrientation() {
        return tabOrientation;
    }

    public void setTabOrientation(Orientation tabOrientation) {
        if (tabOrientation == null) {
            throw new IllegalArgumentException("tabOrientation is null.");
        }

        if (this.tabOrientation != tabOrientation) {
            this.tabOrientation = tabOrientation;
            tabPaneListeners.tabOrientationChanged(this);
        }
    }

    public boolean isCollapsible() {
        return collapsible;
    }

    public void setCollapsible(boolean collapsible) {
        if (collapsible != this.collapsible) {
            this.collapsible = collapsible;
            tabPaneListeners.collapsibleChanged(this);
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        int previousSelectedIndex = this.selectedIndex;

        if (previousSelectedIndex != selectedIndex) {
            this.selectedIndex = selectedIndex;
            tabPaneSelectionListeners.selectedIndexChanged(this, previousSelectedIndex);
        }
    }

    public TabSequence getTabs() {
        return tabSequence;
    }

    protected void insertTab(Component tab, int index) {
        if (tab == null) {
            throw new IllegalArgumentException("tab is null.");
        }

        if (tab.getParent() != null) {
            throw new IllegalArgumentException("Tab already has a parent.");
        }

        // Add the tab to the component sequence
        getComponents().add(tab);
        tabs.insert(tab, index);

        tabPaneListeners.tabInserted(this, index);

        // If the selected tab's index changed as a result of
        // this insertion, update it
        if (selectedIndex >= index) {
            setSelectedIndex(selectedIndex + 1);
        }
    }

    protected Sequence<Component> removeTabs(int index, int count) {
        // If the selected tab is being removed, clear the selection
        if (selectedIndex >= index
            && selectedIndex < index + count) {
            setSelectedIndex(-1);
        }

        // Remove the tabs from the tab list
        Sequence<Component> removed = tabs.remove(index, count);

        tabPaneListeners.tabsRemoved(this, index, removed);

        // Remove the tabs from the component list
        Sequence<Component> components = getComponents();
        for (int i = 0, n = removed.getLength(); i < n; i++) {
            Component tab = removed.get(i);
            components.remove(tab);
        }

        return removed;
    }

    public ListenerList<TabPaneListener> getTabPaneListeners() {
        return tabPaneListeners;
    }

    public ListenerList<TabPaneSelectionListener> getTabPaneSelectionListeners() {
        return tabPaneSelectionListeners;
    }

    public ListenerList<TabPaneAttributeListener> getTabPaneAttributeListeners() {
        return tabPaneAttributeListeners;
    }

    public static String getLabel(Component component) {
        TabPaneAttributes tabPaneAttributes =
            (TabPaneAttributes)component.getAttributes().get(TabPaneAttributes.class);
        return (tabPaneAttributes == null) ? null : tabPaneAttributes.getLabel();
    }

    public static void setLabel(Component component, String label) {
        TabPaneAttributes tabPaneAttributes =
            (TabPaneAttributes)component.getAttributes().get(TabPaneAttributes.class);

        if (tabPaneAttributes == null) {
            tabPaneAttributes = new TabPaneAttributes();
            component.getAttributes().put(tabPaneAttributes);
        }

        tabPaneAttributes.setLabel(label);
    }

    public static Image getIcon(Component component) {
        TabPaneAttributes tabPaneAttributes =
            (TabPaneAttributes)component.getAttributes().get(TabPaneAttributes.class);
        return (tabPaneAttributes == null) ? null : tabPaneAttributes.getIcon();
    }

    public static void setIcon(Component component, Image icon) {
        TabPaneAttributes tabPaneAttributes =
            (TabPaneAttributes)component.getAttributes().get(TabPaneAttributes.class);

        if (tabPaneAttributes == null) {
            tabPaneAttributes = new TabPaneAttributes();
            component.getAttributes().put(tabPaneAttributes);
        }

        tabPaneAttributes.setIcon(icon);
    }

    public static final void setIcon(Component component, URL icon) {
        setIcon(component, Image.load(icon));
    }
}

