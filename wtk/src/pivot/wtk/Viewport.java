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

public abstract class Viewport extends Container {
    public interface Skin {
        public Bounds getViewportBounds();
    }

    private static class ViewportListenerList extends ListenerList<ViewportListener>
        implements ViewportListener {

        public void scrollTopChanged(Viewport viewport, int previousScrollTop) {
            for (ViewportListener listener : this) {
                listener.scrollTopChanged(viewport, previousScrollTop);
            }
        }

        public void scrollLeftChanged(Viewport viewport, int previousScrollLeft) {
            for (ViewportListener listener : this) {
                listener.scrollLeftChanged(viewport, previousScrollLeft);
            }
        }

        public void viewChanged(Viewport viewport, Component previousView) {
            for (ViewportListener listener : this) {
                listener.viewChanged(viewport, previousView);
            }
        }
    }

    private int scrollTop = 0;
    private int scrollLeft = 0;
    private Component view;
    private ViewportListenerList viewportListeners = new ViewportListenerList();

    @Override
    protected void setSkin(pivot.wtk.Skin skin) {
        if (!(skin instanceof Viewport.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + Viewport.Skin.class.getName());
        }

        super.setSkin(skin);
    }

    public int getScrollTop() {
        return scrollTop;
    }

    public void setScrollTop(int scrollTop) {
        if (scrollTop < 0) {
            throw new IllegalArgumentException("Scroll top must be positive");
        }

        int previousScrollTop = this.scrollTop;

        if (scrollTop != previousScrollTop) {
            this.scrollTop = scrollTop;
            viewportListeners.scrollTopChanged(this, previousScrollTop);
        }
    }

    public int getScrollLeft() {
        return scrollLeft;
    }

    public void setScrollLeft(int scrollLeft) {
        if (scrollLeft < 0) {
            throw new IllegalArgumentException("Scroll left must be positive");
        }

        int previousScrollLeft = this.scrollLeft;

        if (scrollLeft != previousScrollLeft) {
            this.scrollLeft = scrollLeft;
            viewportListeners.scrollLeftChanged(this, previousScrollLeft);
        }
    }

    public Component getView() {
        return view;
    }

    public void setView(Component view) {
       Component previousView = this.view;

        if (view != previousView) {
            // Remove any previous view component
            this.view = null;

            if (previousView != null) {
                remove(previousView);
            }

            // Set the new view component
            if (view != null) {
                insert(view, 0);
            }

            this.view = view;

            viewportListeners.viewChanged(this, previousView);
        }
    }

    public Bounds getViewportBounds() {
        Viewport.Skin viewportSkin = (Viewport.Skin)getSkin();
        return viewportSkin.getViewportBounds();
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);
            if (component == view) {
                throw new UnsupportedOperationException();
            }
        }

        // Call the base method to remove the components
        return super.remove(index, count);
    }

    public ListenerList<ViewportListener> getViewportListeners() {
        return viewportListeners;
    }
}
