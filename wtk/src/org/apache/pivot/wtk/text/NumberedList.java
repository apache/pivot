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
 * Element representing a numbered list.
 */
public class NumberedList extends List {
    /**
     * List numbering styles.
     */
    public enum Style {
        DECIMAL,
        LOWER_ALPHA,
        UPPER_ALPHA,
        LOWER_ROMAN,
        UPPER_ROMAN
    }

    private static class NumberedListListenerList extends ListenerList<NumberedListListener>
        implements NumberedListListener {
        @Override
        public void styleChanged(NumberedList numberedList, Style previousStyle) {
            for (NumberedListListener listener : this) {
                listener.styleChanged(numberedList, previousStyle);
            }
        }
    }

    private Style style = Style.DECIMAL;

    private NumberedListListenerList numberedListListeners = new NumberedListListenerList();

    public NumberedList() {
        super();
    }

    public NumberedList(NumberedList numberedList, boolean recursive) {
        super(numberedList, recursive);
        this.style = numberedList.style;
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
            numberedListListeners.styleChanged(this, previousStyle);
        }
    }

    @Override
    public NumberedList duplicate(boolean recursive) {
        return new NumberedList(this, recursive);
    }

    public ListenerList<NumberedListListener> getNumberedListListeners() {
        return numberedListListeners;
    }
}
