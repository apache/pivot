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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Transparency;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.Keyboard.Modifier;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListView.SelectMode;
import org.apache.pivot.wtk.ListViewItemListener;
import org.apache.pivot.wtk.ListViewItemStateListener;
import org.apache.pivot.wtk.ListViewListener;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.ComponentSkin;

/**
 * List view skin.
 */
public class TerraListViewSkin extends ComponentSkin implements ListView.Skin, ListViewListener,
    ListViewItemListener, ListViewItemStateListener, ListViewSelectionListener {
    private Font font;
    private Color color;
    private Color disabledColor;
    private Color backgroundColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color inactiveSelectionColor;
    private Color inactiveSelectionBackgroundColor;
    private Color highlightBackgroundColor;
    private Color alternateItemBackgroundColor;
    private boolean showHighlight;
    private boolean wrapSelectNext;
    private boolean variableItemHeight;
    private Insets checkboxPadding = new Insets(2, 2, 2, 0);

    private int highlightIndex = -1;
    private int selectIndex = -1;

    private ArrayList<Integer> itemBoundaries = null;
    private int fixedItemHeight;

    private boolean validateSelection = false;

    private static final Checkbox CHECKBOX = new Checkbox();

    static {
        CHECKBOX.setSize(CHECKBOX.getPreferredSize());
    }

    public TerraListViewSkin() {
        Theme theme = currentTheme();
        font = theme.getFont();
        color = theme.getColor(1);
        disabledColor = theme.getColor(7);
        backgroundColor = theme.getColor(4);
        selectionColor = theme.getColor(4);
        selectionBackgroundColor = theme.getColor(14);
        inactiveSelectionColor = theme.getColor(1);
        inactiveSelectionBackgroundColor = theme.getColor(9);
        highlightBackgroundColor = theme.getColor(10);
        alternateItemBackgroundColor = null;
        showHighlight = true;
        wrapSelectNext = true;
    }

    @Override
    public void install(final Component component) {
        super.install(component);

        ListView listView = (ListView) component;
        listView.getListViewListeners().add(this);
        listView.getListViewItemListeners().add(this);
        listView.getListViewItemStateListeners().add(this);
        listView.getListViewSelectionListeners().add(this);
    }

    @Override
    public int getPreferredWidth(final int height) {
        int preferredWidth = 0;

        ListView listView = (ListView) getComponent();
        @SuppressWarnings("unchecked")
        List<Object> listData = (List<Object>) listView.getListData();

        ListView.ItemRenderer itemRenderer = listView.getItemRenderer();

        int index = 0;
        for (Object item : listData) {
            itemRenderer.render(item, index++, listView, false, Button.State.UNSELECTED, false, false);
            preferredWidth = Math.max(preferredWidth, itemRenderer.getPreferredWidth(-1));
        }

        if (listView.getCheckmarksEnabled()) {
            preferredWidth += CHECKBOX.getWidth() + checkboxPadding.getWidth();
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(final int width) {
        int preferredHeight = 0;

        ListView listView = (ListView) getComponent();
        @SuppressWarnings("unchecked")
        List<Object> listData = (List<Object>) listView.getListData();
        ListView.ItemRenderer itemRenderer = listView.getItemRenderer();

        if (variableItemHeight) {
            int clientWidth = width;
            if (listView.getCheckmarksEnabled()) {
                clientWidth = Math.max(clientWidth
                    - (CHECKBOX.getWidth() + checkboxPadding.getWidth()), 0);
            }

            int index = 0;
            for (Object item : listData) {
                itemRenderer.render(item, index++, listView, false, Button.State.UNSELECTED, false, false);
                preferredHeight += itemRenderer.getPreferredHeight(clientWidth);
            }
        } else {
            itemRenderer.render(null, -1, listView, false, Button.State.UNSELECTED, false, false);

            int fixedItemHeightLocal = itemRenderer.getPreferredHeight(-1);
            if (listView.getCheckmarksEnabled()) {
                fixedItemHeightLocal = Math.max(CHECKBOX.getHeight()
                    + checkboxPadding.getHeight(), fixedItemHeightLocal);
            }

            preferredHeight = listData.getLength() * fixedItemHeightLocal;
        }

        return preferredHeight;
    }

    @Override
    public int getBaseline(final int width, final int height) {
        ListView listView = (ListView) getComponent();

        int baseline = -1;

        int clientWidth = width;
        if (listView.getCheckmarksEnabled()) {
            clientWidth = Math.max(clientWidth
                - (CHECKBOX.getWidth() + checkboxPadding.getWidth()), 0);
        }

        ListView.ItemRenderer itemRenderer = listView.getItemRenderer();
        @SuppressWarnings("unchecked")
        List<Object> listData = (List<Object>) listView.getListData();
        if (variableItemHeight && listData.getLength() > 0) {
            itemRenderer.render(listData.get(0), 0, listView, false, Button.State.UNSELECTED, false, false);
            int itemHeight = itemRenderer.getPreferredHeight(clientWidth);
            if (listView.getCheckmarksEnabled()) {
                itemHeight = Math.max(CHECKBOX.getHeight()
                    + checkboxPadding.getHeight(), itemHeight);
            }

            baseline = itemRenderer.getBaseline(clientWidth, itemHeight);
        } else {
            itemRenderer.render(null, -1, listView, false, Button.State.UNSELECTED, false, false);

            int fixedItemHeightLocal = itemRenderer.getPreferredHeight(-1);
            if (listView.getCheckmarksEnabled()) {
                fixedItemHeightLocal = Math.max(CHECKBOX.getHeight()
                    + checkboxPadding.getHeight(), fixedItemHeightLocal);
            }

            baseline = itemRenderer.getBaseline(clientWidth, fixedItemHeightLocal);
        }

        return baseline;
    }

    @Override
    public void layout() {
        ListView listView = (ListView) getComponent();
        @SuppressWarnings("unchecked")
        List<Object> listData = (List<Object>) listView.getListData();
        ListView.ItemRenderer itemRenderer = listView.getItemRenderer();

        if (variableItemHeight) {
            int width = getWidth();

            int checkboxHeight = 0;
            if (listView.getCheckmarksEnabled()) {
                checkboxHeight = CHECKBOX.getHeight() + checkboxPadding.getHeight();
            }

            int n = listData.getLength();
            itemBoundaries = new ArrayList<>(n);

            int itemY = 0;
            for (int i = 0; i < n; i++) {
                Object item = listData.get(i);

                int itemWidth = width;
                int itemX = 0;

                Button.State state = Button.State.UNSELECTED;
                if (listView.getCheckmarksEnabled()) {
                    if (listView.getAllowTriStateCheckmarks()) {
                        state = listView.getItemCheckmarkState(i);
                    } else {
                        state = listView.isItemChecked(i) ? Button.State.SELECTED : Button.State.UNSELECTED;
                    }
                    itemX = CHECKBOX.getWidth() + checkboxPadding.getWidth();
                    itemWidth -= itemX;
                }

                itemRenderer.render(item, i, listView, false, state, false, false);
                int itemHeight = itemRenderer.getPreferredHeight(itemWidth);

                if (listView.getCheckmarksEnabled()) {
                    itemHeight = Math.max(itemHeight, checkboxHeight);
                }

                itemY += itemHeight;
                itemBoundaries.add(itemY);
            }
        } else {
            itemRenderer.render(null, -1, listView, false, Button.State.UNSELECTED, false, false);
            fixedItemHeight = itemRenderer.getPreferredHeight(-1);

            if (listView.getCheckmarksEnabled()) {
                fixedItemHeight = Math.max(CHECKBOX.getHeight()
                    + checkboxPadding.getHeight(), fixedItemHeight);
            }
        }

        if (validateSelection) {
            // Ensure that the selection is visible
            Sequence<Span> selectedRanges = listView.getSelectedRanges();

            if (selectedRanges.getLength() > 0) {
                int rangeStart = selectedRanges.get(0).start;
                int rangeEnd = selectedRanges.get(selectedRanges.getLength() - 1).end;

                Bounds selectionBounds = getItemBounds(rangeStart);
                selectionBounds = selectionBounds.union(getItemBounds(rangeEnd));

                Bounds visibleSelectionBounds = listView.getVisibleArea(selectionBounds);
                if (visibleSelectionBounds != null
                    && visibleSelectionBounds.height < selectionBounds.height) {
                    listView.scrollAreaToVisible(selectionBounds);
                }
            }
        }

        validateSelection = false;
    }

    @Override
    public void paint(final Graphics2D graphics) {
        ListView listView = (ListView) getComponent();
        @SuppressWarnings("unchecked")
        List<Object> listData = (List<Object>) listView.getListData();
        ListView.ItemRenderer itemRenderer = listView.getItemRenderer();

        int width = getWidth();
        int height = getHeight();

        // Paint the background
        if (backgroundColor != null) {
            graphics.setPaint(backgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        // Paint the list contents
        int itemStart = 0;
        int itemEnd = listData.getLength() - 1;

        // Ensure that we only paint items that are visible
        Rectangle clipBounds = graphics.getClipBounds();
        if (clipBounds != null) {
            if (variableItemHeight) {
                itemStart = getItemAt(clipBounds.y);
                if (itemStart == -1) {
                    itemStart = listData.getLength();
                }

                if (itemEnd != -1) {
                    int clipBottom = clipBounds.y + clipBounds.height - 1;
                    clipBottom = Math.min(clipBottom, itemBoundaries.get(itemEnd) - 1);
                    itemEnd = getItemAt(clipBottom);
                }
            } else {
                itemStart = Math.max(itemStart,
                    (int) Math.floor(clipBounds.y / (double) fixedItemHeight));
                itemEnd = Math.min(
                    itemEnd,
                    (int) Math.ceil((clipBounds.y + clipBounds.height) / (double) fixedItemHeight) - 1);
            }
        }

        // Paint the item background
        if (alternateItemBackgroundColor != null) {
            for (int itemIndex = itemStart; itemIndex <= itemEnd; itemIndex++) {
                int itemY = getItemY(itemIndex);
                int rowHeight = getItemHeight(itemIndex);
                if (itemIndex % 2 > 0) {
                    graphics.setPaint(alternateItemBackgroundColor);
                    graphics.fillRect(0, itemY, width, rowHeight + 1);
                }
            }
        }

        // Paint the item content
        for (int itemIndex = itemStart; itemIndex <= itemEnd; itemIndex++) {
            Object item = listData.get(itemIndex);
            boolean highlighted = (itemIndex == highlightIndex && listView.getSelectMode() != SelectMode.NONE);
            boolean selected = listView.isItemSelected(itemIndex);
            boolean disabled = listView.isItemDisabled(itemIndex);
            int itemY = getItemY(itemIndex);
            int itemHeight = getItemHeight(itemIndex);

            Color itemBackgroundColor = null;
            if (selected) {
                itemBackgroundColor = (listView.isFocused()) ? this.selectionBackgroundColor
                    : inactiveSelectionBackgroundColor;
            } else {
                if (highlighted && showHighlight && !disabled) {
                    itemBackgroundColor = highlightBackgroundColor;
                }
            }

            if (itemBackgroundColor != null) {
                graphics.setPaint(itemBackgroundColor);
                graphics.fillRect(0, itemY, width, itemHeight);
            }

            int itemX = 0;
            int itemWidth = width;

            Button.State state = Button.State.UNSELECTED;
            if (listView.getCheckmarksEnabled()) {
                if (listView.getAllowTriStateCheckmarks()) {
                    state = listView.getItemCheckmarkState(itemIndex);
                } else {
                    state = listView.isItemChecked(itemIndex) ? Button.State.SELECTED : Button.State.UNSELECTED;
                }

                int checkboxY = (itemHeight - CHECKBOX.getHeight()) / 2;
                Graphics2D checkboxGraphics = (Graphics2D) graphics.create(checkboxPadding.left,
                    itemY + checkboxY, CHECKBOX.getWidth(), CHECKBOX.getHeight());

                CHECKBOX.setEnabled(!disabled && !listView.isCheckmarkDisabled(itemIndex));
                if (listView.getAllowTriStateCheckmarks()) {
                    CHECKBOX.setTriState(true);
                    CHECKBOX.setState(state);
                } else {
                    CHECKBOX.setTriState(false);
                    CHECKBOX.setSelected(state == Button.State.SELECTED);
                }
                CHECKBOX.paint(checkboxGraphics);
                checkboxGraphics.dispose();

                itemX = CHECKBOX.getWidth() + checkboxPadding.getWidth();

                itemWidth -= itemX;
            }

            // Paint the data
            Graphics2D rendererGraphics = (Graphics2D) graphics.create(itemX, itemY, itemWidth,
                itemHeight);

            itemRenderer.render(item, itemIndex, listView, selected, state, highlighted, disabled);
            itemRenderer.setSize(itemWidth, itemHeight);
            itemRenderer.paint(rendererGraphics);
            rendererGraphics.dispose();

            itemY += itemHeight;
        }
    }

    // List view skin methods
    @Override
    public int getItemAt(final int y) {
        Utils.checkNonNegative(y, "y");

        ListView listView = (ListView) getComponent();

        int index;
        if (variableItemHeight) {
            if (y == 0) {
                index = 0;
            } else {
                index = ArrayList.binarySearch(itemBoundaries, y);
                if (index < 0) {
                    index = -(index + 1);
                }
            }
        } else {
            index = (y / fixedItemHeight);

            @SuppressWarnings("unchecked")
            List<Object> listData = (List<Object>) listView.getListData();
            if (index >= listData.getLength()) {
                index = -1;
            }
        }

        return index;
    }

    @Override
    public Bounds getItemBounds(final int index) {
        return new Bounds(0, getItemY(index), getWidth(), getItemHeight(index));
    }

    @Override
    public int getItemIndent() {
        int itemIndent = 0;

        ListView listView = (ListView) getComponent();
        if (listView.getCheckmarksEnabled()) {
            itemIndent = CHECKBOX.getWidth() + checkboxPadding.getWidth();
        }

        return itemIndent;
    }

    private int getItemY(final int index) {
        int itemY;

        if (variableItemHeight) {
            if (index == 0) {
                itemY = 0;
            } else {
                itemY = itemBoundaries.get(index - 1);
            }
        } else {
            itemY = index * fixedItemHeight;
        }

        return itemY;
    }

    private int getItemHeight(final int index) {
        int itemHeight;

        if (variableItemHeight) {
            itemHeight = itemBoundaries.get(index);

            if (index > 0) {
                itemHeight -= itemBoundaries.get(index - 1);
            }
        } else {
            itemHeight = fixedItemHeight;
        }

        return itemHeight;
    }

    @Override
    public final boolean isFocusable() {
        ListView listView = (ListView) getComponent();
        return (listView.getSelectMode() != SelectMode.NONE);
    }

    @Override
    public final boolean isOpaque() {
        return (backgroundColor != null && backgroundColor.getTransparency() == Transparency.OPAQUE);
    }

    public final Font getFont() {
        return font;
    }

    public final void setFont(final Font font) {
        Utils.checkNull(font, "font");

        this.font = font;
        invalidateComponent();
    }

    public final void setFont(final String font) {
        setFont(decodeFont(font));
    }

    public final void setFont(final Dictionary<String, ?> font) {
        setFont(Theme.deriveFont(font));
    }

    public final Color getColor() {
        return color;
    }

    public final void setColor(final Color color) {
        Utils.checkNull(color, "color");

        this.color = color;
        repaintComponent();
    }

    public final void setColor(final String color) {
        setColor(GraphicsUtilities.decodeColor(color, "color"));
    }

    public final void setColor(final int color) {
        Theme theme = currentTheme();
        setColor(theme.getColor(color));
    }

    public final Color getDisabledColor() {
        return disabledColor;
    }

    public final void setDisabledColor(final Color disabledColor) {
        Utils.checkNull(disabledColor, "disabledColor");

        this.disabledColor = disabledColor;
        repaintComponent();
    }

    public final void setDisabledColor(final String disabledColor) {
        setDisabledColor(GraphicsUtilities.decodeColor(disabledColor, "disabledColor"));
    }

    public final void setDisabledColor(final int disabledColor) {
        Theme theme = currentTheme();
        setDisabledColor(theme.getColor(disabledColor));
    }

    public final Color getBackgroundColor() {
        return backgroundColor;
    }

    public final void setBackgroundColor(final Color backgroundColor) {
        // We allow a null background color here
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public final void setBackgroundColor(final String backgroundColor) {
        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor, "backgroundColor"));
    }

    public final void setBackgroundColor(final int backgroundColor) {
        Theme theme = currentTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
    }

    public final Color getSelectionColor() {
        return selectionColor;
    }

    public final void setSelectionColor(final Color selectionColor) {
        Utils.checkNull(selectionColor, "selectionColor");

        this.selectionColor = selectionColor;
        repaintComponent();
    }

    public final void setSelectionColor(final String selectionColor) {
        setSelectionColor(GraphicsUtilities.decodeColor(selectionColor, "selectionColor"));
    }

    public final void setSelectionColor(final int selectionColor) {
        Theme theme = currentTheme();
        setSelectionColor(theme.getColor(selectionColor));
    }

    public final Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    public final void setSelectionBackgroundColor(final Color selectionBackgroundColor) {
        Utils.checkNull(selectionBackgroundColor, "selectionBackgroundColor");

        this.selectionBackgroundColor = selectionBackgroundColor;
        repaintComponent();
    }

    public final void setSelectionBackgroundColor(final String selectionBackgroundColor) {
        setSelectionBackgroundColor(GraphicsUtilities.decodeColor(selectionBackgroundColor,
            "selectionBackgroundColor"));
    }

    public final void setSelectionBackgroundColor(final int selectionBackgroundColor) {
        Theme theme = currentTheme();
        setSelectionBackgroundColor(theme.getColor(selectionBackgroundColor));
    }

    public final Color getInactiveSelectionColor() {
        return inactiveSelectionColor;
    }

    public final void setInactiveSelectionColor(final Color inactiveSelectionColor) {
        Utils.checkNull(inactiveSelectionColor, "inactiveSelectionColor");

        this.inactiveSelectionColor = inactiveSelectionColor;
        repaintComponent();
    }

    public final void setInactiveSelectionColor(final String inactiveSelectionColor) {
        setInactiveSelectionColor(GraphicsUtilities.decodeColor(inactiveSelectionColor,
            "inactiveSelectionColor"));
    }

    public final void setInactiveSelectionColor(final int inactiveSelectionColor) {
        Theme theme = currentTheme();
        setInactiveSelectionColor(theme.getColor(inactiveSelectionColor));
    }

    public final Color getInactiveSelectionBackgroundColor() {
        return inactiveSelectionBackgroundColor;
    }

    public final void setInactiveSelectionBackgroundColor(final Color inactiveSelectionBackgroundColor) {
        Utils.checkNull(inactiveSelectionBackgroundColor, "inactiveSelectionBackgroundColor");

        this.inactiveSelectionBackgroundColor = inactiveSelectionBackgroundColor;
        repaintComponent();
    }

    public final void setInactiveSelectionBackgroundColor(final String inactiveSelectionBackgroundColor) {
        setInactiveSelectionBackgroundColor(GraphicsUtilities.decodeColor(inactiveSelectionBackgroundColor,
            "inactiveSelectionBackgroundColor"));
    }

    public final void setInactiveSelectionBackgroundColor(final int inactiveSelectionBackgroundColor) {
        Theme theme = currentTheme();
        setInactiveSelectionBackgroundColor(theme.getColor(inactiveSelectionBackgroundColor));
    }

    public final Color getHighlightBackgroundColor() {
        return highlightBackgroundColor;
    }

    public final void setHighlightBackgroundColor(final Color highlightBackgroundColor) {
        Utils.checkNull(highlightBackgroundColor, "highlightBackgroundColor");

        this.highlightBackgroundColor = highlightBackgroundColor;
        repaintComponent();
    }

    public final void setHighlightBackgroundColor(final String highlightBackgroundColor) {
        setHighlightBackgroundColor(GraphicsUtilities.decodeColor(highlightBackgroundColor,
            "highlightBackgroundColor"));
    }

    public final void setHighlightBackgroundColor(final int highlightBackgroundColor) {
        Theme theme = currentTheme();
        setHighlightBackgroundColor(theme.getColor(highlightBackgroundColor));
    }

    public final Color getAlternateItemBackgroundColor() {
        return alternateItemBackgroundColor;
    }

    public final void setAlternateItemBackgroundColor(final Color alternateItemBackgroundColor) {
        Utils.checkNull(alternateItemBackgroundColor, "alternateItemBackgroundColor");

        this.alternateItemBackgroundColor = alternateItemBackgroundColor;
        repaintComponent();
    }

    public final void setAlternateItemBackgroundColor(final String alternateItemBackgroundColor) {
        setAlternateItemBackgroundColor(GraphicsUtilities.decodeColor(alternateItemBackgroundColor,
            "alternateItemBackgroundColor"));
    }

    public final void setAlternateItemColor(final int alternateItemBackgroundColor) {
        Theme theme = currentTheme();
        setAlternateItemBackgroundColor(theme.getColor(alternateItemBackgroundColor));
    }

    public final boolean getShowHighlight() {
        return showHighlight;
    }

    public final void setShowHighlight(final boolean showHighlight) {
        this.showHighlight = showHighlight;
        repaintComponent();
    }

    public final boolean getWrapSelectNext() {
        return wrapSelectNext;
    }

    public final void setWrapSelectNext(final boolean wrapSelectNext) {
        this.wrapSelectNext = wrapSelectNext;
    }

    public final Insets getCheckboxPadding() {
        return checkboxPadding;
    }

    public final void setCheckboxPadding(final Insets checkboxPadding) {
        Utils.checkNull(checkboxPadding, "checkboxPadding");

        this.checkboxPadding = checkboxPadding;
        invalidateComponent();
    }

    public final void setCheckboxPadding(final Dictionary<String, ?> checkboxPadding) {
        setCheckboxPadding(new Insets(checkboxPadding));
    }

    public final void setCheckboxPadding(final int checkboxPadding) {
        setCheckboxPadding(new Insets(checkboxPadding));
    }

    public final void setCheckboxPadding(final Number padding) {
        setCheckboxPadding(new Insets(padding));
    }

    public final void setCheckboxPadding(final String checkboxPadding) {
        setCheckboxPadding(Insets.decode(checkboxPadding));
    }

    public final boolean isVariableItemHeight() {
        return variableItemHeight;
    }

    public final void setVariableItemHeight(final boolean variableItemHeight) {
        this.variableItemHeight = variableItemHeight;
        invalidateComponent();
    }

    @Override
    public boolean mouseMove(final Component component, final int x, final int y) {
        boolean consumed = super.mouseMove(component, x, y);

        ListView listView = (ListView) getComponent();

        int previousHighlightIndex = this.highlightIndex;
        highlightIndex = getItemAt(y);

        if (previousHighlightIndex != highlightIndex
            && listView.getSelectMode() != SelectMode.NONE && showHighlight) {
            if (previousHighlightIndex != -1) {
                repaintComponent(getItemBounds(previousHighlightIndex));
            }

            if (highlightIndex != -1) {
                repaintComponent(getItemBounds(highlightIndex));
            }
        }

        return consumed;
    }

    @Override
    public void mouseOut(final Component component) {
        super.mouseOut(component);

        ListView listView = (ListView) getComponent();

        if (highlightIndex != -1 && listView.getSelectMode() != SelectMode.NONE
            && showHighlight) {
            Bounds itemBounds = getItemBounds(highlightIndex);
            repaintComponent(itemBounds.x, itemBounds.y, itemBounds.width, itemBounds.height);
        }

        highlightIndex = -1;
        selectIndex = -1;
    }

    @Override
    public boolean mouseDown(final Component component, final Mouse.Button button, final int x, final int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        ListView listView = (ListView) getComponent();
        int itemIndex = getItemAt(y);

        if (itemIndex != -1 && !listView.isItemDisabled(itemIndex)) {
            if (!listView.getCheckmarksEnabled() || listView.isCheckmarkDisabled(itemIndex)
                || !getCheckboxBounds(itemIndex).contains(x, y)) {
                SelectMode selectMode = listView.getSelectMode();

                if (button == Mouse.Button.LEFT) {
                    Modifier commandModifier = Platform.getCommandModifier();

                    if (Keyboard.isPressed(Modifier.SHIFT)
                        && selectMode == SelectMode.MULTI) {
                        Filter<?> disabledItemFilter = listView.getDisabledItemFilter();

                        if (disabledItemFilter == null) {
                            // Select the range
                            ArrayList<Span> selectedRanges = new ArrayList<>();
                            int startIndex = listView.getFirstSelectedIndex();
                            int endIndex = listView.getLastSelectedIndex();

                            Span selectedRange = (itemIndex > startIndex) ? new Span(startIndex,
                                itemIndex) : new Span(itemIndex, endIndex);
                            selectedRanges.add(selectedRange);

                            listView.setSelectedRanges(selectedRanges);
                        }
                    } else if (Keyboard.isPressed(commandModifier)
                        && selectMode == SelectMode.MULTI) {
                        // Toggle the item's selection state
                        if (listView.isItemSelected(itemIndex)) {
                            listView.removeSelectedIndex(itemIndex);
                        } else {
                            listView.addSelectedIndex(itemIndex);
                        }
                    } else if (Keyboard.isPressed(commandModifier)
                        && selectMode == SelectMode.SINGLE) {
                        // Toggle the item's selection state
                        if (listView.isItemSelected(itemIndex)) {
                            listView.setSelectedIndex(-1);
                        } else {
                            listView.setSelectedIndex(itemIndex);
                        }
                    } else {
                        if (selectMode != SelectMode.NONE) {
                            if (listView.isItemSelected(itemIndex)) {
                                selectIndex = itemIndex;
                            } else {
                                listView.setSelectedIndex(itemIndex);
                            }
                        }
                    }
                }
            }
        }

        listView.requestFocus();

        return consumed;
    }

    @Override
    public boolean mouseUp(final Component component, final Mouse.Button button, final int x, final int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        ListView listView = (ListView) getComponent();
        if (selectIndex != -1
            && listView.getFirstSelectedIndex() != listView.getLastSelectedIndex()) {
            listView.setSelectedIndex(selectIndex);
            selectIndex = -1;
        }

        return consumed;
    }

    @Override
    public boolean mouseClick(final Component component, final Mouse.Button button, final int x, final int y,
        final int count) {
        boolean consumed = super.mouseClick(component, button, x, y, count);

        ListView listView = (ListView) getComponent();
        int itemIndex = getItemAt(y);

        if (itemIndex != -1 && !listView.isItemDisabled(itemIndex)) {
            if (listView.getCheckmarksEnabled() && !listView.isCheckmarkDisabled(itemIndex)
                && getCheckboxBounds(itemIndex).contains(x, y)) {
                if (listView.getAllowTriStateCheckmarks()) {
                    Button.State currentState = listView.getItemCheckmarkState(itemIndex);
                    Button.State nextState = currentState;
                    switch (currentState) {
                        case UNSELECTED:
                            if (listView.getCheckmarksMixedAsChecked()) {
                                nextState = Button.State.SELECTED;
                            } else {
                                nextState = Button.State.MIXED;
                            }
                            break;
                        case MIXED:
                            nextState = Button.State.SELECTED;
                            break;
                        case SELECTED:
                            nextState = Button.State.UNSELECTED;
                            break;
                        default:
                            break;
                    }
                    listView.setItemCheckmarkState(itemIndex, nextState);
                } else {
                    listView.setItemChecked(itemIndex, !listView.isItemChecked(itemIndex));
                }
            } else {
                if (selectIndex != -1 && count == 1 && button == Mouse.Button.LEFT) {
                    ListView.ItemEditor itemEditor = listView.getItemEditor();

                    if (itemEditor != null) {
                        if (itemEditor.isEditing()) {
                            itemEditor.endEdit(true);
                        }

                        itemEditor.beginEdit(listView, selectIndex);
                    }
                }
            }
        }

        selectIndex = -1;

        return consumed;
    }

    private Bounds getCheckboxBounds(final int itemIndex) {
        Bounds itemBounds = getItemBounds(itemIndex);

        int checkboxHeight = CHECKBOX.getHeight();
        return new Bounds(checkboxPadding.left, itemBounds.y + (itemBounds.height - checkboxHeight)
            / 2, CHECKBOX.getWidth(), checkboxHeight);
    }

    @Override
    public boolean mouseWheel(final Component component, final Mouse.ScrollType scrollType, final int scrollAmount,
        final int wheelRotation, final int x, final int y) {
        ListView listView = (ListView) getComponent();

        if (highlightIndex != -1) {
            Bounds itemBounds = getItemBounds(highlightIndex);

            highlightIndex = -1;

            if (listView.getSelectMode() != SelectMode.NONE && showHighlight) {
                repaintComponent(itemBounds.x, itemBounds.y, itemBounds.width, itemBounds.height,
                    true);
            }
        }

        return super.mouseWheel(component, scrollType, scrollAmount, wheelRotation, x, y);
    }

    /**
     * Keyboard handling (arrow keys with modifiers).
     * <ul>
     * <li>{@link KeyCode#UP UP} Selects the previous enabled list item when select
     * mode is not {@link SelectMode#NONE}</li>
     * <li>{@link KeyCode#DOWN DOWN} Selects the next enabled list item when select
     * mode is not {@link SelectMode#NONE}</li>
     * <li>{@link Modifier#SHIFT SHIFT} + {@link KeyCode#UP UP} Increases the selection
     * size by including the previous enabled list item when select mode is
     * {@link SelectMode#MULTI}</li>
     * <li>{@link Modifier#SHIFT SHIFT} + {@link KeyCode#DOWN DOWN} Increases
     * the selection size by including the next enabled list item when select
     * mode is {@link SelectMode#MULTI}</li>
     * </ul>
     */
    @Override
    public boolean keyPressed(final Component component, final int keyCode, final KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

        ListView listView = (ListView) getComponent();
        SelectMode selectMode = listView.getSelectMode();

        switch (keyCode) {
            case KeyCode.UP:
                if (selectMode != SelectMode.NONE) {
                    int index = listView.getFirstSelectedIndex();
                    int count = listView.getListData().getLength();

                    do {
                        index--;
                    } while (index >= 0 && listView.isItemDisabled(index));

                    if (index >= 0) {
                        if (Keyboard.isPressed(Modifier.SHIFT)
                            && listView.getSelectMode() == SelectMode.MULTI) {
                            listView.addSelectedIndex(index);
                        } else {
                            listView.setSelectedIndex(index);
                        }
                    } else if (!Keyboard.isPressed(Modifier.SHIFT)
                        && listView.getSelectMode() == SelectMode.MULTI
                        && count == listView.getSelectedItems().getLength()) {
                        index = listView.getLastSelectedIndex();
                        do {
                            index--;
                        } while (index >= 0 && listView.isItemDisabled(index));

                        listView.setSelectedIndex(Math.max(0, index));
                    }

                    consumed = true;
                }

                break;

            case KeyCode.DOWN:
                if (selectMode != SelectMode.NONE) {
                    int index = listView.getLastSelectedIndex();
                    int count = listView.getListData().getLength();

                    do {
                        index++;
                    } while (index < count && listView.isItemDisabled(index));

                    if (index < count) {
                        if (Keyboard.isPressed(Modifier.SHIFT)
                            && listView.getSelectMode() == SelectMode.MULTI) {
                            listView.addSelectedIndex(index);
                        } else {
                            listView.setSelectedIndex(index);
                        }
                    } else if (!Keyboard.isPressed(Modifier.SHIFT)
                        && listView.getSelectMode() == SelectMode.MULTI
                        && count == listView.getSelectedItems().getLength()) {
                        index = 0;
                        do {
                            index++;
                        } while (index < count && listView.isItemDisabled(index));

                        listView.setSelectedIndex(Math.min(count - 1, index));
                    }

                    consumed = true;
                }

                break;

            default:
                break;
        }

        // Clear the highlight
        if (highlightIndex != -1 && listView.getSelectMode() != SelectMode.NONE
            && showHighlight && consumed) {
            repaintComponent(getItemBounds(highlightIndex));
        }

        highlightIndex = -1;

        return consumed;
    }

    /**
     * {@link KeyCode#SPACE SPACE} Toggles check mark selection when select mode
     * is {@link SelectMode#SINGLE}.
     */
    @Override
    public boolean keyReleased(final Component component, final int keyCode, final KeyLocation keyLocation) {
        boolean consumed = super.keyReleased(component, keyCode, keyLocation);

        ListView listView = (ListView) getComponent();

        switch (keyCode) {
            case KeyCode.SPACE:
                if (listView.getCheckmarksEnabled()
                    && listView.getSelectMode() == SelectMode.SINGLE) {
                    int selectedIndex = listView.getSelectedIndex();

                    if (!listView.isCheckmarkDisabled(selectedIndex)) {
                        listView.setItemChecked(selectedIndex,
                            !listView.isItemChecked(selectedIndex));
                        consumed = true;
                    }
                }

                break;

            default:
                break;
        }

        return consumed;
    }

    /**
     * Select the next enabled list item where the first character of the
     * rendered text matches the typed key (case insensitive).
     */
    @Override
    public boolean keyTyped(final Component component, final char character) {
        boolean consumed = super.keyTyped(component, character);

        ListView listView = (ListView) getComponent();
        List<?> listData = listView.getListData();
        ListView.ItemRenderer itemRenderer = listView.getItemRenderer();

        char characterUpper = Character.toUpperCase(character);

        for (int i = listView.getLastSelectedIndex() + 1, n = listData.getLength(); i < n; i++) {
            if (!listView.isItemDisabled(i)) {
                String string = itemRenderer.toString(listData.get(i));

                if (string != null && string.length() > 0) {
                    char first = Character.toUpperCase(string.charAt(0));

                    if (first == characterUpper) {
                        listView.setSelectedIndex(i);
                        consumed = true;
                        break;
                    }
                }
            }
        }

        if (!consumed && wrapSelectNext) {
            for (int i = 0, n = listData.getLength(); i < n; i++) {
                if (!listView.isItemDisabled(i)) {
                    String string = itemRenderer.toString(listData.get(i));

                    if (string != null && string.length() > 0) {
                        char first = Character.toUpperCase(string.charAt(0));

                        if (first == characterUpper) {
                            listView.setSelectedIndex(i);
                            consumed = true;
                            break;
                        }
                    }
                }
            }
        }

        return consumed;
    }

    // Component state events
    @Override
    public void enabledChanged(final Component component) {
        super.enabledChanged(component);

        repaintComponent();
    }

    @Override
    public void focusedChanged(final Component component, final Component obverseComponent) {
        super.focusedChanged(component, obverseComponent);

        repaintComponent();
    }

    // List view events
    @Override
    public void listDataChanged(final ListView listView, final List<?> previousListData) {
        highlightIndex = -1;
        invalidateComponent();
    }

    @Override
    public void itemRendererChanged(final ListView listView, final ListView.ItemRenderer previousItemRenderer) {
        invalidateComponent();
    }

    @Override
    public void itemEditorChanged(final ListView listView, final ListView.ItemEditor previousItemEditor) {
        // No-op
    }

    @Override
    public void selectModeChanged(final ListView listView, final SelectMode previousSelectMode) {
        repaintComponent();
    }

    @Override
    public void checkmarksEnabledChanged(final ListView listView) {
        invalidateComponent();
    }

    @Override
    public void checkmarksTriStateChanged(final ListView listView) {
        repaintComponent();
    }

    @Override
    public void checkmarksMixedAsCheckedChanged(final ListView listView) {
        repaintComponent();
    }

    @Override
    public void disabledItemFilterChanged(final ListView listView, final Filter<?> previousDisabledItemFilter) {
        repaintComponent();
    }

    @Override
    public void disabledCheckmarkFilterChanged(final ListView listView,
        final Filter<?> previousDisabledCheckmarkFilter) {
        repaintComponent();
    }

    // List view item events
    @Override
    public void itemInserted(final ListView listView, final int index) {
        invalidateComponent();
    }

    @Override
    public void itemsRemoved(final ListView listView, final int index, final int count) {
        if (highlightIndex >= index) {
            highlightIndex = -1;
        }
        invalidateComponent();
    }

    @Override
    public void itemUpdated(final ListView listView, final int index) {
        invalidateComponent();
    }

    @Override
    public void itemsCleared(final ListView listView) {
        highlightIndex = -1;
        invalidateComponent();
    }

    @Override
    public void itemsSorted(final ListView listView) {
        if (variableItemHeight) {
            invalidateComponent();
        } else {
            repaintComponent();
        }
    }

    // List view item state events
    @Override
    public void itemCheckedChanged(final ListView listView, final int index) {
        repaintComponent(getItemBounds(index));
    }

    @Override
    public void itemCheckedStateChanged(final ListView listView, final int index) {
        repaintComponent(getItemBounds(index));
    }

    // List view selection detail events
    @Override
    public void selectedRangeAdded(final ListView listView, final int rangeStart, final int rangeEnd) {
        if (listView.isValid()) {
            Bounds selectionBounds = getItemBounds(rangeStart);
            selectionBounds = selectionBounds.union(getItemBounds(rangeEnd));
            repaintComponent(selectionBounds);

            // Ensure that the selection is visible
            Bounds visibleSelectionBounds = listView.getVisibleArea(selectionBounds);
            if (visibleSelectionBounds.height < selectionBounds.height) {
                listView.scrollAreaToVisible(selectionBounds);
            }
        } else {
            validateSelection = true;
        }
    }

    @Override
    public void selectedRangeRemoved(final ListView listView, final int rangeStart, final int rangeEnd) {
        // Repaint the area containing the removed selection
        if (listView.isValid()) {
            Bounds selectionBounds = getItemBounds(rangeStart);
            selectionBounds = selectionBounds.union(getItemBounds(rangeEnd));
            repaintComponent(selectionBounds);
        }
    }

    @Override
    public void selectedRangesChanged(final ListView listView, final Sequence<Span> previousSelectedRanges) {
        if (previousSelectedRanges != null
            && previousSelectedRanges != listView.getSelectedRanges()) {
            if (listView.isValid()) {
                // Repaint the area occupied by the previous selection
                if (previousSelectedRanges.getLength() > 0) {
                    int rangeStart = previousSelectedRanges.get(0).start;
                    int rangeEnd = previousSelectedRanges.get(previousSelectedRanges.getLength() - 1).end;

                    Bounds previousSelectionBounds = getItemBounds(rangeStart);
                    previousSelectionBounds = previousSelectionBounds.union(getItemBounds(rangeEnd));
                    repaintComponent(previousSelectionBounds);
                }

                // Repaint the area occupied by the current selection
                Sequence<Span> selectedRanges = listView.getSelectedRanges();
                if (selectedRanges.getLength() > 0) {
                    int rangeStart = selectedRanges.get(0).start;
                    int rangeEnd = selectedRanges.get(selectedRanges.getLength() - 1).end;

                    Bounds selectionBounds = getItemBounds(rangeStart);
                    selectionBounds = selectionBounds.union(getItemBounds(rangeEnd));
                    repaintComponent(selectionBounds);

                    // Ensure that the selection is visible
                    Bounds visibleSelectionBounds = listView.getVisibleArea(selectionBounds);
                    if (visibleSelectionBounds != null
                        && visibleSelectionBounds.height < selectionBounds.height) {
                        // Repainting the entire component is a workaround for PIVOT-490
                        repaintComponent();

                        listView.scrollAreaToVisible(selectionBounds);
                    }
                }
            } else {
                validateSelection = true;
            }
        }
    }

    @Override
    public void selectedItemChanged(final ListView listView, final Object previousSelectedItem) {
        // No-op
    }

}
