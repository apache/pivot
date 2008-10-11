package pivot.tools.explorer.tree.renderer;

import java.awt.Color;
import java.awt.Font;

import pivot.tools.explorer.ComponentAdapter;
import pivot.wtk.Component;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.TreeView;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.media.Image;

public class ComponentNodeRenderer extends FlowPane implements TreeView.NodeRenderer {
    protected ImageView imageView = new ImageView();
    protected Label label = new Label();

    public static final int DEFAULT_ICON_WIDTH = 16;
    public static final int DEFAULT_ICON_HEIGHT = 16;
    public static boolean DEFAULT_SHOW_ICON = true;

    public ComponentNodeRenderer() {
        getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

        add(imageView);
        add(label);

        imageView.setPreferredSize(DEFAULT_ICON_WIDTH, DEFAULT_ICON_HEIGHT);
        imageView.setDisplayable(DEFAULT_SHOW_ICON);

        setPreferredHeight(DEFAULT_ICON_HEIGHT);
    }

    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    @SuppressWarnings("unchecked")
    public void render(Object node, TreeView treeView, boolean expanded,
        boolean selected, boolean highlighted, boolean disabled) {
        ComponentAdapter componentAdapter = (ComponentAdapter)node;
        Image icon = componentAdapter.getIcon();
        String text = componentAdapter.getText();

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
            }
        }
    }

    public int getIconWidth() {
        return imageView.getPreferredWidth(-1);
    }

    public void setIconWidth(int iconWidth) {
        if (iconWidth == -1) {
            throw new IllegalArgumentException();
        }

        imageView.setPreferredWidth(iconWidth);
    }

    public int getIconHeight() {
        return imageView.getPreferredHeight(-1);
    }

    public void setIconHeight(int iconHeight) {
        if (iconHeight == -1) {
            throw new IllegalArgumentException();
        }

        imageView.setPreferredHeight(iconHeight);
    }

    public boolean getShowIcon() {
        return imageView.isDisplayable();
    }

    public void setShowIcon(boolean showIcon) {
        imageView.setDisplayable(showIcon);
    }
}
