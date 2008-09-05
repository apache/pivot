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

/**
 * Container that can be expanded or collapsed to respectively show or hide its
 * children. When expanded, the rollup's children are displayed vertically like
 * a vertical flow pane. When collapsed, only the rollup's first child is
 * displayed.
 *
 * @author gbrown
 * @author tvolkert
 */
public class Rollup extends pivot.wtk.Container {
    private static class RollupListenerList extends ListenerList<RollupListener>
        implements RollupListener {
        public void expandedChanged(Rollup rollup) {
            for (RollupListener listener : this) {
                listener.expandedChanged(rollup);
            }
        }
    }

    private boolean expanded = true;
    private RollupListenerList rollupListeners = new RollupListenerList();

    public Rollup() {
        this(false, null);
    }

    public Rollup(boolean expanded) {
        this(expanded, null);
    }

    public Rollup(Component firstChild) {
        this(false, firstChild);
    }

    public Rollup(boolean expanded, Component firstChild) {
        this.expanded = expanded;

        installSkin(Rollup.class);

        if (firstChild != null) {
            add(firstChild);
        }
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        if (expanded != this.expanded) {
            this.expanded = expanded;
            rollupListeners.expandedChanged(this);
        }
    }

    public ListenerList<RollupListener> getRollupListeners() {
        return rollupListeners;
    }
}
