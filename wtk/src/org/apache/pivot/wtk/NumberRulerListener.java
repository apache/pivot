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
 * Listener for changes to a {@link NumberRuler} that affect size and position.
 */
public interface NumberRulerListener {
    /**
     * Default implementation of the listener interface.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements NumberRulerListener {
        @Override
        public void orientationChanged(NumberRuler ruler) {
            // Empty block
        }

        @Override
        public void textSizeChanged(NumberRuler ruler, int previousSize) {
            // Empty block
        }
    }

    /**
     * Listeners list for this interface.
     */
    public static class Listeners extends ListenerList<NumberRulerListener>
        implements NumberRulerListener {
        @Override
        public void orientationChanged(NumberRuler ruler) {
            forEach(listener -> listener.orientationChanged(ruler));
        }
        @Override
        public void textSizeChanged(NumberRuler ruler, int previousSize) {
            forEach(listener -> listener.textSizeChanged(ruler, previousSize));
        }
    }

    /**
     * The orientation of the {@link NumberRuler} changed.
     * <p> Default operation is to do nothing.
     *
     * @param ruler The component that has changed.
     */
    default public void orientationChanged(NumberRuler ruler) {
    }

    /**
     * The text size (number of characters) of the ruler has changed.
     * <p> Default operation is to do nothing.
     *
     * @param ruler The component that has changed.
     * @param previousSize The previous value of the size.
     */
    default public void textSizeChanged(NumberRuler ruler, int previousSize) {
    }
}
