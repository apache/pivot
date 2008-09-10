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
import pivot.wtk.effects.ShadeDecorator;

//TODO: Color and opacity should come from preferences
final class ComponentHighlighter implements ComponentMouseListener {

	private final TreeView tree;
	private Component lastDecoratedComponent;
	private Decorator componentDecorator = new ShadeDecorator(0.33f, Color.CYAN.brighter());

	public ComponentHighlighter(TreeView tree) {
		this.tree = tree;
	}

	public void mouseMove(Component component, int x, int y) {

		if (component == tree) {
			Sequence<Integer> nodeAt = tree.getNodeAt(y);
			if (nodeAt.getLength() != 0) {
				Sequence<ComponentAdapter> nodePath = TreeNodeList.create(tree, nodeAt);
				ComponentAdapter node = nodePath.get(nodePath.getLength() - 1);

				highlightComponent(node.getComponent());
				return;
			}
		}
		highlightComponent(null);

	}

	public void mouseOut(Component component) {
		highlightComponent(null);
	}

	public void mouseOver(Component component) {
	}

	private void highlightComponent(Component component) {

		if (lastDecoratedComponent != null) {
			lastDecoratedComponent.getDecorators().remove(componentDecorator);
		}

		if (component != null) {
			component.getDecorators().add(componentDecorator);
		}

		lastDecoratedComponent = component;

	}
}