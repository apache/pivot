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
import java.util.Iterator;

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Vote;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.media.Image;

/**
 * Top-level container representing the entry point into a user interface.
 * Windows are direct descendants of the display.
 */
@DefaultProperty("content")
public class Window extends Container {
    /**
     * Window skin interface.
     */
    public interface Skin extends org.apache.pivot.wtk.Skin {
        public Bounds getClientArea();
    }

    /**
     * Class representing a mapping from keystrokes to actions.
     */
    public static class ActionMapping {
        private Window window = null;

        private Keyboard.KeyStroke keyStroke = null;
        private Action action = null;

        public ActionMapping() {
        }

        public ActionMapping(Keyboard.KeyStroke keyStroke, Action action) {
            setKeyStroke(keyStroke);
            setAction(action);
        }

        public ActionMapping(Keyboard.KeyStroke keyStroke, String actionID) {
            setKeyStroke(keyStroke);
            setAction(actionID);
        }

        public Window getWindow() {
            return window;
        }

        public Keyboard.KeyStroke getKeyStroke() {
            return keyStroke;
        }

        public void setKeyStroke(Keyboard.KeyStroke keyStroke) {
            Keyboard.KeyStroke previousKeyStroke = this.keyStroke;

            if (keyStroke != previousKeyStroke) {
                if (window != null) {
                    if (keyStroke == null) {
                        throw new IllegalStateException();
                    }

                    if (window.actionMap.containsKey(keyStroke)) {
                        throw new IllegalArgumentException("A mapping for " + keyStroke
                            + " already exists.");
                    }

                    if (previousKeyStroke != null) {
                        window.actionMap.remove(previousKeyStroke);
                    }

                    window.actionMap.put(keyStroke, action);

                    window.windowActionMappingListeners.keyStrokeChanged(this, previousKeyStroke);
                }

                this.keyStroke = keyStroke;
            }
        }

        public void setKeyStroke(String keyStroke) {
            if (keyStroke == null) {
                throw new IllegalArgumentException("keyStroke is null.");
            }

            setKeyStroke(Keyboard.KeyStroke.decode(keyStroke));
        }

        public Action getAction() {
            return action;
        }

        public void setAction(Action action) {
            Action previousAction = this.action;

            if (action != previousAction) {
                if (window != null) {
                    if (action == null) {
                        throw new IllegalStateException();
                    }

                    window.actionMap.put(keyStroke, action);

                    window.windowActionMappingListeners.actionChanged(this, previousAction);
                }

                this.action = action;
            }
        }

        public void setAction(String actionID) {
            if (actionID == null) {
                throw new IllegalArgumentException("actionID is null");
            }

            Action actionLocal = Action.getNamedActions().get(actionID);
            if (actionLocal == null) {
                throw new IllegalArgumentException("An action with ID "
                    + actionID + " does not exist.");
            }

            setAction(actionLocal);
        }
    }

    public class ActionMappingSequence implements Sequence<ActionMapping> {
        @Override
        public int add(ActionMapping actionMapping) {
            if (actionMapping.window != null) {
                throw new IllegalArgumentException("Action mapping already has a window.");
            }

            if (actionMapping.keyStroke == null) {
                throw new IllegalArgumentException("Keystroke is undefined.");
            }

            if (actionMapping.action == null) {
                throw new IllegalArgumentException("Action is undefined.");
            }

            if (actionMap.containsKey(actionMapping.keyStroke)) {
                throw new IllegalArgumentException("A mapping for " + actionMapping.keyStroke
                    + " already exists.");
            }

            actionMapping.window = Window.this;

            int index = actionMappings.add(actionMapping);
            actionMap.put(actionMapping.keyStroke, actionMapping.action);

            windowActionMappingListeners.actionMappingAdded(Window.this);

            return index;
        }

        @Override
        public void insert(ActionMapping actionMapping, int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ActionMapping update(int index, ActionMapping actionMapping) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(ActionMapping actionMapping) {
            int index = indexOf(actionMapping);

            if (index >= 0) {
               remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<ActionMapping> remove(int index, int count) {
            Sequence<ActionMapping> removed = actionMappings.remove(index, count);

            for (int i = 0, n = removed.getLength(); i < n; i++) {
                ActionMapping actionMapping = removed.get(i);

                actionMapping.window = null;

                actionMap.remove(actionMapping.keyStroke);
            }

            windowActionMappingListeners.actionMappingsRemoved(Window.this, index, removed);

            return removed;
        }

        @Override
        public ActionMapping get(int index) {
            return actionMappings.get(index);
        }

        @Override
        public int indexOf(ActionMapping actionMapping) {
            return actionMappings.indexOf(actionMapping);
        }

        @Override
        public int getLength() {
            return actionMappings.getLength();
        }
    }

    public class IconImageSequence implements Sequence<Image>, Iterable<Image> {
        @Override
        public int add(Image image) {
            int index = iconImageList.add(image);

            windowListeners.iconAdded(Window.this, image);

            return index;
        }

        @Override
        public void insert(Image image, int index) {
            iconImageList.insert(image, index);

            windowListeners.iconInserted(Window.this, image, index);
        }

        @Override
        public Image update(int index, Image image) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(Image image) {
            int index = indexOf(image);

            if (index >= 0) {
               remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<Image> remove(int index, int count) {
            Sequence<Image> removed = iconImageList.remove(index, count);
            windowListeners.iconsRemoved(Window.this, index, removed);
            return removed;
        }

        @Override
        public Image get(int index) {
            return iconImageList.get(index);
        }

        @Override
        public int indexOf(Image image) {
            return iconImageList.indexOf(image);
        }

        @Override
        public int getLength() {
            return iconImageList.getLength();
        }

        @Override
        public Iterator<Image> iterator() {
            return new ImmutableIterator<Image>(iconImageList.iterator());
        }
    }

    private static class WindowListenerList extends WTKListenerList<WindowListener>
        implements WindowListener {
        @Override
        public void titleChanged(Window window, String previousTitle) {
            for (WindowListener listener : this) {
                listener.titleChanged(window, previousTitle);
            }
        }

        @Override
        public void iconAdded(Window window, Image addedIcon) {
            for (WindowListener listener : this) {
                listener.iconAdded(window, addedIcon);
            }
        }

        @Override
        public void iconInserted(Window window, Image addedIcon, int index) {
            for (WindowListener listener : this) {
                listener.iconInserted(window, addedIcon, index);
            }
        }

        @Override
        public void iconsRemoved(Window window, int index, Sequence<Image> removed) {
            for (WindowListener listener : this) {
                listener.iconsRemoved(window, index, removed);
            }
        }

        @Override
        public void contentChanged(Window window, Component previousContent) {
            for (WindowListener listener : this) {
                listener.contentChanged(window, previousContent);
            }
        }

        @Override
        public void activeChanged(Window window, Window obverseWindow) {
            for (WindowListener listener : this) {
                listener.activeChanged(window, obverseWindow);
            }
        }

        @Override
        public void maximizedChanged(Window window) {
            for (WindowListener listener : this) {
                listener.maximizedChanged(window);
            }
        }
    }

    private static class WindowStateListenerList extends WTKListenerList<WindowStateListener>
        implements WindowStateListener {
        @Override
        public void windowOpened(Window window) {
            for (WindowStateListener listener : this) {
                listener.windowOpened(window);
            }
        }

        @Override
        public Vote previewWindowClose(Window window) {
            Vote vote = Vote.APPROVE;

            for (WindowStateListener listener : this) {
                vote = vote.tally(listener.previewWindowClose(window));
            }

            return vote;
        }

        @Override
        public void windowCloseVetoed(Window window, Vote reason) {
            for (WindowStateListener listener : this) {
                listener.windowCloseVetoed(window, reason);
            }
        }

        @Override
        public Vote previewWindowOpen(Window window) {
            Vote vote = Vote.APPROVE;

            for (WindowStateListener listener : this) {
                vote = vote.tally(listener.previewWindowOpen(window));
            }

            return vote;
        }


        @Override
        public void windowOpenVetoed(Window window, Vote reason) {
            for (WindowStateListener listener : this) {
                listener.windowOpenVetoed(window, reason);
            }
        }

        @Override
        public void windowClosed(Window window, Display display, Window owner) {
            for (WindowStateListener listener : this) {
                listener.windowClosed(window, display, owner);
            }
        }
    }

    private static class WindowActionMappingListenerList extends WTKListenerList<WindowActionMappingListener>
        implements WindowActionMappingListener {
        @Override
        public void actionMappingAdded(Window window) {
            for (WindowActionMappingListener listener : this) {
                listener.actionMappingAdded(window);
            }
        }

        @Override
        public void actionMappingsRemoved(Window window, int index, Sequence<Window.ActionMapping> removed) {
            for (WindowActionMappingListener listener : this) {
                listener.actionMappingsRemoved(window, index, removed);
            }
        }

        @Override
        public void keyStrokeChanged(Window.ActionMapping actionMapping, Keyboard.KeyStroke previousKeyStroke) {
            for (WindowActionMappingListener listener : this) {
                listener.keyStrokeChanged(actionMapping, previousKeyStroke);
            }
        }

        @Override
        public void actionChanged(Window.ActionMapping actionMapping, Action previousAction) {
            for (WindowActionMappingListener listener : this) {
                listener.actionChanged(actionMapping, previousAction);
            }
        }
    }

    private static class WindowClassListenerList
        extends WTKListenerList<WindowClassListener>
        implements WindowClassListener {
        @Override
        public void activeWindowChanged(Window previousActiveWindow) {
            for (WindowClassListener listener : this) {
                listener.activeWindowChanged(previousActiveWindow);
            }
        }
    }

    private Window owner = null;
    private ArrayList<Window> ownedWindows = new ArrayList<Window>();

    private ArrayList<ActionMapping> actionMappings = new ArrayList<ActionMapping>();
    private ActionMappingSequence actionMappingSequence = new ActionMappingSequence();

    private HashMap<Keyboard.KeyStroke, Action> actionMap = new HashMap<Keyboard.KeyStroke, Action>();

    private String title = null;
    private ArrayList<Image> iconImageList = new ArrayList<Image>();
    private IconImageSequence iconImageSequence = new IconImageSequence();
    private Component content = null;
    private Component focusDescendant = null;

    private boolean opening = false;
    private boolean closing = false;

    private Point restoreLocation = null;

    private WindowListenerList windowListeners = new WindowListenerList();
    private WindowStateListenerList windowStateListeners = new WindowStateListenerList();
    private WindowActionMappingListenerList windowActionMappingListeners = new WindowActionMappingListenerList();

    private static WindowClassListenerList windowClassListeners = new WindowClassListenerList();

    private static Window activeWindow = null;

    public Window() {
        this(null);
    }

    public Window(Component content) {
        setContent(content);
        installSkin(Window.class);
    }

    @Override
    protected void setParent(Container parent) {
        if (parent != null
            && (!(parent instanceof Display))) {
            throw new IllegalArgumentException("Window parent must be null or display, cannot be " + parent);
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

    @Override
    public void setVisible(boolean visible) {
        if (visible
            && owner != null
            && !owner.isVisible()) {
            throw new IllegalStateException("Owner is not visible.");
        }

        super.setVisible(visible);

        if (visible
            && isActive()) {
            clearActive();
        }

        for (Window ownedWindow : ownedWindows) {
            ownedWindow.setVisible(visible);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (!enabled
            && isActive()) {
            clearActive();
        }
    }

    public Window getOwner() {
        return owner;
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

        Window ownerLocal = window.getOwner();

        while (ownerLocal != null
            && ownerLocal != this) {
            ownerLocal = ownerLocal.getOwner();
        }

        return (ownerLocal == this);
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
     * <tt>true</tt> if the window is opening; <tt>false</tt>, otherwise.
     */
    public boolean isOpening() {
        return opening;
    }

    /**
     * Opens the window.
     *
     * @param display
     */
    public final void open(Display display) {
        open(display, null);
    }

    /**
     * Opens the window.
     *
     * @param ownerArgument
     * The window's owner. The window is opened on the owner's display.
     */
    public final void open(Window ownerArgument) {
        if (ownerArgument == null) {
            throw new IllegalArgumentException();
        }

        open(ownerArgument.getDisplay(), ownerArgument);
    }

    /**
     * Opens the window.
     * <p>
     * Note that this method is not a synchronous call, it schedules an event to open the window.
     *
     * @param display
     * The display on which the window will be opened.
     *
     * @param ownerArgument
     * The window's owner, or <tt>null<tt> if the window has no owner.
     */
    public void open(Display display, Window ownerArgument) {
        if (display == null) {
            throw new IllegalArgumentException("display is null.");
        }

        if (ownerArgument != null) {
            if (!ownerArgument.isOpen()) {
                throw new IllegalArgumentException("owner is not open.");
            }

            if (isOwner(ownerArgument)) {
                throw new IllegalArgumentException("owner is an owned descendant of this window.");
            }
        }

        if (isOpen()) {
            if (getDisplay() != display) {
                throw new IllegalStateException("Window is already open on a different display.");
            }

            if (this.owner != ownerArgument) {
                throw new IllegalStateException("Window is already open with a different owner.");
            }
        }

        if (!isOpen()) {
            opening = true;
            Vote vote = windowStateListeners.previewWindowOpen(this);

            if (vote == Vote.APPROVE) {
                // Set the owner and add to the owner's owned window list
                this.owner = ownerArgument;

                if (ownerArgument != null) {
                    ownerArgument.ownedWindows.add(this);
                }

                // Add the window to the display
                display.add(this);

                // Notify listeners
                opening = false;
                windowStateListeners.windowOpened(this);

                moveToFront();
            } else {
                if (vote == Vote.DENY) {
                    opening = false;
                }

                windowStateListeners.windowOpenVetoed(this, vote);
            }

        }
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
     * Closes the window and all of its owned windows. If any owned window fails to close,
     * this window will also fail to close.
     */
    public void close() {
        if (!isClosed()) {
            closing = true;

            // Close all owned windows (create a copy of the owned window
            // list so owned windows can remove themselves from the list
            // without interrupting the iteration)
            boolean cancel = false;
            for (Window ownedWindow : new ArrayList<Window>(this.ownedWindows)) {
                ownedWindow.close();
                cancel |= !(ownedWindow.isClosing()
                    || ownedWindow.isClosed());
            }

            // Close this window only if all owned windows are closing or closed
            // (we allow the owner to close even if an owned window is only reports
            // that it is closing, under the assumption that it will ultimately
            // close - not doing so would prevent close transitions from running
            // in parallel, forcing them to run in series)
            if (cancel) {
                closing = false;
            } else {
                Vote vote = windowStateListeners.previewWindowClose(this);

                if (vote == Vote.APPROVE) {
                    // Remove the window from the display
                    Display display = getDisplay();
                    display.remove(this);

                    // Clear the owner and remove from the owner's owned window list
                    Window ownerLocal = this.owner;
                    this.owner = null;

                    if (ownerLocal != null) {
                        ownerLocal.ownedWindows.remove(this);
                    }

                    // Notify listeners
                    closing = false;

                    windowStateListeners.windowClosed(this, display, ownerLocal);
                } else {
                    if (vote == Vote.DENY) {
                        closing = false;
                    }

                    windowStateListeners.windowCloseVetoed(this, vote);
                }
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
     * Returns the icons for this window.
     */
    public IconImageSequence getIcons() {
        return iconImageSequence;
    }

    /**
     * Sets the window's icon by URL.
     * <p>
     * If the icon already exists in the application context resource cache,
     * the cached value will be used. Otherwise, the icon will be loaded
     * synchronously and added to the cache.
     *
     * @param iconURL
     * The location of the icon to set.
     */
    public void setIcon(URL iconURL) {
        if (iconURL == null) {
            throw new IllegalArgumentException("iconURL is null.");
        }

        Image icon = (Image)ApplicationContext.getResourceCache().get(iconURL);

        if (icon == null) {
            try {
                icon = Image.load(iconURL);
            } catch (TaskExecutionException exception) {
                throw new IllegalArgumentException(exception);
            }

            ApplicationContext.getResourceCache().put(iconURL, icon);
        }

        getIcons().remove(0, getIcons().getLength());
        getIcons().add(icon);
    }

    /**
     * Sets the window's icon by {@linkplain ClassLoader#getResource(String)
     * resource name}.
     *
     * @param iconName
     * The resource name of the icon to set.
     *
     * @see #setIcon(URL)
     */
    public void setIcon(String iconName) {
        if (iconName == null) {
            throw new IllegalArgumentException("iconName is null.");
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(iconName.substring(1));
        if (url == null) {
            throw new IllegalArgumentException("cannot find icon resource " + iconName);
        }
        setIcon(url);
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
     * Returns the bounds of the window's client area.
     */
    public Bounds getClientArea() {
        Window.Skin windowSkin = (Window.Skin)getSkin();
        return windowSkin.getClientArea();
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
     * Requests that this window become the active window.
     *
     * @return
     * <tt>true</tt> if the window became active; <tt>false</tt>, otherwise.
     */
    public boolean requestActive() {
        if (isOpen()
            && isVisible()
            && isEnabled()) {
            setActiveWindow(this);
        }

        return isActive();
    }

    /**
     * Called to notify a window that its active state has changed.
     *
     * @param active
     * @param obverseWindow
     */
    protected void setActive(boolean active, Window obverseWindow) {
        windowListeners.activeChanged(this, obverseWindow);
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
                previousActiveWindow.setActive(false, activeWindow);
            }

            // Activate the window
            if (activeWindow != null) {
                activeWindow.setActive(true, previousActiveWindow);
            }

            windowClassListeners.activeWindowChanged(previousActiveWindow);
        }
    }

    /**
     * Clears the active window.
     */
    public static void clearActive() {
        if (activeWindow != null)
            setActiveWindow(activeWindow.owner);
    }

    /**
     * Returns the window descendant to which focus will be restored when this window
     * is moved to the front.
     */
    public Component getFocusDescendant() {
        return focusDescendant;
    }

    /**
     * Clears the window descendant to which focus will be restored when this
     * window is moved to the front, meaning that when this window is moved to
     * front, focus will not be restored to the window.
     */
    public void clearFocusDescendant() {
        focusDescendant = null;
    }

    @Override
    protected void descendantGainedFocus(Component descendant, Component previousFocusedComponent) {
        this.focusDescendant = descendant;

        super.descendantGainedFocus(descendant, previousFocusedComponent);
    }

    @Override
    protected void descendantRemoved(Component descendant) {
        super.descendantRemoved(descendant);

        if (descendant == focusDescendant) {
            focusDescendant = null;
        }
    }

    /**
     * Returns the action mappings for this window.
     */
    public ActionMappingSequence getActionMappings() {
        return actionMappingSequence;
    }

    /**
     * Determines if this is the top-most window.
     */
    public boolean isTopMost() {
        Display display = getDisplay();
        return display.get(display.getLength() - 1) == this;
    }

    /**
     * Determines if this is the bottom-most window.
     */
    public boolean isBottomMost() {
        Display display = getDisplay();
        return display.get(0) == this;
    }

    /**
     * Moves the window to the top of the window stack. All owned windows are
     * subsequently moved to the front, ensuring that this window's owned windows
     * remain on top of it. If the window does not have any owned windows,
     * focus is restored to it.
     */
    public void moveToFront() {
        if (!isOpen()) {
            throw new IllegalStateException("Window is not open.");
        }

        // If this window is not currently top-most, move it to the top
        Display display = getDisplay();
        int top = display.getLength() - 1;

        int i = display.indexOf(this);
        if (i < top) {
            display.move(i, top);
        }

        int ownedWindowCount = ownedWindows.getLength();

        if (ownedWindowCount == 0) {
            // Restore focus
            if (isShowing()
                && isEnabled()
                && focusDescendant != null) {
                focusDescendant.requestFocus();
            }
        } else {
            // Move all open owned windows to the front of this window, preserving the
            // current z-order
            ArrayList<Integer> ownedWindowIndexes = new ArrayList<Integer>(ownedWindowCount);

            for (Window ownedWindow : ownedWindows) {
                if (ownedWindow.isOpen()) {
                    ownedWindowIndexes.add(display.indexOf(ownedWindow));
                }
            }

            ArrayList.sort(ownedWindowIndexes);

            ArrayList<Window> sortedOwnedWindows = new ArrayList<Window>(ownedWindows.getLength());
            for (Integer index : ownedWindowIndexes) {
                sortedOwnedWindows.add((Window)display.get(index));
            }

            for (Window ownedWindow : sortedOwnedWindows) {
                ownedWindow.moveToFront();
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
        }

        if (containsFocus()) {
            clearFocus();
        }

        // Ensure that the window and all of its owning ancestors are moved
        // to the back
        Display display = getDisplay();

        int i = display.indexOf(this);
        if (i > 0) {
            display.move(i, 0);
        }

        if (owner != null) {
            owner.moveToBack();
        }
    }

    public boolean isMaximized() {
        return (restoreLocation != null);
    }

    public void setMaximized(boolean maximized) {
        if (maximized != isMaximized()) {
            if (maximized) {
                restoreLocation = getLocation();
                setLocation(0, 0);
            } else {
                setLocation(restoreLocation.x, restoreLocation.y);
                restoreLocation = null;
            }

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
    public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        /* Use keyPressed rather than keyReleased other this sequence:
         * Press Ctrl, Press C, Release Ctrl, Release C
         * will not trigger the Ctrl-C action.
         */
        boolean consumed = super.keyPressed(keyCode, keyLocation);

        // Perform any action defined for this keystroke
        // in the active window's action dictionary
        Keyboard.KeyStroke keyStroke = new Keyboard.KeyStroke(keyCode,
            Keyboard.getModifiers());

        Action action = actionMap.get(keyStroke);
        if (action != null
            && action.isEnabled()) {
            action.perform(this);
        }

        return consumed;
    }

    public ListenerList<WindowListener> getWindowListeners() {
        return windowListeners;
    }

    public ListenerList<WindowStateListener> getWindowStateListeners() {
        return windowStateListeners;
    }

    public ListenerList<WindowActionMappingListener> getWindowActionMappingListeners() {
        return windowActionMappingListeners;
    }

    public static ListenerList<WindowClassListener> getWindowClassListeners() {
        return windowClassListeners;
    }
}
