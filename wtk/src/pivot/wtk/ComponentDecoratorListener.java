package pivot.wtk;

import pivot.collections.Sequence;

public interface ComponentDecoratorListener {
    public void decoratorInserted(Component component, int index);
    public void decoratorUpdated(Component component, int index, Decorator previousDecorator);
    public void decoratorsRemoved(Component component, int index,
        Sequence<Decorator> decorators);

}
