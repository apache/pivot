package pivot.tools.explorer;

import pivot.wtk.TreeView;
import pivot.wtk.content.TreeViewNodeRenderer;

/**
 * Extends standard renderer:
 *   - disabled nodes represent disabled components.
 *
 * @author Eugene Ryzhikov
 * @date   Aug 16, 2008
 *
 */
public class ComponentNodeRenderer extends TreeViewNodeRenderer {

	public ComponentNodeRenderer() {
		super();
	}

	@Override
	public void render(
		Object node,
		TreeView treeView,
		boolean expanded,
		boolean selected,
		boolean highlighted,
		boolean disabled) {

		// show node as disabled if component is.
		if ( node instanceof ComponentAdapter ) {
			disabled = !((ComponentAdapter)node).getComponent().isEnabled();
		}
		super.render(node, treeView, expanded, selected, highlighted, disabled);
	}
}
