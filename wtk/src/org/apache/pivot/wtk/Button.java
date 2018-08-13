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
import org.apache.pivot.util.Utils;

/**
 * Abstract base class for button components.
 */
public abstract class Button extends Component {
    /**
     * Enumeration representing a button's selection state.
     */
    public enum State {
        SELECTED, UNSELECTED, MIXED
    }

    /**
     * {@link Renderer} interface to customize the appearance of data in a
     * Button.
     */
    public interface DataRenderer extends Renderer {
        /**
         * Prepares the renderer for layout or paint.
         *
         * @param data The data to render, or <tt>null</tt> if called to
         * calculate preferred height for skins that assume a fixed renderer
         * height.
         * @param button The host component.
         * @param highlighted If <tt>true</tt>, the item is highlighted.
         */
        public void render(Object data, Button button, boolean highlighted);

        /**
         * Converts button data to a string representation.
         *
         * @param data The button's data.
         * @return The data's string representation, or <tt>null</tt> if the data
         * does not have a string representation. <p> Note that this method may
         * be called often during keyboard navigation, so implementations should
         * avoid unnecessary string allocations.
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
         * @param value The value from the bound object that must be
         * converted to a {@code boolean} value for the "selected" state.
         * @return The converted value.
         */
        public boolean isSelected(Object value);

        /**
         * Converts a selection state to a context value during a
         * {@link Component#store(Object)} operation.
         *
         * @param selected The button's "selected" value which must be
         * converted to a suitable value to store as the bound object's
         * property.
         * @return The converted "selected" value.
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
         * @param value The bound object's property value which must be
         * converted to a button's state value.
         * @return The converted button state.
         */
        public State toState(Object value);

        /**
         * Converts a button state to a context value during a
         * {@link Component#store(Object)} operation.
         *
         * @param state The button's current state value which must be
         * converted to a value suitable for storage in the bound object's
         * property.
         * @return The converted state value.
         */
        public Object valueOf(State state);
    }

    /**
     * Translates between a button's buttonData and context data during data
     * binding.
     */
    public interface ButtonDataBindMapping {
        /**
         * Converts a context value to button data during a
         * {@link Component#load(Object)} operation.
         *
         * @param value The value returned from the bound object
         * which must be converted to a suitable value for the
         * button's data.
         * @return The converted button data.
         */
        public Object toButtonData(Object value);

        /**
         * Converts button data to a context value during a
         * {@link Component#store(Object)} operation.
         *
         * @param buttonData The button's current button data which
         * must be converted to a suitable value for the object's
         * property.
         * @return The converted value suitable for the object.
         */
        public Object valueOf(Object buttonData);
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

    private boolean queuedAction = false;
    private int queuedActionDelay = -1;

    private String selectedKey = null;
    private BindType selectedBindType = BindType.BOTH;
    private SelectedBindMapping selectedBindMapping = null;

    private String stateKey = null;
    private BindType stateBindType = BindType.BOTH;
    private StateBindMapping stateBindMapping = null;

    private String buttonDataKey = null;
    private BindType buttonDataBindType = BindType.BOTH;
    private ButtonDataBindMapping buttonDataBindMapping = null;

    private ButtonListener.Listeners buttonListeners = new ButtonListener.Listeners();
    private ButtonStateListener.Listeners buttonStateListeners = new ButtonStateListener.Listeners();
    private ButtonPressListener.Listeners buttonPressListeners = new ButtonPressListener.Listeners();
    private ButtonBindingListener.Listeners buttonBindingListeners = new ButtonBindingListener.Listeners();

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
        Utils.checkNull(dataRenderer, "dataRenderer");

        DataRenderer previousDataRenderer = this.dataRenderer;

        if (previousDataRenderer != dataRenderer) {
            this.dataRenderer = dataRenderer;
            buttonListeners.dataRendererChanged(this, previousDataRenderer);
        }
    }

    /**
     * Returns the action associated with this button.
     *
     * @return The button's action, or <tt>null</tt> if no action is defined.
     */
    public Action getAction() {
        return action;
    }

    /**
     * Sets this button's action.
     *
     * @param action The action to be triggered when this button is pressed, or
     * <tt>null</tt> for no action.
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
     * @param actionID The ID of the action to be triggered when this button is
     * pressed.
     * @throws IllegalArgumentException If an action with the given ID does not
     * exist.
     */
    public void setAction(String actionID) {
        Utils.checkNull(actionID, "actionID");

        Action actionLocal = Action.getNamedActions().get(actionID);
        if (actionLocal == null) {
            throw new IllegalArgumentException("An action with ID " + actionID + " does not exist.");
        }

        setAction(actionLocal);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (action != null && enabled != action.isEnabled()) {
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
            if (queuedAction) {
                Runnable actionCallback = new Action.Callback(action, this);
                if (queuedActionDelay <= 0) {
                    ApplicationContext.queueCallback(actionCallback);
                } else {
                    ApplicationContext.scheduleCallback(actionCallback, (long) queuedActionDelay);
                }
            } else {
                action.perform(this);
            }
        }
    }

    /**
     * @return The button's selected state.
     */
    public boolean isSelected() {
        return (getState() == State.SELECTED);
    }

    /**
     * Sets the button's selected state.
     *
     * @param selected The new "selected" value.
     */
    public void setSelected(boolean selected) {
        setState(selected ? State.SELECTED : State.UNSELECTED);
    }

    /**
     * @return The button's selection state (for tri-state buttons).
     */
    public State getState() {
        return state;
    }

    /**
     * Sets the button's tri-state selection state.
     *
     * @param state The new button selection state.
     */
    public void setState(State state) {
        Utils.checkNull(state, "state");

        if (!toggleButton) {
            throw new IllegalStateException("Button is not in toggle mode.");
        }

        if (state == State.MIXED && !triState) {
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
     * @return The button's toggle state.
     */
    public boolean isToggleButton() {
        return toggleButton;
    }

    /**
     * Sets the button's toggle state.
     *
     * @param toggleButton Whether or not this should be a toggle button.
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
     * @return The button's tri-state state.
     */
    public boolean isTriState() {
        return triState;
    }

    /**
     * Sets the button's tri-state state.
     *
     * @param triState Whether or not to make this a tri-state button.
     */
    public void setTriState(boolean triState) {
        if (!toggleButton) {
            throw new IllegalStateException("Button is not in toggle mode.");
        }

        if (triState && buttonGroup != null) {
            throw new IllegalStateException("Toggle button is a member of a group.");
        }

        if (this.triState != triState) {
            this.triState = triState;
            buttonListeners.triStateChanged(this);
        }
    }

    /**
     * @return Whether or not actions are executed immediately or are queued
     * for later on {@link #press}.
     */
    public boolean isQueuedAction() {
        return queuedAction;
    }

    /**
     * Sets the "queued action" flag so that actions invoked on button press
     * are queued to the {@link ApplicationContext} callback queue to be run
     * "later" instead of being invoked now.
     *
     * @param flag The new value of the queued action flag for this button.
     */
    public void setQueuedAction(boolean flag) {
        this.queuedAction = flag;
    }

    /**
     * @return The (millisecond) delay to use when queuing actions for later.
     */
    public int getQueuedActionDelay() {
        return queuedActionDelay;
    }

    /**
     * Set the delay to be used when {@link #setQueuedAction} is set to <tt>true</tt>.
     * <p> Typically this delay would be a bit longer than the popup window fade
     * transition (for instance), or similar timing.
     *
     * @param delay The delay value (in milliseconds) to use when queuing the action,
     * or <tt>0</tt> to use no delay.
     */
    public void setQueuedActionDelay(int delay) {
        Utils.checkNonNegative(delay, "delay");

        this.queuedActionDelay = delay;
    }

    /**
     * Returns the button's button group.
     *
     * @return The group to which the button belongs, or <tt>null</tt> if the
     * button does not belong to a group.
     */
    public ButtonGroup getButtonGroup() {
        return buttonGroup;
    }

    /**
     * Sets the button's button group.
     *
     * @param buttonGroup The group to which the button will belong, or
     * <tt>null</tt> if the button will not belong to a group.
     */
    public void setButtonGroup(ButtonGroup buttonGroup) {
        if (!toggleButton) {
            throw new IllegalStateException("Button is not in toggle mode.");
        }

        if (buttonGroup != null && triState) {
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

    /**
     * @return The binding key used for this button's data.
     */
    public String getButtonDataKey() {
        return buttonDataKey;
    }

    /**
     * Set the binding key to use for this button's data.
     *
     * @param buttonDataKey The binding key for button data, which should be
     * a field name or object "property" that supplies the button data.
     */
    public void setButtonDataKey(String buttonDataKey) {
        String previousButtonDataKey = this.buttonDataKey;
        if (previousButtonDataKey != buttonDataKey) {
            this.buttonDataKey = buttonDataKey;
            buttonBindingListeners.buttonDataKeyChanged(this, previousButtonDataKey);
        }
    }

    /**
     * @return The type of binding used for button data (that is "load", "store"
     * or "both").
     */
    public BindType getButtonDataBindType() {
        return buttonDataBindType;
    }

    public void setButtonDataBindType(BindType buttonDataBindType) {
        Utils.checkNull(buttonDataBindType, "buttonDataBindType");

        BindType previousButtonDataBindType = this.buttonDataBindType;

        if (previousButtonDataBindType != buttonDataBindType) {
            this.buttonDataBindType = buttonDataBindType;
            buttonBindingListeners.buttonDataBindTypeChanged(this, previousButtonDataBindType);
        }
    }

    /**
     * @return The bind mapping used for button data.
     */
    public ButtonDataBindMapping getButtonDataBindMapping() {
        return buttonDataBindMapping;
    }

    /**
     * Set the bind mapping used for this button's data.  This is a method that is used
     * make a translation between the type of object needed for this button's data and
     * the actual data supplied by the bound object.
     *
     * @param buttonDataBindMapping The new mapping to use (can be {@code null} to disable
     * bind mapping).
     */
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
        Utils.checkNull(selectedBindType, "selectedBindType");

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
        Utils.checkNull(stateBindType, "stateBindType");

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
                if (stateKey != null && stateBindType != BindType.STORE
                    && JSON.containsKey(context, stateKey)) {
                    Object value = JSON.get(context, stateKey);

                    State stateLocal = State.UNSELECTED;
                    if (value instanceof State) {
                        stateLocal = (State) value;
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
                if (selectedKey != null && selectedBindType != BindType.STORE
                    && JSON.containsKey(context, selectedKey)) {
                    Object value = JSON.get(context, selectedKey);

                    boolean selected = false;
                    if (value instanceof Boolean) {
                        selected = ((Boolean) value).booleanValue();
                    } else if (selectedBindMapping == null) {
                        if (value != null) {
                            selected = Boolean.valueOf(value.toString()).booleanValue();
                        }
                    } else {
                        selected = selectedBindMapping.isSelected(value);
                    }

                    setSelected(selected);
                }
            }
        }

        if (buttonDataKey != null && JSON.containsKey(context, buttonDataKey)
            && buttonDataBindType != BindType.STORE) {
            Object value = JSON.get(context, buttonDataKey);
            setButtonData((buttonDataBindMapping == null)
                ? value : buttonDataBindMapping.toButtonData(value));
        }
    }

    @Override
    public void store(Object context) {
        if (toggleButton) {
            if (triState) {
                // Bind using state key
                if (stateKey != null && stateBindType != BindType.LOAD) {
                    JSON.put(context, stateKey, (stateBindMapping == null)
                        ? state : stateBindMapping.valueOf(state));
                }
            } else {
                // Bind using selected key
                if (selectedKey != null && selectedBindType != BindType.LOAD) {
                    JSON.put(context, selectedKey, (selectedBindMapping == null)
                      ? Boolean.valueOf(isSelected()) : selectedBindMapping.valueOf(isSelected()));
                }
            }
        }

        if (buttonDataKey != null && buttonDataBindType != BindType.LOAD) {
            JSON.put(context, buttonDataKey, (buttonDataBindMapping == null)
                ? buttonData : buttonDataBindMapping.valueOf(buttonData));
        }
    }

    @Override
    public void clear() {
        if (buttonDataKey != null) {
            setButtonData(null);
        }

        if (selectedKey != null || stateKey != null) {
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
