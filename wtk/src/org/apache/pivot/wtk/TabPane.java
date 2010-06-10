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
import org.apache.pivot.wtkx.DefaultProperty;


/**
 * Container that provides access to a set of components via selectable tabs,
 * only one of which is visible at a time.
 * <p>
 * TODO Add a getTabAt() method that delegates to the skin.
 */
@DefaultProperty("tabs")
public class TabPane extends Container {
    /**
     * Tab sequence implementation.
     */
    public final class TabSequence implements Sequence<Component>, Iterable<Component> {
        private TabSequence() {
        }

        @Override
        public int add(Component tab) {
            int i = getLength();
            insert(tab, i);

            return i;
        }

        @Override
        public void insert(Component tab, int index) {
            if (tab == null) {
                throw new IllegalArgumentException("tab is null.");
            }

            // Add the tab to the component sequence
            TabPane.this.add(tab);
            tabs.insert(tab, index);

            // Update the selection
            if (selectedIndex >= index) {
                selectedIndex++;
            }

            tabPaneListeners.tabInserted(TabPane.this, index);
        }

        @Override
        public Component update(int index, Component tab) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(Component tab) {
            int index = indexOf(tab);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<Component> remove(int index, int count) {
            // Remove the tabs from the tab list
            Sequence<Component> removed = tabs.remove(index, count);

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

        @Override
        public Component get(int index) {
            return tabs.get(index);
        }

        @Override
        public int indexOf(Component tab) {
            return tabs.indexOf(tab);
        }

        @Override
        public int getLength() {
            return tabs.getLength();
        }

        @Override
        public Iterator<Component> iterator() {
            return new ImmutableIterator<Component>(tabs.iterator());
        }
    }

    private enum Attribute {
        LABEL,
        ICON,
        CLOSEABLE,
        TOOLTIP;
    }

    private static class TabPaneListenerList extends ListenerList<TabPaneListener>
        implements TabPaneListener {
        @Override
        public void tabInserted(TabPane tabPane, int index) {
            for (TabPaneListener listener : this) {
                listener.tabInserted(tabPane, index);
            }
        }

        @Override
        public void tabsRemoved(TabPane tabPane, int index, Sequence<Component> tabs) {
            for (TabPaneListener listener : this) {
                listener.tabsRemoved(tabPane, index, tabs);
            }
        }

        @Override
        public void cornerChanged(TabPane tabPane, Component previousCorner) {
            for (TabPaneListener listener : this) {
                listener.cornerChanged(tabPane, previousCorner);
            }
        }
    }

    private static class TabPaneSelectionListenerList extends ListenerList<TabPaneSelectionListener>
        implements TabPaneSelectionListener {
        @Override
        public Vote previewSelectedIndexChange(TabPane tabPane, int selectedIndex) {
            Vote vote = Vote.APPROVE;

            for (TabPaneSelectionListener listener : this) {
                vote = vote.tally(listener.previewSelectedIndexChange(tabPane, selectedIndex));
            }

            return vote;
        }

        @Override
        public void selectedIndexChangeVetoed(TabPane tabPane, Vote reason) {
            for (TabPaneSelectionListener listener : this) {
                listener.selectedIndexChangeVetoed(tabPane, reason);
            }
        }

        @Override
        public void selectedIndexChanged(TabPane tabPane, int previousSelectedIndex) {
            for (TabPaneSelectionListener listener : this) {
                listener.selectedIndexChanged(tabPane, previousSelectedIndex);
            }
        }
    }

    private static class TabPaneAttributeListenerList extends ListenerList<TabPaneAttributeListener>
        implements TabPaneAttributeListener {
        @Override
        public void labelChanged(TabPane tabPane, Component component, String previousLabel) {
            for (TabPaneAttributeListener listener : this) {
                listener.labelChanged(tabPane, component, previousLabel);
            }
        }

        @Override
        public void iconChanged(TabPane tabPane, Component component, Image previousIcon) {
            for (TabPaneAttributeListener listener : this) {
                listener.iconChanged(tabPane, component, previousIcon);
            }
        }

        @Override
        public void closeableChanged(TabPane tabPane, Component component) {
            for (TabPaneAttributeListener listener : this) {
                listener.closeableChanged(tabPane, component);
            }
        }

        @Override
        public void tooltipTextChanged(TabPane tabPane, Component component, String previousTooltipText) {
            for (TabPaneAttributeListener listener : this) {
                listener.tooltipTextChanged(tabPane, component, previousTooltipText);
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
        installThemeSkin(TabPane.class);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        if (selectedIndex < -1
            || selectedIndex > tabs.getLength() - 1) {
            throw new IndexOutOfBoundsException();
        }

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
                || tabs.indexOf(component) >= 0) {
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

    public static String getLabel(Component component) {
        return (String)component.getAttribute(Attribute.LABEL);
    }

    public static void setLabel(Component component, String label) {
        String previousLabel = (String)component.setAttribute(Attribute.LABEL, label);

        if (previousLabel != label) {
            Container parent = component.getParent();

            if (parent instanceof TabPane) {
                TabPane tabPane = (TabPane)parent;
                tabPane.tabPaneAttributeListeners.labelChanged(tabPane, component, previousLabel);
            }
        }
    }

    public static Image getIcon(Component component) {
        return (Image)component.getAttribute(Attribute.ICON);
    }

    public static void setIcon(Component component, Image icon) {
        Image previousIcon = (Image)component.setAttribute(Attribute.ICON, icon);

        if (previousIcon != icon) {
            Container parent = component.getParent();

            if (parent instanceof TabPane) {
                TabPane tabPane = (TabPane)parent;
                tabPane.tabPaneAttributeListeners.iconChanged(tabPane, component, previousIcon);
            }
        }
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
        Boolean value = (Boolean)component.getAttribute(Attribute.CLOSEABLE);
        return (value == null) ? false : value;
    }

    public static void setCloseable(Component component, boolean closeable) {
        Boolean previousValue = (Boolean)component.setAttribute(Attribute.CLOSEABLE, closeable);
        boolean previousCloseable = (previousValue == null) ? false : previousValue;

        if (previousCloseable != closeable) {
            Container parent = component.getParent();

            if (parent instanceof TabPane) {
                TabPane tabPane = (TabPane)parent;
                tabPane.tabPaneAttributeListeners.closeableChanged(tabPane, component);
            }
        }
    }

    /**
     * Returns the tab component's tooltip text.
     *
     * @return
     * The tab component's tooltip text, or <tt>null</tt> if no tooltip is
     * specified.
     */
    public static String getTooltipText(Component component) {
        return (String)component.getAttribute(Attribute.TOOLTIP);
    }

    /**
     * Sets the tab component's tooltip text.
     *
     * @param tooltipText
     * The tab component's tooltip text, or <tt>null</tt> for no tooltip.
     */
    public static void setTooltipText(Component component, String tooltipText) {
        String previousTooltipText = (String)component.setAttribute(Attribute.TOOLTIP, tooltipText);

        if (previousTooltipText != tooltipText) {
            Container parent = component.getParent();

            if (parent instanceof TabPane) {
                TabPane tabPane = (TabPane)parent;
                tabPane.tabPaneAttributeListeners.tooltipTextChanged(tabPane, component, previousTooltipText);
            }
        }
    }
}

