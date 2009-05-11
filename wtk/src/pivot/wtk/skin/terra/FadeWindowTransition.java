package pivot.wtk.skin.terra;

import pivot.wtk.Component;
import pivot.wtk.Container;
import pivot.wtk.effects.DropShadowDecorator;
import pivot.wtk.effects.FadeTransition;

public class FadeWindowTransition extends FadeTransition {
    private DropShadowDecorator dropShadowDecorator;

    public FadeWindowTransition(Component component, int duration, int rate,
        DropShadowDecorator dropShadowDecorator) {
        super(component, duration, rate);

        this.dropShadowDecorator = dropShadowDecorator;
    }

    @Override
    protected void update() {
        super.update();
        dropShadowDecorator.setShadowOpacity(1.0f - getPercentComplete());

        Component component = getComponent();
        Container parent = component.getParent();
        if (parent != null) {
            parent.repaint(component.getDecoratedBounds());
        }
    }
}
