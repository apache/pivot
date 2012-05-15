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
package org.apache.pivot.wtk.skin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Transparency;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerListener;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.FocusTraversalPolicy;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Mouse;


/**
 * Abstract base class for container skins.
 */
public abstract class ContainerSkin extends ComponentSkin
    implements ContainerListener, ContainerMouseListener {
    /**
     * Focus traversal policy that determines traversal order based on the order
     * of components in the container's component sequence.
     */
    public static class IndexFocusTraversalPolicy implements FocusTraversalPolicy {
        private boolean wrap;

        public IndexFocusTraversalPolicy() {
            this(false);
        }

        public IndexFocusTraversalPolicy(boolean wrap) {
            this.wrap = wrap;
        }

        @Override
        public Component getNextComponent(Container container, Component component, FocusTraversalDirection direction) {
            if (container == null) {
                throw new IllegalArgumentException("container is null.");
            }

            if (direction == null) {
                throw new IllegalArgumentException("direction is null.");
            }

            Component nextComponent = null;

            int n = container.getLength();
            if (n > 0) {
                switch (direction) {
                    case FORWARD: {
                        if (component == null) {
                            // Return the first component in the sequence
                            nextComponent = container.get(0);
                        } else {
                            // Return the next component in the sequence
                            int index = container.indexOf(component);
                            if (index == -1) {
                                throw new IllegalArgumentException();
                            }

                            if (index < n - 1) {
                                nextComponent = container.get(index + 1);
                            } else {
                                if (wrap) {
                                    nextComponent = container.get(0);
                                }
                            }
                        }

                        break;
                    }

                    case BACKWARD: {
                        if (component == null) {
                            // Return the last component in the sequence
                            nextComponent = container.get(n - 1);
                        } else {
                            // Return the previous component in the sequence
                            int index = container.indexOf(component);
                            if (index == -1) {
                                throw new IllegalArgumentException();
                            }

                            if (index > 0) {
                                nextComponent = container.get(index - 1);
                            } else {
                                if (wrap) {
                                    nextComponent = container.get(n - 1);
                                }
                            }
                        }

                        break;
                    }
                }
            }

            return nextComponent;
        }
    }

    private Paint backgroundPaint = null;

    private static FocusTraversalPolicy DEFAULT_FOCUS_TRAVERSAL_POLICY = new IndexFocusTraversalPolicy();

    @Override
    public void install(Component component) {
        super.install(component);

        Container container = (Container)component;

        // Add this as a container listener
        container.getContainerListeners().add(this);
        container.getContainerMouseListeners().add(this);

        // Set the focus traversal policy
        container.setFocusTraversalPolicy(DEFAULT_FOCUS_TRAVERSAL_POLICY);
    }

    @Override
    public int getPreferredWidth(int height) {
        return 0;
    }

    @Override
    public int getPreferredHeight(int width) {
        return 0;
    }

    @Override
    public void paint(Graphics2D graphics) {
        if (backgroundPaint != null) {
            graphics.setPaint(backgroundPaint);
            graphics.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /**
     * @return
     * <tt>false</tt>; by default, containers are not focusable.
     */
    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public boolean isOpaque() {
        return (backgroundPaint != null
            && backgroundPaint.getTransparency() == Transparency.OPAQUE);
    }

    /**
     * Returns the {@link Paint} object used to paint the background of the container
     */
    public Paint getBackgroundPaint() {
        return backgroundPaint;
    }

    /**
     * Sets the object used to paint the background of the container.
     * @param backgroundPaint The {@link Paint} object
     */
    public void setBackgroundPaint(Paint backgroundPaint) {
        this.backgroundPaint = backgroundPaint;
        repaintComponent();
    }

    /**
     * Sets the object used to paint the background of the container.
     * @param backgroundPaint A string recognized by Pivot as a
     * {@linkplain GraphicsUtilities#decodePaint(String) Color or Paint value}.
     */
    public final void setBackgroundPaint(String backgroundPaint) {
        if (backgroundPaint == null) {
            throw new IllegalArgumentException("backgroundPaint is null");
        }

        setBackgroundPaint(GraphicsUtilities.decodePaint(backgroundPaint));
    }

    /**
     * Sets the object used to paint the background of the container.
     * @param backgroundPaint A dictionary containing a
     * {@linkplain GraphicsUtilities#decodePaint(Dictionary) Paint description}.
     */
    public final void setBackgroundPaint(Dictionary<String, ?> backgroundPaint) {
        if (backgroundPaint == null) {
            throw new IllegalArgumentException("backgroundPaint is null");
        }

        setBackgroundPaint(GraphicsUtilities.decodePaint(backgroundPaint));
    }

    /**
     * Returns the color of the container's background if a solid color has been
     * set as the background; otherwise null.
     */
    public Color getBackgroundColor() {
        return (backgroundPaint instanceof Color) ? (Color)backgroundPaint : null;
    }

    /**
     * Sets the background of the container to a solid color.
     */
    public void setBackgroundColor(Color backgroundColor) {
        setBackgroundPaint(backgroundColor);
    }

    /**
     * Sets the background of the container to a solid color.
     * @param backgroundColor Any of the {@linkplain GraphicsUtilities#decodeColor color values recognized by Pivot}.
     */
    public final void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null");
        }

        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
    }

    // Container events
    @Override
    public void componentInserted(Container container, int index) {
        // No-op
    }

    @Override
    public void componentsRemoved(Container container, int index, Sequence<Component> removed) {
        // No-op
    }

    @Override
    public void componentMoved(Container container, int from, int to) {
        // No-op
    }

    @Override
    public void focusTraversalPolicyChanged(Container container,
        FocusTraversalPolicy previousFocusTraversalPolicy) {
        // No-op
    }

    @Override
    public boolean mouseMove(Container container, int x, int y) {
        return false;
    }

    @Override
    public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
        return false;
    }

    @Override
    public boolean mouseUp(Container container, Mouse.Button button, int x, int y) {
        return false;
    }

    @Override
    public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
        int scrollAmount, int wheelRotation, int x, int y) {
        return false;
    }
}
