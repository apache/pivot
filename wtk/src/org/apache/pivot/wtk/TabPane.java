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
import org.apache.pivot.wtk.content.ButtonDataRenderer;
import org.apache.pivot.wtk.skin.TabPaneSkin;

/**
 * Container that provides access to a set of components via selectable tabs,
 * only one of which is visible at a time.
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
            int index = getLength();
            insert(tab, index);

            return index;
        }

        @Override
        public void insert(Component tab, int index) {
            Utils.checkNull(tab, "Tab component");

            // Add the tab to the component sequence
            TabPane.this.add(tab);
            tabs.insert(tab, index);

            // Update the selection
            int previousSelectedIndex = selectedIndex;

            if (selectedIndex >= index) {
                selectedIndex++;
            }

            // Fire insert event
            tabPaneListeners.tabInserted(TabPane.this, index);

            // Fire selection change event, if necessary
            if (selectedIndex != previousSelectedIndex && previousSelectedIndex > -1) {
                tabPaneSelectionListeners.selectedIndexChanged(TabPane.this, selectedIndex);
            }
        }

        @Override
        @UnsupportedOperation
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
            Sequence<Component> removed;

            Vote vote = tabPaneListeners.previewRemoveTabs(TabPane.this, index, count);
            if (vote == Vote.APPROVE) {
                // Remove the tabs from the tab list
                removed = tabs.remove(index, count);

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
                tabPaneListeners.tabsRemoved(TabPane.this, index, removed);

                // Remove the tabs from the component list
                for (int i = 0, n = removed.getLength(); i < n; i++) {
                    Component tab = removed.get(i);
                    TabPane.this.remove(tab);
                }

                // Fire selection change event, if necessary
                if (selectedIndex != previousSelectedIndex && previousSelectedIndex > -1) {
                    tabPaneSelectionListeners.selectedIndexChanged(TabPane.this, selectedIndex);
                }
            } else {
                removed = null;
                tabPaneListeners.removeTabsVetoed(TabPane.this, vote);
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
            return new ImmutableIterator<>(tabs.iterator());
        }
    }

    /**
     * Tab pane skin interface. Tab pane skins must implement this interface to
     * facilitate additional communication between the component and the skin.
     */
    public interface Skin {
        boolean isVisible(int index);

        void setVisible(int index, boolean value);
    }

    private enum Attribute {
        TAB_DATA, TOOLTIP_TEXT;
    }

    private ArrayList<Component> tabs = new ArrayList<>();
    private TabSequence tabSequence = new TabSequence();
    private Component corner = null;
    private int selectedIndex = -1;
    private Button.DataRenderer tabDataRenderer = DEFAULT_TAB_DATA_RENDERER;
    private boolean closeable = false;
    private boolean collapsible = false;

    private TabPaneListener.Listeners tabPaneListeners = new TabPaneListener.Listeners();
    private TabPaneSelectionListener.Listeners tabPaneSelectionListeners = new TabPaneSelectionListener.Listeners();
    private TabPaneAttributeListener.Listeners tabPaneAttributeListeners = new TabPaneAttributeListener.Listeners();

    private static final Button.DataRenderer DEFAULT_TAB_DATA_RENDERER = new ButtonDataRenderer();

    public TabPane() {
        super();
        installSkin(TabPane.class);
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

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        indexBoundsCheck("selectedIndex", selectedIndex, -1, tabs.getLength() - 1);

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

    public void setSelectedTab(Component comp) {
        if (comp == null) {
            setSelectedIndex(-1);
        } else {
            int index = tabs.indexOf(comp);
            if (index < 0) {
                throw new IllegalArgumentException("Component is not a child of the TabPane");
            }
            setSelectedIndex(index);
        }
    }

    public Button.DataRenderer getTabDataRenderer() {
        return tabDataRenderer;
    }

    public void setTabDataRenderer(Button.DataRenderer tabDataRenderer) {
        Utils.checkNull(tabDataRenderer, "Tab data renderer");

        Button.DataRenderer previousTabDataRenderer = this.tabDataRenderer;
        if (previousTabDataRenderer != tabDataRenderer) {
            this.tabDataRenderer = tabDataRenderer;
            tabPaneListeners.tabDataRendererChanged(this, previousTabDataRenderer);
        }
    }

    public boolean isCloseable() {
        return closeable;
    }

    public void setCloseable(boolean closeable) {
        if (this.closeable != closeable) {
            this.closeable = closeable;
            tabPaneListeners.closeableChanged(this);
        }
    }

    /**
     * @return <tt>true</tt> if the TabPane is collapsible and no tab is
     * selected; <tt>false</tt>, otherwise.
     * @see #isCollapsible()
     * @see #getSelectedIndex()
     */
    public boolean isCollapsed() {
        return collapsible && (selectedIndex == -1);
    }

    /**
     * Collapse or expand the TabPane (if it is collapsible).
     *
     * @param collapsed <tt>true</tt> to collapse, <tt>false</tt> to expand and
     * select the first tab. Use {@link #setSelectedIndex(int)} to expand and
     * select a specific Tab.
     * @see #isCollapsible()
     * @see #setSelectedIndex(int)
     */
    public void setCollapsed(boolean collapsed) {
        if (collapsible && (isCollapsed() != collapsed)) {
            int index = (collapsed || tabs.getLength() == 0) ? -1 : 0;
            setSelectedIndex(index);
        }
    }

    public boolean isCollapsible() {
        return collapsible;
    }

    public void setCollapsible(boolean collapsible) {
        if (this.collapsible != collapsible) {
            this.collapsible = collapsible;
            tabPaneListeners.collapsibleChanged(this);
        }
    }

    public boolean isTabVisible(int index) {
        TabPane.Skin tabPaneSkin = (TabPane.Skin) getSkin();
        if (tabPaneSkin instanceof TabPaneSkin) {
            return ((TabPaneSkin) tabPaneSkin).isVisible(index);
        }
        return true;
    }

    public void setTabVisible(int index, boolean value) {
        TabPane.Skin tabPaneSkin = (TabPane.Skin) getSkin();
        if (tabPaneSkin instanceof TabPaneSkin) {
            ((TabPaneSkin) tabPaneSkin).setVisible(index, value);
        }
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);

            if (component == corner || tabs.indexOf(component) >= 0) {
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

    public static Object getTabData(Component component) {
        return component.getAttribute(Attribute.TAB_DATA);
    }

    public static void setTabData(Component component, Object tabData) {
        Object previousTabData = component.setAttribute(Attribute.TAB_DATA, tabData);

        if (previousTabData != tabData) {
            Container parent = component.getParent();

            if (parent instanceof TabPane) {
                TabPane tabPane = (TabPane) parent;
                tabPane.tabPaneAttributeListeners.tabDataChanged(tabPane, component,
                    previousTabData);
            }
        }
    }

    public static String getTooltipText(Component component) {
        return (String) component.getAttribute(Attribute.TOOLTIP_TEXT);
    }

    public static void setTooltipText(Component component, String tooltipText) {
        String previousTooltipText = (String) component.setAttribute(Attribute.TOOLTIP_TEXT,
            tooltipText);

        if (previousTooltipText != tooltipText) {
            Container parent = component.getParent();

            if (parent instanceof TabPane) {
                TabPane tabPane = (TabPane) parent;
                tabPane.tabPaneAttributeListeners.tooltipTextChanged(tabPane, component,
                    previousTooltipText);
            }
        }
    }
}
