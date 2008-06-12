package pivot.tutorials.stocktracker;

import pivot.collections.Dictionary;
import pivot.wtk.TableView;
import pivot.wtk.content.TableViewNumberCellRenderer;

public class ChangeCellRenderer extends TableViewNumberCellRenderer {
    @SuppressWarnings("unchecked")
    public void render(Object value, TableView tableView, TableView.Column column,
        boolean rowSelected, boolean rowHighlighted, boolean rowDisabled) {
        super.render(value, tableView, column, rowSelected, rowHighlighted, rowDisabled);

        if (!rowSelected) {
            // Get the row and cell data
            String columnName = column.getName();
            Dictionary<String, Object> rowData = (Dictionary<String, Object>)value;
            Object cellData = rowData.get(columnName);

            getStyles().put("color", (Float)cellData < 0 ? "#ff0000" : "#008000");
        }
    }
}
