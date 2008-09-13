package pivot.charts.content;

import pivot.collections.Sequence;

/**
 * <p>Base interface for a value series.</p>
 *
 * @param <T>
 * The value type.
 *
 * @author gbrown
 */
public interface ValueSeries<T> extends Series, Sequence<T> {
}
