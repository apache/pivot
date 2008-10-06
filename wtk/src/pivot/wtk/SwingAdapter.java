/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk;

import javax.swing.JComponent;

import pivot.util.ListenerList;

/**
 * Component that wraps a Swing <tt>JComponent</tt> for use in a pivot
 * application.
 *
 * @author tvolkert
 */
public class SwingAdapter extends Component {
    /**
     * Swing adapter listener list.
     *
     * @author tvolkert
     */
    private static class SwingAdapterListenerList extends ListenerList<SwingAdapterListener>
        implements SwingAdapterListener {
        public void swingComponentChanged(SwingAdapter swingAdapter,
            JComponent previousSwingComponent) {
            for (SwingAdapterListener listener : this) {
                listener.swingComponentChanged(swingAdapter, previousSwingComponent);
            }
        }
    }

    private JComponent swingComponent;

    private SwingAdapterListenerList swingAdapterListeners = new SwingAdapterListenerList();

    public SwingAdapter() {
        this(null);
    }

    public SwingAdapter(JComponent swingComponent) {
        this.swingComponent = swingComponent;
    }

    public JComponent getSwingComponent() {
        return swingComponent;
    }

    public void setSwingComponent(JComponent swingComponent) {
        JComponent previousSwingComponent = this.swingComponent;

        if (previousSwingComponent != swingComponent) {
            this.swingComponent = swingComponent;
            swingAdapterListeners.swingComponentChanged(this, previousSwingComponent);
        }
    }

    public ListenerList<SwingAdapterListener> getSwingAdapterListeners() {
        return swingAdapterListeners;
    }
}
