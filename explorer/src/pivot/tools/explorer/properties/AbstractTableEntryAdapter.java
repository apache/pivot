package pivot.tools.explorer.properties;

import pivot.collections.Dictionary;
import pivot.wtk.Component;

public abstract class AbstractTableEntryAdapter implements Dictionary<String, Object>{

	private Component component;
	private String entryName;
	
	public AbstractTableEntryAdapter( Component component, String entryName ) {
		
		if ( component == null ) {
			throw new IllegalArgumentException( "Component cannot be null");
		}

		if ( entryName == null || entryName.trim().length() == 0 ) {
			throw new IllegalArgumentException( "Entry name cannot be empty");
		}
		
		this.component = component;
		this.entryName = entryName;
	}

	protected String getName() {
		return entryName; 
	}
	
	protected Component getComponent() {
		return component;
	}
	
	protected PropertySheetColumn asColumn( String key ) {
		return PropertySheetColumn.valueOf( key == null? "": key.toUpperCase());
	}
	
	@Override
	public boolean containsKey(String key) {
		return asColumn( key ) != null;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public Object remove(String key) {
		return null;
	}

}