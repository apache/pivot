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

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import javax.swing.JComponent;

import pivot.util.ListenerList;
import pivot.wtk.skin.ComponentSkin;

/**
 * Component that wraps a Swing <tt>JComponent</tt> for use in a pivot
 * application.
 * <p>
 * NOTE: Some Swing components do not play nicely with Pivot's
 * <tt>ScrollPane</tt> class. If this behavior is seen, it is recommended that
 * you place your Swing component into a <tt>JScrollPane</tt> instead of using
 * a Pivot scroll pane.
 *
 * @author tvolkert
 */
public class SwingAdapter extends Component {
    /**
     * Swing adapter skin.
     * <p>
     * NOTE: This must live in <tt>pivot.wtk</tt> because it needs protected
     * access to the display host.
     *
     * @author tvolkert
     */
    private class SwingAdapterSkin extends ComponentSkin implements SwingAdapterListener {
        private class SwingContainer extends java.awt.Container {
            static final long serialVersionUID = -9151344702095162523L;

            public SwingContainer() {
                setLayout(new BorderLayout());
            }

            @Override
            public void invalidate() {
                super.invalidate();
                SwingAdapterSkin.this.getComponent().invalidate();
            }
        }

        private ComponentListener componentHandler = new ComponentListener() {
            public void parentChanged(Component component, Container previousParent) {
                Display display = component.getDisplay();
                Display previousDisplay = (previousParent == null) ?
                    null : previousParent.getDisplay();

                // Add our swingContainer to the correct display host
                if (display != previousDisplay) {
                    if (previousDisplay != null) {
                        previousDisplay.getApplicationContext().getDisplayHost().remove(swingContainer);
                    }

                    if (display != null) {
                        display.getApplicationContext().getDisplayHost().add(swingContainer);
                    }
                }

                // Stop listening on the previous ancestry
                Component previousAncestor = previousParent;
                while (previousAncestor != null) {
                    previousAncestor.getComponentListeners().remove(this);
                    previousAncestor = previousAncestor.getParent();
                }

                // Start listening on the new ancestry
                Component ancestor = component.getParent();
                while (ancestor != null) {
                    ancestor.getComponentListeners().add(this);
                    ancestor = ancestor.getParent();
                }
            }

            public void sizeChanged(Component component, int previousWidth, int previousHeight) {
                // No-op
            }

            public void locationChanged(Component component, int previousX, int previousY) {
                SwingAdapter swingAdapter = (SwingAdapter)getComponent();

                Display display = swingAdapter.getDisplay();
                Point displayCoordinates = swingAdapter.mapPointToAncestor(display, 0, 0);
                swingContainer.setLocation(displayCoordinates.x, displayCoordinates.y);
            }

            public void visibleChanged(Component component) {
                SwingAdapter swingAdapter = (SwingAdapter)getComponent();
                swingContainer.setVisible(swingAdapter.isShowing());
            }

            public void styleUpdated(Component component, String styleKey, Object previousValue) {
                // No-op
            }

            public void cursorChanged(Component component, Cursor previousCursor) {
                // No-op
            }

            public void tooltipTextChanged(Component component, String previousTooltipText) {
                // No-op
            }
        };

        private SwingContainer swingContainer = new SwingContainer();

        @Override
        public void install(Component component) {
            super.install(component);

            SwingAdapter swingAdapter = (SwingAdapter)component;
            swingAdapter.getSwingAdapterListeners().add(this);

            swingAdapter.getComponentListeners().add(componentHandler);
            componentHandler.parentChanged(swingAdapter, null);

            swingComponentChanged(swingAdapter, null);
        }

        @Override
        public void uninstall() {
            SwingAdapter swingAdapter = (SwingAdapter)getComponent();
            swingAdapter.getSwingAdapterListeners().remove(this);

            Component ancestor = swingAdapter;
            while (ancestor != null) {
                ancestor.getComponentListeners().remove(componentHandler);
                ancestor = ancestor.getParent();
            }

            Display display = swingAdapter.getDisplay();
            if (display != null) {
                display.getApplicationContext().getDisplayHost().remove(swingContainer);
            }

            JComponent swingComponent = swingAdapter.getSwingComponent();
            if (swingComponent != null) {
                swingContainer.remove(swingComponent);
            }

            super.uninstall();
        }

        public int getPreferredWidth(int height) {
            return getPreferredSize().width;
        }

        public int getPreferredHeight(int width) {
            return getPreferredSize().height;
        }

        @Override
        public Dimensions getPreferredSize() {
            java.awt.Dimension preferredSize = swingContainer.getPreferredSize();
            return new Dimensions(preferredSize.width, preferredSize.height);
        }

        public void layout() {
            SwingAdapter swingAdapter = (SwingAdapter)getComponent();

            int width = getWidth();
            int height = getHeight();

            Display display = swingAdapter.getDisplay();
            Point displayCoordinates = swingAdapter.mapPointToAncestor(display, 0, 0);
            swingContainer.setLocation(displayCoordinates.x, displayCoordinates.y);

            swingContainer.setSize(width, height);
            swingContainer.validate();
        }

        @Override
        public void paint(Graphics2D graphics) {
            swingContainer.paint(graphics);
        }

        @Override
        public boolean isFocusable() {
            return false;
        }

        @Override
        public void enabledChanged(Component component) {
            SwingAdapter swingAdapter = (SwingAdapter)getComponent();
            JComponent swingComponent = swingAdapter.getSwingComponent();

            swingComponent.setEnabled(component.isEnabled());
        }

        // SwingAdapterListener methods

        public void swingComponentChanged(SwingAdapter swingAdapter,
                                          JComponent previousSwingComponent) {
            if (previousSwingComponent != null) {
                swingContainer.remove(previousSwingComponent);
            }

            JComponent swingComponent = swingAdapter.getSwingComponent();

            if (swingComponent != null) {
                swingContainer.add(swingComponent, BorderLayout.CENTER);
            }

            invalidateComponent();
        }
    }

    /**
     * Swing adapter listener list.
     *
     * @author tvolkert
     */
    private static class SwingAdapterListenerList extends ListenerList<SwingAdapterListener>
        implements SwingAdapterListener {
        public void swingComponentChanged(SwingAdapter swingAdapter,
            JComponent previousSwingComponent) {
            for (SwingAdapterListener listener : this) {
                listener.swingComponentChanged(swingAdapter, previousSwingComponent);
            }
        }
    }

    private JComponent swingComponent;

    private SwingAdapterListenerList swingAdapterListeners = new SwingAdapterListenerList();

    public SwingAdapter() {
        this(null);
    }

    public SwingAdapter(JComponent swingComponent) {
        this.swingComponent = swingComponent;

        setSkin(new SwingAdapterSkin());
    }

    public JComponent getSwingComponent() {
        return swingComponent;
    }

    public void setSwingComponent(JComponent swingComponent) {
        JComponent previousSwingComponent = this.swingComponent;

        if (previousSwingComponent != swingComponent) {
            this.swingComponent = swingComponent;
            swingAdapterListeners.swingComponentChanged(this, previousSwingComponent);
        }
    }

    public ListenerList<SwingAdapterListener> getSwingAdapterListeners() {
        return swingAdapterListeners;
    }
}
