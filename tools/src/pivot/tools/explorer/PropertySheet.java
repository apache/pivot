package pivot.tools.explorer;

import pivot.collections.List;
import pivot.wtk.TableView;

public class PropertySheet extends TableView {

	public PropertySheet() {
		super();
	}

	public PropertySheet(List<?> tableData) {
		super(tableData);
	}

	@Override
	public ColumnSequence getColumns() {
		ColumnSequence columns = super.getColumns();
		if ( columns.getLength() == 0 ) {
			columns.add( new Column("Name",  "Name",  150 ));
			columns.add( new Column("Value", "Value", 150, true ));
		}
		return columns;
	}


}
