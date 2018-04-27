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

import org.apache.pivot.collections.List;
import org.apache.pivot.util.ListenerList;

/**
 * Spinner listener interface.
 */
public interface SpinnerListener {
    /**
     * Spinner listeners.
     */
    public static class Listeners extends ListenerList<SpinnerListener> implements
        SpinnerListener {
        @Override
        public void spinnerDataChanged(Spinner spinner, List<?> previousSpinnerData) {
            forEach(listener -> listener.spinnerDataChanged(spinner, previousSpinnerData));
        }

        @Override
        public void itemRendererChanged(Spinner spinner, Spinner.ItemRenderer previousItemRenderer) {
            forEach(listener -> listener.itemRendererChanged(spinner, previousItemRenderer));
        }

        @Override
        public void circularChanged(Spinner spinner) {
            forEach(listener -> listener.circularChanged(spinner));
        }
    }

    /**
     * Spinner listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements SpinnerListener {
        @Override
        public void spinnerDataChanged(Spinner spinner, List<?> previousSpinnerData) {
            // empty block
        }

        @Override
        public void itemRendererChanged(Spinner spinner, Spinner.ItemRenderer previousItemRenderer) {
            // empty block
        }

        @Override
        public void circularChanged(Spinner spinner) {
            // empty block
        }
    }

    /**
     * Called when a spinner's data has changed.
     *
     * @param spinner The source of this event.
     * @param previousSpinnerData What the spinner data used to be.
     */
    default void spinnerDataChanged(Spinner spinner, List<?> previousSpinnerData) {
    }

    /**
     * Called when a spinner's item renderer has changed.
     *
     * @param spinner The source of this event.
     * @param previousItemRenderer What the item renderer used to be.
     */
    default void itemRendererChanged(Spinner spinner, Spinner.ItemRenderer previousItemRenderer) {
    }

    /**
     * Called when a spinner's circular property has changed.
     *
     * @param spinner The source of this event.
     */
    default void circularChanged(Spinner spinner) {
    }
}
