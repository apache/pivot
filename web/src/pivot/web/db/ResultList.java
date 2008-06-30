package pivot.web.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Iterator;

import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ListenerList;

public class ResultList implements List<Dictionary<String, Object>> {
    private ResultSet resultSet = null;

    public ResultList(ResultSet resultSet) {
        if (resultSet == null) {
            throw new IllegalArgumentException("resultSet is null.");
        }

        this.resultSet = resultSet;
    }

    public int add(Dictionary<String, Object> item) {
        // TODO
        return 0;
    }

    public void insert(Dictionary<String, Object> item, int index) {
        try {
            // TODO
            resultSet.insertRow();
        } catch(SQLException exception) {
        }
    }

    public Dictionary<String, Object> update(int index, Dictionary<String, Object> item) {
        // TODO
        return null;
    }

    public int remove(Dictionary<String, Object> item) {
        // TODO
        return 0;
    }

    public Sequence<Dictionary<String, Object>> remove(int index, int count) {
        // TODO
        return null;
    }

    public void clear() {
        // TODO
    }

    public Dictionary<String, Object> get(int index) {
        // TODO If this is a forward-only result set, keep track of the most
        // recent index and move forward by the delta; if index < current index,
        // throw
        return null;
    }

    public int indexOf(Dictionary<String, Object> item) {
        // TODO?
        return 0;
    }

    public int getLength() {
        // TODO?
        return 0;
    }

    public Comparator<Dictionary<String, Object>> getComparator() {
        // TODO?
        return null;
    }

    public void setComparator(Comparator<Dictionary<String, Object>> comparator) {
        // TODO?
    }

    public Iterator<Dictionary<String, Object>> iterator() {
        // TODO Return an iterator that wraps around the result set and calls
        // moveNext()
        return null;
    }

    public ListenerList<ListListener<Dictionary<String, Object>>> getListListeners() {
        // TODO
        return null;
    }
}
