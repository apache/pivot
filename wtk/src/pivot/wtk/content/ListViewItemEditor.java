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

    private ListViewListener listViewListener = new ListViewListener.Adapter() {
        @Override
        public void listDataChanged(ListView listView, List<?> previousListData) {
            cancel();
        }

        @Override
        public void itemEditorChanged(ListView listView, ListView.ItemEditor previousItemEditor) {
            cancel();
        }
    };

    private ListViewItemListener listViewItemListener = new ListViewItemListener.Adapter() {
        @Override
        public void itemInserted(ListView listView, int index) {
            cancel();
        }

        @Override
        public void itemsRemoved(ListView listView, int index, int count) {
            cancel();
        }

        @Override
        public void itemUpdated(ListView listView, int index) {
            cancel();
        }

        @Override
        public void itemsSorted(ListView listView) {
            cancel();
        }
    };

    private ComponentKeyListener textInputKeyHandler = new ComponentKeyListener.Adapter() {
        @Override
        public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            if (keyCode == Keyboard.KeyCode.ENTER) {
                save();
            } else if (keyCode == Keyboard.KeyCode.ESCAPE) {
                cancel();
            }

            return false;
        }
    };

    private WindowStateListener popupWindowStateHandler = new WindowStateListener.Adapter() {
        @Override
        public void windowOpened(Window window) {
            Display display = window.getDisplay();
            display.getContainerMouseListeners().add(displayMouseHandler);

            listView.getListViewListeners().add(listViewListener);
            listView.getListViewItemListeners().add(listViewItemListener);
        }

        @Override
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

    private ContainerMouseListener displayMouseHandler = new ContainerMouseListener.Adapter() {
        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            Display display = (Display)container;
            Window window = (Window)display.getComponentAt(x, y);
            if (popup != window) {
                save();
            }

            return false;
        }

        @Override
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
            popup = new Window(textInput, true);
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
        if (listData.getComparator() == null) {
            listData.update(index, listItem);
        } else {
            // Save local reference to members variables before they get cleared
            ListView listView = this.listView;

            listData.remove(index, 1);
            listData.add(listItem);

            // Re-select the item, and make sure it's visible
            index = listData.indexOf(listItem);
            listView.setSelectedIndex(index);
            listView.scrollAreaToVisible(listView.getItemBounds(index));
        }
    }

    public void cancel() {
        if (!isEditing()) {
            throw new IllegalStateException();
        }

        popup.close();
    }
}
