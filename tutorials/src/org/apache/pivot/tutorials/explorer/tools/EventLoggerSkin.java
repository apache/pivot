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
package org.apache.pivot.tutorials.explorer.tools;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Sequence.Tree.ItemIterator;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.serialization.SerializationException;
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

class EventLoggerSkin extends ContainerSkin implements EventLogger.Skin, EventLoggerListener {

    private static class TreeNodeComparator implements Comparator<TreeNode>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
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

    private TreeView declaredEventsTreeView = null;
    private TableView firedEventsTableView = null;

    private boolean updating = false;

    private static TreeNodeComparator treeNodeComparator = new TreeNodeComparator();

    @Override
    public void install(Component component) {
        super.install(component);

        EventLogger eventLogger = (EventLogger) component;

        eventLogger.getEventLoggerListeners().add(this);

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        try {
            content = (Component) bxmlSerializer.readObject(EventLoggerSkin.class,
                "event_logger_skin.bxml", true);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        eventLogger.add(content);

        declaredEventsTreeView = (TreeView) bxmlSerializer.getNamespace().get(
            "declaredEventsTreeView");
        firedEventsTableView = (TableView) bxmlSerializer.getNamespace().get("firedEventsTableView");

        // Propagate check state upwards or downwards as necessary
        declaredEventsTreeView.getTreeViewNodeStateListeners().add(new TreeViewNodeStateListener() {
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

                    EventLogger eventLoggerLocal = (EventLogger) getComponent();

                    boolean checked = (checkState == TreeView.NodeCheckState.CHECKED);

                    List<?> treeData = treeView.getTreeData();
                    TreeNode treeNode = (TreeNode) Sequence.Tree.get(treeData, path);

                    if (treeNode instanceof List<?>) {
                        if (previousCheckState == TreeView.NodeCheckState.CHECKED
                            || checkState == TreeView.NodeCheckState.CHECKED) {
                            // Propagate downward
                            List<?> treeBranch = (List<?>) treeNode;

                            Path childPath = new Path(path);
                            int lastIndex = childPath.getLength();
                            childPath.add(0);

                            for (int i = 0, n = treeBranch.getLength(); i < n; i++) {
                                childPath.update(lastIndex, i);
                                treeView.setNodeChecked(childPath, checked);

                                EventNode eventNode = (EventNode) treeBranch.get(i);
                                Method event = eventNode.getEvent();

                                if (checked) {
                                    eventLoggerLocal.getIncludeEvents().add(event);
                                } else {
                                    eventLoggerLocal.getIncludeEvents().remove(event);
                                }
                            }
                        }
                    } else {
                        Path parentPath = new Path(path, path.getLength() - 1);

                        EventNode eventNode = (EventNode) treeNode;
                        Method event = eventNode.getEvent();

                        if (checked) {
                            List<?> treeBranch = (List<?>) Sequence.Tree.get(treeData, parentPath);

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

                            eventLoggerLocal.getIncludeEvents().add(event);
                        } else {
                            // Propagate upward
                            treeView.setNodeChecked(parentPath, checked);

                            eventLoggerLocal.getIncludeEvents().remove(event);
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

    // EventLogger.Skin methods

    @Override
    public void clearLog() {
        firedEventsTableView.getTableData().clear();
    }

    @Override
    public void selectAllEvents(boolean select) {
        @SuppressWarnings("unchecked")
        List<TreeNode> treeData = (List<TreeNode>) declaredEventsTreeView.getTreeData();

        ItemIterator<TreeNode> iter = Sequence.Tree.depthFirstIterator(treeData);
        while (iter.hasNext()) {
            iter.next();
            declaredEventsTreeView.setNodeChecked(iter.getPath(), select);
        }
    }

    // EventLoggerListener methods

    @Override
    public void sourceChanged(EventLogger eventLogger, Component previousSource) {
        // Component source = eventLogger.getSource();

        HashMap<Class<?>, ArrayList<Method>> buckets = new HashMap<>();

        for (Method event : eventLogger.getDeclaredEvents()) {
            Class<?> listenerInterface = event.getDeclaringClass();

            ArrayList<Method> bucket = buckets.get(listenerInterface);
            if (bucket == null) {
                bucket = new ArrayList<>();
                buckets.put(listenerInterface, bucket);
            }

            bucket.add(event);
        }

        ArrayList<TreeNode> treeData = new ArrayList<>(treeNodeComparator);
        declaredEventsTreeView.setTreeData(treeData);

        updating = true;
        try {
            for (Class<?> listenerInterface : buckets) {
                TreeBranch treeBranch = new TreeBranch(listenerInterface.getSimpleName());
                treeBranch.setComparator(treeNodeComparator);
                treeData.add(treeBranch);

                for (Method event : buckets.get(listenerInterface)) {
                    treeBranch.add(new EventNode(event));
                    eventLogger.getIncludeEvents().add(event);
                }
            }

            Sequence.Tree.ItemIterator<TreeNode> iter = Sequence.Tree.depthFirstIterator(treeData);
            while (iter.hasNext()) {
                iter.next();
                declaredEventsTreeView.setNodeChecked(iter.getPath(), true);
            }
        } finally {
            updating = false;
        }
    }

    @Override
    public void eventIncluded(EventLogger eventLogger, Method method) {
        setEventIncluded(method, true);
    }

    @Override
    public void eventExcluded(EventLogger eventLogger, Method method) {
        setEventIncluded(method, false);
    }

    private void setEventIncluded(Method event, boolean included) {
        @SuppressWarnings("unchecked")
        List<TreeNode> treeData = (List<TreeNode>) declaredEventsTreeView.getTreeData();

        Sequence.Tree.ItemIterator<TreeNode> iter = Sequence.Tree.depthFirstIterator(treeData);
        while (iter.hasNext()) {
            TreeNode treeNode = iter.next();

            if (treeNode instanceof EventNode) {
                EventNode eventNode = (EventNode) treeNode;

                if (eventNode.getEvent() == event) {
                    declaredEventsTreeView.setNodeChecked(iter.getPath(), included);
                    break;
                }
            }
        }
    }

    @Override
    public void eventFired(EventLogger eventLogger, Method event, Object[] arguments) {
        HashMap<String, Object> row = new HashMap<>();
        row.put("interface", event.getDeclaringClass().getSimpleName());
        row.put("method", event.getName());
        row.put("arguments", Arrays.toString(arguments));

        @SuppressWarnings("unchecked")
        List<Object> tableData = (List<Object>) firedEventsTableView.getTableData();
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
