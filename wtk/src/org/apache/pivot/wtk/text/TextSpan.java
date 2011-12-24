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
 * Element representing an inline range of styled characters.
 */
public class TextSpan extends Element {
    public TextSpan() {
        super();
    }

    public TextSpan(TextSpan span, boolean recursive) {
        super(span, recursive);
    }

    public int add(String text) {
        return add(new TextNode(text));
    }

    @Override
    public void insert(Node node, int index) {
        if (node instanceof Block) {
            throw new IllegalArgumentException("Child node must not be an instance of "
                + Block.class.getName() + ", " + node.getClass());
        }

        super.insert(node, index);
    }

    @Override
    public TextSpan duplicate(boolean recursive) {
        return new TextSpan(this, recursive);
    }

    @Override
    public TextSpan getRange(int offset, int characterCount) {
        return (TextSpan) super.getRange(offset, characterCount);
    }
}
