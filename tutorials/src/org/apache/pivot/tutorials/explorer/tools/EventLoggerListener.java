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
package org.apache.pivot.tutorials.explorer.tools;

import java.lang.reflect.Method;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Component;

/**
 * Event logger listener interface.
 */
public interface EventLoggerListener {
    /**
     * Event logger listeners.
     */
    public static class Listeners extends ListenerList<EventLoggerListener>
        implements EventLoggerListener {
        @Override
        public void sourceChanged(EventLogger eventLogger, Component previousSource) {
            forEach(listener -> listener.sourceChanged(eventLogger, previousSource));
        }

        @Override
        public void eventIncluded(EventLogger eventLogger, Method event) {
            forEach(listener -> listener.eventIncluded(eventLogger, event));
        }

        @Override
        public void eventExcluded(EventLogger eventLogger, Method event) {
            forEach(listener -> listener.eventExcluded(eventLogger, event));
        }

        @Override
        public void eventFired(EventLogger eventLogger, Method event, Object[] arguments) {
            forEach(listener -> listener.eventFired(eventLogger, event, arguments));
        }
    }

    /**
     * Called when an event logger's source has changed.
     *
     * @param eventLogger
     * @param previousSource
     */
    default void sourceChanged(EventLogger eventLogger, Component previousSource) {
    }

    /**
     * Called when a declared event has been included in the list of logged
     * events.
     *
     * @param eventLogger
     * @param event
     */
    default void eventIncluded(EventLogger eventLogger, Method event) {
    }

    /**
     * Called when a declared event has been excluded from the list of logged
     * events.
     *
     * @param eventLogger
     * @param event
     */
    default void eventExcluded(EventLogger eventLogger, Method event) {
    }

    /**
     * Called when an included event has been fired by the event logger's
     * source.
     *
     * @param eventLogger
     * @param event
     * @param arguments
     */
    default void eventFired(EventLogger eventLogger, Method event, Object[] arguments) {
    }
}
