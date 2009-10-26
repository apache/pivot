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
package org.apache.pivot.beans;

import java.lang.reflect.Method;

/**
 * Bean monitor listener interface.
 */
public interface BeanMonitorListener {
    /**
     * Bean monitor listener adapter.
     */
    public static class Adapter implements BeanMonitorListener {
        @Override
        public void sourceChanged(BeanMonitor beanMonitor, Object previousSource) {
        }

        @Override
        public void eventFired(BeanMonitor beanMonitor, Method event, Object[] arguments) {
        }

        @Override
        public void propertyChanged(BeanMonitor beanMonitor, String propertyName) {
        }
    }

    /**
     * Called when a bean monitor's source has changed.
     *
     * @param beanMonitor
     * @param previousSource
     */
    public void sourceChanged(BeanMonitor beanMonitor, Object previousSource);

    /**
     * Called when an event has been fired by the bean monitor's source.
     *
     * @param beanMonitor
     * @param event
     * @param arguments
     */
    public void eventFired(BeanMonitor beanMonitor, Method event, Object[] arguments);

    /**
     * Called when a property of the bean monitor's source has changed.
     *
     * @param beanMonitor
     * @param propertyName
     */
    public void propertyChanged(BeanMonitor beanMonitor, String propertyName);
}
