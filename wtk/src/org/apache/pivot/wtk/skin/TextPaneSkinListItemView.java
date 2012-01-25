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

import java.util.Iterator;

import org.apache.pivot.wtk.text.TextNode;


class TextPaneSkinListItemView extends TextPaneSkinVerticalElementView {

    private TextNode indexTextNode;
    private TextPaneSkinTextNodeView indexTextNodeView;

    public TextPaneSkinListItemView(org.apache.pivot.wtk.text.List.Item listItem) {
        super(listItem);

        this.indexTextNode = new TextNode("");
    }

    @Override
    protected void attach() {
        super.attach();

        // add an extra TextNodeView to render the index text
        indexTextNodeView = new TextPaneSkinTextNodeView(indexTextNode);
        indexTextNodeView.setLocation(0, 0);
        insert(indexTextNodeView, 0);
    }

    public void setIndexText(String indexText) {
        indexTextNode.setText(indexText);
        indexTextNodeView.invalidateUpTree();
    }

    @Override
    protected void childLayout(int breakWidth) {
        indexTextNodeView.layout(breakWidth);

        breakWidth -= indexTextNodeView.getWidth();
        int itemsWidth = 0;
        int itemsY = 0;

        // skip the first item, it's the indexText nodeView
        Iterator<TextPaneSkinNodeView> iterator = this.iterator();
        iterator.next();

        for ( ; iterator.hasNext(); ) {
            TextPaneSkinNodeView nodeView = iterator.next();
            nodeView.layout(breakWidth);

            nodeView.setLocation(indexTextNodeView.getWidth(), itemsY);

            itemsWidth = Math.max(itemsWidth, nodeView.getWidth());
            itemsY += nodeView.getHeight();
        }

        int width = itemsWidth + indexTextNodeView.getWidth();
        int height = Math.max(itemsY, indexTextNodeView.getHeight());

        setSize(width, height);
    }

    public int getIndexTextWidth() {
        return indexTextNodeView.getWidth();
    }
}