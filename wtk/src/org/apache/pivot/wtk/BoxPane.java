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

import java.util.Locale;

import org.apache.pivot.util.ListenerList;

/**
 * Container that arranges components in a line, either vertically or
 * horizontally.
 */
public class BoxPane extends Container {
    private static class BoxPaneListenerList extends ListenerList<BoxPaneListener>
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

    public void setOrientation(String orientation) {
        if (orientation == null) {
            throw new IllegalArgumentException();
        }

        setOrientation(Orientation.valueOf(orientation.toUpperCase(Locale.ENGLISH)));
    }

    public ListenerList<BoxPaneListener> getBoxPaneListeners() {
        return boxPaneListeners;
    }
}
