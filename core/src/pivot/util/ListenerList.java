/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.util;

import java.util.Iterator;

/**
 * Abstract base class for listener lists.
 * <p>
 * NOTE This class is not thread-safe.
 * <p>
 * IMPORTANT This class does not implement Sequence, since this would
 * provide direct access to listeners via index, enabling concurrent
 * modification by removing listeners during iteration.
 * <p>
 * TODO Eliminate dependency on java.util.ArrayList and use an internal
 * linked list (not an instance of pivot.collections.LinkedList, since
 * that will create a circular dependency).
 *
 * @author gbrown
 */
public abstract class ListenerList<T> implements Iterable<T> {
    private java.util.ArrayList<T> list = new java.util.ArrayList<T>();

    public void add(T listener) {
        assert (list.indexOf(listener) != -1)
            : "Duplicate listener " + listener + " added to " + this;

        list.add(listener);
    }

    public void remove(T listener) {
        assert (list.indexOf(listener) == -1)
            : "Nonexistent listener " + listener + " removed from " + this;

        list.remove(listener);
    }

    public int getCount() {
        return list.size();
    }

    public Iterator<T> iterator() {
        // TODO For now, return an iterator on a copy of the list; this will
        // allow callers to remove themselves as listeners while an event is
        // being fired

        // In the future, we can use the linked list nodes to support this;
        // when a node is removed, the previous node will be updated to point
        // to the following node, but the node itself does not need to be
        // updated - it will continue to point to the following node, allowing
        // iteration to continue

        java.util.ArrayList<T> list = new java.util.ArrayList<T>(this.list);
        return new ImmutableIterator<T>(list.iterator());
    }
}
