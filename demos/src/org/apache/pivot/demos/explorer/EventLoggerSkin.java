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

import java.awt.Graphics2D;
import java.lang.reflect.Method;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeViewNodeStateListener;
import org.apache.pivot.wtk.content.TreeBranch;
import org.apache.pivot.wtk.content.TreeNode;
import org.apache.pivot.wtk.skin.ContainerSkin;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

class EventLoggerSkin extends ContainerSkin implements EventLoggerListener {
    private static class TreeNodeComparator implements Comparator<TreeNode> {
        public int compare(TreeNode treeNode1, TreeNode treeNode2) {
            return treeNode1.getText().compareTo(treeNode2.getText());
        }
    }

    private static class EventNode extends TreeNode {
        private Method event;

        public EventNode(Method event) {
            super(event.getName());
            this.event = event;
        }

        public Method getEvent() {
            return event;
        }
    }

    private Component content = null;

    @WTKX private TreeView declaredEventsTreeView = null;
    @WTKX private TableView firedEventsTableView = null;

    private static TreeNodeComparator treeNodeComparator = new TreeNodeComparator();

    @Override
    public void install(Component component) {
        super.install(component);

        EventLogger eventLogger = (EventLogger)component;

        eventLogger.getEventLoggerListeners().add(this);

        Resources resources;
        try {
            resources = new Resources(getClass().getName());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        WTKXSerializer wtkxSerializer = new WTKXSerializer(resources);
        try {
            content = (Component)wtkxSerializer.readObject(this, "event_logger_skin.wtkx");
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        eventLogger.add(content);

        wtkxSerializer.bind(this, EventLoggerSkin.class);

        declaredEventsTreeView.getTreeViewNodeStateListeners().add(new TreeViewNodeStateListener() {
            private boolean updating = false;

            @Override
            public void nodeCheckStateChanged(TreeView treeView, Path path,
                TreeView.NodeCheckState previousCheckState) {
                TreeView.NodeCheckState checkState = treeView.getNodeCheckState(path);

                if (!updating) {
                    // Set the updating flag for the life of this event loop
                    updating = true;
                    ApplicationContext.queueCallback(new Runnable() {
                        @Override
                        public void run() {
                            updating = false;
                        }
                    });

                    EventLogger eventLogger = (EventLogger)getComponent();

                    boolean checked = (checkState == TreeView.NodeCheckState.CHECKED);

                    List<?> treeData = (List<?>)treeView.getTreeData();
                    TreeNode treeNode = (TreeNode)Sequence.Tree.get(treeData, path);

                    if (treeNode instanceof List<?>) {
                        if (previousCheckState == TreeView.NodeCheckState.CHECKED
                            || checkState == TreeView.NodeCheckState.CHECKED) {
                            // Propagate downward
                            List<?> treeBranch = (List<?>)treeNode;

                            Path childPath = new Path(path);
                            int lastIndex = childPath.getLength();
                            childPath.add(0);

                            for (int i = 0, n = treeBranch.getLength(); i < n; i++) {
                                childPath.update(lastIndex, i);
                                treeView.setNodeChecked(childPath, checked);

                                EventNode eventNode = (EventNode)treeBranch.get(i);
                                Method event = eventNode.getEvent();

                                if (checked) {
                                    eventLogger.includeEvent(event);
                                } else {
                                    eventLogger.excludeEvent(event);
                                }
                            }
                        }
                    } else {
                        Path parentPath = new Path(path, path.getLength() - 1);

                        EventNode eventNode = (EventNode)treeNode;
                        Method event = eventNode.getEvent();

                        if (checked) {
                            List<?> treeBranch = (List<?>)Sequence.Tree.get(treeData, parentPath);

                            Path childPath = new Path(path);
                            int lastIndex = parentPath.getLength();

                            int i = 0, n = treeBranch.getLength();
                            while (i < n) {
                                childPath.update(lastIndex, i);

                                if (!treeView.isNodeChecked(childPath)) {
                                    break;
                                }

                                i++;
                            }

                            if (i == n) {
                                // Propagate upward
                                treeView.setNodeChecked(parentPath, checked);
                            }

                            eventLogger.includeEvent(event);
                        } else {
                            // Propagate upward
                            treeView.setNodeChecked(parentPath, checked);

                            eventLogger.excludeEvent(event);
                        }
                    }
                }
            }
        });

        sourceChanged(eventLogger, null);
    }

    @Override
    public int getPreferredWidth(int height) {
        return content.getPreferredWidth(height);
    }

    @Override
    public int getPreferredHeight(int width) {
        return content.getPreferredHeight(width);
    }

    @Override
    public Dimensions getPreferredSize() {
        return content.getPreferredSize();
    }

    @Override
    public void layout() {
        content.setLocation(0, 0);
        content.setSize(getWidth(), getHeight());
    }

    @Override
    public void sourceChanged(EventLogger eventLogger, Component previousSource) {
        HashMap<Class<?>, ArrayList<Method>> buckets = new HashMap<Class<?>, ArrayList<Method>>();

        Sequence<Method> declaredEvents = eventLogger.getDeclaredEvents();
        for (int i = 0, n = declaredEvents.getLength(); i < n; i++) {
            Method event = declaredEvents.get(i);
            Class<?> listenerInterface = event.getDeclaringClass();

            ArrayList<Method> bucket = buckets.get(listenerInterface);
            if (bucket == null) {
                bucket = new ArrayList<Method>();
                buckets.put(listenerInterface, bucket);
            }

            bucket.add(event);
        }

        ArrayList<TreeNode> treeData = new ArrayList<TreeNode>(treeNodeComparator);

        for (Class<?> listenerInterface : buckets) {
            TreeBranch treeBranch = new TreeBranch(listenerInterface.getSimpleName());
            treeBranch.setComparator(treeNodeComparator);
            treeData.add(treeBranch);

            for (Method event : buckets.get(listenerInterface)) {
                treeBranch.add(new EventNode(event));
            }
        }

        declaredEventsTreeView.setTreeData(treeData);
    }

    @Override
    public void eventIncluded(EventLogger eventLogger, Method method) {
        // TODO
    }

    @Override
    public void eventExcluded(EventLogger eventLogger, Method method) {
        // TODO
    }

    @Override
    @SuppressWarnings("unchecked")
    public void eventFired(EventLogger eventLogger, Method event, Object[] arguments) {
        HashMap<String, Object> row = new HashMap<String, Object>();
        row.put("interface", event.getDeclaringClass().getSimpleName());
        row.put("method", event.getName());
        row.put("arguments", Arrays.toString(arguments));

        List<Object> tableData = (List<Object>)firedEventsTableView.getTableData();
        final int rowIndex = tableData.add(row);

        ApplicationContext.queueCallback(new Runnable() {
            @Override
            public void run() {
                Bounds rowBounds = firedEventsTableView.getRowBounds(rowIndex);
                firedEventsTableView.scrollAreaToVisible(rowBounds);
            }
        });
    }
}
