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

/**
 * Element representing a bulleted list.
 */
public class BulletedList extends List {
    /**
     * List bullet styles.
     */
    public enum Style {
        /** unicode character 0x2022 aka. "BULLET" */
        CIRCLE,
        /** unicode character 0x25e6 aka. "WHITE BULLET" */
        CIRCLE_OUTLINE
    }

    private static class BulletedListListenerList extends ListenerList<BulletedListListener>
        implements BulletedListListener {
        @Override
        public void styleChanged(BulletedList bulletedList, Style previousStyle) {
            for (BulletedListListener listener : this) {
                listener.styleChanged(bulletedList, previousStyle);
            }
        }
    }

    private Style style = Style.CIRCLE;

    private BulletedListListenerList bulletedListListeners = new BulletedListListenerList();

    public BulletedList() {
        super();
    }

    public BulletedList(BulletedList bulletedList, boolean recursive) {
        super(bulletedList, recursive);
        this.style = bulletedList.style;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        if (style == null) {
            throw new IllegalArgumentException("style is null.");
        }

        Style previousStyle = this.style;
        if (previousStyle != style) {
            this.style = style;
            bulletedListListeners.styleChanged(this, previousStyle);
        }
    }

    @Override
    public BulletedList duplicate(boolean recursive) {
        return new BulletedList(this, recursive);
    }

    public ListenerList<BulletedListListener> getBulletedListListeners() {
        return bulletedListListeners;
    }
}
