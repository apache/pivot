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

import java.awt.Graphics2D;

import org.apache.pivot.beans.BeanDictionary;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.CardPane;
import org.apache.pivot.wtk.CardPaneListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.DragSource;
import org.apache.pivot.wtk.DropTarget;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewListener;
import org.apache.pivot.wtk.TableViewRowListener;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Viewport;
import org.apache.pivot.wtk.ViewportListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.CardPaneSkin;

/**
 * Default table view row editor.
 */
public class TableViewRowEditor implements TableView.RowEditor {
    /**
     * Paints the row being edited.
     */
    private static class ComponentImage extends Image {
        private Component component;
        private int x;
        private int y;
        private int width;
        private int height;

        public ComponentImage(Component component, Bounds bounds) {
            this(component, bounds.x, bounds.y, bounds.width, bounds.height);
        }

        public ComponentImage(Component component, int x, int y, int width, int height) {
            this.component = component;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public void paint(Graphics2D graphics) {
            graphics.translate(-x, -y);
            graphics.clipRect(x, y, width, height);
            component.paint(graphics);
        }
    }

    /**
     *
     */
    private class EditorPopup extends Window implements ContainerMouseListener,
        ComponentListener, TableViewListener, TableViewRowListener {
        // Fields that determine what is being edited
        private final TableView tableView;
        private final int rowIndex;
        private final int columnIndex;

        // Child components
        private ScrollPane scrollPane;
        private CardPane cardPane;
        private TablePane tablePane;

        private boolean opening = false;
        private boolean closing = false;
        private boolean saving = false;

        private ScrollPane tableViewScrollPane = null;

        @SuppressWarnings("unchecked")
        public EditorPopup(TableView tableView, int rowIndex, int columnIndex) {
            this.tableView = tableView;
            this.rowIndex = rowIndex;
            this.columnIndex = columnIndex;

            // Get the row data, represented as a Dictionary
            Object tableRow = tableView.getTableData().get(rowIndex);
            Dictionary<String, Object> rowData;
            BeanDictionary beanDictionary = null;
            if (tableRow instanceof Dictionary<?, ?>) {
                rowData = (Dictionary<String, Object>)tableRow;
            } else {
                beanDictionary = new BeanDictionary(tableRow);
                rowData = beanDictionary;
            }

            // Set up the editor component hierarchy
            scrollPane = new ScrollPane(ScrollPane.ScrollBarPolicy.NEVER,
                ScrollPane.ScrollBarPolicy.FILL);
            setContent(scrollPane);

            cardPane = new CardPane();
            scrollPane.setView(cardPane);

            cardPane.add(new ImageView(new ComponentImage(tableView,
                tableView.getRowBounds(rowIndex))));
            cardPane.setSelectedIndex(0);
            cardPane.getStyles().put("selectionChangeEffect", editEffect);

            tablePane = new TablePane();
            tablePane.getStyles().put("horizontalSpacing", 1);
            cardPane.add(tablePane);

            TablePane.Row tablePaneRow = new TablePane.Row(1, true);
            tablePane.getRows().add(tablePaneRow);

            // Match the table pane's columns to the table view's
            TableView.ColumnSequence tableViewColumns = tableView.getColumns();
            TablePane.ColumnSequence tablePaneColumns = tablePane.getColumns();

            for (int i = 0, n = tableViewColumns.getLength(); i < n; i++) {
                // Add a new column to the table pane to match the table view column
                TablePane.Column tablePaneColumn = new TablePane.Column();
                tablePaneColumns.add(tablePaneColumn);

                // Determine which component to use as the editor for this column
                String columnName = tableViewColumns.get(i).getName();
                Component editorComponent = cellEditors.get(columnName);

                // Default to a TextInput editor
                if (editorComponent == null) {
                    TextInput editorTextInput = new TextInput();
                    editorTextInput.setTextKey(columnName);
                    editorComponent = editorTextInput;
                }

                // Disable the component for read-only properties
                if (beanDictionary != null
                    && beanDictionary.isReadOnly(columnName)) {
                    editorComponent.getUserData().put(READ_ONLY_KEY, true);
                }

                // Add the editor component to the table pane
                tablePaneRow.add(editorComponent);
            }

            // Load the row data into the editor components
            tablePane.load(rowData);

            // Keep the table view's scroll in sync with the editor's
            scrollPane.getViewportListeners().add(new ViewportListener.Adapter() {
                @Override
                public void scrollLeftChanged(Viewport viewport, int previousScrollLeft) {
                    if (tableViewScrollPane != null) {
                        tableViewScrollPane.setScrollLeft(viewport.getScrollLeft());
                    }
                }
            });
        }

        @Override
        public void open(Display display) {
            if (!isOpen()) {
                super.open(display);

                if (isOpen()) {
                    setOwner(tableView.getWindow());

                    display.getContainerMouseListeners().add(this);
                    tableView.getComponentListeners().add(this);
                    tableView.getTableViewListeners().add(this);
                    tableView.getTableViewRowListeners().add(this);

                    // Scroll the editor to match that of the table view
                    if (tableViewScrollPane != null) {
                        scrollPane.setScrollLeft(tableViewScrollPane.getScrollLeft());
                    }

                    // Set the opening flag
                    opening = true;

                    // Give the editor focus after the transition has completed.
                    // When the transition starts, the row image is the selected
                    // card. so we have to wait until the selected index changes
                    // to give focus to the appropriate editor component
                    cardPane.getCardPaneListeners().add(new CardPaneListener.Adapter() {
                        @Override
                        public void selectedIndexChanged(CardPane cardPane, int previousSelectedIndex) {
                            // Clear the opening flag
                            opening = false;

                            // Focus the initial editor component
                            Component focusComponent = tablePane.getCellComponent(0, columnIndex);
                            focusComponent.requestFocus();

                            // Remove this listener
                            cardPane.getCardPaneListeners().remove(this);
                        }
                    });

                    // Transition to the editor card
                    cardPane.setSelectedIndex(EDITOR_CARD_INDEX);
                }
            }
        }

        @Override
        public void close() {
            if (!isClosed()
                && !opening) {
                // Close once we've transitioned back to the image card
                if (cardPane.getSelectedIndex() == IMAGE_CARD_INDEX) {
                    Display display = getDisplay();
                    display.getContainerMouseListeners().remove(this);

                    super.close();
                    closing = false;

                    // Move the owner to front
                    getOwner().moveToFront();
                    setOwner(null);

                    // Clear the table pane row so the custom cell editors
                    // can be re-used in the next editor popup
                    TablePane.Row tablePaneRow = tablePane.getRows().get(0);
                    tablePaneRow.remove(0, tablePaneRow.getLength());

                    // This marks our editor as no longer editing
                    editorPopup = null;

                    if (saving) {
                        rowEditorListeners.changesSaved(TableViewRowEditor.this, tableView,
                            rowIndex, columnIndex);
                    } else {
                        rowEditorListeners.editCancelled(TableViewRowEditor.this, tableView,
                            rowIndex, columnIndex);
                    }
                } else if (!closing) {
                    closing = true;

                    tableView.getComponentListeners().remove(this);
                    tableView.getTableViewListeners().remove(this);
                    tableView.getTableViewRowListeners().remove(this);

                    // Disable the table pane to prevent interaction while closing
                    tablePane.setEnabled(false);

                    // Close this editor popup when the transition has completed
                    cardPane.getCardPaneListeners().add(new CardPaneListener.Adapter() {
                        @Override
                        public void selectedIndexChanged(CardPane cardPane,
                            int previousSelectedIndex) {
                            close();

                            // Remove this listener
                            cardPane.getCardPaneListeners().remove(this);
                        }
                    });

                    // Transition to the image card
                    cardPane.setSelectedIndex(IMAGE_CARD_INDEX);
                }
            }
        }

        @Override
        protected boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
            if (keyCode == Keyboard.KeyCode.ENTER) {
                saveChanges();
            } else if (keyCode == Keyboard.KeyCode.ESCAPE) {
                cancelEdit();
            }

            return super.keyPressed(keyCode, keyLocation);
        }

        public void setTableViewScrollPane(ScrollPane tableViewScrollPane) {
            this.tableViewScrollPane = tableViewScrollPane;
        }

        public void editRow() {
            open(tableView.getDisplay());
            reposition();
        }

        @SuppressWarnings("unchecked")
        public void saveChanges() {
            // Preview the changes
            HashMap<String, Object> changes = new HashMap<String, Object>();
            tablePane.store(changes);
            Vote vote = rowEditorListeners.previewSaveChanges(TableViewRowEditor.this, tableView,
                rowIndex, columnIndex, changes);

            if (vote == Vote.APPROVE) {
                saving = true;
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
                tablePane.store(rowData);

                // Modifying the table data will close this popup
                if (tableData.getComparator() == null) {
                    tableData.update(rowIndex, tableRow);
                } else {
                    tableData.remove(rowIndex, 1);
                    tableData.add(tableRow);

                    // Re-select the row, and make sure it's visible
                    int newRowIndex = tableData.indexOf(tableRow);
                    tableView.setSelectedIndex(newRowIndex);
                    tableView.scrollAreaToVisible(tableView.getRowBounds(newRowIndex));
                }
            } else if (vote == Vote.DENY) {
                saving = false;
                rowEditorListeners.saveChangesVetoed(TableViewRowEditor.this, vote);
            }
        }

        public void cancelEdit() {
            close();
        }

        /**
         * Repositions this editor popup to be over the row being edited.
         */
        private void reposition() {
            // Calculate the visible bounds of the row
            Bounds bounds = tableView.getRowBounds(rowIndex);
            tableView.scrollAreaToVisible(bounds);
            bounds = tableView.getVisibleArea(bounds);

            // Open this popup over the row
            setLocation(bounds.x, bounds.y);
            setPreferredSize(bounds.width, bounds.height + 1);

            // Match the table pane's columns to the table view's
            TableView.ColumnSequence tableViewColumns = tableView.getColumns();
            TablePane.ColumnSequence tablePaneColumns = tablePane.getColumns();
            TablePane.Row tablePaneRow = tablePane.getRows().get(0);

            for (int i = 0, n = tableViewColumns.getLength(); i < n; i++) {
                TablePane.Column tablePaneColumn = tablePaneColumns.get(i);

                // Size the table pane column to match that of the table view
                // column. We get the real-time column width from the table view as
                // opposed to the width property of the column, because the latter
                // may represent a relative width, and we need the actual width
                int columnWidth = tableView.getColumnBounds(i).width;
                tablePaneColumn.setWidth(columnWidth);

                // Disable the editor component if necessary
                Component editorComponent = tablePaneRow.get(i);
                boolean isReadOnly = (editorComponent.getUserData().get(READ_ONLY_KEY) != null);
                editorComponent.setEnabled(!isReadOnly && columnWidth > 0);
            }
        }

        // ContainerMouseListener methods

        @Override
        public boolean mouseMove(Container container, int x, int y) {
            return false;
        }

        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            if (!opening
                && !closing) {
                // If the event occurred outside the popup, close the popup
                Display display = (Display)container;
                Window window = (Window)display.getComponentAt(x, y);

                if (window != this &&
                    (window == null || !isOwner(window))) {
                    saveChanges();
                }
            }

            return opening;
        }

        @Override
        public boolean mouseUp(Container container, Mouse.Button button, int x, int y) {
            // No-op
            return false;
        }

        @Override
        public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            boolean consumed = false;

            // If the event occurred outside the popup, consume the event
            Display display = (Display)container;
            Window window = (Window)display.getComponentAt(x, y);

            if (window != this &&
                (window == null || !isOwner(window))) {
                consumed = true;
            }

            return consumed;
        }

        // ComponentListener methods

        @Override
        public void parentChanged(Component component, Container previousParent) {
            // No-op
        }

        @Override
        public void sizeChanged(Component component, int previousWidth, int previousHeight) {
            // Re-position the editor popup
            ApplicationContext.queueCallback(new Runnable() {
                @Override
                public void run() {
                    reposition();
                }
            });
        }

        @Override
        public void preferredSizeChanged(Component component, int previousPreferredWidth,
            int previousPreferredHeight) {
            // No-op
        }

        @Override
        public void preferredWidthLimitsChanged(Component component, int previousMinimumPreferredWidth,
            int previousMaximumPreferredWidth) {
            // No-op
        }

        @Override
        public void preferredHeightLimitsChanged(Component component, int previousMinimumPreferredHeight,
            int previousMaximumPreferredHeight) {
            // No-op
        }

        @Override
        public void locationChanged(Component component, int previousX, int previousY) {
            // Re-position the editor popup
            ApplicationContext.queueCallback(new Runnable() {
                @Override
                public void run() {
                    reposition();
                }
            });
        }

        @Override
        public void visibleChanged(Component component) {
            cancelEdit();
        }

        @Override
        public void styleUpdated(Component component, String styleKey, Object previousValue) {
            // No-op
        }

        @Override
        public void cursorChanged(Component component, Cursor previousCursor) {
            // No-op
        }

        @Override
        public void tooltipTextChanged(Component component, String previousTooltipText) {
            // No-op
        }

        @Override
        public void dragSourceChanged(Component component, DragSource previousDragSource) {
            // No-op
        }

        @Override
        public void dropTargetChanged(Component component, DropTarget previousDropTarget) {
            // No-op
        }

        @Override
        public void menuHandlerChanged(Component component, MenuHandler previousMenuHandler) {
            // No-op
        }

        // TableViewListener methods

        @Override
        public void tableDataChanged(TableView tableView, List<?> previousTableData) {
            cancelEdit();
        }

        @Override
        public void rowEditorChanged(TableView tableView, TableView.RowEditor previousRowEditor) {
            cancelEdit();
        }

        @Override
        public void selectModeChanged(TableView tableView, TableView.SelectMode previousSelectMode) {
            // No-op
        }

        @Override
        public void disabledRowFilterChanged(TableView tableView, Filter<?> previousDisabledRowFilter) {
            // No-op
        }

        // TableViewRowListener methods

        @Override
        public void rowInserted(TableView tableView, int index) {
            cancelEdit();
        }

        @Override
        public void rowsRemoved(TableView tableView, int index, int count) {
            cancelEdit();
        }

        @Override
        public void rowUpdated(TableView tableView, int index) {
            cancelEdit();
        }

        @Override
        public void rowsCleared(TableView tableView) {
            cancelEdit();
        }

        @Override
        public void rowsSorted(TableView tableView) {
            cancelEdit();
        }
    }

    private EditorPopup editorPopup = null;

    private HashMap<String, Component> cellEditors = new HashMap<String, Component>();

    private CardPaneSkin.SelectionChangeEffect editEffect = null;

    private RowEditorListenerList rowEditorListeners = new RowEditorListenerList();

    private static final int IMAGE_CARD_INDEX = 0;
    private static final int EDITOR_CARD_INDEX = 1;

    private static final String READ_ONLY_KEY = "readOnly";

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

    /**
     * Gets the effect that this editor uses when changing from a read-only
     * row to an editable row. By default, this editor uses no effect.
     *
     * @return
     * The edit effect, or <tt>null</tt> if no effect is being used.
     */
    public CardPaneSkin.SelectionChangeEffect getEditEffect() {
        return editEffect;
    }

    /**
     * Sets the effect that this editor uses when changing from a read-only
     * row to an editable row.
     *
     * @param editEffect
     * The edit effect, or <tt>null</tt> to not use an effect.
     */
    public void setEditEffect(CardPaneSkin.SelectionChangeEffect editEffect) {
        this.editEffect = editEffect;
    }

    /**
     * Sets the effect that this editor uses when changing from a read-only
     * row to an editable row.
     *
     * @param editEffect
     * The edit effect, or <tt>null</tt> to not use an effect.
     *
     * @see #setEditEffect(CardPaneSkin.SelectionChangeEffect)
     */
    public void setEditEffect(String editEffect) {
        if (editEffect == null) {
            throw new IllegalArgumentException();
        }

        setEditEffect(CardPaneSkin.SelectionChangeEffect.valueOf(editEffect.toUpperCase()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void editRow(TableView tableView, int rowIndex, int columnIndex) {
        if (editorPopup != null) {
            throw new IllegalStateException("Edit already in progress.");
        }

        if (tableView == null) {
            throw new IllegalArgumentException("tableView is null.");
        }

        if (rowIndex < 0
            || rowIndex >= tableView.getTableData().getLength()
            || columnIndex < 0
            || columnIndex >= tableView.getColumns().getLength()) {
            throw new IndexOutOfBoundsException();
        }

        Vote vote = rowEditorListeners.previewEditRow(this, tableView, rowIndex, columnIndex);

        if (vote == Vote.APPROVE) {
            editorPopup = new EditorPopup(tableView, rowIndex, columnIndex);

            Container tableViewParent = tableView.getParent();
            if (tableViewParent instanceof ScrollPane) {
                editorPopup.setTableViewScrollPane((ScrollPane)tableViewParent);
            }

            editorPopup.editRow();

            rowEditorListeners.rowEditing(this, tableView, rowIndex, columnIndex);
        } else if (vote == Vote.DENY) {
            rowEditorListeners.editRowVetoed(this, vote);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEditing() {
        return (editorPopup != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveChanges() {
        if (editorPopup == null) {
            throw new IllegalStateException("No edit in progress.");
        }

        editorPopup.saveChanges();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelEdit() {
        if (editorPopup == null) {
            throw new IllegalStateException("No edit in progress.");
        }

        editorPopup.cancelEdit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListenerList<TableView.RowEditorListener> getRowEditorListeners() {
        return rowEditorListeners;
    }
}
