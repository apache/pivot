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

import org.apache.pivot.scene.Visual;
import org.apache.pivot.util.ObservableList;

/**
 * Class that displays a hierarchical data structure, allowing a user to select
 * one or more paths.
 */
// TODO Not abstract; just for prototyping
public abstract class TreeView extends Component {
    /**
     * Interface defining a tree view node.
     */
    public interface Node {
        public ObservableList<Node> getNodes();
    }

    public interface NodeRenderer extends Visual {
        // TODO
    }

    public Node getRoot() {
        // TODO
        return null;
    }

    // TODO
}
