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

import java.net.URL;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.ArrayStack;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.ThreadUtilities;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.media.Image;


/**
 * Top-level container representing the entry point into a user interface.
 * Windows are direct descendants of the display.
 *
 * @author gbrown
 */
public class Window extends Container {
    /**
     * Action dictionary implementation.
     *
     * @author gbrown
     */
    public final class ActionDictionary
        implements Dictionary<Keyboard.KeyStroke, Action> {
        private ActionDictionary() {
        }

        public Action get(Keyboard.KeyStroke key) {
            return actions.get(key);
        }

        public Action put(Keyboard.KeyStroke key, Action value) {
            return actions.put(key, value);

            // TODO Fire WindowActionListener#actionAdded()/actionUpdated()
        }

        public Action remove(Keyboard.KeyStroke key) {
            return actions.remove(key);

            // TODO Fire WindowActionListener#actionRemoved()
        }

        public boolean containsKey(Keyboard.KeyStroke key) {
            return actions.containsKey(key);
        }

        public boolean isEmpty() {
            return actions.isEmpty();
        }
    }

    /**
     * Window listener list.
     *
     * @author gbrown
     */
    private static class WindowListenerList extends ListenerList<WindowListener>
        implements WindowListener {
        public void titleChanged(Window window, String previousTitle) {
            for (WindowListener listener : this) {
                listener.titleChanged(window, previousTitle);
            }
        }

        public void iconChanged(Window window, Image previousIcon) {
            for (WindowListener listener : this) {
                listener.iconChanged(window, previousIcon);
            }
        }

        public void contentChanged(Window window, Component previousContent) {
            for (WindowListener listener : this) {
                listener.contentChanged(window, previousContent);
            }
        }

        public void windowMoved(Window window, int from, int to) {
            for (WindowListener listener : this) {
                listener.windowMoved(window, from, to);
            }
        }

        public void ownerChanged(Window window, Window previousOwner) {
            for (WindowListener listener : this) {
                listener.ownerChanged(window, previousOwner);
            }
        }

        public void activeChanged(Window window) {
            for (WindowListener listener : this) {
                listener.activeChanged(window);
            }
        }

        public void maximizedChanged(Window window) {
            for (WindowListener listener : this) {
                listener.maximizedChanged(window);
            }
        }
    }

    /**
     * Window state listener list.
     *
     * @author gbrown
     */
    private static class WindowStateListenerList extends ListenerList<WindowStateListener>
        implements WindowStateListener {
        public Vote previewWindowOpen(Window window, Display display) {
            Vote vote = Vote.APPROVE;

            for (WindowStateListener listener : this) {
                vote = vote.tally(listener.previewWindowOpen(window, display));
            }

            return vote;
        }

        public void windowOpenVetoed(Window window, Vote reason) {
            for (WindowStateListener listener : this) {
                listener.windowOpenVetoed(window, reason);
            }
        }

        public void windowOpened(Window window) {
            for (WindowStateListener listener : this) {
                listener.windowOpened(window);
            }
        }

        public Vote previewWindowClose(Window window) {
            Vote vote = Vote.APPROVE;

            for (WindowStateListener listener : this) {
                vote = vote.tally(listener.previewWindowClose(window));
            }

            return vote;
        }

        public void windowCloseVetoed(Window window, Vote reason) {
            for (WindowStateListener listener : this) {
                listener.windowCloseVetoed(window, reason);
            }
        }

        public void windowClosed(Window window, Display display) {
            for (WindowStateListener listener : this) {
                listener.windowClosed(window, display);
            }
        }
    }

    /**
     * Window class listener list.
     *
     * @author tvolkert
     */
    private static class WindowClassListenerList
        extends ListenerList<WindowClassListener>
        implements WindowClassListener {
        public void activeWindowChanged(Window previousActiveWindow) {
            for (WindowClassListener listener : this) {
                listener.activeWindowChanged(previousActiveWindow);
            }
        }
    }

    private boolean auxilliary;

    private Window owner = null;
    private ArrayList<Window> ownedWindows = new ArrayList<Window>();

    private HashMap<Keyboard.KeyStroke, Action> actions = new HashMap<Keyboard.KeyStroke, Action>();
    private ActionDictionary actionDictionary = new ActionDictionary();

    private String title = null;
    private Image icon = null;
    private Component content = null;
    private Component focusDescendant = null;

    private boolean opening = false;
    private boolean closing = false;

    private boolean maximized = false;

    private WindowListenerList windowListeners = new WindowListenerList();
    private WindowStateListenerList windowStateListeners = new WindowStateListenerList();
    private static WindowClassListenerList windowClassListeners = new WindowClassListenerList();

    private static Window activeWindow = null;

    public Window() {
        this(null, false);
    }

    public Window(boolean auxilliary) {
        this(null, auxilliary);
    }

    public Window(Component content) {
        this(content, false);
    }

    public Window(Component content, boolean auxilliary) {
        this.auxilliary = auxilliary;

        setContent(content);
        installSkin(Window.class);
    }

    @Override
    protected void setParent(Container parent) {
        if (parent != null
            && (!(parent instanceof Display))) {
            throw new IllegalArgumentException("Window parent must be null or display.");
        }

        if (parent == null
            && isActive()) {
            clearActive();
        }

        super.setParent(parent);
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

    /**
     * Sets the displayable state of this window and all of its owned
     * descendant windows.
     *
     * @param displayable
     * If <tt>true</tt>, the window and its owned descendants are displayable;
     * otherwise, they are not displayable.
     */
    @Override
    public void setDisplayable(boolean displayable) {
        super.setDisplayable(displayable);

        // Show/hide owned windows
        for (Window ownedWindow : ownedWindows) {
            ownedWindow.setDisplayable(displayable);
        }
    }

    /**
     * Sets the enabled state of this window and all of its owned
     * descendant windows.
     *
     * @param enabled
     * If <tt>true</tt>, the window and its owned descendants are enabled;
     * otherwise, they are not enabled.
     */
    @Override
    public void setEnabled(boolean enabled) {
        if (isEnabled() != enabled) {
            super.setEnabled(enabled);

            if (isEnabled() == enabled) {
                if (!enabled
                    && isActive()) {
                    clearActive();
                }

                // Enable/disable owned windows
                for (Window ownedWindow : ownedWindows) {
                    ownedWindow.setEnabled(enabled);
                }
            }
        }
    }

    public Window getOwner() {
        return owner;
    }

    public void setOwner(Window owner) {
        if (owner != null
            && owner.isAuxilliary()
            && !isAuxilliary()) {
            throw new IllegalArgumentException("Primary windows must have a"
                + " primary owner.");
        }

        Window previousOwner = this.owner;

        if (previousOwner != owner) {
            if (previousOwner != null) {
                previousOwner.ownedWindows.remove(this);
            }

            if (owner != null) {
                owner.ownedWindows.add(this);
                setDisplayable(owner.isDisplayable());
                setEnabled(owner.isEnabled());
            }

            this.owner = owner;

            windowListeners.ownerChanged(this, previousOwner);
        }
    }

    public Window getRootOwner() {
        return (owner == null) ? this : owner.getRootOwner();
    }

    public Window getOwnedWindow(int index) {
        return ownedWindows.get(index);
    }

    public int getOwnedWindowCount() {
        return ownedWindows.getLength();
    }

    /**
     * Tests whether this window is an owning ancestor of a given window. A
     * window is not considered an owner of itself.
     *
     * @param window
     *
     * @return
     * <tt>true</tt> if this window is an owning ancestor of the given window;
     * <tt>false</tt>, otherwise.
     */
    public boolean isOwner(Window window) {
        if (window == null) {
            throw new IllegalArgumentException("window is null.");
        }

        Window owner = window.getOwner();

        while (owner != null
            && owner != this) {
            owner = owner.getOwner();
        }

        return (owner == this);
    }

    /**
     * Returns this window's open state.
     *
     * @return
     * <tt>true</tt> if the window is open; <tt>false</tt>, otherwise.
     */
    public boolean isOpen() {
        return (getParent() != null);
    }

    /**
     * Returns this window's opening state.
     *
     * @return
     * <tt>true</tt> if the window is open; <tt>false</tt>, otherwise.
     */
    public boolean isOpening() {
        return opening;
    }

    /**
     * Opens the window. Opening a window adds it to the display's component
     * sequence. If the window is activatable, it will become the active
     * window.
     *
     * @param display
     * The display on which the window will be opened.
     */
    public void open(Display display) {
        if (display == null) {
            throw new IllegalArgumentException("display is null.");
        }

        if (isOpen()
            && getDisplay() != display) {
            throw new IllegalStateException("Window is already open on a different display.");
        }

        if (owner != null
            && !owner.isOpen()) {
            throw new IllegalArgumentException("Owner is not open.");
        }

        if (owner != null
            && owner.getDisplay() != display) {
            throw new IllegalArgumentException("Owner is opened on a different display.");
        }

        if (!isOpen()
            && !opening) {
            Vote vote = windowStateListeners.previewWindowOpen(this, display);

            if (vote == Vote.APPROVE) {
                opening = true;

                // Add this as child of display
                display.add(this);

                // Notify listeners
                windowStateListeners.windowOpened(this);

                // Move this window to the front (which, unless this window is
                // disabled or incapable of becoming active, will activate the
                // window)
                moveToFront();

                opening = false;
            } else {
                windowStateListeners.windowOpenVetoed(this, vote);
            }
        }
    }

    /**
     * Opens the window.
     *
     * @param owner
     * The window's owner.
     */
    public void open(Window owner) {
        if (owner == null) {
            throw new IllegalArgumentException("owner is null.");
        }

        if (isOpen()
            && getOwner() != owner) {
            throw new IllegalStateException("Window is already open with a different owner.");
        }

        setOwner(owner);
        open(owner.getDisplay());
    }

    /**
     * Returns this window's closed state.
     *
     * @return
     * <tt>true</tt> if the window is closed; <tt>false</tt>, otherwise.
     */
    public boolean isClosed() {
        return !isOpen();
    }

    /**
     * Returns this window's closing state.
     *
     * @return
     * <tt>true</tt> if the window is closing; <tt>false</tt>, otherwise.
     */
    public boolean isClosing() {
        return closing;
    }

    /**
     * Closes the window. Closing a window closes all owned windows and
     * removes the window from the display's component sequence. If the window
     * was the active window, the active window will be cleared. If the window
     * was the focus host, the focused component will be cleared.
     */
    public void close() {
        if (!isClosed()
            && !closing) {
            Vote vote = windowStateListeners.previewWindowClose(this);

            if (vote.isApproved()) {
                closing = true;

                // Close all owned windows (create a copy of the owned window
                // list so owned windows can remove themselves from the list
                // without interrupting the iteration)
                for (Window ownedWindow : new ArrayList<Window>(this.ownedWindows)) {
                    ownedWindow.close();
                }

                // Detach from display
                Display display = getDisplay();
                display.remove(this);

                // Notify listeners
                windowStateListeners.windowClosed(this, display);

                closing = false;
            } else {
                windowStateListeners.windowCloseVetoed(this, vote);
            }
        }
    }

    /**
     * Returns the window's title.
     *
     * @return
     * The pane's title, or <tt>null</tt> if no title is set.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the window's title.
     *
     * @param title
     * The new title, or <tt>null</tt> for no title.
     */
    public void setTitle(String title) {
        String previousTitle = this.title;

        if (previousTitle != title) {
            this.title = title;
            windowListeners.titleChanged(this, previousTitle);
        }
    }

    /**
     * Returns the window's icon.
     *
     * @return
     * The window's icon, or <tt>null</tt> if the window has no icon.
     */
    public Image getIcon() {
        return icon;
    }

    /**
     * Sets the window's icon.
     *
     * @param icon
     * The window's icon, or <tt>null</tt> for no icon.
     */
    public void setIcon(Image icon) {
        Image previousIcon = this.icon;

        if (previousIcon != icon) {
            this.icon = icon;
            windowListeners.iconChanged(this, previousIcon);
        }
    }

    /**
     * Sets the window's icon by URL.
     *
     * @param icon
     * The location of the icon to set.
     */
    public void setIcon(URL icon) {
        if (icon == null) {
            throw new IllegalArgumentException("icon is null.");
        }

        setIcon(Image.load(icon));
    }

    /**
     * Sets the window's icon by resource name.
     *
     * @param icon
     * The resource name of the icon to set.
     */
    public void setIcon(String icon) {
        if (icon == null) {
            throw new IllegalArgumentException("icon is null.");
        }

        ClassLoader classLoader = ThreadUtilities.getClassLoader();
        setIcon(classLoader.getResource(icon));
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

    /**
     * Returns the window's auxilliary flag. Auxilliary windows must have an
     * owner, can't become active, and can only own other auxilliary windows.
     *
     * @return
     * <tt>true</tt> if this is an auxilliary window; <tt>false</tt>, otherwise.
     */
    public boolean isAuxilliary() {
        return auxilliary;
    }

    /**
     * Returns the window's active state.
     *
     * @return
     * <tt>true</tt> if the window is active; <tt>false</tt>; otherwise.
     */
    public boolean isActive() {
        return (activeWindow == this);
    }

    /**
     * Called to notify a window that its active state has changed.
     *
     * @param active
     */
    protected void setActive(boolean active) {
        windowListeners.activeChanged(this);
    }

    /**
     * Requests that this window become active.
     */
    public void requestActive() {
        if (isAuxilliary()) {
            throw new IllegalArgumentException("Window is auxilliary.");
        }

        if (!isOpen()) {
            throw new IllegalArgumentException("Window is not open.");
        }

        if (!isEnabled()) {
            throw new IllegalArgumentException("Window is not enabled.");
        }

        setActiveWindow(this);
    }

    /**
     * Returns the currently active window.
     *
     * @return
     * The window that is currently active, or <tt>null</tt> if no window
     * is active.
     */
    public static Window getActiveWindow() {
        return activeWindow;
    }

    /**
     * Sets the active window. The window must be activatable, open, and
     * enabled. If the window is not currently visible, it will be made
     * visible.
     *
     * @param activeWindow
     * The window to activate, or <tt>null</tt> to clear the active window.
     */
    private static void setActiveWindow(Window activeWindow) {
        Window previousActiveWindow = Window.activeWindow;

        if (previousActiveWindow != activeWindow) {
            // Set the active window
            Window.activeWindow = activeWindow;

            // Notify the windows of the state change
            if (previousActiveWindow != null) {
                previousActiveWindow.setActive(false);
            }

            // Activate the window
            if (activeWindow != null) {
                activeWindow.setActive(true);
            }

            windowClassListeners.activeWindowChanged(previousActiveWindow);
        }
    }

    /**
     * Clears the active window.
     */
    public static void clearActive() {
        setActiveWindow(null);
    }

    /**
     * Returns the window descendant to which focus will be restored by a call
     * to {@link #requestFocus()}.
     */
    public Component getFocusDescendant() {
        return focusDescendant;
    }

    /**
     * Sets the window descendant to which focus will be restored by a call to
     * {@link #requestFocus()}.
     *
     * @param focusDescendant
     */
    protected void setFocusDescendant(Component focusDescendant) {
        assert(focusDescendant == null
            || focusDescendant.getWindow() == this);

        this.focusDescendant = focusDescendant;
    }

    @Override
    protected boolean requestFocus(boolean temporary) {
        // If this window is still an ancestor of the focus descendant
        // and the focus descendant can be focused, restore focus to it;
        // otherwise, clear the focus descendant
        if (focusDescendant != null
            && isAncestor(focusDescendant)
            && !focusDescendant.isBlocked()
            && focusDescendant.isShowing()) {
            focusDescendant.requestFocus(temporary);
        } else {
            focusDescendant = null;
            Component.clearFocus(true);
        }

        return containsFocus();
    }

    /**
     * Returns the global action map for this window.
     */
    public ActionDictionary getActions() {
        return actionDictionary;
    }

    /**
     * Moves the window to the top of the window stack. The window is removed
     * from its current position in the display's component sequence and
     * appended to the end. It is also moved to the top of its owner's owned
     * window list so it becomes top-most of all windows owned by its owner.
     * <p>
     * All windows owned by this window are subsequently moved to the front,
     * ensuring that this window's owned windows remain on top of it.
     * <p>
     * Finally, the window is made active and focus is restored to the most
     * recently focused decendant component.
     */
    public void moveToFront() {
        if (!isOpen()) {
            throw new IllegalStateException("Window is not open.");
        }

        // If this window is not currently top-most, move it to the top
        Display display = getDisplay();

        Window window = this;
        ArrayStack<Integer> ownedWindowIndexes = new ArrayStack<Integer>();
        ownedWindowIndexes.push(0);

        while (ownedWindowIndexes.getLength() > 0) {
            // Get the next owned window index for this window
            int j = ownedWindowIndexes.peek();

            if (j == 0) {
                // Move the window within the window stack
                int i = display.indexOf(window);

                if (i < display.getLength() - 1) {
                    int k = display.getLength() - 1;
                    display.move(i, k);
                    window.windowListeners.windowMoved(window, i, k);
                }
            }

            if (j < window.ownedWindows.getLength()) {
                // There is another owned window to traverse; move down
                // the tree
                ownedWindowIndexes.update(ownedWindowIndexes.getLength() - 1, j + 1);
                window = window.ownedWindows.get(j);

                // If the window is not open, ignore it
                if (window.isOpen()) {
                    ownedWindowIndexes.push(0);
                } else {
                    window = window.owner;
                }
            } else {
                // Activate the window
                if (window.isShowing()
                    && !window.isBlocked()) {
                    if (!window.isAuxilliary()) {
                        window.requestActive();
                        window.requestFocus(true);
                    }
                }

                // This was the last owned window for the current window; move
                // up the tree
                ownedWindowIndexes.pop();
                window = window.owner;
            }
        }

        // Move this window to the top of its owner's owned window list,
        // so it becomes top-most of all windows owned by this window's
        // owner
        if (owner != null) {
            int j = owner.ownedWindows.indexOf(this);

            if (j < owner.ownedWindows.getLength() - 1) {
                owner.ownedWindows.remove(j, 1);
                owner.ownedWindows.add(this);
            }
        }
    }

    /**
     * Moves the window to the bottom of the window stack. If the window is
     * active, the active window will be cleared. If the window is the focus
     * host, the focus will be cleared.
     */
    public void moveToBack() {
        if (!isOpen()) {
            throw new IllegalStateException("Window is not open.");
        }

        if (isActive()) {
            clearActive();
            clearFocus(true);
        }

        Display display = getDisplay();

        // Ensure that the window and all of its owning ancestors are moved
        // to the back
        Window window = this;
        while (window != null) {
            // If this window is not currently bottom-most, move it to the
            // bottom
            int i = display.indexOf(window);

            if (i > 0) {
                display.move(i, 0);
                window.windowListeners.windowMoved(window, i, 0);
            }

            window = window.getOwner();
        }

        // Move this window to the bottom of its owner's owned window list
        if (owner != null) {
            int j = owner.ownedWindows.indexOf(this);

            if (j > 0) {
                owner.ownedWindows.remove(j, 1);
                owner.ownedWindows.insert(this, 0);
            }
        }
    }

    public boolean isMaximized() {
        return maximized;
    }

    public void setMaximized(boolean maximized) {
        if (maximized != this.maximized) {
            this.maximized = maximized;

            invalidate();

            windowListeners.maximizedChanged(this);
        }
    }

    public void align(Bounds bounds,
        HorizontalAlignment horizontalAlignment,
        VerticalAlignment verticalAlignment) {
        align(bounds, horizontalAlignment, 0, verticalAlignment, 0);
    }

    public void align(Bounds bounds,
        HorizontalAlignment horizontalAlignment, int horizontalOffset,
        VerticalAlignment verticalAlignment, int verticalOffset) {

        int x = 0;
        int y = 0;

        Dimensions size = getSize();

        if (horizontalAlignment == HorizontalAlignment.LEFT) {
            x = bounds.x - size.width;
        }
        else if (horizontalAlignment == HorizontalAlignment.RIGHT) {
            x = bounds.x + bounds.width - size.width;
        }
        else if (horizontalAlignment == HorizontalAlignment.CENTER) {
            x = bounds.x + (int)Math.round((double)(bounds.width - size.width) / 2);
        }
        else {
            throw new IllegalArgumentException("Unsupported horizontal alignment.");
        }

        x += horizontalOffset;

        if (verticalAlignment == VerticalAlignment.TOP) {
            y = bounds.y - size.height;
        }
        else if (verticalAlignment == VerticalAlignment.BOTTOM) {
            y = bounds.y + bounds.height;
        }
        else if (verticalAlignment == VerticalAlignment.CENTER) {
            y = bounds.y + (int)Math.round((double)(bounds.height - size.height) / 2);
        }
        else {
            throw new IllegalArgumentException("Unsupported vertical alignment.");
        }

        y += verticalOffset;

        setLocation(x, y);
    }

    @Override
    protected boolean mouseDown(Mouse.Button button, int x, int y) {
        // NOTE This is done here rather than in WindowSkin because the
        // user input methods are not called on the skin when the component
        // is disabled

        if (isEnabled()) {
            // Bring this window to the front
            moveToFront();
        } else {
            ApplicationContext.beep();

            // Bring the window's owner tree to the front
            Window rootOwner = getRootOwner();
            rootOwner.moveToFront();
        }

        return super.mouseDown(button, x, y);
    }

    public ListenerList<WindowListener> getWindowListeners() {
        return windowListeners;
    }

    public ListenerList<WindowStateListener> getWindowStateListeners() {
        return windowStateListeners;
    }

    public static ListenerList<WindowClassListener> getWindowClassListeners() {
        return windowClassListeners;
    }
}
