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
 * Abstract base class for block elements.
 * <p>
 * TODO Add margin, and line spacing properties.
 */
public abstract class Block extends Element {

    private static class BlockListenerList extends ListenerList<BlockListener> implements
        BlockListener {
        @Override
        public void horizontalAlignmentChanged(Block block, HorizontalAlignment previousHorizontalAlignment) {
            for (BlockListener listener : this) {
                listener.horizontalAlignmentChanged(block, previousHorizontalAlignment);
            }
        }
    }

    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;

    private BlockListenerList blockListeners = new BlockListenerList();

    public Block() {
        super();
    }

    public Block(Block blockElement, boolean recursive) {
        super(blockElement, recursive);
        this.horizontalAlignment = blockElement.horizontalAlignment;
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        HorizontalAlignment previousHorizontalAlignment = this.horizontalAlignment;
        if (previousHorizontalAlignment != horizontalAlignment) {
            this.horizontalAlignment = horizontalAlignment;
            blockListeners.horizontalAlignmentChanged(this, previousHorizontalAlignment);
        }
    }

    public ListenerList<BlockListener> getBlockListeners() {
        return blockListeners;
    }
}
