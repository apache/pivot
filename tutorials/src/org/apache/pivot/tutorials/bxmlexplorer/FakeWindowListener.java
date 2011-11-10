package org.apache.pivot.tutorials.bxmlexplorer;

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.media.Image;

public interface FakeWindowListener {
    /**
     * Window listener adapter.
     */
    public static class Adapter implements FakeWindowListener {
        @Override
        public void titleChanged(FakeWindow window, String previousTitle) {
        }

        @Override
        public void iconAdded(FakeWindow window, Image addedIcon) {
        }

        @Override
        public void iconInserted(FakeWindow window, Image addedIcon, int index) {
        }

        @Override
        public void iconsRemoved(FakeWindow window, int index, Sequence<Image> removed) {
        }

        @Override
        public void contentChanged(FakeWindow window, Component previousContent) {
        }

    }

    /**
     * Called when a window's title has changed.
     *
     * @param window
     * @param previousTitle
     */
    public void titleChanged(FakeWindow window, String previousTitle);

    /**
     * Called when a window's icon has changed.
     *
     * @param window
     * @param addedIcon
     */
    public void iconAdded(FakeWindow window, Image addedIcon);

    /**
     * Called when a window's icon has changed.
     *
     * @param window
     * @param addedIcon
     */
    public void iconInserted(FakeWindow window, Image addedIcon, int index);

    /**
     * Called when a window's icon has changed.
     *
     * @param window
     * @param index
     * @param removed
     */
    public void iconsRemoved(FakeWindow window, int index, Sequence<Image> removed);

    /**
     * Called when a window's content component has changed.
     *
     * @param window
     * @param previousContent
     */
    public void contentChanged(FakeWindow window, Component previousContent);

}
