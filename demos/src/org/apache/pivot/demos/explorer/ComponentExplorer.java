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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
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
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeViewSelectionListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.text.Document;
import org.apache.pivot.wtk.text.PlainTextSerializer;
import org.apache.pivot.wtkx.WTKXSerializer;

public class ComponentExplorer implements Application {
    private Window window = null;
    private TreeView treeView = null;
    private ScrollPane contentScrollPane = null;
    private Border contentPane = null;
    private TextArea sourceTextArea = null;
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

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        Resources resources = new Resources(getClass().getName());
        WTKXSerializer wtkxSerializer = new WTKXSerializer(resources);
        window = (Window)wtkxSerializer.readObject(this, "component_explorer.wtkx");

        treeView = wtkxSerializer.getValue("treeView");
        contentScrollPane = wtkxSerializer.getValue("contentScrollPane");
        contentPane = wtkxSerializer.getValue("contentPane");
        sourceTextArea = wtkxSerializer.getValue("sourceTextArea");
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

        treeView.getTreeViewSelectionListeners().add(new TreeViewSelectionListener.Adapter() {
            @Override
            public void selectedPathsChanged(TreeView treeView,
                Sequence<Path> previousSelectedPaths) {
                Component component = null;

                Object node = treeView.getSelectedNode();
                if (node instanceof ComponentNode) {
                    ComponentNode componentNode = (ComponentNode)node;
                    URL url = componentNode.getSrc();

                    Document document = null;

                    try {
                        PlainTextSerializer plainTextSerializer = new PlainTextSerializer("UTF-8");
                        InputStream inputStream = new BufferedInputStream(url.openStream());

                        try {
                            document = plainTextSerializer.readObject(inputStream);
                        } finally {
                            inputStream.close();
                        }
                    } catch (IOException exception) {
                        throw new RuntimeException(exception);
                    } catch (SerializationException exception) {
                        throw new RuntimeException(exception);
                    }

                    sourceTextArea.setDocument(document);

                    WTKXSerializer wtkxSerializer = new WTKXSerializer();
                    try {
                        component = (Component)wtkxSerializer.readObject(url);
                    } catch (IOException exception) {
                        throw new RuntimeException(exception);
                    } catch (SerializationException exception) {
                        throw new RuntimeException(exception);
                    }

                    switch (componentNode.getHorizontalScrollBarPolicy()) {
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

                    switch (componentNode.getVerticalScrollBarPolicy()) {
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

                contentPane.setContent(component);
                componentPropertyInspector.setSource(component);
                componentStyleInspector.setSource(component);
                eventLogger.setSource(component);
            }
        });

        treeView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter() {
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y,
                int count) {
                if (button == Mouse.Button.LEFT && count == 2) {
                    Path path = treeView.getNodeAt(y);

                    if (path != null) {
                        List<?> treeData = treeView.getTreeData();
                        Object node = Tree.get(treeData, path);

                        if (node instanceof List<?>) {
                            treeView.setBranchExpanded(path, !treeView.isBranchExpanded(path));
                        }
                    }
                }

                return false;
            }
        });

        horizontalScrollBarPolicyGroup.getButtonGroupListeners().add
            (new ButtonGroupListener.Adapter() {
            @Override
            public void selectionChanged(ButtonGroup buttonGroup, Button previousSelection) {
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
                    contentScrollPane.setHorizontalScrollBarPolicy(horizontalScrollBarPolicy);
                }
            }
        });

        verticalScrollBarPolicyGroup.getButtonGroupListeners().add
            (new ButtonGroupListener.Adapter() {
            @Override
            public void selectionChanged(ButtonGroup buttonGroup, Button previousSelection) {
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
                    contentScrollPane.setVerticalScrollBarPolicy(verticalScrollBarPolicy);
                }
            }
        });

        Path initialSelectedPath = null;
        Path firstComponentPath = null;

        String classProperty = properties.get(CLASS_PROPERTY);

        Tree.ItemIterator<?> itemIterator = Tree.depthFirstIterator(treeView.getTreeData());
        while (itemIterator.hasNext()) {
            Object node = itemIterator.next();

            if (node instanceof ComponentNode) {
                ComponentNode componentNode = (ComponentNode)node;
                Path path = itemIterator.getPath();

                if (firstComponentPath == null) {
                    firstComponentPath = path;
                }

                if (classProperty != null) {
                    // class property was set; open the corresponding
                    // component node
                    if (componentNode.getText().equals(classProperty)) {
                        initialSelectedPath = path;
                        break;
                    }
                } else {
                    // class property was *not* set; open the first component
                    // node we find
                    initialSelectedPath = path;
                    break;
                }
            }
        }

        // Default the initial selected path to the first component
        if (initialSelectedPath == null) {
            initialSelectedPath = firstComponentPath;
        }

        if (initialSelectedPath != null) {
            // Select the path
            treeView.setSelectedPath(initialSelectedPath);

            // Ensure that it's visible to the user
            Path branchPath = new Path(initialSelectedPath, initialSelectedPath.getLength() - 1);
            while (branchPath.getLength() > 0) {
                treeView.expandBranch(branchPath);
                branchPath.remove(branchPath.getLength() - 1, 1);
            }

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
