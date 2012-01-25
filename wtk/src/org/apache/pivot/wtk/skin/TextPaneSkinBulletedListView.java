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

import org.apache.pivot.wtk.text.BulletedList;
import org.apache.pivot.wtk.text.BulletedListListener;

class TextPaneSkinBulletedListView extends TextPaneSkinListView implements BulletedListListener {

    public TextPaneSkinBulletedListView(BulletedList bulletedList) {
        super(bulletedList);
    }

    @Override
    protected void attach() {
        super.attach();

        BulletedList bulletedList = (BulletedList)getNode();
        bulletedList.getBulletedListListeners().add(this);
    }

    @Override
    protected void detach() {
        super.detach();

        BulletedList bulletedList = (BulletedList)getNode();
        bulletedList.getBulletedListListeners().remove(this);
    }

    @Override
    protected void childLayout(int breakWidth) {
        BulletedList bulletedList = (BulletedList)getNode();

        for (TextPaneSkinNodeView nodeView : this) {
            TextPaneSkinListItemView listItemView = (TextPaneSkinListItemView)nodeView;

            switch (bulletedList.getStyle()) {
                case CIRCLE:
                    listItemView.setIndexText("\u2022 ");
                    break;
                case CIRCLE_OUTLINE:
                    listItemView.setIndexText("\u25e6 ");
                    break;
            }
        }

        this.maxIndexTextWidth = 0;
        for (TextPaneSkinNodeView nodeView : this) {
            TextPaneSkinListItemView listItemView = (TextPaneSkinListItemView)nodeView;
            this.maxIndexTextWidth = Math.max(this.maxIndexTextWidth,
                listItemView.getIndexTextWidth());
        }

        super.childLayout(breakWidth);
    }

    @Override
    public void styleChanged(BulletedList bulletedList, BulletedList.Style previousStyle) {
        invalidateUpTree();
    }
}
