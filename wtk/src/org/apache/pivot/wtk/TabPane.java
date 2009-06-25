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

import java.net.URL;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.ThreadUtilities;
import org.apache.pivot.util.Vote;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.media.Image;


/**
 * Container that provides access to a set of components via selectable tabs,
 * only one of which is visible at a time.
 * <p>
 * TODO Add a getTabAt() method that delegates to the skin.
 *
 * @author gbrown
 */
public class TabPane extends Container {
    /**
     * Defines tab attributes.
     *
     * @author gbrown
     */
    protected static class TabPaneAttributes extends Attributes {
        private String name = null;
        private Image icon = null;
        private boolean closeable = false;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            String previousName = this.name;
            this.name = name;

            Component component = getComponent();
            TabPane tabPane = (TabPane)component.getParent();
            if (tabPane != null) {
                tabPane.tabPaneAttributeListeners.nameChanged(tabPane, component, previousName);
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

        public boolean isCloseable() {
            return closeable;
        }

        public void setCloseable(boolean closeable) {
            Component component = getComponent();
            TabPane tabPane = (TabPane)component.getParent();
            if (tabPane != null) {
                tabPane.tabPaneAttributeListeners.closeableChanged(tabPane, component);
            }
        }
    }

    /**
     * Tab sequence implementation.
     *
     * @author gbrown
     */
    public final class TabSequence implements Sequence<Component>, Iterable<Component> {
        private TabSequence() {
        }

        public int add(Component tab) {
            int i = getLength();
            insert(tab, i);

            return i;
        }

        public void insert(Component tab, int index) {
            if (tab == null) {
                throw new IllegalArgumentException("tab is null.");
            }

            if (tab.getParent() != null) {
                throw new IllegalArgumentException("Tab already has a parent.");
            }

            // Add the tab to the component sequence
            TabPane.this.add(tab);
            tabs.insert(tab, index);

            // Attach the attributes
            tab.setAttributes(new TabPaneAttributes());

            // Update the selection
            if (selectedIndex >= index) {
                selectedIndex++;
            }

            tabPaneListeners.tabInserted(TabPane.this, index);
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
            // Remove the tabs from the tab list
            Sequence<Component> removed = tabs.remove(index, count);

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

            tabPaneListeners.tabsRemoved(TabPane.this, index, removed);

            // Remove the tabs from the component list
            for (int i = 0, n = removed.getLength(); i < n; i++) {
                Component tab = removed.get(i);
                TabPane.this.remove(tab);
            }

            return removed;
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
            return new ImmutableIterator<Component>(tabs.iterator());
        }
    }

    private static class TabPaneListenerList extends ListenerList<TabPaneListener>
        implements TabPaneListener {
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

        public void cornerChanged(TabPane tabPane, Component previousCorner) {
            for (TabPaneListener listener : this) {
                listener.cornerChanged(tabPane, previousCorner);
            }
        }
    }

    private static class TabPaneSelectionListenerList extends ListenerList<TabPaneSelectionListener>
        implements TabPaneSelectionListener {
        public Vote previewSelectedIndexChange(TabPane tabPane, int selectedIndex) {
            Vote vote = Vote.APPROVE;

            for (TabPaneSelectionListener listener : this) {
                vote = vote.tally(listener.previewSelectedIndexChange(tabPane, selectedIndex));
            }

            return vote;
        }

        public void selectedIndexChangeVetoed(TabPane tabPane, Vote reason) {
            for (TabPaneSelectionListener listener : this) {
                listener.selectedIndexChangeVetoed(tabPane, reason);
            }
        }

        public void selectedIndexChanged(TabPane tabPane, int previousSelectedIndex) {
            for (TabPaneSelectionListener listener : this) {
                listener.selectedIndexChanged(tabPane, previousSelectedIndex);
            }
        }
    }

    private static class TabPaneAttributeListenerList extends ListenerList<TabPaneAttributeListener>
        implements TabPaneAttributeListener {
        public void nameChanged(TabPane tabPane, Component component, String previousName) {
            for (TabPaneAttributeListener listener : this) {
                listener.nameChanged(tabPane, component, previousName);
            }
        }

        public void iconChanged(TabPane tabPane, Component component, Image previousIcon) {
            for (TabPaneAttributeListener listener : this) {
                listener.iconChanged(tabPane, component, previousIcon);
            }
        }

        public void closeableChanged(TabPane tabPane, Component component) {
            for (TabPaneAttributeListener listener : this) {
                listener.closeableChanged(tabPane, component);
            }
        }
    }

    private int selectedIndex = -1;

    private ArrayList<Component> tabs = new ArrayList<Component>();
    private TabSequence tabSequence = new TabSequence();

    private Component corner = null;

    private TabPaneListenerList tabPaneListeners = new TabPaneListenerList();
    private TabPaneSelectionListenerList tabPaneSelectionListeners = new TabPaneSelectionListenerList();
    private TabPaneAttributeListenerList tabPaneAttributeListeners = new TabPaneAttributeListenerList();

    public TabPane() {
        super();
        installSkin(TabPane.class);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        int previousSelectedIndex = this.selectedIndex;

        if (previousSelectedIndex != selectedIndex) {
            Vote vote = tabPaneSelectionListeners.previewSelectedIndexChange(this, selectedIndex);

            if (vote == Vote.APPROVE) {
                this.selectedIndex = selectedIndex;
                tabPaneSelectionListeners.selectedIndexChanged(this, previousSelectedIndex);
            } else {
                tabPaneSelectionListeners.selectedIndexChangeVetoed(this, vote);
            }
        }
    }

    public Component getSelectedTab() {
        return (selectedIndex == -1) ? null : tabs.get(selectedIndex);
    }

    public TabSequence getTabs() {
        return tabSequence;
    }

    public Component getCorner() {
        return corner;
    }

    public void setCorner(Component corner) {
        Component previousCorner = this.corner;

        if (previousCorner != corner) {
            // Remove any previous corner component
            this.corner = null;

            if (previousCorner != null) {
                remove(previousCorner);
            }

            // Set the new corner component
            if (corner != null) {
                insert(corner, 0);
            }

            this.corner = corner;

            tabPaneListeners.cornerChanged(this, previousCorner);
        }
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);
            if (component == corner
                || component.getAttributes() != null) {
                throw new UnsupportedOperationException();
            }
        }

        // Call the base method to remove the components
        return super.remove(index, count);
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

    public static String getName(Component component) {
        TabPaneAttributes tabPaneAttributes = (TabPaneAttributes)component.getAttributes();
        return (tabPaneAttributes == null) ? null : tabPaneAttributes.getName();
    }

    public static void setName(Component component, String name) {
        TabPaneAttributes tabPaneAttributes = (TabPaneAttributes)component.getAttributes();
        if (tabPaneAttributes == null) {
            throw new IllegalStateException();
        }

        tabPaneAttributes.setName(name);
    }

    public static Image getIcon(Component component) {
        TabPaneAttributes tabPaneAttributes = (TabPaneAttributes)component.getAttributes();
        return (tabPaneAttributes == null) ? null : tabPaneAttributes.getIcon();
    }

    public static void setIcon(Component component, Image icon) {
        TabPaneAttributes tabPaneAttributes = (TabPaneAttributes)component.getAttributes();
        if (tabPaneAttributes == null) {
            throw new IllegalStateException();
        }

        tabPaneAttributes.setIcon(icon);
    }

    public static final void setIcon(Component component, URL icon) {
        if (icon == null) {
            throw new IllegalArgumentException("icon is null.");
        }

        Image iconImage = (Image)ApplicationContext.getResourceCache().get(icon);

        if (iconImage == null) {
            try {
                iconImage = Image.load(icon);
            } catch (TaskExecutionException exception) {
                throw new IllegalArgumentException(exception);
            }

            ApplicationContext.getResourceCache().put(icon, iconImage);
        }

        setIcon(component, iconImage);
    }

    public static final void setIcon(Component component, String icon) {
        if (icon == null) {
            throw new IllegalArgumentException("icon is null.");
        }

        ClassLoader classLoader = ThreadUtilities.getClassLoader();
        setIcon(component, classLoader.getResource(icon));
    }

    public static boolean isCloseable(Component component) {
        TabPaneAttributes tabPaneAttributes = (TabPaneAttributes)component.getAttributes();
        return (tabPaneAttributes == null) ? null : tabPaneAttributes.isCloseable();
    }

    public static void setCloseable(Component component, boolean closeable) {
        TabPaneAttributes tabPaneAttributes = (TabPaneAttributes)component.getAttributes();
        if (tabPaneAttributes == null) {
            throw new IllegalStateException();
        }

        tabPaneAttributes.setCloseable(closeable);
    }
}

