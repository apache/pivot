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

import java.net.URL;

import pivot.collections.ArrayList;
import pivot.collections.ArrayStack;
import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.collections.Sequence;
import pivot.util.ListenerList;
import pivot.wtk.media.Image;

public class Window extends Container {
    /**
     * Class representing the global action map for a window.
     *
     * @author gbrown
     */
    public final class ActionDictionary
        implements Dictionary<Keyboard.KeyStroke, Action> {
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

        public void contentChanged(Window window, Component previousContentComponent) {
            for (WindowListener listener : this) {
                listener.contentChanged(window, previousContentComponent);
            }
        }

        public void activeChanged(Window window) {
            for (WindowListener listener : this) {
                listener.activeChanged(window);
            }
        }

        public void focusHostChanged(Window window) {
            for (WindowListener listener : this) {
                listener.focusHostChanged(window);
            }
        }

        public void maximizedChanged(Window window) {
            for (WindowListener listener : this) {
                listener.maximizedChanged(window);
            }
        }
    }

    /**
     * Window listener list.
     *
     * @author gbrown
     */
    private static class WindowStateListenerList extends ListenerList<WindowStateListener>
        implements WindowStateListener {
        public boolean previewWindowOpen(Window window, Display display) {
            boolean allowed = true;

            for (WindowStateListener listener : this) {
                allowed = listener.previewWindowOpen(window, display);

                if (!allowed) {
                    break;
                }
            }

            return allowed;
        }

        public void windowOpened(Window window) {
            for (WindowStateListener listener : this) {
                listener.windowOpened(window);
            }
        }

        public boolean previewWindowClose(Window window) {
            boolean allowed = true;

            for (WindowStateListener listener : this) {
                allowed = listener.previewWindowClose(window);

                if (!allowed) {
                    break;
                }
            }

            return allowed;
        }

        public void windowClosed(Window window, Display display) {
            for (WindowStateListener listener : this) {
                listener.windowClosed(window, display);
            }
        }
    }

    private Window owner = null;
    private ArrayList<Window> ownedWindows = new ArrayList<Window>();

    private HashMap<Keyboard.KeyStroke, Action> actions = new HashMap<Keyboard.KeyStroke, Action>();
    private ActionDictionary actionDictionary = new ActionDictionary();

    private String title = null;
    private Image icon = null;
    private Component content = null;
    private Component activeDescendant = null;

    private boolean maximized = false;

    private WindowListenerList windowListeners = new WindowListenerList();
    private WindowStateListenerList windowStateListeners = new WindowStateListenerList();
    private static WindowClassListenerList windowClassListeners = new WindowClassListenerList();

    private static Window activeWindow = null;

    public Window() {
        this(null, null);
    }

    public Window(String title) {
        this(title, null);
    }

    public Window(Component content) {
        this(null, content);
    }

    public Window(String title, Component content) {
        setTitle(title);
        setContent(content);
        installSkin(Window.class);
    }

    public Display getDisplay() {
        return (Display)getParent();
    }

    @Override
    protected void setParent(Container parent) {
        if (parent != null
            && (!(parent instanceof Display))) {
            throw new IllegalArgumentException("Window parent must be null or display.");
        }

        if (parent == null
            && isActive()) {
            setActiveWindow(null);
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

        if (!displayable) {
            if (isActive()) {
                setActiveWindow(null);
            }
        }

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
        super.setEnabled(enabled);

        if (!enabled) {
            if (isActive()) {
                setActiveWindow(null);
            }
        }

        // Enable/disable owned windows
        for (Window ownedWindow : ownedWindows) {
            ownedWindow.setEnabled(enabled);
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

    public boolean isOwningAncestorOf(Window window) {
        Window owner = window;

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
     * Opens the window. Opening a window adds it to the display's component
     * sequence. If the window is activatable, it will become the active
     * window.
     *
     * @param display
     * The display on which the window will be opened.
     */
    public void open(Display display) {
        if (isOpen()) {
            throw new IllegalStateException("Window is already open.");
        }

        if (display == null) {
            throw new IllegalArgumentException("display is null.");
        }

        if (windowStateListeners.previewWindowOpen(this, display)) {
            // Add this as child of display
            display.add(this);

            // Show the window
            setDisplayable(true);

            // Notify listeners
            windowStateListeners.windowOpened(this);

            // Move this window to the front (which, unless this window is
            // disabled or incapable of becoming active, will activate the
            // window)
            moveToFront();
        }
    }

    /**
     * Opens the window.
     *
     * @param owner
     * The window's owner.
     */
    public void open(Window owner) {
        if (isOpen()) {
            throw new IllegalStateException("Window is already open.");
        }

        if (owner == null) {
            throw new IllegalArgumentException("owner is null.");
        }

        if (!owner.isOpen()) {
            throw new IllegalStateException("Owner is not open.");
        }

        if (!isAuxilliary()
            && owner.isAuxilliary()) {
            throw new IllegalArgumentException("Primary windows must have a"
                + " primary owner.");
        }

        // Add this to the owner's owned window list
        owner.ownedWindows.add(this);

        // Set the owner
        this.owner = owner;

        // Open the window
        open(owner.getDisplay());

        // Did the state listeners allow us to open?
        if (isOpen()) {
            // Ensure that the window's owner tree is visible
            Window rootOwner = getRootOwner();
            rootOwner.setDisplayable(true);
        } else {
            // Remove this from the owner's owned window list
            owner.ownedWindows.remove(this);

            // Clear the owner
            this.owner = null;
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
     * Closes the window. Closing a window closes all owned windows and
     * removes the window from the display's component sequence. If the window
     * was the active window, the active window will be cleared. If the window
     * was the focus host, the focused component will be cleared.
     */
    public void close() {
        if (!isClosed()
            && windowStateListeners.previewWindowClose(this)) {
            if (isActive()) {
                setActiveWindow(null);
            }

            if (isFocusHost()) {
                setFocusedComponent(null);
            }

            // Close all owned windows (create a copy of the owned window
            // list so owned windows can remove themselves from the list
            // without interrupting the iteration)
            for (Window ownedWindow : new ArrayList<Window>(this.ownedWindows)) {
                ownedWindow.close();
            }

            // Remove this from the owner's owned window list
            if (owner != null) {
                owner.ownedWindows.remove(this);
            }

            // Clear the owner
            owner = null;

            // Hide the window
            setDisplayable(false);

            // Detach from display
            Display display = getDisplay();
            display.remove(this);

            // Notify listeners
            windowStateListeners.windowClosed(this, display);
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

        if (previousTitle == null ^ title == null) {
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

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
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
     * Returns the window's auxilliary state. Auxilliary windows must have an
     * owner, can't become active, and can only own other auxilliary windows.
     *
     * @return
     * <tt>true</tt> if this is an auxilliary window; <tt>false</tt>, otherwise.
     */
    public boolean isAuxilliary() {
        return false;
    }

    /**
     * Returns the window's active state.
     *
     * @return
     * <tt>true</tt> if the window is active; <tt>false</tt>; otherwise.
     */
    public boolean isActive() {
        return activeWindow == this;
    }

    /**
     * Called to notify a window that its active state has changed.
     *
     * @param active
     */
    protected void setActive(boolean active) {
        if (active) {
            // If this window is still an ancestor of the active descendant
            // and the active descendant can be focused, restore focus to it;
            // otherwise, clear the active descendant
            if (activeDescendant != null) {
                if (isAncestor(activeDescendant)
                    && activeDescendant.isEnabled()
                    && activeDescendant.isShowing()) {
                    setFocusedComponent(activeDescendant, true);
                } else {
                    activeDescendant = null;
                }
            }
        } else {
            // Temporarily clear the focus
            setFocusedComponent(null, true);
        }

        windowListeners.activeChanged(this);
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
    public static void setActiveWindow(Window activeWindow) {
        Window previousActiveWindow = Window.activeWindow;

        if (previousActiveWindow != activeWindow) {
            if (activeWindow != null) {
                if (activeWindow.isAuxilliary()) {
                    throw new IllegalArgumentException("activeWindow is auxilliary.");
                }

                if (!activeWindow.isOpen()) {
                    throw new IllegalArgumentException("activeWindow is not open.");
                }

                if (!activeWindow.isEnabled()) {
                    throw new IllegalArgumentException("activeWindow is not enabled.");
                }
            }

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
     * Determines if this window is the focus host.
     *
     * @return
     * <tt>true</tt> if this window is an ancestor of the component that
     * currently has the focus; <tt>false</tt>, otherwise.
     */
    public boolean isFocusHost() {
        Component focusedComponent = getFocusedComponent();
        return (focusedComponent != null
            && focusedComponent.getWindow() == this);
    }

    /**
     * Notifies the window that one of its descendants has gained the focus.
     *
     * @param previousFocusedComponent
     * The component that previously had the focus.
     */
    protected void descendantGainedFocus(Component previousFocusedComponent) {
        // Maintain a reference to the focused component so we can
        // restore it later
        activeDescendant = getFocusedComponent();

        // Notify listeners if this window's focus host state changed
        if (previousFocusedComponent == null
            || previousFocusedComponent.getWindow() != this) {
            windowListeners.focusHostChanged(this);
        }
    }

    /**
     * Notifies the window that one of its descendants has lost the focus.
     *
     * @param descendant
     * The descendant that previously had the focus.
     */
    protected void descendantLostFocus(Component descendant) {
        Component focusedComponent = getFocusedComponent();

        // Notify listeners if this window's focus host state changed
        if (focusedComponent == null
            || focusedComponent.getWindow() != this) {
            windowListeners.focusHostChanged(this);
        }
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
        Container parent = getParent();

        Window window = this;
        ArrayStack<Integer> ownedWindowIndexes = new ArrayStack<Integer>();
        ownedWindowIndexes.push(0);

        while (ownedWindowIndexes.getLength() > 0) {
            // Get the next owned window index for this window
            int j = ownedWindowIndexes.peek();

            if (j == 0) {
                // Move the window within the window stack
                int i = parent.indexOf(window);

                if (i < parent.getLength() - 1) {
                    parent.remove(i, 1);
                    parent.add(window);
                }
            }

            if (j < window.ownedWindows.getLength()) {
                // There is another owned window to traverse; move down
                // the tree
                window = window.ownedWindows.get(j);
                ownedWindowIndexes.poke(j + 1);
                ownedWindowIndexes.push(0);
            } else {
                // Activate the window
                if (window.isEnabled()
                    && !window.isAuxilliary()) {
                    setActiveWindow(window);
                }

                // This was the last owned window for the current window; move
                // up the tree
                ownedWindowIndexes.pop();
                window = window.getOwner();
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
            // Clear the active window
            setActiveWindow(null);
        }

        if (isFocusHost()) {
            // Clear the focus
            setFocusedComponent(null);
        }

        Container parent = getParent();

        // Ensure that the window and all of its owning ancestors are moved
        // to the back
        Window window = this;
        while (window != null) {
            // If this window is not currently bottom-most, move it to the
            // bottom
            int i = parent.indexOf(window);

            if (i > 0) {
                parent.remove(i, 1);
                parent.insert(window, 0);
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
        }
        else {
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
