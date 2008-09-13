package pivot.demos.transition;

import pivot.wtk.Component;
import pivot.wtk.effects.FadeDecorator;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;
import pivot.wtk.effects.easing.Easing;
import pivot.wtk.effects.easing.Quadratic;

public class CollapseTransition extends Transition {
    private Component component;
    private int initialWidth;
    private Easing easing = new Quadratic();
    private FadeDecorator fadeDecorator = new FadeDecorator();

    public CollapseTransition(Component component, int duration, int rate) {
        super(duration, rate, false);

        this.component = component;
        initialWidth = component.getWidth();
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
        float percentComplete = getPercentComplete();

        if (percentComplete < 1.0f) {
            int duration = getDuration();

            int width = (int)((float)initialWidth * (1.0f - percentComplete));

            width = (int)easing.easeInOut(getElapsedTime(), initialWidth, width - initialWidth, duration);
            component.setPreferredWidth(width);

            fadeDecorator.setOpacity(1.0f - percentComplete);
            component.repaint();
        } else {
            component.getParent().remove(component);
        }
    }
}
