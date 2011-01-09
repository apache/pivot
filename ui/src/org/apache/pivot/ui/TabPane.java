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
package org.apache.pivot.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.pivot.bxml.DefaultProperty;
import org.apache.pivot.scene.Group;
import org.apache.pivot.scene.Node;
import org.apache.pivot.util.ObservableList;
import org.apache.pivot.util.ObservableListAdapter;

/**
 * Container that provides access to a set of components via selectable tabs,
 * only one of which is visible at a time.
 */
@DefaultProperty("tabs")
// TODO Not abstract; just for prototyping
public abstract class TabPane extends Group {
    // Tab list
    private class TabList extends ObservableListAdapter<Tab> {
        public TabList() {
            super(new ArrayList<Tab>());
        }

        @Override
        public void add(int index, Tab tab) {
            getNodes().add(tab);
            super.add(tab);
        }

        @Override
        public boolean addAll(int index, Collection<? extends Tab> tabs) {
            getNodes().addAll(tabs);
            return super.addAll(index, tabs);
        }

        @Override
        public Tab remove(int index) {
            getNodes().remove(get(index));
            return super.remove(index);
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            for (int i = fromIndex; i < toIndex; i++) {
                getNodes().remove(get(i));
            }

            super.removeRange(fromIndex, toIndex);
        }

        @Override
        public Tab set(int index, Tab tab) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Tab> setAll(int index, Collection<? extends Tab> tabs) {
            throw new UnsupportedOperationException();
        }
    }

    private TabList tabs = new TabList();
    private Component anchor = null;
    private Component corner = null;

    private ObservableList<Node> nodes = new ObservableListAdapter<Node>(getNodes()) {
        @Override
        public Node remove(int index) {
            validateRemove(get(index));
            return super.remove(index);
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            for (int i = fromIndex; i < toIndex; i++) {
                validateRemove(nodes.get(i));
            }

            super.removeRange(fromIndex, toIndex);
        }

        private void validateRemove(Node node) {
            if (node == anchor
                || node == corner
                || tabs.indexOf(node) >= 0) {
                throw new IllegalArgumentException();
            }
        }
    };

    /**
     * Class representing a tab in a tab pane.
     */
    @DefaultProperty("content")
    // TODO Not abstract; just for prototyping
    public abstract static class Tab extends Group {
        private Object tabData = null;
        private Component content = null;

        private ObservableList<Node> nodes = new ObservableListAdapter<Node>(getNodes()) {
            @Override
            public Node remove(int index) {
                validateRemove(get(index));
                return super.remove(index);
            }

            @Override
            protected void removeRange(int fromIndex, int toIndex) {
                for (int i = fromIndex; i < toIndex; i++) {
                    validateRemove(nodes.get(i));
                }

                super.removeRange(fromIndex, toIndex);
            }

            private void validateRemove(Node node) {
                if (node == content) {
                    throw new IllegalArgumentException();
                }
            }
        };

        @Override
        public ObservableList<Node> getNodes() {
            return nodes;
        }

        public Component getContent() {
            return content;
        }

        public void setContent(Component content) {
            this.content = content;

            // TODO Fire event
        }

        public Object getTabData() {
            return tabData;
        }

        public void setTabData(Object tabData) {
            this.tabData = tabData;

            // TODO Fire event
        }
    }

    @Override
    public ObservableList<Node> getNodes() {
        return nodes;
    }

    public ObservableList<Tab> getTabs() {
        return tabs;
    }
}
