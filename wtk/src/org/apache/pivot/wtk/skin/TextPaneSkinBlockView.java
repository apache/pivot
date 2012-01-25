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
package org.apache.pivot.wtk.skin;

import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.text.Block;
import org.apache.pivot.wtk.text.BlockListener;

abstract class TextPaneSkinBlockView extends TextPaneSkinElementView implements
    BlockListener {

    public TextPaneSkinBlockView(Block block) {
        super(block);
    }

    @Override
    protected void attach() {
        super.attach();

        Block block = (Block)getNode();
        block.getBlockListeners().add(this);
    }

    @Override
    protected void detach() {
        super.detach();

        Block block = (Block)getNode();
        block.getBlockListeners().remove(this);
    }

    @Override
    public void horizontalAlignmentChanged(Block block, HorizontalAlignment previousHorizontalAlignment) {
        invalidateUpTree();
    }
}
