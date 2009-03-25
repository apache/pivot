package pivot.wtk;

import pivot.collections.Sequence;
import pivot.wtk.effects.Decorator;

/**
 * Component decorator listener interface.
 *
 * @author gbrown
 */
public interface ComponentDecoratorListener {
    /**
     * Called when a decorator has been inserted into a component's decorator
     * sequence.
     *
     * @param component
     * @param index
     */
    public void decoratorInserted(Component component, int index);

    /**
     * Called when a decorator has been updated in a component's decorator
     * sequence.
     *
     * @param component
     * @param index
     * @param previousDecorator
     */
    public void decoratorUpdated(Component component, int index, Decorator previousDecorator);

    /**
     * Called when decorators have been removed from a component's decorator
     * sequence.
     *
     * @param component
     * @param index
     * @param decorators
     */
    public void decoratorsRemoved(Component component, int index,
        Sequence<Decorator> decorators);
}
