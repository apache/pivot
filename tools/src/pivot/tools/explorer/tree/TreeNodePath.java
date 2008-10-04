package pivot.tools.explorer.tree;

import pivot.collections.ArrayList;
import pivot.collections.List;
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
public class TreeNodePath<T> extends ArrayList<T> {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates tree node path based on provided node index path
	 * @param <T>
	 * @param treeView
	 * @param indexPath
	 * @return
	 */
	public static final <T extends Sequence<T>> TreeNodePath<T> create( final TreeView treeView, final Sequence<Integer> indexPath ) {

		final TreeNodePath<T> result = new TreeNodePath<T>();

		new TreePathVisitor<T>() {
			@Override
			protected void visit(T node) {
				result.add(node);
			}
		}.start(treeView, indexPath);

		return result;

	}
	
	public static final <T extends Sequence<T>> TreeNodePath<T> createFromSelection( final TreeView treeView ) {
		return create( treeView, treeView.getSelectedPath());
	}
	
	/**
	 * Attmpts to set tree node path as selected tree in the tree 
	 * @param tree
	 */
	@SuppressWarnings("unchecked")
	public Sequence<Integer> toIndexPath( final TreeView tree ) {
		
		List<Integer> indexPath = new ArrayList<Integer>();
		
		Sequence<T> nodes = (Sequence<T>) tree.getTreeData();

		int index;
		T node;
		for ( int i=0, s=getLength(); i<s; i++ ) {
			
			node = get(i);
			index = nodes.indexOf(node);
			if ( index < 0 ) break;
			
			indexPath.add(index);
			if ( node instanceof Sequence<?> ) {
				nodes = (Sequence<T>) node;
			} else {
				throw new RuntimeException( "Node does not support List interface");
			}
			
		}

		return indexPath;	
		
	}
	
	public void applyAsSelection( TreeView tree ) {
		tree.setSelectedPath( toIndexPath(tree));
	}

	@Override
	public String toString() {
		return Strings.createDelimited( this, "/");
	}

}
