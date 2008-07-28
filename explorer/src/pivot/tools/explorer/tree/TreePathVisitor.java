/**
 * 
 */
package pivot.tools.explorer.tree;

import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.wtk.TreeView;

/**
 * Represents the "closure" to help visit the nodes it the tree path.
 * Assumes that tree nodes implement Sequence interface of the same type
 * 
 * @author Eugene Ryzhikov
 * @date   Jul 24, 2008
 *
 * @param <T>
 */
public abstract class TreePathVisitor<T extends Sequence<? super T>> {
	

	/**
	 * Starts visiting process on treeView using path
	 * @param treeView
	 * @param path
	 */
	@SuppressWarnings("unchecked")
	public final void start( TreeView treeView, Sequence<Integer> path) {

		List<T> nodes =  (List<T>) treeView.getTreeData();
		
		for ( int i=0, s=path.getLength(); i< s; i++ ) {
			T node = nodes.get( path.get(i) );
			visit( node );
			if ( node instanceof List<?> ) {
				nodes = (List<T>)node;
			}
		}
		
	}
	
	protected abstract void visit( T node );
	
}