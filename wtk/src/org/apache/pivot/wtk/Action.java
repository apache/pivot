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

import java.util.Iterator;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;


/**
 * Abstract base class for "actions". Actions are common application
 * behaviors generally triggered by buttons and keyboard shortcuts.
 */
public abstract class Action {
    /**
     * Action dictionary implementation.
     */
    public static final class NamedActionDictionary
        implements Dictionary<String, Action>, Iterable<String> {
        private NamedActionDictionary() {
        }

        @Override
        public Action get(String id) {
            return namedActions.get(id);
        }

        @Override
        public Action put(String id, Action action) {
            if (action == null) {
                throw new IllegalArgumentException("action is null.");
            }

            boolean update = containsKey(id);
            Action previousAction = namedActions.put(id, action);

            if (update) {
                actionClassListeners.actionUpdated(id, previousAction);
            }
            else {
                actionClassListeners.actionAdded(id);
            }

            return previousAction;
        }

        @Override
        public Action remove(String id) {
            Action action = null;

            if (containsKey(id)) {
                action = namedActions.remove(id);
                actionClassListeners.actionRemoved(id, action);
            }

            return action;
        }

        @Override
        public boolean containsKey(String id) {
            return namedActions.containsKey(id);
        }

        @Override
        public Iterator<String> iterator() {
            return new ImmutableIterator<String>(namedActions.iterator());
        }
    }

    private static class ActionListenerList extends WTKListenerList<ActionListener>
        implements ActionListener {
        @Override
        public void enabledChanged(Action action) {
            for (ActionListener listener : this) {
                listener.enabledChanged(action);
            }
        }
    }

    private static class ActionClassListenerList extends WTKListenerList<ActionClassListener>
        implements ActionClassListener {
        @Override
        public void actionAdded(String id) {
            for (ActionClassListener listener : this) {
                listener.actionAdded(id);
            }
        }

        @Override
        public void actionUpdated(String id, Action previousAction) {
            for (ActionClassListener listener : this) {
                listener.actionUpdated(id, previousAction);
            }
        }

        @Override
        public void actionRemoved(String id, Action action) {
            for (ActionClassListener listener : this) {
                listener.actionRemoved(id, action);
            }
        }
    }


    private boolean enabled = true;

    private ActionListenerList actionListeners = new ActionListenerList();

    private static HashMap<String, Action> namedActions = new HashMap<String, Action>();
    private static NamedActionDictionary namedActionDictionary = new NamedActionDictionary();

    private static ActionClassListenerList actionClassListeners = new ActionClassListenerList();

    public Action() {
        this(true);
    }

    public Action(boolean enabled) {
        setEnabled(enabled);
    }

    /**
     * Returns a text description of the action. Subclasses should override this
     * to return a meaningful description if one is needed.
     */
    public String getDescription() {
        return null;
    }

    /**
     * Performs the action.
     *
     * @param source
     * The component that initiated the action.
     */
    public abstract void perform(Component source);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            actionListeners.enabledChanged(this);
        }
    }

    public static NamedActionDictionary getNamedActions() {
        return namedActionDictionary;
    }

    public ListenerList<ActionListener> getActionListeners() {
        return actionListeners;
    }

    public static ListenerList<ActionClassListener> getActionClassListeners() {
        return actionClassListeners;
    }
}
