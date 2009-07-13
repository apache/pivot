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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.BoxPaneListener;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.VerticalAlignment;

/**
 * Box pane skin.
 * <p>
 * TODO Cache preferred component sizes when alignment is justified, so we
 * don't need to recalculate them during layout.
 *
 * @author gbrown
 */
public class BoxPaneSkin extends ContainerSkin
    implements BoxPaneListener {
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
    private VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
    private Insets padding = new Insets(0);
    private int spacing = 4;
    private boolean fill = false;

    @Override
    public void install(Component component) {
        super.install(component);

        BoxPane boxPane = (BoxPane)component;
        boxPane.getBoxPaneListeners().add(this);
    }

    @Override
    public void uninstall() {
        BoxPane boxPane = (BoxPane)getComponent();
        boxPane.getBoxPaneListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        BoxPane boxPane = (BoxPane)getComponent();

        Orientation orientation = boxPane.getOrientation();
        if (orientation == Orientation.HORIZONTAL) {
            // Include padding in constraint
            if (height != -1) {
                height = Math.max(height - (padding.top + padding.bottom), 0);
            }

            // Preferred width is the sum of the preferred widths of all components
            int i = 0;
            for (int j = 0, n = boxPane.getLength(); j < n; j++) {
                Component component = boxPane.get(j);

                if (component.isDisplayable()) {
                    preferredWidth += component.getPreferredWidth(height);
                    i++;
                }
            }

            // Include spacing
            if (i > 1) {
                preferredWidth += spacing * (i - 1);
            }
        } else {
            // Preferred width is the maximum preferred width of all components
            for (int i = 0, n = boxPane.getLength(); i < n; i++) {
                Component component = boxPane.get(i);

                if (component.isDisplayable()) {
                    preferredWidth = Math.max(preferredWidth,
                        component.getPreferredWidth(-1));
                }
            }
        }

        // Include left and right padding values
        preferredWidth += padding.left + padding.right;

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        BoxPane boxPane = (BoxPane)getComponent();

        Orientation orientation = boxPane.getOrientation();
        if (orientation == Orientation.VERTICAL) {
            // Include padding in constraint
            if (width != -1) {
                width = Math.max(width - (padding.left + padding.right), 0);
            }

            // Preferred height is the sum of the preferred heights of all components
            int i = 0;
            for (int j = 0, n = boxPane.getLength(); j < n; j++) {
                Component component = boxPane.get(i);

                if (component.isDisplayable()) {
                    preferredHeight += component.getPreferredHeight(width);
                    i++;
                }
            }

            // Include spacing
            if (i > 1) {
                preferredHeight += spacing * (i - 1);
            }
        } else {
            // Preferred height is the maximum preferred height of all components
            for (int i = 0, n = boxPane.getLength(); i < n; i++) {
                Component component = boxPane.get(i);

                if (component.isDisplayable()) {
                    preferredHeight = Math.max(preferredHeight,
                        component.getPreferredHeight(-1));
                }
            }
        }

        // Include top and bottom padding values
        preferredHeight += padding.top + padding.bottom;

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        BoxPane boxPane = (BoxPane)getComponent();

        switch (boxPane.getOrientation()) {
            case HORIZONTAL: {
                // Preferred width is the sum of the preferred widths of all components
                int i = 0;
                for (int j = 0, n = boxPane.getLength(); j < n; j++) {
                    Component component = boxPane.get(j);

                    if (component.isDisplayable()) {
                        Dimensions preferredSize = component.getPreferredSize();
                        preferredWidth += preferredSize.width;
                        preferredHeight = Math.max(preferredSize.height, preferredHeight);
                        i++;
                    }
                }

                // Include spacing
                if (i > 1) {
                    preferredWidth += spacing * (i - 1);
                }

                break;
            }

            case VERTICAL: {
                // Preferred height is the sum of the preferred heights of all components
                int i = 0;
                for (int j = 0, n = boxPane.getLength(); j < n; j++) {
                    Component component = boxPane.get(j);

                    if (component.isDisplayable()) {
                        Dimensions preferredSize = component.getPreferredSize();
                        preferredWidth = Math.max(preferredSize.width, preferredWidth);
                        preferredHeight += preferredSize.height;
                        i++;
                    }
                }

                // Include spacing
                if (i > 1) {
                    preferredHeight += spacing * (i - 1);
                }

                break;
            }
        }

        // Include padding
        preferredWidth += padding.left + padding.right;
        preferredHeight += padding.top + padding.bottom;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    public void layout() {
        BoxPane boxPane = (BoxPane)getComponent();
        int n = boxPane.getLength();

        int width = getWidth();
        int height = getHeight();

        Orientation orientation = boxPane.getOrientation();
        if (orientation == Orientation.HORIZONTAL) {
            int preferredWidth = (fill) ? getPreferredWidth(height) : getPreferredWidth(-1);

            // Determine the starting x-coordinate
            int componentX = 0;

            switch (horizontalAlignment) {
                case CENTER: {
                    componentX = (int)Math.round((double)(width - preferredWidth) / 2);
                    break;
                }

                case RIGHT: {
                    componentX = width - preferredWidth;
                    break;
                }
            }

            componentX += padding.left;

            // Lay out the components
            for (int i = 0; i < n; i++) {
                Component component = boxPane.get(i);

                if (component.isDisplayable()) {
                    int componentWidth = 0;
                    int componentHeight = 0;
                    int componentY = 0;

                    if (fill) {
                        componentY = padding.top;
                        componentHeight = Math.max(height - (padding.top
                            + padding.bottom), 0);
                        componentWidth = component.getPreferredWidth(componentHeight);
                    } else {
                        Dimensions preferredComponentSize = component.getPreferredSize();
                        componentWidth = preferredComponentSize.width;
                        componentHeight = preferredComponentSize.height;
                    }

                    switch (verticalAlignment) {
                        case TOP: {
                            componentY = padding.top;
                            break;
                        }

                        case CENTER: {
                            componentY = (int)Math.round((double)(height - componentHeight) / 2);
                            break;
                        }

                        case BOTTOM: {
                            componentY = height - padding.bottom
                                - componentHeight;
                            break;
                        }
                    }

                    // Set the component's size and position
                    component.setSize(componentWidth, componentHeight);
                    component.setLocation(componentX, componentY);

                    // Ensure that the component is visible
                    component.setVisible(true);

                    // Increment the x-coordinate
                    componentX += componentWidth + spacing;
                } else {
                    // Hide the component
                    component.setVisible(false);
                }
            }
        } else {
            int preferredHeight = (fill) ? getPreferredHeight(width) : getPreferredHeight(-1);

            // Determine the starting y-coordinate
            int componentY = 0;

            switch (verticalAlignment) {
                case CENTER: {
                    componentY = (int)Math.round((double)(height - preferredHeight) / 2);
                    break;
                }

                case BOTTOM: {
                    componentY = height - preferredHeight;
                    break;
                }
            }

            componentY += padding.top;

            // Lay out the components
            for (int i = 0; i < n; i++) {
                Component component = boxPane.get(i);

                if (component.isDisplayable()) {
                    int componentWidth = 0;
                    int componentHeight = 0;
                    int componentX = 0;

                    if (fill) {
                        componentX = padding.left;
                        componentWidth = Math.max(width - (padding.left
                            + padding.right), 0);
                        componentHeight = component.getPreferredHeight(componentWidth);
                    } else {
                        Dimensions preferredComponentSize = component.getPreferredSize();
                        componentWidth = preferredComponentSize.width;
                        componentHeight = preferredComponentSize.height;
                    }

                    switch (horizontalAlignment) {
                        case LEFT: {
                            componentX = padding.left;
                            break;
                        }

                        case CENTER: {
                            componentX = (int)Math.round((double)(width - componentWidth) / 2);
                            break;
                        }

                        case RIGHT: {
                            componentX = width - padding.right
                                - componentWidth;
                            break;
                        }
                    }

                    // Set the component's size and position
                    component.setSize(componentWidth, componentHeight);
                    component.setLocation(componentX, componentY);

                    // Ensure that the component is visible
                    component.setVisible(true);

                    // Increment the y-coordinate
                    componentY += componentHeight + spacing;
                } else {
                    // Hide the component
                    component.setVisible(false);
                }
            }
        }
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        this.horizontalAlignment = horizontalAlignment;
        invalidateComponent();
    }

    public final void setHorizontalAlignment(String horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        setHorizontalAlignment(HorizontalAlignment.valueOf(horizontalAlignment.toUpperCase()));
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        if (verticalAlignment == null) {
            throw new IllegalArgumentException("verticalAlignment is null.");
        }

        this.verticalAlignment = verticalAlignment;
        invalidateComponent();
    }

    public final void setVerticalAlignment(String verticalAlignment) {
        if (verticalAlignment == null) {
            throw new IllegalArgumentException("verticalAlignment is null.");
        }

        setVerticalAlignment(VerticalAlignment.valueOf(verticalAlignment.toUpperCase()));
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(int spacing) {
        this.spacing = spacing;
        invalidateComponent();
    }

    public final void setSpacing(Number spacing) {
        if (spacing == null) {
            throw new IllegalArgumentException("spacing is null.");
        }

        setSpacing(spacing.intValue());
    }

    public boolean getFill() {
        return fill;
    }

    public void setFill(boolean fill) {
        this.fill = fill;
        invalidateComponent();
    }

    // Box pane events
    public void orientationChanged(BoxPane boxPane) {
        invalidateComponent();
    }
}
