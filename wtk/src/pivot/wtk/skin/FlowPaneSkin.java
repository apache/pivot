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

import pivot.collections.Dictionary;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.FlowPane;
import pivot.wtk.FlowPaneListener;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Insets;
import pivot.wtk.Orientation;
import pivot.wtk.VerticalAlignment;

/**
 * <p>Flow pane skin.</p>
 *
 * @author gbrown
 */
public class FlowPaneSkin extends ContainerSkin
    implements FlowPaneListener {
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
    private VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
    private Insets padding = new Insets(0);
    private int spacing = 4;

    @Override
    public void install(Component component) {
        validateComponentType(component, FlowPane.class);

        super.install(component);
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        // Include padding in constraint
        if (height != -1) {
            height = Math.max(height - (padding.top + padding.bottom), 0);
        }

        FlowPane flowPane = (FlowPane)getComponent();
        int n = flowPane.getLength();

        Orientation orientation = flowPane.getOrientation();
        if (orientation == Orientation.HORIZONTAL) {
            // Preferred width is the sum of the preferred widths of all
            // components, plus spacing
            int displayableComponentCount = 0;

            for (int i = 0; i < n; i++) {
                Component component = flowPane.get(i);

                if (component.isDisplayable()) {
                    preferredWidth += component.getPreferredWidth(height);
                    displayableComponentCount++;
                }
            }

            if (displayableComponentCount > 1) {
                preferredWidth += spacing * (displayableComponentCount - 1);
            }
        } else {
            // Preferred width is the maximum preferred width of all components
            int maxComponentWidth = 0;

            // Determine the fixed and total preferred heights, if necessary
            int totalSpacing = 0;
            int totalPreferredHeight = 0;

            if (horizontalAlignment == HorizontalAlignment.JUSTIFY
                && height != -1) {
                int displayableComponentCount = 0;
                for (int i = 0; i < n; i++) {
                    Component component = flowPane.get(i);

                    if (component.isDisplayable()) {
                        totalPreferredHeight += component.getPreferredHeight(-1);
                        displayableComponentCount++;
                    }
                }

                if (displayableComponentCount > 1) {
                    totalSpacing = spacing * (displayableComponentCount - 1);
                }
            }

            for (int i = 0; i < n; i++) {
                Component component = flowPane.get(i);

                if (component.isDisplayable()) {
                    int componentHeight = -1;

                    if (verticalAlignment == VerticalAlignment.JUSTIFY
                        && height != -1) {
                        int preferredHeight = component.getPreferredHeight(-1);

                        if (height > totalSpacing
                            && preferredHeight > totalSpacing) {
                            double heightScale = (double)preferredHeight
                                / (double)totalPreferredHeight;

                            componentHeight = (int)Math.round((double)(height
                                - totalSpacing) * heightScale);
                        } else {
                            componentHeight = 0;
                        }
                    }

                    maxComponentWidth = Math.max(maxComponentWidth,
                        component.getPreferredWidth(componentHeight));
                }
            }

            preferredWidth += maxComponentWidth;
        }

        // Include left and right padding values
        preferredWidth += padding.left + padding.right;

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        // Include padding in constraint
        if (width != -1) {
            width = Math.max(width - (padding.left + padding.right), 0);
        }

        FlowPane flowPane = (FlowPane)getComponent();
        int n = flowPane.getLength();

        Orientation orientation = flowPane.getOrientation();
        if (orientation == Orientation.VERTICAL) {
            // Preferred height is the sum of the preferred heights of all
            // components, plus padding and spacing
            int displayableComponentCount = 0;

            for (int i = 0; i < n; i++) {
                Component component = flowPane.get(i);

                if (component.isDisplayable()) {
                    preferredHeight += component.getPreferredHeight(width);
                    displayableComponentCount++;
                }
            }

            if (displayableComponentCount > 1) {
                preferredHeight += spacing * (displayableComponentCount - 1);
            }
        } else {
            // Preferred height is the maximum preferred height of all
            // components, plus padding
            int maxComponentHeight = 0;

            // Determine the fixed and total preferred widths, if necessary
            int totalSpacing = 0;
            int totalPreferredWidth = 0;

            if (horizontalAlignment == HorizontalAlignment.JUSTIFY
                && width != -1) {
                int displayableComponentCount = 0;

                for (int i = 0; i < n; i++) {
                    Component component = flowPane.get(i);

                    if (component.isDisplayable()) {
                        totalPreferredWidth += component.getPreferredWidth(-1);
                        displayableComponentCount++;
                    }
                }

                if (displayableComponentCount > 1) {
                    totalSpacing = spacing * (displayableComponentCount - 1);
                }
            }

            for (int i = 0; i < n; i++) {
                Component component = flowPane.get(i);

                if (component.isDisplayable()) {
                    int componentWidth = -1;

                    if (horizontalAlignment == HorizontalAlignment.JUSTIFY
                        && width != -1) {
                        int preferredWidth = component.getPreferredWidth(-1);

                        if (width > totalSpacing
                            && preferredWidth > totalSpacing) {
                            double widthScale = (double)preferredWidth
                                / (double)totalPreferredWidth;

                            componentWidth = (int)Math.round((double)(width
                                - totalSpacing) * widthScale);
                        }
                    }

                    maxComponentHeight = Math.max(maxComponentHeight,
                        component.getPreferredHeight(componentWidth));
                }
            }

            preferredHeight += maxComponentHeight;
        }

        // Include top and bottom padding values
        preferredHeight += padding.top + padding.bottom;

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        // TODO Optimize by performing calculations here?
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        FlowPane flowPane = (FlowPane)getComponent();
        int n = flowPane.getLength();

        int width = getWidth();
        int height = getHeight();

        Orientation orientation = flowPane.getOrientation();
        if (orientation == Orientation.HORIZONTAL) {
            int preferredWidth = getPreferredWidth(height);

            // Determine the fixed width (used in scaling components
            // when justified horizontally)
            int fixedWidth = 0;
            if (horizontalAlignment == HorizontalAlignment.JUSTIFY) {
                fixedWidth = padding.left + padding.right;

                int displayableComponentCount = 0;

                for (int i = 0; i < n; i++) {
                    Component component = flowPane.get(i);

                    if (component.isDisplayable()) {
                        displayableComponentCount++;
                    }
                }

                if (displayableComponentCount > 1) {
                    fixedWidth += spacing * (displayableComponentCount - 1);
                }
            }

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
                Component component = flowPane.get(i);

                if (component.isDisplayable()) {
                    int componentWidth = 0;
                    int componentHeight = 0;
                    int componentY = 0;

                    // If the contents are horizontally justified, scale the
                    // component's width to match the available space
                    if (horizontalAlignment == HorizontalAlignment.JUSTIFY) {
                        if (width > fixedWidth
                            && preferredWidth > fixedWidth) {
                            double widthScale = ((double)(width - fixedWidth)
                                / (double)(preferredWidth - fixedWidth));

                            componentWidth = (int)Math.max(Math.round((double)component.getPreferredWidth(-1)
                                * widthScale), 0);

                            if (verticalAlignment == VerticalAlignment.JUSTIFY) {
                                componentY = padding.top;
                                componentHeight = Math.max(height - (padding.top
                                    + padding.bottom), 0);
                            } else {
                                componentHeight = component.getPreferredHeight(componentWidth);
                            }
                        }
                    } else {
                        if (verticalAlignment == VerticalAlignment.JUSTIFY) {
                            componentY = padding.top;
                            componentHeight = Math.max(height - (padding.top
                                + padding.bottom), 0);
                            componentWidth = component.getPreferredWidth(componentHeight);
                        } else {
                            Dimensions preferredComponentSize = component.getPreferredSize();
                            componentWidth = preferredComponentSize.width;
                            componentHeight = preferredComponentSize.height;
                        }
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
            int preferredHeight = getPreferredHeight(width);

            // Determine the fixed height (used in scaling components
            // when justified vertically)
            int fixedHeight = 0;
            if (verticalAlignment == VerticalAlignment.JUSTIFY) {
                fixedHeight = padding.top + padding.bottom;

                int displayableComponentCount = 0;

                for (int i = 0; i < n; i++) {
                    Component component = flowPane.get(i);

                    if (component.isDisplayable()) {
                        displayableComponentCount++;
                    }
                }

                if (displayableComponentCount > 1) {
                    fixedHeight += spacing * (displayableComponentCount - 1);
                }
            }

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
                Component component = flowPane.get(i);

                if (component.isDisplayable()) {
                    int componentWidth = 0;
                    int componentHeight = 0;
                    int componentX = 0;

                    if (horizontalAlignment == HorizontalAlignment.JUSTIFY) {
                        componentX = padding.left;
                        componentWidth = Math.max(width - (padding.left
                            + padding.right), 0);
                        componentHeight = component.getPreferredHeight(componentWidth);
                    } else {
                    }

                    // If the contents are vertically justified, scale the
                    // component's height to match the available space
                    if (verticalAlignment == VerticalAlignment.JUSTIFY) {
                        if (height > fixedHeight
                            && preferredHeight > fixedHeight) {
                            double heightScale = (double)(height - fixedHeight)
                                / (double)(preferredHeight - fixedHeight);

                            componentHeight = (int)Math.max(Math.round((double)component.getPreferredHeight(-1)
                                * heightScale), 0);

                            if (horizontalAlignment == HorizontalAlignment.JUSTIFY) {
                                componentX = padding.left;
                                componentWidth = Math.max(width - (padding.left
                                    + padding.right), 0);
                            } else {
                                componentWidth = component.getPreferredWidth(componentHeight);
                            }
                        }
                    } else {
                        if (horizontalAlignment == HorizontalAlignment.JUSTIFY) {
                            componentX = padding.left;
                            componentWidth = Math.max(width - (padding.left
                                + padding.right), 0);
                            componentHeight = component.getPreferredHeight(componentWidth);
                        } else {
                            Dimensions preferredComponentSize = component.getPreferredSize();
                            componentWidth = preferredComponentSize.width;
                            componentHeight = preferredComponentSize.height;
                        }
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

        setHorizontalAlignment(HorizontalAlignment.decode(horizontalAlignment));
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

        setVerticalAlignment(VerticalAlignment.decode(verticalAlignment));
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

    // Flow pane events
    public void orientationChanged(FlowPane flowPane) {
        invalidateComponent();
    }
}
