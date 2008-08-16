package pivot.tools.explorer;

import pivot.wtk.TreeView;
import pivot.wtk.content.TreeViewNodeRenderer;

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
		
		boolean cmptDisabled = node instanceof ComponentAdapter?
				!((ComponentAdapter)node).getComponent().isEnabled() : disabled;
				
		super.render(node, treeView, expanded, selected, highlighted, cmptDisabled);
	}
}
