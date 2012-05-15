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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Group;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.Keyboard.Modifier;

/**
 * Extension of {@link ButtonGroup} providing keyboard navigation within the
 * group and modified focus navigation that treats the group as a single
 * focusable entity.<br/><br/>
 *
 * {@link KeyCode#UP UP} & {@link KeyCode#LEFT LEFT} Select the previous
 * button<br/>
 * {@link KeyCode#DOWN DOWN} & {@link KeyCode#RIGHT RIGHT} Select the next
 * button<br/>
 * {@link KeyCode#HOME HOME} Select the first button<br/>
 * {@link KeyCode#END END} Select the last button<br/><br/>
 *
 * (Note that only {@link Component#isFocusable() focusable} buttons are
 * considered when searching for a Button to select)<br/><br/>
 *
 * When a button within the group is focused and key is typed, an attempt is
 * made to find the next button (default) or previous button (when the SHIFT
 * modifier is pressed) whose renderer text starts with the typed character.
 * This search will always behave as if the <code>circular</code> property were
 * set.<br/><br/>
 *
 * By default, {@link KeyCode#TAB TAB} and {@link KeyCode#TAB SHIFT+TAB}
 * key presses will transfer focus out of the group (forwards or backwards
 * respectively).
 * This is managed by the {@link #setIntraGroupFocusTransferEnabled(boolean)
 * intraGroupFocusTransferEnabled} property.<br/><br/>
 *
 * The {@link #setCircular(boolean) circular} property can be enabled to allow
 * the selection to transfer seamlessly from one end of the group to the other.
 * (i.e. holding down an arrow key will cycle through all focusable buttons)
 * <br/><br/>
 *
 * Note that due to the conflicting return types of the <code>add(T)</code> and
 * <code>remove(T)</code> methods in the {@link Group#add(Object) Group} and
 * {@link Sequence#add(Object) Sequence} interfaces, this class cannot actually
 * implement <code>Sequence&lt;Button&gt;</code>, although most of the same
 * methods are implemented.<br/>
 */
public class RadioButtonGroup extends ButtonGroup {

    /**
     * Filter used to determine selectable buttons whose rendered data starts
     * with the target character.
     */
    private class FirstCharacterFilter implements Filter<Integer> {
        private char target = '\0';

        public void setTarget(char target) {
            this.target = Character.toUpperCase(target);
        }

        @Override
        public boolean include(Integer index) {
            boolean include = defaultFilter.include(index);
            if (include) {
                Button button = buttonOrder.get(index);
                String rendered = button.getDataRenderer().toString(button.getButtonData());
                if (rendered != null && rendered.length() > 0) {
                    char first = Character.toUpperCase(rendered.charAt(0));
                    if (first != target) {
                        include = false;
                    }
                }
            }
            return include;
        }
    }

    /**
     * ComponentKeyListener to be applied to all buttons as they are added to
     * the group.<br/><br/>
     *
     * At least one button in the group must be focused for this listener to be
     * executed, but that won't necessarily be a selected button.<br/>
     * This also means that the group will not be empty, although some of the
     * buttons contained within may not be focusable, or even visible.
     */
    private final ComponentKeyListener componentKeyListener = new ComponentKeyListener.Adapter() {
        /**
         * Handle TAB & SHIFT+TAB focus traversal, HOME, END & arrow keys
         */
        @Override
        public boolean keyPressed(Component component, int keyCode, KeyLocation keyLocation) {
            int modifiers = Keyboard.getModifiers();
            boolean handled = false;

            /*
             * Potentially transfer focus away from the buttons in this group.
             *
             * At this point we know that at least one button is focused, so we
             * just need to find the first or last (and possibly only) focusable
             * button depending on the focus transfer direction and then
             * transfer away from it.
             */
            if (!intraGroupFocusTransferEnabled) {
                if (keyCode == KeyCode.TAB) {
                    if (modifiers == 0) {
                        Button lastFocusableButton = get(findPrevious(buttonOrder.getLength()));
                        lastFocusableButton.transferFocus(FocusTraversalDirection.FORWARD);
                        handled = true;
                    } else if (modifiers == Modifier.SHIFT.getMask()) {
                        Button firstFocusableButton = get(findNext(NO_SELECTION_INDEX));
                        firstFocusableButton.transferFocus(FocusTraversalDirection.BACKWARD);
                        handled = true;
                    }
                }
            }

            // Navigation/selection within the group
            if (!handled && modifiers == 0) {
                RadioButtonGroup radioButtonGroup = RadioButtonGroup.this;
                Button selectedButton = radioButtonGroup.getSelection();
                handled = true;
                if (keyCode == Keyboard.KeyCode.HOME) {
                    radioButtonGroup.selectFirstButton();
                } else if (keyCode == Keyboard.KeyCode.END) {
                    radioButtonGroup.selectLastButton();
                } else if (keyCode == Keyboard.KeyCode.LEFT || keyCode == Keyboard.KeyCode.UP) {
                    radioButtonGroup.selectPreviousButton(selectedButton);
                } else if (keyCode == Keyboard.KeyCode.RIGHT || keyCode == Keyboard.KeyCode.DOWN) {
                    radioButtonGroup.selectNextButton(selectedButton);
                } else {
                    handled = false;
                }
            }

            return handled;
        }

        /**
         * Attempt to jump to the button whose rendered text begins with the
         * typed character.<br/>
         */
        @Override
        public boolean keyTyped(Component component, char character) {
            int modifiers = Keyboard.getModifiers();
            boolean handled = false;

            // We are only interested when a key is typed with no modifier, or
            // just SHIFT (which is used to reverse the search direction)
            boolean noModifiersPressed = (modifiers == 0);
            boolean shiftPressed = (modifiers == Modifier.SHIFT.getMask());
            if (noModifiersPressed || shiftPressed) {
                RadioButtonGroup radioButtonGroup = RadioButtonGroup.this;
                Button selectedButton = radioButtonGroup.getSelection();

                firstCharacterFilter.setTarget(character);

                // Determine the starting point for the search
                int searchStartIndex;
                if (selectedButton != null) {
                    searchStartIndex = radioButtonGroup.indexOf(selectedButton);
                } else {
                    if (noModifiersPressed) {
                        searchStartIndex = NO_SELECTION_INDEX;
                    } else {
                        searchStartIndex = buttonOrder.getLength();
                    }
                }

                int result = NOT_FOUND_INDEX;
                if (noModifiersPressed) {
                    result = radioButtonGroup.findNext(searchStartIndex, firstCharacterFilter, true);
                } else if (shiftPressed) {
                    result = radioButtonGroup.findPrevious(searchStartIndex, firstCharacterFilter,
                        true);
                }

                // Consider the event to have been handled if a different
                // button end up being selected
                if (result != NOT_FOUND_INDEX && result != searchStartIndex) {
                    radioButtonGroup.setSelection(result);
                    handled = true;
                }
            }
            return handled;
        }
    };

    /**
     * Ensure that all buttons in this group have the custom
     * ComponentKeyListener.<br/>
     * This relies on the logic within ButtonGroup to prevent duplicates.
     */
    private final ButtonGroupListener buttonGroupListener = new ButtonGroupListener.Adapter() {
        @Override
        public void buttonAdded(ButtonGroup buttonGroup, Button button) {
            button.getComponentKeyListeners().add(componentKeyListener);
        }

        @Override
        public void buttonRemoved(ButtonGroup buttonGroup, Button button) {
            button.getComponentKeyListeners().remove(componentKeyListener);
        }
    };

    /**
     * Filter used to determine selectable buttons within the group
     */
    private final Filter<Integer> defaultFilter = new Filter<Integer>() {
        @Override
        public boolean include(Integer index) {
            Button button = buttonOrder.get(index);
            boolean focusable = button.isFocusable();
            return focusable;
        }
    };

    private final FirstCharacterFilter firstCharacterFilter = new FirstCharacterFilter();
    private final List<Button> buttonOrder = new ArrayList<Button>();
    private boolean circular = false;
    private boolean intraGroupFocusTransferEnabled = false;

    private static final int NOT_FOUND_INDEX = -1;
    private static final int NO_SELECTION_INDEX = -1;

    public RadioButtonGroup() {
        getButtonGroupListeners().add(buttonGroupListener);
    }

    /**
     * When enabled, a search for the
     * {@link RadioButtonGroup#selectPreviousButton(Button) previous} or
     * {@link RadioButtonGroup#selectNextButton(Button) next} focusable button
     * will not stop when the group's lower or upper bounds (respectively) are
     * reached.<br/>
     *
     * Instead, the search will 'wrap' and continue from the opposite bound
     * until each button in the entire group has been tested for inclusion.
     * <br/><br/>
     *
     * Defaults to <code>false</code>
     */
    public boolean isCircular() {
        return circular;
    }

    /**
     * When enabled, a search for the
     * {@link RadioButtonGroup#selectPreviousButton(Button) previous} or
     * {@link RadioButtonGroup#selectNextButton(Button) next} focusable button
     * will not stop when the group's lower or upper bounds (respectively) are
     * reached.<br/>
     *
     * Instead, the search will 'wrap' and continue from the opposite bound
     * until each button in the entire group has been tested for inclusion.
     */
    public void setCircular(boolean circular) {
        this.circular = circular;
    }

    /**
     * When true, TAB and SHIFT+TAB transfer focus out of the RadioButtonGroup.
     * <br/>
     * Defaults to <code>false</code>
     */
    public boolean isIntraGroupFocusTransferEnabled() {
        return intraGroupFocusTransferEnabled;
    }

    /**
     * Controls whether TAB and SHIFT+TAB will transfer focus out of the
     * RadioButtonGroup, or simply maintain their default behaviour.
     */
    public void setIntraGroupFocusTransferEnabled(boolean intraGroupFocusTransferEnabled) {
        this.intraGroupFocusTransferEnabled = intraGroupFocusTransferEnabled;
    }

    /**
     * Add a button to the group.
     *
     * @see Group#add(Object)
     * @see Sequence#add(Object)
     */
    @Override
    public boolean add(Button button) {
        if (button == null) {
            throw new IllegalArgumentException("Button cannot be null");
        }
        boolean result = super.add(button);
        if (result) {
            buttonOrder.add(button);
        }
        return result;
    }

    /**
     * Return the button at the specified index.
     *
     * @see Sequence#get(int)
     */
    public Button get(int index) {
        return buttonOrder.get(index);
    }

    /**
     * Return the number of buttons in the group.
     *
     * @see Sequence#getLength()
     */
    public int getLength() {
        return buttonOrder.getLength();
    }

    /**
     * Return the index (order) of the button within the group.
     *
     * @return The index or -1 if the button does not belong to this
     * RadioButtonGroup
     *
     * @see Sequence#indexOf(Object)
     */
    public int indexOf(Button button) {
        return buttonOrder.indexOf(button);
    }

    /**
     * Insert a button at the specified index.
     *
     * @see Sequence#insert(Object, int)
     */
    public void insert(Button button, int index) {
        if (button == null) {
            throw new IllegalArgumentException("Button cannot be null");
        }
        boolean result = super.add(button);
        if (result) {
            buttonOrder.insert(button, index);
        }
    }

    /**
     * Remove the button from the group.
     *
     * @see Group#remove(Object)
     * @see Sequence#remove(Object)
     */
    @Override
    public boolean remove(Button button) {
        boolean result = false;
        if (button != null) {
            result = super.remove(button);
            if (result) {
                buttonOrder.remove(button);
            }
        }
        return result;
    }

    /**
     * Remove <code>count</code> buttons from the group starting at
     * <code>index</code>.
     *
     * @see Sequence#remove(int, int)
     */
    public Sequence<Button> remove(int index, int count) {
        Sequence<Button> removed = new ArrayList<Button>();
        while (count-- > 0) {
            Button button = get(index);
            boolean result = this.remove(button);
            if (result) {
                removed.add(button);
            }
        }
        return removed;
    }


    /**
     * Return an iterator for the <strong>ordered</strong> list of buttons
     */
    @Override
    public Iterator<Button> iterator() {
        return new ImmutableIterator<Button>(buttonOrder.iterator());
    }

    /**
     * Select and <strong>focus</strong> the specified button.
     *
     * @see ButtonGroup#setSelection(Button)
     */
    @Override
    public void setSelection(Button button) {
        super.setSelection(button);
        if (button != null) {
            button.requestFocus();
        }
    }

    /**
     * Select and <strong>focus</strong> the button at the specified index,
     * unless the index is NOT_FOUND_INDEX.
     */
    public void setSelection(int index) {
        if (index != NOT_FOUND_INDEX) {
            this.setSelection(buttonOrder.get(index));
        }
    }

    /**
     * Select the first focusable button in the group.
     */
    public void selectFirstButton() {
        setSelection(findNext(NO_SELECTION_INDEX));
    }

    /**
     * Select the last focusable button in the group.
     */
    public void selectLastButton() {
        setSelection(findPrevious(buttonOrder.getLength()));
    }

    /**
     * Working forwards from the specified button, select the first focusable
     * button.
     *
     * @param button If null, the first available button will be selected,
     * unless the group contains a selected or focused button, in which case
     * that button will be used as the starting point for the search.
     */
    public void selectNextButton(Button button) {
        // No explicit starting point was supplied
        if (button == null) {
            // If there is a selected button in this group, we will try to use
            // it as the starting point.
            button = getSelection();

            if (button == null) {
                // No selection, but perhaps one of the buttons has focus?
                Component focusedComponent = Component.getFocusedComponent();
                if (focusedComponent instanceof Button) {
                    int index = this.indexOf((Button)focusedComponent);
                    if (index != NOT_FOUND_INDEX) {
                        button = this.get(index);
                    }
                }
            }
            // Try again, using new starting point if one was determined
            if (button != null) {
                selectNextButton(button);
            } else {
                selectFirstButton();
            }
        } else {
            int index = indexOf(button);
            if (index == NOT_FOUND_INDEX) {
                throw new IllegalArgumentException(
                    "Button does not belong to this RadioButtonGroup.");
            }
            index = findNext(index);
            if (index != NOT_FOUND_INDEX) {
                setSelection(index);
            }
        }
    }

    /**
     * Working backwards from the specified button, select the first focusable
     * button.
     *
     * @param button If null, the last available button will be selected, unless
     * the group contains a selected or focused button, in which case that
     * button will be used as the starting point for the search.
     */
    public void selectPreviousButton(Button button) {
        // No explicit starting point was supplied
        if (button == null) {
            // If there is a selected button in this group, we will try to use
            // it as the starting point.
            button = getSelection();

            if (button == null) {
                // No selection, but perhaps one of the buttons has focus?
                Component focusedComponent = Component.getFocusedComponent();
                if (focusedComponent instanceof Button) {
                    int index = this.indexOf((Button)focusedComponent);
                    if (index != NOT_FOUND_INDEX) {
                        button = this.get(index);
                    }
                }
            }

            // Try again, using new starting point if one was determined
            if (button != null) {
                selectPreviousButton(button);
            } else {
                selectLastButton();
            }
        } else {
            int index = indexOf(button);
            if (index == NOT_FOUND_INDEX) {
                throw new IllegalArgumentException(
                    "Button does not belong to this RadioButtonGroup.");
            }
            index = findPrevious(index);
            if (index != NOT_FOUND_INDEX) {
                setSelection(index);
            }
        }
    }

    /**
     * Iterate forwards over the buttons in the group, looping back to the start
     * if the upper bound is reached and the <code>circular</code> parameter is
     * true.
     *
     * @param index Index to which the 'next' is relative
     * @param filter Alternative filter to use during the search.
     * @param circularArgument Loop when upper bound is reached
     * @return The first button found to satisfy the filter
     *
     * @see #setCircular(boolean)
     */
    private int findNext(int index, Filter<Integer> filter, boolean circularArgument) {
        filter = (filter == null ? defaultFilter : filter);
        int result = NOT_FOUND_INDEX;
        int length = buttonOrder.getLength();
        if (length > 0) {
            // (index + 1) --> last index
            for (int i = (index + 1); i < length; i++) {
                if (filter.include(i)) {
                    result = i;
                    break;
                }
            }
            if (circularArgument && result == NOT_FOUND_INDEX) {
                // first index --> index
                for (int i = 0; i <= index; i++) {
                    if (filter.include(i)) {
                        result = i;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private int findNext(int index) {
        return findNext(index, defaultFilter, circular);
    }

    /**
     * Iterate backwards over the buttons in the group, looping back to the end
     * if the lower bound is reached and the <code>circular</code> parameter is
     * true.
     *
     * @param index Index to which the 'previous' is relative
     * @param filter Alternative filter to use during the search.
     * @param circularArgument Loop when lower bound is reached
     * @return The first focusable button found
     *
     * @see #setCircular(boolean)
     */
    private int findPrevious(int index, Filter<Integer> filter, boolean circularArgument) {
        filter = (filter == null ? defaultFilter : filter);
        int result = NOT_FOUND_INDEX;
        int length = buttonOrder.getLength();
        if (length > 0) {
            // (index - 1) --> first index
            for (int i = (index - 1); i >= 0; i--) {
                if (filter.include(i)) {
                    result = i;
                    break;
                }
            }
            if (circularArgument && result == NOT_FOUND_INDEX) {
                // last index --> index
                for (int i = (length - 1); i >= index; i--) {
                    if (filter.include(i)) {
                        result = i;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private int findPrevious(int index) {
        return findPrevious(index, defaultFilter, circular);
    }
}
