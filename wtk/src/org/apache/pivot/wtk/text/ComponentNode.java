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
package org.apache.pivot.wtk.text;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Component;

/**
 * Node representing a live pivot component.
 */
public class ComponentNode extends Node {

    private static class ComponentNodeListenerList extends ListenerList<ComponentNodeListener> implements
        ComponentNodeListener {
        @Override
        public void componentChanged(ComponentNode componentNode, Component previousComponent) {
            for (ComponentNodeListener listener : this) {
                listener.componentChanged(componentNode, previousComponent);
            }
        }
    }

    private Component component = null;

    private ComponentNodeListenerList componentNodeListeners = new ComponentNodeListenerList();

    public ComponentNode() {
    }

    public ComponentNode(ComponentNode componentNode) {
        setComponent(componentNode.getComponent());
    }

    public ComponentNode(Component component) {
        setComponent(component);
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        Component previousComponent = this.component;

        if (previousComponent != component) {
            this.component = component;
            componentNodeListeners.componentChanged(this, previousComponent);
        }
    }

    @Override
    public char getCharacterAt(int offset) {
        return 0x00;
    }

    @Override
    public int getCharacterCount() {
        return 1;
    }

    @Override
    public void insertRange(Node range, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node removeRange(int offset, int span) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getRange(int offset, int characterCount) {
        if (offset < 0 || offset > 1) {
            throw new IndexOutOfBoundsException();
        }

        if (characterCount != 1) {
            throw new IllegalArgumentException("Invalid characterCount.");
        }

        return new ComponentNode(this);
    }

    @Override
    public Node duplicate(boolean recursive) {
        return new ComponentNode(this);
    }

    public ListenerList<ComponentNodeListener> getComponentNodeListeners() {
        return componentNodeListeners;
    }
}
