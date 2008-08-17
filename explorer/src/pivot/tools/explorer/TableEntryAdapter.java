package pivot.tools.explorer;

import java.util.Comparator;

import pivot.collections.Dictionary;

/**
 * Adapts dictionary entry (key/value pair) to be shown in the TableView
 * TableView should have two columns: name and value 
 * 
 * @author Eugene Ryzhikov
 * @date   Aug 16, 2008
 *
 */
public class TableEntryAdapter implements Dictionary<String, Object>{

	private Dictionary<String, Object> dictionary;
	private String entryName;

	public static final Comparator<TableEntryAdapter> COMPARATOR = new Comparator<TableEntryAdapter>() {

		public int compare(TableEntryAdapter o1, TableEntryAdapter o2) {

			if ( o1 == o2 ) return 0;
			if ( o1 == null ) return -1;
			if ( o2 == null ) return 1;

			return o1.getName().compareTo(o2.getName());

		}

	};


	public TableEntryAdapter( Dictionary<String, Object> dictionary, String entryName ) {

		if ( dictionary == null ) {
			throw new IllegalArgumentException( "Dictionary is null");
		}

		if ( entryName == null || entryName.trim().length() == 0 ) {
			throw new IllegalArgumentException( "Entry name is empty");
		}

		this.dictionary = dictionary;
		this.entryName = entryName;

	}

	protected String getName() {
		return entryName;
	}

	public Object get(String key) {

		switch( asColumn( key )) {
			case NAME : return getName();
			case VALUE: return dictionary.get(getName());
			default   : return null;
		}
	}

	public Object put(String key, Object value) {
		return PropertySheetColumn.VALUE == asColumn( key )? dictionary.put(getName(), value): null;
	}



	protected PropertySheetColumn asColumn( String key ) {
		return PropertySheetColumn.valueOf( key == null? "": key.toUpperCase());
	}

	public boolean containsKey(String key) {
		return asColumn( key ) != null;
	}

	public boolean isEmpty() {
		return false;
	}

	public Object remove(String key) {
		return null;
	}

}