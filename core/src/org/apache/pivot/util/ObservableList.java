/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * A list that fires events when its content changes.
 */
public interface ObservableList<E> extends List<E> {
    /**
     * Observable list listener list.
     */
    public static class ObservableListListenerList<E>
        extends ListenerList<ObservableListListener<E>>
        implements ObservableListListener<E> {
        @Override
        public void elementsAdded(ObservableList<E> list, int fromIndex, int toIndex) {
            for (ObservableListListener<E> listener : listeners()) {
                listener.elementsAdded(list, fromIndex, toIndex);
            }
        }

        @Override
        public void elementsRemoved(ObservableList<E> list, int fromIndex, List<E> elements) {
            for (ObservableListListener<E> listener : listeners()) {
                listener.elementsRemoved(list, fromIndex, elements);
            }
        }

        @Override
        public void elementsUpdated(ObservableList<E> list, int fromIndex, List<E> previousElements) {
            for (ObservableListListener<E> listener : listeners()) {
                listener.elementsUpdated(list, fromIndex, previousElements);
            }
        }
    }

    public List<E> setAll(int index, Collection<? extends E> collection);

    public void sort(Comparator<E> comparator);

    public ListenerList<ObservableListListener<E>> getObservableListListeners();
}
