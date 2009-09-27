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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.util.ListenerList;

/**
 * Abstract base class for button components.
 */
public abstract class Button extends Component {
    /**
     * Enumeration representing a button's selection state.
     */
    public enum State {
        SELECTED,
        UNSELECTED,
        MIXED
    }

    /**
     * Button data renderer interface.
     */
    public interface DataRenderer extends Renderer {
        public void render(Object data, Button button, boolean highlighted);
    }

    /**
     * Button listener list.
     */
    private static class ButtonListenerList extends ListenerList<ButtonListener>
        implements ButtonListener {
        @Override
        public void buttonDataChanged(Button button, Object previousButtonData) {
            for (ButtonListener listener : this) {
                listener.buttonDataChanged(button, previousButtonData);
            }
        }

        @Override
        public void dataRendererChanged(Button button, DataRenderer previousDataRenderer) {
            for (ButtonListener listener : this) {
                listener.dataRendererChanged(button, previousDataRenderer);
            }
        }

        @Override
        public void actionChanged(Button button, Action previousAction) {
            for (ButtonListener listener : this) {
                listener.actionChanged(button, previousAction);
            }
        }

        @Override
        public void toggleButtonChanged(Button button) {
            for (ButtonListener listener : this) {
                listener.toggleButtonChanged(button);
            }
        }

        @Override
        public void triStateChanged(Button button) {
            for (ButtonListener listener : this) {
                listener.triStateChanged(button);
            }
        }

        @Override
        public void buttonGroupChanged(Button button, ButtonGroup previousButtonGroup) {
            for (ButtonListener listener : this) {
                listener.buttonGroupChanged(button, previousButtonGroup);
            }
        }

        @Override
        public void selectedKeyChanged(Button button, String previousSelectedKey) {
            for (ButtonListener listener : this) {
                listener.selectedKeyChanged(button, previousSelectedKey);
            }
        }

        @Override
        public void stateKeyChanged(Button button, String previousStateKey) {
            for (ButtonListener listener : this) {
                listener.stateKeyChanged(button, previousStateKey);
            }
        }
    }

    /**
     * Button state listener list.
     */
    private static class ButtonStateListenerList extends ListenerList<ButtonStateListener>
        implements ButtonStateListener {
        @Override
        public void stateChanged(Button button, Button.State previousState) {
            for (ButtonStateListener listener : this) {
                listener.stateChanged(button, previousState);
            }
        }
    }

    /**
     * Button press listener list.
     */
    private static class ButtonPressListenerList extends ListenerList<ButtonPressListener>
        implements ButtonPressListener {
        @Override
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
        @Override
        public void enabledChanged(Action action) {
            setEnabled(action.isEnabled());
        }
    };

    private State state = null;
    private ButtonGroup buttonGroup = null;

    private boolean toggleButton = false;
    private boolean triState = false;
    private String selectedKey = null;
    private String stateKey = null;

    private ButtonListenerList buttonListeners = new ButtonListenerList();
    private ButtonStateListenerList buttonStateListeners = new ButtonStateListenerList();
    private ButtonPressListenerList buttonPressListeners = new ButtonPressListenerList();

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

        Action action = Action.getNamedActions().get(actionID);
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
        buttonPressListeners.buttonPressed(this);

        if (action != null) {
            action.perform();
        }
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
            this.state = state;

            if (buttonGroup != null) {
                // Update the group's selection
                Button selection = buttonGroup.getSelection();

                if (state == State.SELECTED) {
                    // Set this as the new selection (do this before
                    // de-selecting any currently selected button so the
                    // group's change event isn't fired twice)
                    buttonGroup.setSelection(this);

                    // De-select any previously selected button
                    if (selection != null) {
                        selection.setSelected(false);
                    }
                }
                else {
                    // If this button is currently selected, clear the
                    // selection
                    if (selection == this) {
                        buttonGroup.setSelection(null);
                    }
                }
            }

            buttonStateListeners.stateChanged(this, previousState);
        }
    }

    public void setState(String state) {
        if (state == null) {
            throw new IllegalArgumentException("state is null.");
        }

        setState(State.valueOf(state.toUpperCase()));
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
                setButtonGroup(null);
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
            && buttonGroup != null) {
            throw new IllegalStateException("Toggle button is a member of a group.");
        }

        if (this.triState != triState) {
            this.triState = triState;
            buttonListeners.triStateChanged(this);
        }
    }

    /**
     * Returns the button's button group.
     *
     * @return
     * The group to which the button belongs, or <tt>null</tt> if the button
     * does not belong to a group.
     */
    public ButtonGroup getButtonGroup() {
        return buttonGroup;
    }

    /**
     * Sets the button's button group.
     *
     * @param buttonGroup
     * The group to which the button will belong, or <tt>null</tt> if the button
     * will not belong to a group.
     */
    public void setButtonGroup(ButtonGroup buttonGroup) {
        if (!toggleButton) {
            throw new IllegalStateException("Button is not in toggle mode.");
        }

        if (buttonGroup != null
            && triState) {
            throw new IllegalStateException("Toggle button is tri-state.");
        }

        ButtonGroup previousButtonGroup = this.buttonGroup;

        if (previousButtonGroup != buttonGroup) {
            this.buttonGroup = buttonGroup;

            if (previousButtonGroup != null) {
                previousButtonGroup.remove(this);
            }

            if (buttonGroup != null) {
                buttonGroup.add(this);
            }

            buttonListeners.buttonGroupChanged(this, previousButtonGroup);
        }
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
    public void load(Dictionary<String, ?> context) {
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

            if (!(value instanceof State)) {
                throw new IllegalArgumentException("value must be an instance of "
                    + State.class.getName());
            }

            setState((State)value);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void store(Dictionary<String, ?> context) {
        if (isEnabled()) {
            if (selectedKey != null) {
                ((Dictionary<String, Boolean>)context).put(selectedKey, isSelected());
            }

            if (stateKey != null) {
                ((Dictionary<String, State>)context).put(stateKey, state);
            }
        }
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
