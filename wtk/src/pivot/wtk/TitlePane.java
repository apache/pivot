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

import pivot.collections.Sequence;
import pivot.util.ListenerList;

/**
 * NOTE This class is abstract because it does not install a skin by default.
 *
 * @author gbrown
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
    private Component content = null;
    private TitlePaneListenerList titlePaneListeners = new TitlePaneListenerList();

    public TitlePane() {
        super();
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
        return content;
    }

    public void setContent(Component content) {
        if (content != this.content) {
            if (content != null) {
                if (content.getParent() != null) {
                    throw new IllegalArgumentException("Component already has a parent.");
                }

                // Add the component
                add(content);
            }

            // Set the component as the new content component (note that we
            // set the new component before removing the old one so two
            // content change events don't get fired)
            Component previousContent = this.content;
            this.content = content;

            // Remove any previous content component
            if (previousContent != null) {
                remove(previousContent);
            }

            titlePaneListeners.contentChanged(this, previousContent);
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

    public ListenerList<TitlePaneListener> getTitlePaneListeners() {
        return titlePaneListeners;
    }
}
