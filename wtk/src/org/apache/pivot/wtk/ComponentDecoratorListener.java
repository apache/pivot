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

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.effects.Decorator;

/**
 * Component decorator listener interface.
 */
public interface ComponentDecoratorListener {
    /**
     * Component decorator list adapter.
     */
    public static class Adapter implements ComponentDecoratorListener {
        @Override
        public void decoratorInserted(Component component, int index) {
            // empty block
        }

        @Override
        public void decoratorUpdated(Component component, int index, Decorator previousDecorator) {
            // empty block
        }

        @Override
        public void decoratorsRemoved(Component component, int index,
            Sequence<Decorator> decorators) {
            // empty block
        }
    }

    /**
     * Called when a decorator has been inserted into a component's decorator
     * sequence.
     *
     * @param component
     * @param index
     */
    public void decoratorInserted(Component component, int index);

    /**
     * Called when a decorator has been updated in a component's decorator
     * sequence.
     *
     * @param component
     * @param index
     * @param previousDecorator
     */
    public void decoratorUpdated(Component component, int index, Decorator previousDecorator);

    /**
     * Called when decorators have been removed from a component's decorator
     * sequence.
     *
     * @param component
     * @param index
     * @param decorators
     */
    public void decoratorsRemoved(Component component, int index,
        Sequence<Decorator> decorators);
}
