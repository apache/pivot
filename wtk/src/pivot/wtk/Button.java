/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.util.ListenerList;
import pivot.util.Vote;

/**
 * Abstract base class for button components.
 *
 * TODO Ensure that button group names are unique across applications?
 *
 * TODO Allow callers to remove a button group (possibly by exposing group
 * dictionary interface).
 *
 * @author gbrown
 */
public abstract class Button extends Component {
    /**
     * Enumeration representing a button's selection state.
     *
     * @author gbrown
     */
    public enum State {
        SELECTED,
        UNSELECTED,
        MIXED;

        public static State decode(String value) {
            return valueOf(value.toUpperCase());
        }
    }

    /**
     * Button data renderer interface.
     *
     * @author gbrown
     */
    public interface DataRenderer extends Renderer {
        public void render(Object data, Button button, boolean highlighted);
    }

    /**
     * Class representing a toggle button group.
     *
     * @author gbrown
     */
    public static class Group {
        private static class GroupListenerList extends ListenerList<GroupListener>
            implements GroupListener {
            public void selectionChanged(Group group, Button previousSelection) {
                for (GroupListener listener : this) {
                    listener.selectionChanged(group, previousSelection);
                }
            }
        }

        private Button selection = null;
        private GroupListenerList groupListeners = new GroupListenerList();

        public Group() {
        }

        public Button getSelection() {
            return selection;
        }

        private void setSelection(Button selection) {
            Button previousSelection = this.selection;

            if (previousSelection != selection) {
                this.selection = selection;
                groupListeners.selectionChanged(this, previousSelection);
            }
        }

        public ListenerList<GroupListener> getGroupListeners() {
            return groupListeners;
        }
    }

    /**
     * Listener interface for toggle button groups.
     *
     * @author gbrown
     */
    public interface GroupListener {
        public void selectionChanged(Group group, Button previousSelection);
    }

    /**
     * Button listener list.
     *
     * @author gbrown
     */
    private static class ButtonListenerList extends ListenerList<ButtonListener>
        implements ButtonListener {
        public void buttonDataChanged(Button button, Object previousButtonData) {
            for (ButtonListener listener : this) {
                listener.buttonDataChanged(button, previousButtonData);
            }
        }

        public void dataRendererChanged(Button button, DataRenderer previousDataRenderer) {
            for (ButtonListener listener : this) {
                listener.dataRendererChanged(button, previousDataRenderer);
            }
        }

        public void actionChanged(Button button, Action previousAction) {
            for (ButtonListener listener : this) {
                listener.actionChanged(button, previousAction);
            }
        }

        public void toggleButtonChanged(Button button) {
            for (ButtonListener listener : this) {
                listener.toggleButtonChanged(button);
            }
        }

        public void triStateChanged(Button button) {
            for (ButtonListener listener : this) {
                listener.triStateChanged(button);
            }
        }

        public void groupChanged(Button button, Button.Group previousGroup) {
            for (ButtonListener listener : this) {
                listener.groupChanged(button, previousGroup);
            }
        }

        public void selectedKeyChanged(Button button, String previousSelectedKey) {
            for (ButtonListener listener : this) {
                listener.selectedKeyChanged(button, previousSelectedKey);
            }
        }

        public void stateKeyChanged(Button button, String previousStateKey) {
            for (ButtonListener listener : this) {
                listener.stateKeyChanged(button, previousStateKey);
            }
        }
    }

    /**
     * Button state listener list.
     *
     * @author gbrown
     */
    private static class ButtonStateListenerList extends ListenerList<ButtonStateListener>
        implements ButtonStateListener {
        public Vote previewStateChange(Button button, Button.State state) {
            Vote vote = Vote.APPROVE;

            for (ButtonStateListener listener : this) {
                vote = vote.tally(listener.previewStateChange(button, state));
            }

            return vote;
        }

        public void stateChangeVetoed(Button button, Vote reason) {
            for (ButtonStateListener listener : this) {
                listener.stateChangeVetoed(button, reason);
            }
        }

        public void stateChanged(Button button, Button.State previousState) {
            for (ButtonStateListener listener : this) {
                listener.stateChanged(button, previousState);
            }
        }
    }

    /**
     * Button press listener list.
     *
     * @author gbrown
     */
    private static class ButtonPressListenerList extends ListenerList<ButtonPressListener>
        implements ButtonPressListener {
        public void buttonPressed(Button button) {
            for (ButtonPressListener listener : this) {
                listener.buttonPressed(button);
            }
        }
    }

    private Object buttonData = null;
    private DataRenderer dataRenderer = null;
    private Action action = null;
    private ActionListener actionListener = new ActionListener() {
        public void enabledChanged(Action action) {
            setEnabled(action.isEnabled());
        }
    };

    private State state = null;
    private Group group = null;

    private boolean toggleButton = false;
    private boolean triState = false;
    private String selectedKey = null;
    private String stateKey = null;

    private ButtonListenerList buttonListeners = new ButtonListenerList();
    private ButtonStateListenerList buttonStateListeners = new ButtonStateListenerList();
    private ButtonPressListenerList buttonPressListeners = new ButtonPressListenerList();

    private static HashMap<String, Group> groups = new HashMap<String, Group>();

    public Button() {
        this(null);
    }

    public Button(Object buttonData) {
        this.buttonData = buttonData;
    }

    public Object getButtonData() {
        return buttonData;
    }

    public void setButtonData(Object buttonData) {
        Object previousButtonData = this.buttonData;
        if (previousButtonData != buttonData) {
            this.buttonData = buttonData;

            buttonListeners.buttonDataChanged(this, previousButtonData);
        }
    }

    public DataRenderer getDataRenderer() {
        return dataRenderer;
    }

    public void setDataRenderer(DataRenderer dataRenderer) {
        if (dataRenderer == null) {
            throw new IllegalArgumentException("dataRenderer is null.");
        }

        DataRenderer previousDataRenderer = this.dataRenderer;

        if (previousDataRenderer != dataRenderer) {
            this.dataRenderer = dataRenderer;
            buttonListeners.dataRendererChanged(this, previousDataRenderer);
        }
    }

    /**
     * Returns the action associated with this button.
     *
     * @return
     * The button's action, or <tt>null</tt> if no action is defined.
     */
    public Action getAction() {
        return action;
    }

    /**
     * Sets this button's action.
     *
     * @param action
     * The action to be triggered when this button is pressed, or <tt>null</tt>
     * for no action.
     */
    public void setAction(Action action) {
        Action previousAction = this.action;

        if (previousAction != action) {
            if (previousAction != null) {
                previousAction.getActionListeners().remove(actionListener);
            }

            this.action = action;

            if (action != null) {
                action.getActionListeners().add(actionListener);
                setEnabled(action.isEnabled());
            }

            buttonListeners.actionChanged(this, previousAction);
        }
    }

    /**
     * Sets this button's action.
     *
     * @param actionID
     * The ID of the action to be triggered when this button is pressed.
     *
     * @throws IllegalArgumentException
     * If an action with the given ID does not exist.
     */
    public void setAction(String actionID) {
        if (actionID == null) {
            throw new IllegalArgumentException("actionID is null");
        }

        Action action = Action.getActions().get(actionID);
        if (action == null) {
            throw new IllegalArgumentException("An action with ID "
                + actionID + " does not exist.");
        }

        setAction(action);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (action != null
            && enabled != action.isEnabled()) {
            throw new IllegalArgumentException("Button and action enabled"
                + " states are not consistent.");
        }

        super.setEnabled(enabled);
    }

    /**
     * "Presses" the button. Performs any action associated with the button.
     */
    public void press() {
        if (action != null) {
            action.perform();
        }

        buttonPressListeners.buttonPressed(this);
    }

    /**
     * Returns the button's selected state.
     */
    public boolean isSelected() {
        return (getState() == State.SELECTED);
    }

    /**
     * Sets the button's selected state.
     *
     * @param selected
     */
    public void setSelected(boolean selected) {
        setState(selected ? State.SELECTED : State.UNSELECTED);
    }

    /**
     * Returns the button's selection state.
     */
    public State getState() {
        return state;
    }

    /**
     * Sets the button's selection state.
     *
     * @param state
     */
    public void setState(State state) {
        if (state == null) {
            throw new IllegalArgumentException("state is null.");
        }

        if (!toggleButton) {
            throw new IllegalStateException("Button is not in toggle mode.");
        }

        if (state == State.MIXED
            && !triState) {
            throw new IllegalArgumentException("Button is not tri-state.");
        }

        State previousState = this.state;

        if (previousState != state) {
            Vote vote = buttonStateListeners.previewStateChange(this, state);

            if (vote == Vote.APPROVE) {
                this.state = state;

                if (group != null) {
                    // Update the group's selection
                    Button selection = group.getSelection();

                    if (state == State.SELECTED) {
                        // Set this as the new selection (do this before
                        // de-selecting any currently selected button so the
                        // group's change event isn't fired twice)
                        group.setSelection(this);

                        // De-select any previously selected button
                        if (selection != null) {
                            selection.setSelected(false);
                        }
                    }
                    else {
                        // If this button is currently selected, clear the
                        // selection
                        if (selection == this) {
                            group.setSelection(null);
                        }
                    }
                }

                buttonStateListeners.stateChanged(this, previousState);
            } else {
                buttonStateListeners.stateChangeVetoed(this, vote);
            }
        }
    }

    public void setState(String state) {
        if (state == null) {
            throw new IllegalArgumentException("state is null.");
        }

        setState(State.decode(state));
    }

    /**
     * Returns the button's toggle state.
     */
    public boolean isToggleButton() {
        return toggleButton;
    }

    /**
     * Sets the button's toggle state.
     *
     * @param toggleButton
     */
    public void setToggleButton(boolean toggleButton) {
        if (this.toggleButton != toggleButton) {
            // Non-toggle push buttons can't be selected, can't be part of a
            // group, and can't be tri-state
            if (!toggleButton) {
                setSelected(false);
                setGroup((Group)null);
                setTriState(false);
            }

            this.toggleButton = toggleButton;

            buttonListeners.toggleButtonChanged(this);
        }
    }

    /**
     * Returns the button's tri-state state.
     */
    public boolean isTriState() {
        return triState;
    }

    /**
     * Sets the button's tri-state state.
     *
     * @param triState
     */
    public void setTriState(boolean triState) {
        if (!toggleButton) {
            throw new IllegalStateException("Button is not in toggle mode.");
        }

        if (triState
            && group != null) {
            throw new IllegalStateException("Toggle button is a member of a group.");
        }

        if (this.triState != triState) {
            this.triState = triState;
            buttonListeners.triStateChanged(this);
        }
    }

    /**
     * Returns the button's group.
     */
    public Group getGroup() {
        return group;
    }

    /**
     * Sets the button's group.
     *
     * @param group
     */
    public void setGroup(Group group) {
        if (!toggleButton) {
            throw new IllegalStateException("Button is not in toggle mode.");
        }

        if (group != null
            && triState) {
            throw new IllegalStateException("Toggle button is tri-state.");
        }

        Group previousGroup = this.group;

        if (previousGroup != group) {
            this.group = group;

            // If this was the selected button in the previous group,
            // clear the group's selection
            if (previousGroup != null
                && previousGroup.getSelection() == this) {
                previousGroup.setSelection(null);
            }

            // If this button is selected, set it as the group's selection
            if (group != null
                && isSelected()) {
                group.setSelection(this);
            }

            buttonListeners.groupChanged(this, previousGroup);
        }
    }

    public void setGroup(String group) {
        if (group == null) {
            throw new IllegalArgumentException("group is null.");
        }

        if (!groups.containsKey(group)) {
            groups.put(group, new Group());
        }

        setGroup(groups.get(group));
    }

    public String getSelectedKey() {
        return selectedKey;
    }

    public void setSelectedKey(String selectedKey) {
        String previousSelectedKey = this.selectedKey;
        this.selectedKey = selectedKey;
        buttonListeners.selectedKeyChanged(this, previousSelectedKey);
    }

    public String getStateKey() {
        return stateKey;
    }

    public void setStateKey(String stateKey) {
        String previousStateKey = this.stateKey;
        this.stateKey = stateKey;
        buttonListeners.stateKeyChanged(this, previousStateKey);
    }

    @Override
    public void load(Dictionary<String, Object> context) {
        if (selectedKey != null
            && context.containsKey(selectedKey)) {
            Object value = context.get(selectedKey);

            if (!(value instanceof Boolean)) {
                throw new IllegalArgumentException("value must be an instance of "
                    + Boolean.class.getName());
            }

            setSelected((Boolean)value);
        }

        if (stateKey != null
            && context.containsKey(stateKey)) {
            Object value = context.get(stateKey);
            if (value instanceof String) {
                value = State.decode((String)value);
            }

            if (!(value instanceof State)) {
                throw new IllegalArgumentException("value must be an instance of "
                    + State.class.getName());
            }

            setState((State)value);
        }
    }

    @Override
    public void store(Dictionary<String, Object> context) {
        if (selectedKey != null) {
            context.put(selectedKey, isSelected());
        }

        if (stateKey != null) {
            context.put(stateKey, state);
        }
    }

    public static Group getGroup(String groupName) {
        return groups.get(groupName);
    }

    public ListenerList<ButtonListener> getButtonListeners() {
        return buttonListeners;
    }

    public ListenerList<ButtonStateListener> getButtonStateListeners() {
        return buttonStateListeners;
    }

    public ListenerList<ButtonPressListener> getButtonPressListeners() {
        return buttonPressListeners;
    }
}
