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
package pivot.wtk.content;

import java.awt.Color;
import java.awt.Font;
import java.net.URL;

import pivot.collections.Dictionary;
import pivot.collections.Map;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.Renderer;
import pivot.wtk.TreeView;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.media.Image;

public class TreeViewNodeRenderer extends FlowPane implements TreeView.NodeRenderer {
    private class PropertyDictionary extends Renderer.PropertyDictionary {
        public Object get(String key) {
            if (key == null) {
                throw new IllegalArgumentException("key is null.");
            }

            Object value = null;

            if (key.equals(ICON_WIDTH_KEY)) {
                value = getIconWidth();
            } else if (key.equals(ICON_HEIGHT_KEY)) {
                value = getIconHeight();
            } else if (key.equals(ICON_SIZE_KEY)) {
                value = getIconSize();
            } else if (key.equals(SHOW_ICON_KEY)) {
                value = getShowIcon();
            } else {
                // No-op
            }

            return value;
        }

        @SuppressWarnings("unchecked")
        public Object put(String key, Object value) {
            if (key == null) {
                throw new IllegalArgumentException("key is null.");
            }

            Object previousValue = null;

            if (key.equals(ICON_SIZE_KEY)) {
                if (value instanceof Map<?, ?>) {
                    value = new Dimensions((Map<String, Object>)value);
                }

                Dimensions dimensions = (Dimensions)value;
                setIconSize(dimensions.width, dimensions.height);
            } else if (key.equals(SHOW_ICON_KEY)) {
                setShowIcon((Boolean)value);
            } else {
                System.out.println("\"" + key + "\" is not a valid property for "
                    + getClass().getName() + ".");
            }

            return previousValue;
        }

        public Object remove(String key) {
            if (key == null) {
                throw new IllegalArgumentException("key is null.");
            }

            Object previousValue = null;

            if (key.equals(ICON_SIZE_KEY)) {
                previousValue = put(key, new Dimensions(DEFAULT_ICON_WIDTH, DEFAULT_ICON_HEIGHT));
            } else if (key.equals(SHOW_ICON_KEY)) {
                previousValue = put(key, DEFAULT_SHOW_ICON);
            } else {
                // No-op
            }

            return previousValue;
        }

        public boolean containsKey(String key) {
            if (key == null) {
                throw new IllegalArgumentException("key is null.");
            }

            return (key.equals(ICON_WIDTH_KEY)
                || key.equals(ICON_HEIGHT_KEY)
                || key.equals(ICON_SIZE_KEY)
                || key.equals(SHOW_ICON_KEY));
        }

        public boolean isEmpty() {
            return false;
        }
    }

    protected ImageView imageView = new ImageView();
    protected Label label = new Label();

    private PropertyDictionary properties = new PropertyDictionary();

    public static final String ICON_URL_KEY = "iconURL";
    public static final String LABEL_KEY = "label";

    public static final String ICON_WIDTH_KEY = "iconWidth";
    public static final String ICON_HEIGHT_KEY = "iconHeight";
    public static final String ICON_SIZE_KEY = "iconSize";
    public static final String SHOW_ICON_KEY = "showIcon";

    public static final int DEFAULT_ICON_WIDTH = 16;
    public static final int DEFAULT_ICON_HEIGHT = 16;
    public static boolean DEFAULT_SHOW_ICON = true;

    public TreeViewNodeRenderer() {
        super();

        getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

        getComponents().add(imageView);
        getComponents().add(label);

        imageView.setPreferredSize(DEFAULT_ICON_WIDTH, DEFAULT_ICON_HEIGHT);
        imageView.setDisplayable(DEFAULT_SHOW_ICON);

        setPreferredHeight(DEFAULT_ICON_HEIGHT);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    @SuppressWarnings("unchecked")
    public void render(Object node, TreeView treeView, boolean expanded,
        boolean selected, boolean highlighted, boolean disabled) {
        Image icon = null;
        String text = null;

        if (node instanceof TreeNode) {
            TreeNode treeNode = (TreeNode)node;
            icon = expanded ? treeNode.getExpandedIcon() : treeNode.getIcon();
            text = treeNode.getLabel();
        } else if (node instanceof Dictionary) {
            Dictionary<String, Object> dictionary = (Dictionary<String, Object>)node;

            URL iconURL = (URL)dictionary.get(ICON_URL_KEY);
            if (iconURL != null) {
                ApplicationContext applicationContext = ApplicationContext.getInstance();
                icon = (Image)applicationContext.getResources().get(iconURL);

                if (icon == null) {
                    icon = Image.load(iconURL);
                    applicationContext.getResources().put(iconURL, icon);
                }
            }

            text = (String)dictionary.get(LABEL_KEY);
        } else if (node instanceof Image) {
            icon = (Image)node;
        } else {
            text = (node == null) ? null : node.toString();
        }

        // Update the image view
        imageView.setImage(icon);
        imageView.getStyles().put("opacity",
            (treeView.isEnabled() && !disabled) ? 1.0f : 0.5f);

        // Show/hide the label
        if (text == null) {
            label.setDisplayable(false);
        } else {
            label.setDisplayable(true);
            label.setText(text);

            // Update the label styles
            Component.StyleDictionary labelStyles = label.getStyles();

            Object labelFont = treeView.getStyles().get("font");
            if (labelFont instanceof Font) {
                labelStyles.put("font", labelFont);
            } else {
                labelStyles.remove("font");
            }

            Object color = null;
            if (treeView.isEnabled() && !disabled) {
                if (selected) {
                    if (treeView.isFocused()) {
                        color = treeView.getStyles().get("selectionColor");
                    } else {
                        color = treeView.getStyles().get("inactiveSelectionColor");
                    }
                } else {
                    color = treeView.getStyles().get("color");
                }
            } else {
                color = treeView.getStyles().get("disabledColor");
            }

            if (color instanceof Color) {
                labelStyles.put("color", color);
            } else {
                labelStyles.remove("color");
            }
        }
    }

    public int getIconWidth() {
        return imageView.getPreferredWidth(-1);
    }

    public int getIconHeight() {
        return imageView.getPreferredHeight(-1);
    }

    public Dimensions getIconSize() {
        return new Dimensions(getIconWidth(), getIconHeight());
    }

    public void setIconSize(int width, int height) {
        imageView.setPreferredSize(width, height);
    }

    public boolean getShowIcon() {
        return imageView.isDisplayable();
    }

    public void setShowIcon(boolean showIcon) {
        imageView.setDisplayable(showIcon);
    }

    public PropertyDictionary getProperties() {
        return properties;
    }
}
