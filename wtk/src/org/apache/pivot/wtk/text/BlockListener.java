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

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.HorizontalAlignment;

/**
 * Block listener interface.
 */
public interface BlockListener {
    /**
     * Block listeners.
     */
    public static class Listeners extends ListenerList<BlockListener> implements BlockListener {
        @Override
        public void horizontalAlignmentChanged(Block block,
            HorizontalAlignment previousHorizontalAlignment) {
            forEach(listener -> listener.horizontalAlignmentChanged(block, previousHorizontalAlignment));
        }
    }

    /**
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public class Adapter implements BlockListener {
        @Override
        public void horizontalAlignmentChanged(Block block,
            HorizontalAlignment previousHorizontalAlignment) {
            // empty block
        }
    }

    /**
     * Called when the horizontal alignment has changed.
     *
     * @param block                       The text block in question.
     * @param previousHorizontalAlignment The previous alignment value.
     */
    default void horizontalAlignmentChanged(Block block,
        HorizontalAlignment previousHorizontalAlignment) {
    }
}
