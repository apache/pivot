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

import java.awt.Color;
import java.awt.Graphics2D;

import pivot.beans.BeanDictionary;
import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.collections.List;
import pivot.util.Vote;
import pivot.wtk.Bounds;
import pivot.wtk.CardPane;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.ComponentListener;
import pivot.wtk.Container;
import pivot.wtk.ContainerMouseListener;
import pivot.wtk.Display;
import pivot.wtk.ImageView;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.ScrollPane;
import pivot.wtk.TablePane;
import pivot.wtk.TableView;
import pivot.wtk.TableViewListener;
import pivot.wtk.TableViewRowListener;
import pivot.wtk.TextInput;
import pivot.wtk.Viewport;
import pivot.wtk.ViewportListener;
import pivot.wtk.Window;
import pivot.wtk.WindowStateListener;
import pivot.wtk.effects.FlipTransition;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;
import pivot.wtk.media.Image;

/**
 * Default table view row editor.
 *
 * @author tvolkert
 */
public class TableViewRowEditor implements TableView.RowEditor {
    /**
     * Paints the row being edited.
     *
     * @author tvolkert
     */
    private Image tableRowImage = new Image() {
        public int getWidth() {
            return (tableView == null) ? 0 : tableView.getRowBounds(rowIndex).width;
        }

        public int getHeight() {
            return (tableView == null) ? 0 : tableView.getRowBounds(rowIndex).height;
        }

        public void paint(Graphics2D graphics) {
            Bounds rowBounds = tableView.getRowBounds(rowIndex);
            int width = rowBounds.width;
            int height = rowBounds.height;

            TableView.ColumnSequence columns = tableView.getColumns();
            Component.StyleDictionary styles = tableView.getStyles();

            boolean rowSelected = tableView.isRowSelected(rowIndex);
            boolean rowDisabled = tableView.isRowDisabled(rowIndex);

            // Paint the background
            Color backgroundColor = (Color)styles.get("backgroundColor");

            if (rowSelected) {
                backgroundColor = tableView.isFocused() ?
                    (Color)styles.get("selectionBackgroundColor") :
                    (Color)styles.get("inactiveSelectionBackgroundColor");
            } else {
                Color alternateRowColor = (Color)styles.get("alternateRowColor");

                if (alternateRowColor != null && rowIndex % 2 > 0) {
                    backgroundColor = alternateRowColor;
                }
            }

            if (backgroundColor != null) {
                graphics.setPaint(backgroundColor);
                graphics.fillRect(0, 0, width, height);
            }

            // Paint the cells
            Object tableRow = tableView.getTableData().get(rowIndex);
            int cellX = 0;

            for (int i = 0, n = columns.getLength(); i < n; i++) {
                TableView.Column column = columns.get(i);
                TableView.CellRenderer cellRenderer = column.getCellRenderer();

                int columnWidth = tableView.getColumnBounds(i).width;

                Graphics2D rendererGraphics = (Graphics2D)graphics.create(cellX, 0, columnWidth, height);

                cellRenderer.render(tableRow, tableView, column, rowSelected, false, rowDisabled);
                cellRenderer.setSize(columnWidth, height - 1);
                cellRenderer.paint(rendererGraphics);

                rendererGraphics.dispose();

                cellX += columnWidth + 1;
            }
        }
    };

    /**
     * Responsible for "edit initialization" and "edit finalization" tasks when
     * the edit popup is opened and closed, respectively.  Also responsible for
     * running the flip transition to close the popup.
     *
     * @author tvolkert
     */
    private WindowStateListener popupStateHandler = new WindowStateListener.Adapter() {
        private boolean closeTransitionStarted = false;
        private boolean closeTransitionComplete = false;

        @Override
        public void windowOpened(Window window) {
            Display display = window.getDisplay();
            display.getContainerMouseListeners().add(displayMouseHandler);

            tableView.getComponentListeners().add(tableViewComponentHandler);
            tableView.getTableViewListeners().add(tableViewHandler);
            tableView.getTableViewRowListeners().add(tableViewRowHandler);

            // Scroll the editor to match that of the table view
            if (tableViewScrollPane != null) {
                editorScrollPane.setScrollLeft(tableViewScrollPane.getScrollLeft());
            }

            // Start the "flip open" transition
            flipTransition = new FlipTransition(FLIP_DURATION, editorCardPane, 0, Math.PI);

            flipTransition.start(new TransitionListener() {
                public void transitionCompleted(Transition transition) {
                    Component focusComponent = editorTablePane.getCellComponent(0, columnIndex);
                    focusComponent.requestFocus();
                }
            });
        }

        @Override
        public Vote previewWindowClose(Window window) {
            Vote vote = (closeTransitionComplete ? Vote.APPROVE : Vote.DEFER);

            if (!closeTransitionStarted) {
                closeTransitionStarted = true;

                // Restore focus to the table view
                tableView.requestFocus();

                int duration = FLIP_DURATION;
                double beginTheta = Math.PI;

                // If we're still flipping open, then start the reverse flip at
                // the point where we're stopping the current flip
                if (flipTransition.isRunning()) {
                    flipTransition.stop();
                    duration = flipTransition.getElapsedTime();
                    beginTheta = flipTransition.getCurrentTheta();
                }

                flipTransition.setDuration(duration);
                flipTransition.setBeginTheta(beginTheta);
                flipTransition.setEndTheta(0);

                flipTransition.start(new TransitionListener() {
                    public void transitionCompleted(Transition transition) {
                        closeTransitionComplete = true;
                        popup.close();
                    }
                });
            }

            return vote;
        }

        @Override
        public void windowClosed(Window window, Display display) {
            // Clean up
            display.getContainerMouseListeners().remove(displayMouseHandler);

            tableView.getComponentListeners().remove(tableViewComponentHandler);
            tableView.getTableViewListeners().remove(tableViewHandler);
            tableView.getTableViewRowListeners().remove(tableViewRowHandler);

            // Reset flags
            closeTransitionStarted = false;
            closeTransitionComplete = false;

            // Free memory
            tableView = null;
            tableViewScrollPane = null;
            flipTransition = null;

            TablePane.ColumnSequence tablePaneColumns = editorTablePane.getColumns();
            TablePane.Row tablePaneRow = editorTablePane.getRows().get(0);
            tablePaneColumns.remove(0, tablePaneColumns.getLength());
            tablePaneRow.remove(0, tablePaneRow.getLength());
        }
    };

    /**
     * Responsible for saving or cancelling the edit based on the user pressing
     * the <tt>ENTER</tt> key or the <tt>ESCAPE</tt> key, respectively.
     *
     * @author tvolkert
     */
    private ComponentKeyListener popupKeyHandler = new ComponentKeyListener.Adapter() {
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
     * Responsible for closing the popup whenever the user clicks outside the
     * bounds of the popup.
     *
     * @author tvolkert
     */
    private ContainerMouseListener displayMouseHandler = new ContainerMouseListener.Adapter() {
        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            boolean consumed = false;

            // If the event occurred outside the popup, close the popup
            Display display = (Display)container;
            Window window = (Window)display.getComponentAt(x, y);

            if (popup != window &&
                (window == null || !popup.isOwner(window))) {
                save();
                consumed = true;
            }

            return consumed;
        }

        @Override
        public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            boolean consumed = false;

            // If the event occurred outside the popup, consume the event
            Display display = (Display)container;
            Window window = (Window)display.getComponentAt(x, y);

            if (popup != window &&
                (window == null || !popup.isOwner(window))) {
                consumed = true;
            }

            return consumed;
        }
    };

    /**
     * Responsible for cancelling the edit if the table view's size changes.
     *
     * @author tvolkert
     */
    private ComponentListener tableViewComponentHandler = new ComponentListener.Adapter() {
        @Override
        public void sizeChanged(Component component, int previousWidth, int previousHeight) {
            cancel();
        }
    };

    /**
     * Responsible for cancelling the edit if any relevant changes are made to
     * the table view while we're editing.
     *
     * @author tvolkert
     */
    private TableViewListener tableViewHandler = new TableViewListener.Adapter() {
        @Override
        public void rowEditorChanged(TableView tableView, TableView.RowEditor previousRowEditor) {
            cancel();
        }

        @Override
        public void tableDataChanged(TableView tableView, List<?> previousTableData) {
            cancel();
        }
    };

    /**
     * Responsible for cancelling the edit if any changes are made to the table
     * data while we're editing.
     *
     * @author tvolkert
     */
    private TableViewRowListener tableViewRowHandler = new TableViewRowListener.Adapter() {
        @Override
        public void rowInserted(TableView tableView, int index) {
            cancel();
        }

        @Override
        public void rowsRemoved(TableView tableView, int index, int count) {
            cancel();
        }

        @Override
        public void rowsSorted(TableView tableView) {
            cancel();
        }

        @Override
        public void rowsCleared(TableView tableView) {
            cancel();
        }

        @Override
        public void rowUpdated(TableView tableView, int index) {
            cancel();
        }
    };

    /**
     * Responsible for keeping the table view scroll pane's scrollLeft value in
     * sync with the editor scroll pane's scrollLeft value.
     *
     * @author tvolkert
     */
    private ViewportListener viewportHandler = new ViewportListener.Adapter() {
        @Override
        public void scrollLeftChanged(Viewport viewport, int previousScrollLeft) {
            if (tableViewScrollPane != null) {
                tableViewScrollPane.setScrollLeft(viewport.getScrollLeft());
            }
        }
    };

    // Transient data (only meaningful during editing)
    private TableView tableView = null;
    private ScrollPane tableViewScrollPane = null;
    private int rowIndex;
    private int columnIndex;

    // Editor components (persistent across edits)
    private Window popup;
    private ScrollPane editorScrollPane;
    private CardPane editorCardPane;
    private TablePane editorTablePane;

    // Cell editors specified by the caller
    private HashMap<String, Component> cellEditors = new HashMap<String, Component>();

    // Transition
    private FlipTransition flipTransition = null;

    // The duration in milliseconds of a full (non-interrupted) transition
    private static final int FLIP_DURATION = 350;

    /**
     * Creates a new <tt>TableViewRowEditor</tt>. This object should only be
     * associated with one table view at a time.
     */
    public TableViewRowEditor() {
        // Create the editor components
        popup = new Window(true);
        editorScrollPane = new ScrollPane(ScrollPane.ScrollBarPolicy.NEVER, ScrollPane.ScrollBarPolicy.FILL);
        editorCardPane = new CardPane();
        editorTablePane = new TablePane();

        // Set up the editor component hierarchy
        popup.setContent(editorScrollPane);
        editorScrollPane.setView(editorCardPane);
        editorCardPane.add(new ImageView(tableRowImage));
        editorCardPane.add(editorTablePane);
        editorTablePane.getRows().add(new TablePane.Row(1, true));

        // Register listeners
        editorScrollPane.getViewportListeners().add(viewportHandler);
        popup.getWindowStateListeners().add(popupStateHandler);
        popup.getComponentKeyListeners().add(popupKeyHandler);
    }

    /**
     * Gets this row editor's cell editor dictionary. The caller may specify
     * explicit editor components and place them in this dictionary by their
     * table view column names. Any column that does not have an entry in this
     * dictionary will have a <tt>TextInput</tt> implicitly associated with it
     * during editing.
     * <p>
     * This row editor uses data binding to populate the cell editor components
     * and to get the data back out of those components, so it is the caller's
     * responsibility to set up the data binding keys in each component they
     * specify in this dictionary. The data binding key should equal the column
     * name that the cell editor serves.
     *
     * @return
     * The cell editor dictionary.
     */
    public Dictionary<String, Component> getCellEditors() {
        return cellEditors;
    }

    @SuppressWarnings("unchecked")
    public void edit(TableView tableView, int rowIndex, int columnIndex) {
        if (isEditing()) {
            throw new IllegalStateException();
        }

        this.tableView = tableView;
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;

        Container tableViewParent = tableView.getParent();
        if (tableViewParent instanceof ScrollPane) {
            tableViewScrollPane = (ScrollPane)tableViewParent;
        }

        // Match the table pane's columns to the table view's
        TableView.ColumnSequence tableViewColumns = tableView.getColumns();
        TablePane.ColumnSequence tablePaneColumns = editorTablePane.getColumns();
        TablePane.Row tablePaneRow = editorTablePane.getRows().get(0);

        for (int i = 0, n = tableViewColumns.getLength(); i < n; i++) {
            // Add a new column to the table pane to match the table view column
            TablePane.Column tablePaneColumn = new TablePane.Column();
            tablePaneColumns.add(tablePaneColumn);

            // Size the table pane column to match that of the table view
            // column. We get the real-time column width from the table view as
            // opposed to the width property of the column, because the latter
            // may represent a relative width, and we need the actual width
            int columnWidth = tableView.getColumnBounds(i).width;
            tablePaneColumn.setWidth(columnWidth, false);

            // Determine which component to use as the editor for this column
            String columnName = tableViewColumns.get(i).getName();
            Component editorComponent = cellEditors.get(columnName);

            // Default to a TextInput editor
            if (editorComponent == null) {
                TextInput editorTextInput = new TextInput();
                editorTextInput.setTextKey(columnName);
                editorComponent = editorTextInput;
            }

            // Add the editor component to the table pane
            tablePaneRow.add(editorComponent);

            if (columnWidth == 0) {
                // Remove non-visible components from focus contention
                editorComponent.setEnabled(false);
            }
        }

        // Get the row data, represented as a Dictionary
        Object tableRow = tableView.getTableData().get(rowIndex);
        Dictionary<String, Object> rowData;
        if (tableRow instanceof Dictionary<?, ?>) {
            rowData = (Dictionary<String, Object>)tableRow;
        } else {
            rowData = new BeanDictionary(tableRow);
        }

        // Load the row data into the editor components
        editorTablePane.load(rowData);

        // Calculate the visible bounds of the row
        Bounds editBounds = tableView.getRowBounds(rowIndex);
        tableView.scrollAreaToVisible(editBounds);
        editBounds = tableView.getVisibleArea(editBounds);

        // Open the popup over the row
        popup.setLocation(editBounds.x, editBounds.y);
        popup.setPreferredSize(editBounds.width, editBounds.height);
        popup.open(tableView.getWindow());
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

        // Update the row data using data binding
        editorTablePane.store(rowData);

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
