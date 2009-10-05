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
package org.apache.pivot.wtk.text.test;

import java.awt.Color;
import java.awt.Font;

import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeView.NodeCheckState;


public class NodeAdapterRenderer extends Label implements TreeView.NodeRenderer {
    @Override
    public void render(Object node, Path path, TreeView treeView, boolean expanded,
        boolean selected, NodeCheckState checkState, boolean highlighted, boolean disabled) {
        Object labelFont = treeView.getStyles().get("font");
        if (labelFont instanceof Font) {
            getStyles().put("font", labelFont);
        }

        Object color = null;
        if (treeView.isEnabled() && !disabled) {
            if (selected) {
                if (treeView.isFocused()) {
                    color = treeView.getStyles().get("selectionColor");
                } else {
                    color = treeView.getStyles().get("inactiveSelectionColor");
                }
            } else {
                color = treeView.getStyles().get("color");
            }
        } else {
            color = treeView.getStyles().get("disabledColor");
        }

        if (color instanceof Color) {
            getStyles().put("color", color);
        }

        if (node != null) {
           NodeAdapter nodeAdapter = (NodeAdapter)node;
           setText(nodeAdapter.getText());
        }
    }
}
