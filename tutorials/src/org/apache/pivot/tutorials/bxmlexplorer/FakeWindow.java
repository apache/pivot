package org.apache.pivot.tutorials.bxmlexplorer;

import java.net.URL;

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.WTKListenerList;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.Window.IconImageSequence;
import org.apache.pivot.wtk.WindowListener;
import org.apache.pivot.wtk.media.Image;

/**
 * Because we can't render a real Window object inside our container, create a fake window
 * that looks mostly like a real window.
 */
@DefaultProperty("content")
public class FakeWindow extends Container {

    private static class FakeWindowListenerList extends WTKListenerList<FakeWindowListener>
        implements FakeWindowListener {
        @Override
        public void titleChanged(FakeWindow window, String previousTitle) {
            for (FakeWindowListener listener : this) {
                listener.titleChanged(window, previousTitle);
            }
        }

        @Override
        public void iconAdded(FakeWindow window, Image addedIcon) {
            for (FakeWindowListener listener : this) {
                listener.iconAdded(window, addedIcon);
            }
        }

        @Override
        public void iconInserted(FakeWindow window, Image addedIcon, int index) {
            for (FakeWindowListener listener : this) {
                listener.iconInserted(window, addedIcon, index);
            }
        }

        @Override
        public void iconsRemoved(FakeWindow window, int index, Sequence<Image> removed) {
            for (FakeWindowListener listener : this) {
                listener.iconsRemoved(window, index, removed);
            }
        }

        @Override
        public void contentChanged(FakeWindow window, Component previousContent) {
            for (FakeWindowListener listener : this) {
                listener.contentChanged(window, previousContent);
            }
        }

    }

    private FakeWindowListenerList windowListeners = new FakeWindowListenerList();

    private Component content = null;

    public final Window window;

    public FakeWindow(Window _window) {
        Component content = _window.getContent();
        _window.setContent(null);
        this.window = _window;
        window.getWindowListeners().add(new WindowListener() {

            @Override
            public void titleChanged(Window window, String previousTitle) {
                windowListeners.titleChanged(FakeWindow.this, previousTitle);
            }

            @Override
            public void iconAdded(Window window, Image addedIcon) {
                windowListeners.iconAdded(FakeWindow.this, addedIcon);
            }

            @Override
            public void iconInserted(Window window, Image addedIcon, int index) {
                windowListeners.iconInserted(FakeWindow.this, addedIcon, index);
            }

            @Override
            public void iconsRemoved(Window window, int index, Sequence<Image> removed) {
                windowListeners.iconsRemoved(FakeWindow.this, index, removed);
            }

            @Override
            public void contentChanged(Window window, Component previousContent) {
                windowListeners.contentChanged(FakeWindow.this, previousContent);
            }

            @Override
            public void activeChanged(Window window, Window obverseWindow) {
            }

            @Override
            public void maximizedChanged(Window window) {
            }
        });
        setContent(content);
        setSkin(new FakeWindowSkin());
    }

    public IconImageSequence getIcons() {
        return window.getIcons();
    }

    public void setIcon(URL iconURL) {
        window.setIcon(iconURL);
    }

    public void setIcon(String iconName) {
        window.setIcon(iconName);
    }

    public String getTitle() {
        return window.getTitle();
    }

    public void setTitle(String title) {
        window.setTitle(title);
    }

    public ListenerList<FakeWindowListener> getWindowListeners() {
        return windowListeners;
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
                insert(content, 0);
            }

            this.content = content;

            windowListeners.contentChanged(this, previousContent);
        }
    }
}
