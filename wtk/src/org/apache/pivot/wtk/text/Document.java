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

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.util.ClassUtils;

/**
 * Node representing the root of an element hierarchy.
 */
public class Document extends Block {
    public Document() {
        super();
    }

    public Document(final Document document, final boolean recursive) {
        super(document, recursive);
    }

    @Override
    @UnsupportedOperation
    protected void setParent(final Element parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Document duplicate(final boolean recursive) {
        return new Document(this, recursive);
    }

    @Override
    public void insert(final Node node, final int index) {
        if (!ClassUtils.instanceOf(node, Block.class, ComponentNode.class, ImageNode.class)) {
            throw new IllegalArgumentException("Child node ("
                + node.getClass().getSimpleName() + ") must be an instance of "
                + Block.class.getName() + " or "
                + ComponentNode.class.getName() + " or "
                + ImageNode.class.getName());
        }

        super.insert(node, index);
    }
}
