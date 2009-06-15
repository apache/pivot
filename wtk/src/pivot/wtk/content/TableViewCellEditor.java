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

import org.apache.pivot.beans.BeanDictionary;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;

import pivot.wtk.Bounds;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.Container;
import pivot.wtk.ContainerMouseListener;
import pivot.wtk.Display;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.TableView;
import pivot.wtk.TableViewRowListener;
import pivot.wtk.TableViewListener;
import pivot.wtk.TextInput;
import pivot.wtk.Window;
import pivot.wtk.WindowStateListener;

/**
 * Default table view cell editor.
 *
 * @author tvolkert
 */
public class TableViewCellEditor implements TableView.RowEditor {
    /**
     * Responsible for cancelling the edit if any relevant changes are made to
     * the table view while we're editing.
     */
    private TableViewListener tableViewListener = new TableViewListener.Adapter() {
        @Override
        public void tableDataChanged(TableView tableView, List<?> previousTableData) {
            cancel();
        }

        @Override
        public void rowEditorChanged(TableView tableView, TableView.RowEditor previousRowEditor) {
            cancel();
        }
    };

    /**
     * Responsible for cancelling the edit if any changes are made to
     * the table data while we're editing.
     */
    private TableViewRowListener tableViewRowListener = new TableViewRowListener.Adapter() {
        @Override
        public void rowInserted(TableView tableView, int rowIndex) {
            cancel();
        }

        @Override
        public void rowsRemoved(TableView tableView, int rowIndex, int count) {
            cancel();
        }

        @Override
        public void rowUpdated(TableView tableView, int rowIndex) {
            cancel();
        }

        @Override
        public void rowsSorted(TableView tableView) {
            cancel();
        }
    };

    /**
     * Responsible for saving or cancelling the edit based on the user pressing
     * the <tt>ENTER</tt> key or the <tt>ESCAPE</tt> key, respectively.
     */
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

    /**
     * Responsible for "edit initialization" and "edit finalization" tasks when
     * the edit popup is opened and closed, respectively.
     */
    private WindowStateListener popupWindowStateHandler = new WindowStateListener.Adapter() {
        @Override
        public void windowOpened(Window window) {
            Display display = window.getDisplay();
            display.getContainerMouseListeners().add(displayMouseHandler);

            tableView.getTableViewListeners().add(tableViewListener);
            tableView.getTableViewRowListeners().add(tableViewRowListener);
        }

        @Override
        public void windowClosed(Window window, Display display) {
            // Clean up
            display.getContainerMouseListeners().remove(displayMouseHandler);

            tableView.getTableViewListeners().remove(tableViewListener);
            tableView.getTableViewRowListeners().remove(tableViewRowListener);

            // Restore focus to the table view
            tableView.requestFocus();

            // Free memory
            tableView = null;
            textInput = null;
            popup = null;
        }
    };

    /**
     * Responsible for closing the popup whenever the user clicks outside the
     * bounds of the popup.
     */
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

    private TableView tableView = null;
    private int rowIndex = -1;
    private int columnIndex = -1;

    private TextInput textInput = null;
    private Window popup = null;

    @SuppressWarnings("unchecked")
    public void edit(TableView tableView, int rowIndex, int columnIndex) {
        if (this.tableView != null) {
            throw new IllegalStateException("Currently editing.");
        }

        this.tableView = tableView;
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;

        // Get the row data, represented as a Dictionary
        Object tableRow = tableView.getTableData().get(rowIndex);
        Dictionary<String, Object> rowData;
        if (tableRow instanceof Dictionary<?, ?>) {
            rowData = (Dictionary<String, Object>)tableRow;
        } else {
            rowData = new BeanDictionary(tableRow);
        }

        // Get the data being edited
        String columnName = tableView.getColumns().get(columnIndex).getName();
        Object cellData = rowData.get(columnName);

        // Get the cell bounds
        Bounds cellBounds = tableView.getCellBounds(rowIndex, columnIndex);
        tableView.scrollAreaToVisible(cellBounds);
        cellBounds = tableView.getVisibleArea(cellBounds);

        // Create the text input
        textInput = new TextInput();
        textInput.setText(cellData == null ? "" : cellData.toString());
        textInput.setPreferredWidth(cellBounds.width);
        textInput.getComponentKeyListeners().add(textInputKeyHandler);

        // Create and open the popup
        popup = new Window(textInput, true);
        popup.getWindowStateListeners().add(popupWindowStateHandler);
        popup.setLocation(cellBounds.x, cellBounds.y
            + (cellBounds.height - textInput.getPreferredHeight(-1)) / 2);
        popup.open(tableView.getWindow());

        textInput.requestFocus();
    }

    public boolean isEditing() {
        return (tableView != null);
    }

    @SuppressWarnings("unchecked")
    public void save() {
        if (!isEditing()) {
            throw new IllegalStateException();
        }

        List<Object> tableData = (List<Object>)tableView.getTableData();

        // Get the row data, represented as a Dictionary
        Object tableRow = tableData.get(rowIndex);
        Dictionary<String, Object> rowData;
        if (tableRow instanceof Dictionary<?, ?>) {
            rowData = (Dictionary<String, Object>)tableRow;
        } else {
            rowData = new BeanDictionary(tableRow);
        }

        // Update the cell data
        String text = textInput.getText();
        String columnName = tableView.getColumns().get(columnIndex).getName();
        rowData.put(columnName, text);

        // Notifying the parent will close the popup
        if (tableData.getComparator() == null) {
            tableData.update(rowIndex, tableRow);
        } else {
            // Save local reference to members variables before they get cleared
            TableView tableView = this.tableView;

            tableData.remove(rowIndex, 1);
            tableData.add(tableRow);

            // Re-select the row, and make sure it's visible
            rowIndex = tableData.indexOf(tableRow);
            tableView.setSelectedIndex(rowIndex);
            tableView.scrollAreaToVisible(tableView.getRowBounds(rowIndex));
        }
    }

    public void cancel() {
        if (!isEditing()) {
            throw new IllegalStateException();
        }

        popup.close();
    }
}
