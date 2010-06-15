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

import java.awt.Color;
import java.awt.Font;

import org.apache.pivot.collections.Sequence;

/**
 * Element listener interface.
 */
public interface ElementListener {
    public class Adapter implements ElementListener {
        @Override
        public void nodeInserted(Element element, int index) {
        }
        @Override
        public void nodesRemoved(Element element, int index, Sequence<Node> nodes) {
        }
        @Override
        public void fontChanged(Element element, Font previousFont) {
        }
        @Override
        public void backgroundColorChanged(Element element, Color previousBackgroundColor) {
        }
        @Override
        public void foregroundColorChanged(Element element, Color previousForegroundColor) {
        }
    }
    /**
     * Called when a node has been inserted into an element.
     *
     * @param element
     * @param index
     */
    public void nodeInserted(Element element, int index);

    /**
     * Called when nodes have been removed from an element.
     *
     * @param element
     * @param index
     * @param nodes
     */
    public void nodesRemoved(Element element, int index, Sequence<Node> nodes);

    /**
     * Called when the font has changed.
     *
     * @param element
     * @param previousFont
     */
    public void fontChanged(Element element, java.awt.Font previousFont);


    /**
     * Called when the background color has changed.
     *
     * @param element
     * @param previousBackgroundColor
     */
    public void backgroundColorChanged(Element element, Color previousBackgroundColor);

    /**
     * Called when the foreground color has changed.
     *
     * @param element
     * @param previousForegroundColor
     */
    public void foregroundColorChanged(Element element, Color previousForegroundColor);

}
