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
 * TODO Create subclasses that install inset and outset skins, which will
 * probably have different style properties than BorderSkin.
 *
 * @author gbrown
 */
@ComponentInfo(icon="Border.png")
public class Border extends Container {
    private class BorderListenerList extends ListenerList<BorderListener>
        implements BorderListener {
        public void titleChanged(Border border, String previousTitle) {
            for (BorderListener listener : this) {
                listener.titleChanged(border, previousTitle);
            }
        }
    }

    private String title = null;
    private BorderListenerList borderListeners = new BorderListenerList();

    public Border() {
        this(null);
    }

    public Border(Component content) {
        installSkin(Border.class);

        setContent(content);
    }

    @Override
    public void insert(Component component, int index) {
        if (getLength() > 0) {
            throw new IllegalStateException(Border.class.getName()
                + " already has a content component.");
        }

        super.insert(component, index);
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

        if (previousTitle == null ^ title == null) {
            this.title = title;
            borderListeners.titleChanged(this, previousTitle);
        }
    }

    public Component getContent() {
        return (getLength() > 0) ? get(0) : null;
    }

    public void setContent(Component content) {
        Component previousContent = getContent();
        if (previousContent != null) {
            remove(previousContent);
        }

        if (content != null) {
            add(content);
        }
    }

    public ListenerList<BorderListener> getBorderListeners() {
        return borderListeners;
    }
}
