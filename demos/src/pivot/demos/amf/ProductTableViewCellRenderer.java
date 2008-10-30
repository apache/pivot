package pivot.demos.amf;

import java.awt.Color;
import java.awt.Font;

import pivot.beans.BeanDictionary;
import pivot.wtk.Label;
import pivot.wtk.TableView;

public class ProductTableViewCellRenderer extends Label
	implements TableView.CellRenderer {
	public ProductTableViewCellRenderer() {
		getStyles().put("padding", 3);
	}

    public void render(Object value, TableView tableView, TableView.Column column,
        boolean rowSelected, boolean rowHighlighted, boolean rowDisabled) {
    	Object cellData = null;

        // Get the row and cell data
        String columnName = column.getName();
        if (columnName != null) {
        	BeanDictionary rowData = new BeanDictionary(value);
            cellData = rowData.get(columnName);
        }

        setText(cellData == null ? null : cellData.toString());

        Object font = tableView.getStyles().get("font");
        if (font instanceof Font) {
            getStyles().put("font", font);
        }

        Object color = null;

        if (tableView.isEnabled() && !rowDisabled) {
            if (rowSelected) {
                if (tableView.isFocused()) {
                    color = tableView.getStyles().get("selectionColor");
                } else {
                    color = tableView.getStyles().get("inactiveSelectionColor");
                }
            } else {
                color = tableView.getStyles().get("color");
            }
        } else {
            color = tableView.getStyles().get("disabledColor");
        }

        if (color instanceof Color) {
            getStyles().put("color", color);
        }
    }
}
