package pivot.tools.explorer.utils;

import pivot.collections.ArrayList;
import pivot.collections.List;

public final class Collections {

	public static final <T> List<T> list( T... items ) {
		return new ArrayList<T>( items );
	}

}
