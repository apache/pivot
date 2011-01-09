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

import org.apache.pivot.scene.Group;
import org.apache.pivot.scene.Layout;
import org.apache.pivot.scene.Node;

/**
 * A layout with no preferred size that assigns each child nodes its preferred
 * size but does not change its position.
 */
public class StageLayout implements Layout {
    @Override
    public int getBaseline(Group group, int width, int height) {
        return -1;
    }

    @Override
    public int getPreferredHeight(Group group, int width) {
        return 0;
    }

    @Override
    public int getPreferredWidth(Group group, int height) {
        return 0;
    }

    @Override
    public void layout(Group group) {
        for (Node node : group.getNodes()) {
            node.setSize(node.getPreferredWidth(-1), node.getPreferredHeight(-1));
        }
    }
}
