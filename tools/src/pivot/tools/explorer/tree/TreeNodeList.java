package pivot.tools.explorer.tree;

import pivot.collections.ArrayList;
import pivot.collections.Sequence;
import pivot.tools.explorer.utils.Strings;
import pivot.wtk.TreeView;

/**
 * List of tree nodes. Usually represents tree path.
 *
 * @author Eugene Ryzhikov
 * @date   Jul 31, 2008
 *
 * @param <T>
 */
public class TreeNodeList<T> extends ArrayList<T> {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates tree node path based on provided node index path
	 * @param <T>
	 * @param treeView
	 * @param indexPath
	 * @return
	 */
	public static final <T extends Sequence<T>> TreeNodeList<T> create( final TreeView treeView, final Sequence<Integer> indexPath ) {

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
		return Strings.createDelimited( this, "/");
	}

}
