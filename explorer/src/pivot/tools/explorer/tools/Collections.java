package pivot.tools.explorer.tools;

import pivot.collections.ArrayList;
import pivot.collections.List;

public final class Collections {

	public static <T> List<T> emptyList() {
		return new ArrayList<T>(0);
	}

	public static final <T> List<T> list( T... items ) {
		return new ArrayList<T>( items );
	}

}
