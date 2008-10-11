package pivot.tools.explorer;

import java.util.Comparator;
import java.util.Iterator;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ImmutableIterator;
import pivot.util.ListenerList;
import pivot.wtk.Component;
import pivot.wtk.Container;

public class ContainerAdapter extends ComponentAdapter
    implements List<ComponentAdapter> {
    public ContainerAdapter(Container container) {
        super(container);

        for (Component c : container) {
            add(c instanceof Container ? new ContainerAdapter((Container)c) : new ComponentAdapter(c));
        }
    }

    private ArrayList<ComponentAdapter> componentAdapters = new ArrayList<ComponentAdapter>();
    private ListListenerList<ComponentAdapter> listListeners = new ListListenerList<ComponentAdapter>();

    public int add(ComponentAdapter componentAdapter) {
        int index = componentAdapters.getLength();
        insert(componentAdapter, index);

        return index;
    }

    public void insert(ComponentAdapter componentAdapter, int index) {
        componentAdapters.insert(componentAdapter, index);
        listListeners.itemInserted(this, index);
    }

    public ComponentAdapter update(int index, ComponentAdapter componentAdapter) {
        ComponentAdapter previousComponentAdapter = componentAdapters.update(index, componentAdapter);
        listListeners.itemUpdated(this, index, previousComponentAdapter);

        return previousComponentAdapter;
    }

    public int remove(ComponentAdapter componentAdapter) {
        int index = componentAdapters.indexOf(componentAdapter);
        if (index != -1) {
            remove(index, 1);
        }

        return index;
    }

    public Sequence<ComponentAdapter> remove(int index, int count) {
        Sequence<ComponentAdapter> removed = componentAdapters.remove(index, count);
        listListeners.itemsRemoved(this, index, removed);

        return removed;
    }

    public void clear() {
        componentAdapters.clear();
        listListeners.itemsRemoved(this, 0, null);
    }

    public ComponentAdapter get(int index) {
        return componentAdapters.get(index);
    }

    public int indexOf(ComponentAdapter componentAdapter) {
        return componentAdapters.indexOf(componentAdapter);
    }

    public int getLength() {
        return componentAdapters.getLength();
    }

    public Comparator<ComponentAdapter> getComparator() {
        return componentAdapters.getComparator();
    }

    public void setComparator(Comparator<ComponentAdapter> comparator) {
        Comparator<ComponentAdapter> previousComparator = componentAdapters.getComparator();
        componentAdapters.setComparator(comparator);
        listListeners.comparatorChanged(this, previousComparator);
    }

    public Iterator<ComponentAdapter> iterator() {
        return new ImmutableIterator<ComponentAdapter>(componentAdapters.iterator());
    }

    public ListenerList<ListListener<ComponentAdapter>> getListListeners() {
        return listListeners;
    }
}
