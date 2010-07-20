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
 * Abstract base class for list elements.
 * <p>
 * TODO Add indent and item spacing properties.
 */
public abstract class List extends Block {
    /**
     * Element representing a list item.
     */
    public static class Item extends Element {
        public Item() {
            super();
        }

        public Item(Item item, boolean recursive) {
            super(item, recursive);
        }

        @Override
        public void insert(Node node, int index) {
            if (!(node instanceof Block)) {
                throw new IllegalArgumentException("Child node must be an instance of "
                    + Block.class.getName());
            }

            super.insert(node, index);
        }

        @Override
        public Item duplicate(boolean recursive) {
            return new Item(this, recursive);
        }
    }

    public List() {
        super();
    }

    public List(List list, boolean recursive) {
        super(list, recursive);
    }

    @Override
    public void insert(Node node, int index) {
        if (!(node instanceof Item)) {
            throw new IllegalArgumentException("Child node must be an instance of "
                + Item.class.getName());
        }

        super.insert(node, index);
    }
}
