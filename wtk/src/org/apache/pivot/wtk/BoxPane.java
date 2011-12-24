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
package org.apache.pivot.wtk;

import org.apache.pivot.util.ListenerList;

/**
 * Container that arranges components in a line, either vertically or
 * horizontally.
 *
 * <p>Layout considerations: Note that by default, a BoxPane does not pass any
 * preferred size information to its components.  That means, for example,
 * that the preferred width of a horizontal BoxPane is simply the sum of
 * the preferred widths of its components, plus whatever extra space is
 * dictated by its <code>padding</code> and <code>spacing</code> styles.
 * This is also true in the orthogonal dimension if the <code>fill</code>
 * style is not set: the preferred height of a horizontal BoxPane is the
 * maximum preferred height of its components (plus padding, if any).
 *
 * <p>If a BoxPane is given less than its preferred size by its parent
 * container, then the contents will be clipped; if given more than its
 * preferred size, the contents will not be stretched to fill the space.
 *
 * <p>This behavior of BoxPane can be useful for "canceling" the layout
 * effects of a parent container and/or to align a component that does
 * not have its own alignment styles.  For example, if you place a
 * PushButton in a cell of a TablePane, the button will expand to fill the
 * whole cell.  If instead you place the button inside a BoxPane in the
 * cell, it will retain its natural size, and you can set the
 * horizontalAlignment and verticalAlignment styles of the BoxPane to, say,
 * center the button within the cell.
 */
public class BoxPane extends Container {
    private static class BoxPaneListenerList extends WTKListenerList<BoxPaneListener>
        implements BoxPaneListener {
        @Override
        public void orientationChanged(BoxPane boxPane) {
            for (BoxPaneListener listener : this) {
                listener.orientationChanged(boxPane);
            }
        }
    }

    private Orientation orientation = null;
    private BoxPaneListenerList boxPaneListeners = new BoxPaneListenerList();

    public BoxPane() {
        this(Orientation.HORIZONTAL);
    }

    public BoxPane(Orientation orientation) {
        setOrientation(orientation);

        installSkin(BoxPane.class);
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        if (orientation == null) {
            throw new IllegalArgumentException();
        }

        if (this.orientation != orientation) {
            this.orientation = orientation;
            boxPaneListeners.orientationChanged(this);
        }
    }

    public ListenerList<BoxPaneListener> getBoxPaneListeners() {
        return boxPaneListeners;
    }
}
