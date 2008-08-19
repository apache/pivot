package pivot.tools.explorer.table.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Date;

import pivot.collections.HashMap;
import pivot.collections.Map;
import pivot.tools.explorer.TableEntryAdapter;
import pivot.wtk.TableView;
import pivot.wtk.TableView.Column;
import pivot.wtk.content.TableViewCellRenderer;
import pivot.wtk.content.TableViewDateCellRenderer;

public class PropertyValueTableViewCellRenderer extends TableViewCellRenderer {

	private Map<Class<?>, TableView.CellRenderer> map = new HashMap<Class<?>, TableView.CellRenderer>();
	private TableView.CellRenderer defaultRenderer = new TableViewCellRenderer();
	private TableView.CellRenderer lastRenderer = defaultRenderer;

	public PropertyValueTableViewCellRenderer() {
		super();

//		map.put(Boolean.class, new TableViewBooleanCellRenderer());
		map.put(Date.class, new TableViewDateCellRenderer());
		map.put(Color.class, new ColorTablewViewCellRenderer());

//		TableViewNumberCellRenderer numberRenderer = new TableViewNumberCellRenderer();
//		map.put(Byte.class, numberRenderer);
//		map.put(Short.class, numberRenderer);
//		map.put(Integer.class, numberRenderer);
//		map.put(Long.class, numberRenderer);
//		map.put(Float.class, numberRenderer);
//		map.put(Double.class, numberRenderer);
//		map.put(BigInteger.class, numberRenderer);
//		map.put(BigDecimal.class, numberRenderer);


	}


	@Override
	public void render(
			Object value,
			TableView tableView,
			Column column,
			boolean rowSelected,
			boolean rowHighlighted,
			boolean rowDisabled) {

		lastRenderer = getRenderer(value);
		lastRenderer.render(value, tableView, column, rowSelected, rowHighlighted, rowDisabled);

	}

	private TableView.CellRenderer getRenderer(Object value) {
		if (value instanceof TableEntryAdapter) {
			Object propertyValue = ((TableEntryAdapter)value).getValue();
			if ( propertyValue != null ) {
				TableView.CellRenderer renderer = map.get(propertyValue.getClass());
				if ( renderer != null ) return renderer;
			}
		}
		return defaultRenderer;
	}

	@Override
	public void paint(Graphics2D graphics) {
		lastRenderer.paint(graphics);
	}

	@Override
	public void setSize(int width, int height) {
		lastRenderer.setSize(width, height);
	}

}
