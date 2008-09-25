package pivot.wtk.skin.terra;

import pivot.wtk.Component;
import pivot.wtk.effects.FadeDecorator;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;

public class FadeTransition extends Transition {
    private Component component;
    private FadeDecorator fadeDecorator = new FadeDecorator();

    public FadeTransition(Component component, int duration, int rate) {
        super(duration, rate, false);
        this.component = component;
    }

    @Override
    public void start(TransitionListener transitionListener) {
        component.getDecorators().add(fadeDecorator);

        super.start(transitionListener);
    }

    @Override
    public void stop() {
        component.getDecorators().remove(fadeDecorator);

        super.stop();
    }

    @Override
    protected void update() {
        fadeDecorator.setOpacity(1.0f - getPercentComplete());
        component.repaint();
    }
}
