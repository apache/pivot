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
 * @author gbrown
 */
public class FlowPane extends Container {
    private static class FlowPaneListenerList extends ListenerList<FlowPaneListener>
        implements FlowPaneListener {
        public void orientationChanged(FlowPane flowPane) {
            for (FlowPaneListener listener : this) {
                listener.orientationChanged(flowPane);
            }
        }
    }

    private Orientation orientation = null;
    private FlowPaneListenerList flowPaneListeners = new FlowPaneListenerList();

    public FlowPane() {
        this(Orientation.HORIZONTAL);
    }

    public FlowPane(Orientation orientation) {
        this.orientation = orientation;

        installSkin(FlowPane.class);
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        if (this.orientation != orientation) {
            this.orientation = orientation;
            flowPaneListeners.orientationChanged(this);
        }
    }

    public void setOrientation(String orientation) {
        if (orientation == null) {
            throw new IllegalArgumentException();
        }

        setOrientation(Orientation.decode(orientation));
    }

    public ListenerList<FlowPaneListener> getFlowPaneListeners() {
        return flowPaneListeners;
    }
}
