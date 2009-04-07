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
package pivot.demos.roweditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Comparator;

import pivot.collections.HashMap;
import pivot.collections.List;
import pivot.collections.Map;
import pivot.serialization.JSONSerializer;
import pivot.util.CalendarDate;
import pivot.util.Vote;
import pivot.wtk.Action;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Bounds;
import pivot.wtk.CardPane;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.Container;
import pivot.wtk.ContainerMouseListener;
import pivot.wtk.Display;
import pivot.wtk.ImageView;
import pivot.wtk.Keyboard;
import pivot.wtk.ListButton;
import pivot.wtk.ListView;
import pivot.wtk.Mouse;
import pivot.wtk.Point;
import pivot.wtk.ScrollPane;
import pivot.wtk.Spinner;
import pivot.wtk.TablePane;
import pivot.wtk.TableView;
import pivot.wtk.TableViewListener;
import pivot.wtk.TableViewRowListener;
import pivot.wtk.TextInput;
import pivot.wtk.Viewport;
import pivot.wtk.ViewportListener;
import pivot.wtk.Window;
import pivot.wtk.WindowStateListener;
import pivot.wtk.content.CalendarDateSpinnerData;
import pivot.wtk.effects.FlipTransition;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;
import pivot.wtk.media.Image;
import pivot.wtkx.WTKXSerializer;

/**
 * Editor for expenses rows.
 *
 * @author tvolkert
 */
@SuppressWarnings("unchecked")
public class TableViewRowEditor implements TableView.RowEditor {
    /**
     * Paints the row being edited.
     *
     * @author tvolkert
     */
    private Image tableRowImage = new Image() {
        public int getWidth() {
            return tableView.getRowBounds(rowIndex).width;
        }

        public int getHeight() {
            return tableView.getRowBounds(rowIndex).height;
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
    private WindowStateListener popupStateHandler = new WindowStateListener() {
        private boolean transitionStarted = false;
        private boolean transitionComplete = false;

        public Vote previewWindowOpen(Window window, Display display) {
            return Vote.APPROVE;
        }

        public void windowOpenVetoed(Window window, Vote reason) {
        }

        public void windowOpened(Window window) {
            Display display = window.getDisplay();
            display.getContainerMouseListeners().add(displayMouseHandler);

            tableView.getTableViewListeners().add(tableViewHandler);
            tableView.getTableViewRowListeners().add(tableViewRowHandler);

            // Scroll the editor to match that of the table view
            ScrollPane scrollPane = (ScrollPane)wtkxSerializer.getObjectByName("scrollPane");
            scrollPane.setScrollLeft(tableViewScrollPane.getScrollLeft());

            // Start the "flip open" transition
            CardPane cardPane = (CardPane)wtkxSerializer.getObjectByName("cardPane");
            flipTransition = new FlipTransition(FLIP_DURATION, cardPane, 0, Math.PI);

            flipTransition.start(new TransitionListener() {
                public void transitionCompleted(Transition transition) {
                    TablePane tablePane = (TablePane)wtkxSerializer.getObjectByName("tablePane");
                    Component focusComponent = tablePane.getCellComponent(0, columnIndex);
                    focusComponent.requestFocus();
                }
            });
        }

        public Vote previewWindowClose(Window window) {
            Vote vote = transitionComplete ? Vote.APPROVE : Vote.DEFER;

            if (!transitionStarted) {
                transitionStarted = true;

                int duration = FLIP_DURATION;
                double beginTheta = Math.PI;

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
                        transitionComplete = true;
                        popup.close();
                    }
                });
            }

            return vote;
        }

        public void windowCloseVetoed(Window window, Vote reason) {
        }

        public void windowClosed(Window window, Display display) {
            // Clean up
            display.getContainerMouseListeners().remove(displayMouseHandler);

            tableView.getTableViewListeners().remove(tableViewHandler);
            tableView.getTableViewRowListeners().remove(tableViewRowHandler);

            transitionStarted = false;

            // Restore focus to the table view
            tableView.requestFocus();

            // Free memory
            tableView = null;
            wtkxSerializer = null;
            popup = null;
            flipTransition = null;
        }
    };

    /**
     * Responsible for saving or cancelling the edit based on the user pressing
     * the <tt>ENTER</tt> key or the <tt>ESCAPE</tt> key, respectively.
     *
     * @author tvolkert
     */
    private ComponentKeyListener popupKeyHandler = new ComponentKeyListener() {
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

        public boolean keyTyped(Component component, char character)  {
            return false;
        }
    };

    /**
     * Responsible for closing the popup whenever the user clicks outside the
     * bounds of the popup.
     *
     * @author tvolkert
     */
    private ContainerMouseListener displayMouseHandler = new ContainerMouseListener() {
        public boolean mouseMove(Container container, int x, int y) {
            return false;
        }

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

        public boolean mouseUp(Container container, Mouse.Button button, int x, int y) {
            return false;
        }

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
     * Responsible for cancelling the edit if any relevant changes are made to
     * the table view while we're editing.
     *
     * @author tvolkert
     */
    private TableViewListener tableViewHandler = new TableViewListener() {
        public void rowEditorChanged(TableView tableView, TableView.RowEditor previousRowEditor) {
            cancel();
        }

        public void selectModeChanged(TableView tableView, TableView.SelectMode previousSelectMode) {
        }

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
    private TableViewRowListener tableViewRowHandler = new TableViewRowListener() {
        public void rowInserted(TableView tableView, int index) {
            cancel();
        }

        public void rowsRemoved(TableView tableView, int index, int count) {
            cancel();
        }

        public void rowsSorted(TableView tableView) {
            cancel();
        }

        public void rowUpdated(TableView tableView, int index) {
            cancel();
        }
    };

    /**
     * Responsible for keeping the expenses scroll pane's scrollLeft value in
     * sync with the popup scroll pane's scrollLeft value.
     *
     * @author tvolkert
     */
    private ViewportListener viewportHandler = new ViewportListener() {
        public void scrollLeftChanged(Viewport viewport, int previousScrollLeft) {
            tableViewScrollPane.setScrollLeft(viewport.getScrollLeft());
        }

        public void scrollTopChanged(Viewport scrollPane, int previousScrollTop) {
        }

        public void viewChanged(Viewport scrollPane, Component previousView) {
        }
    };

    private ScrollPane tableViewScrollPane = null;

    private TableView tableView = null;
    private int rowIndex;
    private int columnIndex;

    private WTKXSerializer wtkxSerializer = null;
    private Window popup = null;

    private FlipTransition flipTransition = null;

    private static final int FLIP_DURATION = 350;

    public ScrollPane getTableViewScrollPane() {
        return tableViewScrollPane;
    }

    public void setTableViewScrollPane(ScrollPane tableViewScrollPane) {
        this.tableViewScrollPane = tableViewScrollPane;
    }

    public void edit(TableView tableView, int rowIndex, int columnIndex) {
        if (isEditing()) {
            throw new IllegalStateException();
        }

        this.tableView = tableView;
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;

        // Get the data being edited
        List<CustomTableRow> tableData = (List<CustomTableRow>)tableView.getTableData();
        CustomTableRow tableRow = tableData.get(rowIndex);

        // Create the editor popup
        popup = new Window();
        popup.getWindowStateListeners().add(popupStateHandler);
        popup.getComponentKeyListeners().add(popupKeyHandler);

        // Set the content of the popup
        wtkxSerializer = new WTKXSerializer();

        try {
            popup.setContent((Component)wtkxSerializer.readObject(getClass().getResource("editor.wtkx")));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        // Match the editor's column widths to the table view's
        TablePane tablePane = (TablePane)wtkxSerializer.getObjectByName("tablePane");
        TablePane.ColumnSequence columns = tablePane.getColumns();

        for (int i = 0, n = columns.getLength(); i < n; i++) {
            int columnWidth = tableView.getColumnBounds(i).width;
            columns.get(i).setWidth(columnWidth, false);

            if (columnWidth == 0) {
                // Remove non-visible components from focus contention
                tablePane.getCellComponent(0, i).setEnabled(false);
            }
        }

        // Tie the table view's scroll to the popup's
        ScrollPane scrollPane = (ScrollPane)wtkxSerializer.getObjectByName("scrollPane");
        scrollPane.getViewportListeners().add(viewportHandler);

        tablePane.load(tableRow);

        // Set the editor's table row image
        ImageView imageView = (ImageView)wtkxSerializer.getObjectByName("imageView");
        imageView.setImage(tableRowImage);

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

    public void save() {
        if (!isEditing()) {
            throw new IllegalStateException();
        }

        List<CustomTableRow> tableData = (List<CustomTableRow>)tableView.getTableData();
        CustomTableRow tableRow = tableData.get(rowIndex);

        // Update the row data using data binding
        TablePane tablePane = (TablePane)wtkxSerializer.getObjectByName("tablePane");
        tablePane.store(tableRow);

        // Notifying the table data of the update will close the popup
        tableData.update(rowIndex, tableRow);
    }

    public void cancel() {
        if (!isEditing()) {
            throw new IllegalStateException();
        }

        popup.close();
    }
}
