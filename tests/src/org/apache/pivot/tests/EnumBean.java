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

package org.apache.pivot.tests;

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.util.Vote;
import org.apache.pivot.web.Query;
import org.apache.pivot.wtk.BindType;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.DropAction;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.SortDirection;
import org.apache.pivot.wtk.SplitPane;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewHeader;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextDecoration;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.media.BufferedImageSerializer;
import org.apache.pivot.wtk.media.Picture;
import org.apache.pivot.wtk.skin.CardPaneSkin;
import org.apache.pivot.wtk.text.BulletedList;
import org.apache.pivot.wtk.text.NumberedList;

/**
 * Simple bean for testing String to enum coercion.
 * {@link BeanAdapter#coerce(Object, Class)}. <p> All accessors were created
 * using Eclipse's 'Generate Getters & Setters" source generation. No additional
 * code has been added to them, so they can safely be deleted and regenerated if
 * required.
 */
public class EnumBean {

    // Public non-static field for testing BeanAdapter#get("orientationField");
    public Orientation orientationField;

    private BindType bindType;
    private BufferedImageSerializer.Format bufferedImageSerializerFormat;
    private BulletedList.Style bulletedListStyle;
    private Button.State buttonState;
    private CardPaneSkin.SelectionChangeEffect selectionChangeEffect;
    private Cursor cursor;
    private DropAction dropAction;
    private FileBrowserSheet.Mode fileBrowserSheetMode;
    private FocusTraversalDirection focusTraversalDirection;
    private GraphicsUtilities.PaintType paintType;
    private HorizontalAlignment horizontalAlignment;
    private ImageView.ImageBindMapping.Type imageBindMappingType;
    private Keyboard.KeyLocation keyLocation;
    private Keyboard.Modifier modifier;
    private ListView.SelectMode listViewSelectMode;
    private MessageBusTest.TestMessage testMessage;
    private MessageType messageType;
    private Mouse.Button mouseButton;
    private Mouse.ScrollType mouseScrollType;
    private NumberedList.Style numberedListStyle;
    private Orientation orientation;
    private Picture.Interpolation interpolation;
    private Query.Method queryMethod;
    private ScrollPane.Corner.Placement placement;
    private ScrollPane.ScrollBarPolicy scrollBarPolicy;
    private SortDirection sortDirection;
    private SplitPane.Region splitPaneRegion;
    private SplitPane.ResizeMode splitPaneResizeMode;
    private TableView.SelectMode tableViewSelectMode;
    private TableViewHeader.SortMode sortMode;
    private TextDecoration textDecoration;
    private TextArea.ScrollDirection scrollDirection;
    private TreeView.NodeCheckState nodeCheckState;
    private TreeView.SelectMode treeViewSelectMode;
    private VerticalAlignment verticalAlignment;
    private Vote vote;

    public BindType getBindType() {
        return bindType;
    }

    public void setBindType(BindType bindType) {
        this.bindType = bindType;
    }

    public BufferedImageSerializer.Format getBufferedImageSerializerFormat() {
        return bufferedImageSerializerFormat;
    }

    public void setBufferedImageSerializerFormat(
        BufferedImageSerializer.Format bufferedImageSerializerFormat) {
        this.bufferedImageSerializerFormat = bufferedImageSerializerFormat;
    }

    public BulletedList.Style getBulletedListStyle() {
        return bulletedListStyle;
    }

    public void setBulletedListStyle(BulletedList.Style bulletedListStyle) {
        this.bulletedListStyle = bulletedListStyle;
    }

    public Button.State getButtonState() {
        return buttonState;
    }

    public void setButtonState(Button.State buttonState) {
        this.buttonState = buttonState;
    }

    public CardPaneSkin.SelectionChangeEffect getSelectionChangeEffect() {
        return selectionChangeEffect;
    }

    public void setSelectionChangeEffect(CardPaneSkin.SelectionChangeEffect selectionChangeEffect) {
        this.selectionChangeEffect = selectionChangeEffect;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    public DropAction getDropAction() {
        return dropAction;
    }

    public void setDropAction(DropAction dropAction) {
        this.dropAction = dropAction;
    }

    public FileBrowserSheet.Mode getFileBrowserSheetMode() {
        return fileBrowserSheetMode;
    }

    public void setFileBrowserSheetMode(FileBrowserSheet.Mode fileBrowserSheetMode) {
        this.fileBrowserSheetMode = fileBrowserSheetMode;
    }

    public FocusTraversalDirection getFocusTraversalDirection() {
        return focusTraversalDirection;
    }

    public void setFocusTraversalDirection(FocusTraversalDirection focusTraversalDirection) {
        this.focusTraversalDirection = focusTraversalDirection;
    }

    public GraphicsUtilities.PaintType getPaintType() {
        return paintType;
    }

    public void setPaintType(GraphicsUtilities.PaintType paintType) {
        this.paintType = paintType;
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    public ImageView.ImageBindMapping.Type getImageBindMappingType() {
        return imageBindMappingType;
    }

    public void setImageBindMappingType(ImageView.ImageBindMapping.Type imageBindMappingType) {
        this.imageBindMappingType = imageBindMappingType;
    }

    public Keyboard.KeyLocation getKeyLocation() {
        return keyLocation;
    }

    public void setKeyLocation(Keyboard.KeyLocation keyLocation) {
        this.keyLocation = keyLocation;
    }

    public Keyboard.Modifier getModifier() {
        return modifier;
    }

    public void setModifier(Keyboard.Modifier modifier) {
        this.modifier = modifier;
    }

    public ListView.SelectMode getListViewSelectMode() {
        return listViewSelectMode;
    }

    public void setListViewSelectMode(ListView.SelectMode listViewSelectMode) {
        this.listViewSelectMode = listViewSelectMode;
    }

    public MessageBusTest.TestMessage getTestMessage() {
        return testMessage;
    }

    public void setTestMessage(MessageBusTest.TestMessage testMessage) {
        this.testMessage = testMessage;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Mouse.Button getMouseButton() {
        return mouseButton;
    }

    public void setMouseButton(Mouse.Button mouseButton) {
        this.mouseButton = mouseButton;
    }

    public Mouse.ScrollType getMouseScrollType() {
        return mouseScrollType;
    }

    public void setMouseScrollType(Mouse.ScrollType mouseScrollType) {
        this.mouseScrollType = mouseScrollType;
    }

    public NumberedList.Style getNumberedListStyle() {
        return numberedListStyle;
    }

    public void setNumberedListStyle(NumberedList.Style numberedListStyle) {
        this.numberedListStyle = numberedListStyle;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public Picture.Interpolation getInterpolation() {
        return interpolation;
    }

    public void setInterpolation(Picture.Interpolation interpolation) {
        this.interpolation = interpolation;
    }

    public Query.Method getQueryMethod() {
        return queryMethod;
    }

    public void setQueryMethod(Query.Method queryMethod) {
        this.queryMethod = queryMethod;
    }

    public ScrollPane.Corner.Placement getPlacement() {
        return placement;
    }

    public void setPlacement(ScrollPane.Corner.Placement placement) {
        this.placement = placement;
    }

    public ScrollPane.ScrollBarPolicy getScrollBarPolicy() {
        return scrollBarPolicy;
    }

    public void setScrollBarPolicy(ScrollPane.ScrollBarPolicy scrollBarPolicy) {
        this.scrollBarPolicy = scrollBarPolicy;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(SortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }

    public SplitPane.Region getSplitPaneRegion() {
        return splitPaneRegion;
    }

    public void setSplitPaneRegion(SplitPane.Region splitPaneRegion) {
        this.splitPaneRegion = splitPaneRegion;
    }

    public SplitPane.ResizeMode getSplitPaneResizeMode() {
        return splitPaneResizeMode;
    }

    public void setSplitPaneResizeMode(SplitPane.ResizeMode splitPaneResizeMode) {
        this.splitPaneResizeMode = splitPaneResizeMode;
    }

    public TableView.SelectMode getTableViewSelectMode() {
        return tableViewSelectMode;
    }

    public void setTableViewSelectMode(TableView.SelectMode tableViewSelectMode) {
        this.tableViewSelectMode = tableViewSelectMode;
    }

    public TableViewHeader.SortMode getSortMode() {
        return sortMode;
    }

    public void setSortMode(TableViewHeader.SortMode sortMode) {
        this.sortMode = sortMode;
    }

    public TextDecoration getTextDecoration() {
        return textDecoration;
    }

    public void setTextDecoration(TextDecoration textDecoration) {
        this.textDecoration = textDecoration;
    }

    public TextArea.ScrollDirection getScrollDirection() {
        return scrollDirection;
    }

    public void setScrollDirection(TextArea.ScrollDirection scrollDirection) {
        this.scrollDirection = scrollDirection;
    }

    public TreeView.NodeCheckState getNodeCheckState() {
        return nodeCheckState;
    }

    public void setNodeCheckState(TreeView.NodeCheckState nodeCheckState) {
        this.nodeCheckState = nodeCheckState;
    }

    public TreeView.SelectMode getTreeViewSelectMode() {
        return treeViewSelectMode;
    }

    public void setTreeViewSelectMode(TreeView.SelectMode treeViewSelectMode) {
        this.treeViewSelectMode = treeViewSelectMode;
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public Vote getVote() {
        return vote;
    }

    public void setVote(Vote vote) {
        this.vote = vote;
    }
}
