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
import pivot.util.Vote;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Keyboard;
import pivot.wtk.ListView;
import pivot.wtk.ListViewItemStateListener;
import pivot.wtk.ListViewListener;
import pivot.wtk.ListViewItemListener;
import pivot.wtk.ListViewSelectionDetailListener;
import pivot.wtk.Mouse;
import pivot.wtk.Bounds;
import pivot.wtk.Span;
import pivot.wtk.skin.ComponentSkin;

/**
 * List view skin.
 * <p>
 * NOTE This skin assumes a fixed renderer height.
 * <p>
 * TODO Support a "showToggleButtons" style.
 *
 * @author gbrown
 */
public class TerraListViewSkin extends ComponentSkin implements ListView.Skin,
    ListViewListener, ListViewItemListener, ListViewItemStateListener,
    ListViewSelectionDetailListener {
    private Font font = new Font("Verdana", Font.PLAIN, 11);
    private Color color = Color.BLACK;
    private Color disabledColor = new Color(0x99, 0x99, 0x99);
    private Color backgroundColor = Color.WHITE;
    private Color selectionColor = Color.WHITE;
    private Color selectionBackgroundColor = new Color(0x14, 0x53, 0x8B);
    private Color inactiveSelectionColor = Color.BLACK;
    private Color inactiveSelectionBackgroundColor = new Color(0xcc, 0xca, 0xc2);
    private Color highlightColor = Color.BLACK;
    private Color highlightBackgroundColor = new Color(0xe6, 0xe3, 0xda);
    private boolean showHighlight = true;

    private int highlightedIndex = -1;

    public void install(Component component) {
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
            itemStart = (int)Math.floor(clipBounds.getY() / (double)itemHeight);
            itemEnd = Math.min(itemEnd, (int)Math.ceil((clipBounds.getY()
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
                if (highlighted && showHighlight && !disabled) {
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
            rendererGraphics.dispose();
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

    public Bounds getItemBounds(int index) {
        ListView listView = (ListView)getComponent();
        ListView.ItemRenderer renderer = listView.getItemRenderer();

        int itemHeight = renderer.getPreferredHeight(-1);
        return new Bounds(0, index * itemHeight, getWidth(), itemHeight);
    }

    @Override
    public boolean isFocusable() {
        ListView listView = (ListView)getComponent();
        return (listView.getSelectMode() != ListView.SelectMode.NONE);
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


    public Color getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(Color selectionColor) {
        if (selectionColor == null) {
            throw new IllegalArgumentException("selectionColor is null.");
        }

        this.selectionColor = selectionColor;
        repaintComponent();
    }

    public final void setSelectionColor(String selectionColor) {
        if (selectionColor == null) {
            throw new IllegalArgumentException("selectionColor is null.");
        }

        setSelectionColor(Color.decode(selectionColor));
    }

    public Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    public void setSelectionBackgroundColor(Color selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        this.selectionBackgroundColor = selectionBackgroundColor;
        repaintComponent();
    }

    public final void setSelectionBackgroundColor(String selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        setSelectionBackgroundColor(Color.decode(selectionBackgroundColor));
    }

    public Color getInactiveSelectionColor() {
        return inactiveSelectionColor;
    }

    public void setInactiveSelectionColor(Color inactiveSelectionColor) {
        if (inactiveSelectionColor == null) {
            throw new IllegalArgumentException("inactiveSelectionColor is null.");
        }

        this.inactiveSelectionColor = inactiveSelectionColor;
        repaintComponent();
    }

    public final void setInactiveSelectionColor(String inactiveSelectionColor) {
        if (inactiveSelectionColor == null) {
            throw new IllegalArgumentException("inactiveSelectionColor is null.");
        }

        setInactiveSelectionColor(Color.decode(inactiveSelectionColor));
    }

    public Color getInactiveSelectionBackgroundColor() {
        return inactiveSelectionBackgroundColor;
    }

    public void setInactiveSelectionBackgroundColor(Color inactiveSelectionBackgroundColor) {
        if (inactiveSelectionBackgroundColor == null) {
            throw new IllegalArgumentException("inactiveSelectionBackgroundColor is null.");
        }

        this.inactiveSelectionBackgroundColor = inactiveSelectionBackgroundColor;
        repaintComponent();
    }

    public final void setInactiveSelectionBackgroundColor(String inactiveSelectionBackgroundColor) {
        if (inactiveSelectionBackgroundColor == null) {
            throw new IllegalArgumentException("inactiveSelectionBackgroundColor is null.");
        }

        setInactiveSelectionBackgroundColor(Color.decode(inactiveSelectionBackgroundColor));
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(Color highlightColor) {
        if (highlightColor == null) {
            throw new IllegalArgumentException("highlightColor is null.");
        }

        this.highlightColor = highlightColor;
        repaintComponent();
    }

    public final void setHighlightColor(String highlightColor) {
        if (highlightColor == null) {
            throw new IllegalArgumentException("highlightColor is null.");
        }

        setHighlightColor(Color.decode(highlightColor));
    }

    public Color getHighlightBackgroundColor() {
        return highlightBackgroundColor;
    }

    public void setHighlightBackgroundColor(Color highlightBackgroundColor) {
        if (highlightBackgroundColor == null) {
            throw new IllegalArgumentException("highlightBackgroundColor is null.");
        }

        this.highlightBackgroundColor = highlightBackgroundColor;
        repaintComponent();
    }

    public final void setHighlightBackgroundColor(String highlightBackgroundColor) {
        if (highlightBackgroundColor == null) {
            throw new IllegalArgumentException("highlightBackgroundColor is null.");
        }

        setHighlightBackgroundColor(Color.decode(highlightBackgroundColor));
    }

    public boolean getShowHighlight() {
        return showHighlight;
    }

    public void setShowHighlight(boolean showHighlight) {
        this.showHighlight = showHighlight;
        repaintComponent();
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

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
    public void mouseOut(Component component) {
        super.mouseOut(component);

        if (highlightedIndex != -1) {
            Bounds itemBounds = getItemBounds(highlightedIndex);
            repaintComponent(itemBounds.x, itemBounds.y, itemBounds.width, itemBounds.height);
        }

        highlightedIndex = -1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        ListView listView = (ListView)getComponent();

        if (isFocusable()) {
            listView.requestFocus();
        }

        List<Object> listData = (List<Object>)listView.getListData();
        ListView.ItemRenderer renderer = listView.getItemRenderer();

        int itemHeight = renderer.getPreferredHeight(-1);
        int itemIndex = y / itemHeight;

        if (itemIndex < listData.getLength()
            && !listView.isItemDisabled(itemIndex)) {
            ListView.SelectMode selectMode = listView.getSelectMode();
            if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)
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
            } else if (Keyboard.isPressed(Keyboard.Modifier.CTRL)
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
    public boolean mouseWheel(Component component, Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        if (highlightedIndex != -1) {
            Bounds itemBounds = getItemBounds(highlightedIndex);
            repaintComponent(itemBounds.x, itemBounds.y, itemBounds.width, itemBounds.height);
        }

        highlightedIndex = -1;

        return super.mouseWheel(component, scrollType, scrollAmount, wheelRotation, x, y);
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

        ListView listView = (ListView)getComponent();

        switch (keyCode) {
            case Keyboard.KeyCode.UP: {
                int index = listView.getFirstSelectedIndex();

                do {
                    index--;
                } while (index >= 0
                    && listView.isItemDisabled(index));

                if (index >= 0) {
                    if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)
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
                    if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)
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

    public Vote previewItemDisabledChange(ListView listView, int index) {
        return Vote.APPROVE;
    }

    public void itemDisabledChangeVetoed(ListView listView, int index, Vote reason) {
        // No-op
    }

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
