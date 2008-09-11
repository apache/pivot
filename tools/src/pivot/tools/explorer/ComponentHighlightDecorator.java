/**
 * 
 */
package pivot.tools.explorer;

import java.awt.Color;
import java.awt.Graphics2D;

import pivot.wtk.Component;
import pivot.wtk.effects.ShadeDecorator;

/**
 * Currently based on ShadDecorator
 * plus slightly darker border around the component
 * 
 * @author Eugene Ryzhikov
 * @date   Sep 10, 2008
 *
 */
final class ComponentHighlightDecorator extends ShadeDecorator {
	
	private Graphics2D graphics;
	private Component component;

	ComponentHighlightDecorator(float opacity, Color color) {
		super(opacity, color);
	}

	@Override
    public void update() {
		super.update();
        graphics.setColor(getColor().darker().darker());
        graphics.drawRect(0, 0, component.getWidth()-1, component.getHeight()-1);
    }

	@Override
	public Graphics2D prepare(Component component, Graphics2D graphics) {
		this.component = component;
		return this.graphics = super.prepare(component, graphics);
	}
	
}