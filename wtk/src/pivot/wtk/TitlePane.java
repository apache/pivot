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
 * Abstract base class for "title panes", containers that contain a single
 * content component and present a title.
 */
public abstract class TitlePane extends Container {
    private class TitlePaneListenerList extends ListenerList<TitlePaneListener>
        implements TitlePaneListener {
        public void titleChanged(TitlePane titlePane, String previousTitle) {
            for (TitlePaneListener listener : this) {
                listener.titleChanged(titlePane, previousTitle);
            }
        }

        public void contentChanged(TitlePane titlePane, Component previousContentComponent) {
            for (TitlePaneListener listener : this) {
                listener.contentChanged(titlePane, previousContentComponent);
            }
        }
    }

    private String title = null;
    private TitlePaneListenerList titlePaneListeners = new TitlePaneListenerList();

    @Override
    public void insert(Component component, int index) {
        if (getLength() > 0) {
            throw new IllegalStateException("Content component is already set.");
        }

        super.insert(component, index);
    }

    /**
     * Returns the pane's title.
     *
     * @return
     * The pane's title, or <tt>null</tt> if no title is set.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the pane's title.
     *
     * @param title
     * The new title, or <tt>null</tt> for no title.
     */
    public void setTitle(String title) {
        String previousTitle = this.title;

        if ((previousTitle != null
                && title != null
                && !previousTitle.equals(title))
            || previousTitle != title) {
            this.title = title;
            titlePaneListeners.titleChanged(this, previousTitle);
        }
    }

    public Component getContent() {
        return (getLength() > 0) ? get(0) : null;
    }

    public void setContent(Component content) {
        Component previousContent = getContent();

        if (content != previousContent) {
            // Remove any previous content component
            if (previousContent != null) {
                remove(previousContent);
            }

            // Add the component
            if (content != null) {
                add(content);
            }

            titlePaneListeners.contentChanged(this, previousContent);
        }
    }

    public ListenerList<TitlePaneListener> getTitlePaneListeners() {
        return titlePaneListeners;
    }
}
