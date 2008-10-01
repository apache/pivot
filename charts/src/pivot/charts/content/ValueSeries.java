package pivot.charts.content;

import pivot.collections.Sequence;

/**
 * Base interface for a value series.
 *
 * @param <T>
 * The value type.
 *
 * @author gbrown
 */
public interface ValueSeries<T> extends Series, Sequence<T> {
}
