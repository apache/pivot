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
package org.apache.pivot.demos.xml;

import java.awt.Color;
import java.awt.Font;

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.xml.Element;
import org.apache.pivot.xml.TextNode;

/**
 * Custom tree view node renderer for presenting XML nodes.
 */
public class NodeRenderer extends Label implements TreeView.NodeRenderer {
    public static final int MAXIMUM_TEXT_LENGTH = 20;

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    @Override
    public void render(Object node, Sequence.Tree.Path path, int rowIndex, TreeView treeView,
        boolean expanded, boolean selected, TreeView.NodeCheckState checkState,
        boolean highlighted, boolean disabled) {
        if (node != null) {
            String text;
            if (node instanceof Element) {
                Element element = (Element) node;
                text = "<" + element.getName() + ">";
            } else if (node instanceof TextNode) {
                TextNode textNode = (TextNode) node;
                text = textNode.getText();

                if (text.length() > MAXIMUM_TEXT_LENGTH) {
                    text = "\"" + text.substring(0, MAXIMUM_TEXT_LENGTH) + "\"...";
                } else {
                    text = "\"" + text + "\"";
                }
            } else {
                throw new IllegalArgumentException("Unknown node type: " + node.getClass().getName());
            }

            setText(text);

            Font font = treeView.getStyles().getFont(Style.font);
            getStyles().put(Style.font, font);

            Color color;
            if (treeView.isEnabled() && !disabled) {
                if (selected) {
                    if (treeView.isFocused()) {
                        color = treeView.getStyles().getColor(Style.selectionColor);
                    } else {
                        color = treeView.getStyles().getColor(Style.inactiveSelectionColor);
                    }
                } else {
                    color = treeView.getStyles().getColor(Style.color);
                }
            } else {
                color = treeView.getStyles().getColor(Style.disabledColor);
            }

            getStyles().put(Style.color, color);
        }
    }

    @Override
    public String toString(Object node) {
        String string;
        if (node instanceof Element) {
            Element element = (Element) node;
            string = element.getName();
        } else if (node instanceof TextNode) {
            TextNode textNode = (TextNode) node;
            string = textNode.getText();
        } else {
            throw new IllegalArgumentException("Unknown node type: " + node.getClass().getName());
        }

        return string;
    }
}
