package pivot.tutorials.stocktracker;

import java.awt.Color;

import pivot.collections.Dictionary;
import pivot.wtk.TableView;
import pivot.wtk.content.TableViewNumberCellRenderer;

public class ChangeCellRenderer extends TableViewNumberCellRenderer {
    public static final Color UP_COLOR = new Color(0x00, 0x80, 0x00);
    public static final Color DOWN_COLOR = new Color(0xff, 0x00, 0x00);

    @SuppressWarnings("unchecked")
    public void render(Object value, TableView tableView, TableView.Column column,
        boolean rowSelected, boolean rowHighlighted, boolean rowDisabled) {
        super.render(value, tableView, column, rowSelected, rowHighlighted, rowDisabled);

        if (!rowSelected) {
            // Get the row and cell data
            String columnName = column.getName();
            Dictionary<String, Object> rowData = (Dictionary<String, Object>)value;
            Object cellData = rowData.get(columnName);

            getStyles().put("color", (Float)cellData < 0 ? DOWN_COLOR : UP_COLOR);
        }
    }
}
