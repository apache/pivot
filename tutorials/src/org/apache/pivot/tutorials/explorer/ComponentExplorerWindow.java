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
package org.apache.pivot.tutorials.explorer;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.pivot.beans.BeanSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.tools.wtk.ComponentPropertyInspector;
import org.apache.pivot.tools.wtk.ComponentStyleInspector;
import org.apache.pivot.tools.wtk.EventLogger;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonGroupListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.ScrollPane.ScrollBarPolicy;
import org.apache.pivot.wtk.SplitPane;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeViewSelectionListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.text.Document;
import org.apache.pivot.wtk.text.PlainTextSerializer;

public class ComponentExplorerWindow extends Window implements Bindable {
    private String classProperty;

    private SplitPane splitPane = null;
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
    private Button horizontalAlwaysButton = null;
    private Button verticalAutoButton = null;
    private Button verticalFillButton = null;
    private Button verticalFillToCapacityButton = null;
    private Button verticalNeverButton = null;
    private Button verticalAlwaysButton = null;

    public static final String CLASS_PROPERTY = "class";

    @Override
    public void initialize(Dictionary<String, Object> context, Resources resources) {
        splitPane = (SplitPane)context.get("splitPane");
        treeView = (TreeView)context.get("treeView");
        contentScrollPane = (ScrollPane)context.get("contentScrollPane");
        contentPane = (Border)context.get("contentPane");
        sourceTextArea = (TextArea)context.get("sourceTextArea");
        componentPropertyInspector = (ComponentPropertyInspector)context.get("componentPropertyInspector");
        componentStyleInspector = (ComponentStyleInspector)context.get("componentStyleInspector");
        eventLogger = (EventLogger)context.get("eventLogger");

        horizontalScrollBarPolicyGroup = (ButtonGroup)context.get("horizontalScrollBarPolicyGroup");
        verticalScrollBarPolicyGroup = (ButtonGroup)context.get("verticalScrollBarPolicyGroup");
        horizontalAutoButton = (Button)context.get("horizontalAutoButton");
        horizontalFillButton = (Button)context.get("horizontalFillButton");
        horizontalFillToCapacityButton = (Button)context.get("horizontalFillToCapacityButton");
        horizontalNeverButton = (Button)context.get("horizontalNeverButton");
        horizontalAlwaysButton = (Button)context.get("horizontalAlwaysButton");
        verticalAutoButton = (Button)context.get("verticalAutoButton");
        verticalFillButton = (Button)context.get("verticalFillButton");
        verticalFillToCapacityButton = (Button)context.get("verticalFillToCapacityButton");
        verticalNeverButton = (Button)context.get("verticalNeverButton");
        verticalAlwaysButton = (Button)context.get("verticalAlwaysButton");

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

                    BeanSerializer beanSerializer = new BeanSerializer();
                    try {
                        component = (Component)beanSerializer.readObject(url);
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
                    case ALWAYS:
                        horizontalScrollBarPolicyGroup.setSelection(horizontalAlwaysButton);
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
                    case ALWAYS:
                        verticalScrollBarPolicyGroup.setSelection(verticalAlwaysButton);
                        break;
                    }
                } else {
                    sourceTextArea.setText((String)null);
                }

                contentPane.setContent(component);
                componentPropertyInspector.setSource(component);
                componentStyleInspector.setSource(component);
                eventLogger.setSource(component);
                eventLogger.clearLog();
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

        horizontalScrollBarPolicyGroup.getButtonGroupListeners().add(new ButtonGroupListener.Adapter() {
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
                } else if (button == horizontalAlwaysButton) {
                    horizontalScrollBarPolicy = ScrollBarPolicy.ALWAYS;
                }

                if (horizontalScrollBarPolicy != null) {
                    contentScrollPane.setHorizontalScrollBarPolicy(horizontalScrollBarPolicy);
                }
            }
        });

        verticalScrollBarPolicyGroup.getButtonGroupListeners().add(new ButtonGroupListener.Adapter() {
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
                } else if (button == verticalAlwaysButton) {
                    verticalScrollBarPolicy = ScrollBarPolicy.ALWAYS;
                }

                if (verticalScrollBarPolicy != null) {
                    contentScrollPane.setVerticalScrollBarPolicy(verticalScrollBarPolicy);
                }
            }
        });
    }

    @Override
    public void open(Display display, Window owner) {
        super.open(display, owner);

        Path initialSelectedPath = null;
        Path firstComponentPath = null;

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
                        splitPane.setSplitRatio(0);
                        splitPane.setLocked(true);

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

        treeView.requestFocus();
    }

    public String getClassProperty() {
        return classProperty;
    }

    public void setClassProperty(String classProperty) {
        this.classProperty = classProperty;
    }
}
