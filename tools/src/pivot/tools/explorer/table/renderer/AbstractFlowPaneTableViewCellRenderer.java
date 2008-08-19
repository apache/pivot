package pivot.tools.explorer.table.renderer;

import java.awt.Color;
import java.awt.Font;

import pivot.collections.Dictionary;
import pivot.wtk.Component;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Insets;
import pivot.wtk.TableView;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.TableView.Column;

public abstract class AbstractFlowPaneTableViewCellRenderer extends FlowPane implements TableView.CellRenderer {


	private Component styleComponent = this;

	public AbstractFlowPaneTableViewCellRenderer() {
		super();

        StyleDictionary styles = getStyles();
		styles.put("verticalAlignment", VerticalAlignment.CENTER);
        styles.put("horizontalAlignment", HorizontalAlignment.LEFT);
        styles.put("padding", new Insets(2));

	}

	protected void setStyleComponent( Component component ) {
		styleComponent = component;
	}

	@Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

	@SuppressWarnings("unchecked")
	protected final Object getCellData( Object value, Column column ) {
		String columnName = column.getName();
        if (columnName != null && (value instanceof Dictionary)) {
            return ((Dictionary<String, Object>)value).get(columnName);
        } else {
        	return null;
        }
	}

	protected final void renderStyles(TableView tableView, boolean rowSelected, boolean rowDisabled) {
        Component.StyleDictionary tableViewStyles = tableView.getStyles();
        Component.StyleDictionary styles = styleComponent.getStyles();

        Object font = tableViewStyles.get("font");

        if (font instanceof Font) {
            styles.put("font", font);
        }

        Object color = null;

        if (tableView.isEnabled() && !rowDisabled) {
            if (rowSelected) {
                if (tableView.isFocused()) {
                    color = tableViewStyles.get("selectionColor");
                } else {
                    color = tableViewStyles.get("inactiveSelectionColor");
                }
            } else {
                color = tableViewStyles.get("color");
            }
        } else {
            color = tableViewStyles.get("disabledColor");
        }

        if (color instanceof Color) {
            styles.put("color", color);
        }
    }

}
