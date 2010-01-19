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
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Direction;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListButtonListener;
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;

/**
 * Abstract base class for list button skins.
 * <p>
 * TODO Rather than blindly closing when a mouse down is received, we could
 * instead cache the selection state in the popup's container mouse down event
 * and compare it to the current state in component mouse down. If different,
 * we close the popup. This would also tie this base class less tightly to its
 * concrete subclasses.
 */
public abstract class ListButtonSkin extends ButtonSkin
    implements ListButtonListener, ListButtonSelectionListener {
    protected ListView listView;
    protected Window listViewPopup;

    private ComponentMouseButtonListener listViewPopupMouseButtonListener = new ComponentMouseButtonListener.Adapter() {
        @Override
        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            ListButton listButton = (ListButton)getComponent();

            listViewPopup.close();

            int index = listView.getSelectedIndex();
            listButton.setSelectedIndex(index);

            return true;
        }
    };

    private ComponentKeyListener listViewPopupKeyListener = new ComponentKeyListener.Adapter() {
        @Override
        public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            ListButton listButton = (ListButton)getComponent();

            switch (keyCode) {
                case Keyboard.KeyCode.ENTER: {
                    listViewPopup.close();

                    int index = listView.getSelectedIndex();
                    listButton.setSelectedIndex(index);

                    break;
                }

                case Keyboard.KeyCode.TAB: {
                    listViewPopup.close();

                    int index = listView.getSelectedIndex();
                    listButton.setSelectedIndex(index);

                    Direction direction = (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) ?
                        Direction.BACKWARD : Direction.FORWARD;
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
        }

        @Override
        public void windowClosed(Window window, Display display, Window owner) {
            display.getContainerMouseListeners().remove(displayMouseListener);

            Window componentWindow = getComponent().getWindow();
            if (componentWindow != null) {
                // The list button may have been detached from the component
                // hierarchy while our transition was running
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

    // Component state events
    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        listViewPopup.close();
        pressed = false;
    }

    @Override
    public void focusedChanged(Component component, Component obverseComponent) {
        super.focusedChanged(component, obverseComponent);

        // Close the popup if focus was transferred to a component whose
        // window is not the popup
        if (!component.isFocused()
            && !listViewPopup.containsFocus()) {
            listViewPopup.close();
        }

        pressed = false;
    }

    // Component mouse events
    @Override
    public void mouseOut(Component component) {
        super.mouseOut(component);

        pressed = false;
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        pressed = true;
        repaintComponent();

        return consumed;
    }

    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        pressed = false;
        repaintComponent();

        return consumed;
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        boolean consumed = super.mouseClick(component, button, x, y, count);

        ListButton listButton = (ListButton)getComponent();

        listButton.requestFocus();
        listButton.press();

        return consumed;
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = true;
            repaintComponent();
            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.UP) {
            ListButton listButton = (ListButton)getComponent();
            int selectedIndex = listButton.getSelectedIndex();

            if (selectedIndex > 0) {
                listButton.setSelectedIndex(selectedIndex - 1);
                consumed = true;
            }
        } else if (keyCode == Keyboard.KeyCode.DOWN) {
            ListButton listButton = (ListButton)getComponent();
            int selectedIndex = listButton.getSelectedIndex();

            if (selectedIndex < listButton.getListData().getLength() - 1) {
                listButton.setSelectedIndex(selectedIndex + 1);
                consumed = true;
            }
        } else {
            consumed = super.keyPressed(component, keyCode, keyLocation);
        }

        return consumed;
    }

    @Override
    public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        ListButton listButton = (ListButton)getComponent();

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = false;
            repaintComponent();

            listButton.press();
        } else {
            consumed = super.keyReleased(component, keyCode, keyLocation);
        }

        return consumed;
    }

    // List button events
    @Override
    public void listDataChanged(ListButton listButton, List<?> previousListData) {
        listView.setListData(listButton.getListData());
    }

    @Override
    public void itemRendererChanged(ListButton listButton, ListView.ItemRenderer previousItemRenderer) {
        listView.setItemRenderer(listButton.getItemRenderer());
    }

    @Override
    public void disabledItemFilterChanged(ListButton listButton, Filter<?> previousDisabledItemFilter) {
        listView.setDisabledItemFilter(listButton.getDisabledItemFilter());
    }

    @Override
    public void selectedItemKeyChanged(ListButton listButton, String previousSelectedItemKey) {
        // No-op
    }

    // List button selection events
    @Override
    public void selectedIndexChanged(ListButton listButton, int previousSelectedIndex) {
        // Set the selected item as the button data
        int selectedIndex = listButton.getSelectedIndex();

        Object buttonData = (selectedIndex == -1) ? null : listButton.getListData().get(selectedIndex);
        listButton.setButtonData(buttonData);

        listView.setSelectedIndex(selectedIndex);
    }
}
