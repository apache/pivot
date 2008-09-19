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
import pivot.wtk.media.Image;

/**
 * <p>Container that provides access to a set of components via selectable
 * tabs.</p>
 *
 * @author gbrown
 */
@ComponentInfo(icon="TabPane.png")
public class TabPane extends Container {
    /**
     * <p>Defines tab attributes.</p>
     *
     * @author gbrown
     */
    protected static class TabPaneAttributes extends Attributes {
        private String name = null;
        private Image icon = null;

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
    }

    /**
     * <p>Tab sequence implementation.</p>
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
            return new ImmutableIterator<Component>(tabs.iterator());
        }
    }

    private static class TabPaneListenerList extends ListenerList<TabPaneListener>
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

        public void cornerChanged(TabPane tabPane, Component previousCorner) {
            for (TabPaneListener listener : this) {
                listener.cornerChanged(tabPane, previousCorner);
            }
        }
    }

    private static class TabPaneSelectionListenerList extends ListenerList<TabPaneSelectionListener>
        implements TabPaneSelectionListener {
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
    }

    private Orientation tabOrientation = Orientation.HORIZONTAL;
    private boolean collapsible = false;
    private int selectedIndex = -1;

    private ArrayList<Component> tabs = new ArrayList<Component>();
    private TabSequence tabSequence = new TabSequence();

    private Component corner = null;

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

    public void setTabOrientation(String tabOrientation) {
        if (tabOrientation == null) {
            throw new IllegalArgumentException("tabOrientation is null.");
        }

        setTabOrientation(Orientation.decode(tabOrientation));
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

    protected void insertTab(Component tab, int index) {
        if (tab == null) {
            throw new IllegalArgumentException("tab is null.");
        }

        if (tab.getParent() != null) {
            throw new IllegalArgumentException("Tab already has a parent.");
        }

        // Add the tab to the component sequence
        add(tab);
        tabs.insert(tab, index);

        // Attach the attributes
        tab.setAttributes(new TabPaneAttributes());

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

        // Detach the attributes
        for (int i = 0, n = removed.getLength(); i < n; i++) {
            removed.get(i).setAttributes(null);
        }

        tabPaneListeners.tabsRemoved(this, index, removed);

        // Remove the tabs from the component list
        for (int i = 0, n = removed.getLength(); i < n; i++) {
            Component tab = removed.get(i);
            remove(tab);
        }

        return removed;
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

