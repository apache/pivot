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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Line2D;

import pivot.collections.ArrayList;
import pivot.collections.Map;
import pivot.collections.Sequence;
import pivot.wtk.Component;
import pivot.wtk.Container;
import pivot.wtk.Dimensions;
import pivot.wtk.Insets;
import pivot.wtk.Keyboard;
import pivot.wtk.Point;
import pivot.wtk.Rectangle;
import pivot.wtk.TablePane;
import pivot.wtk.TablePaneListener;

/**
 * TODO Respect TablePane.MARGIN_ATTRIBUTE
 *
 * @author tvolkert
 */
public class TablePaneSkin extends ContainerSkin implements TablePane.Skin,
    TablePaneListener {
    // Style properties
    protected Insets padding = DEFAULT_PADDING;
    protected int spacing = DEFAULT_SPACING;
    protected boolean showHorizontalGridLines = DEFAULT_SHOW_HORIZONTAL_GRID_LINES;
    protected boolean showVerticalGridLines = DEFAULT_SHOW_VERTICAL_GRID_LINES;
    protected Color gridColor = DEFAULT_GRID_COLOR;
    protected Color selectionBackgroundColor = DEFAULT_SELECTION_BACKGROUND_COLOR;

    // Default style values
    private static final Insets DEFAULT_PADDING = new Insets(0);
    private static final int DEFAULT_SPACING = 0;
    private static final boolean DEFAULT_SHOW_HORIZONTAL_GRID_LINES = false;
    private static final boolean DEFAULT_SHOW_VERTICAL_GRID_LINES = false;
    private static final Color DEFAULT_GRID_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color DEFAULT_SELECTION_BACKGROUND_COLOR = new Color(0xCC, 0xCA, 0xC2);

    // Style keys
    protected static final String PADDING_KEY = "padding";
    protected static final String SPACING_KEY = "spacing";
    protected static final String SHOW_HORIZONTAL_GRID_LINES_KEY = "showHorizontalGridLines";
    protected static final String SHOW_VERTICAL_GRID_LINES_KEY = "showVerticalGridLines";
    protected static final String GRID_COLOR_KEY = "gridColor";
    protected static final String SELECTION_BACKGROUND_COLOR_KEY = "selectionBackgroundColor";

    @Override
    public void install(Component component) {
        validateComponentType(component, TablePane.class);

        super.install(component);

        TablePane tablePane = (TablePane)component;
        tablePane.getTablePaneListeners().add(this);
    }

    @Override
    public void uninstall() {
        TablePane tablePane = (TablePane)getComponent();
        tablePane.getTablePaneListeners().remove(this);

        super.uninstall();
    }

    @Override
    public int getPreferredWidth(int height) {
        TablePane tablePane = (TablePane)getComponent();
        int numCols = tablePane.getColumns().getLength();

        int preferredWidth = padding.left + padding.right + ((numCols - 1) * spacing);

        int totalRelativeWeight = 0;
        ArrayList<Integer> defaultWidthColumns = new ArrayList<Integer>();

        for (int j = 0; j < numCols; j++) {
            TablePane.Column column = tablePane.getColumns().get(j);
            int columnWidth = column.getWidth();

            if (column.isRelative()) {
                // A relative width column gets at least as much as its
                // default but will grow to accomodate its relative constraint.
                defaultWidthColumns.add(j);

                totalRelativeWeight += columnWidth;
            } else {
                if (columnWidth < 0) {
                    // We'll handle default-width columns on our next pass.
                    defaultWidthColumns.add(j);
                } else {
                    preferredWidth += columnWidth;
                }
            }
        }

        if (defaultWidthColumns.getLength() > 0) {
            // We have at least one default-width column. Such columns take on
            // the width of their widest cell. To find that out, we have to ask
            // the preferred width of each component in that column, given the
            // appropriate height constraint. The height constraint is the row
            // height, which in turn means that we need to calculate the row
            // heights first.
            int totalRelativeWidth = 0;
            int[] rowHeights = getRowHeights(height, null);
            int numRows = tablePane.getRows().getLength();

            for (int k = 0, n = defaultWidthColumns.getLength(); k < n; k++) {
                int j = defaultWidthColumns.get(k);
                TablePane.Column column = tablePane.getColumns().get(j);
                int columnWidth = 0;

                for (int i = 0; i < numRows; i++) {
                    int rowHeight = rowHeights[i];
                    Component child = tablePane.getCellComponent(i, j);

                    if (child != null
                        && child.isDisplayable()) {
                       int cellWidth = child.getPreferredWidth(rowHeight);
                       columnWidth = Math.max(columnWidth, cellWidth);
                    }
                }

                if (column.isRelative()) {
                    // Figure out how much total relative width we need to
                    // accomodate both (1) the widths relative to one another
                    // and (2) ensuring that each relative width column is at
                    // least as wide as its default width.
                    double weightPercentage = (double)column.getWidth()
                        / (double)totalRelativeWeight;
                    totalRelativeWidth = Math.max(totalRelativeWidth,
                        (int)((double)columnWidth / weightPercentage));
                } else {
                    preferredWidth += columnWidth;
                }
            }

            preferredWidth += totalRelativeWidth;
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        TablePane tablePane = (TablePane)getComponent();
        int numRows = tablePane.getRows().getLength();

        int preferredHeight = padding.top + padding.bottom + ((numRows - 1) * spacing);

        int totalRelativeWeight = 0;
        ArrayList<Integer> defaultHeightRows = new ArrayList<Integer>();

        for (int i = 0; i < numRows; i++) {
            TablePane.Row row = tablePane.getRows().get(i);
            int rowHeight = row.getHeight();

            if (row.isRelative()) {
                // A relative height row gets at least as much as its default
                // but will grow to accomodate its relative constraint.
                defaultHeightRows.add(i);

                totalRelativeWeight += rowHeight;
            } else {
                if (rowHeight < 0) {
                    // We'll handle default-height rows on our next pass.
                    defaultHeightRows.add(i);
                } else {
                    preferredHeight += rowHeight;
                }
            }
        }

        if (defaultHeightRows.getLength() > 0) {
            // We have at least one default-height row. Such rows take on
            // the height of their tallest cell. To find that out, we have to
            // ask the preferred height of each component in that row, given the
            // appropriate width constraint. The width constraint is the column
            // width, which in turn means that we need to calculate the column
            // widths first.
            int totalRelativeHeight = 0;
            int[] colWidths = getColumnWidths(tablePane, width, null);
            int numCols = tablePane.getColumns().getLength();

            for (int k = 0, n = defaultHeightRows.getLength(); k < n; k++) {
                int i = defaultHeightRows.get(k);
                TablePane.Row row = tablePane.getRows().get(i);
                int rowHeight = 0;

                for (int j = 0; j < numCols; j++) {
                    int colWidth = colWidths[j];
                    Component child = tablePane.getCellComponent(i, j);

                    if (child != null
                        && child.isDisplayable()) {
                       int cellHeight = child.getPreferredHeight(colWidth);
                       rowHeight = Math.max(rowHeight, cellHeight);
                    }
                }

                if (row.isRelative()) {
                    // Figure out how much total relative height we need to
                    // accomodate both (1) the heights relative to one another
                    // and (2) ensuring that each relative height row is at
                    // least as tall as its default height.
                    double weightPercentage = (double)row.getHeight()
                        / (double)totalRelativeWeight;
                    totalRelativeHeight = Math.max(totalRelativeHeight,
                        (int)((double)rowHeight / weightPercentage));
                } else {
                    preferredHeight += rowHeight;
                }
            }

            preferredHeight += totalRelativeHeight;
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        // TODO: Make sure that relative width columns and relative height rows
        // get at least as much as their default, like the getPreferredWidth()
        // and getPreferredHeight() calculations do.

        TablePane tablePane = (TablePane)getComponent();
        int numRows = tablePane.getRows().getLength();
        int numCols = tablePane.getColumns().getLength();

        int preferredWidth = padding.left + padding.right + ((numCols - 1) * spacing);
        int preferredHeight = padding.top + padding.bottom + ((numRows - 1) * spacing);

        int[] columnWidths = getColumnWidths(tablePane, -1, null);
        int[] rowHeights = getRowHeights(-1, columnWidths);

        for (int i = 0; i < numRows; i++) {
            int rowHeight = rowHeights[i];
            if (rowHeight > 0) {
                preferredHeight += rowHeight;
            }
        }

        for (int j = 0; j < numCols; j++) {
            int columnWidth = columnWidths[j];
            if (columnWidth > 0) {
                preferredWidth += columnWidth;
            }
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    public void layout() {
        int width = getWidth();
        int height = getHeight();

        TablePane tablePane = (TablePane)getComponent();
        int numRows = tablePane.getRows().getLength();
        int numCols = tablePane.getColumns().getLength();

        int[] columnWidths = getColumnWidths(tablePane, width, null);
        int[] rowHeights = getRowHeights(height, columnWidths);

        int componentY = padding.top;

        for (int i = 0; i < numRows; i++) {

            int componentX = padding.left;
            for (int j = 0; j < numCols; j++) {
                Component child = tablePane.getCellComponent(i, j);

                if (child != null) {
                    if (child.isDisplayable()) {
                        child.setLocation(componentX, componentY);

                        Component.AttributeDictionary childAttributes = child.getAttributes();

                        int colSpan = 1;
                        if (childAttributes.containsKey(TablePane.COLUMN_SPAN_ATTRIBUTE)) {
                            colSpan = Math.max(
                                (Integer)childAttributes.get(TablePane.COLUMN_SPAN_ATTRIBUTE),
                                1);
                        }
                        int childWidth = (colSpan - 1) * spacing;
                        for (int k = 0; k < colSpan; k++) {
                            childWidth += columnWidths[j + k];
                        }

                        int rowSpan = 1;
                        if (childAttributes.containsKey(TablePane.ROW_SPAN_ATTRIBUTE)) {
                            rowSpan = Math.max(
                                (Integer)childAttributes.get(TablePane.ROW_SPAN_ATTRIBUTE),
                                1);
                        }
                        int childHeight = (rowSpan - 1) * spacing;
                        for (int k = 0; k < rowSpan; k++) {
                            childHeight += rowHeights[i + k];
                        }

                        // Set the component's size
                        child.setSize(childWidth, childHeight);

                        // Show the component
                        child.setVisible(true);
                    } else {
                        // Hide the component
                        child.setVisible(false);
                    }
                }

                componentX += (columnWidths[j] + spacing);
            }

            componentY += (rowHeights[i] + spacing);
        }
    }

    private int[] getRowHeights(int height, int[] columnWidths) {
        TablePane tablepane = (TablePane)getComponent();
        int numRows = tablepane.getRows().getLength();
        int numCols = tablepane.getColumns().getLength();

        int heights[] = new int[numRows];

        int totalAbsoluteHeight = padding.top + padding.bottom + (numRows - 1)
            * spacing;
        int totalRelativeHeight = 0;

        for (int i = 0; i < numRows; i++) {
            TablePane.Row row = tablepane.getRows().get(i);
            int rowHeight = row.getHeight();

            if (row.isRelative()) {
                heights[i] = rowHeight * -1;
                totalRelativeHeight += rowHeight;
            } else {
                if (rowHeight < 0) {
                    // Default height row; we must calculate the height.
                    rowHeight = 0;
                    for (int j = 0; j < numCols; j++) {
                        Component child = tablepane.getCellComponent(i, j);

                        if (child != null
                            && child.isDisplayable()) {
                            int widthConstraint;

                            if (columnWidths != null) {
                                widthConstraint = columnWidths[j];
                                if (widthConstraint < 0) {
                                    // We skip this column.
                                    continue;
                                }
                            } else {
                                TablePane.Column column = tablepane.getColumns().get(j);
                                if (column.isRelative()) {
                                    // We must skip this column.
                                    continue;
                                }
                                widthConstraint = column.getWidth();
                            }

                            int childHeight = child.getPreferredHeight(widthConstraint);
                            if (childHeight > rowHeight) {
                                rowHeight = childHeight;
                            }
                        }
                    }
                }

                heights[i] = rowHeight;
                totalAbsoluteHeight += rowHeight;
            }
        }

        if (height >= 0) {
            int remainingHeight = Math.max(height - totalAbsoluteHeight, 0);
            for (int i = 0; i < numRows; i++) {
                if (heights[i] < 0) {
                    float heightPercentage = (float)(heights[i] * -1)
                        / (float)totalRelativeHeight;
                    heights[i] = (int)((float)remainingHeight * heightPercentage);
                }
            }
        }

        return heights;
    }

    public static int[] getColumnWidths(TablePane tablePane, int width, int[] rowHeights) {
        int numRows = tablePane.getRows().getLength();
        int numCols = tablePane.getColumns().getLength();

        int widths[] = new int[numCols];

        Insets padding = (Insets)tablePane.getStyles().get("padding");
        int spacing = (Integer)tablePane.getStyles().get("spacing");

        int totalAbsoluteWidth = padding.left + padding.right + (numCols - 1) * spacing;
        int totalRelativeWidth = 0;

        for (int j = 0; j < numCols; j++) {
            TablePane.Column column = tablePane.getColumns().get(j);
            int columnWidth = column.getWidth();

            if (column.isRelative()) {
                widths[j] = columnWidth * -1;
                totalRelativeWidth += columnWidth;
            } else {
                if (columnWidth < 0) {
                    // Default width column; we must calculate the width.
                    columnWidth = 0;
                    for (int i = 0; i < numRows; i++) {
                        Component child = tablePane.getCellComponent(i, j);

                        if (child != null
                            && child.isDisplayable()) {
                            int heightConstraint;

                            if (rowHeights != null) {
                                heightConstraint = rowHeights[i];
                                if (heightConstraint < 0) {
                                    // We skip this row.
                                    continue;
                                }
                            } else {
                                TablePane.Row row = tablePane.getRows().get(i);
                                if (row.isRelative()) {
                                    // We must skip this row.
                                    continue;
                                }
                                heightConstraint = row.getHeight();
                            }

                            int childWidth = child.getPreferredWidth(heightConstraint);
                            if (childWidth > columnWidth) {
                                columnWidth = childWidth;
                            }
                        }
                    }
                }

                widths[j] = columnWidth;
                totalAbsoluteWidth += columnWidth;
            }
        }

        if (width >= 0) {
            int remainingWidth = Math.max(width - totalAbsoluteWidth, 0);
            for (int j = 0; j < numCols; j++) {
                if (widths[j] < 0) {
                    float widthPercentage = (float)(widths[j] * -1)
                        / (float)totalRelativeWidth;
                    widths[j] = (int)((float)remainingWidth * widthPercentage);
                }
            }
        }

        return widths;
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        TablePane tablePane = (TablePane)getComponent();

        int width = getWidth();
        int height = getHeight();

        int[] columnWidths = getColumnWidths(tablePane, width, null);
        int[] rowHeights = getRowHeights(height, columnWidths);

        int numRows = tablePane.getRows().getLength();
        int numCols = tablePane.getColumns().getLength();

        graphics.setPaint(selectionBackgroundColor);

        // Paint the selected rows
        // TODO optimize the only paint rows that intersect with
        // the current clip rect
        for (int i = 0, rowY = padding.top; i < numRows; i++) {
            TablePane.Row row = tablePane.getRows().get(i);

            if (row.isSelected()) {
                Rectangle bounds = new Rectangle(0, rowY, width, rowHeights[i]);
                graphics.fill(bounds);
            }

            rowY += rowHeights[i] + spacing;
        }

        // Paint the selected columns
        // TODO optimize the only paint columns that intersect with
        // the current clip rect
        for (int j = 0, columnX = padding.left; j < numCols; j++) {
            TablePane.Column column = tablePane.getColumns().get(j);

            if (column.isSelected()) {
                Rectangle bounds = new Rectangle(columnX, 0, columnWidths[j], height);
                graphics.fill(bounds);
            }

            columnX += columnWidths[j] + spacing;
        }

        // Paint the grid lines
        if ((showHorizontalGridLines || showVerticalGridLines)
            && spacing > 0) {
            Graphics2D gridGraphics = (Graphics2D)graphics.create();

            gridGraphics.setStroke(new BasicStroke());
            gridGraphics.setPaint(gridColor);

            // Find any components that span multiple rows or columns, and
            // ensure that the grid lines don't get painted through their
            // cells. We'll only instantiate gridClip if we find such cells
            Area gridClip = null;

            for (int i = 0, componentY = padding.top; i < numRows; i++) {
                for (int j = 0, componentX = padding.left; j < numCols; j++) {
                    Component component = tablePane.getCellComponent(i, j);

                    if (component != null) {
                        Component.AttributeDictionary attributes =
                            component.getAttributes();

                        int rowSpan = 1;
                        int colSpan = 1;

                        Integer rowSpanAttribute = (Integer)attributes.get
                            (TablePane.ROW_SPAN_ATTRIBUTE);
                        if (rowSpanAttribute != null) {
                            rowSpan = Math.max(rowSpan, rowSpanAttribute);
                        }

                        Integer colSpanAttribute = (Integer)attributes.get
                            (TablePane.COLUMN_SPAN_ATTRIBUTE);
                        if (colSpanAttribute != null) {
                            colSpan = Math.max(colSpan, colSpanAttribute);
                        }

                        if (rowSpan > 1
                            || colSpan > 1) {
                            int rowY = componentY;
                            int columnX = componentX;

                            int rowHeight = rowHeights[i];
                            int columnWidth = columnWidths[j];

                            for (int k = i + 1; k < i + rowSpan; k++) {
                                rowHeight += rowHeights[k] + spacing;
                            }

                            for (int k = j + 1; k < j + colSpan; k++) {
                                columnWidth += columnWidths[k] + spacing;
                            }

                            if (gridClip == null) {
                                gridClip = new Area(graphics.getClip());
                            }

                            if (spacing > 1) {
                                rowHeight += spacing - 1;
                                columnWidth += spacing - 1;

                                columnX -= (int)(((float)spacing * 0.5f) - 0.5f);
                                rowY -= (int)(((float)spacing * 0.5f) - 0.5f);
                            }

                            Rectangle bounds = new Rectangle(columnX, rowY,
                                columnWidth, rowHeight);
                            gridClip.subtract(new Area(bounds));
                        }
                    }

                    componentX += columnWidths[j] + spacing;
                }

                componentY += rowHeights[i] + spacing;
            }

            if (gridClip != null) {
                gridGraphics.clip(gridClip);
            }

            if (showHorizontalGridLines) {
                int rowY = padding.top + (rowHeights[0] + spacing);

                // TODO optimize the only paint grid lines that intersect with
                // the current clip rect
                for (int i = 1; i < numRows; i++) {
                    int gridY = Math.max(rowY
                        - (int)Math.ceil((float)spacing * 0.5), 0);
                    int gridWidth = Math.max(width - (padding.left + padding.right), 0);
                    Line2D horizontalGridLine = new Line2D.Double(padding.left, gridY,
                        gridWidth - 1, gridY);
                    gridGraphics.draw(horizontalGridLine);

                    rowY += (rowHeights[i] + spacing);
                }
            }

            if (showVerticalGridLines) {
                int columnX = padding.left + (columnWidths[0] + spacing);

                // TODO optimize the only paint grid lines that intersect with
                // the current clip rect
                for (int j = 1; j < numCols; j++) {
                    int gridX = Math.max(columnX
                        - (int)Math.ceil((float)spacing * 0.5), 0);
                    int gridHeight = Math.max(height
                        - (padding.top + padding.bottom), 0);
                    Line2D verticalGridLine = new Line2D.Double(gridX, padding.top, gridX,
                        gridHeight - 1);
                    gridGraphics.draw(verticalGridLine);

                    columnX += (columnWidths[j] + spacing);
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        TablePane tablePane = (TablePane)getComponent();
        Component focusedComponent = Component.getFocusedComponent();

        Point coordinates = focusedComponent.mapPointToAncestor(tablePane, 0, 0);

        if (coordinates != null) {
            int row = tablePane.getRowAt(coordinates.y);
            int column = tablePane.getColumnAt(coordinates.x);

            Component cellComponent = null;

            switch (keyCode) {
            case Keyboard.KeyCode.UP:
                if (row-- > 0) {
                    do {
                        cellComponent = tablePane.getCellComponent(row, column);
                    } while (row-- > 0
                        && (cellComponent == null
                        || !cellComponent.isFocusable()));
                }
                break;

            case Keyboard.KeyCode.DOWN:
                int rowCount = tablePane.getRows().getLength();

                if (row++ < rowCount - 1) {
                    do {
                        cellComponent = tablePane.getCellComponent(row, column);
                    } while (row++ < rowCount - 1
                        && (cellComponent == null
                        || !cellComponent.isFocusable()));
                }
                break;

            case Keyboard.KeyCode.LEFT:
                if (column-- > 0) {
                    do {
                        cellComponent = tablePane.getCellComponent(row, column);
                    } while (column-- > 0
                        && (cellComponent == null
                        || !cellComponent.isFocusable()));
                }
                break;

            case Keyboard.KeyCode.RIGHT:
                int columnCount = tablePane.getColumns().getLength();

                if (column++ < columnCount - 1) {
                    do {
                        cellComponent = tablePane.getCellComponent(row, column);
                    } while (column++ < columnCount - 1
                        && (cellComponent == null
                        || !cellComponent.isFocusable()));
                }
                break;

            }

            if (cellComponent != null
                && cellComponent.isFocusable()) {
                Component.setFocusedComponent(cellComponent);
                consumed = true;
            }
        }

        return consumed;
    }

    @Override
    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        if (key.equals(PADDING_KEY)) {
            value = padding;
        } else if (key.equals(SPACING_KEY)) {
            value = spacing;
        } else if (key.equals(SHOW_HORIZONTAL_GRID_LINES_KEY)) {
            value = showHorizontalGridLines;
        } else if (key.equals(SHOW_VERTICAL_GRID_LINES_KEY)) {
            value = showVerticalGridLines;
        } else if (key.equals(GRID_COLOR_KEY)) {
            value = gridColor;
        } else if (key.equals(SELECTION_BACKGROUND_COLOR_KEY)) {
            value = selectionBackgroundColor;
        } else {
            value = super.get(key);
        }

        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(PADDING_KEY)) {
            if (value instanceof Number) {
                value = new Insets(((Number)value).intValue());
            } else {
                if (value instanceof Map<?, ?>) {
                    value = new Insets((Map<String, Object>)value);
                }
            }

            validatePropertyType(key, value, Insets.class, false);

            previousValue = padding;
            padding = (Insets)value;

            invalidateComponent();
        } else if (key.equals(SPACING_KEY)) {
            if (value instanceof Number) {
                value = ((Number)value).intValue();
            }

            validatePropertyType(key, value, Integer.class, false);

            previousValue = spacing;
            spacing = (Integer)value;

            invalidateComponent();
        } else if (key.equals(SHOW_HORIZONTAL_GRID_LINES_KEY)) {
            if (value instanceof String) {
                value = Boolean.parseBoolean((String)value);
            }

            validatePropertyType(key, value, Boolean.class, false);

            previousValue = showHorizontalGridLines;
            showHorizontalGridLines = (Boolean)value;

            repaintComponent();
        } else if (key.equals(SHOW_VERTICAL_GRID_LINES_KEY)) {
            if (value instanceof String) {
                value = Boolean.parseBoolean((String)value);
            }

            validatePropertyType(key, value, Boolean.class, false);

            previousValue = showVerticalGridLines;
            showVerticalGridLines = (Boolean)value;

            repaintComponent();
        } else if (key.equals(GRID_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = gridColor;
            gridColor = (Color)value;

            if (showHorizontalGridLines || showVerticalGridLines) {
                repaintComponent();
            }
        } else if (key.equals(SELECTION_BACKGROUND_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = selectionBackgroundColor;
            selectionBackgroundColor = (Color)value;

            repaintComponent();
        } else {
            super.put(key, value);
        }

        return previousValue;
    }

    @Override
    public Object remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(PADDING_KEY)) {
            previousValue = put(key, DEFAULT_PADDING);
        } else if (key.equals(SPACING_KEY)) {
            previousValue = put(key, DEFAULT_SPACING);
        } else if (key.equals(SHOW_HORIZONTAL_GRID_LINES_KEY)) {
            previousValue = put(key, DEFAULT_SHOW_HORIZONTAL_GRID_LINES);
        } else if (key.equals(SHOW_VERTICAL_GRID_LINES_KEY)) {
            previousValue = put(key, DEFAULT_SHOW_VERTICAL_GRID_LINES);
        } else if (key.equals(GRID_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_GRID_COLOR);
        } else if (key.equals(SELECTION_BACKGROUND_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_SELECTION_BACKGROUND_COLOR);
        } else {
            previousValue = super.remove(key);
        }

        return previousValue;
    }

    @Override
    public boolean containsKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        return (key.equals(PADDING_KEY)
            || key.equals(SPACING_KEY)
            || key.equals(SHOW_HORIZONTAL_GRID_LINES_KEY)
            || key.equals(SHOW_VERTICAL_GRID_LINES_KEY)
            || key.equals(GRID_COLOR_KEY)
            || key.equals(SELECTION_BACKGROUND_COLOR_KEY)
            || super.containsKey(key));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    // ComponentAttributeListener methods

    @Override
    public void attributeAdded(Component component, Container.Attribute attribute) {
        if (attribute == TablePane.COLUMN_SPAN_ATTRIBUTE
            || attribute == TablePane.ROW_SPAN_ATTRIBUTE
            || attribute == TablePane.MARGIN_ATTRIBUTE) {
            invalidateComponent();
        } else {
            super.attributeAdded(component, attribute);
        }
    }

    @Override
    public void attributeUpdated(Component component,
        Container.Attribute attribute, Object previousValue) {
        if (attribute == TablePane.COLUMN_SPAN_ATTRIBUTE
            || attribute == TablePane.ROW_SPAN_ATTRIBUTE
            || attribute == TablePane.MARGIN_ATTRIBUTE) {
            invalidateComponent();
        } else {
            super.attributeUpdated(component, attribute, previousValue);
        }
    }

    @Override
    public void attributeRemoved(Component component,
        Container.Attribute attribute, Object value) {
        if (attribute == TablePane.COLUMN_SPAN_ATTRIBUTE
            || attribute == TablePane.ROW_SPAN_ATTRIBUTE
            || attribute == TablePane.MARGIN_ATTRIBUTE) {
            invalidateComponent();
        } else {
            super.attributeRemoved(component, attribute, value);
        }
    }

    // TablePane.Skin methods

    public int getRowAt(int y) {
        TablePane tablePane = (TablePane)getComponent();

        int rowIndex = -1;

        int width = getWidth();
        int height = getHeight();

        int[] columnWidths = getColumnWidths(tablePane, width, null);
        int[] rowHeights = getRowHeights(height, columnWidths);

        for (int i = 0, rowY = padding.top; rowY <= y && i < rowHeights.length; i++) {
            int rowHeight = rowHeights[i];

            if (y < rowY + rowHeight) {
                rowIndex = i;
                break;
            }

            rowY += rowHeight + spacing;
        }

        return rowIndex;
    }

    public Rectangle getRowBounds(int row) {
        TablePane tablePane = (TablePane)getComponent();

        int width = getWidth();
        int height = getHeight();

        int[] columnWidths = getColumnWidths(tablePane, width, null);
        int[] rowHeights = getRowHeights(height, columnWidths);

        int rowY = padding.top;

        for (int i = 0; i < row; i++) {
            rowY += rowHeights[i] + spacing;
        }

        return new Rectangle(0, rowY, width, rowHeights[row]);
    }

    public int getColumnAt(int x) {
        TablePane tablePane = (TablePane)getComponent();

        int columnIndex = -1;

        int width = getWidth();
        int height = getHeight();

        int[] rowHeights = getRowHeights(height, null);
        int[] columnWidths = getColumnWidths(tablePane, width, rowHeights);

        for (int j = 0, columnX = padding.left; columnX <= x && j < columnWidths.length; j++) {
            int columnWidth = columnWidths[j];

            if (x < columnX + columnWidth) {
                columnIndex = j;
                break;
            }

            columnX += columnWidth + spacing;
        }

        return columnIndex;
    }

    public Rectangle getColumnBounds(int column) {
        TablePane tablePane = (TablePane)getComponent();

        int width = getWidth();
        int height = getHeight();

        int[] rowHeights = getRowHeights(height, null);
        int[] columnWidths = getColumnWidths(tablePane, width, rowHeights);

        int columnX = padding.left;

        for (int j = 0; j < column; j++) {
            columnX += columnWidths[j] + spacing;
        }

        return new Rectangle(columnX, 0, columnWidths[column], height);
    }

    // TablePaneListener methods

    public void rowInserted(TablePane tablePane, int index) {
        invalidateComponent();
    }

    public void rowsRemoved(TablePane tablePane, int index, Sequence<TablePane.Row> rows) {
        invalidateComponent();
    }

    public void rowHeightChanged(TablePane tablePane, int index,
        int previousHeight, boolean previousRelative) {
        invalidateComponent();
    }

    public void rowSelectedChanged(TablePane tablePane, int index) {
        repaintComponent(getRowBounds(index));
    }

    public void columnInserted(TablePane tablePane, int index) {
        invalidateComponent();
    }

    public void columnsRemoved(TablePane tablePane, int index,
        Sequence<TablePane.Column> columns) {
        invalidateComponent();
    }

    public void columnHeaderDataChanged(TablePane tablePane, int index,
        Object previousHeaderData) {
        // No-op
    }

    public void columnWidthChanged(TablePane tablePane, int index,
        int previousWidth, boolean previousRelative) {
        invalidateComponent();
    }

    public void columnSelectedChanged(TablePane tablePane, int index) {
        repaintComponent(getColumnBounds(index));
    }

    public void cellComponentChanged(TablePane tablePane, int row, int column,
        Component previousComponent) {
        // No-op
    }
}
