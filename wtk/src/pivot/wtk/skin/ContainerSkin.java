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
package pivot.wtk.skin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Transparency;

import pivot.collections.Sequence;
import pivot.wtk.Component;
import pivot.wtk.Container;
import pivot.wtk.ContainerListener;
import pivot.wtk.Direction;
import pivot.wtk.GraphicsUtilities;
import pivot.wtk.FocusTraversalPolicy;

/**
 * Abstract base class for container skins.
 *
 * @author gbrown
 */
public abstract class ContainerSkin extends ComponentSkin
    implements ContainerListener {
    /**
     * Focus traversal policy that determines traversal order based on the order
     * of components in the container's component sequence.
     *
     * @author gbrown
     */
    public static class IndexFocusTraversalPolicy implements FocusTraversalPolicy {
        private boolean wrap;

        public IndexFocusTraversalPolicy() {
            this(false);
        }

        public IndexFocusTraversalPolicy(boolean wrap) {
            this.wrap = wrap;
        }

        public Component getNextComponent(Container container, Component component, Direction direction) {
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
                                if (wrap
                                    && container.containsFocus()) {
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
                                if (wrap
                                    && container.containsFocus()) {
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

    @Override
    public void install(Component component) {
        super.install(component);

        Container container = (Container)component;

        // Add this as a container listener
        container.getContainerListeners().add(this);

        // Set the focus traversal policy
        container.setFocusTraversalPolicy(new IndexFocusTraversalPolicy());
    }

    public void uninstall() {
        Container container = (Container)getComponent();

        // Remove this as a container listener
        container.getContainerListeners().remove(this);

        // Clear the focus traversal policy
        container.setFocusTraversalPolicy(null);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        return 0;
    }

    public int getPreferredHeight(int width) {
        return 0;
    }

    public void paint(Graphics2D graphics) {
        if (backgroundPaint != null) {
            graphics.setPaint(backgroundPaint);
            graphics.fillRect(0, 0, getWidth(), getHeight());
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

    /**
     * By default, a container's opacity depends on its
     * <tt>backgroundPaint</tt> style.
     *
     * @return
     * <tt>true</tt> if <tt>backgroundPaint</tt> is non-<tt>null</tt>.  False
     * otherwise.
     */
    @Override
    public boolean isOpaque() {
        boolean opaque = false;

        if (backgroundPaint != null
            && backgroundPaint.getTransparency() == Transparency.OPAQUE) {
            opaque = true;
        }

        return opaque;
    }

    public Paint getBackgroundPaint() {
        return backgroundPaint;
    }

    public void setBackgroundPaint(Paint backgroundPaint) {
        this.backgroundPaint = backgroundPaint;
        repaintComponent();
    }

    public Color getBackgroundColor() {
        if (backgroundPaint != null
            && !(backgroundPaint instanceof Color)) {
            throw new IllegalStateException("Background paint is not a Color.");
        }
        return (Color)backgroundPaint;
    }

    public void setBackgroundColor(Color backgroundColor) {
        setBackgroundPaint(backgroundColor);
    }

    public final void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null");
        }

        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
    }

    // Container events
    public void componentInserted(Container container, int index) {
    }

    public void componentsRemoved(Container container, int index, Sequence<Component> removed) {
    }

    public void contextKeyChanged(Container container, String previousContextKey) {
        // No-op
    }

    public void focusTraversalPolicyChanged(Container container,
        FocusTraversalPolicy previousFocusTraversalPolicy) {
        // No-op
    }
}
