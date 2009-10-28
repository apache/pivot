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

import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.tools.wtk.ComponentPropertyInspector;
import org.apache.pivot.tools.wtk.ComponentStyleInspector;
import org.apache.pivot.tools.wtk.EventLogger;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeViewSelectionListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;

public class ComponentExplorer implements Application {
    private Window window = null;
    private TreeView treeView = null;
    private ScrollPane scrollPane = null;
    private ComponentPropertyInspector componentPropertyInspector = null;
    private ComponentStyleInspector componentStyleInspector = null;
    private EventLogger eventLogger = null;

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        Resources resources = new Resources(getClass().getName());
        WTKXSerializer wtkxSerializer = new WTKXSerializer(resources);
        window = (Window)wtkxSerializer.readObject(this, "component_explorer.wtkx");

        treeView = wtkxSerializer.getValue("treeView");
        scrollPane = wtkxSerializer.getValue("scrollPane");
        componentPropertyInspector = wtkxSerializer.getValue("componentPropertyInspector");
        componentStyleInspector = wtkxSerializer.getValue("componentStyleInspector");
        eventLogger = wtkxSerializer.getValue("eventLogger");

        treeView.getTreeViewSelectionListeners().add(new TreeViewSelectionListener.Adapter() {
            @Override
            public void selectedPathsChanged(TreeView treeView,
                Sequence<Path> previousSelectedPaths) {
                Component component = null;

                Object node = treeView.getSelectedNode();
                if (node instanceof ComponentNode) {
                    ComponentNode componentNode = (ComponentNode)node;

                    WTKXSerializer wtkxSerializer = new WTKXSerializer();
                    try {
                        component = (Component)wtkxSerializer.readObject(componentNode.getSrc());
                    } catch (IOException exception) {
                        throw new RuntimeException(exception);
                    } catch (SerializationException exception) {
                        throw new RuntimeException(exception);
                    }

                    scrollPane.setHorizontalScrollBarPolicy
                        (componentNode.getHorizontalScrollBarPolicy());
                    scrollPane.setVerticalScrollBarPolicy
                        (componentNode.getVerticalScrollBarPolicy());
                }

                scrollPane.setView(component);
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
                        Object treeNode = Sequence.Tree.get(treeData, path);

                        if (treeNode instanceof List<?>) {
                            treeView.setBranchExpanded(path, !treeView.isBranchExpanded(path));
                        }
                    }
                }

                return false;
            }
        });

        treeView.expandAll();

        window.open(display);
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
