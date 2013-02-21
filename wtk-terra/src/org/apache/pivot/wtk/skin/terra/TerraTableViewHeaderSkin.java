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
package org.apache.pivot.wtk.skin.terra;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.SortDirection;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewColumnListener;
import org.apache.pivot.wtk.TableViewHeader;
import org.apache.pivot.wtk.TableViewHeaderListener;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.TableViewHeader.SortMode;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.ComponentSkin;

/**
 * Table view header skin.
 */
public class TerraTableViewHeaderSkin extends ComponentSkin
    implements TableViewHeader.Skin, TableViewHeaderListener, TableViewColumnListener,
        TableViewSortListener {
    private class SortIndicatorImage extends Image {
        private SortDirection sortDirection = null;

        public SortIndicatorImage(SortDirection sortDirection) {
            this.sortDirection = sortDirection;
        }

        @Override
        public int getWidth() {
            return 7;
        }

        @Override
        public int getHeight() {
            return 4;
        }

        @Override
        public void paint(Graphics2D graphics) {
            GeneralPath shape = new GeneralPath();

            switch (sortDirection) {
                case ASCENDING: {
                    shape.moveTo(0, 3);
                    shape.lineTo(3, 0);
                    shape.lineTo(6, 3);
                    break;
                }

                case DESCENDING: {
                    shape.moveTo(0, 0);
                    shape.lineTo(3, 3);
                    shape.lineTo(6, 0);
                    break;
                }

                default: {
                    break;
                }
            }

            shape.closePath();

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.setStroke(new BasicStroke(0));
            graphics.setPaint(borderColor);

            graphics.draw(shape);
            graphics.fill(shape);
        }
    }

    private Font font;
    private Color color;
    private Color disabledColor;
    private Color backgroundColor;
    private Color disabledBackgroundColor;
    private Color borderColor;
    private Color disabledBorderColor;
    private boolean headersPressable;
    private boolean columnsResizable;
    private boolean includeTrailingVerticalGridLine;

    private Color bevelColor;
    private Color pressedBevelColor;
    private Color disabledBevelColor;

    private ArrayList<Integer> headerWidths = null;

    private int pressedHeaderIndex = -1;
    private int resizeHeaderIndex = -1;

    private static final int SORT_INDICATOR_PADDING = 2;
    private static final int RESIZE_HANDLE_SIZE = 6;
    public static final int MINIMUM_COLUMN_WIDTH = 2;

    private SortIndicatorImage sortAscendingImage = new SortIndicatorImage(SortDirection.ASCENDING);
    private SortIndicatorImage sortDescendingImage = new SortIndicatorImage(SortDirection.DESCENDING);

    public TerraTableViewHeaderSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();

        font = theme.getFont();
        color = theme.getColor(1);
        disabledColor = theme.getColor(7);
        backgroundColor = theme.getColor(10);
        disabledBackgroundColor = theme.getColor(10);
        borderColor = theme.getColor(7);
        disabledBorderColor = theme.getColor(7);
        headersPressable = true;
        columnsResizable = true;
        includeTrailingVerticalGridLine = false;

        // Set the derived colors
        bevelColor = TerraTheme.brighten(backgroundColor);
        pressedBevelColor = TerraTheme.darken(backgroundColor);
        disabledBevelColor = disabledBackgroundColor;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        TableViewHeader tableViewHeader = (TableViewHeader)component;
        tableViewHeader.getTableViewHeaderListeners().add(this);

        TableView tableView = tableViewHeader.getTableView();
        if (tableView != null) {
            tableView.getTableViewColumnListeners().add(this);
            tableView.getTableViewSortListeners().add(this);
        }
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
        TableView tableView = tableViewHeader.getTableView();

        if (tableView != null) {
            preferredWidth = TerraTableViewSkin.getPreferredWidth(tableView,
                includeTrailingVerticalGridLine);
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
        TableView tableView = tableViewHeader.getTableView();

        if (tableView != null) {
            TableView.ColumnSequence columns = tableView.getColumns();

            for (int i = 0, n = columns.getLength(); i < n; i++) {
                TableView.Column column = columns.get(i);
                TableView.HeaderDataRenderer headerDataRenderer = column.getHeaderDataRenderer();
                headerDataRenderer.render(column.getHeaderData(), i, tableViewHeader, column.getName(), false);
                preferredHeight = Math.max(preferredHeight, headerDataRenderer.getPreferredHeight(-1));
            }

            // Include the bottom border
            preferredHeight++;
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    @Override
    public int getBaseline(int width, int height) {
        int baseline = -1;

        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
        TableView tableView = tableViewHeader.getTableView();

        if (tableView != null) {
            ArrayList<Integer> headerWidthsLocal = TerraTableViewSkin.getColumnWidths(tableView, width);
            int rowHeight = getPreferredHeight(width) - 1;

            TableView.ColumnSequence columns = tableView.getColumns();

            for (int i = 0, n = columns.getLength(); i < n; i++) {
                TableView.Column column = columns.get(i);
                TableView.HeaderDataRenderer headerDataRenderer = column.getHeaderDataRenderer();
                headerDataRenderer.render(column.getHeaderData(), i, tableViewHeader, column.getName(), false);
                baseline = Math.max(baseline, headerDataRenderer.getBaseline(headerWidthsLocal.get(i), rowHeight));
            }
        }

        return baseline;
    }

    @Override
    public void layout() {
        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
        TableView tableView = tableViewHeader.getTableView();

        if (tableView != null) {
            headerWidths = TerraTableViewSkin.getColumnWidths(tableView, getWidth());
        } else {
            headerWidths = null;
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();

        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();

        Color backgroundColorLocal = null;
        Color bevelColorLocal = null;
        Color borderColorLocal = null;

        if (tableViewHeader.isEnabled()) {
            backgroundColorLocal = this.backgroundColor;
            bevelColorLocal = this.bevelColor;
            borderColorLocal = this.borderColor;
        } else {
            backgroundColorLocal = disabledBackgroundColor;
            bevelColorLocal = disabledBevelColor;
            borderColorLocal = disabledBorderColor;
        }

        // Paint the background
        graphics.setPaint(new GradientPaint(width / 2f, 0, bevelColorLocal,
            width / 2f, height, backgroundColorLocal));
        graphics.fillRect(0, 0, width, height);

        // Paint the border
        graphics.setPaint(borderColorLocal);
        graphics.setStroke(new BasicStroke(1));
        graphics.draw(new Line2D.Double(0.5, height - 0.5, width - 0.5, height - 0.5));

        // Paint the content
        TableView tableView = tableViewHeader.getTableView();

        if (tableView != null) {
            TableView.ColumnSequence columns = tableView.getColumns();

            int headerX = 0;
            for (int columnIndex = 0, columnCount = columns.getLength();
                columnIndex < columnCount; columnIndex++) {
                TableView.Column column = columns.get(columnIndex);
                int headerWidth = headerWidths.get(columnIndex);

                // Paint the pressed bevel
                if (columnIndex == pressedHeaderIndex) {
                    graphics.setPaint(new GradientPaint(width / 2f, 0, pressedBevelColor,
                        width / 2f, height, backgroundColorLocal));
                    graphics.fillRect(headerX, 0, headerWidth, height - 1);
                }

                // Paint the header data
                Object headerData = column.getHeaderData();
                TableView.HeaderDataRenderer headerDataRenderer = column.getHeaderDataRenderer();
                headerDataRenderer.render(headerData, columnIndex, tableViewHeader, column.getName(), false);
                headerDataRenderer.setSize(headerWidth, height - 1);

                Graphics2D rendererGraphics = (Graphics2D)graphics.create(headerX, 0,
                    headerWidth, height - 1);
                headerDataRenderer.paint(rendererGraphics);
                rendererGraphics.dispose();

                // Draw the sort image
                Image sortImage = null;
                String columnName = column.getName();
                SortDirection sortDirection = tableView.getSort().get(columnName);

                if (sortDirection != null) {
                    switch (sortDirection) {
                        case ASCENDING: {
                            sortImage = sortAscendingImage;
                            break;
                        }

                        case DESCENDING: {
                            sortImage = sortDescendingImage;
                            break;
                        }

                        default: {
                            break;
                        }
                    }
                }

                if (sortImage != null) {
                    int sortImageMargin = sortImage.getWidth() + SORT_INDICATOR_PADDING * 2;

                    if (headerWidth >= headerDataRenderer.getPreferredWidth(-1) + sortImageMargin) {
                        Graphics2D sortImageGraphics = (Graphics2D)graphics.create();
                        sortImageGraphics.translate(headerX + headerWidth - sortImageMargin,
                            (height - sortImage.getHeight()) / 2);
                        sortImage.paint(sortImageGraphics);
                        sortImageGraphics.dispose();
                    }
                }

                // Draw the divider
                headerX += headerWidth;

                if (columnIndex < columnCount - 1
                    || includeTrailingVerticalGridLine) {
                    graphics.setPaint(borderColorLocal);
                    graphics.draw(new Line2D.Double(headerX + 0.5, 0.5, headerX + 0.5, height - 0.5));
                }

                headerX++;
            }
        }
    }

    @Override
    public int getHeaderAt(int x) {
        if (x < 0) {
            throw new IllegalArgumentException("x is negative");
        }

        int headerIndex = -1;

        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
        TableView tableView = tableViewHeader.getTableView();

        if (tableView != null) {
            int i = 0;
            int n = tableView.getColumns().getLength();
            int headerX = 0;
            while (i < n
                && x > headerX) {
                headerX += (headerWidths.get(i) + 1);
                i++;
            }

            if (x <= headerX) {
                headerIndex = i - 1;
            }
        }

        return headerIndex;
    }

    @Override
    public Bounds getHeaderBounds(int headerIndex) {
        Bounds headerBounds = null;

        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
        TableView tableView = tableViewHeader.getTableView();

        if (tableView != null) {
            if (headerIndex < 0
                || headerIndex >= headerWidths.getLength()) {
                throw new IndexOutOfBoundsException();
            }

            int cellX = 0;
            for (int i = 0; i < headerIndex; i++) {
                cellX += (headerWidths.get(i) + 1);
            }

            headerBounds = new Bounds(cellX, 0, headerWidths.get(headerIndex), getHeight() - 1);
        }

        return headerBounds;
    }

    @Override
    public boolean isFocusable() {
        return false;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        this.font = font;
        invalidateComponent();
    }

    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(decodeFont(font));
    }

    public final void setFont(Dictionary<String, ?> font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Theme.deriveFont(font));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.color = color;
        repaintComponent();
    }

    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(GraphicsUtilities.decodeColor(color));
    }

    public final void setColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setColor(theme.getColor(color));
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(Color disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        this.disabledColor = disabledColor;
        repaintComponent();
    }

    public final void setDisabledColor(String disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        setDisabledColor(GraphicsUtilities.decodeColor(disabledColor));
    }

    public final void setDisabledColor(int disabledColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setDisabledColor(theme.getColor(disabledColor));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        this.backgroundColor = backgroundColor;
        bevelColor = TerraTheme.brighten(backgroundColor);
        pressedBevelColor = TerraTheme.darken(backgroundColor);
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
    }

    public final void setBackgroundColor(int backgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
    }

    public Color getDisabledBackgroundColor() {
        return disabledBackgroundColor;
    }

    public void setDisabledBackgroundColor(Color disabledBackgroundColor) {
        if (disabledBackgroundColor == null) {
            throw new IllegalArgumentException("disabledBackgroundColor is null.");
        }

        this.disabledBackgroundColor = disabledBackgroundColor;
        disabledBevelColor = disabledBackgroundColor;
        repaintComponent();
    }

    public final void setDisabledBackgroundColor(String disabledBackgroundColor) {
        if (disabledBackgroundColor == null) {
            throw new IllegalArgumentException("disabledBackgroundColor is null.");
        }

        setDisabledBackgroundColor(GraphicsUtilities.decodeColor(disabledBackgroundColor));
    }

    public final void setDisabledBackgroundColor(int disabledBackgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setDisabledBackgroundColor(theme.getColor(disabledBackgroundColor));
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        this.borderColor = borderColor;
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        setBorderColor(GraphicsUtilities.decodeColor(borderColor));
    }

    public final void setBorderColor(int borderColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBorderColor(theme.getColor(borderColor));
    }

    public Color getDisabledBorderColor() {
        return disabledBorderColor;
    }

    public void setDisabledBorderColor(Color disabledBorderColor) {
        if (disabledBorderColor == null) {
            throw new IllegalArgumentException("disabledBorderColor is null.");
        }

        this.disabledBorderColor = disabledBorderColor;
        repaintComponent();
    }

    public final void setDisabledBorderColor(String disabledBorderColor) {
        if (disabledBorderColor == null) {
            throw new IllegalArgumentException("disabledBorderColor is null.");
        }

        setDisabledBorderColor(GraphicsUtilities.decodeColor(disabledBorderColor));
    }

    public final void setDisabledBorderColor(int disabledBorderColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setDisabledBorderColor(theme.getColor(disabledBorderColor));
    }

    public boolean getHeadersPressable() {
        return headersPressable;
    }

    public void setHeadersPressable(boolean headersPressable) {
        this.headersPressable = headersPressable;

        pressedHeaderIndex = -1;
        repaintComponent();
    }

    public boolean getColumnsResizable() {
        return columnsResizable;
    }

    public void setColumnsResizable(boolean columnsResizable) {
        this.columnsResizable = columnsResizable;
    }

    public boolean getIncludeTrailingVerticalGridLine() {
        return includeTrailingVerticalGridLine;
    }

    public void setIncludeTrailingVerticalGridLine(boolean includeTrailingVerticalGridLine) {
        this.includeTrailingVerticalGridLine = includeTrailingVerticalGridLine;
        invalidateComponent();
    }

    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        pressedHeaderIndex = -1;
        resizeHeaderIndex = -1;
        repaintComponent();
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);
        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
        TableView tableView = tableViewHeader.getTableView();

        if (tableView != null) {
            if (resizeHeaderIndex != -1
                && Mouse.getCapturer() != tableViewHeader) {
                Mouse.capture(tableViewHeader);
            }

            if (Mouse.getCapturer() == tableViewHeader) {
                TableView.Column column = tableView.getColumns().get(resizeHeaderIndex);
                Bounds headerBounds = getHeaderBounds(resizeHeaderIndex);
                int columnWidth = Math.max(x - headerBounds.x, MINIMUM_COLUMN_WIDTH);
                column.setWidth(columnWidth, false);
            } else {
                int headerIndex = getHeaderAt(x);

                if (headerIndex != -1
                    && columnsResizable) {
                    Bounds headerBounds = getHeaderBounds(headerIndex);
                    TableView.Column column = tableView.getColumns().get(headerIndex);

                    if (!column.isRelative()
                        && column.getWidth() != -1
                        && x > headerBounds.x + headerBounds.width - RESIZE_HANDLE_SIZE) {
                        tableViewHeader.setCursor(Cursor.RESIZE_EAST);
                    } else {
                        tableViewHeader.setCursor((Cursor)null);
                    }
                } else {
                    tableViewHeader.setCursor((Cursor)null);
                }
            }
        }

        return consumed;
    }

    @Override
    public void mouseOut(Component component) {
        super.mouseOut(component);

        if (pressedHeaderIndex != -1) {
            repaintComponent(getHeaderBounds(pressedHeaderIndex));
        }

        pressedHeaderIndex = -1;
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        if (button == Mouse.Button.LEFT) {
            TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
            TableView tableView = tableViewHeader.getTableView();

            if (tableView != null) {
                int headerIndex = getHeaderAt(x);

                if (headerIndex != -1) {
                    Bounds headerBounds = getHeaderBounds(headerIndex);
                    TableView.Column column = tableView.getColumns().get(headerIndex);

                    if (columnsResizable
                        && !column.isRelative()
                        && column.getWidth() != -1
                        && x > headerBounds.x + headerBounds.width - RESIZE_HANDLE_SIZE) {
                        resizeHeaderIndex = headerIndex;
                    } else if (headersPressable) {
                        pressedHeaderIndex = headerIndex;
                        repaintComponent(headerBounds);
                    }
                }
            }
        }

        return consumed;
    }

    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        if (button == Mouse.Button.LEFT) {
            if (resizeHeaderIndex != -1) {
                if (Mouse.getCapturer() == component) {
                    Mouse.release();
                    resizeHeaderIndex = -1;
                }
            } else if (pressedHeaderIndex != -1) {
                repaintComponent(getHeaderBounds(pressedHeaderIndex));
            }
        }

        return consumed;
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        boolean consumed = super.mouseClick(component, button, x, y, count);

        if (button == Mouse.Button.LEFT) {
            TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
            TableView tableView = tableViewHeader.getTableView();

            if (resizeHeaderIndex != -1) {
                TableView.Column column = tableView.getColumns().get(resizeHeaderIndex);

                if (count == 2
                    && !column.isRelative()
                    && column.getWidth() != -1) {
                    // Size the column to fit its contents
                    int columnWidth = 0;

                    TableView.CellRenderer cellRenderer = column.getCellRenderer();
                    List<?> tableData = tableView.getTableData();

                    int rowIndex = 0;
                    for (Object rowData : tableData) {
                        cellRenderer.render(rowData, rowIndex++, resizeHeaderIndex, tableView,
                            column.getName(), false, false, false);
                        columnWidth = Math.max(cellRenderer.getPreferredWidth(-1), columnWidth);
                    }

                    column.setWidth(columnWidth);
                }
            } else if (pressedHeaderIndex != -1) {
                // Press the header
                tableViewHeader.pressHeader(pressedHeaderIndex);

                // Update the sort
                TableViewHeader.SortMode sortMode = tableViewHeader.getSortMode();

                if (sortMode != TableViewHeader.SortMode.NONE) {
                    TableView.Column column = tableView.getColumns().get(pressedHeaderIndex);
                    String columnName = column.getName();

                    SortDirection sortDirection = tableView.getSort().get(columnName);
                    if (sortDirection == null) {
                        sortDirection = SortDirection.ASCENDING;
                    } else if (sortDirection == SortDirection.ASCENDING) {
                        sortDirection = SortDirection.DESCENDING;
                    } else {
                        sortDirection = SortDirection.ASCENDING;
                    }

                    if (sortMode == TableViewHeader.SortMode.MULTI_COLUMN
                        && Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                        tableView.getSort().put(columnName, sortDirection);
                    } else {
                        tableView.setSort(columnName, sortDirection);
                    }

                    consumed = true;
                }
            }

            resizeHeaderIndex = -1;
            pressedHeaderIndex = -1;
        }

        return consumed;
    }

    // Table view header events
    @Override
    public void tableViewChanged(TableViewHeader tableViewHeader,
        TableView previousTableView) {
        if (previousTableView != null) {
            previousTableView.getTableViewColumnListeners().remove(this);
            previousTableView.getTableViewSortListeners().remove(this);
        }

        TableView tableView = tableViewHeader.getTableView();
        if (tableView != null) {
            tableView.getTableViewColumnListeners().add(this);
            tableView.getTableViewSortListeners().add(this);
        }

        invalidateComponent();
    }

    @Override
    public void sortModeChanged(TableViewHeader tableViewHeader, SortMode previousSortMode) {
        // No-op
    }

    // Table view column events
    @Override
    public void columnInserted(TableView tableView, int index) {
        invalidateComponent();
    }

    @Override
    public void columnsRemoved(TableView tableView, int index, Sequence<TableView.Column> columns) {
        invalidateComponent();
    }

    @Override
    public void columnNameChanged(TableView.Column column, String previousName) {
        // No-op
    }

    @Override
    public void columnHeaderDataChanged(TableView.Column column, Object previousHeaderData) {
        invalidateComponent();
    }

    @Override
    public void columnHeaderDataRendererChanged(TableView.Column column,
        TableView.HeaderDataRenderer previousHeaderDataRenderer) {
        invalidateComponent();
    }

    @Override
    public void columnWidthChanged(TableView.Column column, int previousWidth, boolean previousRelative) {
        invalidateComponent();
    }

    @Override
    public void columnWidthLimitsChanged(TableView.Column column, int  previousMinimumWidth, int previousMaximumWidth) {
        invalidateComponent();
    }

    @Override
    public void columnFilterChanged(TableView.Column column, Object previousFilter) {
        // No-op
    }

    @Override
    public void columnCellRendererChanged(TableView.Column column, TableView.CellRenderer previousCellRenderer) {
        // No-op
    }

    // Table view sort events
    @Override
    public void sortAdded(TableView tableView, String columnName) {
        repaintComponent();
    }

    @Override
    public void sortUpdated(TableView tableView, String columnName,
        SortDirection previousSortDirection) {
        repaintComponent();
    }

    @Override
    public void sortRemoved(TableView tableView, String columnName,
        SortDirection sortDirection) {
        repaintComponent();
    }

    @Override
    public void sortChanged(TableView tableView) {
        repaintComponent();
    }

}
