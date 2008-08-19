/**
 *
 */
package pivot.tools.explorer.table.renderer;

import pivot.wtk.Checkbox;
import pivot.wtk.Component;

class BooleanPropertyCelRenderer extends AbstractFlowPaneTableViewCellRenderer {

	private Checkbox checkbox = new Checkbox();

	public BooleanPropertyCelRenderer() {
		super();
		add(checkbox);
	}

	protected Component getStyleComponent() {
		return checkbox;
	}

	@Override
	protected void render(Object cellData) {
		if ( cellData instanceof Boolean ) {
			checkbox.setSelected( ((Boolean)cellData).booleanValue());
		}
	}


}