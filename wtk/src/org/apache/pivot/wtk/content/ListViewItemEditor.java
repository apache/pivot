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
package org.apache.pivot.wtk.content;

import org.apache.pivot.collections.List;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Window;

/**
 * Default list view item editor.
 */
public class ListViewItemEditor extends Window implements ListView.ItemEditor {
    private ListView listView = null;
    private int itemIndex = -1;

    private TextInput textInput = new TextInput();

    private ContainerMouseListener displayMouseHandler = new ContainerMouseListener.Adapter() {
        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            Display display = (Display)container;
            Window window = (Window)display.getComponentAt(x, y);

            if (window != ListViewItemEditor.this) {
                endEdit(true);
            }

            return false;
        }

        @Override
        public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            Display display = (Display)container;
            Window window = (Window)display.getComponentAt(x, y);

            return (window != ListViewItemEditor.this);
        }
    };

    public ListViewItemEditor() {
        setContent(textInput);
    }

    public ListView getListView() {
        return listView;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    public TextInput getTextInput() {
        return textInput;
    }

    @Override
    public void beginEdit(ListView listViewArgument, int itemIndexArgument) {
        this.listView = listViewArgument;
        this.itemIndex = itemIndexArgument;

        // Get the data being edited
        List<?> listData = listViewArgument.getListData();
        ListItem listItem = (ListItem)listData.get(itemIndexArgument);

        textInput.setText(listItem.getText());
        textInput.selectAll();

        // Get the item bounds
        Bounds itemBounds = listViewArgument.getItemBounds(itemIndexArgument);
        int itemIndent = listViewArgument.getItemIndent();
        itemBounds = new Bounds(itemBounds.x + itemIndent, itemBounds.y,
            itemBounds.width - itemIndent, itemBounds.height);

        // Render the item data
        ListViewItemRenderer itemRenderer = (ListViewItemRenderer)listViewArgument.getItemRenderer();
        itemRenderer.render(listItem, itemIndexArgument, listViewArgument, false, false, false, false);
        itemRenderer.setSize(itemBounds.width, itemBounds.height);

        // Calculate the text bounds
        Bounds textBounds = itemRenderer.getTextBounds();

        // Calculate the bounds of what is being edited
        Insets padding = (Insets)textInput.getStyles().get("padding");
        Bounds editBounds = new Bounds(itemBounds.x + textBounds.x - (padding.left + 1),
            itemBounds.y, itemBounds.width - textBounds.x + (padding.left + 1),
            itemBounds.height);

        // Scroll to make the item as visible as possible
        listViewArgument.scrollAreaToVisible(editBounds.x, editBounds.y,
            textBounds.width + padding.left + 1, editBounds.height);

        // Constrain the bounds by what is visible through viewport ancestors
        editBounds = listViewArgument.getVisibleArea(editBounds);
        Point location = listViewArgument.mapPointToAncestor(listViewArgument.getDisplay(), editBounds.x, editBounds.y);

        textInput.setPreferredWidth(editBounds.width);
        setLocation(location.x, location.y + (editBounds.height - getPreferredHeight(-1)) / 2);

        // Open the editor
        open(listViewArgument.getWindow());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void endEdit(boolean result) {
        if (result) {
            // Update the item data
            String text = textInput.getText();

            List<Object> listData = (List<Object>)listView.getListData();
            ListItem listItem = (ListItem)listData.get(itemIndex);

            listItem.setText(text);

            if (listData.getComparator() == null) {
                listData.update(itemIndex, listItem);
            } else {
                listData.remove(itemIndex, 1);
                listData.add(listItem);

                // Re-select the item, and make sure it's visible
                itemIndex = listData.indexOf(listItem);
                listView.setSelectedIndex(itemIndex);
                listView.scrollAreaToVisible(listView.getItemBounds(itemIndex));
            }
        }

        getOwner().moveToFront();
        listView.requestFocus();

        listView = null;
        itemIndex = -1;

        close();
    }

    @Override
    public boolean isEditing() {
        return (listView != null);
    }

    @Override
    public void open(Display display, Window owner) {
        if (listView == null) {
            throw new IllegalStateException();
        }

        super.open(display, owner);
        display.getContainerMouseListeners().add(displayMouseHandler);

        requestFocus();
    }

    @Override
    public void close() {
        Display display = getDisplay();
        display.getContainerMouseListeners().remove(displayMouseHandler);

        super.close();
    }

    @Override
    public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed;

        if (keyCode == Keyboard.KeyCode.ENTER) {
            endEdit(true);
            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.ESCAPE) {
            endEdit(false);
            consumed = true;
        } else {
            consumed = false;
        }

        return consumed;
    }
}
