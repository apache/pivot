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
package pivot.wtk.content;

import pivot.collections.List;
import pivot.util.Vote;
import pivot.wtk.Bounds;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.Container;
import pivot.wtk.ContainerMouseListener;
import pivot.wtk.Display;
import pivot.wtk.Insets;
import pivot.wtk.Keyboard;
import pivot.wtk.ListView;
import pivot.wtk.ListViewItemListener;
import pivot.wtk.ListViewListener;
import pivot.wtk.Mouse;
import pivot.wtk.TextInput;
import pivot.wtk.Window;
import pivot.wtk.WindowStateListener;

/**
 * Default list view item editor.
 *
 * @author gbrown
 */
public class ListViewItemEditor implements ListView.ItemEditor {
    private ListView listView = null;
    private int index = -1;

    private TextInput textInput = null;
    private Window popup = null;

    private ListViewListener listViewListener = new ListViewListener() {
        public void listDataChanged(ListView listView, List<?> previousListData) {
            cancel();
        }

        public void itemRendererChanged(ListView listView, ListView.ItemRenderer previousItemRenderer) {
            // No-op
        }

        public void itemEditorChanged(ListView listView, ListView.ItemEditor previousItemEditor) {
            cancel();
        }

        public void selectModeChanged(ListView listView, ListView.SelectMode previousSelectMode) {
            // No-op
        }

        public void checkmarksEnabledChanged(ListView listView) {
            // No-op
        }

        public void selectedItemKeyChanged(ListView listView, String previousSelectedItemKey) {
            // No-op
        }

        public void selectedItemsKeyChanged(ListView listView, String previousSelectedItemsKey) {
            // No-op
        }
    };

    private ListViewItemListener listViewItemListener = new ListViewItemListener() {
        public void itemInserted(ListView listView, int index) {
            cancel();
        }

        public void itemsRemoved(ListView listView, int index, int count) {
            cancel();
        }

        public void itemUpdated(ListView listView, int index) {
            cancel();
        }

        public void itemsSorted(ListView listView) {
            cancel();
        }
    };

    private ComponentKeyListener textInputKeyHandler = new ComponentKeyListener() {
        @SuppressWarnings("unchecked")
        public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            if (keyCode == Keyboard.KeyCode.ENTER) {
                save();
            } else if (keyCode == Keyboard.KeyCode.ESCAPE) {
                cancel();
            }

            return false;
        }

        public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            return false;
        }

        public boolean keyTyped(Component component, char character) {
            return false;
        }
    };

    private WindowStateListener popupWindowStateHandler = new WindowStateListener() {
        public Vote previewWindowOpen(Window window, Display display) {
            return Vote.APPROVE;
        }

        public void windowOpenVetoed(Window window, Vote reason) {
        }

        public void windowOpened(Window window) {
            Display display = window.getDisplay();
            display.getContainerMouseListeners().add(displayMouseHandler);

            listView.getListViewListeners().add(listViewListener);
            listView.getListViewItemListeners().add(listViewItemListener);
        }

        public Vote previewWindowClose(Window window) {
            return Vote.APPROVE;
        }

        public void windowCloseVetoed(Window window, Vote reason) {
        }

        public void windowClosed(Window window, Display display) {
            display.getContainerMouseListeners().remove(displayMouseHandler);

            listView.getListViewListeners().remove(listViewListener);
            listView.getListViewItemListeners().remove(listViewItemListener);

            listView.requestFocus();

            listView = null;
            index = -1;
            textInput = null;
            popup = null;
        }
    };

    private ContainerMouseListener displayMouseHandler = new ContainerMouseListener() {
        public boolean mouseMove(Container container, int x, int y) {
            return false;
        }

        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            Display display = (Display)container;
            Window window = (Window)display.getComponentAt(x, y);
            if (popup != window) {
                save();
            }

            return false;
        }

        public boolean mouseUp(Container container, Mouse.Button button, int x, int y) {
            return false;
        }

        public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            return true;
        }
    };

    public void edit(ListView listView, int index) {
        if (this.listView != null) {
            throw new IllegalStateException("Currently editing.");
        }

        this.listView = listView;
        this.index = index;

        // Get the data being edited
        List<?> listData = listView.getListData();
        ListItem listItem = (ListItem)listData.get(index);

        // Get the item bounds
        Bounds itemBounds = listView.getItemBounds(index);
        int itemIndent = listView.getItemIndent();
        itemBounds.x += itemIndent;
        itemBounds.width -= itemIndent;

        // Render the item data
        ListViewItemRenderer itemRenderer = (ListViewItemRenderer)listView.getItemRenderer();
        itemRenderer.render(listItem, listView, false, false, false, false);
        itemRenderer.setSize(itemBounds.width, itemBounds.height);

        // Calculate the text bounds
        Bounds textBounds = itemRenderer.getTextBounds();

        if (textBounds != null) {
            textInput = new TextInput();
            Insets padding = (Insets)textInput.getStyles().get("padding");

            // Calculate the bounds of what we're editing
            Bounds editBounds = new Bounds(itemBounds);
            editBounds.x += textBounds.x - (padding.left + 1);
            editBounds.width -= textBounds.x;
            editBounds.width += (padding.left + 1);

            // Scroll to make the text as visible as possible
            listView.scrollAreaToVisible(editBounds.x, editBounds.y,
                textBounds.width + padding.left + 1, editBounds.height);

            // Constrain the bounds by what is visible through Viewport ancestors
            editBounds = listView.getVisibleArea(editBounds.x, editBounds.y,
                editBounds.width, editBounds.height);

            textInput.setText(listItem.getText());
            textInput.setPreferredWidth(editBounds.width);
            textInput.getComponentKeyListeners().add(textInputKeyHandler);

            // Create and open the popup
            popup = new Window(textInput);
            popup.getWindowStateListeners().add(popupWindowStateHandler);

            popup.setLocation(editBounds.x, editBounds.y
                + (editBounds.height - textInput.getPreferredHeight(-1)) / 2);
            popup.open(listView.getWindow());

            textInput.requestFocus();
        }
    }

    public boolean isEditing() {
        return (listView != null);
    }

    @SuppressWarnings("unchecked")
    public void save() {
        if (!isEditing()) {
            throw new IllegalStateException();
        }

        List<Object> listData = (List<Object>)listView.getListData();
        ListItem listItem = (ListItem)listData.get(index);

        // Update the item data
        String text = textInput.getText();
        listItem.setText(text);

        // Notifying the parent will close the popup
        listData.update(index, listItem);
    }

    public void cancel() {
        if (!isEditing()) {
            throw new IllegalStateException();
        }

        popup.close();
    }
}
