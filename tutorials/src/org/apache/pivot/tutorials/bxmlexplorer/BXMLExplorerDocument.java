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
package org.apache.pivot.tutorials.bxmlexplorer;

import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.tutorials.explorer.tools.ComponentPropertyInspector;
import org.apache.pivot.tutorials.explorer.tools.ComponentStyleInspector;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.CardPane;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.Mouse.Button;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeView.SelectMode;
import org.apache.pivot.wtk.TreeViewSelectionListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.TreeBranch;
import org.apache.pivot.wtk.content.TreeNode;
import org.apache.pivot.wtk.content.TreeViewNodeRenderer;
import org.apache.pivot.wtk.effects.Decorator;
import org.apache.pivot.wtk.effects.ShadeDecorator;
import org.xml.sax.SAXException;

public class BXMLExplorerDocument extends CardPane implements Bindable {
    @BXML private TreeView treeView;
    @BXML private CardPane playgroundCardPane;
    @BXML private TextPane bxmlSourceTextPane;
    @BXML private ComponentPropertyInspector componentPropertyInspector;
    @BXML private ComponentStyleInspector componentStyleInspector;
    @BXML private PushButton reloadButton;

    private File file;
    private Component loadedComponent;
    /**
     * Maps the WTK widgets -&gt; bxml:id values.
     */
    private HashMap<Object, String> widgetToID = null;
    private HashMap<Object, TreeNode> componentToTreeNode = null;

    public BXMLExplorerDocument() {
    }

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        treeView.setSelectMode(SelectMode.SINGLE);
        treeView.setNodeRenderer(new MyTreeViewNodeRenderer());
        treeView.getTreeViewSelectionListeners().add(new TreeViewSelectionListener() {
            private final Decorator focusDecorator = new ShadeDecorator(0.2f, Color.RED);
            private Component previousSelectedComponent = null;

            @Override
            public void selectedNodeChanged(TreeView treeViewArgument, Object previousSelectedNode) {
                TreeNode node = (TreeNode) treeViewArgument.getSelectedNode();
                if (previousSelectedComponent != null
                    && previousSelectedComponent.getDecorators().indexOf(focusDecorator) > -1) {
                    previousSelectedComponent.getDecorators().remove(focusDecorator);
                    previousSelectedComponent = null;
                }
                if (node == null || !(node.getUserData() instanceof Component)) {
                    // TODO make the inspectors able to deal with things like
                    // TablePane.Row
                    componentPropertyInspector.setSource(null);
                    componentStyleInspector.setSource(null);
                    return;
                }
                Component selectedComp = (Component) node.getUserData();
                if (selectedComp != null
                    && selectedComp.getDecorators().indexOf(focusDecorator) == -1) {
                    selectedComp.getDecorators().add(focusDecorator);
                    previousSelectedComponent = selectedComp;
                }

                if (selectedComp instanceof FakeWindow) {
                    selectedComp = ((FakeWindow) selectedComp).window;
                }
                componentPropertyInspector.setSource(selectedComp);
                componentStyleInspector.setSource(selectedComp);
            }

            @Override
            public void selectedPathsChanged(TreeView treeViewArgument,
                Sequence<Path> previousSelectedPaths) {
                // if the selection becomes empty, remove the decorator
                if (treeViewArgument.getSelectedNode() == null && previousSelectedComponent != null
                    && previousSelectedComponent.getDecorators().indexOf(focusDecorator) > -1) {
                    previousSelectedComponent.getDecorators().remove(focusDecorator);
                }
            }

        });

        playgroundCardPane.getComponentMouseButtonListeners().add(
            new ComponentMouseButtonListener() {
                @Override
                public boolean mouseClick(Component component, Button button, int x, int y,
                    int count) {
                    if (count == 1) {
                        Component comp = playgroundCardPane.getDescendantAt(x, y);
                        if (comp != null) {
                            TreeNode treeNode = componentToTreeNode.get(comp);
                            Path path = getPathForNode(treeView, treeNode);
                            if (path != null) {
                                treeView.setSelectedPath(path);
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });

        reloadButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(org.apache.pivot.wtk.Button button) {
                playgroundCardPane.remove(loadedComponent);
                widgetToID = null;
                componentToTreeNode = null;
                loadedComponent = null;
                try {
                    load(file);
                } catch (RuntimeException exception) {
                    exception.printStackTrace();
                    BXMLExplorer.displayLoadException(exception,
                        BXMLExplorerDocument.this.getWindow());
                } catch (IOException exception) {
                    exception.printStackTrace();
                    BXMLExplorer.displayLoadException(exception,
                        BXMLExplorerDocument.this.getWindow());
                } catch (SerializationException exception) {
                    exception.printStackTrace();
                    BXMLExplorer.displayLoadException(exception,
                        BXMLExplorerDocument.this.getWindow());
                } catch (ParserConfigurationException exception) {
                    exception.printStackTrace();
                    BXMLExplorer.displayLoadException(exception,
                        BXMLExplorerDocument.this.getWindow());
                } catch (SAXException exception) {
                    exception.printStackTrace();
                    BXMLExplorer.displayLoadException(exception,
                        BXMLExplorerDocument.this.getWindow());
                }
            }
        });
    }

    private static Path getPathForNode(TreeView treeView, TreeNode treeNode) {
        @SuppressWarnings("unchecked")
        List<Object> treeData = (List<Object>) treeView.getTreeData();
        Sequence.Tree.ItemIterator<Object> itemIterator = Sequence.Tree.depthFirstIterator(treeData);
        while (itemIterator.hasNext()) {
            Object node = itemIterator.next();
            if (node == treeNode) {
                return itemIterator.getPath();
            }
        }
        return null;
    }

    public File getLoadedFile() {
        return file;
    }

    public void load(File f) throws IOException, SerializationException,
        ParserConfigurationException, SAXException {
        BXMLSerializer serializer = new BXMLSerializer();
        serializer.setLocation(f.toURI().toURL());
        try (InputStream in = Files.newInputStream(f.toPath())) {
            Object obj = serializer.readObject(in);
            if (!(obj instanceof Component)) {
                throw new IllegalStateException("obj " + obj + " is of class " + obj.getClass()
                    + " which is not a subtype of Component");
            }
            // create an inverse map so we can IDs for widgets
            widgetToID = new HashMap<>();
            for (String key : serializer.getNamespace()) {
                widgetToID.put(serializer.getNamespace().get(key), key);
            }
            // we can't add a Window into the component hierarchy, so fake it
            if (obj instanceof Window) {
                obj = new FakeWindow((Window) obj);
            }
            // create the explorer tree
            componentToTreeNode = new HashMap<>();
            // the root node is not visible
            final TreeBranch rootbranch = new TreeBranch("");
            rootbranch.add(analyseObjectTree(obj));
            treeView.setTreeData(rootbranch);
            treeView.expandAll();
            // add the loaded widget to the display
            this.loadedComponent = (Component) obj;
            playgroundCardPane.add((Component) obj);
        }
        try (InputStream in2 = Files.newInputStream(f.toPath())) {
            CreateHighlightedXML xml = new CreateHighlightedXML();
            bxmlSourceTextPane.setDocument(xml.prettyPrint(in2));
        }
        this.file = f;
    }

    @SuppressWarnings("unchecked")
    private TreeNode analyseObjectTree(Object container) {
        // We don't want the RowSequence object to show up in the tree, it
        // doesn't look neat
        if (container instanceof TablePane) {
            TreeBranch branch = new TreeBranch(nameForObject(container));
            TablePane table = (TablePane) container;
            for (TablePane.Row row : table.getRows()) {
                TreeNode childBranch = analyseObjectTree(row);
                branch.add(childBranch);
            }
            setComponentIconOnTreeNode(container, branch);
            return branch;
        }

        // We don't want to analyse the components that are added as part of the
        // skin, so use similar logic to BXMLSerializer
        DefaultProperty defaultProperty = container.getClass().getAnnotation(DefaultProperty.class);
        if (defaultProperty != null) {
            TreeBranch branch = new TreeBranch(nameForObject(container));
            String defaultPropertyName = defaultProperty.value();
            BeanAdapter beanAdapter = new BeanAdapter(container);
            if (!beanAdapter.containsKey(defaultPropertyName)) {
                throw new IllegalStateException("default property " + defaultPropertyName
                    + " not found on " + container);
            }
            Object defaultPropertyValue = beanAdapter.get(defaultPropertyName);
            if (defaultPropertyValue != null) {
                if (defaultPropertyValue instanceof Component) {
                    TreeNode childBranch = analyseObjectTree(defaultPropertyValue);
                    branch.add(childBranch);
                }
            }
            // An empty branch looks untidy if it has an arrow next to it,
            // so make empty branches into nodes.
            if (branch.isEmpty()) {
                TreeNode node = new TreeNode(branch.getText());
                setComponentIconOnTreeNode(container, node);
                return node;
            }
            setComponentIconOnTreeNode(container, branch);
            return branch;
        }

        if (container instanceof Sequence<?>) {
            TreeBranch branch = new TreeBranch(nameForObject(container));
            Iterable<Object> sequence = (Iterable<Object>) container;
            for (Object child : sequence) {
                TreeNode childBranch = analyseObjectTree(child);
                branch.add(childBranch);
            }
            setComponentIconOnTreeNode(container, branch);
            return branch;
        }

        TreeNode node = new TreeNode(nameForObject(container));
        setComponentIconOnTreeNode(container, node);
        return node;
    }

    private void setComponentIconOnTreeNode(Object container, TreeNode branch) {
        componentToTreeNode.put(container, branch);
        branch.setUserData(container);
        setComponentIconOnTreeNode(branch, container);
    }

    private static void setComponentIconOnTreeNode(TreeNode treeNode, Object comp) {
        String resource = null;
        if (comp instanceof Label) {
            resource = "label.png";
        }
        if (comp instanceof ImageView) {
            resource = "/org/apache/pivot/tutorials/IMG_0725_2.jpg";
        }
        if (comp instanceof PushButton) {
            resource = "pushbutton.png";
        }
        if (comp instanceof RadioButton) {
            resource = "radiobutton.png";
        }
        if (comp instanceof Checkbox) {
            resource = "checkbox.png";
        }
        if (comp instanceof LinkButton) {
            resource = "linkbutton.png";
        }
        if (comp instanceof TablePane) {
            resource = "tablepane.png";
        }
        if (resource != null) {
            URL url = BXMLExplorerDocument.class.getResource(resource);
            if (url == null) {
                throw new IllegalStateException("could not load resource " + resource);
            }
            treeNode.setIcon(url);
        }
    }

    private String nameForObject(final Object obj) {
        Object objUpdated = obj;
        if (objUpdated instanceof FakeWindow) {
            objUpdated = ((FakeWindow) objUpdated).window;
        }
        String bxmlID = widgetToID.get(objUpdated);
        if (bxmlID == null) {
            return objUpdated.getClass().getSimpleName();
        }
        return objUpdated.getClass().getSimpleName() + " " + bxmlID;
    }

    private static class MyTreeViewNodeRenderer extends TreeViewNodeRenderer {
        public MyTreeViewNodeRenderer() {
            setFillIcon(true);
            setIconWidth(32);
            setIconHeight(32);
        }
    }

}
