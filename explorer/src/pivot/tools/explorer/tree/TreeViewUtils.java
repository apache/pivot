package pivot.tools.explorer.tree;

import pivot.collections.ArrayList;
import pivot.collections.Sequence;
import pivot.wtk.TreeView;

public final class TreeViewUtils {
	
	/**
	 * Returns tree path with actual nodes 
	 * @param <T>
	 * @param treeView
	 * @param indexPath
	 * @return
	 */
	public static final <T extends Sequence<? super T>> Sequence<T> getNodePath( TreeView treeView,  Sequence<Integer> indexPath ) {
		
		final Sequence<T> result = new ArrayList<T>();
		
		new TreePathVisitor<T>() {
			@Override
			protected void visit(T node) {
				result.add(node);
			}
		}.start(treeView, indexPath);
		
		return result;
		
	}
	
	/**
	 * Returns string representation of path
	 * @param treeView
	 * @param indexPath
	 * @return
	 */
	public static final <T extends Sequence<? super T>> String getStringPath( TreeView treeView,  Sequence<Integer> indexPath ) {
		
		final StringBuilder sb = new StringBuilder();
		
		new TreePathVisitor<T>() {
			@Override
			protected void visit(T node) {
				if ( sb.length() > 0 ) sb.append('/');
				sb.append( node.toString() );
			}
		}.start(treeView, indexPath);
		
		return sb.toString();
		
	}	
	
	
	
}


