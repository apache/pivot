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

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.effects.Decorator;

/**
 * Abstract base class for containers.
 */
public abstract class Container extends Component
    implements Sequence<Component>, Iterable<Component> {
    private static class ContainerListenerList extends WTKListenerList<ContainerListener>
        implements ContainerListener {
        @Override
        public void componentInserted(Container container, int index) {
            for (ContainerListener listener : this) {
                listener.componentInserted(container, index);
            }
        }

        @Override
        public void componentsRemoved(Container container, int index, Sequence<Component> components) {
            for (ContainerListener listener : this) {
                listener.componentsRemoved(container, index, components);
            }
        }

        @Override
        public void componentMoved(Container container, int from, int to) {
            for (ContainerListener listener : this) {
                listener.componentMoved(container, from, to);
            }
        }

        @Override
        public void focusTraversalPolicyChanged(Container container,
            FocusTraversalPolicy previousFocusTraversalPolicy) {
            for (ContainerListener listener : this) {
                listener.focusTraversalPolicyChanged(container, previousFocusTraversalPolicy);
            }
        }
    }

    private static class ContainerMouseListenerList extends WTKListenerList<ContainerMouseListener>
        implements ContainerMouseListener {
        @Override
        public boolean mouseMove(Container container, int x, int y) {
            boolean consumed = false;

            for (ContainerMouseListener listener : this) {
                consumed |= listener.mouseMove(container, x, y);
            }

            return consumed;
        }

        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            boolean consumed = false;

            for (ContainerMouseListener listener : this) {
                consumed |= listener.mouseDown(container, button, x, y);
            }

            return consumed;
        }

        @Override
        public boolean mouseUp(Container container, Mouse.Button button, int x, int y) {
            boolean consumed = false;

            for (ContainerMouseListener listener : this) {
                consumed |= listener.mouseUp(container, button, x, y);
            }

            return consumed;
        }

        @Override
        public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            boolean consumed = false;

            for (ContainerMouseListener listener : this) {
                consumed |= listener.mouseWheel(container, scrollType, scrollAmount, wheelRotation, x, y);
            }

            return consumed;
        }
    }

    private ArrayList<Component> components = new ArrayList<Component>();

    private FocusTraversalPolicy focusTraversalPolicy = null;

    private Component mouseOverComponent = null;
    private boolean mouseDown = false;
    private Component mouseDownComponent = null;
    private long mouseDownTime = 0;
    private int mouseClickCount = 0;
    private boolean mouseClickConsumed = false;

    // The component's double-buffering buffer and flags
    private boolean doubleBuffering = false;
    private java.awt.image.BufferedImage doubleBufferImage = null;
    private boolean doubleBufferedRepaintRequired = false;

    private ContainerListenerList containerListeners = new ContainerListenerList();
    private ContainerMouseListenerList containerMouseListeners = new ContainerMouseListenerList();

    @Override
    public final int add(Component component) {
        int index = getLength();
        insert(component, index);

        return index;
    }

    @Override
    public void insert(Component component, int index) {
        assertEventDispatchThread();
        if (component == null) {
            throw new IllegalArgumentException("component is null.");
        }

        if (component instanceof Container
            && ((Container)component).isAncestor(this)) {
            throw new IllegalArgumentException("Component already exists in ancestry.");
        }

        if (component.getParent() != null) {
            throw new IllegalArgumentException("Component already has a parent.");
        }

        component.setParent(Container.this);
        components.insert(component, index);

        // Repaint the area occupied by the new component
        repaint(component.getDecoratedBounds());

        invalidate();

        containerListeners.componentInserted(Container.this, index);
    }

    @Override
    public Component update(int index, Component component) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final int remove(Component component) {
        int index = indexOf(component);
        if (index != -1) {
            remove(index, 1);
        }

        return index;
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        assertEventDispatchThread();
        Sequence<Component> removed = components.remove(index, count);

        // Set the removed components' parent to null and repaint the area
        // formerly occupied by the components
        for (int i = 0, n = removed.getLength(); i < n; i++) {
            Component component = removed.get(i);
            if (component == mouseOverComponent) {
                if (mouseOverComponent.isMouseOver()) {
                    mouseOverComponent.mouseOut();
                }

                mouseOverComponent = null;
                Mouse.setCursor(this);
            }

            repaint(component.getDecoratedBounds());

            component.setParent(null);
        }

        if (removed.getLength() > 0) {
            invalidate();
            containerListeners.componentsRemoved(Container.this, index, removed);
        }

        return removed;
    }

    public final Sequence<Component> removeAll() {
        return remove(0, getLength());
    }

    /**
     * Moves a component within the component sequence.
     *
     * @param from
     * @param to
     */
    public void move(int from, int to) {
        assertEventDispatchThread();
        if (from != to) {
            int n = components.getLength();

            indexBoundsCheck("from", from, 0, n - 1);
            indexBoundsCheck("to", to, 0, n - 1);

            Sequence<Component> removed = components.remove(from, 1);
            Component component = removed.get(0);
            components.insert(component, to);

            // Repaint the area occupied by the component
            repaint(component.getDecoratedBounds());

            // Notify listeners
            containerListeners.componentMoved(this, from, to);
        }
    }

    @Override
    public Component get(int index) {
        assertEventDispatchThread();
        return components.get(index);
    }

    @Override
    public int indexOf(Component component) {
        assertEventDispatchThread();
        return components.indexOf(component);
    }

    @Override
    public int getLength() {
        assertEventDispatchThread();
        return components.getLength();
    }

    @Override
    public Iterator<Component> iterator() {
        assertEventDispatchThread();
        return new ImmutableIterator<Component>(components.iterator());
    }

    @Override
    protected void setParent(Container parent) {
        // If this container is being removed from the component hierarchy
        // and contains the focused component, clear the focus
        if (parent == null
            && containsFocus()) {
            clearFocus();
        }

        super.setParent(parent);
    }

    public Component getComponentAt(int x, int y) {
        assertEventDispatchThread();
        Component component = null;

        int i = components.getLength() - 1;
        while (i >= 0) {
            component = components.get(i);
            if (component.isVisible()) {
                Bounds bounds = component.getBounds();
                if (bounds.contains(x, y)) {
                    break;
                }
            }

            i--;
        }

        if (i < 0) {
            component = null;
        }

        return component;
    }

    public Component getDescendantAt(int x, int y) {
        Component component = getComponentAt(x, y);

        if (component instanceof Container) {
            Container container = (Container)component;
            component = container.getDescendantAt(x - container.getX(),
                y - container.getY());
        }

        if (component == null) {
            component = this;
        }

        return component;
    }

    public Component getNamedComponent(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }

        Component namedComponent = null;

        for (Component component : this) {
            if (name.equals(component.getName())) {
                namedComponent = component;
                break;
            }
        }

        return namedComponent;
    }

    @Override
    public void setVisible(boolean visible) {
        if (!visible
            && containsFocus()) {
            clearFocus();
        }

        super.setVisible(visible);
    }

    @Override
    protected void layout() {
        super.layout();

        for (int i = 0, n = components.getLength(); i < n; i++) {
            Component component = components.get(i);
            component.validate();
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        if (!doubleBuffering) {
            paint0(graphics);
        } else {
            boolean freshImage = false;
            if (doubleBufferImage == null) {
                GraphicsConfiguration gc = graphics.getDeviceConfiguration();
                doubleBufferImage = gc.createCompatibleImage(getWidth(), getHeight(),
                        Transparency.OPAQUE);
                doubleBufferedRepaintRequired = true;
                freshImage = true;
            }

            Graphics2D bufferedImageGraphics = (Graphics2D)doubleBufferImage.getGraphics();
            try {
                if (doubleBufferedRepaintRequired) {
                    if (!freshImage) {
                        bufferedImageGraphics.setClip(graphics.getClip());
                    }
                    paint0(bufferedImageGraphics);
                    doubleBufferedRepaintRequired = false;
                }
                graphics.drawImage(doubleBufferImage, 0, 0, null);
            } finally {
                bufferedImageGraphics.dispose();
            }

        }
    }

    private void paint0(Graphics2D graphics) {
        int count = getLength();

        // Determine the paint bounds
        Bounds paintBounds = new Bounds(0, 0, getWidth(), getHeight());
        Rectangle clipBounds = graphics.getClipBounds();
        if (clipBounds != null) {
            paintBounds = paintBounds.intersect(clipBounds);
        }

        // Determine if we need to paint the container, or if it's completely
        // obscured by a child component.
        boolean paintContainer = true;
        for (int i = 0; i < count; i++) {
            Component component = get(i);

            if (component.isVisible()
                && component.isOpaque()
                && component.getBounds().contains(paintBounds)) {
                paintContainer = false;
                break;
            }
        }

        if (paintContainer) {
            // Give the base method a copy of the graphics context; otherwise,
            // container skins can change the graphics state before it is passed
            // to subcomponents
            Graphics2D containerGraphics = (Graphics2D)graphics.create();
            super.paint(containerGraphics);
            containerGraphics.dispose();
        }

        for (int i = 0; i < count; i++) {
            Component component = get(i);

            // Calculate the decorated bounds
            Bounds decoratedBounds = component.getDecoratedBounds();

            // Only paint components that are visible and intersect the
            // current clip rectangle
            if (component.isVisible()
                && decoratedBounds.intersects(paintBounds)) {
                Bounds componentBounds = component.getBounds();

                // Create a copy of the current graphics context and
                // translate to the component's coordinate system
                Graphics2D decoratedGraphics = (Graphics2D)graphics.create();
                decoratedGraphics.translate(componentBounds.x, componentBounds.y);

                // Prepare the decorators
                DecoratorSequence decorators = component.getDecorators();
                int n = decorators.getLength();
                for (int j = n - 1; j >= 0; j--) {
                    Decorator decorator = decorators.get(j);
                    decoratedGraphics = decorator.prepare(component, decoratedGraphics);
                }

                // Paint the component
                Graphics2D componentGraphics = (Graphics2D)decoratedGraphics.create();
                componentGraphics.clipRect(0, 0, componentBounds.width, componentBounds.height);
                component.paint(componentGraphics);
                componentGraphics.dispose();

                // Update the decorators
                for (int j = 0; j < n; j++) {
                    Decorator decorator = decorators.get(j);
                    decorator.update();
                }
            }
        }
    }

    /**
     * Tests if this container is an ancestor of a given component. A container
     * is considered to be its own ancestor.
     *
     * @param component
     * The component to test.
     *
     * @return
     * <tt>true</tt> if this container is an ancestor of <tt>component</tt>;
     * <tt>false</tt> otherwise.
     */
    public boolean isAncestor(Component component) {
        boolean ancestor = false;

        Component parent = component;
        while (parent != null) {
           if (parent == this) {
              ancestor = true;
              break;
           }

           parent = parent.getParent();
        }

        return ancestor;
     }

    /**
     * Requests that focus be given to this container. If this container is not
     * focusable, this requests that focus be set to the first focusable
     * descendant in this container.
     *
     * @return
     * The component that got the focus, or <tt>null</tt> if the focus request
     * was denied
     */
    @Override
    public boolean requestFocus() {
        boolean focused = false;

        if (isFocusable()) {
            focused = super.requestFocus();
        } else {
            if (focusTraversalPolicy != null) {
                Component first = focusTraversalPolicy.getNextComponent(this, null, FocusTraversalDirection.FORWARD);

                Component component = first;
                while (component != null
                    && !component.requestFocus()) {
                    component = focusTraversalPolicy.getNextComponent(this, component, FocusTraversalDirection.FORWARD);

                    // Ensure that we don't get into an infinite loop
                    if (component == first) {
                        break;
                    }
                }

                focused = (component != null);
            }
        }

        return focused;
    }

    /**
     * Transfers focus to the next focusable component.
     *
     * @param component
     * The component from which focus will be transferred.
     *
     * @param direction
     * The direction in which to transfer focus.
     */
    public Component transferFocus(Component component, FocusTraversalDirection direction) {
        if (focusTraversalPolicy == null) {
            // The container has no traversal policy; move up a level
            component = transferFocus(direction);
        } else {
            do {
                component = focusTraversalPolicy.getNextComponent(this, component, direction);

                if (component != null) {
                    if (component.isFocusable()) {
                        component.requestFocus();
                    } else {
                        if (component instanceof Container) {
                            Container container = (Container)component;
                            component = container.transferFocus(null, direction);
                        }
                    }
                }
            } while (component != null
                && !component.isFocused());

            if (component == null) {
                // We are at the end of the traversal
                component = transferFocus(direction);
            }
        }

        return component;
    }

    /**
     * Returns this container's focus traversal policy.
     */
    public FocusTraversalPolicy getFocusTraversalPolicy() {
        return this.focusTraversalPolicy;
    }

    /**
     * Sets this container's focus traversal policy.
     *
     * @param focusTraversalPolicy
     * The focus traversal policy to use with this container.
     */
    public void setFocusTraversalPolicy(FocusTraversalPolicy focusTraversalPolicy) {
        FocusTraversalPolicy previousFocusTraversalPolicy = this.focusTraversalPolicy;

        if (previousFocusTraversalPolicy != focusTraversalPolicy) {
            this.focusTraversalPolicy = focusTraversalPolicy;
            containerListeners.focusTraversalPolicyChanged(this, previousFocusTraversalPolicy);
        }
    }

    /**
     * Tests whether this container is an ancestor of the currently focused
     * component.
     *
     * @return
     * <tt>true</tt> if a component is focused and this container is an
     * ancestor of the component; <tt>false</tt>, otherwise.
     */
    public boolean containsFocus() {
        Component focusedComponent = getFocusedComponent();
        return (focusedComponent != null
            && isAncestor(focusedComponent));
    }

    protected void descendantAdded(Component descendant) {
        Container parent = getParent();

        if (parent != null) {
            parent.descendantAdded(descendant);
        }
    }

    protected void descendantRemoved(Component descendant) {
        Container parent = getParent();

        if (parent != null) {
            parent.descendantRemoved(descendant);
        }
    }

    protected void descendantGainedFocus(Component descendant, Component previousFocusedComponent) {
        Container parent = getParent();

        if (parent != null) {
            parent.descendantGainedFocus(descendant, previousFocusedComponent);
        }
    }

    protected void descendantLostFocus(Component descendant) {
        Container parent = getParent();

        if (parent != null) {
            parent.descendantLostFocus(descendant);
        }
    }

    /**
     * Propagates binding to subcomponents.
     *
     * @param context
     */
    @Override
    public void load(Object context) {
        for (Component component : components) {
            component.load(context);
        }
    }

    /**
     * Propagates binding to subcomponents.
     *
     * @param context
     */
    @Override
    public void store(Object context) {
        for (Component component : components) {
            component.store(context);
        }
    }

    /**
     * Propagates clear operation to subcomponents.
     */
    @Override
    public void clear() {
        for (Component component : components) {
            component.clear();
        }
    }

    @Override
    protected boolean mouseMove(int x, int y) {
        boolean consumed = false;

        // Clear the mouse over component if its mouse-over state has
        // changed (e.g. if its enabled or visible properties have
        // changed)
        if (mouseOverComponent != null
            && !mouseOverComponent.isMouseOver()) {
            mouseOverComponent = null;
        }

        if (isEnabled()) {
            // Synthesize mouse over/out events
            Component component = getComponentAt(x, y);

            if (mouseOverComponent != component) {
                if (mouseOverComponent != null) {
                    mouseOverComponent.mouseOut();
                }

                mouseOverComponent = null;
                Mouse.setCursor(this);
            }

            // Notify container listeners
            consumed = containerMouseListeners.mouseMove(this, x, y);

            if (!consumed) {
                if (mouseOverComponent != component) {
                    mouseOverComponent = component;

                    if (mouseOverComponent != null) {
                        mouseOverComponent.mouseOver();
                        Mouse.setCursor(mouseOverComponent);
                    }
                }

                // Propagate event to subcomponents
                if (component != null) {
                    consumed = component.mouseMove(x - component.getX(),
                        y - component.getY());
                }

                // Notify the base class
                if (!consumed) {
                    consumed = super.mouseMove(x, y);
                }
            }
        }

        return consumed;
    }

    @Override
    protected void mouseOut() {
        // Ensure that mouse out is called on descendant components
        if (mouseOverComponent != null
            && mouseOverComponent.isMouseOver()) {
            mouseOverComponent.mouseOut();
        }

        mouseOverComponent = null;

        super.mouseOut();
    }

    @Override
    protected boolean mouseDown(Mouse.Button button, int x, int y) {
        boolean consumed = false;

        mouseDown = true;

        if (isEnabled()) {
            // Notify container listeners
            consumed = containerMouseListeners.mouseDown(this, button, x, y);

            if (!consumed) {
                // Synthesize mouse click event
                Component component = getComponentAt(x, y);

                long currentTime = System.currentTimeMillis();
                int multiClickInterval = Platform.getMultiClickInterval();
                if (mouseDownComponent == component
                    && currentTime - mouseDownTime < multiClickInterval) {
                    mouseClickCount++;
                } else {
                    mouseDownTime = System.currentTimeMillis();
                    mouseClickCount = 1;
                }

                mouseDownComponent = component;

                // Propagate event to subcomponents
                if (component != null) {
                    // Ensure that mouse over is called
                    if (!component.isMouseOver()) {
                        component.mouseOver();
                    }

                    consumed = component.mouseDown(button, x - component.getX(),
                        y - component.getY());
                }

                // Notify the base class
                if (!consumed) {
                    consumed = super.mouseDown(button, x, y);
                }
            }
        }

        return consumed;
    }

    @Override
    protected boolean mouseUp(Mouse.Button button, int x, int y) {
        boolean consumed = false;

        if (isEnabled()) {
            // Notify container listeners
            consumed = containerMouseListeners.mouseUp(this, button, x, y);

            if (!consumed) {
                // Propagate event to subcomponents
                Component component = getComponentAt(x, y);

                if (component != null) {
                    // Ensure that mouse over is called
                    if (!component.isMouseOver()) {
                        component.mouseOver();
                    }

                    consumed = component.mouseUp(button, x - component.getX(),
                        y - component.getY());
                }

                // Notify the base class
                if (!consumed) {
                    consumed = super.mouseUp(button, x, y);
                }

                // Synthesize mouse click event
                if (mouseDown
                    && component != null
                    && component == mouseDownComponent
                    && component.isEnabled()
                    && component.isVisible()) {
                    mouseClickConsumed = component.mouseClick(button, x - component.getX(),
                        y - component.getY(), mouseClickCount);
                }
            }
        }

        mouseDown = false;

        return consumed;
    }

    @Override
    protected boolean mouseClick(Mouse.Button button, int x, int y, int count) {
        if (isEnabled()) {
            if (!mouseClickConsumed) {
                // Allow the event to propagate
                mouseClickConsumed = super.mouseClick(button, x, y, count);
            }
        }

        return mouseClickConsumed;
    }

    @Override
    protected boolean mouseWheel(Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        boolean consumed = false;

        if (isEnabled()) {
            // Notify container listeners
            consumed = containerMouseListeners.mouseWheel(this, scrollType, scrollAmount,
                wheelRotation, x, y);

            if (!consumed) {
                // Propagate event to subcomponents
                Component component = getComponentAt(x, y);

                if (component != null) {
                    // Ensure that mouse over is called
                    if (!component.isMouseOver()) {
                        component.mouseOver();
                    }

                    consumed = component.mouseWheel(scrollType, scrollAmount, wheelRotation,
                        x - component.getX(), y - component.getY());
                }

                // Notify the base class
                if (!consumed) {
                    consumed = super.mouseWheel(scrollType, scrollAmount, wheelRotation, x, y);
                }
            }
        }

        return consumed;
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if (doubleBuffering) {
            doubleBufferImage = null;
        }
    }

    @Override
    public void repaint(int x, int y, int width, int height, boolean immediate) {
        super.repaint(x, y, width, height, immediate);

        if (doubleBuffering) {
            doubleBufferedRepaintRequired = true;
        }
    }

    @Override
    public Graphics2D getGraphics() {
        Graphics2D g = super.getGraphics();
        doubleBufferedRepaintRequired = true;
        return g;
    }

    public boolean isDoubleBuffered() {
        return doubleBuffering;
    }

    public void setDoubleBuffered(boolean b) {
       doubleBuffering = b;
       if (b) {
           invalidate();
       } else {
           doubleBufferImage = null;
           doubleBufferedRepaintRequired = false;
       }
    }

    public ListenerList<ContainerListener> getContainerListeners() {
        return containerListeners;
    }

    public ListenerList<ContainerMouseListener> getContainerMouseListeners() {
        return containerMouseListeners;
    }

    public interface EDT_Checker {
        public void check(Component component);
    }

    protected final void assertEventDispatchThread() {
        assertEventDispatchThread(this);
    }

    private static EDT_Checker EDT_CHECKER = new EDT_Checker() {
        @Override
        public void check(Component component) {
            String threadName = Thread.currentThread().getName();
            /* Currently, application startup happens on the main thread, so we need to allow
             * that thread to modify WTK state.
             */
            if (threadName.equals("main") || threadName.equals("javawsApplicationMain")) {
                return;
            }
            // Allow components to be constructed from outside the event thread
            if (component.getDisplay() == null) {
                return;
            }
            /*
             * See Sun/Oracle bug 6424157. There is a race condition where we can be running on the event thread
             * but isDispatchThread() will return false.
             */
            if (threadName.startsWith("AWT-EventQueue-")) {
                return;
            }
            if (!java.awt.EventQueue.isDispatchThread()) {
                throw new IllegalStateException("this method can only be called from the AWT event dispatch thread"
                    + ", and not from \"" + threadName + "\"");
            }
        }
    };

    public static final void assertEventDispatchThread(Component component) {
        if (EDT_CHECKER != null) {
            EDT_CHECKER.check(component);
        }
    }

    public static final void setEventDispatchThreadChecker(EDT_Checker runnable) {
        EDT_CHECKER = runnable;
    }
}
