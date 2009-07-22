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
import java.awt.Rectangle;
import java.util.Iterator;

import org.apache.pivot.beans.BeanDictionary;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.effects.Decorator;


/**
 * Abstract base class for containers.
 * <p>
 * NOTES:
 * <ul>
 * <li>Child components that have special meaning to a container should be
 * installed via a dedicated method (for example,
 * {@link org.apache.pivot.wtk.Window#setContent(Component)}); additional components may
 * be added by the skin when installed. Other components may still be added but
 * may not be rendered properly by the installed skin.</li>
 * <li>Callers should not rely on component position within container to mean
 * anything other than paint order.</li>
 * </ul>
 *
 * @author gbrown
 */
public abstract class Container extends Component
    implements Sequence<Component>, Iterable<Component> {
    private static class ContainerListenerList extends ListenerList<ContainerListener>
        implements ContainerListener {
        public void componentInserted(Container container, int index) {
            for (ContainerListener listener : this) {
                listener.componentInserted(container, index);
            }
        }

        public void componentsRemoved(Container container, int index, Sequence<Component> components) {
            for (ContainerListener listener : this) {
                listener.componentsRemoved(container, index, components);
            }
        }

        public void contextKeyChanged(Container container, String previousContextKey) {
            for (ContainerListener listener : this) {
                listener.contextKeyChanged(container, previousContextKey);
            }
        }

        public void focusTraversalPolicyChanged(Container container,
            FocusTraversalPolicy previousFocusTraversalPolicy) {
            for (ContainerListener listener : this) {
                listener.focusTraversalPolicyChanged(container, previousFocusTraversalPolicy);
            }
        }
    }

    private static class ContainerMouseListenerList extends ListenerList<ContainerMouseListener>
        implements ContainerMouseListener {
        public boolean mouseMove(Container container, int x, int y) {
            boolean consumed = false;

            for (ContainerMouseListener listener : this) {
                consumed |= listener.mouseMove(container, x, y);
            }

            return consumed;
        }

        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            boolean consumed = false;

            for (ContainerMouseListener listener : this) {
                consumed |= listener.mouseDown(container, button, x, y);
            }

            return consumed;
        }

        public boolean mouseUp(Container container, Mouse.Button button, int x, int y) {
            boolean consumed = false;

            for (ContainerMouseListener listener : this) {
                consumed |= listener.mouseUp(container, button, x, y);
            }

            return consumed;
        }

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
    private String contextKey = null;

    private Component mouseOverComponent = null;

    private Component mouseDownComponent = null;
    private long mouseDownTime = 0;
    private int mouseClickCount = 0;
    private boolean mouseClickConsumed = false;

    private ContainerListenerList containerListeners = new ContainerListenerList();
    private ContainerMouseListenerList containerMouseListeners = new ContainerMouseListenerList();

    public final int add(Component component) {
        int i = getLength();
        insert(component, i);

        return i;
    }

    public void insert(Component component, int index) {
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

    public Component update(int index, Component component) {
        throw new UnsupportedOperationException();
    }

    public final int remove(Component component) {
        int index = indexOf(component);
        if (index != -1) {
            remove(index, 1);
        }

        return index;
    }

    public Sequence<Component> remove(int index, int count) {
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
     * Moves a component within the component sequence. This method does not
     * fire any events; it is the caller's responsibility to ensure that
     * appropriate events are fired (see
     * {@link WindowListener#windowMoved(Window, int, int)} as an example).
     *
     * @param from
     * @param to
     */
    protected void move(int from, int to) {
        if (from != to) {
            Sequence<Component> removed = components.remove(from, 1);
            Component component = removed.get(0);
            components.insert(component, to);

            // Repaint the area occupied by the component
            repaint(component.getDecoratedBounds());
        }
    }

    public Component get(int index) {
        return components.get(index);
    }

    public int indexOf(Component component) {
        return components.indexOf(component);
    }

    public int getLength() {
        return components.getLength();
    }

    public Iterator<Component> iterator() {
        return new ImmutableIterator<Component>(components.iterator());
    }

    @Override
    protected void setParent(Container parent) {
        // If this container is being removed from the component hierarchy
        // and contains the focused component, clear the focus
        if (parent == null
            && containsFocus()) {
            clearFocus(true);
        }

        super.setParent(parent);
    }

    public Component getComponentAt(int x, int y) {
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

    @Override
    public void setVisible(boolean visible) {
        if (!visible
            && containsFocus()) {
            clearFocus(true);
        }

        super.setVisible(visible);
    }

    @Override
    public void validate() {
        if (!isValid()
            && isVisible()) {
            super.validate();

            for (int i = 0, n = components.getLength(); i < n; i++) {
                Component component = components.get(i);
                component.validate();
            }
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        int count = getLength();

        // Determine the paint bounds
        Bounds paintBounds = new Bounds(0, 0, getWidth(), getHeight());
        Rectangle clipBounds = graphics.getClipBounds();
        if (clipBounds != null) {
            paintBounds = paintBounds.intersect(new Bounds(clipBounds));
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

    @Override
    public void setEnabled(boolean enabled) {
        if (isEnabled() != enabled) {
            super.setEnabled(enabled);

            if (isEnabled() == enabled) {
                if (!enabled
                    && containsFocus()) {
                    clearFocus(true);
                }
            }
        }
    }

    /**
     * Unsupported for containers. Only leaf components can have tooltips.
     */
    public void setTooltip(String tooltip) {
        throw new UnsupportedOperationException("A container cannot have a toolip.");
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
     * @return
     * <tt>false</tt>; containers are not focusable.
     */
    @Override
    public final boolean isFocusable() {
        return false;
    }

    /**
     * Requests that focus be set to the first focusable descendant in this
     * container.
     */
    @Override
    protected boolean requestFocus(boolean temporary) {
        if (isShowing()
            && !isBlocked()) {
            FocusTraversalPolicy focusTraversalPolicy = getFocusTraversalPolicy();
            Component component = focusTraversalPolicy.getNextComponent(this, null, Direction.FORWARD);
            if (component != null) {
                component.requestFocus();
            }
        }

        return containsFocus();
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

    /**
     * Returns the container's context key.
     *
     * @return
     * The context key, or <tt>null</tt> if no context key is set.
     */
    public String getContextKey() {
        return contextKey;
    }

    /**
     * Sets the component's context key.
     *
     * @param contextKey
     * The context key, or <tt>null</tt> to clear the context.
     */
    public void setContextKey(String contextKey) {
        String previousContextKey = this.contextKey;

        if ((previousContextKey != null
            && contextKey != null
            && !previousContextKey.equals(contextKey))
            || previousContextKey != contextKey) {
            this.contextKey = contextKey;
            containerListeners.contextKeyChanged(this, previousContextKey);
        }
    }

    /**
     * Propagates binding to subcomponents. If this container has a binding
     * set, propagates the bound value as a nested context.
     *
     * @param context
     */
    @SuppressWarnings("unchecked")
    public void load(Dictionary<String, ?> context) {
        if (contextKey != null
            && context.containsKey(contextKey)) {
            Object value = context.get(contextKey);
            if (value instanceof Dictionary<?, ?>) {
                context = (Map<String, Object>)value;
            } else {
                context = new BeanDictionary(value);
            }
        }

        for (Component component : components) {
            component.load(context);
        }
    }

    /**
     * Propagates binding to subcomponents. If this container has a binding
     * set, propagates the bound value as a nested context.
     *
     * @param context
     */
    @SuppressWarnings("unchecked")
    public void store(Dictionary<String, ?> context) {
        if (isEnabled()) {
            if (contextKey != null) {
                // Bound value is expected to be a sub-context
                Object value = context.get(contextKey);
                if (value instanceof Dictionary<?, ?>) {
                    context = (Map<String, Object>)value;
                } else {
                    context = new BeanDictionary(value);
                }
            }

            for (Component component : components) {
                component.store(context);
            }
        }
    }

    @Override
    protected boolean mouseMove(int x, int y) {
        boolean consumed = false;

        if (isEnabled()) {
            // Notify container listeners
            consumed = containerMouseListeners.mouseMove(this, x, y);

            if (!consumed) {
                // Clear the mouse over component if its mouse-over state has
                // changed (e.g. if its enabled or visible properties have
                // changed)
                if (mouseOverComponent != null
                    && !mouseOverComponent.isMouseOver()) {
                    mouseOverComponent = null;
                }

                // Synthesize mouse over/out events
                Component component = getComponentAt(x, y);

                if (mouseOverComponent != component) {
                    if (mouseOverComponent != null) {
                        mouseOverComponent.mouseOut();
                    }

                    mouseOverComponent = component;

                    if (mouseOverComponent == null) {
                        Mouse.setCursor(this);
                    } else {
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
                    consumed = component.mouseUp(button, x - component.getX(),
                        y - component.getY());
                }

                // Notify the base class
                if (!consumed) {
                    consumed = super.mouseUp(button, x, y);
                }

                // Synthesize mouse click event
                if (component != null
                    && component == mouseDownComponent
                    && mouseDownComponent.isEnabled()
                    && mouseDownComponent.isVisible()) {
                    mouseClickConsumed = component.mouseClick(button, x - component.getX(),
                        y - component.getY(), mouseClickCount);
                }
            }
        }

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

    public ListenerList<ContainerListener> getContainerListeners() {
        return containerListeners;
    }

    public ListenerList<ContainerMouseListener> getContainerMouseListeners() {
        return containerMouseListeners;
    }
}
