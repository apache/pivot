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

import java.util.Set;

/**
 * A set that fires events when its content changes.
 */
public interface ObservableSet<E> extends Set<E> {
    /**
     * Observable set listener list.
     */
    public static class ObservableSetListenerList<E>
        extends ListenerList<ObservableSetListener<E>>
        implements ObservableSetListener<E> {
        public void elementAdded(ObservableSet<E> set, E element) {
            for (ObservableSetListener<E> listener : listeners()) {
                listener.elementAdded(set, element);
            }
        }

        public void elementRemoved(ObservableSet<E> set, Object element) {
            for (ObservableSetListener<E> listener : listeners()) {
                listener.elementRemoved(set, element);
            }
        }
    }

    public ListenerList<ObservableSetListener<E>> getObservableSetListeners();
}
