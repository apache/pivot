package pivot.tools.explorer.tree;

import pivot.collections.ArrayList;
import pivot.collections.Sequence;
import pivot.wtk.TreeView;

/**
 * List of tree nodes. Usually represents tree path. 
 * 
 * @author Eugene Ryzhikov
 * @date   Jul 31, 2008
 *
 * @param <T>
 */
public class TreeNodeList<T extends Sequence<? super T>> extends ArrayList<T> {

	/**
	 * Creates tree node path based on provided node index path
	 * @param <T>
	 * @param treeView
	 * @param indexPath
	 * @return
	 */
	public static final <T extends Sequence<? super T>> TreeNodeList<T> create( final TreeView treeView, final Sequence<Integer> indexPath ) {
		
		final TreeNodeList<T> result = new TreeNodeList<T>();

		new TreePathVisitor<T>() {
			@Override
			protected void visit(T node) {
				result.add(node);
			}
		}.start(treeView, indexPath);

		return result;

	}
	
	@Override
	public String toString() { 
		StringBuilder sb = new StringBuilder();
        for (int i = 0, n = getLength(); i < n; i++) {
            if (i > 0) sb.append("/");
            sb.append(get(i));
        }
        return sb.toString();
	}
	
}
