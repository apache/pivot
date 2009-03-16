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

/**
 * Base for renderers which use FlowPane
 *
 * @author Eugene Ryzhikov
 * @date   Aug 19, 2008
 *
 */
public abstract class AbstractFlowPaneTableViewCellRenderer extends FlowPane implements TableView.CellRenderer {

	public AbstractFlowPaneTableViewCellRenderer() {
		super();

        StyleDictionary styles = getStyles();
		styles.put("verticalAlignment", VerticalAlignment.CENTER);
        styles.put("horizontalAlignment", HorizontalAlignment.LEFT);
        styles.put("padding", new Insets(2));

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

	protected Component getStyleComponent() {
		return this;
	}

	public final void render(Object value, TableView tableView, Column column, boolean rowSelected, boolean rowHighlighted,
			boolean rowDisabled) {

		render(getCellData(value, column));
        renderStyles( getStyleComponent(), tableView, rowSelected, rowDisabled);

	}

	protected abstract void render( Object cellData );

	protected final void renderStyles( Component styleComponent, TableView tableView, boolean rowSelected, boolean rowDisabled) {
        Component.StyleDictionary tableViewStyles = tableView.getStyles();
        Component.StyleDictionary styles = styleComponent.getStyles();

        Object font = tableViewStyles.get("font");

        if (font instanceof Font) {
            styles.put("font", font);
        }


        String styleKey = "disabledColor";
        if (tableView.isEnabled() && !rowDisabled) {
            if (rowSelected) {
            	styleKey = tableView.isFocused()? "selectionColor": "inactiveSelectionColor";
            } else {
            	styleKey = "color";
            }
        }

        Object color = tableViewStyles.get( styleKey );

        if (color instanceof Color) {
            styles.put("color", color);
        }
    }

}
