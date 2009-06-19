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
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import org.apache.pivot.beans.BeanDictionary;
import org.apache.pivot.beans.PropertyNotFoundException;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.HashSet;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.effects.Decorator;


/**
 * Top level abstract base class for all components. In MVC terminology, a
 * component represents the "controller". It has no inherent visual
 * representation and acts as an intermediary between the component's data (the
 * "model") and the skin, an implementation of {@link Skin} which serves as
 * the "view".
 * <p>
 * TODO Add a contains() method or some equivalent that will support mouse
 * interaction with non-rectangular components.
 */
public abstract class Component implements ConstrainedVisual {
    /**
     * Style dictionary implementation.
     *
     * @author gbrown
     */
    public final class StyleDictionary extends BeanDictionary {
        private StyleDictionary(org.apache.pivot.wtk.Skin skin) {
            super(skin, true);
        }

        public Object put(String key, Object value) {
            Object previousValue = null;

            try {
                previousValue = super.put(key, value);
                customStyles.add(key);
                componentListeners.styleUpdated(Component.this, key, previousValue);
            } catch(PropertyNotFoundException exception) {
                System.err.println("\"" + key + "\" is not a valid style for "
                    + Component.this);
            }

            return previousValue;
        }
    }

    /**
     * User data dictionary implementation.
     *
     * @author gbrown
     */
    public final class UserDataDictionary implements
        Dictionary<String, Object>, Iterable<String> {
        private UserDataDictionary() {
        }

        public Object get(String key) {
            return userData.get(key);
        }

        public Object put(String key, Object value) {
            boolean update = userData.containsKey(key);
            Object previousValue = userData.put(key, value);

            if (update) {
                componentDataListeners.valueUpdated(Component.this, key, previousValue);
            } else {
                componentDataListeners.valueAdded(Component.this, key);
            }

            return previousValue;
        }

        public Object remove(String key) {
            Object previousValue;
            if (userData.containsKey(key)) {
                previousValue = userData.remove(key);
                componentDataListeners.valueRemoved(Component.this, key, previousValue);
            } else {
                previousValue = null;
            }

            return previousValue;
        }

        public boolean containsKey(String key) {
            return userData.containsKey(key);
        }

        public boolean isEmpty() {
            return userData.isEmpty();
        }

        public Iterator<String> iterator() {
            return new ImmutableIterator<String>(userData.iterator());
        }
    }

    /**
     * Decorator sequence implementation.
     *
     * @author tvolkert
     * @author gbrown
     */
    public final class DecoratorSequence implements Sequence<Decorator>,
        Iterable<Decorator> {
        public int add(Decorator decorator) {
            int i = getLength();
            insert(decorator, i);

            return i;
        }

        public void insert(Decorator decorator, int index) {
            if (decorator == null) {
                throw new IllegalArgumentException("decorator is null");
            }

            // Repaint the the component's previous decorated region
            if (parent != null) {
                parent.repaint(getDecoratedBounds());
            }

            decorators.insert(decorator, index);

            // Repaint the the component's current decorated region
            if (parent != null) {
                parent.repaint(getDecoratedBounds());
            }

            componentDecoratorListeners.decoratorInserted(Component.this, index);
        }

        public Decorator update(int index, Decorator decorator) {
            if (decorator == null) {
                throw new IllegalArgumentException("decorator is null.");
            }

            // Repaint the the component's previous decorated region
            if (parent != null) {
                parent.repaint(getDecoratedBounds());
            }

            Decorator previousDecorator = decorators.update(index, decorator);

            // Repaint the the component's current decorated region
            if (parent != null) {
                parent.repaint(getDecoratedBounds());
            }

            componentDecoratorListeners.decoratorUpdated(Component.this, index,
                previousDecorator);

            return previousDecorator;
        }

        public int remove(Decorator decorator) {
            int index = indexOf(decorator);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        public Sequence<Decorator> remove(int index, int count) {
            if (count > 0) {
                // Repaint the the component's previous decorated region
                if (parent != null) {
                    parent.repaint(getDecoratedBounds());
                }
            }

            Sequence<Decorator> removed = decorators.remove(index, count);

            if (count > 0) {
                if (parent != null) {
                    // Repaint the the component's current decorated region
                    parent.repaint(getDecoratedBounds());
                }

                componentDecoratorListeners.decoratorsRemoved(Component.this, index, removed);
            }

            return removed;
        }

        public Sequence<Decorator> removeAll() {
            return remove(0, getLength());
        }

        public Decorator get(int index) {
            return decorators.get(index);
        }

        public int indexOf(Decorator decorator) {
            return decorators.indexOf(decorator);
        }

        public int getLength() {
            return decorators.getLength();
        }

        public Iterator<Decorator> iterator() {
            return new ImmutableIterator<Decorator>(decorators.iterator());
        }
    }

    /**
     * Abstract base class for component "attributes". Attributes are attached
     * properties that are specific to a particular component type.
     *
     * @author gbrown
     */
    public static abstract class Attributes {
        private Component component = null;

        protected Attributes() {
        }

        public Component getComponent() {
            return component;
        }

        private void setComponent(Component component) {
            this.component = component;
        }
    }

    /**
     * Provides dictionary access to all components by handle.
     *
     * @author gbrown
     */
    public static class ComponentDictionary implements
        Dictionary<Integer, Component>, Iterable<Integer> {
        private ComponentDictionary() {
        }

        public Component get(Integer key) {
            return components.get(key);
        }

        public Component put(Integer key, Component value) {
            throw new UnsupportedOperationException();
        }

        public Component remove(Integer key) {
            throw new UnsupportedOperationException();
        }

        public boolean containsKey(Integer key) {
            return components.containsKey(key);
        }

        public boolean isEmpty() {
            return components.isEmpty();
        }

        public Iterator<Integer> iterator() {
            return new ImmutableIterator<Integer>(components.iterator());
        }
    }

    private static class ComponentListenerList extends ListenerList<ComponentListener>
        implements ComponentListener {
        public void parentChanged(Component component, Container previousParent) {
            for (ComponentListener listener : this) {
                listener.parentChanged(component, previousParent);
            }
        }

        public void sizeChanged(Component component, int previousWidth, int previousHeight) {
            for (ComponentListener listener : this) {
                listener.sizeChanged(component, previousWidth, previousHeight);
            }
        }

        public void locationChanged(Component component, int previousX, int previousY) {
            for (ComponentListener listener : this) {
                listener.locationChanged(component, previousX, previousY);
            }
        }

        public void visibleChanged(Component component) {
            for (ComponentListener listener : this) {
                listener.visibleChanged(component);
            }
        }

        public void styleUpdated(Component component, String styleKey, Object previousValue) {
            for (ComponentListener listener : this) {
                listener.styleUpdated(component, styleKey, previousValue);
            }
        }

        public void cursorChanged(Component component, Cursor previousCursor) {
            for (ComponentListener listener : this) {
                listener.cursorChanged(component, previousCursor);
            }
        }

        public void tooltipTextChanged(Component component, String previousTooltipText) {
            for (ComponentListener listener : this) {
                listener.tooltipTextChanged(component, previousTooltipText);
            }
        }
    }

    private static class ComponentLayoutListenerList extends
        ListenerList<ComponentLayoutListener> implements ComponentLayoutListener {
        public void preferredSizeChanged(Component component,
            int previousPreferredWidth, int previousPreferredHeight) {
            for (ComponentLayoutListener listener : this) {
                listener.preferredSizeChanged(component,
                    previousPreferredWidth, previousPreferredHeight);
            }
        }

        public void displayableChanged(Component component) {
            for (ComponentLayoutListener listener : this) {
                listener.displayableChanged(component);
            }
        }
    }

    private static class ComponentStateListenerList extends
        ListenerList<ComponentStateListener> implements ComponentStateListener {
        public void enabledChanged(Component component) {
            for (ComponentStateListener listener : this) {
                listener.enabledChanged(component);
            }
        }

        public void focusedChanged(Component component, boolean temporary) {
            for (ComponentStateListener listener : this) {
                listener.focusedChanged(component, temporary);
            }
        }
    }

    private static class ComponentDecoratorListenerList extends
        ListenerList<ComponentDecoratorListener> implements ComponentDecoratorListener {
        public void decoratorInserted(Component component, int index) {
            for (ComponentDecoratorListener listener : this) {
                listener.decoratorInserted(component, index);
            }
        }

        public void decoratorUpdated(Component component, int index, Decorator previousDecorator) {
            for (ComponentDecoratorListener listener : this) {
                listener.decoratorUpdated(component, index, previousDecorator);
            }
        }

        public void decoratorsRemoved(Component component, int index,
            Sequence<Decorator> decorators) {
            for (ComponentDecoratorListener listener : this) {
                listener.decoratorsRemoved(component, index, decorators);
            }
        }
    }

    private static class ComponentMouseListenerList extends ListenerList<ComponentMouseListener>
        implements ComponentMouseListener {
        public boolean mouseMove(Component component, int x, int y) {
            boolean consumed = false;

            for (ComponentMouseListener listener : this) {
                consumed |= listener.mouseMove(component, x, y);
            }

            return consumed;
        }

        public void mouseOver(Component component) {
            for (ComponentMouseListener listener : this) {
                listener.mouseOver(component);
            }
        }

        public void mouseOut(Component component) {
            for (ComponentMouseListener listener : this) {
                listener.mouseOut(component);
            }
        }
    }

    private static class ComponentMouseButtonListenerList extends ListenerList<ComponentMouseButtonListener>
        implements ComponentMouseButtonListener {
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = false;

            for (ComponentMouseButtonListener listener : this) {
                consumed |= listener.mouseDown(component, button, x, y);
            }

            return consumed;
        }

        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = false;

            for (ComponentMouseButtonListener listener : this) {
                consumed |= listener.mouseUp(component, button, x, y);
            }

            return consumed;
        }

        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            boolean consumed = false;

            for (ComponentMouseButtonListener listener : this) {
                consumed |= listener.mouseClick(component, button, x, y, count);
            }

            return consumed;
        }
    }

    private static class ComponentMouseWheelListenerList extends ListenerList<ComponentMouseWheelListener>
        implements ComponentMouseWheelListener {
        public boolean mouseWheel(Component component, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            boolean consumed = false;

            for (ComponentMouseWheelListener listener : this) {
                consumed |= listener.mouseWheel(component, scrollType, scrollAmount,
                    wheelRotation, x, y);
            }

            return consumed;
        }
    }

    private static class ComponentKeyListenerList extends ListenerList<ComponentKeyListener>
        implements ComponentKeyListener {
        public boolean keyTyped(Component component, char character) {
            boolean consumed = false;

            for (ComponentKeyListener listener : this) {
                consumed |= listener.keyTyped(component, character);
            }

            return consumed;
        }

        public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            boolean consumed = false;

            for (ComponentKeyListener listener : this) {
                consumed |= listener.keyPressed(component, keyCode, keyLocation);
            }

            return consumed;
        }

        public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            boolean consumed = false;

            for (ComponentKeyListener listener : this) {
                consumed |= listener.keyReleased(component, keyCode, keyLocation);
            }

            return consumed;
        }
    }

    private static class ComponentDragDropListenerList extends ListenerList<ComponentDragDropListener>
        implements ComponentDragDropListener {
        public void dragSourceChanged(Component component, DragSource previousDragSource) {
            for (ComponentDragDropListener listener : this) {
                listener.dragSourceChanged(component, previousDragSource);
            }
        }

        public void dropTargetChanged(Component component, DropTarget previousDropTarget) {
            for (ComponentDragDropListener listener : this) {
                listener.dropTargetChanged(component, previousDropTarget);
            }
        }
    }

    private static class ComponentDataListenerList extends ListenerList<ComponentDataListener>
        implements ComponentDataListener {
        public void valueAdded(Component component, String key) {
            for (ComponentDataListener listener : this) {
                listener.valueAdded(component, key);
            }
        }

        public void valueUpdated(Component component, String key, Object previousValue) {
            for (ComponentDataListener listener : this) {
                listener.valueUpdated(component, key, previousValue);
            }
        }

        public void valueRemoved(Component component, String key, Object value) {
            for (ComponentDataListener listener : this) {
                listener.valueRemoved(component, key, value);
            }
        }
    }

    private static class ComponentClassListenerList extends ListenerList<ComponentClassListener>
        implements ComponentClassListener {
        public void focusedComponentChanged(Component previousFocusedComponent) {
            for (ComponentClassListener listener : this) {
                listener.focusedComponentChanged(previousFocusedComponent);
            }
        }
    }

    // The component's handle
    private final Integer handle;

    // The currently installed skin, or null if no skin is installed
    private org.apache.pivot.wtk.Skin skin = null;

    // Preferred width and height values explicitly set by the user
    private int preferredWidth = -1;
    private int preferredHeight = -1;

    // Calculated preferred size value
    private Dimensions preferredSize = null;

    // The component's parent container, or null if the component does not have
    // a parent
    private Container parent = null;

    // The component's valid state
    private boolean valid = false;

    // The component's location, relative to the parent's origin
    private int x = 0;
    private int y = 0;

    // The component's visible flag
    private boolean visible = true;

    // The component's displayable flag
    private boolean displayable = true;

    // The component's decorators
    private ArrayList<Decorator> decorators = new ArrayList<Decorator>();
    private DecoratorSequence decoratorSequence = new DecoratorSequence();

    // The component's enabled flag
    private boolean enabled = true;

    // The component's mouse-over flag
    private boolean mouseOver = false;

    // The cursor that is displayed over the component
    private Cursor cursor = null;

    // The tooltip text
    private String tooltipText = null;

    // The component's drag source
    private DragSource dragSource = null;

    // The component's drop target
    private DropTarget dropTarget = null;

    // User data
    private HashMap<String, Object> userData = new HashMap<String, Object>();
    private UserDataDictionary userDataDictionary = new UserDataDictionary();

    // Proxy class for getting/setting style properties on the skin
    private StyleDictionary styleDictionary = null;

    // Custom style keys
    private HashSet<String> customStyles = new HashSet<String>();

    // Attached properties
    private Attributes attributes = null;

    // Event listener lists.
    private ComponentListenerList componentListeners = new ComponentListenerList();
    private ComponentLayoutListenerList componentLayoutListeners = new ComponentLayoutListenerList();
    private ComponentStateListenerList componentStateListeners = new ComponentStateListenerList();
    private ComponentDecoratorListenerList componentDecoratorListeners = new ComponentDecoratorListenerList();
    private ComponentMouseListenerList componentMouseListeners = new ComponentMouseListenerList();
    private ComponentMouseButtonListenerList componentMouseButtonListeners = new ComponentMouseButtonListenerList();
    private ComponentMouseWheelListenerList componentMouseWheelListeners = new ComponentMouseWheelListenerList();
    private ComponentKeyListenerList componentKeyListeners = new ComponentKeyListenerList();
    private ComponentDragDropListenerList componentDragDropListeners = new ComponentDragDropListenerList();
    private ComponentDataListenerList componentDataListeners = new ComponentDataListenerList();

    // The component that currently has the focus
    private static Component focusedComponent = null;

    // The next available component handle
    private static int nextHandle = 0;

    // Map of all components by handle
    private static HashMap<Integer, Component> components = new HashMap<Integer, Component>(true);
    private static ComponentDictionary componentDictionary = new ComponentDictionary();

    // Class event listeners
    private static ComponentClassListenerList componentClassListeners = new ComponentClassListenerList();

    /**
     * Creates a new component.
     */
    public Component() {
        handle = nextHandle++;
        components.put(handle, this);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            components.remove(handle);
        } finally {
            super.finalize();
        }
    }

    /**
     * Return's the component's handle.
     */
    public Integer getHandle() {
        return handle;
    }

    /**
     * Returns the currently installed skin.
     *
     * @return
     * The currently installed skin.
     */
    protected org.apache.pivot.wtk.Skin getSkin() {
        return skin;
    }

    /**
     * Sets the skin, replacing any previous skin.
     *
     * @param skin
     * The new skin.
     */
    protected void setSkin(org.apache.pivot.wtk.Skin skin) {
        if (skin == null) {
            throw new IllegalArgumentException("skin is null.");
        }

        if (this.skin != null) {
            this.skin.uninstall();
        }

        this.skin = skin;
        styleDictionary = new StyleDictionary(skin);
        skin.install(this);

        invalidate();
        repaint();
    }

    /**
     * Installs the skin for the given component class, unless a subclass has
     * defined a more specific skin. Any component that defines a custom skin
     * class must call this method.
     *
     * @param componentClass
     */
    @SuppressWarnings("unchecked")
    protected final void installSkin(Class<? extends Component> componentClass) {
        // Walk up component hierarchy from this type; if we find a match
        // and the super class equals the given component class, install
        // the skin. Otherwise, ignore - it will be installed later by a
        // subclass of the component class.
        Class<?> type = getClass();

        Theme theme = Theme.getTheme();
        Class<? extends org.apache.pivot.wtk.Skin> skinClass = theme.getSkinClass((Class<? extends Component>)type);

        while (skinClass == null
            && type != componentClass
            && type != Component.class) {
            type = type.getSuperclass();

            if (type != Component.class) {
                skinClass = theme.getSkinClass((Class<? extends Component>)type);
            }
        }

        if (type == Component.class) {
            throw new IllegalArgumentException(componentClass.getName()
                + " is not an ancestor of " + getClass().getName());
        }

        if (skinClass == null) {
            throw new IllegalArgumentException("No skin mapping for "
                + componentClass.getName() + " found.");
        }

        if (type == componentClass) {
            // Cache the values of custom styles
            HashMap<String, Object> styles = new HashMap<String, Object>();
            for (String key : customStyles) {
                styles.put(key, styleDictionary.get(key));
            }

            try {
                setSkin(skinClass.newInstance());
            } catch(InstantiationException exception) {
                throw new IllegalArgumentException(exception);
            } catch(IllegalAccessException exception) {
                throw new IllegalArgumentException(exception);
            }

            // Re-apply custom styles
            setStyles(styles);
        }
    }

    public Container getParent() {
        return parent;
    }

    protected void setParent(Container parent) {
        // If this component is being removed from the component hierarchy
        // and is currently focused, clear the focus
        if (parent == null
            && isFocused()) {
            clearFocus();
        }

        Container previousParent = this.parent;
        this.parent = parent;

        componentListeners.parentChanged(this, previousParent);
    }

    public Window getWindow() {
        Component component = this;

        while (component != null
            && !(component instanceof Window)) {
            component = component.getParent();
        }

        return (Window)component;
    }

    public Display getDisplay() {
        Component component = this;

        while (component != null
            && !(component instanceof Display)) {
            component = component.getParent();
        }

        return (Display)component;
    }

    public int getWidth() {
        return skin.getWidth();
    }

    public void setWidth(int width) {
        setSize(width, getHeight());
    }

    public int getHeight() {
        return skin.getHeight();
    }

    public void setHeight(int height) {
        setSize(getWidth(), height);
    }

    public Dimensions getSize() {
        return new Dimensions(this.getWidth(), this.getHeight());
    }

    public final void setSize(Dimensions size) {
        if (size == null) {
            throw new IllegalArgumentException("size is null.");
        }

        setSize(size.width, size.height);
    }

    /**
     * NOTE This method should only be called during layout. Callers should
     * use {@link #setPreferredSize(int, int)}.
     *
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        if (width < 0) {
            throw new IllegalArgumentException("width is negative.");
        }

        if (height < 0) {
            throw new IllegalArgumentException("height is negative.");
        }

        int previousWidth = getWidth();
        int previousHeight = getHeight();

        if (width != previousWidth
            || height != previousHeight) {
            // This component's size changed, most likely as a result
            // of being laid out; it must be flagged as invalid to ensure
            // that layout is propagated downward when validate() is
            // called on it
            invalidate();

            // Redraw the region formerly occupied by this component
            if (parent != null) {
                parent.repaint(getDecoratedBounds());
            }

            // Set the size of the skin
            skin.setSize(width, height);

            // Redraw the region currently occupied by this component
            if (parent != null) {
                parent.repaint(getDecoratedBounds());
            }

            componentListeners.sizeChanged(this, previousWidth, previousHeight);
        }
    }

    /**
     * Returns the component's unconstrained preferred width.
     */
    public int getPreferredWidth() {
        return getPreferredWidth(-1);
    }

    /**
     * Returns the component's constrained preferred width.
     *
     * @param height
     * The height value by which the preferred width should be constrained, or
     * <tt>-1</tt> for no constraint.
     *
     * @return
     * The constrained preferred width.
     */
    public int getPreferredWidth(int height) {
        int preferredWidth;

        if (this.preferredWidth == -1) {
            if (height == -1) {
                preferredWidth = getPreferredSize().width;
            } else {
                preferredWidth = skin.getPreferredWidth(height);
            }
        } else {
            preferredWidth = this.preferredWidth;
        }

        return preferredWidth;
    }

    /**
     * Sets the component's preferred width.
     *
     * @param preferredWidth
     * The preferred width value, or <tt>-1</tt> to use the default
     * value determined by the skin.
     */
    public void setPreferredWidth(int preferredWidth) {
        if (preferredWidth < -1) {
            throw new IllegalArgumentException(preferredWidth
                + " is not a valid value for preferredWidth.");
        }

        int previousPreferredWidth = this.preferredWidth;

        if (previousPreferredWidth != preferredWidth) {
            this.preferredWidth = preferredWidth;

            invalidate();

            componentLayoutListeners.preferredSizeChanged(this,
                previousPreferredWidth, preferredHeight);
        }
    }

    /**
     * Returns a flag indicating whether the preferred width was explicitly
     * set by the caller or is the default value determined by the skin.
     *
     * @return
     * <tt>true</tt> if the preferred width was explicitly set; <tt>false</tt>,
     * otherwise.
     */
    public boolean isPreferredWidthSet() {
        return (preferredWidth != -1);
    }

    /**
     * Returns the component's unconstrained preferred height.
     */
    public int getPreferredHeight() {
        return getPreferredHeight(-1);
    }

    /**
     * Returns the component's constrained preferred height.
     *
     * @param width
     * The width value by which the preferred height should be constrained, or
     * <tt>-1</tt> for no constraint.
     *
     * @return
     * The constrained preferred height.
     */
    public int getPreferredHeight(int width) {
        int preferredHeight;

        if (this.preferredHeight == -1) {
            if (width == -1) {
                preferredHeight = getPreferredSize().height;
            } else {
                preferredHeight = skin.getPreferredHeight(width);
            }
        } else {
            preferredHeight = this.preferredHeight;
        }

        return preferredHeight;
    }

    /**
     * Sets the component's preferred height.
     *
     * @param preferredHeight
     * The preferred height value, or <tt>-1</tt> to use the default
     * value determined by the skin.
     */
    public void setPreferredHeight(int preferredHeight) {
        if (preferredHeight < -1) {
            throw new IllegalArgumentException(preferredHeight
                + " is not a valid value for preferredHeight.");
        }

        int previousPreferredHeight = this.preferredHeight;

        if (previousPreferredHeight != preferredHeight) {
            this.preferredHeight = preferredHeight;

            invalidate();

            componentLayoutListeners.preferredSizeChanged(this,
                preferredWidth, previousPreferredHeight);
        }
    }

    /**
     * Returns a flag indicating whether the preferred height was explicitly
     * set by the caller or is the default value determined by the skin.
     *
     * @return
     * <tt>true</tt> if the preferred height was explicitly set; <tt>false</tt>,
     * otherwise.
     */
    public boolean isPreferredHeightSet() {
        return (preferredHeight != -1);
    }

    /**
     * Gets the component's unconstrained preferred size.
     */
    public Dimensions getPreferredSize() {
        if (preferredSize == null) {
            if (preferredWidth == -1
                && preferredHeight == -1) {
                preferredSize = skin.getPreferredSize();
            } else if (preferredWidth == -1) {
                preferredSize = new Dimensions(skin.getPreferredWidth(preferredHeight),
                    preferredHeight);
            } else if (preferredHeight == -1) {
                preferredSize = new Dimensions(preferredWidth,
                    skin.getPreferredHeight(preferredWidth));
            } else {
                preferredSize = new Dimensions(preferredWidth, preferredHeight);
            }
        }

        return preferredSize;
    }

    public final void setPreferredSize(Dimensions preferredSize) {
        if (preferredSize == null) {
            throw new IllegalArgumentException("preferredSize is null.");
        }

        setPreferredSize(preferredSize.width, preferredSize.height);
    }

    /**
     * Sets the component's preferred size.
     *
     * @param preferredWidth
     * The preferred width value, or <tt>-1</tt> to use the default
     * value determined by the skin.
     *
     * @param preferredHeight
     * The preferred height value, or <tt>-1</tt> to use the default
     * value determined by the skin.
     */
    public void setPreferredSize(int preferredWidth, int preferredHeight) {
        if (preferredWidth < -1) {
            throw new IllegalArgumentException(preferredWidth
                + " is not a valid value for preferredWidth.");
        }

        if (preferredHeight < -1) {
            throw new IllegalArgumentException(preferredHeight
                + " is not a valid value for preferredHeight.");
        }

        int previousPreferredWidth = this.preferredWidth;
        int previousPreferredHeight = this.preferredHeight;

        if (previousPreferredWidth != preferredWidth
            || previousPreferredHeight != preferredHeight) {
            this.preferredWidth = preferredWidth;
            this.preferredHeight = preferredHeight;

            invalidate();

            componentLayoutListeners.preferredSizeChanged(this,
                previousPreferredWidth, previousPreferredHeight);
        }
    }

    /**
     * Returns a flag indicating whether the preferred size was explicitly
     * set by the caller or is the default value determined by the skin.
     *
     * @return
     * <tt>true</tt> if the preferred size was explicitly set; <tt>false</tt>,
     * otherwise.
     */
    public boolean isPreferredSizeSet() {
        return isPreferredWidthSet()
            && isPreferredHeightSet();
    }

    /**
     * Returns the component's x-coordinate.
     *
     * @return
     * The component's horizontal position relative to the origin of the
     * parent container.
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the component's x-coordinate.
     *
     * @param x
     * The component's horizontal position relative to the origin of the
     * parent container.
     */
    public void setX(int x) {
        setLocation(x, getY());
    }

    /**
     * Returns the component's y-coordinate.
     *
     * @return
     * The component's vertical position relative to the origin of the
     * parent container.
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the component's y-coordinate.
     *
     * @param y
     * The component's vertical position relative to the origin of the
     * parent container.
     */
    public void setY(int y) {
        setLocation(getX(), y);
    }

    /**
     * Returns the component's location.
     *
     * @return
     * A point value containing the component's horizontal and vertical
     * position relative to the origin of the parent container.
     */
    public Point getLocation() {
        return new Point(getX(), getY());
    }

    /**
     * Sets the component's location.
     *
     * NOTE This method should only be called when performing layout.
     * However, since some containers do not reposition components during
     * layout, it is valid for callers to invoke this method directly when
     * such containers.
     *
     * @param x
     * The component's horizontal position relative to the origin of the
     * parent container.
     *
     * @param y
     * The component's vertical position relative to the origin of the
     * parent container.
     */
    public void setLocation(int x, int y) {
        int previousX = this.x;
        int previousY = this.y;

        if (previousX != x
            || previousY != y) {
            // Redraw the region formerly occupied by this component
            if (parent != null) {
                parent.repaint(getDecoratedBounds());
            }

            // Set the new coordinates
            this.x = x;
            this.y = y;

            // Redraw the region currently occupied by this component
            if (parent != null) {
                parent.repaint(getDecoratedBounds());
            }

            componentListeners.locationChanged(this, previousX, previousY);
        }
    }

    /**
     * Sets the component's location.
     *
     * @param location
     * A point value containing the component's horizontal and vertical
     * position relative to the origin of the parent container.
     *
     * @see #setLocation(int, int)
     */
    public final void setLocation(Point location) {
        if (location == null) {
            throw new IllegalArgumentException("location cannot be null.");
        }

        setLocation(location.x, location.y);
    }

    /**
     * Returns the component's bounding area.
     *
     * @return
     * The component's bounding area. The <tt>x</tt> and <tt>y</tt> values are
     * relative to the parent container.
     */
    public Bounds getBounds() {
        return new Bounds(x, y, getWidth(), getHeight());
    }

    /**
     * Returns the component's bounding area including decorators.
     *
     * @return
     * The decorated bounding area. The <tt>x</tt> and <tt>y</tt> values are
     * relative to the parent container.
     */
    public Bounds getDecoratedBounds() {
        Bounds decoratedBounds = new Bounds(0, 0, getWidth(), getHeight());

        for (Decorator decorator : decorators) {
            decoratedBounds = decoratedBounds.union(decorator.getBounds(this));
        }

        return new Bounds(decoratedBounds.x + x, decoratedBounds.y + y,
            decoratedBounds.width, decoratedBounds.height);
    }

    /**
     * Returns the component's visibility.
     *
     * @return
     * <tt>true</tt> if the component will be painted; <tt>false</tt>,
     * otherwise.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the component's visibility.
     * <p>
     * NOTE This method should only be called during layout. Callers should
     * use {@link #setDisplayable(boolean)}.
     *
     * @param visible
     * <tt>true</tt> if the component should be painted; <tt>false</tt>,
     * otherwise.
     */
    public void setVisible(boolean visible) {
        if (this.visible != visible) {
            // If this component is being hidden and has the focus, clear
            // the focus
            if (!visible) {
                if (isFocused()) {
                    clearFocus();
                }

                // Ensure that the mouse out event is processed
                if (mouseOver) {
                    mouseOut();
                }
            }

            // Redraw the region formerly occupied by this component
            if (parent != null) {
                parent.repaint(getDecoratedBounds());
            }

            this.visible = visible;

            // Redraw the region currently occupied by this component
            if (parent != null) {
                parent.repaint(getDecoratedBounds());
            }

            // Ensure the layout is valid
            if (visible
                && !valid) {
                validate();
            }

            componentListeners.visibleChanged(this);
        }
    }

    /**
     * Returns the component's displayability.
     *
     * NOTE Container skins should generally try to respect this flag when
     * laying out, as follows:
     * <ul>
     *   <li>
     *     When a component's displayable flag is <tt>true</tt>, the
     *     container skin should include the component in layout and set its
     *     visibility to <tt>true</tt>.
     *   </li>
     *   <li>
     *     When a component's displayable flag is <tt>false</tt>, the
     *     container skin should not include the component in layout and set
     *     its visibility to <tt>false</tt>.
     *   </li>
     * </ul>
     * However, depending on the nature of the skin, it may ignore this flag
     * and manage its components' visibilities internally.
     *
     * @return
     * <tt>true</tt> if the component will participate in layout;
     * <tt>false</tt>, otherwise.
     */
    public boolean isDisplayable() {
        return displayable;
    }

    /**
     * Sets the component's displayability.
     *
     * @param displayable
     * <tt>true</tt> if the component will participate in layout;
     * <tt>false</tt>, otherwise.
     */
    public void setDisplayable(boolean displayable) {
        if (this.displayable != displayable) {
            this.displayable = displayable;

            invalidate();

            componentLayoutListeners.displayableChanged(this);
        }
    }

    /**
     * Returns the component's decorator sequence.
     *
     * @return
     * The component's decorator sequence
     */
    public DecoratorSequence getDecorators() {
        return decoratorSequence;
    }

    /**
     * Maps a point in this component's coordinate system to the specified
     * ancestor's coordinate space.
     *
     * @param x
     * The x-coordinate in this component's coordinate space
     *
     * @param y
     * The y-coordinate in this component's coordinate space
     *
     * @return
     * A point containing the translated coordinates, or <tt>null</tt> if the
     * component is not a descendant of the specified ancestor.
     */
    public Point mapPointToAncestor(Container ancestor, int x, int y) {
        if (ancestor == null) {
            throw new IllegalArgumentException("ancestor is null");
        }

        Point coordinates = null;

        Component component = this;

        while (component != null
            && coordinates == null) {
            if (component == ancestor) {
                coordinates = new Point(x, y);
            } else {
                x += component.x;
                y += component.y;

                component = component.getParent();
            }
        }

        return coordinates;
    }

    /**
     * Maps a point in the specified ancestor's coordinate space to this
     * component's coordinate system.
     *
     * @param x
     * The x-coordinate in the ancestors's coordinate space.
     *
     * @param y
     * The y-coordinate in the ancestor's coordinate space.
     *
     * @return
     * A point containing the translated coordinates, or <tt>null</tt> if the
     * component is not a descendant of the specified ancestor.
     */
    public Point mapPointFromAncestor(Container ancestor, int x, int y) {
        if (ancestor == null) {
            throw new IllegalArgumentException("ancestor is null");
        }

        Point coordinates = null;

        Component component = this;

        while (component != null
            && coordinates == null) {
            if (component == ancestor) {
                coordinates = new Point(x, y);
            } else {
                x -= component.x;
                y -= component.y;

                component = component.getParent();
            }
        }

        return coordinates;
    }

    /**
     * Determines if this component is showing. To be showing, the component
     * and all of its ancestors must be visible, and the component's window
     * must be open.
     *
     * @return
     * <tt>true</tt> if this component is showing; <tt>false</tt> otherwise
     */
    public boolean isShowing() {
        boolean showing = true;

        Component component = this;
        while (component != null
            && showing) {
            Container parent = component.getParent();
            showing &= (component.isVisible()
                && (parent != null || component instanceof Display));

            component = parent;
        }

        return showing;
    }

    /**
     * Determines the visible area of a component. The visible area is defined
     * as the intersection of the component's area with the visible area of its
     * ancestors, or, in the case of a Viewport, the viewport bounds.
     *
     * @return
     * The visible area of the component in display coordinates, or
     * <tt>null</tt> if the component is either not showing or not part of the
     * component hierarchy.
     */
    public Bounds getVisibleArea() {
        return getVisibleArea(0, 0, getWidth(), getHeight());
    }

    /**
     * Determines the visible area of a component. The visible area is defined
     * as the intersection of the component's area with the visible area of its
     * ancestors, or, in the case of a Viewport, the viewport bounds.
     *
     * @param area
     *
     * @return
     * The visible area of the component in display coordinates, or
     * <tt>null</tt> if the component is either not showing or not part of the
     * component hierarchy.
     */
    public Bounds getVisibleArea(Bounds area) {
        if (area == null) {
            throw new IllegalArgumentException("area is null.");
        }

        return getVisibleArea(area.x, area.y, area.width, area.height);
    }

    /**
     * Determines the visible area of a component. The visible area is defined
     * as the intersection of the component's area with the visible area of its
     * ancestors, or, in the case of a Viewport, the viewport bounds.
     *
     * @param x
     * @param y
     * @param width
     * @param height
     *
     * @return
     * The visible area of the component in display coordinates, or
     * <tt>null</tt> if the component is either not showing or not part of the
     * component hierarchy.
     */
    public Bounds getVisibleArea(int x, int y, int width, int height) {
        Bounds visibleArea = null;

        Component component = this;

        int top = y;
        int left = x;
        int bottom = y + height - 1;
        int right = x + width - 1;

        while (component != null
            && component.isVisible()) {
            int minTop = 0;
            int minLeft = 0;
            int maxBottom = component.getHeight() - 1;
            int maxRight = component.getWidth() - 1;

            if (component instanceof Viewport) {
                Viewport viewport = (Viewport)component;
                Bounds bounds = viewport.getViewportBounds();
                minTop = bounds.y;
                minLeft = bounds.x;
                maxBottom = bounds.y + bounds.height - 1;
                maxRight = bounds.x + bounds.width - 1;
            }

            top = component.y + Math.max(top, minTop);
            left = component.x + Math.max(left, minLeft);
            bottom = component.y + Math.max(Math.min(bottom, maxBottom), -1);
            right = component.x + Math.max(Math.min(right, maxRight), -1);

            if (component instanceof Display) {
                visibleArea = new Bounds(left, top, right - left + 1, bottom - top + 1);
            }

            component = component.getParent();
        }

        return visibleArea;
    }

    /**
     * Ensures that the given area of a component is visible within the
     * viewports of all applicable ancestors.
     *
     * @param area
     */
    public void scrollAreaToVisible(Bounds area) {
        if (area == null) {
            throw new IllegalArgumentException("area is null.");
        }

        scrollAreaToVisible(area.x, area.y, area.width, area.height);
    }

    /**
     * Ensures that the given area of a component is visible within the
     * viewports of all applicable ancestors.
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void scrollAreaToVisible(int x, int y, int width, int height) {
        Component component = this;

        while (component != null) {
            if (component instanceof Viewport) {
                Viewport viewport = (Viewport)component;
                Component view = viewport.getView();

                try {
                    Bounds viewportBounds = viewport.getViewportBounds();

                    int deltaX = 0;

                    int leftDisplacement = x - viewportBounds.x;
                    int rightDisplacement = (x + width) -
                        (viewportBounds.x + viewportBounds.width);

                    if ((leftDisplacement & rightDisplacement) < 0) {
                        // Both leftDisplacement and rightDisplacement are
                        // negative; the area lies to the left of our viewport
                        // bounds
                        deltaX = Math.max(leftDisplacement, rightDisplacement);
                    } else if ((leftDisplacement | rightDisplacement) > 0) {
                        // Both leftDisplacement and rightDisplacement are
                        // positive; the area lies to the right of our viewport
                        // bounds
                        deltaX = Math.min(leftDisplacement, rightDisplacement);
                    }

                    if (deltaX != 0) {
                        int viewWidth = (view == null) ? 0 : view.getWidth();
                        int scrollLeft = viewport.getScrollLeft();
                        scrollLeft = Math.min(Math.max(scrollLeft + deltaX, 0),
                            Math.max(viewWidth - viewportBounds.width, 0));
                        viewport.setScrollLeft(scrollLeft);

                        x -= deltaX;
                    }

                    x = Math.max(x, viewportBounds.x);
                    width = Math.min(width,
                        Math.max(viewportBounds.width - (x - viewportBounds.x), 0));

                    int deltaY = 0;

                    int topDisplacement = y - viewportBounds.y;
                    int bottomDisplacement = (y + height) -
                        (viewportBounds.y + viewportBounds.height);

                    if ((topDisplacement & bottomDisplacement) < 0) {
                        // Both topDisplacement and bottomDisplacement are
                        // negative; the area lies above our viewport bounds
                        deltaY = Math.max(topDisplacement, bottomDisplacement);
                    } else if ((topDisplacement | bottomDisplacement) > 0) {
                        // Both topDisplacement and bottomDisplacement are
                        // positive; the area lies below our viewport bounds
                        deltaY = Math.min(topDisplacement, bottomDisplacement);
                    }

                    if (deltaY != 0) {
                        int viewHeight = (view == null) ? 0 : view.getHeight();
                        int scrollTop = viewport.getScrollTop();
                        scrollTop = Math.min(Math.max(scrollTop + deltaY, 0),
                            Math.max(viewHeight - viewportBounds.height, 0));
                        viewport.setScrollTop(scrollTop);

                        y -= deltaY;
                    }

                    y = Math.max(y, viewportBounds.y);
                    height = Math.min(height,
                        Math.max(viewportBounds.height - (y - viewportBounds.y), 0));
                } catch (UnsupportedOperationException ex) {
                    // If the viewport doesn't support getting the viewport
                    // bounds, we simply act as we would have had the viewport
                    // been any other type of component; namely, we do nothing
                    // and proceed to its parent
                }
            }

            x += component.x;
            y += component.y;

            component = component.getParent();
        }
    }

    /**
     * Returns the component's valid state.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Flags the component's hierarchy as invalid, and clears any cached
     * preferred size.
     */
    public void invalidate() {
        valid = false;

        // Clear the preferred size
        preferredSize = null;

        if (parent != null) {
            parent.invalidate();
        }
    }

    /**
     * Lays out the component by calling {@link Skin#layout()}.
     */
    public void validate() {
        if (!valid
            && visible) {
            skin.layout();
            valid = true;
        }
    }

    /**
     * Flags the entire component as needing to be repainted.
     */
    public final void repaint() {
        repaint(false);
    }

    /**
     * Flags the entire component as needing to be repainted.
     *
     * @param immediate
     */
    public final void repaint(boolean immediate) {
        repaint(0, 0, getWidth(), getHeight(), immediate);
    }

    /**
     * Flags an area as needing to be repainted.
     *
     * @param area
     */
    public final void repaint(Bounds area) {
        repaint(area, false);
    }

    /**
     * Flags an area as needing to be repainted or repaints the rectangle
     * immediately.
     *
     * @param area
     * @param immediate
     */
    public final void repaint(Bounds area, boolean immediate) {
        if (area == null) {
            throw new IllegalArgumentException("area is null.");
        }

        repaint(area.x, area.y, area.width, area.height, immediate);
    }

    /**
     * Flags an area as needing to be repainted.
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public final void repaint(int x, int y, int width, int height) {
        repaint(x, y, width, height, false);
    }

    /**
     * Flags an area as needing to be repainted.
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param immediate
     */
    public void repaint(int x, int y, int width, int height, boolean immediate) {
        if (parent != null) {
            // Constrain the repaint area to this component's bounds
            int top = y;
            int left = x;
            int bottom = top + height - 1;
            int right = left + width - 1;

            x = Math.max(left, 0);
            y = Math.max(top, 0);
            width = Math.min(right, getWidth() - 1) - x + 1;
            height = Math.min(bottom, getHeight() - 1) - y + 1;

            if (width > 0
                && height > 0) {
                // Notify the parent that the region needs updating
                parent.repaint(x + this.x, y + this.y, width, height, immediate);

                // Repaint any affected decorators
                for (Decorator decorator : decorators) {
                    AffineTransform transform = decorator.getTransform(this);

                    if (!transform.isIdentity()) {
                        // Apply the decorator's transform to the repaint area
                        Rectangle area = new Rectangle(x, y, width, height);
                        Shape transformedShape = transform.createTransformedShape(area);
                        Bounds tranformedBounds = new Bounds(transformedShape.getBounds());

                        // Limit the transformed area to the decorator's bounds
                        tranformedBounds = tranformedBounds.intersect(decorator.getBounds(this));

                        // Add the bounded area to the repaint region
                        parent.repaint(tranformedBounds.x + this.x, tranformedBounds.y + this.y,
                            tranformedBounds.width, tranformedBounds.height, immediate);
                    }
                }
            }
        }
    }

    /**
     * Paints the component. Delegates to the skin.
     */
    public void paint(Graphics2D graphics) {
        skin.paint(graphics);
    }

    /**
     * Creates a graphics context for this component. This graphics context
     * will not be double buffered. In other words, drawing operations on it
     * will operate directly on the video RAM.
     *
     * @return
     * A graphics context for this component, or <tt>null</tt> if this
     * component is not showing.
     *
     * @see #isShowing()
     */
    public Graphics2D getGraphics() {
        Graphics2D graphics = null;

        int x = 0;
        int y = 0;

        Component component = this;

        while (component != null
            && component.isVisible()
            && !(component instanceof Display)) {
            x += component.x;
            y += component.y;

            component = component.getParent();
        }

        if (component != null
            && component.isVisible()) {
            Display display = (Display)component;
            graphics = (Graphics2D)display.getDisplayHost().getGraphics();

            double scale = display.getDisplayHost().getScale();
            if (scale != 1) {
                graphics.scale(scale, scale);
            }

            graphics.translate(x, y);
            graphics.clipRect(0, 0, getWidth(), getHeight());
        }

        return graphics;
    }

    /**
     * Returns the component's enabled state.
     *
     * @return
     * <tt>true</tt> if the component is enabled; <tt>false</tt>, otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the component's enabled state. Enabled components respond to user
     * input events; disabled components do not.
     *
     * @param enabled
     * <tt>true</tt> if the component is enabled; <tt>false</tt>, otherwise.
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            if (!enabled) {
                // If this component has the focus, clear it
                if (isFocused()) {
                    clearFocus();
                }

                // Ensure that the mouse out event is processed
                if (mouseOver) {
                    mouseOut();
                }
            }

            this.enabled = enabled;

            componentStateListeners.enabledChanged(this);
        }
    }

    /**
     * Determines if this component is blocked. A component is blocked if the
     * component or any of its ancestors is disabled.
     *
     * @return
     * <tt>true</tt> if the component is blocked; <tt>false</tt>, otherwise.
     */
    public boolean isBlocked() {
        boolean blocked = false;

        Component component = this;

        while (component != null
            && !blocked) {
            blocked = !component.isEnabled();
            component = component.getParent();
        }

        return blocked;
    }

    /**
     * Determines if the mouse is positioned over this component.
     *
     * @return
     * <tt>true</tt> if the mouse is currently located over this component;
     * <tt>false</tt>, otherwise.
     */
    public boolean isMouseOver() {
        return mouseOver;
    }

    /**
     * Returns the cursor that is displayed when the mouse pointer is over
     * this component.
     *
     * @return
     * The cursor that is displayed over the component.
     */
    public Cursor getCursor() {
        return cursor;
    }

    /**
     * Sets the cursor that is displayed when the mouse pointer is over
     * this component.
     *
     * @param cursor
     * The cursor to display over the component, or <tt>null</tt> to inherit
     * the cursor of the parent container.
     */
    public void setCursor(Cursor cursor) {
        Cursor previousCursor = this.cursor;

        if (previousCursor != cursor) {
            this.cursor = cursor;

            if (mouseOver) {
                Mouse.setCursor(this);
            }

            componentListeners.cursorChanged(this, previousCursor);
        }
    }

    public final void setCursor(String cursor) {
        setCursor((cursor == null) ? null : Cursor.decode(cursor));
    }

    /**
     * Returns the component's tooltip text.
     *
     * @return
     * The component's tooltip text, or <tt>null</tt> if no tooltip is
     * specified.
     */
    public String getTooltipText() {
        return tooltipText;
    }

    /**
     * Sets the component's tooltip text.
     *
     * @param tooltipText
     * The component's tooltip text, or <tt>null</tt> for no tooltip.
     */
    public void setTooltipText(String tooltipText) {
        String previousTooltipText = this.tooltipText;

        if (previousTooltipText != tooltipText) {
            this.tooltipText = tooltipText;
            componentListeners.tooltipTextChanged(this, previousTooltipText);
        }
    }

    /**
     * Tells whether or not this component is fully opaque when painted.
     *
     * @return
     * <tt>true</tt> if this component is opaque; </tt>false</tt> if any part
     * of it is transparent or translucent.
     */
    public boolean isOpaque() {
        return skin.isOpaque();
    }

    /**
     * Returns this component's focusability. A focusable component is capable
     * of receiving the focus.
     *
     * @return
     * <tt>true</tt> if the component is enabled and visible.
     */
    public boolean isFocusable() {
        return skin.isFocusable();
    }

    /**
     * Returns the component's focused state.
     *
     * @return
     * <tt>true</tt> if the component has the input focus; <tt>false</tt>;
     * otherwise.
     */
    public boolean isFocused() {
        return focusedComponent == this;
    }

    /**
     * Called to notify a component that its focus state has changed.
     *
     * @param focused
     * <tt>true</tt> if the component has received the input focus;
     * <tt>false</tt> if the component has lost the focus.
     *
     * @param temporary
     * <tt>true</tt> if this focus change is temporary; <tt>false</tt>,
     * otherwise.
     */
    protected void setFocused(boolean focused, boolean temporary) {
        componentStateListeners.focusedChanged(this, temporary);
    }

    /**
     * Requests that focus be given to this component.
     */
    public void requestFocus() {
        requestFocus(false);
    }

    /**
     * Requests that focus be given to this component.
     *
     * @param temporary
     * If <tt>true</tt>, indicates that focus is being restored from a
     * temporary loss.
     */
    protected void requestFocus(boolean temporary) {
        if (!isFocusable()) {
            throw new IllegalArgumentException("Component is not focusable.");
        }

        if (!isShowing()) {
            throw new IllegalArgumentException("Component is not showing.");
        }

        if (isBlocked()) {
            throw new IllegalArgumentException("Component is blocked.");
        }

        setFocusedComponent(this, temporary);
    }

    /**
     * Transfers focus to the next focusable component.
     *
     * @param direction
     * The direction in which to transfer focus.
     */
    public void transferFocus(Direction direction) {
        Component component = this;

        // Loop until we either find a component that is capable of receiving
        // the focus or we run out of components
        do {
            // Attempt to traverse the current component's parent
            Container container = component.getParent();
            FocusTraversalPolicy focusTraversalPolicy = container.getFocusTraversalPolicy();

            if (focusTraversalPolicy == null) {
                // This container has no traversal policy; move up a level
                component = container;
            } else {
                // Get the next component in the traversal
                component = focusTraversalPolicy.getNextComponent(container, component, direction);

                // If the next component is a container, attempt to traverse
                // down into it
                while (component instanceof Container) {
                    container = (Container)component;
                    component = null;

                    focusTraversalPolicy = container.getFocusTraversalPolicy();

                    if (focusTraversalPolicy != null) {
                        component = focusTraversalPolicy.getNextComponent(container, component, direction);
                    }
                }

                if (component == null) {
                    // We are at the end of the traversal; move up a level
                    component = container;
                }
            }
        } while (component != null
            && !(component.isFocusable()
                && !component.isBlocked()
                && component.isShowing()));

        // Focus the component (which may be null)
        setFocusedComponent(component, false);
    }

    /**
     * Returns the currently focused component.
     *
     * @return
     * The component that currently has the focus, or <tt>null</tt> if no
     * component is focused.
     */
    public static Component getFocusedComponent() {
        return focusedComponent;
    }

    /**
     * Sets the focused component.
     *
     * @param focusedComponent
     * The component to focus, or <tt>null</tt> to clear the focus.
     *
     * @param temporary
     * <tt>true</tt> if this focus change is or was temporary; <tt>false</tt>,
     * if it is permanent.
     */
    private static void setFocusedComponent(Component focusedComponent, boolean temporary) {
        Component previousFocusedComponent = Component.focusedComponent;

        if (previousFocusedComponent != focusedComponent) {
            // Set the focused component
            Component.focusedComponent = focusedComponent;

            // Notify the components of the state change
            if (previousFocusedComponent != null) {
                previousFocusedComponent.setFocused(false, temporary);
            }

            if (focusedComponent == null) {
                if (previousFocusedComponent != null
                    && !temporary) {
                    previousFocusedComponent.getWindow().setActiveDescendant(null);
                }
            } else {
                focusedComponent.setFocused(true, temporary);
                focusedComponent.getWindow().setActiveDescendant(focusedComponent);
            }

            componentClassListeners.focusedComponentChanged(previousFocusedComponent);
        }
    }

    /**
     * Clears the focus.
     */
    public static void clearFocus() {
        clearFocus(false);
    }

    /**
     * Returns the component dictionary.
     */
    public static ComponentDictionary getComponents() {
        return componentDictionary;
    }

    /**
     * Clears the focus.
     *
     * @param temporary
     * If <tt>true</tt>, the focus is being cleared temporarily.
     */
    protected static void clearFocus(boolean temporary) {
        setFocusedComponent(null, temporary);
    }

    /**
     * Copies bound values from the bind context to the component. This
     * functionality must be provided by the subclass; the base implementation
     * is a no-op.
     *
     * @param context
     */
    public void load(Dictionary<String, ?> context) {
    }

    /**
     * Copies bound values from the bind context to the component by converting
     * the given context to a bean dictionary.
     *
     * @param context
     */
    public final void load(Object context) {
        load(new BeanDictionary(context));
    }

    /**
     * Copies bound values from the component to the bind context. This
     * functionality must be provided by the subclass; the base implementation
     * is a no-op.
     *
     * @param context
     */
    public void store(Dictionary<String, ?> context) {
    }

    /**
     * Copies bound values from the component to the bind context by converting
     * the given context to a bean dictionary.
     *
     * @param context
     */
    public final void store(Object context) {
        store(new BeanDictionary(context));
    }

    public DragSource getDragSource() {
        return dragSource;
    }

    public void setDragSource(DragSource dragSource) {
        DragSource previousDragSource = this.dragSource;

        if (previousDragSource != dragSource) {
            this.dragSource = dragSource;
            componentDragDropListeners.dragSourceChanged(this, previousDragSource);
        }
    }

    public DropTarget getDropTarget() {
        return dropTarget;
    }

    public void setDropTarget(DropTarget dropTarget) {
        DropTarget previousDropTarget = this.dropTarget;

        if (previousDropTarget != dropTarget) {
            this.dropTarget = dropTarget;
            componentDragDropListeners.dropTargetChanged(this, previousDropTarget);
        }
    }

    /**
     * Returns the user data dictionary.
     */
    public UserDataDictionary getUserData() {
        return userDataDictionary;
    }

    /**
     * Returns a dictionary instance representing the component's style
     * properties. This is effectively a pass-through to the skin's dictionary
     * implementation. It allows callers to modify the properties of the skin
     * without directly obtaining a reference to the skin.
     */
    public final StyleDictionary getStyles() {
        return styleDictionary;
    }

    /**
     * Applies a set of styles.
     *
     * @param styles
     */
    public void setStyles(Map<String, ?> styles) {
        if (styles == null) {
            throw new IllegalArgumentException("styles is null.");
        }

        for (String key : styles) {
            getStyles().put(key, styles.get(key));
        }
    }

    /**
     * Applies a set of styles.
     *
     * @param styles
     * The location of the styles to apply. If the styles have been previously
     * applied, they will be retrieved from the resource cache in the
     * application context. Otherwise, they will be loaded from the given
     * location and added to the cache before being applied.
     */
    @SuppressWarnings("unchecked")
    public void setStyles(URL styles) throws IOException, SerializationException {
        if (styles == null) {
            throw new IllegalArgumentException("styles is null.");
        }

        Map<String, Object> cachedStyles =
            (Map<String, Object>)ApplicationContext.getResourceCache().get(styles);

        if (cachedStyles == null) {
            JSONSerializer jsonSerializer = new JSONSerializer();
            cachedStyles =
                (Map<String, Object>)jsonSerializer.readObject(styles.openStream());

            ApplicationContext.getResourceCache().put(styles, cachedStyles);
        }

        setStyles(cachedStyles);
    }

    /**
     * Applies a set of styles encoded as a JSON string.
     *
     * @param styles
     */
    public void setStyles(String styles) {
        if (styles == null) {
            throw new IllegalArgumentException("styles is null.");
        }

        setStyles(JSONSerializer.parseMap(styles));
    }

    /**
     * Returns the currently installed attributes.
     *
     * @return
     * This component's attributes
     */
    public Attributes getAttributes() {
        return attributes;
    }

    /**
     * Sets the attributes.
     *
     * @param attributes
     */
    protected void setAttributes(Attributes attributes) {
        if (this.attributes != null) {
            this.attributes.setComponent(null);
        }

        this.attributes = attributes;

        if (this.attributes != null) {
            this.attributes.setComponent(this);
        }
    }

    protected boolean mouseMove(int x, int y) {
        boolean consumed = false;

        if (enabled) {
            consumed = componentMouseListeners.mouseMove(this, x, y);
        }

        return consumed;
    }

    protected void mouseOver() {
        if (enabled) {
            mouseOver = true;
            componentMouseListeners.mouseOver(this);
        }
    }

    protected void mouseOut() {
        if (enabled) {
            mouseOver = false;
            componentMouseListeners.mouseOut(this);
        }
    }

    protected boolean mouseDown(Mouse.Button button, int x, int y) {
        boolean consumed = false;

        if (enabled) {
            consumed = componentMouseButtonListeners.mouseDown(this, button, x, y);
        }

        return consumed;
    }

    protected boolean mouseUp(Mouse.Button button, int x, int y) {
        boolean consumed = false;

        if (enabled) {
            consumed = componentMouseButtonListeners.mouseUp(this, button, x, y);
        }

        return consumed;
    }

    protected boolean mouseClick(Mouse.Button button, int x, int y, int count) {
        boolean consumed = false;

        if (enabled) {
            consumed = componentMouseButtonListeners.mouseClick(this, button, x, y, count);
        }

        return consumed;
    }

    protected boolean mouseWheel(Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        boolean consumed = false;

        if (enabled) {
            consumed = componentMouseWheelListeners.mouseWheel(this, scrollType,
                scrollAmount, wheelRotation, x, y);
        }

        return consumed;
    }

    protected boolean keyTyped(char character) {
        boolean consumed = false;

        if (enabled) {
            consumed = componentKeyListeners.keyTyped(this, character);

            if (!consumed && parent != null) {
                consumed = parent.keyTyped(character);
            }
        }

        return consumed;
    }

    protected boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (enabled) {
            consumed = componentKeyListeners.keyPressed(this, keyCode, keyLocation);

            if (!consumed && parent != null) {
                consumed = parent.keyPressed(keyCode, keyLocation);
            }
        }

        return consumed;
    }

    protected boolean keyReleased(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (enabled) {
            consumed = componentKeyListeners.keyReleased(this, keyCode, keyLocation);

            if (!consumed && parent != null) {
                consumed = parent.keyReleased(keyCode, keyLocation);
            }
        }

        return consumed;
    }

    @Override
    public String toString() {
        String s = this.getClass().getName() + "#" + getHandle();
        return s;
    }

    public ListenerList<ComponentListener> getComponentListeners() {
        return componentListeners;
    }

    public ListenerList<ComponentLayoutListener> getComponentLayoutListeners() {
        return componentLayoutListeners;
    }

    public ListenerList<ComponentStateListener> getComponentStateListeners() {
        return componentStateListeners;
    }

    public ListenerList<ComponentDecoratorListener> getComponentDecoratorListeners() {
        return componentDecoratorListeners;
    }

    public ListenerList<ComponentMouseListener> getComponentMouseListeners() {
        return componentMouseListeners;
    }

    public ListenerList<ComponentMouseButtonListener> getComponentMouseButtonListeners() {
        return componentMouseButtonListeners;
    }

    public ListenerList<ComponentMouseWheelListener> getComponentMouseWheelListeners() {
        return componentMouseWheelListeners;
    }

    public ListenerList<ComponentKeyListener> getComponentKeyListeners() {
        return componentKeyListeners;
    }

    public ListenerList<ComponentDragDropListener> getComponentDragDropListeners() {
        return componentDragDropListeners;
    }

    public ListenerList<ComponentDataListener> getComponentDataListeners() {
        return componentDataListeners;
    }

    public static ListenerList<ComponentClassListener> getComponentClassListeners() {
        return componentClassListeners;
    }
}
