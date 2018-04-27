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
package org.apache.pivot.wtk;

import org.apache.pivot.util.ListenerList;

/**
 * List view item state listener interface.
 */
public interface ListViewItemStateListener {
    /**
     * List view item state listeners.
     */
    public static class Listeners extends ListenerList<ListViewItemStateListener>
        implements ListViewItemStateListener {
        @Override
        public void itemCheckedChanged(ListView listView, int index) {
            forEach(listener -> listener.itemCheckedChanged(listView, index));
        }

        @Override
        public void itemCheckedStateChanged(ListView listView, int index) {
            forEach(listener -> listener.itemCheckedStateChanged(listView, index));
        }
    }

    /**
     * Adapter class that provides a default implementation of these interface
     * methods.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public class Adapter implements ListViewItemStateListener {
        @Override
        public void itemCheckedChanged(ListView listView, int index) {
            // Do nothing
        }
        @Override
        public void itemCheckedStateChanged(ListView listView, int index) {
            // Do nothing
        }
    }

    /**
     * Called when an item's checked state has changed.
     *
     * @param listView The list view whose state has changed.
     * @param index    The index of the item whose checked state has changed.
     */
    default void itemCheckedChanged(ListView listView, int index) {
    }

    /**
     * Called when a tri-state item's state has changed, that is, in or out
     * of the {@link org.apache.pivot.wtk.Button.State#MIXED} state.
     *
     * @param listView The list view whose state has changed.
     * @param index    The index of the item whose tri-state has changed.
     */
    default void itemCheckedStateChanged(ListView listView, int index) {
    }

}
