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
import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Cursor;
import pivot.wtk.Dimensions;
import pivot.wtk.Display;
import pivot.wtk.Mouse;
import pivot.wtk.Point;
import pivot.wtk.Rectangle;
import pivot.wtk.SortDirection;
import pivot.wtk.TableView;
import pivot.wtk.TableViewColumnListener;
import pivot.wtk.TableViewHeader;
import pivot.wtk.TableViewHeaderListener;
import pivot.wtk.media.Image;
import pivot.wtk.skin.ComponentSkin;

public class TableViewHeaderSkin extends ComponentSkin
    implements TableViewHeader.Skin, TableViewHeaderListener, TableViewColumnListener {
    private class SortIndicatorImage extends ImageAsset {
        private SortDirection sortDirection = null;

        public SortIndicatorImage(SortDirection sortDirection) {
            this.sortDirection = sortDirection;
        }

        public int getPreferredWidth(int height) {
            return 7;
        }

        public int getPreferredHeight(int width) {
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

    private class DragHandler
        implements ComponentMouseListener, ComponentMouseButtonListener {
        TableView.Column column = null;
        int headerX = 0;
        int offset = 0;

        public static final int MINIMUM_COLUMN_WIDTH = 2;

        public DragHandler(TableView.Column column, int headerX, int offset) {
            assert (!column.isRelative()) : "Relative width columns cannot be resized.";

            this.column = column;
            this.headerX = headerX;
            this.offset = offset;
        }

        public void mouseMove(Component component, int x, int y) {
            int columnWidth = Math.max(x - headerX + offset, MINIMUM_COLUMN_WIDTH);
            column.setWidth(columnWidth, false);
        }

        public void mouseOver(Component component) {
        }

        public void mouseOut(Component component) {
        }

        public void mouseDown(Component component, Mouse.Button button, int x, int y) {
        }

        public void mouseUp(Component component, Mouse.Button button, int x, int y) {
            ApplicationContext.getInstance().setCursor(component.getCursor());
            Display.getInstance().getComponentMouseListeners().remove(this);
            Display.getInstance().getComponentMouseButtonListeners().remove(this);
        }

        public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        }
    }

    private Font font = new Font("Verdana", Font.PLAIN, 11);
    private Color color = Color.BLACK;
    private Color disabledColor = new Color(0x99, 0x99, 0x99);
    private Color backgroundColor = new Color(0xE6, 0xE3, 0xDA);
    private Color disabledBackgroundColor = new Color(0xF7, 0xF5, 0xEB);
    private Color borderColor = new Color(0x99, 0x99, 0x99);
    private Color disabledBorderColor = new Color(0xCC, 0xCC, 0xCC);
    private Color bevelColor = new Color(0xF7, 0xF5, 0xEB);
    private Color pressedBevelColor = new Color(0xCC, 0xCA, 0xC2);
    private Color disabledBevelColor = Color.WHITE;

    private int pressedHeaderIndex = -1;

    private static final int SORT_INDICATOR_PADDING = 2;
    private static final int RESIZE_HANDLE_SIZE = 6;

    protected SortIndicatorImage sortAscendingImage = new SortIndicatorImage(SortDirection.ASCENDING);
    protected SortIndicatorImage sortDescendingImage = new SortIndicatorImage(SortDirection.DESCENDING);

    public TableViewHeaderSkin() {
    }

    public void install(Component component) {
        validateComponentType(component, TableViewHeader.class);

        super.install(component);

        TableViewHeader tableViewHeader = (TableViewHeader)component;
        tableViewHeader.getTableViewHeaderListeners().add(this);

        TableView tableView = tableViewHeader.getTableView();
        tableView.getTableViewColumnListeners().add(this);
    }

    public void uninstall() {
        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
        tableViewHeader.getTableViewHeaderListeners().remove(this);

        TableView tableView = tableViewHeader.getTableView();
        tableView.getTableViewColumnListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
        TableView tableView = tableViewHeader.getTableView();
        TableView.ColumnSequence columns = tableView.getColumns();

        for (int i = 0, n = columns.getLength(); i < n; i++) {
            TableView.Column column = columns.get(i);

            if (!column.isRelative()) {
                preferredWidth += column.getWidth();

                // Include space for vertical gridlines
                if (i > 0
                    && i < n - 1) {
                    preferredWidth++;
                }
            }
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
        TableViewHeader.DataRenderer dataRenderer = tableViewHeader.getDataRenderer();
        TableView tableView = tableViewHeader.getTableView();
        TableView.ColumnSequence columns = tableView.getColumns();

        int preferredHeight = 0;

        for (int i = 0, n = columns.getLength(); i < n; i++) {
            TableView.Column column = columns.get(i);
            dataRenderer.render(column.getHeaderData(), tableViewHeader, false);
            preferredHeight = Math.max(preferredHeight, dataRenderer.getPreferredHeight(-1));
        }

        // Include the bottom border
        preferredHeight++;

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
        TableView tableView = tableViewHeader.getTableView();
        TableView.ColumnSequence columns = tableView.getColumns();

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
        Rectangle bounds = new Rectangle(0, 0, width, height);
        graphics.fill(bounds);

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
        // TODO Optimize by painting only headers that intersect with the
        // current clip region?
        int cellX = 0;
        Sequence<Integer> columnWidths = getColumnWidths();
        TableViewHeader.DataRenderer dataRenderer = tableViewHeader.getDataRenderer();

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
                int sortImageMargin = sortImage.getPreferredWidth(-1)
                + SORT_INDICATOR_PADDING * 2;

                if (columnWidth >= dataRenderer.getPreferredWidth(-1) + sortImageMargin) {
                    Graphics2D sortImageGraphics = (Graphics2D)graphics.create();
                    sortImageGraphics.translate(cellX + columnWidth - sortImageMargin,
                        (height - sortImage.getPreferredHeight(-1)) / 2);
                    sortImage.paint(sortImageGraphics);
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

    /**
     * Returns the column widths for this table header.
     *
     * @return
     * The widths of all columns based on the current overall width.
     */
    public Sequence<Integer> getColumnWidths() {
        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();

        return TableViewSkin.getColumnWidths(tableViewHeader.getTableView().getColumns(),
            getWidth());
    }

    public int getHeaderAt(int x) {
        if (x < 0) {
            throw new IllegalArgumentException("x is negative");
        }

        Sequence<Integer> columnWidths = getColumnWidths();

        int i = 0;
        int n = columnWidths.getLength();
        int columnX = 0;
        while (i < n
            && x > columnX) {
            columnX += (columnWidths.get(i) + 1);
            i++;
        }

        int index = -1;

        if (x <= columnX) {
            index = i - 1;
        }

        return index;
    }

    public Rectangle getHeaderBounds(int index) {
        Sequence<Integer> columnWidths = getColumnWidths();

        if (index < 0
            || index >= columnWidths.getLength()) {
            throw new IndexOutOfBoundsException();
        }


        int cellX = 0;
        for (int i = 0; i < index; i++) {
            cellX += (columnWidths.get(i) + 1);
        }

        return new Rectangle(cellX, 0, columnWidths.get(index), getHeight() - 1);
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

        setColor(Color.decode(color));
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

        setDisabledColor(Color.decode(disabledColor));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        setBackgroundColor(Color.decode(backgroundColor));
    }

    public Color getDisabledBackgroundColor() {
        return disabledBackgroundColor;
    }

    public void setDisabledBackgroundColor(Color disabledBackgroundColor) {
        if (disabledBackgroundColor == null) {
            throw new IllegalArgumentException("disabledBackgroundColor is null.");
        }

        this.disabledBackgroundColor = disabledBackgroundColor;
        repaintComponent();
    }

    public final void setDisabledBackgroundColor(String disabledBackgroundColor) {
        if (disabledBackgroundColor == null) {
            throw new IllegalArgumentException("disabledBackgroundColor is null.");
        }

        setDisabledBackgroundColor(Color.decode(disabledBackgroundColor));
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

        setBorderColor(Color.decode(borderColor));
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

        setDisabledBorderColor(Color.decode(disabledBorderColor));
    }

    public Color getBevelColor() {
        return bevelColor;
    }

    public void setBevelColor(Color bevelColor) {
        if (bevelColor == null) {
            throw new IllegalArgumentException("bevelColor is null.");
        }

        this.bevelColor = bevelColor;
        repaintComponent();
    }

    public final void setBevelColor(String bevelColor) {
        if (bevelColor == null) {
            throw new IllegalArgumentException("bevelColor is null.");
        }

        setBevelColor(Color.decode(bevelColor));
    }

    public Color getPressedBevelColor() {
        return pressedBevelColor;
    }

    public void setPressedBevelColor(Color pressedBevelColor) {
        if (pressedBevelColor == null) {
            throw new IllegalArgumentException("pressedBevelColor is null.");
        }

        this.pressedBevelColor = pressedBevelColor;
        repaintComponent();
    }

    public final void setPressedBevelColor(String pressedBevelColor) {
        if (pressedBevelColor == null) {
            throw new IllegalArgumentException("pressedBevelColor is null.");
        }

        setPressedBevelColor(Color.decode(pressedBevelColor));
    }

    public Color getDisabledBevelColor() {
        return disabledBevelColor;
    }

    public void setDisabledBevelColor(Color disabledBevelColor) {
        if (disabledBevelColor == null) {
            throw new IllegalArgumentException("disabledBevelColor is null.");
        }

        this.disabledBevelColor = disabledBevelColor;
        repaintComponent();
    }

    public final void setDisabledBevelColor(String disabledBevelColor) {
        if (disabledBevelColor == null) {
            throw new IllegalArgumentException("disabledBevelColor is null.");
        }

        setDisabledBackgroundColor(Color.decode(disabledBevelColor));
    }

    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        pressedHeaderIndex = -1;
        repaintComponent();
    }

    @Override
    public boolean mouseMove(int x, int y) {
        boolean consumed = super.mouseMove(x, y);

        int headerIndex = getHeaderAt(x);
        if (headerIndex != -1) {
            TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
            TableView tableView = tableViewHeader.getTableView();
            TableView.ColumnSequence columns = tableView.getColumns();

            TableView.Column column = columns.get(headerIndex);

            Rectangle headerBounds = getHeaderBounds(headerIndex);

            if (Mouse.getButtons() == 0) {
                if (!column.isRelative()
                    && x > headerBounds.x + headerBounds.width - RESIZE_HANDLE_SIZE) {
                    ApplicationContext.getInstance().setCursor(Cursor.RESIZE_EAST);
                } else {
                    ApplicationContext.getInstance().setCursor(getComponent().getCursor());
                }
            }
        }

        return consumed;
    }

    @Override
    public void mouseOut() {
        super.mouseOut();

        if (pressedHeaderIndex != -1) {
            // TODO Repaint pressed header bounds only
            repaintComponent();
            pressedHeaderIndex = -1;
        }
    }

    @Override
    public boolean mouseDown(Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(button, x, y);

        int headerIndex = getHeaderAt(x);

        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();
        TableView tableView = tableViewHeader.getTableView();
        TableView.ColumnSequence columns = tableView.getColumns();

        TableView.Column column = columns.get(headerIndex);

        Rectangle headerBounds = getHeaderBounds(headerIndex);

        if (!column.isRelative()
            && x > headerBounds.x + headerBounds.width - RESIZE_HANDLE_SIZE) {
            // Begin drag
            Point headerCoordinates = tableViewHeader.mapPointToAncestor(Display.getInstance(),
                headerBounds.x, 0);
            DragHandler dragHandler = new DragHandler(column, headerCoordinates.x,
                headerBounds.x + headerBounds.width - x);
            Display.getInstance().getComponentMouseListeners().add(dragHandler);
            Display.getInstance().getComponentMouseButtonListeners().add(dragHandler);
        } else {
            pressedHeaderIndex = getHeaderAt(x);

            // TODO Repaint pressed header bounds only
            repaintComponent();
        }

        return consumed;
    }

    @Override
    public boolean mouseUp(Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(button, x, y);

        repaintComponent();

        return consumed;
    }

    @Override
    public void mouseClick(Mouse.Button button, int x, int y, int count) {
        TableViewHeader tableViewHeader = (TableViewHeader)getComponent();

        if (pressedHeaderIndex != -1) {
            tableViewHeader.pressHeader(pressedHeaderIndex);
        }

        pressedHeaderIndex = -1;
    }

    // Table view header events
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

    public void columnNameChanged(TableView tableView, int index, String previousName) {
        // No-op
    }

    public void columnHeaderDataChanged(TableView tableView, int index, Object previousHeaderData) {
        invalidateComponent();
    }

    public void columnWidthChanged(TableView tableView, int index, int previousWidth, boolean previousRelative) {
        invalidateComponent();
    }

    public void columnSortDirectionChanged(TableView tableView, int index, SortDirection previousSortDirection) {
        repaintComponent();
    }

    public void columnFilterChanged(TableView tableView, int index, Object previousFilter) {
        // No-op
    }

    public void columnCellRendererChanged(TableView tableView, int index, TableView.CellRenderer previousCellRenderer) {
        // No-op
    }
}
