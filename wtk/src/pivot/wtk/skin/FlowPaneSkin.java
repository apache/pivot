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

import pivot.collections.Map;
import pivot.wtk.Component;
import pivot.wtk.Container;
import pivot.wtk.Dimensions;
import pivot.wtk.FlowPane;
import pivot.wtk.FlowPaneListener;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Insets;
import pivot.wtk.Orientation;
import pivot.wtk.VerticalAlignment;

/**
 * TODO Add support for "direction" style (an instance of Direction)
 *
 * @author gbrown
 *
 */
public class FlowPaneSkin extends ContainerSkin
    implements FlowPaneListener {
    protected HorizontalAlignment horizontalAlignment = DEFAULT_HORIZONTAL_ALIGNMENT;
    protected VerticalAlignment verticalAlignment = DEFAULT_VERTICAL_ALIGNMENT;
    protected Insets padding = DEFAULT_PADDING;
    protected int spacing = DEFAULT_SPACING;

    protected static final String HORIZONTAL_ALIGNMENT_KEY = "horizontalAlignment";
    protected static final String VERTICAL_ALIGNMENT_KEY = "verticalAlignment";
    protected static final String PADDING_KEY = "padding";
    protected static final String SPACING_KEY = "spacing";

    private static final HorizontalAlignment DEFAULT_HORIZONTAL_ALIGNMENT = HorizontalAlignment.LEFT;
    private static final VerticalAlignment DEFAULT_VERTICAL_ALIGNMENT = VerticalAlignment.TOP;
    private static final Insets DEFAULT_PADDING = new Insets(0);
    private static final int DEFAULT_SPACING = 4;

    public FlowPaneSkin() {
        super();
    }

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
        Container.ComponentSequence components = flowPane.getComponents();
        int n = components.getLength();

        Orientation orientation = flowPane.getOrientation();
        if (orientation == Orientation.HORIZONTAL) {
            // Preferred width is the sum of the preferred widths of all
            // components, plus spacing
            int displayableComponentCount = 0;

            for (int i = 0; i < n; i++) {
                Component component = components.get(i);

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
                    Component component = components.get(i);

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
                Component component = components.get(i);

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
        Container.ComponentSequence components = flowPane.getComponents();
        int n = components.getLength();

        Orientation orientation = flowPane.getOrientation();
        if (orientation == Orientation.VERTICAL) {
            // Preferred height is the sum of the preferred heights of all
            // components, plus padding and spacing
            int displayableComponentCount = 0;

            for (int i = 0; i < n; i++) {
                Component component = components.get(i);

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
                    Component component = components.get(i);

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
                Component component = components.get(i);

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
        Container.ComponentSequence components = flowPane.getComponents();
        int n = components.getLength();

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
                    Component component = components.get(i);

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
                Component component = components.get(i);

                if (component.isDisplayable()) {
                    int componentWidth = 0;
                    int componentHeight = 0;
                    int componentY = 0;

                    if (verticalAlignment == VerticalAlignment.JUSTIFY) {
                        componentY = padding.top;
                        componentHeight = Math.max(height - (padding.top
                            + padding.bottom), 0);
                        componentWidth = component.getPreferredWidth(componentHeight);
                    } else {
                        Dimensions preferredComponentSize = component.getPreferredSize();

                        componentWidth = preferredComponentSize.width;
                        componentHeight = preferredComponentSize.height;

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
                    }

                    // Set the component's position
                    component.setLocation(componentX, componentY);

                    // If the contents are horizontally justified, scale the
                    // component's width to match the available space
                    if (horizontalAlignment == HorizontalAlignment.JUSTIFY) {
                        if (width > fixedWidth
                            && preferredWidth > fixedWidth) {
                            double widthScale = ((double)(width - fixedWidth)
                                / (double)(preferredWidth - fixedWidth));

                            componentWidth = (int)Math.max(Math.round((double)componentWidth
                                * widthScale), 0);

                            if (verticalAlignment != VerticalAlignment.JUSTIFY) {
                                componentHeight = component.getPreferredHeight(componentWidth);
                            }
                        } else {
                            componentWidth = 0;
                        }
                    }

                    // Set the component's size
                    component.setSize(componentWidth, componentHeight);

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
                    Component component = components.get(i);

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
                Component component = components.get(i);

                if (component.isDisplayable()) {
                    int componentWidth = 0;
                    int componentHeight = 0;
                    int componentX = 0;

                    if (horizontalAlignment == HorizontalAlignment.JUSTIFY) {
                        componentX = padding.left;
                        componentWidth = width - (padding.left
                            + padding.right);
                        componentHeight = component.getPreferredHeight(componentWidth);
                    } else {
                        Dimensions preferredComponentSize = component.getPreferredSize();

                        componentWidth = preferredComponentSize.width;
                        componentHeight = preferredComponentSize.height;

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
                    }

                    // Set the component's position
                    component.setLocation(componentX, componentY);

                    // If the contents are vertically justified, scale the
                    // component's height to match the available space
                    if (verticalAlignment == VerticalAlignment.JUSTIFY) {
                        if (height > fixedHeight
                            && preferredHeight > fixedHeight) {
                            double heightScale = (double)(height - fixedHeight)
                                / (double)(preferredHeight - fixedHeight);

                            componentHeight = (int)Math.max(Math.round((double)componentHeight
                                * heightScale), 0);

                            if (horizontalAlignment != HorizontalAlignment.JUSTIFY) {
                                componentWidth = component.getPreferredWidth(componentHeight);
                            }
                        } else {
                            componentHeight = 0;
                        }
                    }

                    // Set the component's size
                    component.setSize(componentWidth, componentHeight);

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

    @Override
    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        if (key.equals(HORIZONTAL_ALIGNMENT_KEY)) {
            value = horizontalAlignment;
        } else if (key.equals(VERTICAL_ALIGNMENT_KEY)) {
            value = verticalAlignment;
        } else if (key.equals(PADDING_KEY)) {
            value = padding;
        } else if (key.equals(SPACING_KEY)) {
            value = spacing;
        } else {
            value = super.get(key);
        }

        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(HORIZONTAL_ALIGNMENT_KEY)) {
            if (value instanceof String) {
                value = HorizontalAlignment.decode((String)value);
            }

            validatePropertyType(key, value, HorizontalAlignment.class, false);

            previousValue = horizontalAlignment;
            horizontalAlignment = (HorizontalAlignment)value;

            invalidateComponent();
        } else if (key.equals(VERTICAL_ALIGNMENT_KEY)) {
            if (value instanceof String) {
                value = VerticalAlignment.decode((String)value);
            }

            validatePropertyType(key, value, VerticalAlignment.class, false);

            previousValue = verticalAlignment;
            verticalAlignment = (VerticalAlignment)value;

            invalidateComponent();
        } else if (key.equals(PADDING_KEY)) {
            if (value instanceof Number) {
                value = new Insets(((Number)value).intValue());
            } else {
                if (value instanceof Map<?, ?>) {
                    value = new Insets((Map<String, Object>)value);
                }
            }

            validatePropertyType(key, value, Insets.class, false);

            previousValue = padding;
            padding = (Insets)value;

            invalidateComponent();
        } else if (key.equals(SPACING_KEY)) {
            if (value instanceof Number) {
                value = ((Number)value).intValue();
            }

            validatePropertyType(key, value, Integer.class, false);

            previousValue = spacing;
            spacing = (Integer)value;

            invalidateComponent();
        } else {
            previousValue = super.put(key, value);
        }

        return previousValue;
    }

    @Override
    public Object remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(HORIZONTAL_ALIGNMENT_KEY)) {
            previousValue = put(key, DEFAULT_HORIZONTAL_ALIGNMENT);
        } else if (key.equals(VERTICAL_ALIGNMENT_KEY)) {
            previousValue = put(key, DEFAULT_VERTICAL_ALIGNMENT);
        } else if (key.equals(PADDING_KEY)) {
            previousValue = put(key, DEFAULT_PADDING);
        } else if (key.equals(SPACING_KEY)) {
            previousValue = put(key, DEFAULT_SPACING);
        } else {
            previousValue = super.remove(key);
        }

        return previousValue;
    }

    @Override
    public boolean containsKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        return (key.equals(HORIZONTAL_ALIGNMENT_KEY)
            || key.equals(VERTICAL_ALIGNMENT_KEY)
            || key.equals(PADDING_KEY)
            || key.equals(SPACING_KEY)
            || super.containsKey(key));
    }

    // Flow pane events
    public void orientationChanged(FlowPane flowPane) {
        invalidateComponent();
    }
}
