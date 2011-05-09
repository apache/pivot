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

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Vote;

/**
 * Container that can be expanded or collapsed to respectively show or hide its
 * content. A rollup has a heading component that is always visible, and when
 * the user expands the rollup, its content component will be shown beneath the
 * heading.
 */
@DefaultProperty("content")
public class Rollup extends Container {
    private static class RollupListenerList extends WTKListenerList<RollupListener>
        implements RollupListener {
        @Override
        public void headingChanged(Rollup rollup, Component previousHeading) {
            for (RollupListener listener : this) {
                listener.headingChanged(rollup, previousHeading);
            }
        }

        @Override
        public void contentChanged(Rollup rollup, Component previousContent) {
            for (RollupListener listener : this) {
                listener.contentChanged(rollup, previousContent);
            }
        }

        @Override
        public void collapsibleChanged(Rollup rollup) {
            for (RollupListener listener : this) {
                listener.collapsibleChanged(rollup);
            }
        }
    }

    private static class RollupStateListenerList extends WTKListenerList<RollupStateListener>
        implements RollupStateListener {
        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            Vote vote = Vote.APPROVE;

            for (RollupStateListener listener : this) {
                vote = vote.tally(listener.previewExpandedChange(rollup));
            }

            return vote;
        }

        @Override
        public void expandedChangeVetoed(Rollup rollup, Vote reason) {
            for (RollupStateListener listener : this) {
                listener.expandedChangeVetoed(rollup, reason);
            }
        }

        @Override
        public void expandedChanged(Rollup rollup) {
            for (RollupStateListener listener : this) {
                listener.expandedChanged(rollup);
            }
        }
    }

    private Component heading = null;
    private Component content = null;

    private boolean expanded = true;
    private boolean collapsible = true;

    private RollupListenerList rollupListeners = new RollupListenerList();
    private RollupStateListenerList rollupStateListeners = new RollupStateListenerList();

    public Rollup() {
        this(false, null);
    }

    public Rollup(boolean expanded) {
        this(expanded, null);
    }

    public Rollup(Component content) {
        this(false, content);
    }

    public Rollup(boolean expanded, Component content) {
        this.expanded = expanded;

        installSkin(Rollup.class);

        if (content != null) {
            setContent(content);
        }
    }

    public Component getHeading() {
        return heading;
    }

    public void setHeading(Component heading) {
       Component previousHeading = this.heading;

        if (heading != previousHeading) {
            // Remove any previous heading component
            this.heading = null;

            if (previousHeading != null) {
                remove(previousHeading);
            }

            // Set the new heading component
            if (heading != null) {
                add(heading);
            }

            this.heading = heading;

            rollupListeners.headingChanged(this, previousHeading);
        }
    }

    public Component getContent() {
        return content;
    }

    public void setContent(Component content) {
       Component previousContent = this.content;

        if (content != previousContent) {
            // Remove any previous content component
            this.content = null;

            if (previousContent != null) {
                remove(previousContent);
            }

            // Set the new content component
            if (content != null) {
                add(content);
            }

            this.content = content;

            rollupListeners.contentChanged(this, previousContent);
        }
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        if (expanded != this.expanded) {
            Vote vote = rollupStateListeners.previewExpandedChange(this);

            if (vote == Vote.APPROVE) {
                this.expanded = expanded;
                rollupStateListeners.expandedChanged(this);
            } else {
                rollupStateListeners.expandedChangeVetoed(this, vote);
            }
        }
    }

    public boolean isCollapsible() {
        return collapsible;
    }

    public void setCollapsible(boolean collapsible) {
        if (this.collapsible != collapsible) {
            this.collapsible = collapsible;
            rollupListeners.collapsibleChanged(this);
        }
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);
            if (component == heading
                || component == content) {
                throw new UnsupportedOperationException();
            }
        }

        // Call the base method to remove the components
        return super.remove(index, count);
    }

    public ListenerList<RollupListener> getRollupListeners() {
        return rollupListeners;
    }

    public ListenerList<RollupStateListener> getRollupStateListeners() {
        return rollupStateListeners;
    }
}
