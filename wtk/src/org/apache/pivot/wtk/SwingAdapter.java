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

import java.awt.DefaultKeyboardFocusManager;
import java.awt.KeyboardFocusManager;

import javax.swing.JComponent;
import javax.swing.RepaintManager;

import org.apache.pivot.util.ListenerList;

/**
 * Allows a Swing component to be used within a Pivot application.
 */
public class SwingAdapter extends Component {
    private static class SwingAdapterListenerList extends ListenerList<SwingAdapterListener>
        implements SwingAdapterListener {
        public void swingComponentChanged(SwingAdapter swingAdapter, JComponent previousSwingComponent) {
            for (SwingAdapterListener listener : this) {
                listener.swingComponentChanged(swingAdapter, previousSwingComponent);
            }
        }
    }

    static {
        RepaintManager.setCurrentManager(new SwingAdapterRepaintManager());
        KeyboardFocusManager.setCurrentKeyboardFocusManager(new SwingAdapterKeyboardFocusManager());
    }

    private JComponent swingComponent = null;

    private SwingAdapterListenerList swingAdapterListeners = new SwingAdapterListenerList();

    public SwingAdapter() {
        installThemeSkin(SwingAdapter.class);
    }

    public JComponent getSwingComponent() {
        return swingComponent;
    }

    public void setSwingComponent(JComponent swingComponent) {
        JComponent previousSwingComponent = this.swingComponent;

        if (previousSwingComponent != swingComponent) {
            if (previousSwingComponent != null) {
                previousSwingComponent.putClientProperty(SwingAdapter.class, null);
            }

            if (swingComponent != null) {
                swingComponent.putClientProperty(SwingAdapter.class, this);
            }

            this.swingComponent = swingComponent;

            swingAdapterListeners.swingComponentChanged(this, previousSwingComponent);
        }
    }

    public ListenerList<SwingAdapterListener> getSwingAdapterListeners() {
        return swingAdapterListeners;
    }
}

class SwingAdapterRepaintManager extends RepaintManager {
    public void addDirtyRegion(JComponent component, int x, int y, int width, int height) {
        super.addDirtyRegion(component, x, y, width, height);

        SwingAdapter swingAdapter = (SwingAdapter)component.getClientProperty(SwingAdapter.class);
        if (swingAdapter != null) {
            swingAdapter.repaint(x, y, width, height);
        }
    }
}

class SwingAdapterKeyboardFocusManager extends DefaultKeyboardFocusManager {
    // TODO
}

