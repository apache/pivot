package pivot.collections;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;

import pivot.util.ListenerList;

/**
 * Implementation of the {@link Set} interface that is backed by a bitfield
 * representing enum values. Enum values are mapped to the bitfield by their
 * ordinal value.
 */
public class EnumSet<E extends Enum<E>> implements Set<E>, Serializable {
    private static final long serialVersionUID = 0;

    private int bitSet = 0;
    private SetListenerList<E> setListeners = new SetListenerList<E>();

    public void add(E element) {
        bitSet |= (1 << element.ordinal());
    }

    public void remove(E element) {
        bitSet &= ~(1 << element.ordinal());
    }

    public void clear() {
        bitSet = 0;
    }

    public boolean contains(E element) {
        return (bitSet & (1 << element.ordinal())) > 0;
    }

    public boolean isEmpty() {
        return bitSet > 0;
    }

    public ListenerList<SetListener<E>> getSetListeners() {
        return setListeners;
    }

    public void setSetListener(SetListener<E> listener) {
        setListeners.add(listener);
    }

    public Comparator<E> getComparator() {
        return null;
    }

    public void setComparator(Comparator<E> comparator) {
        throw new UnsupportedOperationException();
    }

    public Iterator<E> iterator() {
        // TODO
        return null;
    }
}
