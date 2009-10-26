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
package org.apache.pivot.tools.wtk;

import java.lang.reflect.Method;

import org.apache.pivot.beans.BeanMonitor;
import org.apache.pivot.beans.BeanMonitorListener;
import org.apache.pivot.collections.Group;
import org.apache.pivot.collections.HashSet;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.immutable.ImmutableSet;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;

public class EventLogger extends Container {
    private static class EventLoggerListenerList extends ListenerList<EventLoggerListener>
        implements EventLoggerListener {
        @Override
        public void sourceChanged(EventLogger eventLogger, Component previousSource) {
            for (EventLoggerListener listener : this) {
                listener.sourceChanged(eventLogger, previousSource);
            }
        }

        @Override
        public void eventIncluded(EventLogger eventLogger, Method event) {
            for (EventLoggerListener listener : this) {
                listener.eventIncluded(eventLogger, event);
            }
        }

        @Override
        public void eventExcluded(EventLogger eventLogger, Method event) {
            for (EventLoggerListener listener : this) {
                listener.eventExcluded(eventLogger, event);
            }
        }

        @Override
        public void eventFired(EventLogger eventLogger, Method event, Object[] arguments) {
            for (EventLoggerListener listener : this) {
                listener.eventFired(eventLogger, event, arguments);
            }
        }
    }

    private BeanMonitorListener beanMonitorHandler = new BeanMonitorListener.Adapter() {
        @Override
        public void sourceChanged(BeanMonitor meanMonitor, Object previousSource) {
            eventLoggerListeners.sourceChanged(EventLogger.this, (Component)previousSource);
        }

        @Override
        public void eventFired(BeanMonitor meanMonitor, Method event, Object[] arguments) {
            if (includeEvents.contains(event)) {
                eventLoggerListeners.eventFired(EventLogger.this, event, arguments);
            }
        }
    };

    private BeanMonitor beanMonitor = new BeanMonitor();

    private HashSet<Method> includeEvents = new HashSet<Method>();

    private EventLoggerListenerList eventLoggerListeners = new EventLoggerListenerList();

    public EventLogger() {
        this(null);
    }

    public EventLogger(Component source) {
        beanMonitor.getBeanMonitorListeners().add(beanMonitorHandler);
        setSource(source);
        setSkin(new EventLoggerSkin());
    }

    public Component getSource() {
        return (Component)beanMonitor.getSource();
    }

    public void setSource(Component source) {
        beanMonitor.setSource(source);
    }

    public Sequence<Method> getDeclaredEvents() {
        return beanMonitor.getDeclaredEvents();
    }

    public Group<Method> getIncludeEvents() {
        return new ImmutableSet<Method>(includeEvents);
    }

    public void includeEvent(Method event) {
        if (!includeEvents.contains(event)) {
            includeEvents.add(event);
            eventLoggerListeners.eventIncluded(this, event);
        }
    }

    public void excludeEvent(Method event) {
        if (includeEvents.contains(event)) {
            includeEvents.remove(event);
            eventLoggerListeners.eventExcluded(this, event);
        }
    }

    public boolean isEventIncluded(Method event) {
        return includeEvents.contains(event);
    }

    public ListenerList<EventLoggerListener> getEventLoggerListeners() {
        return eventLoggerListeners;
    }
}
