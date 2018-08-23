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
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerListener;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.FocusTraversalPolicy;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Theme;

/**
 * Abstract base class for container skins.
 */
public abstract class ContainerSkin extends ComponentSkin implements ContainerListener,
    ContainerMouseListener {
    /**
     * Focus traversal policy that determines traversal order based on the order
     * of components in the container's component sequence.
     */
    public static class IndexFocusTraversalPolicy implements FocusTraversalPolicy {
        private boolean wrap;

        public IndexFocusTraversalPolicy() {
            this(false);
        }

        public IndexFocusTraversalPolicy(final boolean wrap) {
            this.wrap = wrap;
        }

        @Override
        public Component getNextComponent(final Container container, final Component component,
            final FocusTraversalDirection direction) {
            Utils.checkNull(container, "container");
            Utils.checkNull(direction, "direction");

            Component nextComponent = null;

            int n = container.getLength();
            if (n > 0) {
                switch (direction) {
                    case FORWARD:
                        if (component == null) {
                            // Return the first component in the sequence
                            nextComponent = container.get(0);
                        } else {
                            // Return the next component in the sequence
                            int index = container.indexOf(component);
                            if (index == -1) {
                                throw new IllegalArgumentException("Component is not a child of the container.");
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

                    case BACKWARD:
                        if (component == null) {
                            // Return the last component in the sequence
                            nextComponent = container.get(n - 1);
                        } else {
                            // Return the previous component in the sequence
                            int index = container.indexOf(component);
                            if (index == -1) {
                                throw new IllegalArgumentException("Component is not a child of the container.");
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

                    default:
                        break;
                }
            }

            return nextComponent;
        }
    }

    private Paint backgroundPaint = null;

    private static final FocusTraversalPolicy DEFAULT_FOCUS_TRAVERSAL_POLICY = new IndexFocusTraversalPolicy();

    @Override
    public void install(final Component component) {
        super.install(component);

        Container container = (Container) component;

        // Add this as a container listener
        container.getContainerListeners().add(this);
        container.getContainerMouseListeners().add(this);

        // Set the focus traversal policy
        container.setFocusTraversalPolicy(DEFAULT_FOCUS_TRAVERSAL_POLICY);
    }

    @Override
    public int getPreferredWidth(final int height) {
        return 0;
    }

    @Override
    public int getPreferredHeight(final int width) {
        return 0;
    }

    @Override
    public void paint(final Graphics2D graphics) {
        if (backgroundPaint != null) {
            graphics.setPaint(backgroundPaint);
            graphics.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /**
     * @return <tt>false</tt>; by default, containers are not focusable.
     */
    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public boolean isOpaque() {
        return (backgroundPaint != null && backgroundPaint.getTransparency() == Transparency.OPAQUE);
    }

    /**
     * @return The {@link Paint} object used to paint the background of the
     * container.
     */
    public Paint getBackgroundPaint() {
        return backgroundPaint;
    }

    /**
     * Sets the object used to paint the background of the container.
     *
     * @param backgroundPaint The new {@link Paint} object to paint the background.
     */
    public void setBackgroundPaint(final Paint backgroundPaint) {
        this.backgroundPaint = backgroundPaint;
        repaintComponent();
    }

    /**
     * Sets the object used to paint the background of the container.
     *
     * @param backgroundPaint A string recognized by Pivot as a
     * {@linkplain GraphicsUtilities#decodePaint(String) Color or Paint value}.
     */
    public final void setBackgroundPaint(final String backgroundPaint) {
        setBackgroundPaint(GraphicsUtilities.decodePaint(backgroundPaint));
    }

    /**
     * Sets the object used to paint the background of the container.
     *
     * @param backgroundPaint A dictionary containing a
     * {@linkplain GraphicsUtilities#decodePaint(Dictionary) Paint description}.
     */
    public final void setBackgroundPaint(final Dictionary<String, ?> backgroundPaint) {
        setBackgroundPaint(GraphicsUtilities.decodePaint(backgroundPaint));
    }

    /**
     * @return The color of the container's background if a solid color has been
     * set as the background; otherwise {@code null}.
     */
    public Color getBackgroundColor() {
        return (backgroundPaint instanceof Color) ? (Color) backgroundPaint : null;
    }

    /**
     * Sets the background of the container to a solid color.
     *
     * @param backgroundColor The new background color for the container.
     */
    public void setBackgroundColor(final Color backgroundColor) {
        setBackgroundPaint(backgroundColor);
    }

    /**
     * Sets the background of the container to a solid color.
     *
     * @param backgroundColor Any of the
     * {@linkplain GraphicsUtilities#decodeColor color values recognized by
     * Pivot}.
     */
    public final void setBackgroundColor(final String backgroundColor) {
        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor, "backgroundColor"));
    }

    /**
     * Sets the background of the container to one of the theme colors.
     *
     * @param backgroundColor An index into the theme's color palette.
     */
    public final void setBackgroundColor(final int backgroundColor) {
        Theme theme = currentTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
    }

    // Container events
    @Override
    public void componentInserted(final Container container, final int index) {
        // No-op
    }

    @Override
    public void componentsRemoved(final Container container, final int index, final Sequence<Component> removed) {
        // No-op
    }

    @Override
    public void componentMoved(final Container container, final int from, final int to) {
        // No-op
    }

    @Override
    public void focusTraversalPolicyChanged(final Container container,
        final FocusTraversalPolicy previousFocusTraversalPolicy) {
        // No-op
    }

    @Override
    public boolean mouseMove(final Container container, final int x, final int y) {
        return false;
    }

    @Override
    public boolean mouseDown(final Container container, final Mouse.Button button, final int x, final int y) {
        return false;
    }

    @Override
    public boolean mouseUp(final Container container, final Mouse.Button button, final int x, final int y) {
        return false;
    }

    @Override
    public boolean mouseWheel(final Container container, final Mouse.ScrollType scrollType, final int scrollAmount,
        final int wheelRotation, final int x, final int y) {
        return false;
    }
}
