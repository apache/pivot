package pivot.wtkx;

import java.util.Comparator;
import java.util.Iterator;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ListenerList;

/**
 * Class representing an untyped node in a WTKX document.
 *
 * @author gbrown
 */
public class Element implements Dictionary<String, Object>, List<Object> {
    private class ItemIterator implements Iterator<Object> {
        Iterator<Object> source = null;

        public ItemIterator(Iterator<Object> source) {
            this.source = source;
        }

        public boolean hasNext() {
            return source.hasNext();
        }

        public Object next() {
            return source.next();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private String tagName;
    private HashMap<String, Object> dictionary = new HashMap<String, Object>();
    private ArrayList<Object> list = new ArrayList<Object>();

    private ListListenerList<Object> listListeners = new ListListenerList<Object>();

    public Element(String tagName) {
        if (tagName == null) {
            throw new IllegalArgumentException("tagName is null.");
        }

        this.tagName = tagName;
    }

    public String getTagName() {
        return tagName;
    }

    // Dictionary methods
    public Object get(String key) {
        return dictionary.get(key);
    }

    public Object put(String key, Object value) {
        return dictionary.put(key, value);
    }

    public Object remove(String key) {
        return dictionary.remove(key);
    }

    public boolean containsKey(String key) {
        return dictionary.containsKey(key);
    }

    public boolean isEmpty() {
        return dictionary.isEmpty();
    }

    public Iterator<String> getKeys() {
        return dictionary.iterator();
    }

    // List methods
    public int add(Object item) {
        int index = getLength();
        insert(item, index);

        return index;
    }

    public void insert(Object item, int index) {
        list.insert(item, index);
        listListeners.itemInserted(this, index);
    }

    public int remove(Object item) {
        int index = indexOf(item);
        remove(index, 1);

        return index;
    }

    public Sequence<Object> remove(int index, int count) {
        Sequence<Object> removed = list.remove(index, count);
        listListeners.itemsRemoved(this, index, removed);

        return removed;
    }

    public void clear() {
        list.clear();
        listListeners.itemsRemoved(this, 0, null);
    }

    public Object update(int index, Object item) {
        Object previousItem = list.update(index, item);
        listListeners.itemUpdated(list, index, previousItem);

        return previousItem;
    }

    public Object get(int index) {
        return list.get(index);
    }

    public int indexOf(Object item) {
        return list.indexOf(item);
    }

    public int getLength() {
        return list.getLength();
    }

    public Comparator<Object> getComparator() {
        return list.getComparator();
    }

    public void setComparator(Comparator<Object> comparator) {
        Comparator<Object> previousComparator = list.getComparator();
        list.setComparator(comparator);
        listListeners.comparatorChanged(this, previousComparator);
    }

    public Iterator<Object> iterator() {
        return new ItemIterator(list.iterator());
    }

    public ListenerList<ListListener<Object>> getListListeners() {
        return listListeners;
    }
}
