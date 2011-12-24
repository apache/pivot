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
package org.apache.pivot.tutorials.bxmlexplorer;

import java.net.URL;

import org.apache.pivot.wtk.ScrollPane.ScrollBarPolicy;
import org.apache.pivot.wtk.content.TreeNode;

/**
 * Node in the component explorer's tree view that represents a component type.
 */
public class ComponentNode extends TreeNode {
    private URL src = null;
    private ScrollBarPolicy horizontalScrollBarPolicy = ScrollBarPolicy.FILL;
    private ScrollBarPolicy verticalScrollBarPolicy = ScrollBarPolicy.FILL;

    public ComponentNode() {
        setIcon(ComponentNode.class.getResource("/org/apache/pivot/tutorials/page_white.png"));
    }

    public URL getSrc() {
        return src;
    }

    public void setSrc(URL src) {
        this.src = src;
    }

    public ScrollBarPolicy getHorizontalScrollBarPolicy() {
        return horizontalScrollBarPolicy;
    }

    public void setHorizontalScrollBarPolicy(ScrollBarPolicy horizontalScrollBarPolicy) {
        this.horizontalScrollBarPolicy = horizontalScrollBarPolicy;
    }

    public ScrollBarPolicy getVerticalScrollBarPolicy() {
        return verticalScrollBarPolicy;
    }

    public void setVerticalScrollBarPolicy(ScrollBarPolicy verticalScrollBarPolicy) {
        this.verticalScrollBarPolicy = verticalScrollBarPolicy;
    }
}
