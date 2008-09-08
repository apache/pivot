package pivot.demos.transition;

import pivot.wtk.Component;
import pivot.wtk.effects.FadeDecorator;
import pivot.wtk.effects.Transition;

public class CollapseTransition extends Transition {
    private Component component;
    private int initialWidth;
    private FadeDecorator fadeDecorator = new FadeDecorator();

    public CollapseTransition(Component component, int duration, int rate) {
        super(duration, rate, false);

        this.component = component;
        initialWidth = component.getWidth();

        component.getDecorators().add(fadeDecorator);
    }

    @Override
    protected void update() {
        float percentComplete = getPercentComplete();

        if (percentComplete < 1.0f) {
            int width = (int)((float)initialWidth * (1.0f - percentComplete));
            component.setPreferredWidth(width);

            fadeDecorator.setOpacity(1.0f - percentComplete);
            component.repaint();
        } else {
            component.getParent().remove(component);
        }
    }
}
