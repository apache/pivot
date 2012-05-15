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
package org.apache.pivot.wtk.skin;

import java.awt.Graphics2D;

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.text.ComponentNode;
import org.apache.pivot.wtk.text.ComponentNodeListener;

class TextPaneSkinComponentNodeView extends TextPaneSkinNodeView implements ComponentNodeListener {

    private final ComponentListener myComponentListener = new ComponentListener.Adapter() {
        @Override
        public void sizeChanged(Component component, int previousWidth, int previousHeight) {
            invalidateUpTree();
        }
    };

    public TextPaneSkinComponentNodeView(ComponentNode componentNode) {
        super(componentNode);
    }

    @Override
    protected void attach() {
        super.attach();

        ComponentNode componentNode = (ComponentNode) getNode();
        componentNode.getComponentNodeListeners().add(this);

        Component component = componentNode.getComponent();
        if (component != null) {
            component.getComponentListeners().add(myComponentListener);
        }
    }

    @Override
    protected void detach() {
        super.detach();

        ComponentNode componentNode = (ComponentNode) getNode();
        componentNode.getComponentNodeListeners().remove(this);
    }

    @Override
    protected void childLayout(int breakWidth) {
        ComponentNode componentNode = (ComponentNode) getNode();
        Component component = componentNode.getComponent();

        if (component == null) {
            setSize(0, 0);
        } else {
            component.validate();
            component.setSize(component.getPreferredWidth(), component.getPreferredHeight());
            setSize(component.getWidth(), component.getHeight());
        }
    }

    @Override
    public Dimensions getPreferredSize(int breakWidth) {
        ComponentNode componentNode = (ComponentNode) getNode();
        Component component = componentNode.getComponent();

        if (component == null) {
            return new Dimensions(0, 0);
        }
        return new Dimensions(component.getPreferredWidth(), component.getPreferredHeight());
    }

    @Override
    public int getBaseline() {
        ComponentNode componentNode = (ComponentNode) getNode();
        Component component = componentNode.getComponent();

        int baseline = -1;
        if (component != null) {
            baseline = component.getBaseline();
        }
        return baseline;
    }

    @Override
    protected void setSkinLocation(int skinX, int skinY) {
        ComponentNode componentNode = (ComponentNode) getNode();
        Component component = componentNode.getComponent();

        if (component != null) {
            // I have to un-translate the x and y coordinates because the
            // component is painted by the Container object, and it's co-ordinates
            // are relative to the Container object, not to the document node hierarchy.
            component.setLocation(skinX, skinY);
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        // do nothing
    }

    @Override
    public int getInsertionPoint(int x, int y) {
        return 0;
    }

    @Override
    public int getNextInsertionPoint(int x, int from, TextPane.ScrollDirection direction) {
        return (from == -1) ? 0 : -1;
    }

    @Override
    public int getRowAt(int offset) {
        return -1;
    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public Bounds getCharacterBounds(int offset) {
        return new Bounds(0, 0, getWidth(), getHeight());
    }

    @Override
    public void componentChanged(ComponentNode componentNode, Component previousComponent) {
        invalidateUpTree();

        Component component = componentNode.getComponent();
        if (component != null) {
            component.getComponentListeners().add(myComponentListener);
        }

        if (previousComponent != null) {
            previousComponent.getComponentListeners().remove(myComponentListener);
        }
    }
}