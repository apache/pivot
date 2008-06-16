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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Keyboard;
import pivot.wtk.ListView;
import pivot.wtk.ListViewListener;
import pivot.wtk.ListViewItemListener;
import pivot.wtk.ListViewItemStateListener;
import pivot.wtk.ListViewSelectionDetailListener;
import pivot.wtk.Mouse;
import pivot.wtk.Rectangle;
import pivot.wtk.Span;
import pivot.wtk.skin.ComponentSkin;

/**
 * NOTE This skin assumes a fixed renderer height.
 *
 * TODO Support a "showToggleButtons" style.
 *
 * TODO Support a "showHighlight" style?
 *
 * @author gbrown
 */
public class ListViewSkin extends ComponentSkin implements ListView.Skin,
    ListViewListener, ListViewItemListener, ListViewItemStateListener,
    ListViewSelectionDetailListener {
    private int highlightedIndex = -1;

    // Style properties
    protected Font font = DEFAULT_FONT;
    protected Color color = DEFAULT_COLOR;
    protected Color disabledColor = DEFAULT_DISABLED_COLOR;
    protected Color backgroundColor = DEFAULT_BACKGROUND_COLOR;
    protected Color selectionColor = DEFAULT_SELECTION_COLOR;
    protected Color selectionBackgroundColor = DEFAULT_SELECTION_BACKGROUND_COLOR;
    protected Color inactiveSelectionColor = DEFAULT_INACTIVE_SELECTION_COLOR;
    protected Color inactiveSelectionBackgroundColor = DEFAULT_INACTIVE_SELECTION_BACKGROUND_COLOR;
    protected Color highlightColor = DEFAULT_HIGHLIGHT_COLOR;
    protected Color highlightBackgroundColor = DEFAULT_HIGHLIGHT_BACKGROUND_COLOR;

    // Default style values
    private static final Font DEFAULT_FONT = new Font("Verdana", Font.PLAIN, 11);
    private static final Color DEFAULT_COLOR = Color.BLACK;
    private static final Color DEFAULT_DISABLED_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    private static final Color DEFAULT_SELECTION_COLOR = Color.WHITE;
    private static final Color DEFAULT_SELECTION_BACKGROUND_COLOR = new Color(0x14, 0x53, 0x8B);
    private static final Color DEFAULT_INACTIVE_SELECTION_COLOR = Color.BLACK;
    private static final Color DEFAULT_INACTIVE_SELECTION_BACKGROUND_COLOR = new Color(0xcc, 0xca, 0xc2);
    private static final Color DEFAULT_HIGHLIGHT_COLOR = Color.BLACK;
    private static final Color DEFAULT_HIGHLIGHT_BACKGROUND_COLOR = new Color(0xe6, 0xe3, 0xda);

    // Style keys
    protected static final String FONT_KEY = "font";
    protected static final String COLOR_KEY = "color";
    protected static final String DISABLED_COLOR_KEY = "disabledColor";
    protected static final String BACKGROUND_COLOR_KEY = "backgroundColor";
    protected static final String SELECTION_COLOR_KEY = "selectionColor";
    protected static final String SELECTION_BACKGROUND_COLOR_KEY = "selectionBackgroundColor";
    protected static final String INACTIVE_SELECTION_COLOR_KEY = "inactiveSelectionColor";
    protected static final String INACTIVE_SELECTION_BACKGROUND_COLOR_KEY = "inactiveSelectionBackgroundColor";
    protected static final String HIGHLIGHT_COLOR_KEY = "highlightColor";
    protected static final String HIGHLIGHT_BACKGROUND_COLOR_KEY = "highlightBackgroundColor";

    public ListViewSkin() {
    }

    public void install(Component component) {
        validateComponentType(component, ListView.class);

        super.install(component);

        ListView listView = (ListView)component;
        listView.getListViewListeners().add(this);
        listView.getListViewItemListeners().add(this);
        listView.getListViewItemStateListeners().add(this);
        listView.getListViewSelectionDetailListeners().add(this);
    }

    public void uninstall() {
        ListView listView = (ListView)getComponent();
        listView.getListViewListeners().remove(this);
        listView.getListViewItemListeners().remove(this);
        listView.getListViewItemStateListeners().remove(this);
        listView.getListViewSelectionDetailListeners().remove(this);

        super.uninstall();
    }

    @SuppressWarnings("unchecked")
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        ListView listView = (ListView)getComponent();
        List<Object> listData = (List<Object>)listView.getListData();

        ListView.ItemRenderer renderer = listView.getItemRenderer();

        for (Object item : listData) {
            renderer.render(item, listView, false, false, false);
            preferredWidth = Math.max(preferredWidth, renderer.getPreferredWidth(-1));
        }

        return preferredWidth;
    }

    @SuppressWarnings("unchecked")
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        ListView listView = (ListView)getComponent();
        List<Object> listData = (List<Object>)listView.getListData();

        ListView.ItemRenderer renderer = listView.getItemRenderer();
        preferredHeight = listData.getLength() * renderer.getPreferredHeight(-1);

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        // No-op
    }

    @SuppressWarnings("unchecked")
    public void paint(Graphics2D graphics) {
        ListView listView = (ListView)getComponent();
        List<Object> listData = (List<Object>)listView.getListData();
        ListView.ItemRenderer renderer = listView.getItemRenderer();

        int width = getWidth();
        int height = getHeight();
        int itemHeight = renderer.getPreferredHeight(-1);

        // Paint the background
        graphics.setPaint(backgroundColor);
        graphics.fillRect(0, 0, width, height);

        // Paint the list contents
        int itemStart = 0;
        int itemEnd = listData.getLength() - 1;

        // Ensure that we only paint items that are visible
        Shape clip = graphics.getClip();
        if (clip != null) {
            Rectangle2D clipBounds = clip.getBounds();
            itemStart = (int)Math.floor((double)clipBounds.getY() / (double)itemHeight);
            itemEnd = Math.min(itemEnd, (int)Math.ceil((double)(clipBounds.getY()
                + clipBounds.getHeight()) / (double)itemHeight) - 1);
        }

        for (int itemIndex = itemStart; itemIndex <= itemEnd; itemIndex++) {
            Object item = listData.get(itemIndex);
            boolean highlighted = (itemIndex == highlightedIndex
                && Mouse.getButtons() == 0
                && listView.getSelectMode() != ListView.SelectMode.NONE);
            boolean selected = listView.isIndexSelected(itemIndex);
            boolean disabled = listView.isItemDisabled(itemIndex);

            Color itemBackgroundColor = null;

            if (selected) {
                itemBackgroundColor = (listView.isFocused())
                    ? this.selectionBackgroundColor : inactiveSelectionBackgroundColor;
            } else {
                if (highlighted && !disabled) {
                    itemBackgroundColor = highlightBackgroundColor;
                }
            }

            if (itemBackgroundColor != null) {
                graphics.setPaint(itemBackgroundColor);
                graphics.fillRect(0, itemIndex * itemHeight, width, itemHeight);
            }

            // Paint the data
            Graphics2D rendererGraphics = (Graphics2D)graphics.create(0, itemIndex * itemHeight,
                width, itemHeight);

            renderer.render(item, listView, selected, highlighted, disabled);
            renderer.setSize(width, itemHeight);
            renderer.paint(rendererGraphics);
        }
    }

    // List view skin methods
    @SuppressWarnings("unchecked")
    public int getItemAt(int y) {
        if (y < 0) {
            throw new IllegalArgumentException("y is negative");
        }

        ListView listView = (ListView)getComponent();
        List<Object> listData = (List<Object>)listView.getListData();

        ListView.ItemRenderer renderer = listView.getItemRenderer();

        int index = (y / renderer.getPreferredHeight(-1));

        if (index >= listData.getLength()) {
            index = -1;
        }

        return index;
    }

    public Rectangle getItemBounds(int index) {
        ListView listView = (ListView)getComponent();
        ListView.ItemRenderer renderer = listView.getItemRenderer();

        int itemHeight = renderer.getPreferredHeight(-1);
        return new Rectangle(0, index * itemHeight, getWidth(), itemHeight);
    }

    @Override
    public boolean isFocusable() {
        ListView listView = (ListView)getComponent();
        return (listView.getSelectMode() != ListView.SelectMode.NONE);
    }

    @Override
    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        if (key.equals(FONT_KEY)) {
            value = font;
        } else if (key.equals(COLOR_KEY)) {
            value = color;
        } else if (key.equals(DISABLED_COLOR_KEY)) {
            value = disabledColor;
        } else if (key.equals(BACKGROUND_COLOR_KEY)) {
            value = backgroundColor;
        } else if (key.equals(SELECTION_COLOR_KEY)) {
            value = selectionColor;
        } else if (key.equals(SELECTION_BACKGROUND_COLOR_KEY)) {
            value = selectionBackgroundColor;
        } else if (key.equals(INACTIVE_SELECTION_COLOR_KEY)) {
            value = inactiveSelectionColor;
        } else if (key.equals(INACTIVE_SELECTION_BACKGROUND_COLOR_KEY)) {
            value = inactiveSelectionBackgroundColor;
        } else if (key.equals(HIGHLIGHT_COLOR_KEY)) {
            value = highlightColor;
        } else if (key.equals(HIGHLIGHT_BACKGROUND_COLOR_KEY)) {
            value = highlightBackgroundColor;
        } else {
            value = super.get(key);
        }

        return value;
    }

    @Override
    public Object put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(FONT_KEY)) {
            if (value instanceof String) {
                value = Font.decode((String)value);
            }

            validatePropertyType(key, value, Font.class, false);

            previousValue = font;
            font = (Font)value;

            invalidateComponent();
        } else if (key.equals(COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = color;
            color = (Color)value;

            repaintComponent();
        } else if (key.equals(DISABLED_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = disabledColor;
            disabledColor = (Color)value;

            repaintComponent();
        } else if (key.equals(BACKGROUND_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = backgroundColor;
            backgroundColor = (Color)value;

            repaintComponent();
        } else if (key.equals(SELECTION_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = selectionColor;
            selectionColor = (Color)value;

            repaintComponent();
        } else if (key.equals(SELECTION_BACKGROUND_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = selectionBackgroundColor;
            selectionBackgroundColor = (Color)value;

            repaintComponent();
        } else if (key.equals(INACTIVE_SELECTION_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = inactiveSelectionColor;
            inactiveSelectionColor = (Color)value;

            repaintComponent();
        } else if (key.equals(INACTIVE_SELECTION_BACKGROUND_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = inactiveSelectionBackgroundColor;
            inactiveSelectionBackgroundColor = (Color)value;

            repaintComponent();
        } else if (key.equals(HIGHLIGHT_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = highlightColor;
            highlightColor = (Color)value;

            repaintComponent();
        } else if (key.equals(HIGHLIGHT_BACKGROUND_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = highlightBackgroundColor;
            highlightBackgroundColor = (Color)value;

            repaintComponent();
        } else {
            previousValue = super.put(key, value);
        }

        return previousValue;
    }

    @Override
    public Object remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(FONT_KEY)) {
            previousValue = put(key, DEFAULT_FONT);
        } else if (key.equals(COLOR_KEY)) {
            previousValue = put(key, DEFAULT_COLOR);
        } else if (key.equals(DISABLED_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_DISABLED_COLOR);
        } else if (key.equals(BACKGROUND_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_BACKGROUND_COLOR);
        } else if (key.equals(SELECTION_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_SELECTION_COLOR);
        } else if (key.equals(SELECTION_BACKGROUND_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_SELECTION_BACKGROUND_COLOR);
        } else if (key.equals(INACTIVE_SELECTION_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_INACTIVE_SELECTION_COLOR);
        } else if (key.equals(INACTIVE_SELECTION_BACKGROUND_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_INACTIVE_SELECTION_BACKGROUND_COLOR);
        } else if (key.equals(HIGHLIGHT_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_HIGHLIGHT_COLOR);
        } else if (key.equals(HIGHLIGHT_BACKGROUND_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_HIGHLIGHT_BACKGROUND_COLOR);
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

        return (key.equals(FONT_KEY)
            || key.equals(COLOR_KEY)
            || key.equals(DISABLED_COLOR_KEY)
            || key.equals(BACKGROUND_COLOR_KEY)
            || key.equals(SELECTION_BACKGROUND_COLOR_KEY)
            || key.equals(SELECTION_BACKGROUND_COLOR_KEY)
            || key.equals(INACTIVE_SELECTION_COLOR_KEY)
            || key.equals(INACTIVE_SELECTION_BACKGROUND_COLOR_KEY)
            || key.equals(HIGHLIGHT_COLOR_KEY)
            || key.equals(HIGHLIGHT_BACKGROUND_COLOR_KEY)
            || super.containsKey(key));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean mouseMove(int x, int y) {
        boolean consumed = super.mouseMove(x, y);

        int previousHighlightedIndex = this.highlightedIndex;
        highlightedIndex = getItemAt(y);

        if (previousHighlightedIndex != highlightedIndex) {
            if (previousHighlightedIndex != -1) {
                repaintComponent(getItemBounds(previousHighlightedIndex));
            }

            if (highlightedIndex != -1) {
                repaintComponent(getItemBounds(highlightedIndex));
            }
        }

        return consumed;
    }

    @Override
    public void mouseOut() {
        super.mouseOut();

        if (highlightedIndex != -1) {
            Rectangle itemBounds = getItemBounds(highlightedIndex);
            repaintComponent(itemBounds.x, itemBounds.y, itemBounds.width, itemBounds.height);
        }

        highlightedIndex = -1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void mouseClick(Mouse.Button button, int x, int y, int count) {
        ListView listView = (ListView)getComponent();

        if (isFocusable()) {
            Component.setFocusedComponent(listView);
        }

        List<Object> listData = (List<Object>)listView.getListData();
        ListView.ItemRenderer renderer = listView.getItemRenderer();

        int itemHeight = renderer.getPreferredHeight(-1);
        int itemIndex = y / itemHeight;

        if (itemIndex < listData.getLength()
            && !listView.isItemDisabled(itemIndex)) {
            ListView.SelectMode selectMode = listView.getSelectMode();
            int keyboardModifiers = Keyboard.getModifiers();

            if ((keyboardModifiers & Keyboard.Modifier.SHIFT.getMask()) > 0
                && selectMode == ListView.SelectMode.MULTI) {
                // Select the range
                int startIndex = listView.getFirstSelectedIndex();
                int endIndex = listView.getLastSelectedIndex();
                Span selectedRange = (itemIndex > startIndex) ?
                    new Span(startIndex, itemIndex) : new Span(itemIndex, endIndex);

                ArrayList<Span> selectedRanges = new ArrayList<Span>();
                Sequence<Integer> disabledIndexes = listView.getDisabledIndexes();
                if (disabledIndexes.getLength() == 0) {
                    selectedRanges.add(selectedRange);
                } else {
                    // TODO Split the range by the disabled indexes; for now,
                    // just return
                    return;
                }

                listView.setSelectedRanges(selectedRanges);
            } else if ((keyboardModifiers & Keyboard.Modifier.CTRL.getMask()) > 0
                && selectMode == ListView.SelectMode.MULTI) {
                // Toggle the item's selection state
                if (listView.isIndexSelected(itemIndex)) {
                    listView.removeSelectedIndex(itemIndex);
                } else {
                    listView.addSelectedIndex(itemIndex);
                }
            } else {
                // Select the item
                if ((selectMode == ListView.SelectMode.SINGLE
                        && listView.getSelectedIndex() != itemIndex)
                    || selectMode == ListView.SelectMode.MULTI) {
                    listView.setSelectedIndex(itemIndex);
                }
            }
        }
    }

    @Override
    public boolean mouseWheel(Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        if (highlightedIndex != -1) {
            Rectangle itemBounds = getItemBounds(highlightedIndex);
            repaintComponent(itemBounds.x, itemBounds.y, itemBounds.width, itemBounds.height);
        }

        highlightedIndex = -1;

        return super.mouseWheel(scrollType, scrollAmount, wheelRotation, x, y);
    }

    @Override
    public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(keyCode, keyLocation);

        ListView listView = (ListView)getComponent();

        switch (keyCode) {
            case Keyboard.KeyCode.UP: {
                int index = listView.getFirstSelectedIndex();

                do {
                    index--;
                } while (index >= 0
                    && listView.isItemDisabled(index));

                if (index >= 0) {
                    if ((Keyboard.getModifiers() & Keyboard.Modifier.SHIFT.getMask()) > 0
                        && listView.getSelectMode() == ListView.SelectMode.MULTI) {
                        listView.addSelectedIndex(index);
                    } else {
                        listView.setSelectedIndex(index);
                    }

                    listView.scrollAreaToVisible(getItemBounds(index));
                }

                consumed = true;
                break;
            }

            case Keyboard.KeyCode.DOWN: {
                int index = listView.getLastSelectedIndex();
                int count = listView.getListData().getLength();

                do {
                    index++;
                } while (index < count
                    && listView.isItemDisabled(index));

                if (index < count) {
                    if ((Keyboard.getModifiers() & Keyboard.Modifier.SHIFT.getMask()) > 0
                        && listView.getSelectMode() == ListView.SelectMode.MULTI) {
                        listView.addSelectedIndex(index);
                    } else {
                        listView.setSelectedIndex(index);
                    }

                    listView.scrollAreaToVisible(getItemBounds(index));
                }

                consumed = true;
                break;
            }
        }

        // Clear the highlight
        if (highlightedIndex != -1) {
            highlightedIndex = -1;
            repaintComponent(getItemBounds(highlightedIndex));
        }

        return consumed;
    }

    // Component state events
    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        repaintComponent();
    }

    @Override
    public void focusedChanged(Component component, boolean temporary) {
        super.focusedChanged(component, temporary);

        repaintComponent();
    }

    // List view events
    public void listDataChanged(ListView listView, List<?> previousListData) {
        invalidateComponent();
    }

    public void itemRendererChanged(ListView listView, ListView.ItemRenderer previousItemRenderer) {
        invalidateComponent();
    }

    public void selectModeChanged(ListView listView, ListView.SelectMode previousSelectMode) {
        // No-op
    }

    public void selectedValueKeyChanged(ListView listView, String previousSelectedIndexKey) {
        // No-op
    }

    public void selectedValuesKeyChanged(ListView listView, String previousSelectedValuesKey) {
        // No-op
    }

    public void valueMappingChanged(ListView listView, ListView.ValueMapping previousValueMapping) {
        // No-op
    }

    // List view item events
    public void itemInserted(ListView listView, int index) {
        invalidateComponent();
    }

    public void itemsRemoved(ListView listView, int index, int count) {
        invalidateComponent();
    }

    public void itemUpdated(ListView listView, int index) {
        invalidateComponent();
    }

    public void itemsSorted(ListView listView) {
        repaintComponent();
    }

    // List view item state events
    public void itemDisabledChanged(ListView listView, int index) {
        repaintComponent(getItemBounds(index));
    }

    // List view selection detail events
    public void selectedRangeAdded(ListView listView, int rangeStart, int rangeEnd) {
        // Repaint the area containing the added selection
        ListView.ItemRenderer renderer = listView.getItemRenderer();

        int itemHeight = renderer.getPreferredHeight(-1);
        repaintComponent(0, rangeStart * itemHeight,
            getWidth(), (rangeEnd - rangeStart + 1) * itemHeight);
    }

    public void selectedRangeRemoved(ListView listView, int rangeStart, int rangeEnd) {
        // Repaint the area containing the removed selection
        ListView.ItemRenderer renderer = listView.getItemRenderer();

        int itemHeight = renderer.getPreferredHeight(-1);
        repaintComponent(0, rangeStart * itemHeight,
            getWidth(), (rangeEnd - rangeStart + 1) * itemHeight);
    }

    public void selectionReset(ListView listView, Sequence<Span> previousSelectedRanges) {
        // TODO Repaint only the area that changed (intersection of previous
        // and new selection)
        repaintComponent();
    }
}
