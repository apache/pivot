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

import java.awt.Graphics2D;
import java.util.Iterator;

import pivot.beans.BeanDictionary;
import pivot.beans.PropertyNotFoundException;
import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.Map;
import pivot.collections.Sequence;
import pivot.serialization.JSONSerializer;
import pivot.util.ListenerList;
import pivot.wtk.Mouse.Button;
import pivot.wtk.Mouse.ScrollType;

/**
 * Top level abstract base class for all components. In MVC terminology, a
 * component represents the "controller". It has no inherent visual
 * representation and acts as an intermediary between the component's data (the
 * "model") and the skin, an implementation of <tt>pivot.wtk.Skin</tt> which
 * serves as the "view".
 *
 * Components may have multiple skins, including skins packaged as part of a
 * "theme" as well as custom skins defined by a caller. Each skin represents a
 * different way to visualize the state of the model data defined by the
 * component.
 *
 * TODO Add a getShape() method that will support non-rectangular components.
 * DisplaySkin can use this to paint an appropriate drop shadow, and Component
 * can use it to perform hit testing for mouse events and cursor display.
 */
public abstract class Component implements Visual {
    protected static abstract class Attributes {
        private Component component = null;

        public Component getComponent() {
            return component;
        }

        private void setComponent(Component component) {
            this.component = component;
        }
    }

    /**
     * Style dictionary implementation.
     *
     * @author gbrown
     */
    public class StyleDictionary extends BeanDictionary {
        public StyleDictionary(Skin skin) {
            super(skin);
        }

        public Object put(String key, Object value) {
            Object previousValue = null;

            try {
                previousValue = super.put(key, value);
                componentListeners.styleUpdated(Component.this, key, previousValue);
            } catch(PropertyNotFoundException exception) {
                System.out.println("\"" + key + "\" is not a valid style for "
                    + Component.this);
            }

            return previousValue;
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
        private class DecoratorIterator implements Iterator<Decorator> {
            Iterator<Decorator> source = null;

            public DecoratorIterator(Iterator<Decorator> source) {
                this.source = source;
            }

            public boolean hasNext() {
                return source.hasNext();
            }

            public Decorator next() {
                return source.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        public int add(Decorator decorator) {
            int i = getLength();
            insert(decorator, i);

            return i;
        }

        public void insert(Decorator decorator, int index) {
            if (decorator == null) {
                throw new IllegalArgumentException("decorator is null");
            }

            decorators.insert(decorator, index);
            decorator.install(Component.this);

            repaint();

            componentDecoratorListeners.decoratorInserted(Component.this, index);
        }

        public Decorator update(int index, Decorator decorator) {
            if (decorator == null) {
                throw new IllegalArgumentException("decorator is null.");
            }

            Decorator previousDecorator = decorators.update(index, decorator);
            previousDecorator.uninstall();
            decorator.install(Component.this);

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
            Sequence<Decorator> removed = decorators.remove(index, count);

            for (int i = 0, n = removed.getLength(); i < n; i++) {
                Decorator decorator = removed.get(i);
                decorator.uninstall();
            }

            if (count > 0) {
                repaint();
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
            return new DecoratorIterator(decorators.iterator());
        }
    }

    /**
     * Holds cached preferred size constraint/value pairs.
     *
     * @author tvolkert
     */
    private class PreferredSizeCache {
        public int constraint;
        public int value;

        public PreferredSizeCache(int constraint, int value) {
            this.constraint = constraint;
            this.value = value;
        }
    }

    /**
     * Component listener list.
     *
     * @author gbrown
     */
    private class ComponentListenerList extends ListenerList<ComponentListener>
        implements ComponentListener {
        public void skinClassChanged(Component component,
            Class<? extends Skin> previousSkinClass) {
            for (ComponentListener listener : this) {
                listener.skinClassChanged(component, previousSkinClass);
            }
        }

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

    private class ComponentLayoutListenerList extends
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

    private class ComponentStateListenerList extends
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

    private class ComponentDecoratorListenerList extends
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

    private class ComponentMouseListenerList extends ListenerList<ComponentMouseListener>
        implements ComponentMouseListener {
        public void mouseMove(Component component, int x, int y) {
            for (ComponentMouseListener listener : this) {
                listener.mouseMove(component, x, y);
            }
        }

        public void mouseOut(Component component) {
            for (ComponentMouseListener listener : this) {
                listener.mouseOut(component);
            }
        }

        public void mouseOver(Component component) {
            for (ComponentMouseListener listener : this) {
                listener.mouseOver(component);
            }
        }
    }

    private class ComponentMouseButtonListenerList
        extends ListenerList<ComponentMouseButtonListener>
        implements ComponentMouseButtonListener {
        public void mouseDown(Component component, Button button, int x, int y) {
            for (ComponentMouseButtonListener listener : this) {
                listener.mouseDown(component, button, x, y);
            }
        }

        public void mouseUp(Component component, Button button, int x, int y) {
            for (ComponentMouseButtonListener listener : this) {
                listener.mouseUp(component, button, x, y);
            }
        }

        public void mouseClick(Component component, Button button, int x, int y, int count) {
            for (ComponentMouseButtonListener listener : this) {
                listener.mouseClick(component, button, x, y, count);
            }
        }
    }

    private class ComponentMouseWheelListenerList
        extends ListenerList<ComponentMouseWheelListener>
        implements ComponentMouseWheelListener {
        public void mouseWheel(Component component, ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            for (ComponentMouseWheelListener listener : this) {
                listener.mouseWheel(component, scrollType, scrollAmount, wheelRotation, x, y);
            }
        }
    }

    private class ComponentKeyListenerList extends ListenerList<ComponentKeyListener>
        implements ComponentKeyListener {
        public void keyTyped(Component component, char character) {
            for (ComponentKeyListener listener : this) {
                listener.keyTyped(component, character);
            }
        }

        public void keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            for (ComponentKeyListener listener : this) {
                listener.keyPressed(component, keyCode, keyLocation);
            }
        }

        public void keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            for (ComponentKeyListener listener : this) {
                listener.keyReleased(component, keyCode, keyLocation);
            }
        }
    }

    private class ComponentDataListenerList extends
        ListenerList<ComponentDataListener> implements ComponentDataListener {
        public void userDataChanged(Component component, Object previousValue) {
            for (ComponentDataListener listener : this) {
                listener.userDataChanged(component, previousValue);
            }
        }
    }

    private class ComponentDragDropListenerList extends
        ListenerList<ComponentDragDropListener> implements ComponentDragDropListener {
        public void dragHandlerChanged(Component component, DragHandler previousDragHandler) {
            for (ComponentDragDropListener listener : this) {
                listener.dragHandlerChanged(component, previousDragHandler);
            }
        }

        public void dropHandlerChanged(Component component, DropHandler previousDropHandler) {
            for (ComponentDragDropListener listener : this) {
                listener.dropHandlerChanged(component, previousDropHandler);
            }
        }
    }

    /**
     * Component class listener list.
     *
     * @author tvolkert
     */
    private static class ComponentClassListenerList
        extends ListenerList<ComponentClassListener>
        implements ComponentClassListener {

        public void focusedComponentChanged(Component previousFocusedComponent) {
            for (ComponentClassListener listener : this) {
                listener.focusedComponentChanged(previousFocusedComponent);
            }
        }
    }

    private final int handle = nextHandle++;

    /**
     * The currently installed skin, or null if no skin is installed.
     */
    private Skin skin = null;

    /**
     * The component's preferred width, height, and cache.
     */
    private int preferredWidth = -1;
    private int preferredHeight = -1;

    private PreferredSizeCache preferredWidthCache = null;
    private PreferredSizeCache preferredHeightCache = null;

    /**
     * The component's parent container, or null if the component does not have
     * a parent.
     */
    private Container parent = null;

    /**
     * The component's location. These coordinates are relative to the origin of
     * the component's parent.
     */
    private int x = 0;
    private int y = 0;

    /**
     * The component's visible flag.
     */
    private boolean visible = true;

    /**
     * The component's displayable flag.
     */
    private boolean displayable = true;

    /**
     * The component's decorators.
     */
    private ArrayList<Decorator> decorators = new ArrayList<Decorator>();
    private DecoratorSequence decoratorSequence = new DecoratorSequence();

    /**
     * The component's enabled flag.
     */
    private boolean enabled = true;

    /**
     * The component's mouse-over flag.
     */
    private boolean mouseOver = false;

    /**
     * The cursor that is displayed over the component.
     */
    private Cursor cursor = Cursor.DEFAULT;

    /**
     * The tooltip text.
     */
    private String tooltipText = null;

    /**
     * User data.
     */
    private Object userData = null;

    /**
     * Drag handler.
     */
    private DragHandler dragHandler = null;

    /**
     * Drop handler.
     */
    private DropHandler dropHandler = null;

    /**
     * Proxy class for getting/setting style properties on the skin.
     */
    private StyleDictionary styleDictionary = null;

    /**
     * Attached properties.
     */
    private Attributes attributes = null;

    /**
     * Event listener lists.
     */
    private ComponentListenerList componentListeners = new ComponentListenerList();
    private ComponentLayoutListenerList componentLayoutListeners = new ComponentLayoutListenerList();
    private ComponentStateListenerList componentStateListeners = new ComponentStateListenerList();
    private ComponentDecoratorListenerList componentDecoratorListeners = new ComponentDecoratorListenerList();
    private ComponentMouseListenerList componentMouseListeners = new ComponentMouseListenerList();
    private ComponentMouseButtonListenerList componentMouseButtonListeners = new ComponentMouseButtonListenerList();
    private ComponentMouseWheelListenerList componentMouseWheelListeners = new ComponentMouseWheelListenerList();
    private ComponentKeyListenerList componentKeyListeners = new ComponentKeyListenerList();
    private ComponentDataListenerList componentDataListeners = new ComponentDataListenerList();
    private ComponentDragDropListenerList componentDragDropListeners = new ComponentDragDropListenerList();

    private static ComponentClassListenerList componentClassListeners = new ComponentClassListenerList();

    /**
     * The component that currently has the focus.
     */
    private static Component focusedComponent = null;

    private static int nextHandle = 0;

    public int getHandle() {
        return handle;
    }

    /**
     * Returns the type of the skin that is currently associated with this
     * component.
     */
    public Class<? extends Skin> getSkinClass() {
        Class<? extends Skin> skinClass = null;
        if (skin != null) {
            skinClass = skin.getClass();
        }

        return skinClass;
    }

    /**
     * Installs a skin on this component, removing any skin that was previously
     * attached and redrawing the component.
     *
     * @param skinClass
     * The type of the skin to install.
     *
     * @throws IllegalArgumentException
     * If the skin cannot be installed on this component.
     */
    public void setSkinClass(Class<? extends Skin> skinClass) {
        if (skinClass == null) {
            throw new IllegalArgumentException("skinClass is null.");
        }

        Class<? extends Skin> previousSkinClass = null;

        if (skin != null) {
            previousSkinClass = skin.getClass();
            skin.uninstall();
        }

        styleDictionary = null;
        skin = null;

        try {
            skin = skinClass.newInstance();
        } catch(Exception exception) {
            throw new IllegalArgumentException("The skin could not be installed.", exception);
        }

        styleDictionary = new StyleDictionary(skin);
        skin.install(this);

        invalidate();
        repaint();

        componentListeners.skinClassChanged(this, previousSkinClass);
    }

    protected Skin getSkin() {
        return skin;
    }

    /**
     * Installs the skin for the given component class, unless a subclass has
     * defined a more specific skin. Any component that defines a custom skin
     * class must call this method.
     *
     * @param componentClass
     */
    @SuppressWarnings("unchecked")
    protected void installSkin(Class<? extends Component> componentClass) {
        assert (skin == null) : "Skin is already installed.";

        // Walk the class hierarchy of this component's type to find a match
        Theme theme = Theme.getTheme();

        Class<?> superClass = getClass();
        Class<? extends Skin> skinClass = null;

        while (superClass != componentClass
            && superClass != Component.class
            && skinClass == null) {
            skinClass = theme.getSkinClass((Class<? extends Component>)superClass);

            if (skinClass == null) {
                superClass = superClass.getSuperclass();
            }
        }

        assert (superClass != Component.class) : componentClass.getName()
            + " is not an ancestor of " + getClass().getName();

        if (superClass == componentClass) {
            skinClass = theme.getSkinClass(componentClass);
            assert (skinClass != null) :
                "No skin mapping specified for " + componentClass.getName();

            setSkinClass(skinClass);
        }
    }

    public Container getParent() {
        return parent;
    }

    protected void setParent(Container parent) {
        if (parent != null
            && skin == null) {
            throw new IllegalStateException(this + " has no skin.");
        }

        // If this component is being removed from the component hierarchy
        // and is currently focused, clear the focus
        if (parent == null
            && isFocused()) {
            setFocusedComponent(null);
        }

        Container previousParent = this.parent;
        this.parent = parent;

        componentListeners.parentChanged(this, previousParent);
    }

    public Window getWindow() {
        Component component = this;

        while ((component != null) && !(component instanceof Window)) {
            component = component.getParent();
        }

        return (Window) component;
    }

    public int getWidth() {
        return (skin == null) ? 0 : skin.getWidth();
    }

    public int getHeight() {
        return (skin == null) ? 0 : skin.getHeight();
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

        if (skin != null) {
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
                repaint();

                // Set the size of the skin
                skin.setSize(width, height);

                // Redraw the region currently occupied by this component
                repaint();

                componentListeners.sizeChanged(this, previousWidth, previousHeight);
            }
        }
    }

    public int getPreferredWidth() {
        return getPreferredWidth(-1);
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = this.preferredWidth;

        if (preferredWidth == -1
            && skin != null) {
            if (height == -1) {
                height = preferredHeight;
            }

            if (preferredWidthCache != null
                && preferredWidthCache.constraint == height) {
                preferredWidth = preferredWidthCache.value;
            } else {
                preferredWidth = skin.getPreferredWidth(height);

                if (isValid()) {
                    // Update the cache
                    if (preferredWidthCache == null) {
                        preferredWidthCache = new PreferredSizeCache(height,
                            preferredWidth);
                    } else {
                        preferredWidthCache.constraint = height;
                        preferredWidthCache.value = preferredWidth;
                    }
                }
            }
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

    public int getPreferredHeight() {
        return getPreferredHeight(-1);
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = this.preferredHeight;

        if (preferredHeight == -1
            && skin != null) {
            if (width == -1) {
                width = preferredWidth;
            }

            if (preferredHeightCache != null
                && preferredHeightCache.constraint == width) {
                preferredHeight = preferredHeightCache.value;
            } else {
                preferredHeight = skin.getPreferredHeight(width);

                if (isValid()) {
                    // Update the cache
                    if (preferredHeightCache == null) {
                        preferredHeightCache = new PreferredSizeCache(width,
                            preferredHeight);
                    } else {
                        preferredHeightCache.constraint = width;
                        preferredHeightCache.value = preferredHeight;
                    }
                }
            }
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
        Dimensions preferredSize = null;

        if (isPreferredWidthSet()
            && isPreferredHeightSet()) {
            preferredSize = new Dimensions(preferredWidth, preferredHeight);
        } else if (isPreferredWidthSet()) {
            if (skin != null) {
                int preferredHeight;

                if (preferredHeightCache != null
                    && preferredHeightCache.constraint == preferredWidth) {
                    preferredHeight = preferredHeightCache.value;
                } else {
                    preferredHeight = skin.getPreferredHeight(preferredWidth);

                    if (isValid()) {
                        // Update the cache
                        if (preferredHeightCache == null) {
                            preferredHeightCache = new PreferredSizeCache(preferredWidth,
                                preferredHeight);
                        } else {
                            preferredHeightCache.constraint = preferredWidth;
                            preferredHeightCache.value = preferredHeight;
                        }
                    }
                }

                preferredSize = new Dimensions(preferredWidth, preferredHeight);
            }
        } else if (isPreferredHeightSet()) {
            if (skin != null) {
                int preferredWidth;

                if (preferredWidthCache != null
                    && preferredWidthCache.constraint == preferredHeight) {
                    preferredWidth = preferredWidthCache.value;
                } else {
                    preferredWidth = skin.getPreferredWidth(preferredHeight);

                    if (isValid()) {
                        // Update the cache
                        if (preferredWidthCache == null) {
                            preferredWidthCache = new PreferredSizeCache(preferredHeight,
                                preferredWidth);
                        } else {
                            preferredWidthCache.constraint = preferredHeight;
                            preferredWidthCache.value = preferredWidth;
                        }
                    }
                }

                preferredSize = new Dimensions(preferredWidth, preferredHeight);
            }
        } else {
            if (skin != null) {
                if (preferredWidthCache != null
                    && preferredWidthCache.constraint == -1
                    && preferredHeightCache != null
                    && preferredHeightCache.constraint == -1) {
                    preferredSize = new Dimensions(preferredWidthCache.value,
                        preferredHeightCache.value);
                } else {
                    preferredSize = skin.getPreferredSize();

                    if (isValid()) {
                        // Update the cache
                        if (preferredWidthCache == null) {
                            preferredWidthCache = new PreferredSizeCache(-1,
                                preferredSize.width);
                        } else {
                            preferredWidthCache.constraint = -1;
                            preferredWidthCache.value = preferredSize.width;
                        }

                        if (preferredHeightCache == null) {
                            preferredHeightCache = new PreferredSizeCache(-1,
                                preferredSize.height);
                        } else {
                            preferredHeightCache.constraint = -1;
                            preferredHeightCache.value = preferredSize.height;
                        }
                    }
                }
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
            repaint();

            // Set the new coordinates
            this.x = x;
            this.y = y;

            // Redraw the region currently occupied by this component
            repaint();

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
     * A rectangle value containing the component's horizontal and vertical
     * position relative to the origin of the parent container and the width
     * and height of the component.
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, getWidth(), getHeight());
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
            if (!visible
                && isFocused()) {
                setFocusedComponent(null);
            }

            // Redraw the region formerly occupied by this component
            repaint();

            this.visible = visible;

            // Redraw the region currently occupied by this component
            repaint();

            if (visible) {
                // This component is being shown; ensure that it's layout
                // is valid
                invalidate();
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
     * component is not a descendant of the specified ancestor
     */
    public Point mapPointToAncestor(Container ancestor, int x, int y) {
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
     * component is not a descendant of the specified ancestor
     */
    public Point mapPointFromAncestor(Container ancestor, int x, int y) {
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
     */
    public boolean isShowing() {
        boolean showing = true;

        Component component = this;
        Display display = Display.getInstance();

        while (component != null
            && showing) {
            Container parent = component.getParent();
            showing &= (component.isVisible()
                && (parent != null || component == display));

            component = parent;
        }

        return showing;
    }

    /**
     * Determines the visible bounds of an area in component space: the
     * intersection of the area with the visible area of the component
     * and its ancestors.
     *
     * @param x
     * @param y
     * @param width
     * @param height
     *
     * @return
     * The visible bounding rectangle of the given area in display coordinates,
     * or <tt>null</tt> if the component is either not visible or not part of
     * the container hierarchy.
     */
    public Rectangle getVisibleArea(int x, int y, int width, int height) {
        Rectangle visibleArea = null;

        Component component = this;
        Display display = Display.getInstance();

        int top = y;
        int left = x;
        int bottom = y + height - 1;
        int right = x + width - 1;

        while (component != null
            && component.isVisible()) {
            top = component.y + Math.max(top, 0);
            left = component.x + Math.max(left, 0);
            bottom = component.y + Math.max(Math.min(bottom, component.getHeight() - 1), -1);
            right = component.x + Math.max(Math.min(right, component.getWidth() - 1), -1);

            if (component == display) {
                visibleArea = new Rectangle(left, top, right - left + 1, bottom - top + 1);
            }

            component = component.getParent();
        }

        return visibleArea;
    }

    /**
     * If the component is in a viewport, ensures that the given area is
     * visible.
     *
     * @param area
     */
    public void scrollAreaToVisible(Rectangle area) {
        if (area == null) {
            throw new IllegalArgumentException("area is null.");
        }

        scrollAreaToVisible(area.x, area.y, area.width, area.height);
    }

    /**
     * If the component is in a viewport, ensures that the given area is
     * visible.
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
                    Rectangle viewportBounds = viewport.getViewportBounds();

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
                    // been any other type of component.  Namely, we do nothing
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
     *
     * @return
     * <tt>true</tt>; non-container components are always valid.
     */
    public boolean isValid() {
        return true;
    }

    /**
     * Notifies the component's parent that it needs to re-layout.
     */
    public void invalidate() {
        preferredWidthCache = null;
        preferredHeightCache = null;

        if (parent != null) {
            parent.invalidate();
        }
    }

    /**
     * Lays out the component by calling {@link Skin#layout()}.
     * <p>
     * This is an effective no-op for non-containers since the skin's
     * implementation of layout() will be a no-op.
     */
    public void validate() {
        if (skin != null) {
            if (getWidth() > 0
                && getHeight() > 0) {
                skin.layout();
            }
        }
    }

    /**
     * Flags the entire component as needing to be repainted.
     */
    public final void repaint() {
        repaint(0, 0, getWidth(), getHeight());
    }

    /**
     * Flags the given rectangle as needing to be repainted.
     */
    public final void repaint(Rectangle rectangle) {
        repaint(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    /**
     * Flags the given rectangle as needing to be repainted.
     */
    public void repaint(int x, int y, int width, int height) {
        Rectangle visibleArea = getVisibleArea(x, y, width, height);

        if (visibleArea != null) {
            ApplicationContext.getInstance().repaint(visibleArea.x, visibleArea.y,
                visibleArea.width, visibleArea.height);
        }
    }

    /**
     * Paints the component. Delegates to the skin.
     */
    public void paint(Graphics2D graphics) {
        if (skin != null) {
            skin.paint(graphics);
        }
    }

    /**
     * Returns a graphics context that can be used to paint the component.
     *
     * @return
     */
    public Graphics2D getGraphics() {
        Graphics2D graphics = null;

        Rectangle visibleBounds = getVisibleArea(0, 0, getWidth(), getHeight());

        if (visibleBounds != null) {
            graphics = ApplicationContext.getInstance().getGraphics();

            if (graphics != null) {
                graphics.clip(visibleBounds);

                Point displayLocation = mapPointToAncestor(Display.getInstance(), 0, 0);
                graphics.translate(displayLocation.x, displayLocation.y);
            }
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
                    setFocusedComponent(null);
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
     * The cursor to display over the component.
     */
    public void setCursor(Cursor cursor) {
        if (cursor == null) {
            throw new IllegalArgumentException("cursor is null.");
        }

        Cursor previousCursor = this.cursor;

        if (previousCursor != cursor) {
            this.cursor = cursor;
            componentListeners.cursorChanged(this, previousCursor);
        }
    }

    public final void setCursor(String cursor) {
        if (cursor == null) {
            throw new IllegalArgumentException("cursor is null.");
        }

        setCursor(Cursor.decode(cursor));
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
     * Returns the component's drag handler.
     *
     * @return
     * The component's drag handler, or <tt>null</tt> if no handler is
     * installed.
     */
    public DragHandler getDragHandler() {
        return dragHandler;
    }

    /**
     * Sets the component's drag handler.
     *
     * @param dragHandler
     * The drag handler to install, or <tt>null</tt> for no handler.
     */
    public void setDragHandler(DragHandler dragHandler) {
        DragHandler previousDragHandler = this.dragHandler;
        if (previousDragHandler != dragHandler) {
            this.dragHandler = dragHandler;
            componentDragDropListeners.dragHandlerChanged(this, previousDragHandler);
        }
    }

    /**
     * Returns the component's drop handler.
     *
     * @return
     * The component's drop handler, or <tt>null</tt> if no handler is
     * installed.
     */
    public DropHandler getDropHandler() {
        return dropHandler;
    }

    /**
     * Sets the component's drop handler.
     *
     * @param dropHandler
     * The drop handler to install, or <tt>null</tt> for no handler.
     */
    public void setDropHandler(DropHandler dropHandler) {
        DropHandler previousDropHandler = this.dropHandler;
        if (previousDropHandler != dropHandler) {
            this.dropHandler = dropHandler;
            componentDragDropListeners.dropHandlerChanged(this, previousDropHandler);
        }
    }

    /**
     * Returns this component's focusability. A focusable component is capable
     * of receiving the focus.
     *
     * @return
     * <tt>true</tt> if the component is enabled and visible.
     */
    public boolean isFocusable() {
        return (skin == null) ? false : skin.isFocusable();
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
     * Sets the focused component. The component must be focusable, unblocked,
     * and showing, and its window must be open.
     *
     * @param component
     * The component to focus, or <tt>null</tt> to clear the focus.
     */
    public static void setFocusedComponent(Component focusedComponent) {
        setFocusedComponent(focusedComponent, false);
    }

    /**
     * Sets the focused component.
     *
     * @param component
     * The component to focus, or <tt>null</tt> to clear the focus.
     *
     * @param temporary
     * <tt>true</tt> if this focus change is or was temporary; <tt>false</tt>,
     * if it is permanent.
     */
    protected static void setFocusedComponent(Component focusedComponent, boolean temporary) {
        Component previousFocusedComponent = Component.focusedComponent;

        if (previousFocusedComponent != focusedComponent) {
            if (focusedComponent != null) {
                if (!focusedComponent.isFocusable()) {
                    throw new IllegalArgumentException("focusedComponent is not focusable.");
                }

                if (focusedComponent.isBlocked()) {
                    throw new IllegalArgumentException("focusedComponent is blocked.");
                }

                if (!focusedComponent.isShowing()) {
                    throw new IllegalArgumentException("focusedComponent is not showing.");
                }
            }

            // Set the focused component
            Component.focusedComponent = focusedComponent;

            // Notify the components of the state change
            if (previousFocusedComponent != null) {
                previousFocusedComponent.setFocused(false, temporary);
                previousFocusedComponent.getWindow().descendantLostFocus(previousFocusedComponent);
            }

            if (focusedComponent != null) {
                focusedComponent.setFocused(true, temporary);
                focusedComponent.getWindow().descendantGainedFocus(focusedComponent);
            }

            componentClassListeners.focusedComponentChanged(previousFocusedComponent);
        }
    }

    /**
     * Transfers focus to the next focusable component.
     */
    public static void transferFocus(Direction direction) {
        Component component = focusedComponent;

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
        setFocusedComponent(component);
    }

    /**
     * Copies bound values from the bind context to the component. This
     * functionality must be provided by the subclass; the base implementation
     * is a no-op.
     *
     * @param context
     */
    public void load(Dictionary<String, Object> context) {
    }

    /**
     * Copies bound values from the component to the bind context. This
     * functionality must be provided by the subclass; the base implementation
     * is a no-op.
     *
     * @param context
     */
    public void store(Dictionary<String, Object> context) {
    }

    public Object getUserData() {
        return userData;
    }

    public void setUserData(Object userData) {
        Object previousUserData = this.userData;
        this.userData = userData;
        componentDataListeners.userDataChanged(this, previousUserData);
    }

    /**
     * Returns a dictionary instance representing the component's style
     * properties. This is effectively a pass-through to the skin's dictionary
     * implementation. It allows callers to modify the properties of the skin
     * without directly obtaining a reference to the skin.
     */
    public StyleDictionary getStyles() {
        return styleDictionary;
    }

    /**
     * Applies a set of styles.
     *
     * @param styles
     */
    public void setStyles(Map<String, Object> styles) {
        if (styles == null) {
            throw new IllegalArgumentException("styles is null.");
        }

        for (String key : styles) {
            getStyles().put(key, styles.get(key));
        }
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
     */
    protected Attributes getAttributes() {
        return attributes;
    }

    /**
     * Sets the attributes.
     *
     * @param attributes
     */
    protected void setAttributes(Attributes attributes) {
        assert (parent != null);

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
            if (skin != null) {
                consumed = skin.mouseMove(x, y);
            }

            componentMouseListeners.mouseMove(this, x, y);
        }

        return consumed;
    }

    protected void mouseOver() {
        if (enabled) {
            // Only change the cursor if no mouse buttons are pressed
            if (Mouse.getButtons() == 0) {
                ApplicationContext.getInstance().setCursor(cursor);
            }

            if (skin != null) {
                skin.mouseOver();
            }

            mouseOver = true;

            componentMouseListeners.mouseOver(this);
        }
    }

    protected void mouseOut() {
        if (enabled) {
            // Only change the cursor if no mouse buttons are pressed
            if (Mouse.getButtons() == 0) {
                ApplicationContext.getInstance().setCursor((parent == null) ?
                    Cursor.DEFAULT : parent.getCursor());
            }

            if (skin != null) {
                skin.mouseOut();
            }

            mouseOver = false;

            componentMouseListeners.mouseOut(this);
        }
    }

    protected boolean mouseDown(Mouse.Button button, int x, int y) {
        boolean consumed = false;

        if (enabled) {
            if (skin != null) {
                consumed = skin.mouseDown(button, x, y);
            }

            componentMouseButtonListeners.mouseDown(this, button, x, y);
        }

        return consumed;
    }

    protected boolean mouseUp(Mouse.Button button, int x, int y) {
        boolean consumed = false;

        if (enabled) {
            if (skin != null) {
                consumed = skin.mouseUp(button, x, y);
            }

            componentMouseButtonListeners.mouseUp(this, button, x, y);
        }

        return consumed;
    }

    protected void mouseClick(Mouse.Button button, int x, int y, int count) {
        if (enabled) {
            if (skin != null) {
                skin.mouseClick(button, x, y, count);
            }

            componentMouseButtonListeners.mouseClick(this, button, x, y, count);
        }
    }

    protected boolean mouseWheel(Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        boolean consumed = false;

        if (enabled) {
            if (skin != null) {
                consumed = skin.mouseWheel(scrollType, scrollAmount, wheelRotation, x, y);
            }

            componentMouseWheelListeners.mouseWheel(this, scrollType, scrollAmount,
                wheelRotation, x, y);
        }

        return consumed;
    }

    protected void keyTyped(char character) {
        if (enabled) {
            if (skin != null) {
                skin.keyTyped(character);
            }

            componentKeyListeners.keyTyped(this, character);

            if (parent != null) {
                parent.keyTyped(character);
            }
        }
    }

    protected boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (enabled) {
            if (skin != null) {
                consumed = skin.keyPressed(keyCode, keyLocation);
            }

            componentKeyListeners.keyPressed(this, keyCode, keyLocation);

            if (!consumed && parent != null) {
                parent.keyPressed(keyCode, keyLocation);
            }
        }

        return consumed;
    }

    protected boolean keyReleased(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (enabled) {
            if (skin != null) {
                consumed = skin.keyReleased(keyCode, keyLocation);
            }

            componentKeyListeners.keyReleased(this, keyCode, keyLocation);

            if (!consumed && parent != null) {
                parent.keyReleased(keyCode, keyLocation);
            }
        }

        return consumed;
    }

    @Override
    public String toString() {
        String s = this.getClass().getName() + "#" + getHandle();

        Class<? extends Skin> skinClass = getSkinClass();
        if (skinClass != null) {
            s += " [" + skinClass.getName() + "]";
        }

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

    public ListenerList<ComponentDataListener> getComponentDataListeners() {
        return componentDataListeners;
    }

    public ListenerList<ComponentDragDropListener> getComponentDragDropListeners() {
        return componentDragDropListeners;
    }

    public static ListenerList<ComponentClassListener> getComponentClassListeners() {
        return componentClassListeners;
    }
}
