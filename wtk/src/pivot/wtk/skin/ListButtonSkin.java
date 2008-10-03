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
package pivot.wtk.skin;

import pivot.collections.List;
import pivot.wtk.Button;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Dimensions;
import pivot.wtk.Direction;
import pivot.wtk.Display;
import pivot.wtk.Keyboard;
import pivot.wtk.ListButton;
import pivot.wtk.ListButtonListener;
import pivot.wtk.ListButtonSelectionListener;
import pivot.wtk.ListView;
import pivot.wtk.Mouse;
import pivot.wtk.Point;
import pivot.wtk.Popup;
import pivot.wtk.Window;

/**
 * Abstract base class for list button skins.
 * <p>
 * TODO Extend Popup instead of adding event listeners? May slightly simplify
 * implementation.
 * <p>
 * TODO Rather than blindly closing when a mouse down is received, we could
 * instead cache the selection state in the popup's container mouse down event
 * and compare it to the current state in component mouse down. If different,
 * we close the popup. This would also tie this base class less tightly to its
 * concrete subclasses.
 *
 * @author gbrown
 */
public abstract class ListButtonSkin extends ButtonSkin
    implements ListButton.Skin, ListButtonListener, ListButtonSelectionListener {
    private class ListViewPopupKeyHandler implements ComponentKeyListener {
        public void keyTyped(Component component, char character) {
            // No-op
        }

        public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            switch (keyCode) {
                case Keyboard.KeyCode.ESCAPE: {
                    listViewPopup.close();
                    getComponent().requestFocus();
                    break;
                }

                case Keyboard.KeyCode.TAB:
                case Keyboard.KeyCode.ENTER: {
                    ListButton listButton = (ListButton)getComponent();
                    ListView listView = getListView();

                    int index = listView.getSelectedIndex();

                    listView.clearSelection();
                    listButton.setSelectedIndex(index);

                    listViewPopup.close();

                    if (keyCode == Keyboard.KeyCode.TAB) {
                        Direction direction = (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) ?
                            Direction.BACKWARD : Direction.FORWARD;
                        listButton.transferFocus(direction);
                    } else {
                        listButton.requestFocus();
                    }

                    break;
                }
            }

            return false;
        }

        public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            return false;
        }
    }

    private class ListViewPopupMouseListener implements ComponentMouseButtonListener {
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            ListButton listButton = (ListButton)getComponent();
            ListView listView = getListView();

            int index = listView.getSelectedIndex();

            listView.clearSelection();
            listButton.setSelectedIndex(index);

            listViewPopup.close();
            getComponent().requestFocus();
        }
    }

    protected Popup listViewPopup = null;
    protected boolean pressed = false;

    public ListButtonSkin() {
        listViewPopup = new Popup();
        listViewPopup.getComponentKeyListeners().add(new ListViewPopupKeyHandler());
        listViewPopup.getComponentMouseButtonListeners().add(new ListViewPopupMouseListener());
    }

    @Override
    public void install(Component component) {
        super.install(component);

        ListButton listButton = (ListButton)component;
        listButton.getListButtonListeners().add(this);
        listButton.getListButtonSelectionListeners().add(this);

        ListView listView = getListView();
        listView.setListData(listButton.getListData());
    }

    @Override
    public void uninstall() {
        ListButton listButton = (ListButton)getComponent();
        listButton.getListButtonListeners().remove(this);
        listButton.getListButtonSelectionListeners().remove(this);

        listViewPopup.close();

        super.uninstall();
    }

    // Component state events
    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        listViewPopup.close();
        pressed = false;
    }

    @Override
    public void focusedChanged(Component component, boolean temporary) {
        super.focusedChanged(component, temporary);

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
    public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        ListButton listButton = (ListButton)getComponent();
        ListView listView = getListView();

        listButton.requestFocus();
        listButton.press();

        if (listView.isShowing()) {
            listView.requestFocus();
        }
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

    // Button events
    @Override
    public void buttonPressed(Button button) {
        if (listViewPopup.isOpen()) {
            listViewPopup.close();
        } else {
            ListButton listButton = (ListButton)button;
            ListView listView = getListView();

            Component content = listViewPopup.getContent();

            if (listButton.getListData().getLength() > 0) {
                // Determine the popup's location and preferred size, relative
                // to the button
                Window window = listButton.getWindow();

                if (window != null) {
                    int width = getWidth();
                    int height = getHeight();

                    Display display = listButton.getWindow().getDisplay();

                    // Ensure that the popup remains within the bounds of the display
                    Point buttonLocation = listButton.mapPointToAncestor(display, 0, 0);

                    Dimensions displaySize = display.getSize();
                    Dimensions popupSize = content.getPreferredSize();

                    int x = buttonLocation.x;
                    if (popupSize.width > width
                        && x + popupSize.width > displaySize.width) {
                        x = buttonLocation.x + width - popupSize.width;
                    }

                    int y = buttonLocation.y + height - 1;
                    if (y + popupSize.height > displaySize.height) {
                        if (buttonLocation.y - popupSize.height > 0) {
                            y = buttonLocation.y - popupSize.height + 1;
                        } else {
                            popupSize.height = displaySize.height - y;
                        }
                    } else {
                        popupSize.height = -1;
                    }

                    listViewPopup.setLocation(x, y);
                    listViewPopup.setPreferredSize(popupSize);
                    listViewPopup.open(listButton);

                    if (listView.getFirstSelectedIndex() == -1
                        && listView.getListData().getLength() > 0) {
                        listView.setSelectedIndex(0);
                    }

                    listView.requestFocus();
                }
            }
        }
    }

    // List button events
    public void listDataChanged(ListButton listButton, List<?> previousListData) {
        ListView listView = getListView();
        listView.setListData(listButton.getListData());
    }

    public void itemRendererChanged(ListButton listButton, ListView.ItemRenderer previousItemRenderer) {
        ListView listView = getListView();
        listView.setItemRenderer(listButton.getItemRenderer());
    }

    public void selectedValueKeyChanged(ListButton listButton, String previousSelectedValueKey) {
        // No-op
    }

    // List button selection events
    public void selectedIndexChanged(ListButton listButton, int previousSelectedIndex) {
        // Set the selected item as the button data
        int selectedIndex = listButton.getSelectedIndex();

        Object buttonData = (selectedIndex == -1) ? null : listButton.getListData().get(selectedIndex);
        listButton.setButtonData(buttonData);
    }
}
