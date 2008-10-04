/**
 *
 */
package pivot.tools.explorer.tree;

import pivot.collections.Sequence;
import pivot.wtk.TreeView;

/**
 * Represents a "closure" visiting all the nodes in provided tree path.
 * Assumes that tree nodes implement List interface of the same type
 *
 * @author Eugene Ryzhikov
 * @date   Jul 24, 2008
 *
 * @param <T> tree node type
 */
public abstract class TreePathVisitor<T extends Sequence<T>> {


	/**
	 * Starts visiting process
	 * @param treeView
	 * @param path
	 */
	@SuppressWarnings("unchecked")
	public final void start( TreeView treeView, Sequence<Integer> path ) {
		
		if (path==null) return;
		
		Sequence<T> nodes = (Sequence<T>) treeView.getTreeData();

		for ( int i=0, s=path.getLength(); i< s; i++ ) {
			T node = nodes.get( path.get(i) );
			visit( node );
			if ( node instanceof Sequence<?> ) {
				nodes = (Sequence<T>) node;
			} else {
				throw new RuntimeException( "Node does not support List interface");
			}
		}

	}

	protected abstract void visit( T node );

}