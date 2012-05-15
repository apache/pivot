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

import java.util.Locale;

import org.apache.pivot.json.JSON;
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
     * {@link Renderer} interface to customize the appearance of data in a Button.
     */
    public interface DataRenderer extends Renderer {
        /**
         * Prepares the renderer for layout or paint.
         *
         * @param data
         * The data to render, or <tt>null</tt> if called to calculate preferred
         * height for skins that assume a fixed renderer height.
         *
         * @param button
         * The host component.
         *
         * @param highlighted
         * If <tt>true</tt>, the item is highlighted.
         */
        public void render(Object data, Button button, boolean highlighted);

        /**
         * Converts button data to a string representation.
         *
         * @param data
         *
         * @return
         * The data's string representation, or <tt>null</tt> if the data does not
         * have a string representation.
         * <p>
         * Note that this method may be called often during keyboard navigation, so
         * implementations should avoid unnecessary string allocations.
         */
        public String toString(Object data);
    }

    /**
     * Translates between selection state and context data during data binding.
     */
    public interface SelectedBindMapping {
        /**
         * Converts a context value to a selection state during a
         * {@link Component#load(Object)} operation.
         *
         * @param value
         */
        public boolean isSelected(Object value);

        /**
         * Converts a selection state to a context value during a
         * {@link Component#store(Object)} operation.
         *
         * @param selected
         */
        public Object valueOf(boolean selected);
    }

    /**
     * Translates between button state and context data during data binding.
     */
    public interface StateBindMapping {
        /**
         * Converts a context value to a button state during a
         * {@link Component#load(Object)} operation.
         *
         * @param value
         */
        public State toState(Object value);

        /**
         * Converts a button state to a context value during a
         * {@link Component#store(Object)} operation.
         *
         * @param state
         */
        public Object valueOf(State state);
    }

    /**
     * Translates between button buttonData and context data during data binding.
     */
    public interface ButtonDataBindMapping {
        /**
         * Converts a context value to button data during a
         * {@link Component#load(Object)} operation.
         *
         * @param value
         */
        public Object toButtonData(Object value);

        /**
         * Converts button data to a context value during a
         * {@link Component#store(Object)} operation.
         *
         * @param buttonData
         */
        public Object valueOf(Object buttonData);
    }

    private static class ButtonListenerList extends WTKListenerList<ButtonListener>
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
    }

    private static class ButtonStateListenerList extends WTKListenerList<ButtonStateListener>
        implements ButtonStateListener {
        @Override
        public void stateChanged(Button button, Button.State previousState) {
            for (ButtonStateListener listener : this) {
                listener.stateChanged(button, previousState);
            }
        }
    }

    private static class ButtonPressListenerList extends WTKListenerList<ButtonPressListener>
        implements ButtonPressListener {
        @Override
        public void buttonPressed(Button button) {
            for (ButtonPressListener listener : this) {
                listener.buttonPressed(button);
            }
        }
    }

    private static class ButtonBindingListenerList extends WTKListenerList<ButtonBindingListener>
        implements ButtonBindingListener {
        @Override
        public void buttonDataKeyChanged(Button button, String previousButtonDataKey) {
            for (ButtonBindingListener listener : this) {
                listener.buttonDataKeyChanged(button, previousButtonDataKey);
            }
        }

        @Override
        public void buttonDataBindTypeChanged(Button button, BindType previousDataBindType) {
            for (ButtonBindingListener listener : this) {
                listener.buttonDataBindTypeChanged(button, previousDataBindType);
            }
        }

        @Override
        public void buttonDataBindMappingChanged(Button button, Button.ButtonDataBindMapping previousButtonDataBindMapping) {
            for (ButtonBindingListener listener : this) {
                listener.buttonDataBindMappingChanged(button, previousButtonDataBindMapping);
            }
        }

        @Override
        public void selectedKeyChanged(Button button, String previousSelectedKey) {
            for (ButtonBindingListener listener : this) {
                listener.selectedKeyChanged(button, previousSelectedKey);
            }
        }

        @Override
        public void selectedBindTypeChanged(Button button, BindType previousSelectedBindType) {
            for (ButtonBindingListener listener : this) {
                listener.selectedBindTypeChanged(button, previousSelectedBindType);
            }
        }

        @Override
        public void selectedBindMappingChanged(Button button, Button.SelectedBindMapping previousSelectedBindMapping) {
            for (ButtonBindingListener listener : this) {
                listener.selectedBindMappingChanged(button, previousSelectedBindMapping);
            }
        }

        @Override
        public void stateKeyChanged(Button button, String previousStateKey) {
            for (ButtonBindingListener listener : this) {
                listener.stateKeyChanged(button, previousStateKey);
            }
        }

        @Override
        public void stateBindTypeChanged(Button button, BindType previousStateBindType) {
            for (ButtonBindingListener listener : this) {
                listener.stateBindTypeChanged(button, previousStateBindType);
            }
        }

        @Override
        public void stateBindMappingChanged(Button button, Button.StateBindMapping previousStateBindMapping) {
            for (ButtonBindingListener listener : this) {
                listener.stateBindMappingChanged(button, previousStateBindMapping);
            }
        }
    }

    private Object buttonData = null;
    private DataRenderer dataRenderer = null;

    private Action action = null;
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void enabledChanged(Action actionArgument) {
            setEnabled(actionArgument.isEnabled());
        }
    };

    private State state = State.UNSELECTED;

    private boolean toggleButton = false;
    private boolean triState = false;

    private ButtonGroup buttonGroup = null;

    private String selectedKey = null;
    private BindType selectedBindType = BindType.BOTH;
    private SelectedBindMapping selectedBindMapping = null;

    private String stateKey = null;
    private BindType stateBindType = BindType.BOTH;
    private StateBindMapping stateBindMapping = null;

    private String buttonDataKey = null;
    private BindType buttonDataBindType = BindType.BOTH;
    private ButtonDataBindMapping buttonDataBindMapping = null;

    private ButtonListenerList buttonListeners = new ButtonListenerList();
    private ButtonStateListenerList buttonStateListeners = new ButtonStateListenerList();
    private ButtonPressListenerList buttonPressListeners = new ButtonPressListenerList();
    private ButtonBindingListenerList buttonBindingListeners = new ButtonBindingListenerList();

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

        Action actionLocal = Action.getNamedActions().get(actionID);
        if (actionLocal == null) {
            throw new IllegalArgumentException("An action with ID "
                + actionID + " does not exist.");
        }

        setAction(actionLocal);
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
            action.perform(this);
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

            // Update the button group's state
            if (buttonGroup != null) {
                if (state == State.SELECTED) {
                    buttonGroup.setSelection(this);
                } else {
                    if (buttonGroup.getSelection() == this) {
                        buttonGroup.setSelection(null);
                    }
                }
            }

            buttonStateListeners.stateChanged(this, previousState);
        }
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

    public String getButtonDataKey() {
        return buttonDataKey;
    }

    public void setButtonDataKey(String buttonDataKey) {
        String previousButtonDataKey = this.buttonDataKey;
        if (previousButtonDataKey != buttonDataKey) {
            this.buttonDataKey = buttonDataKey;
            buttonBindingListeners.buttonDataKeyChanged(this, previousButtonDataKey);
        }
    }

    public BindType getButtonDataBindType() {
        return buttonDataBindType;
    }

    public void setButtonDataBindType(BindType buttonDataBindType) {
        if (buttonDataBindType == null) {
            throw new IllegalArgumentException();
        }

        BindType previousButtonDataBindType = this.buttonDataBindType;

        if (previousButtonDataBindType != buttonDataBindType) {
            this.buttonDataBindType = buttonDataBindType;
            buttonBindingListeners.buttonDataBindTypeChanged(this, previousButtonDataBindType);
        }
    }

    public ButtonDataBindMapping getButtonDataBindMapping() {
        return buttonDataBindMapping;
    }

    public void setButtonDataBindMapping(ButtonDataBindMapping buttonDataBindMapping) {
        ButtonDataBindMapping previousButtonDataBindMapping = this.buttonDataBindMapping;

        if (previousButtonDataBindMapping != buttonDataBindMapping) {
            this.buttonDataBindMapping = buttonDataBindMapping;
            buttonBindingListeners.buttonDataBindMappingChanged(this, previousButtonDataBindMapping);
        }
    }

    public String getSelectedKey() {
        return selectedKey;
    }

    public void setSelectedKey(String selectedKey) {
        String previousSelectedKey = this.selectedKey;

        if (previousSelectedKey != selectedKey) {
            this.selectedKey = selectedKey;
            buttonBindingListeners.selectedKeyChanged(this, previousSelectedKey);
        }
    }

    public BindType getSelectedBindType() {
        return selectedBindType;
    }

    public void setSelectedBindType(BindType selectedBindType) {
        if (selectedBindType == null) {
            throw new IllegalArgumentException();
        }

        BindType previousSelectedBindType = this.selectedBindType;

        if (previousSelectedBindType != selectedBindType) {
            this.selectedBindType = selectedBindType;
            buttonBindingListeners.selectedBindTypeChanged(this, previousSelectedBindType);
        }
    }

    public SelectedBindMapping getSelectedBindMapping() {
        return selectedBindMapping;
    }

    public void setSelectedBindMapping(SelectedBindMapping selectedBindMapping) {
        SelectedBindMapping previousSelectedBindMapping = this.selectedBindMapping;

        if (previousSelectedBindMapping != selectedBindMapping) {
            this.selectedBindMapping = selectedBindMapping;
            buttonBindingListeners.selectedBindMappingChanged(this, previousSelectedBindMapping);
        }
    }

    public String getStateKey() {
        return stateKey;
    }

    public void setStateKey(String stateKey) {
        String previousStateKey = this.stateKey;

        if (previousStateKey != stateKey) {
            this.stateKey = stateKey;
            buttonBindingListeners.stateKeyChanged(this, previousStateKey);
        }
    }

    public BindType getStateBindType() {
        return stateBindType;
    }

    public void setStateBindType(BindType stateBindType) {
        if (stateBindType == null) {
            throw new IllegalArgumentException();
        }

        BindType previousStateBindType = this.stateBindType;

        if (previousStateBindType != stateBindType) {
            this.stateBindType = stateBindType;
            buttonBindingListeners.stateBindTypeChanged(this, previousStateBindType);
        }
    }

    public StateBindMapping getStateBindMapping() {
        return stateBindMapping;
    }

    public void setStateBindMapping(StateBindMapping stateBindMapping) {
        StateBindMapping previousStateBindMapping = this.stateBindMapping;

        if (previousStateBindMapping != stateBindMapping) {
            this.stateBindMapping = stateBindMapping;
            buttonBindingListeners.stateBindMappingChanged(this, previousStateBindMapping);
        }
    }

    @Override
    public void load(Object context) {
        if (toggleButton) {
            if (triState) {
                // Bind using state key
                if (stateKey != null
                    && stateBindType != BindType.STORE
                    && JSON.containsKey(context, stateKey)) {
                    Object value = JSON.get(context, stateKey);

                    State stateLocal = State.UNSELECTED;
                    if (value instanceof State) {
                        stateLocal = (State)value;
                    } else if (stateBindMapping == null) {
                        if (value != null) {
                            stateLocal = State.valueOf(value.toString().toUpperCase(Locale.ENGLISH));
                        }
                    } else {
                        stateLocal = stateBindMapping.toState(value);
                    }

                    setState(stateLocal);
                }
            } else {
                // Bind using selected key
                if (selectedKey != null
                    && selectedBindType != BindType.STORE
                    && JSON.containsKey(context, selectedKey)) {
                    Object value = JSON.get(context, selectedKey);

                    boolean selected = false;
                    if (value instanceof Boolean) {
                        selected = (Boolean)value;
                    } else if (selectedBindMapping == null) {
                        if (value != null) {
                            selected = Boolean.valueOf(value.toString());
                        }
                    } else {
                        selected = selectedBindMapping.isSelected(value);
                    }

                    setSelected(selected);
                }
            }
        }

        if (buttonDataKey != null
            && JSON.containsKey(context, buttonDataKey)
            && buttonDataBindType != BindType.STORE) {
            Object value = JSON.get(context, buttonDataKey);
            setButtonData((buttonDataBindMapping == null) ?
                value : buttonDataBindMapping.toButtonData(value));
        }
    }

    @Override
    public void store(Object context) {
        if (toggleButton) {
            if (triState) {
                // Bind using state key
                if (stateKey != null
                    && stateBindType != BindType.LOAD) {
                    JSON.put(context, stateKey, (stateBindMapping == null) ?
                        state : stateBindMapping.valueOf(state));
                }
            } else {
                // Bind using selected key
                if (selectedKey != null
                    && selectedBindType != BindType.LOAD) {
                    JSON.put(context, selectedKey, (selectedBindMapping == null) ?
                        isSelected() : selectedBindMapping.valueOf(isSelected()));
                }
            }
        }

        if (buttonDataKey != null
            && buttonDataBindType != BindType.LOAD) {
            JSON.put(context, buttonDataKey, (buttonDataBindMapping == null) ?
                buttonData : buttonDataBindMapping.valueOf(buttonData));
        }
    }

    @Override
    public void clear() {
        if (buttonDataKey != null) {
            setButtonData(null);
        }

        if (selectedKey != null
            || stateKey != null) {
            setSelected(false);
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

    public ListenerList<ButtonBindingListener> getButtonBindingListeners() {
        return buttonBindingListeners;
    }
}
