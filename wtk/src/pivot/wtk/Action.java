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
package pivot.wtk;

import java.util.Iterator;

import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.util.ImmutableIterator;
import pivot.util.ListenerList;

/**
 * Abstract base class for "actions". Actions are common application
 * behaviors generally triggered by buttons and keyboard shortcuts.
 *
 * @author gbrown
 */
public abstract class Action {
    /**
     * Action dictionary implementation.
     *
     * @author gbrown
     */
    public static final class ActionDictionary
        implements Dictionary<String, Action>, Iterable<String> {
        private ActionDictionary() {
        }

        public Action get(String id) {
            return actions.get(id);
        }

        public Action put(String id, Action action) {
            if (action == null) {
                throw new IllegalArgumentException("action is null.");
            }

            Action previousAction = actions.put(id, action);

            if (previousAction != null) {
                previousAction.id = null;
            }

            action.id = id;

            // TODO Fire ActionClassListener#actionAdded()/actionUpdated()

            return previousAction;
        }

        public Action remove(String id) {
            Action previousAction = actions.remove(id);

            if (previousAction != null) {
                previousAction.id = null;
            }

            // TODO Fire ActionClassListener#actionRemoved()

            return previousAction;
        }

        public boolean containsKey(String id) {
            return actions.containsKey(id);
        }

        public boolean isEmpty() {
            return actions.isEmpty();
        }

        public Iterator<String> iterator() {
            return new ImmutableIterator<String>(actions.iterator());
        }
    }

    private static class ActionListenerList extends ListenerList<ActionListener>
        implements ActionListener {
        public void enabledChanged(Action action) {
            for (ActionListener listener : this) {
                listener.enabledChanged(action);
            }
        }
    }

    private String id;
    private boolean enabled = true;

    private ActionListenerList actionListeners = new ActionListenerList();

    private static HashMap<String, Action> actions = new HashMap<String, Action>();
    private static ActionDictionary actionDictionary = new ActionDictionary();

    public Action() {
        this(null);
    }

    public Action(String id) {
        if (id != null) {
            actions.put(id, this);
        }
    }

    public String getID() {
        return id;
    }

    /**
     * Returns a text description of the action.
     */
    public abstract String getDescription();

    /**
     * Performs the action.
     */
    public abstract void perform();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            actionListeners.enabledChanged(this);
        }
    }

    public static ActionDictionary getActions() {
        return actionDictionary;
    }

    public ListenerList<ActionListener> getActionListeners() {
        return actionListeners;
    }
}
