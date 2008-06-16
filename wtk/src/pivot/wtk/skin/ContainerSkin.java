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
package pivot.wtk.skin;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

import pivot.collections.Sequence;
import pivot.wtk.Component;
import pivot.wtk.ComponentAttributeListener;
import pivot.wtk.Container;
import pivot.wtk.ContainerListener;
import pivot.wtk.Dimensions;
import pivot.wtk.Direction;
import pivot.wtk.FocusTraversalPolicy;
import pivot.wtk.Rectangle;

public abstract class ContainerSkin extends ComponentSkin
    implements ContainerListener, ComponentAttributeListener {
    /**
     * Focus traversal policy that determines traversal order based on the order
     * of components in the container's component sequence.
     *
     * @author gbrown
     */
    public static class IndexFocusTraversalPolicy implements FocusTraversalPolicy {
        public Component getNextComponent(Container container, Component component, Direction direction) {
            if (container == null) {
                throw new IllegalArgumentException("container is null.");
            }

            if (direction == null) {
                throw new IllegalArgumentException("direction is null.");
            }

            Component nextComponent = null;
            Container.ComponentSequence components = container.getComponents();

            switch (direction) {
                case FORWARD: {
                    if (component == null) {
                        // Return the first component in the sequence
                        if (components.getLength() > 0) {
                            nextComponent = components.get(0);
                        }
                    } else {
                        // Return the next component in the sequence
                        int index = components.indexOf(component);

                        if (index >= 0
                            && index < components.getLength() - 1) {
                            nextComponent = components.get(index + 1);
                        }
                    }

                    break;
                }

                case BACKWARD: {
                    if (component == null) {
                        // Return the last component in the sequence
                        int n = components.getLength();
                        if (n > 0) {
                            nextComponent = components.get(n - 1);
                        }
                    } else {
                        // Return the previous component in the sequence
                        int index = components.indexOf(component);

                        if (index > 0
                            && index < components.getLength()) {
                            nextComponent = components.get(index - 1);
                        }
                    }

                    break;
                }
            }

            return nextComponent;
        }
    }

    private static final FocusTraversalPolicy DEFAULT_FOCUS_TRAVERSAL_POLICY = new IndexFocusTraversalPolicy();

    // Style properties
    protected Color backgroundColor = DEFAULT_BACKGROUND_COLOR;
    protected float backgroundOpacity = DEFAULT_BACKGROUND_OPACITY;

    // Default style values
    private static final Color DEFAULT_BACKGROUND_COLOR = null;
    private static final float DEFAULT_BACKGROUND_OPACITY = 1.0f;

    // Style keys
    protected static final String BACKGROUND_COLOR_KEY = "backgroundColor";
    protected static final String BACKGROUND_OPACITY_KEY = "backgroundOpacity";

    public ContainerSkin() {
        super();
    }

    @Override
    public void install(Component component) {
        validateComponentType(component, Container.class);

        super.install(component);

        Container container = (Container)component;

        // Add this as a container listener
        container.getContainerListeners().add(this);

        // Add this as an attribute listener on all child components
        for (Component childComponent : container.getComponents()) {
            childComponent.getComponentAttributeListeners().add(this);
        }

        // Set the focus traversal policy
        container.setFocusTraversalPolicy(DEFAULT_FOCUS_TRAVERSAL_POLICY);
    }

    public void uninstall() {
        Container container = (Container)getComponent();

        // Remove this as a container listener
        container.getContainerListeners().remove(this);

        // Remove this as an attribute listener on all child components
        for (Component childComponent : container.getComponents()) {
            childComponent.getComponentAttributeListeners().add(this);
        }

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        return 0;
    }

    public int getPreferredHeight(int width) {
        return 0;
    }

    public Dimensions getPreferredSize() {
        return new Dimensions(0, 0);
    }

    public void paint(Graphics2D graphics) {
        if (backgroundColor != null
            && backgroundOpacity > 0.0f) {
            if (backgroundOpacity < 1.0f) {
                graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, backgroundOpacity));
            }

            graphics.setPaint(backgroundColor);
            Rectangle bounds = new Rectangle(0, 0, getWidth(), getHeight());
            graphics.fill(bounds);
        }
    }

    /**
     * @return
     * <tt>false</tt>; containers are not focusable.
     */
    @Override
    public final boolean isFocusable() {
        return false;
    }

    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        if (key.equals(BACKGROUND_COLOR_KEY)) {
            value = backgroundColor;
        } else if (key.equals(BACKGROUND_OPACITY_KEY)) {
            value = backgroundOpacity;
        } else {
            value = super.get(key);
        }

        return value;
    }

    public Object put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(BACKGROUND_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, true);

            previousValue = backgroundColor;
            backgroundColor = (Color)value;

            repaintComponent();
        } else if (key.equals(BACKGROUND_OPACITY_KEY)) {
            if (value instanceof String) {
                value = Float.parseFloat((String)value);
            }

            validatePropertyType(key, value, Float.class, false);

            previousValue = backgroundOpacity;
            backgroundOpacity = (Float)value;

            repaintComponent();
        } else {
            super.put(key, value);
        }

        return previousValue;
    }

    public Object remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(BACKGROUND_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_BACKGROUND_COLOR);
        } else if (key.equals(BACKGROUND_OPACITY_KEY)) {
            previousValue = put(key, DEFAULT_BACKGROUND_OPACITY);
        } else {
            previousValue = super.remove(key);
        }

        return previousValue;
    }

    public boolean containsKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        return (key.equals(BACKGROUND_COLOR_KEY)
            || key.equals(BACKGROUND_OPACITY_KEY)
            || super.containsKey(key));
    }

    public boolean isEmpty() {
        return false;
    }

    // Container events
    public void componentInserted(Container container, int index) {
        Component component = container.getComponents().get(index);
        component.getComponentAttributeListeners().add(this);
    }

    public void componentsRemoved(Container container, int index, Sequence<Component> components) {
        for (int i = 0, n = components.getLength(); i < n; i++) {
            Component component = components.get(i);
            component.getComponentAttributeListeners().remove(this);
        }
    }

    public void contextKeyChanged(Container container, String previousContextKey) {
        // No-op
    }

    public void focusTraversalPolicyChanged(Container container,
        FocusTraversalPolicy previousFocusTraversalPolicy) {
        // No-op
    }

    // Component attribute events
    public void attributeAdded(Component component, Container.Attribute attribute) {
    }

    public void attributeUpdated(Component component, Container.Attribute attribute,
        Object previousValue) {
    }

    public void attributeRemoved(Component component, Container.Attribute attribute,
        Object value) {
    }
}
