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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSON;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.TableView;

/**
 * Table cell renderer that supports dynamic rendering based on the type of
 * content being rendered.
 */
public class TableViewMultiCellRenderer implements TableView.CellRenderer {
    /**
     * Internal style dictionary that supports no styles.
     */
    private static class StyleDictionary implements Dictionary<String, Object> {
        @Override
        public Object get(String key) {
            return null;
        }

        @Override
        public Object put(String key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(String key) {
            return false;
        }
    }

    /**
     * Maps the type of value being rendered (the value class) to a specific
     * cell renderer.
     */
    public static final class RendererMapping {
        private Class<?> valueClass = null;
        private TableView.CellRenderer cellRenderer = null;

        private TableViewMultiCellRenderer multiCellRenderer = null;

        public Class<?> getValueClass() {
            return valueClass;
        }

        public void setValueClass(Class<?> valueClass) {
            if (valueClass == null) {
                throw new IllegalArgumentException("valueClass is null.");
            }

            Class<?> previousValueClass = this.valueClass;

            if (valueClass != previousValueClass) {
                this.valueClass = valueClass;

                if (multiCellRenderer != null) {
                    multiCellRenderer.cellRenderers.remove(previousValueClass);
                    multiCellRenderer.cellRenderers.put(valueClass, cellRenderer);
                }
            }
        }

        public void setValueClass(String valueClass) {
            if (valueClass == null) {
                throw new IllegalArgumentException("valueClass is null.");
            }

            try {
                setValueClass(Class.forName(valueClass));
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }

        public TableView.CellRenderer getCellRenderer() {
            return cellRenderer;
        }

        public void setCellRenderer(TableView.CellRenderer cellRenderer) {
            if (cellRenderer == null) {
                throw new IllegalArgumentException("cellRenderer is null.");
            }

            TableView.CellRenderer previousCellRenderer = this.cellRenderer;

            if (cellRenderer != previousCellRenderer) {
                this.cellRenderer = cellRenderer;

                if (multiCellRenderer != null) {
                    multiCellRenderer.cellRenderers.put(valueClass, cellRenderer);
                }
            }
        }
    }

    /**
     * Provides a sequence hook into this renderer's mappings, thus enabling
     * developers to define their multi-cell renderer in BXML.
     */
    private class RendererMappingSequence implements Sequence<RendererMapping> {
        private ArrayList<RendererMapping> mappings = new ArrayList<RendererMapping>();

        @Override
        public int add(RendererMapping item) {
            int index = mappings.getLength();
            insert(item, index);
            return index;
        }

        @Override
        public void insert(RendererMapping item, int index) {
            if (item == null) {
                throw new IllegalArgumentException("item is null.");
            }

            Class<?> valueClass = item.getValueClass();

            if (cellRenderers.containsKey(valueClass)) {
                throw new IllegalArgumentException("Duplicate value class mapping: " +
                    (valueClass == null ? "null" : valueClass.getName()));
            }

            mappings.insert(item, index);

            TableView.CellRenderer cellRenderer = item.getCellRenderer();
            cellRenderers.put(valueClass, cellRenderer);

            item.multiCellRenderer = TableViewMultiCellRenderer.this;
        }

        @Override
        public RendererMapping update(int index, RendererMapping item) {
            if (item == null) {
                throw new IllegalArgumentException("item is null.");
            }

            if (index >= getLength()) {
                throw new IndexOutOfBoundsException();
            }

            RendererMapping previousItem = mappings.get(index);

            if (item != previousItem) {
                Class<?> valueClass = item.getValueClass();
                Class<?> previousValueClass = previousItem.getValueClass();

                if (cellRenderers.containsKey(valueClass)
                    && valueClass != previousValueClass) {
                    throw new IllegalArgumentException("Duplicate value class mapping: " +
                        valueClass.getName());
                }

                mappings.update(index, item);

                TableView.CellRenderer cellRenderer = item.getCellRenderer();
                cellRenderers.remove(previousValueClass);
                cellRenderers.put(valueClass, cellRenderer);

                previousItem.multiCellRenderer = null;
                item.multiCellRenderer = TableViewMultiCellRenderer.this;
            }

            return previousItem;
        }

        @Override
        public int remove(RendererMapping item) {
            int index = mappings.indexOf(item);

            if (index >= 0) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<RendererMapping> remove(int index, int count) {
            Sequence<RendererMapping> removed = mappings.remove(index, count);

            for (int i = 0, n = removed.getLength(); i < n; i++) {
                RendererMapping item = removed.get(i);
                Class<?> valueClass = item.getValueClass();

                cellRenderers.remove(valueClass);
                item.multiCellRenderer = null;
            }

            return removed;
        }

        @Override
        public RendererMapping get(int index) {
            return mappings.get(index);
        }

        @Override
        public int indexOf(RendererMapping item) {
            return mappings.indexOf(item);
        }

        @Override
        public int getLength() {
            return mappings.getLength();
        }
    }

    private int width;
    private int height;

    private HashMap<Class<?>, TableView.CellRenderer> cellRenderers =
        new HashMap<Class<?>, TableView.CellRenderer>();

    private TableView.CellRenderer defaultRenderer = new TableViewCellRenderer();
    private TableView.CellRenderer currentRenderer = defaultRenderer;

    private RendererMappingSequence rendererMappings = new RendererMappingSequence();

    private static final StyleDictionary STYLES = new StyleDictionary();

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getBaseline() {
        return currentRenderer.getBaseline();
    }

    @Override
    public void paint(Graphics2D graphics) {
        currentRenderer.paint(graphics);
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;

        currentRenderer.setSize(width, height);
    }

    @Override
    public int getPreferredWidth(int heightArgument) {
        return currentRenderer.getPreferredWidth(heightArgument);
    }

    @Override
    public int getPreferredHeight(int widthArgument) {
        // Our preferred height is the maximum of all our possible renderers'
        // preferred height
        int preferredHeight = defaultRenderer.getPreferredHeight(widthArgument);

        for (Class<?> key : cellRenderers) {
            TableView.CellRenderer renderer = cellRenderers.get(key);
            preferredHeight = Math.max(preferredHeight,
                renderer.getPreferredHeight(widthArgument));
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    @Override
    public int getBaseline(int widthArgument, int heightArgument) {
        return -1;
    }

    @Override
    public Dictionary<String, Object> getStyles() {
        return STYLES;
    }

    @SuppressWarnings("null")  // false warning from eclipse
    @Override
    public void render(Object row, int rowIndex, int columnIndex,
        TableView tableView, String columnName,
        boolean selected, boolean highlighted, boolean disabled) {
        if (row == null) {
            for (Class<?> key : cellRenderers) {
                TableView.CellRenderer renderer = cellRenderers.get(key);
                renderer.render(null, rowIndex, columnIndex, tableView, columnName,
                    selected, highlighted, disabled);
            }
        } else {
            Object cellData = JSON.get(row, columnName);

            TableView.CellRenderer cellRenderer = null;
            Class<?> valueClass = (cellData == null ? null : cellData.getClass());
            while (cellRenderer == null
                && valueClass != Object.class) {
                cellRenderer = cellRenderers.get(valueClass);

                if (cellRenderer == null) {
                    valueClass = valueClass.getSuperclass();
                }
            }

            if (cellRenderer == null) {
                cellRenderer = defaultRenderer;
            }

            if (cellRenderer != currentRenderer) {
                currentRenderer = cellRenderer;
                cellRenderer.setSize(width, height);
            }

            cellRenderer.render(row, rowIndex, columnIndex, tableView, columnName,
                selected, highlighted, disabled);
        }
    }

    @SuppressWarnings("null")  // false warning from eclipse
    @Override
    public String toString(Object row, String columnName) {
        Object cellData = JSON.get(row, columnName);

        TableView.CellRenderer cellRenderer = null;
        Class<?> valueClass = (cellData == null ? null : cellData.getClass());
        while (cellRenderer == null
            && valueClass != Object.class) {
            cellRenderer = cellRenderers.get(valueClass);

            if (cellRenderer == null) {
                valueClass = valueClass.getSuperclass();
            }
        }

        if (cellRenderer == null) {
            cellRenderer = defaultRenderer;
        }

        return cellRenderer.toString(row, columnName);
    }

    public TableView.CellRenderer getDefaultRenderer() {
        return defaultRenderer;
    }

    public void setDefaultRenderer(TableView.CellRenderer defaultRenderer) {
        if (defaultRenderer == null) {
            throw new IllegalArgumentException("defaultRenderer is null.");
        }

        this.defaultRenderer = defaultRenderer;
    }

    public Sequence<RendererMapping> getRendererMappings() {
        return rendererMappings;
    }
}
