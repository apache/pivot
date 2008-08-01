package pivot.tools.explorer.tools;

/**
 * String utilities
 * This class may be later replaced with more common one such as the one from  Jakarta Commons 
 * 
 * @author Eugene Ryzhikov
 * @date   Aug 1, 2008
 *
 */
public final class Strings {

	public static final <T> String createDelimited( Iterable<T> data, String delimiter ) {
		
		if ( data == null ) return "";
		
		StringBuilder sb = new StringBuilder();
		for( T item: data ) {
			if ( sb.length() > 0) sb.append("/");
			sb.append(item);
		}
        return sb.toString();
		
	}
	
}
