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

import pivot.util.ListenerList;

public class Expander extends TitlePane {
    private class ExpanderListenerList extends ListenerList<ExpanderListener>
    implements ExpanderListener {
        public void expandedChanged(Expander expander) {
            for (ExpanderListener listener : this) {
                listener.expandedChanged(expander);
            }
        }
    }

    private boolean expanded = true;
    private ExpanderListenerList expanderListeners = new ExpanderListenerList();

    public Expander() {
        installSkin(Expander.class);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        if (expanded != this.expanded) {
            this.expanded = expanded;
            expanderListeners.expandedChanged(this);
        }
    }

    public ListenerList<ExpanderListener> getExpanderListeners() {
        return expanderListeners;
    }
}
