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
package org.apache.pivot.wtk.skin;

import org.apache.pivot.collections.List;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListButtonListener;
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;

/**
 * Abstract base class for list button skins.
 */
public abstract class ListButtonSkin extends ButtonSkin
    implements ListButton.Skin, ListButtonListener, ListButtonSelectionListener {
    protected ListView listView;
    protected Window listViewPopup;

    private ComponentMouseButtonListener listViewPopupMouseButtonListener = new ComponentMouseButtonListener.Adapter() {
        @Override
        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            ListButton listButton = (ListButton)getComponent();

            listViewPopup.close();

            int index = listView.getSelectedIndex();
            listButton.setSelectedIndex(index);

            if (listButton.isRepeatable()) {
                listButton.press();
            }

            return true;
        }
    };

    private ComponentKeyListener listViewPopupKeyListener = new ComponentKeyListener.Adapter() {
        /**
         * {@link KeyCode#ESCAPE ESCAPE} Close the popup.<br>
         * {@link KeyCode#ENTER ENTER} Choose the selected list item.<br>
         * {@link KeyCode#TAB TAB} Choose the selected list item and transfer
         * focus forwards.<br>
         * {@link KeyCode#TAB TAB} + {@link Keyboard.Modifier#SHIFT SHIFT} Choose the
         * selected list item and transfer focus backwards.
         */
        @Override
        public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            ListButton listButton = (ListButton)getComponent();

            switch (keyCode) {
                case Keyboard.KeyCode.ENTER: {
                    listViewPopup.close();

                    int index = listView.getSelectedIndex();
                    listButton.setSelectedIndex(index);

                    if (listButton.isRepeatable()) {
                        listButton.press();
                    }

                    break;
                }

                case Keyboard.KeyCode.TAB: {
                    listViewPopup.close();

                    int index = listView.getSelectedIndex();
                    listButton.setSelectedIndex(index);

                    FocusTraversalDirection direction = (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) ?
                        FocusTraversalDirection.BACKWARD : FocusTraversalDirection.FORWARD;
                    listButton.transferFocus(direction);

                    break;
                }

                case Keyboard.KeyCode.ESCAPE: {
                    listViewPopup.close();
                    break;
                }
            }

            return false;
        }
    };

    private WindowStateListener listViewPopupWindowStateListener = new WindowStateListener.Adapter() {
        @Override
        public void windowOpened(Window window) {
            Display display = window.getDisplay();
            display.getContainerMouseListeners().add(displayMouseListener);

            window.requestFocus();
        }

        @Override
        public Vote previewWindowClose(Window window) {
            if (window.containsFocus()) {
                getComponent().requestFocus();
            }

            return Vote.APPROVE;
        }

        @Override
        public void windowCloseVetoed(Window window, Vote reason) {
            if (reason == Vote.DENY) {
                window.requestFocus();
            }
        }

        @Override
        public void windowClosed(Window window, Display display, Window owner) {
            display.getContainerMouseListeners().remove(displayMouseListener);

            Window componentWindow = getComponent().getWindow();
            if (componentWindow != null
                && componentWindow.isOpen()
                && !componentWindow.isClosing()) {
                componentWindow.moveToFront();
            }
        }
    };

    private ContainerMouseListener displayMouseListener = new ContainerMouseListener.Adapter() {
        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            Display display = (Display)container;
            Component descendant = display.getDescendantAt(x, y);

            if (!listViewPopup.isAncestor(descendant)
                && descendant != ListButtonSkin.this.getComponent()) {
                listViewPopup.close();
            }

            return false;
        }

        @Override
        public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            boolean consumed = false;

            Display display = (Display)container;
            Window window = (Window)display.getComponentAt(x, y);

            if (window != listViewPopup) {
                consumed = true;
            }

            return consumed;
        }
    };

    protected boolean pressed = false;

    public ListButtonSkin() {
        listView = new ListView();

        listViewPopup = new Window();
        listViewPopup.getComponentMouseButtonListeners().add(listViewPopupMouseButtonListener);
        listViewPopup.getComponentKeyListeners().add(listViewPopupKeyListener);
        listViewPopup.getWindowStateListeners().add(listViewPopupWindowStateListener);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        ListButton listButton = (ListButton)component;
        listButton.getListButtonListeners().add(this);
        listButton.getListButtonSelectionListeners().add(this);

        listView.setListData(listButton.getListData());
        listView.setItemRenderer(listButton.getItemRenderer());
    }

    // ListButton.Skin methods
    @Override
    public Window getListViewPopup() {
        return listViewPopup;
    }

    public abstract Bounds getTriggerBounds();

    // Component state events
    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        if (!component.isEnabled()) {
            pressed = false;
        }

        repaintComponent();

        listViewPopup.close();
    }

    @Override
    public void focusedChanged(Component component, Component obverseComponent) {
        super.focusedChanged(component, obverseComponent);

        repaintComponent();

        // Close the popup if focus was transferred to a component whose
        // window is not the popup
        if (!component.isFocused()) {
            pressed = false;

            if (!listViewPopup.containsFocus()) {
                listViewPopup.close();
            }
        }
    }

    // Component mouse events
    @Override
    public void mouseOut(Component component) {
        super.mouseOut(component);

        pressed = false;
        repaintComponent();
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        pressed = true;
        repaintComponent();

        ListButton listButton = (ListButton)component;

        if (listViewPopup.isOpen()) {
            listViewPopup.close();
        } else if (listButton.isRepeatable() && !getTriggerBounds().contains(x, y)) {
            listButton.requestFocus();
        } else {
            listViewPopup.open(component.getWindow());
        }

        return consumed;
    }

    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        pressed = false;
        repaintComponent();

        return super.mouseUp(component, button, x, y);
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        boolean consumed = super.mouseClick(component, button, x, y, count);

        ListButton listButton = (ListButton)getComponent();
        if (listButton.isRepeatable() && !getTriggerBounds().contains(x, y)) {
            listButton.press();
        }

        return consumed;
    }

    /**
     * {@link KeyCode#SPACE SPACE} Repaints the component to reflect the pressed
     * state and opens the popup.<br>
     * {@link KeyCode#UP UP} Selects the previous enabled list item.<br>
     * {@link KeyCode#DOWN DOWN} Selects the next enabled list item.
     *
     * @see #keyReleased(Component, int,
     * org.apache.pivot.wtk.Keyboard.KeyLocation)
     */
    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        ListButton listButton = (ListButton)getComponent();

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = true;
            repaintComponent();

            if (listViewPopup.isOpen()) {
                listViewPopup.close();
            } else if (!listButton.isRepeatable()){
                listViewPopup.open(component.getWindow());
            }
        } else if (keyCode == Keyboard.KeyCode.UP) {
            int index = listButton.getSelectedIndex();

            do {
                index--;
            } while (index >= 0
                && listButton.isItemDisabled(index));

            if (index >= 0) {
                listButton.setSelectedIndex(index);
                consumed = true;
            }
        } else if (keyCode == Keyboard.KeyCode.DOWN) {
            if (Keyboard.isPressed(Keyboard.Modifier.ALT)) {
                listViewPopup.open(component.getWindow());

                consumed = true;
            } else {
                int index = listButton.getSelectedIndex();
                int count = listButton.getListData().getLength();

                do {
                    index++;
                } while (index < count
                    && listView.isItemDisabled(index));

                if (index < count) {
                    listButton.setSelectedIndex(index);
                    consumed = true;
                }
            }
        } else {
            consumed = super.keyPressed(component, keyCode, keyLocation);
        }

        return consumed;
    }

    /**
     * {@link KeyCode#SPACE SPACE} 'presses' the button.
     */
    @Override
    public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        ListButton listButton = (ListButton)getComponent();

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = false;
            repaintComponent();

            if (listButton.isRepeatable()) {
                listButton.press();
            }
        } else {
            consumed = super.keyReleased(component, keyCode, keyLocation);
        }

        return consumed;
    }

    /**
     * Select the next enabled list item where the first character of the
     * rendered text matches the typed key (case insensitive).
     */
    @Override
    public boolean keyTyped(Component component, char character) {
        boolean consumed = super.keyTyped(component, character);

        ListButton listButton = (ListButton)getComponent();

        List<?> listData = listButton.getListData();
        ListView.ItemRenderer itemRenderer = listButton.getItemRenderer();

        character = Character.toUpperCase(character);

        for (int i = listButton.getSelectedIndex() + 1, n = listData.getLength(); i < n; i++) {
            if (!listButton.isItemDisabled(i)) {
                String string = itemRenderer.toString(listData.get(i));

                if (string != null
                    && string.length() > 0) {
                    char first = Character.toUpperCase(string.charAt(0));

                    if (first == character) {
                        listButton.setSelectedIndex(i);
                        consumed = true;
                        break;
                    }
                }
            }
        }

        return consumed;
    }

    // List button events
    @Override
    public void listDataChanged(ListButton listButton, List<?> previousListData) {
        listButton.setButtonData(null);
        listView.setListData(listButton.getListData());
        invalidateComponent();
    }

    @Override
    public void itemRendererChanged(ListButton listButton, ListView.ItemRenderer previousItemRenderer) {
        listView.setItemRenderer(listButton.getItemRenderer());
    }

    @Override
    public void repeatableChanged(ListButton listButton) {
        // No-op
    }

    @Override
    public void disabledItemFilterChanged(ListButton listButton, Filter<?> previousDisabledItemFilter) {
        listView.setDisabledItemFilter(listButton.getDisabledItemFilter());
    }

    @Override
    public void listSizeChanged(ListButton listButton, int previousListSize) {
        // No-op
    }

    // List button selection events
    @Override
    public void selectedIndexChanged(ListButton listButton, int previousSelectedIndex) {
        int selectedIndex = listButton.getSelectedIndex();

        if (selectedIndex != previousSelectedIndex) {
            // This was not an indirect selection change
            Object buttonData = (selectedIndex == -1) ? null : listButton.getListData().get(selectedIndex);
            listButton.setButtonData(buttonData);

            listView.setSelectedIndex(selectedIndex);
        }
    }

    @Override
    public void selectedItemChanged(ListButton listButton, Object previousSelectedItem) {
        // No-op
    }
}
