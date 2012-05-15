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

import org.apache.pivot.collections.Group;
import org.apache.pivot.collections.HashSet;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;

/**
 * Class representing a toggle button group.
 */
public class ButtonGroup implements Group<Button>, Iterable<Button> {
    private static class ButtonGroupListenerList extends WTKListenerList<ButtonGroupListener>
        implements ButtonGroupListener {
        @Override
        public void buttonAdded(ButtonGroup buttonGroup, Button button) {
            for (ButtonGroupListener listener : this) {
                listener.buttonAdded(buttonGroup, button);
            }
        }

        @Override
        public void buttonRemoved(ButtonGroup buttonGroup, Button button) {
            for (ButtonGroupListener listener : this) {
                listener.buttonRemoved(buttonGroup, button);
            }
        }

        @Override
        public void selectionChanged(ButtonGroup buttonGroup, Button previousSelection) {
            for (ButtonGroupListener listener : this) {
                listener.selectionChanged(buttonGroup, previousSelection);
            }
        }
    }

    private HashSet<Button> buttons = new HashSet<Button>();
    private Button selection = null;

    private ButtonGroupListenerList buttonGroupListeners = new ButtonGroupListenerList();

    @Override
    public boolean add(Button button) {
        boolean added = false;

        if (!contains(button)) {
            buttons.add(button);
            added = true;

            if (button.isSelected()) {
                if (selection == null) {
                    selection = button;
                } else {
                    button.setSelected(false);
                }
            }

            button.setButtonGroup(this);

            buttonGroupListeners.buttonAdded(this, button);
        }

        return added;
    }

    @Override
    public boolean remove(Button button) {
        boolean removed = false;

        if (contains(button)) {
            buttons.remove(button);
            removed = true;

            if (button.isSelected()) {
                selection = null;
            }

            button.setButtonGroup(null);

            buttonGroupListeners.buttonRemoved(this, button);
        }

        return removed;
    }

    @Override
    public boolean contains(Button button) {
        return buttons.contains(button);
    }

    public Button getSelection() {
        return selection;
    }

    public void setSelection(Button selection) {
        if (selection != null
            && selection.getButtonGroup() != this) {
            throw new IllegalArgumentException();
        }

        Button previousSelection = this.selection;

        if (previousSelection != selection) {
            this.selection = selection;

            if (previousSelection != null) {
                previousSelection.setSelected(false);
            }

            if (selection != null) {
                selection.setSelected(true);
            }

            buttonGroupListeners.selectionChanged(this, previousSelection);
        }
    }

    @Override
    public Iterator<Button> iterator() {
        return new ImmutableIterator<Button>(buttons.iterator());
    }

    public ListenerList<ButtonGroupListener> getButtonGroupListeners() {
        return buttonGroupListeners;
    }
}
