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
 * Container that fills it space and then arranges components
 * in a line, either vertically or horizontally.
 */
public class FillPane extends Container {
    private static class FillPaneListenerList extends WTKListenerList<FillPaneListener>
        implements FillPaneListener {
        @Override
        public void orientationChanged(FillPane fillPane) {
            for (FillPaneListener listener : this) {
                listener.orientationChanged(fillPane);
            }
        }
    }

    private Orientation orientation = null;
    private FillPaneListenerList fillPaneListeners = new FillPaneListenerList();

    public FillPane() {
        this(Orientation.HORIZONTAL);
    }

    public FillPane(Orientation orientation) {
        setOrientation(orientation);

        installSkin(FillPane.class);
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
            fillPaneListeners.orientationChanged(this);
        }
    }

    public ListenerList<FillPaneListener> getFillPaneListeners() {
        return fillPaneListeners;
    }
}
