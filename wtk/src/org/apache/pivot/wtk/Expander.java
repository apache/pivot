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
 * Navigation container that allows a user to expand and collapse a content
 * component.
 */
@DefaultProperty("content")
public class Expander extends Container {
    private static class ExpanderListenerList extends WTKListenerList<ExpanderListener>
    implements ExpanderListener {
        @Override
        public void titleChanged(Expander expander, String previousTitle) {
            for (ExpanderListener listener : this) {
                listener.titleChanged(expander, previousTitle);
            }
        }

        @Override
        public void collapsibleChanged(Expander expander) {
            for (ExpanderListener listener : this) {
                listener.collapsibleChanged(expander);
            }
        }

        @Override
        public Vote previewExpandedChange(Expander expander) {
            Vote vote = Vote.APPROVE;

            for (ExpanderListener listener : this) {
                vote = vote.tally(listener.previewExpandedChange(expander));
            }

            return vote;
        }

        @Override
        public void expandedChangeVetoed(Expander expander, Vote reason) {
            for (ExpanderListener listener : this) {
                listener.expandedChangeVetoed(expander, reason);
            }
        }

        @Override
        public void expandedChanged(Expander expander) {
            for (ExpanderListener listener : this) {
                listener.expandedChanged(expander);
            }
        }

        @Override
        public void contentChanged(Expander expander, Component previousContent) {
            for (ExpanderListener listener : this) {
                listener.contentChanged(expander, previousContent);
            }
        }
    }

    private String title = null;
    private boolean collapsible = true;
    private boolean expanded = true;
    private Component content = null;

    private ExpanderListenerList expanderListeners = new ExpanderListenerList();

    public Expander() {
        installSkin(Expander.class);
    }

    /**
     * Returns the expander's title.
     *
     * @return
     * The pane's title, or <tt>null</tt> if no title is set.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the expander's title.
     *
     * @param title
     * The new title, or <tt>null</tt> for no title.
     */
    public void setTitle(String title) {
        String previousTitle = this.title;

        if (title != previousTitle) {
            this.title = title;
            expanderListeners.titleChanged(this, previousTitle);
        }
    }

    /**
     * Returns the expander's collapsible flag.
     *
     * @return
     * The collapsible flag
     */
    public boolean isCollapsible() {
        return collapsible;
    }

    /**
     * Sets the expander's collapsible flag.
     *
     * @param collapsible
     * The collapsible flag
     */
    public void setCollapsible(boolean collapsible) {
        if (collapsible != this.collapsible) {
            if (!collapsible
                && !expanded) {
                throw new IllegalStateException();
            }

            this.collapsible = collapsible;
            expanderListeners.collapsibleChanged(this);
        }
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        if (expanded != this.expanded) {
            if (!collapsible
                && !expanded) {
                throw new IllegalStateException();
            }

            Vote vote = expanderListeners.previewExpandedChange(this);

            if (vote == Vote.APPROVE) {
                this.expanded = expanded;
                expanderListeners.expandedChanged(this);
            } else {
                expanderListeners.expandedChangeVetoed(this, vote);
            }
        }
    }

    public Component getContent() {
        return content;
    }

    public void setContent(Component content) {
        Component previousContent = this.content;

        if (content != previousContent) {
            this.content = null;

            // Remove any previous content component
            if (previousContent != null) {
                remove(previousContent);
            }

            // Add the component
            if (content != null) {
                add(content);
            }

            this.content = content;

            expanderListeners.contentChanged(this, previousContent);
        }
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);
            if (component == content) {
                throw new UnsupportedOperationException();
            }
        }

        // Call the base method to remove the components
        return super.remove(index, count);
    }

    public ListenerList<ExpanderListener> getExpanderListeners() {
        return expanderListeners;
    }
}
