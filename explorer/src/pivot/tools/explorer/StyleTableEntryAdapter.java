/**
 * 
 */
package pivot.tools.explorer;

import pivot.tools.explorer.properties.AbstractTableEntryAdapter;
import pivot.tools.explorer.properties.PropertySheetColumn;
import pivot.wtk.Component;
import pivot.wtk.Component.StyleDictionary;

class StyleTableEntryAdapter extends AbstractTableEntryAdapter {
	
	public StyleTableEntryAdapter(Component component, String styleName) {
		super(component, styleName );
	}

	@Override
	public Object get(String key) {
		
		switch( asColumn( key )) {
			case NAME : return getName();
			case VALUE: return getStyles().get(getName());
			default   : return null;
		}
	}

	private StyleDictionary getStyles() {
		return getComponent().getStyles();
	}

	@Override
	public Object put(String key, Object value) {
		return PropertySheetColumn.VALUE == asColumn( key )? getStyles().put(getName(), value): null;
	}
	
}