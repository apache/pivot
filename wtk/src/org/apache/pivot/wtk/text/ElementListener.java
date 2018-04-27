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
import org.apache.pivot.util.ListenerList;

/**
 * Element listener interface.
 */
public interface ElementListener {
    /**
     * Element listeners.
     */
    public static class Listeners extends ListenerList<ElementListener> implements
        ElementListener {
        @Override
        public void nodeInserted(Element element, int index) {
            forEach(listener -> listener.nodeInserted(element, index));
        }

        @Override
        public void nodesRemoved(Element element, int index, Sequence<Node> nodes) {
            forEach(listener -> listener.nodesRemoved(element, index, nodes));
        }

        @Override
        public void fontChanged(Element element, Font previousFont) {
            forEach(listener -> listener.fontChanged(element, previousFont));
        }

        @Override
        public void backgroundColorChanged(Element element, Color previousBackgroundColor) {
            forEach(listener -> listener.backgroundColorChanged(element, previousBackgroundColor));
        }

        @Override
        public void foregroundColorChanged(Element element, Color previousForegroundColor) {
            forEach(listener -> listener.foregroundColorChanged(element, previousForegroundColor));
        }

        @Override
        public void underlineChanged(Element element) {
            forEach(listener -> listener.underlineChanged(element));
        }

        @Override
        public void strikethroughChanged(Element element) {
            forEach(listener -> listener.strikethroughChanged(element));
        }
    }

    /**
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public class Adapter implements ElementListener {
        @Override
        public void nodeInserted(Element element, int index) {
            // empty block
        }

        @Override
        public void nodesRemoved(Element element, int index, Sequence<Node> nodes) {
            // empty block
        }

        @Override
        public void fontChanged(Element element, Font previousFont) {
            // empty block
        }

        @Override
        public void backgroundColorChanged(Element element, Color previousBackgroundColor) {
            // empty block
        }

        @Override
        public void foregroundColorChanged(Element element, Color previousForegroundColor) {
            // empty block
        }

        @Override
        public void underlineChanged(Element element) {
            // empty block
        }

        @Override
        public void strikethroughChanged(Element element) {
            // empty block
        }
    }

    /**
     * Called when a node has been inserted into an element.
     *
     * @param element The element that has changed.
     * @param index   Where in the element's node sequence the new one was inserted.
     */
    default void nodeInserted(Element element, int index) {
    }

    /**
     * Called when nodes have been removed from an element.
     *
     * @param element The element that changed.
     * @param index   The starting index of where nodes were removed.
     * @param nodes   The actual sequence of removed nodes.
     */
    default void nodesRemoved(Element element, int index, Sequence<Node> nodes) {
    }

    /**
     * Called when the font has changed.
     *
     * @param element      The element that changed.
     * @param previousFont What the font used to be.
     */
    default void fontChanged(Element element, java.awt.Font previousFont) {
    }

    /**
     * Called when the background color has changed.
     *
     * @param element                 The element that changed.
     * @param previousBackgroundColor What the background color was before the change.
     */
    default void backgroundColorChanged(Element element, Color previousBackgroundColor) {
    }

    /**
     * Called when the foreground color has changed.
     *
     * @param element                 The element whose color changed.
     * @param previousForegroundColor The old foreground color.
     */
    default void foregroundColorChanged(Element element, Color previousForegroundColor) {
    }

    /**
     * Called when underline style has changed.
     *
     * @param element The element that changed.
     */
    default void underlineChanged(Element element) {
    }

    /**
     * Called when strikethrough style has changed.
     *
     * @param element The element that changed.
     */
    default void strikethroughChanged(Element element) {
    }
}
