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

/**
 * Node representing the root of an element hierarchy.
 */
public class Document extends Block {
    public Document() {
        super();
    }

    public Document(Document document, boolean recursive) {
        super(document, recursive);
    }

    @Override
    protected void setParent(Element parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Document duplicate(boolean recursive) {
        return new Document(this, recursive);
    }

    @Override
    public void insert(Node node, int index) {
        if (!(node instanceof Block)) {
            throw new IllegalArgumentException("Child node must be an instance of "
                + Block.class.getName());
        }

        super.insert(node, index);
    }
}
