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
package org.apache.pivot.demos.explorer;

import java.io.IOException;
import java.net.URL;

import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence.Tree;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.tools.wtk.ComponentPropertyInspector;
import org.apache.pivot.tools.wtk.ComponentStyleInspector;
import org.apache.pivot.tools.wtk.EventLogger;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonGroupListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.ScrollPane.ScrollBarPolicy;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TabPaneSelectionListener;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;

public class ComponentExplorer implements Application {
    private Window window = null;
    private TreeView treeView = null;
    private TabPane contentTabPane = null;
    private ComponentPropertyInspector componentPropertyInspector = null;
    private ComponentStyleInspector componentStyleInspector = null;
    private EventLogger eventLogger = null;

    private ButtonGroup horizontalScrollBarPolicyGroup = null;
    private ButtonGroup verticalScrollBarPolicyGroup = null;
    private Button horizontalAutoButton = null;
    private Button horizontalFillButton = null;
    private Button horizontalFillToCapacityButton = null;
    private Button horizontalNeverButton = null;
    private Button verticalAutoButton = null;
    private Button verticalFillButton = null;
    private Button verticalFillToCapacityButton = null;
    private Button verticalNeverButton = null;

    public static final String CLASS_PROPERTY = "class";

    private static final String SRC_KEY = "src";
    private static final String HORIZONTAL_SCROLL_BAR_POLICY_KEY = "horizontalScrollBarPolicy";
    private static final String VERTICAL_SCROLL_BAR_POLICY_KEY = "verticalScrollBarPolicy";
    private static final String COMPONENT_KEY = "component";
    private static final String SCROLL_PANE_KEY = "scrollPane";

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        Resources resources = new Resources(getClass().getName());
        WTKXSerializer wtkxSerializer = new WTKXSerializer(resources);
        window = (Window)wtkxSerializer.readObject(this, "component_explorer.wtkx");

        treeView = wtkxSerializer.getValue("treeView");
        contentTabPane = wtkxSerializer.getValue("contentTabPane");
        componentPropertyInspector = wtkxSerializer.getValue("componentPropertyInspector");
        componentStyleInspector = wtkxSerializer.getValue("componentStyleInspector");
        eventLogger = wtkxSerializer.getValue("eventLogger");

        horizontalScrollBarPolicyGroup = wtkxSerializer.getValue("horizontalScrollBarPolicyGroup");
        verticalScrollBarPolicyGroup = wtkxSerializer.getValue("verticalScrollBarPolicyGroup");
        horizontalAutoButton = wtkxSerializer.getValue("horizontalAutoButton");
        horizontalFillButton = wtkxSerializer.getValue("horizontalFillButton");
        horizontalFillToCapacityButton = wtkxSerializer.getValue("horizontalFillToCapacityButton");
        horizontalNeverButton = wtkxSerializer.getValue("horizontalNeverButton");
        verticalAutoButton = wtkxSerializer.getValue("verticalAutoButton");
        verticalFillButton = wtkxSerializer.getValue("verticalFillButton");
        verticalFillToCapacityButton = wtkxSerializer.getValue("verticalFillToCapacityButton");
        verticalNeverButton = wtkxSerializer.getValue("verticalNeverButton");

        treeView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter() {
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y,
                int count) {
                if (button == Mouse.Button.LEFT && count == 2) {
                    Path path = treeView.getNodeAt(y);

                    if (path != null) {
                        List<?> treeData = treeView.getTreeData();
                        Object node = Tree.get(treeData, path);

                        if (node instanceof ComponentNode) {
                            open((ComponentNode)node);
                        } else if (node instanceof List<?>) {
                            treeView.setBranchExpanded(path, !treeView.isBranchExpanded(path));
                        }
                    }
                }

                return false;
            }
        });

        contentTabPane.getTabPaneSelectionListeners().add(new TabPaneSelectionListener.Adapter() {
            @Override
            public void selectedIndexChanged(TabPane tabPane, int previousSelectedIndex) {
                Component selectedTab = tabPane.getSelectedTab();

                if (selectedTab != null) {
                    Component component = (Component)selectedTab.getUserData().get(COMPONENT_KEY);
                    componentPropertyInspector.setSource(component);
                    componentStyleInspector.setSource(component);
                    eventLogger.setSource(component);

                    ScrollBarPolicy horizontalScrollBarPolicy = (ScrollBarPolicy)
                        selectedTab.getUserData().get(HORIZONTAL_SCROLL_BAR_POLICY_KEY);
                    switch (horizontalScrollBarPolicy) {
                    case AUTO:
                        horizontalScrollBarPolicyGroup.setSelection(horizontalAutoButton);
                        break;
                    case FILL:
                        horizontalScrollBarPolicyGroup.setSelection(horizontalFillButton);
                        break;
                    case FILL_TO_CAPACITY:
                        horizontalScrollBarPolicyGroup.setSelection(horizontalFillToCapacityButton);
                        break;
                    case NEVER:
                        horizontalScrollBarPolicyGroup.setSelection(horizontalNeverButton);
                        break;
                    }

                    ScrollBarPolicy verticalScrollBarPolicy = (ScrollBarPolicy)
                        selectedTab.getUserData().get(VERTICAL_SCROLL_BAR_POLICY_KEY);
                    switch (verticalScrollBarPolicy) {
                    case AUTO:
                        verticalScrollBarPolicyGroup.setSelection(verticalAutoButton);
                        break;
                    case FILL:
                        verticalScrollBarPolicyGroup.setSelection(verticalFillButton);
                        break;
                    case FILL_TO_CAPACITY:
                        verticalScrollBarPolicyGroup.setSelection(verticalFillToCapacityButton);
                        break;
                    case NEVER:
                        verticalScrollBarPolicyGroup.setSelection(verticalNeverButton);
                        break;
                    }
                }
            }
        });

        horizontalScrollBarPolicyGroup.getButtonGroupListeners().add
            (new ButtonGroupListener.Adapter() {
            @Override
            public void selectionChanged(ButtonGroup buttonGroup, Button previousSelection) {
                Component selectedTab = contentTabPane.getSelectedTab();

                if (selectedTab != null) {
                    Button button = buttonGroup.getSelection();

                    ScrollBarPolicy horizontalScrollBarPolicy = null;

                    if (button == horizontalAutoButton) {
                        horizontalScrollBarPolicy = ScrollBarPolicy.AUTO;
                    } else if (button == horizontalFillButton) {
                        horizontalScrollBarPolicy = ScrollBarPolicy.FILL;
                    } else if (button == horizontalFillToCapacityButton) {
                        horizontalScrollBarPolicy = ScrollBarPolicy.FILL_TO_CAPACITY;
                    } else if (button == horizontalNeverButton) {
                        horizontalScrollBarPolicy = ScrollBarPolicy.NEVER;
                    }

                    if (horizontalScrollBarPolicy != null) {
                        selectedTab.getUserData().put(HORIZONTAL_SCROLL_BAR_POLICY_KEY,
                            horizontalScrollBarPolicy);

                        ScrollPane scrollPane = (ScrollPane)selectedTab.getUserData().get
                            (SCROLL_PANE_KEY);
                        scrollPane.setHorizontalScrollBarPolicy(horizontalScrollBarPolicy);
                    }
                }
            }
        });

        verticalScrollBarPolicyGroup.getButtonGroupListeners().add
            (new ButtonGroupListener.Adapter() {
            @Override
            public void selectionChanged(ButtonGroup buttonGroup, Button previousSelection) {
                Component selectedTab = contentTabPane.getSelectedTab();

                if (selectedTab != null) {
                    Button button = buttonGroup.getSelection();

                    ScrollBarPolicy verticalScrollBarPolicy = null;

                    if (button == verticalAutoButton) {
                        verticalScrollBarPolicy = ScrollBarPolicy.AUTO;
                    } else if (button == verticalFillButton) {
                        verticalScrollBarPolicy = ScrollBarPolicy.FILL;
                    } else if (button == verticalFillToCapacityButton) {
                        verticalScrollBarPolicy = ScrollBarPolicy.FILL_TO_CAPACITY;
                    } else if (button == verticalNeverButton) {
                        verticalScrollBarPolicy = ScrollBarPolicy.NEVER;
                    }

                    if (verticalScrollBarPolicy != null) {
                        selectedTab.getUserData().put(VERTICAL_SCROLL_BAR_POLICY_KEY,
                            verticalScrollBarPolicy);

                        ScrollPane scrollPane = (ScrollPane)selectedTab.getUserData().get
                            (SCROLL_PANE_KEY);
                        scrollPane.setVerticalScrollBarPolicy(verticalScrollBarPolicy);
                    }
                }
            }
        });

        Path initialSelectedPath = null;

        String classProperty = properties.get(CLASS_PROPERTY);
        Tree.ItemIterator<?> itemIterator = Tree.depthFirstIterator(treeView.getTreeData());
        while (itemIterator.hasNext()) {
            Object node = itemIterator.next();

            if (node instanceof ComponentNode) {
                ComponentNode componentNode = (ComponentNode)node;

                if (classProperty != null) {
                    // class property was set; open the corresponding
                    // component node
                    if (componentNode.getText().equals(classProperty)) {
                        open(componentNode);
                        initialSelectedPath = itemIterator.getPath();
                        break;
                    }
                } else {
                    // class property was *not* set; open the first component
                    // node we find
                    open(componentNode);
                    initialSelectedPath = itemIterator.getPath();
                    break;
                }
            }
        }

        // If we've selected a path, ensure that it's visible to the user
        if (initialSelectedPath != null) {
            treeView.setSelectedPath(initialSelectedPath);

            Path branchPath = new Path(initialSelectedPath, initialSelectedPath.getLength() - 1);
            treeView.expandBranch(branchPath);

            final Path path = initialSelectedPath;
            ApplicationContext.queueCallback(new Runnable() {
                @Override
                public void run() {
                    treeView.scrollAreaToVisible(treeView.getNodeBounds(path));
                }
            });
        }

        window.open(display);

        treeView.requestFocus();
    }

    private void open(ComponentNode componentNode) {
        URL src = componentNode.getSrc();
        String srcPath = src.getPath();
        ScrollBarPolicy horizontalScrollBarPolicy = componentNode.getHorizontalScrollBarPolicy();
        ScrollBarPolicy verticalScrollBarPolicy = componentNode.getVerticalScrollBarPolicy();

        int tabIndex = -1;
        TabPane.TabSequence tabs = contentTabPane.getTabs();
        for (int i = 0, n = tabs.getLength(); i < n; i++) {
            Component tab = tabs.get(i);

            if (srcPath.equals((String)tab.getUserData().get(SRC_KEY))) {
                tabIndex = i;
                break;
            }
        }

        if (tabIndex == -1) {
            Component component = null;

            WTKXSerializer wtkxSerializer = new WTKXSerializer();
            try {
                component = (Component)wtkxSerializer.readObject(src);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            } catch (SerializationException exception) {
                throw new RuntimeException(exception);
            }

            Border contentPane = new Border();
            contentPane.getStyles().put("thickness", 0);
            contentPane.getStyles().put("padding", 6);
            contentPane.setContent(component);

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setHorizontalScrollBarPolicy(horizontalScrollBarPolicy);
            scrollPane.setVerticalScrollBarPolicy(verticalScrollBarPolicy);
            scrollPane.setView(contentPane);

            Border border = new Border();
            border.getUserData().put(SRC_KEY, srcPath);
            border.getUserData().put(HORIZONTAL_SCROLL_BAR_POLICY_KEY, horizontalScrollBarPolicy);
            border.getUserData().put(VERTICAL_SCROLL_BAR_POLICY_KEY, verticalScrollBarPolicy);
            border.getUserData().put(COMPONENT_KEY, component);
            border.getUserData().put(SCROLL_PANE_KEY, scrollPane);
            border.setContent(scrollPane);

            tabIndex = contentTabPane.getTabs().add(border);
            TabPane.setLabel(border, srcPath.substring(srcPath.lastIndexOf('/') + 1));
        }

        contentTabPane.setSelectedIndex(tabIndex);
    }

    @Override
    public boolean shutdown(boolean optional) throws Exception {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(ComponentExplorer.class, args);
    }
}
