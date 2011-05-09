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

/**
 * Container that displays a border.
 */
@DefaultProperty("content")
public class Border extends Container {
    private static class BorderListenerList extends WTKListenerList<BorderListener>
        implements BorderListener {
        @Override
        public void titleChanged(Border border, String previousTitle) {
            for (BorderListener listener : this) {
                listener.titleChanged(border, previousTitle);
            }
        }

        @Override
        public void contentChanged(Border border, Component previousContent) {
            for (BorderListener listener : this) {
                listener.contentChanged(border, previousContent);
            }
        }
    }

    private String title = null;
    private Component content = null;
    private BorderListenerList borderListeners = new BorderListenerList();

    public Border() {
        this(null);
    }

    public Border(Component content) {
        installSkin(Border.class);

        setContent(content);
    }

    /**
     * Returns the border's title.
     *
     * @return
     * The border's title, or <tt>null</tt> if no title is set.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the border's title.
     *
     * @param title
     * The new title, or <tt>null</tt> for no title.
     */
    public void setTitle(String title) {
        String previousTitle = this.title;

        if (previousTitle != title) {
            this.title = title;
            borderListeners.titleChanged(this, previousTitle);
        }
    }

    /**
     * Returns the border's content component.
     *
     * @return
     * The border's content component, or <tt>null</tt> if the border does
     * not have a content component.
     */
    public Component getContent() {
        return content;
    }

    /**
     * Sets the border's content component.
     *
     * @param content
     * The border's content component, or <tt>null</tt> for no content.
     */
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

            borderListeners.contentChanged(this, previousContent);
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

    public ListenerList<BorderListener> getBorderListeners() {
        return borderListeners;
    }
}
