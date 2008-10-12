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
package pivot.wtk.skin.terra;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import pivot.collections.Sequence;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Cursor;
import pivot.wtk.Dimensions;
import pivot.wtk.Display;
import pivot.wtk.Mouse;
import pivot.wtk.Point;
import pivot.wtk.Bounds;
import pivot.wtk.SortDirection;
import pivot.wtk.TableView;
import pivot.wtk.TableViewColumnListener;
import pivot.wtk.TableViewHeader;
import pivot.wtk.TableViewHeaderListener;
import pivot.wtk.Theme;
import pivot.wtk.media.Image;
import pivot.wtk.skin.ComponentSkin;

/**
 * Table view header skin.
 *
 * @author gbrown
 */
public class TerraTableViewHeaderSkin extends ComponentSkin
    implements TableViewHeader.Skin, TableViewHeaderListener, TableViewColumnListener {
    private class SortIndicatorImage extends Image {
        private SortDirection sortDirection = null;

        public SortIndicatorImage(SortDirection sortDirection) {
            this.sortDirection = sortDirection;
        }

        public int getWidth() {
            return 7;
        }

        public int getHeight() {
            return 4;
        }

        public Dimensions getPreferredSize() {
            return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
        }

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

    private class ResizeHandler
        implements ComponentMouseListener, ComponentMouseButtonListener {
        TableView.Column column = null;
        int headerX = 0;
        int offset = 0;

        public static final int MINIMUM_COLUMN_WIDTH = 2;

        public ResizeHandler(TableView.Column column, int headerX, int offset) {
            assert (!column.isRelative()) : "Relative width columns cannot be resized.";

            this.column = column;
            this.headerX = headerX;
            this.offset = offset;
        }

        public boolean mouseMove(Component component, int x, int y) {
            int columnWidth = Math.max(x - headerX + offset, MINIMUM_COLUMN_WIDTH);
            column.setWidth(columnWidth, false);

            return false;
        }

        public void mouseOver(Component component) {
        }

        public void mouseOut(Component component) {
        }

        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            Mouse.setCursor(component.getCursor());

            assert (component instanceof Display);
            component.getComponentMouseListeners().remove(this);
            component.getComponentMouseButtonListeners().remove(this);

            resizing = false;
            Mouse.setCursor(getComponent().getCursor());

            return false;
        }

        public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
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

    // Derived colors
    private Color bevelColor;
    private Color pressedBevelColor;
    private Color disabledBevelColor;

    private int pressedHeaderIndex = -1;
    private boolean resizing = false;

    private static final int SORT_INDICATOR_PADDING = 2;
    private static final int RESIZE_HANDLE_SIZE = 6;

    protected SortIndicatorImage sortAscendingImage = new SortIndicatorImage(SortDirection.ASCENDING);
    protected SortIndicatorImage sortDescendingImage = new SortIndicatorImage(SortDirection.DESCENDING);

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

    public void install(Component component) {
        super.install(component);

        TableViewHeader tableViewHeader = (TableViewHeader)component;
        tableViewHeader.getTableViewHeaderListeners().add(this);

        TableView tableView = tableViewHeader.getTableView();
        if (tableView != null) {
            tableView.getTableViewColumnListeners().add(this);
        }
    }

    public void uninstall() {
        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
        tableViewHeader.getTableViewHeaderListeners().remove(this);

        TableView tableView = tableViewHeader.getTableView();
        if (tableView != null) {
            tableView.getTableViewColumnListeners().remove(this);
        }

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
        TableView tableView = tableViewHeader.getTableView();

        if (tableView != null) {
            TableView.ColumnSequence columns = tableView.getColumns();

            int n = columns.getLength();
            int gridLineStop = includeTrailingVerticalGridLine ? n : n - 1;

            for (int i = 0; i < n; i++) {
                TableView.Column column = columns.get(i);

                if (!column.isRelative()) {
                    preferredWidth += column.getWidth();

                    // Include space for vertical gridlines
                    if (i < gridLineStop) {
                        preferredWidth++;
                    }
                }
            }
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
        TableView tableView = tableViewHeader.getTableView();

        if (tableView != null) {
            TableView.ColumnSequence columns = tableView.getColumns();
            TableViewHeader.DataRenderer dataRenderer = tableViewHeader.getDataRenderer();

            for (int i = 0, n = columns.getLength(); i < n; i++) {
                TableView.Column column = columns.get(i);
                dataRenderer.render(column.getHeaderData(), tableViewHeader, false);
                preferredHeight = Math.max(preferredHeight, dataRenderer.getPreferredHeight(-1));
            }

            // Include the bottom border
            preferredHeight++;
        }

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();

        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();

        Color backgroundColor = null;
        Color bevelColor = null;
        Color borderColor = null;

        if (tableViewHeader.isEnabled()) {
            backgroundColor = this.backgroundColor;
            bevelColor = this.bevelColor;
            borderColor = this.borderColor;
        } else {
            backgroundColor = disabledBackgroundColor;
            bevelColor = disabledBevelColor;
            borderColor = disabledBorderColor;
        }

        // Paint the background
        graphics.setPaint(backgroundColor);
        graphics.fillRect(0, 0, width, height);

        // Draw all lines with a 1px solid stroke
        graphics.setStroke(new BasicStroke());

        // Paint the bevel
        Line2D bevelLine = new Line2D.Double(0, 0, width, 0);
        graphics.setPaint(bevelColor);
        graphics.draw(bevelLine);

        // Paint the border
        Line2D borderLine = new Line2D.Double(0, height - 1, width, height - 1);
        graphics.setPaint(borderColor);
        graphics.draw(borderLine);

        // Paint the content
        TableView tableView = tableViewHeader.getTableView();

        if (tableView != null) {
            TableView.ColumnSequence columns = tableView.getColumns();
            Sequence<Integer> columnWidths =
                TerraTableViewSkin.getColumnWidths(columns, getWidth());

            TableViewHeader.DataRenderer dataRenderer = tableViewHeader.getDataRenderer();

            int cellX = 0;
            for (int columnIndex = 0, columnCount = columns.getLength();
                columnIndex < columnCount; columnIndex++) {
                TableView.Column column = columns.get(columnIndex);
                int columnWidth = columnWidths.get(columnIndex);

                // Paint the pressed bevel
                if (columnIndex == pressedHeaderIndex) {
                    bevelLine = new Line2D.Double(cellX, 0, cellX + columnWidth, 0);
                    graphics.setPaint(pressedBevelColor);
                    graphics.draw(bevelLine);
                }

                // Paint the header data
                Object headerData = column.getHeaderData();
                dataRenderer.render(headerData, tableViewHeader, false);
                dataRenderer.setSize(columnWidth, height - 1);

                Graphics2D rendererGraphics = (Graphics2D)graphics.create(cellX, 0,
                    columnWidth, height - 1);
                dataRenderer.paint(rendererGraphics);
                rendererGraphics.dispose();

                // Draw the sort image
                Image sortImage = null;
                SortDirection sortDirection = column.getSortDirection();

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
                    }
                }

                if (sortImage != null) {
                    int sortImageMargin = sortImage.getWidth()
                    + SORT_INDICATOR_PADDING * 2;

                    if (columnWidth >= dataRenderer.getPreferredWidth(-1) + sortImageMargin) {
                        Graphics2D sortImageGraphics = (Graphics2D)graphics.create();
                        sortImageGraphics.translate(cellX + columnWidth - sortImageMargin,
                            (height - sortImage.getHeight()) / 2);
                        sortImage.paint(sortImageGraphics);
                        sortImageGraphics.dispose();
                    }
                }

                // Draw the divider
                cellX += columnWidth;

                Line2D dividerLine = new Line2D.Double(cellX, 0, cellX, height - 1);
                graphics.setPaint(borderColor);
                graphics.draw(dividerLine);

                cellX++;
            }
        }
    }

    public int getHeaderAt(int x) {
        if (x < 0) {
            throw new IllegalArgumentException("x is negative");
        }

        int index = -1;

        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
        TableView tableView = tableViewHeader.getTableView();

        if (tableView != null) {
            Sequence<Integer> columnWidths =
                TerraTableViewSkin.getColumnWidths(tableView.getColumns(), getWidth());

            int i = 0;
            int n = columnWidths.getLength();
            int columnX = 0;
            while (i < n
                && x > columnX) {
                columnX += (columnWidths.get(i) + 1);
                i++;
            }

            if (x <= columnX) {
                index = i - 1;
            }
        }

        return index;
    }

    public Bounds getHeaderBounds(int index) {
        Bounds headerBounds = null;

        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
        TableView tableView = tableViewHeader.getTableView();

        if (tableView != null) {
            Sequence<Integer> columnWidths =
                TerraTableViewSkin.getColumnWidths(tableView.getColumns(), getWidth());

            if (index < 0
                || index >= columnWidths.getLength()) {
                throw new IndexOutOfBoundsException();
            }

            int cellX = 0;
            for (int i = 0; i < index; i++) {
                cellX += (columnWidths.get(i) + 1);
            }

            headerBounds = new Bounds(cellX, 0, columnWidths.get(index), getHeight() - 1);
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

        setFont(Font.decode(font));
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

        setColor(decodeColor(color));
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

        setDisabledColor(decodeColor(disabledColor));
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

        setBackgroundColor(decodeColor(backgroundColor));
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

        setDisabledBackgroundColor(decodeColor(disabledBackgroundColor));
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

        setBorderColor(decodeColor(borderColor));
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

        setDisabledBorderColor(decodeColor(disabledBorderColor));
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
        repaintComponent();
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

        if (!resizing) {
            TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
            TableView tableView = tableViewHeader.getTableView();

            if (tableView != null) {
                int headerIndex = getHeaderAt(x);

                Cursor cursor = tableViewHeader.getCursor();
                if (headerIndex != -1
                    && columnsResizable) {
                    Bounds headerBounds = getHeaderBounds(headerIndex);
                    TableView.Column column = tableView.getColumns().get(headerIndex);

                    if (!column.isRelative()
                        && x > headerBounds.x + headerBounds.width - RESIZE_HANDLE_SIZE) {
                        cursor = Cursor.RESIZE_EAST;
                    }
                }

                Mouse.setCursor(cursor);
            }
        }

        return consumed;
    }

    @Override
    public void mouseOut(Component component) {
        super.mouseOut(component);

        if (pressedHeaderIndex != -1) {
            repaintComponent(getHeaderBounds(pressedHeaderIndex));
            pressedHeaderIndex = -1;
        }
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
        TableView tableView = tableViewHeader.getTableView();

        if (tableView != null) {
            int headerIndex = getHeaderAt(x);

            if (headerIndex != -1) {
                Bounds headerBounds = getHeaderBounds(headerIndex);
                TableView.Column column = tableView.getColumns().get(headerIndex);

                if (columnsResizable
                    && !column.isRelative()
                    && x > headerBounds.x + headerBounds.width - RESIZE_HANDLE_SIZE) {
                    // Begin drag
                    Display display = tableViewHeader.getDisplay();
                    Point headerCoordinates = tableViewHeader.mapPointToAncestor(display,
                        headerBounds.x, 0);
                    ResizeHandler dragHandler = new ResizeHandler(column, headerCoordinates.x,
                        headerBounds.x + headerBounds.width - x);

                    display.getComponentMouseListeners().add(dragHandler);
                    display.getComponentMouseButtonListeners().add(dragHandler);

                    resizing = true;
                    Mouse.setCursor(Cursor.RESIZE_EAST);
                } else if (headersPressable) {
                    pressedHeaderIndex = headerIndex;
                    repaintComponent(getHeaderBounds(pressedHeaderIndex));
                }
            }
        }

        return consumed;
    }

    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        if (pressedHeaderIndex != -1) {
            repaintComponent(getHeaderBounds(pressedHeaderIndex));
        }

        return consumed;
    }

    @Override
    public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();

        if (pressedHeaderIndex != -1
            && headersPressable) {
            tableViewHeader.pressHeader(pressedHeaderIndex);
        }

        pressedHeaderIndex = -1;
    }

    // Table view header events
    public void tableViewChanged(TableViewHeader tableViewHeader,
        TableView previousTableView) {
        if (previousTableView != null) {
            previousTableView.getTableViewColumnListeners().remove(this);
        }

        TableView tableView = tableViewHeader.getTableView();
        if (tableView != null) {
            tableView.getTableViewColumnListeners().add(this);
        }

        invalidateComponent();
    }

    public void dataRendererChanged(TableViewHeader tableViewHeader,
        TableViewHeader.DataRenderer previousDataRenderer) {
        invalidateComponent();
    }

    // Table view column events
    public void columnInserted(TableView tableView, int index) {
        invalidateComponent();
    }

    public void columnsRemoved(TableView tableView, int index, Sequence<TableView.Column> columns) {
        invalidateComponent();
    }

    public void columnNameChanged(TableView.Column column, String previousName) {
        // No-op
    }

    public void columnHeaderDataChanged(TableView.Column column, Object previousHeaderData) {
        invalidateComponent();
    }

    public void columnWidthChanged(TableView.Column column, int previousWidth, boolean previousRelative) {
        invalidateComponent();
    }

    public void columnSortDirectionChanged(TableView.Column column, SortDirection previousSortDirection) {
        repaintComponent();
    }

    public void columnFilterChanged(TableView.Column column, Object previousFilter) {
        // No-op
    }

    public void columnCellRendererChanged(TableView.Column column, TableView.CellRenderer previousCellRenderer) {
        // No-op
    }
}
