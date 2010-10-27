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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.CardPane;
import org.apache.pivot.wtk.CardPaneListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Viewport;
import org.apache.pivot.wtk.ViewportListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.CardPaneSkin;

/**
 * Default table view row editor.
 */
public class TableViewRowEditor extends Window implements TableView.RowEditor {
    private class RowImage extends Image {
        private Bounds bounds = new Bounds(0, 0, 0, 0);

        @Override
        public int getWidth() {
            return bounds.width;
        }

        @Override
        public int getHeight() {
            return bounds.height;
        }

        @Override
        public void paint(Graphics2D graphics) {
            graphics.translate(-bounds.x, -bounds.y);
            graphics.clipRect(bounds.x, bounds.y, bounds.width, bounds.height);
            tableView.paint(graphics);
        }
    }

    private TableView tableView = null;
    private int rowIndex = -1;
    private int columnIndex = -1;

    private ScrollPane tableViewScrollPane = null;

    private ScrollPane scrollPane = new ScrollPane(ScrollPane.ScrollBarPolicy.NEVER,
        ScrollPane.ScrollBarPolicy.FILL);
    private CardPane cardPane = new CardPane();
    private TablePane tablePane = new TablePane();
    private TablePane.Row editorRow = new TablePane.Row();

    private RowImage rowImage = new RowImage();

    private HashMap<String, Component> cellEditors = new HashMap<String, Component>();

    private ContainerMouseListener displayMouseHandler = new ContainerMouseListener.Adapter() {
        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            Display display = (Display)container;
            Window window = (Window)display.getComponentAt(x, y);

            boolean consumed = false;
            if (window != TableViewRowEditor.this
                && (window == null || !isOwner(window))) {
                close(true);
                consumed = true;
            }

            return consumed;
        }

        @Override
        public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            Display display = (Display)container;
            Window window = (Window)display.getComponentAt(x, y);

            return (window != TableViewRowEditor.this);
        }
    };

    private static final int IMAGE_CARD_INDEX = 0;
    private static final int EDITOR_CARD_INDEX = 1;

    public TableViewRowEditor() {
        setContent(scrollPane);
        scrollPane.setView(cardPane);
        scrollPane.getViewportListeners().add(new ViewportListener.Adapter() {
            @Override
            public void scrollLeftChanged(Viewport viewport, int previousScrollLeft) {
                if (tableViewScrollPane != null) {
                    tableViewScrollPane.setScrollLeft(viewport.getScrollLeft());
                }
            }
        });

        cardPane.add(new ImageView(rowImage));
        cardPane.add(tablePane);
        cardPane.getCardPaneListeners().add(new CardPaneListener.Adapter() {
            @Override
            public void selectedIndexChanged(CardPane cardPane, int previousSelectedIndex) {
                if (previousSelectedIndex == IMAGE_CARD_INDEX) {
                    editorRow.get(columnIndex).requestFocus();
                } else {
                    close();
                }
            }
        });

        tablePane.getStyles().put("horizontalSpacing", 1);
        tablePane.getRows().add(editorRow);
    }

    @Override
    public void edit(TableView tableView, int rowIndex, int columnIndex) {
        this.tableView = tableView;
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;

        Container tableViewParent = tableView.getParent();
        tableViewScrollPane = (tableViewParent instanceof ScrollPane) ? (ScrollPane)tableViewParent : null;

        open(tableView.getWindow());
    }

    /**
     * Gets this row editor's cell editor dictionary. The caller may specify
     * explicit editor components and place them in this dictionary by their
     * table view column names. Any column that does not have an entry in this
     * dictionary will have a {@link TextInput} implicitly associated with it
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
        return (CardPaneSkin.SelectionChangeEffect)cardPane.getStyles().get("selectionChangeEffect");
    }

    /**
     * Sets the effect that this editor uses when changing from a read-only
     * row to an editable row.
     *
     * @param editEffect
     * The edit effect, or <tt>null</tt> for no effect.
     */
    public void setEditEffect(CardPaneSkin.SelectionChangeEffect editEffect) {
        cardPane.getStyles().put("selectionChangeEffect", editEffect);
    }

    /**
     * Gets the effect duration that this editor uses when changing from a
     * read-only row to an editable row.
     *
     * @return
     * The effect duration in milliseconds.
     */
    public int getEditEffectDuration() {
        return (Integer)cardPane.getStyles().get("selectionChangeDuration");
    }

    /**
     * Sets the effect duration that this editor uses when changing from a
     * read-only row to an editable row.
     *
     * @param effectDuration
     * The effect duration in milliseconds.
     */
    public void setEditEffectDuration(int effectDuration) {
        cardPane.getStyles().put("selectionChangeDuration", effectDuration);
    }

    /**
     * Gets the effect rate that this editor uses when changing from a
     * read-only row to an editable row.
     *
     * @return
     * The effect rate.
     */
    public int getEditEffectRate() {
        return (Integer)cardPane.getStyles().get("selectionChangeRate");
    }

    /**
     * Sets the effect duration that this editor uses when changing from a
     * read-only row to an editable row.
     *
     * @param effectRate
     * The effect rate.
     */
    public void setEditEffectRate(int effectRate) {
        cardPane.getStyles().put("selectionChangeRate", effectRate);
    }

    @Override
    public void open(Display display, Window owner) {
        if (owner == null) {
            throw new IllegalArgumentException();
        }

        super.open(display, owner);
        display.getContainerMouseListeners().add(displayMouseHandler);

        // Get the data being edited
        List<?> tableData = tableView.getTableData();
        Object tableRow = tableData.get(rowIndex);

        // Create the editor components
        TableView.ColumnSequence tableViewColumns = tableView.getColumns();
        TablePane.ColumnSequence tablePaneColumns = tablePane.getColumns();

        for (int i = 0, n = tableViewColumns.getLength(); i < n; i++) {
            // Add a new column to the table pane to match the table view column
            TablePane.Column tablePaneColumn = new TablePane.Column();
            tablePaneColumn.setWidth(tableView.getColumnBounds(i).width);
            tablePaneColumns.add(tablePaneColumn);

            // Determine which component to use as the editor for this column
            String columnName = tableViewColumns.get(i).getName();
            Component editorComponent = null;
            if (columnName != null) {
                editorComponent = cellEditors.get(columnName);
            }

            // Default to a text input editor
            if (editorComponent == null) {
                TextInput editorTextInput = new TextInput();
                editorTextInput.setTextKey(columnName);
                editorComponent = editorTextInput;
            }

            // Add the editor component to the table pane
            editorRow.add(editorComponent);
        }

        // Load the row data into the editor components
        tablePane.load(tableRow);

        // Get the row bounds
        Bounds rowBounds = tableView.getRowBounds(rowIndex);
        rowImage.bounds = rowBounds;

        // Scroll to make the row as visible as possible
        tableView.scrollAreaToVisible(rowBounds.x, rowBounds.y, rowBounds.width, rowBounds.height);

        // Constrain the bounds by what is visible through viewport ancestors
        rowBounds = tableView.getVisibleArea(rowBounds);
        Point location = tableView.mapPointToAncestor(tableView.getDisplay(), rowBounds.x, rowBounds.y);

        // Set size and location and match scroll left
        setPreferredWidth(rowBounds.width);
        setLocation(location.x, location.y + (rowBounds.height - getPreferredHeight(-1)) / 2);

        if (tableViewScrollPane != null) {
            scrollPane.setScrollLeft(tableViewScrollPane.getScrollLeft());
        }

        // Start the transition
        cardPane.setSelectedIndex(EDITOR_CARD_INDEX);
    }

    @Override
    public final void close() {
        close(false);
    }

    @SuppressWarnings("unchecked")
    public void close(boolean result) {
        boolean valid = true;

        if (result) {
            // Preview the changes
            HashMap previewTableRow = new HashMap<String, Object>();
            tablePane.store(previewTableRow);

            valid = validate(previewTableRow, rowIndex);

            if (valid) {
                // Update the row data
                List<Object> tableData = (List<Object>)tableView.getTableData();
                Object tableRow = tableData.get(rowIndex);
                tablePane.store(tableRow);

                if (tableData.getComparator() == null) {
                    tableData.update(rowIndex, tableRow);
                } else {
                    tableData.remove(rowIndex, 1);
                    tableData.add(tableRow);

                    // Re-select the item, and make sure it's visible
                    rowIndex = tableData.indexOf(tableRow);
                    tableView.setSelectedIndex(rowIndex);
                    tableView.scrollAreaToVisible(tableView.getRowBounds(rowIndex));
                }
            }
        }

        if (valid) {
            if (cardPane.getSelectedIndex() == EDITOR_CARD_INDEX) {
                cardPane.setSelectedIndex(IMAGE_CARD_INDEX);
            } else {
                getOwner().moveToFront();
                tableView.requestFocus();

                Display display = getDisplay();
                display.getContainerMouseListeners().remove(displayMouseHandler);

                super.close();

                // Clear the editor components
                TablePane.ColumnSequence tablePaneColumns = tablePane.getColumns();
                tablePaneColumns.remove(0, tablePaneColumns.getLength());
                editorRow.remove(0, editorRow.getLength());

                tableView = null;
                rowIndex = -1;
                columnIndex = -1;

                tableViewScrollPane = null;
            }
        }
    }

    protected boolean validate(Object tableRow, int rowIndex) {
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (keyCode == Keyboard.KeyCode.ENTER) {
            close(true);
            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.ESCAPE) {
            close(false);
            consumed = true;
        }

        return consumed;
    }
}
