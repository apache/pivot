package pivot.wtk.skin.terra;

import pivot.wtk.Component;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;
import pivot.wtk.effects.TranslationDecorator;
import pivot.wtk.effects.easing.Easing;
import pivot.wtk.effects.easing.Quadratic;

public class SlideTransition extends Transition {
    private Component component;
    int x0;
    int x1;
    int y0;
    int y1;
    boolean reverse;

    private Easing easing = new Quadratic();
    private TranslationDecorator translationDecorator = new TranslationDecorator();

    public SlideTransition(Component component, int x0, int x1, int y0, int y1,
        boolean reverse, int duration, int rate) {
        super(duration, rate, false);
        this.component = component;
        this.x0 = x0;
        this.x1 = x1;
        this.y0 = y0;
        this.y1 = y1;
        this.reverse = reverse;
    }

    @Override
    public void start(TransitionListener transitionListener) {
        component.getDecorators().add(translationDecorator);

        super.start(transitionListener);
    }

    @Override
    public void stop() {
        component.getDecorators().remove(translationDecorator);

        super.stop();
    }

    @Override
    protected void update() {
        int elapsedTime = getElapsedTime();
        int duration = getDuration();
        float percentComplete = getPercentComplete();

        float deltaX = (float)(x1 - x0) * percentComplete;
        int x = (int)(reverse ? easing.easeIn(elapsedTime, x0, deltaX, duration)
            : easing.easeOut(elapsedTime, x0, deltaX, duration));

        float deltaY = (float)(y1 - y0) * percentComplete;
        int y = (int)(reverse ? easing.easeOut(elapsedTime, y0, deltaY, duration)
            : easing.easeOut(elapsedTime, y0, deltaY, duration));

        translationDecorator.setOffset(x, y);
        component.repaint();
    }
}
