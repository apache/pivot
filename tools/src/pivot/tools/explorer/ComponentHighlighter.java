/**
 *
 */
package pivot.tools.explorer;

import java.awt.Color;

import pivot.collections.Sequence;
import pivot.tools.explorer.tree.TreeNodeList;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.Decorator;
import pivot.wtk.TreeView;


/**
 * Mouse Listener to highlight components under mouse
 *
 * @author Eugene Ryzhikov
 * @date   Sep 10, 2008
 *
 */
//TODO: Color and opacity should come from preferences
final class ComponentHighlighter implements ComponentMouseListener {

	private final TreeView tree;
	private Decorator componentDecorator = new ComponentHighlightDecorator(0.33f, Color.CYAN.brighter());
	private Component lastDecoratedComponent;

	public ComponentHighlighter(TreeView tree) {
		this.tree = tree;
	}

	public boolean mouseMove(Component component, int x, int y) {
		if (component == tree) {
			Sequence<Integer> nodeAt = tree.getNodeAt(y);
			if (nodeAt.getLength() != 0) {
				Sequence<ComponentAdapter> nodePath = TreeNodeList.create(tree, nodeAt);
				ComponentAdapter node = nodePath.get(nodePath.getLength() - 1);

				highlightComponent(node.getComponent());
				return false;
			}
		}
		highlightComponent(null);
		return false;
	}

	public void mouseOut(Component component) {
		highlightComponent(null);
	}

	public void mouseOver(Component component) {}

	private void highlightComponent(Component component) {

		if (lastDecoratedComponent != null) {
			lastDecoratedComponent.getDecorators().remove(componentDecorator);
		}

		lastDecoratedComponent = component;

		if (lastDecoratedComponent != null) {
			lastDecoratedComponent.getDecorators().add(componentDecorator);
		}

	}
}