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
package org.apache.pivot.tutorials;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Comparator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Vote;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.DragSource;
import org.apache.pivot.wtk.DropAction;
import org.apache.pivot.wtk.DropTarget;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.LocalManifest;
import org.apache.pivot.wtk.Manifest;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Rollup;
import org.apache.pivot.wtk.RollupStateListener;
import org.apache.pivot.wtk.Slider;
import org.apache.pivot.wtk.SliderValueListener;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewHeader;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.Visual;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.CalendarDateSpinnerData;
import org.apache.pivot.wtk.content.ListItem;
import org.apache.pivot.wtk.content.NumericSpinnerData;
import org.apache.pivot.wtk.content.TableRow;
import org.apache.pivot.wtk.content.TableViewHeaderData;
import org.apache.pivot.wtk.content.TreeBranch;
import org.apache.pivot.wtk.content.TreeNode;
import org.apache.pivot.wtk.effects.ReflectionDecorator;
import org.apache.pivot.wtk.effects.WatermarkDecorator;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.terra.TerraTheme;
import org.apache.pivot.wtk.text.Document;
import org.apache.pivot.wtk.text.PlainTextSerializer;
import org.apache.pivot.wtkx.WTKXSerializer;

public class KitchenSink implements Application, Application.AboutHandler {
    private abstract class RollupStateHandler
        implements RollupStateListener {
        @Override
        public void expandedChangeVetoed(Rollup rollup, Vote reason) {
            // No-op
        }

        @Override
        public void expandedChanged(Rollup rollup) {
            // No-op
        }
    }

    private class ButtonsRollupStateHandler extends RollupStateHandler {
        private Component component = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                try {
                    component = (Component)wtkxSerializer.readObject(this, "buttons.wtkx");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                rollup.setContent(component);
            }

            return Vote.APPROVE;
        }
    }

    private class ListsRollupStateHandler extends RollupStateHandler {
        private Component component = null;
        private ListView editableListView = null;
        private ListView iconListView = null;
        private ListView checkedListView = null;
        private ListButton iconListButton = null;

        @SuppressWarnings("unchecked")
        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                try {
                    component = (Component)wtkxSerializer.readObject(this, "lists.wtkx");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                editableListView = (ListView)wtkxSerializer.get("editableListView");
                iconListView = (ListView)wtkxSerializer.get("iconListView");
                checkedListView = (ListView)wtkxSerializer.get("checkedListView");
                iconListButton = (ListButton)wtkxSerializer.get("iconListButton");

                rollup.setContent(component);

                List<ListItem> listData = (List<ListItem>)editableListView.getListData();
                listData.setComparator(new Comparator<ListItem>() {
                    @Override
                    public int compare(ListItem listItem1, ListItem listItem2) {
                        String text1 = listItem1.getText();
                        String text2 = listItem2.getText();
                        return text1.compareToIgnoreCase(text2);
                    }
                });


                Filter<ListItem> disabledItemFilter = new Filter<ListItem>() {
                    @Override
                    public boolean include(ListItem listItem) {
                        return Character.toLowerCase(listItem.getText().charAt(0)) == 'c';
                    }
                };

                iconListView.setDisabledItemFilter(disabledItemFilter);
                iconListButton.setDisabledItemFilter(disabledItemFilter);

                checkedListView.setItemChecked(0, true);
                checkedListView.setItemChecked(2, true);
                checkedListView.setItemChecked(3, true);
            }

            return Vote.APPROVE;
        }
    }

    private class TextRollupStateHandler extends RollupStateHandler {
        private Component component = null;
        private TextArea textArea = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                try {
                    component = (Component)wtkxSerializer.readObject(this, "text.wtkx");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                textArea = (TextArea)wtkxSerializer.get("textArea");
                rollup.setContent(component);

                PlainTextSerializer plainTextSerializer = new PlainTextSerializer("UTF-8");
                InputStream inputStream = getClass().getResourceAsStream("text_area.txt");

                Document document = null;
                try {
                    document = plainTextSerializer.readObject(inputStream);
                } catch(Exception exception) {
                    System.err.println(exception);
                }

                textArea.setDocument(document);

                final WatermarkDecorator watermarkDecorator = new WatermarkDecorator("Preview");
                watermarkDecorator.setOpacity(0.1f);
                watermarkDecorator.setFont(watermarkDecorator.getFont().deriveFont(Font.BOLD, 24));

                textArea.getDecorators().add(watermarkDecorator);
            }

            return Vote.APPROVE;
        }
    }

    private class CalendarsRollupStateHandler extends RollupStateHandler {
        private Component component = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                try {
                    component = (Component)wtkxSerializer.readObject(this, "calendars.wtkx");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                rollup.setContent(component);
            }

            return Vote.APPROVE;
        }
    }

    private class NavigationRollupStateHandler extends RollupStateHandler {
        private Component component = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                try {
                    component = (Component)wtkxSerializer.readObject(this, "navigation.wtkx");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                rollup.setContent(component);
            }

            return Vote.APPROVE;
        }
    }

    private class SplittersRollupStateHandler extends RollupStateHandler {
        private Component component = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                try {
                    component = (Component)wtkxSerializer.readObject(this, "splitters.wtkx");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                rollup.setContent(component);
            }

            return Vote.APPROVE;
        }
    }

    private class MenusRollupStateHandler extends RollupStateHandler {
        private Component component = null;
        private ImageView menuImageView = null;
        private Menu.Item helpAboutMenuItem = null;

        private Menu.Section menuSection = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                Action.getNamedActions().put("selectImageAction", new Action() {
                    @Override
                    public String getDescription() {
                        return "Select Image Action";
                    }

                    @Override
                    public void perform() {
                        Button.Group imageMenuGroup = Button.getNamedGroups().get("imageMenuGroup");
                        Button selectedItem = imageMenuGroup.getSelection();

                        String imageName = (String)selectedItem.getUserData().get("image");
                        URL imageURL = getClass().getResource(imageName);

                        // If the image has not been added to the resource cache yet,
                        // add it
                        Image image = (Image)ApplicationContext.getResourceCache().get(imageURL);

                        if (image == null) {
                            try {
                                image = Image.load(imageURL);
                            } catch (TaskExecutionException exception) {
                                throw new RuntimeException(exception);
                            }

                            ApplicationContext.getResourceCache().put(imageURL, image);
                        }

                        // Update the image
                        menuImageView.setImage(image);
                    }
                });

                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                try {
                    component = (Component)wtkxSerializer.readObject(this, "menus.wtkx");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                menuImageView = (ImageView)wtkxSerializer.get("menuImageView");
                helpAboutMenuItem  = (Menu.Item)wtkxSerializer.get("menuBar.helpAboutMenuItem");

                rollup.setContent(component);

                try {
                    menuSection = (Menu.Section)wtkxSerializer.readObject(this, "menu_section.wtkx");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                menuImageView.setMenuHandler(new MenuHandler.Adapter() {
                    @Override
                    public boolean configureContextMenu(Component component, Menu menu, int x, int y) {
                        menu.getSections().add(menuSection);
                        return false;
                    }
                });

                helpAboutMenuItem.getButtonPressListeners().add(new ButtonPressListener() {
                    @Override
                    public void buttonPressed(Button button) {
                        aboutRequested();
                    }
                });
            }

            return Vote.APPROVE;
        }
    }

    private class MetersRollupStateHandler extends RollupStateHandler {
        private Component component = null;
        private ActivityIndicator activityIndicator1 = null;
        private ActivityIndicator activityIndicator2 = null;
        private ActivityIndicator activityIndicator3 = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                try {
                    component = (Component)wtkxSerializer.readObject(this, "meters.wtkx");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                activityIndicator1 = (ActivityIndicator)wtkxSerializer.get("activityIndicator1");
                activityIndicator2 = (ActivityIndicator)wtkxSerializer.get("activityIndicator2");
                activityIndicator3 = (ActivityIndicator)wtkxSerializer.get("activityIndicator3");

                rollup.setContent(component);

                metersRollup.getRollupStateListeners().add(new RollupStateListener() {
                    @Override
                    public Vote previewExpandedChange(Rollup rollup) {
                        return Vote.APPROVE;
                    }

                    @Override
                    public void expandedChangeVetoed(Rollup rollup, Vote reason) {
                        // No-op
                    }

                    @Override
                    public void expandedChanged(Rollup rollup) {
                        activityIndicator1.setActive(rollup.isExpanded());
                        activityIndicator2.setActive(rollup.isExpanded());
                        activityIndicator3.setActive(rollup.isExpanded());
                    }
                });
}

            return Vote.APPROVE;
        }
    }

    private class SpinnersRollupStateHandler extends RollupStateHandler {
        private Component component = null;

        private Spinner numericSpinner = null;
        private Spinner dateSpinner = null;

        private Slider redSlider = null;
        private Slider greenSlider = null;
        private Slider blueSlider = null;
        private Border colorBorder = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                try {
                    component = (Component)wtkxSerializer.readObject(this, "spinners.wtkx");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                numericSpinner = (Spinner)wtkxSerializer.get("numericSpinner");
                dateSpinner = (Spinner)wtkxSerializer.get("dateSpinner");

                redSlider = (Slider)wtkxSerializer.get("redSlider");
                greenSlider = (Slider)wtkxSerializer.get("greenSlider");
                blueSlider = (Slider)wtkxSerializer.get("blueSlider");
                colorBorder = (Border)wtkxSerializer.get("colorBorder");

                rollup.setContent(component);

                initializeNumericSpinner(numericSpinner);
                initializeDateSpinner(dateSpinner);

                SliderValueListener sliderValueListener = new SliderValueListener() {
                    @Override
                    public void valueChanged(Slider slider, int previousValue) {
                        Color color = new Color(redSlider.getValue(), greenSlider.getValue(),
                            blueSlider.getValue());
                        colorBorder.getStyles().put("backgroundColor", color);
                    }
                };

                redSlider.getSliderValueListeners().add(sliderValueListener);
                greenSlider.getSliderValueListeners().add(sliderValueListener);
                blueSlider.getSliderValueListeners().add(sliderValueListener);

                Color color = new Color(redSlider.getValue(), greenSlider.getValue(),
                    blueSlider.getValue());
                colorBorder.getStyles().put("backgroundColor", color);
            }

            return Vote.APPROVE;
        }

        private void initializeNumericSpinner(Spinner numericSpinner) {
            NumericSpinnerData numericSpinnerData = new NumericSpinnerData(0, 256, 4);
            numericSpinner.setSpinnerData(numericSpinnerData);
            numericSpinner.setSelectedIndex(0);
        }

        private void initializeDateSpinner(Spinner dateSpinner) {
            CalendarDate lowerBound = new CalendarDate(2008, 0, 0);
            CalendarDate upperBound = new CalendarDate(2019, 11, 30);
            CalendarDateSpinnerData spinnerData = new CalendarDateSpinnerData(lowerBound, upperBound);

            CalendarDate today = new CalendarDate();
            dateSpinner.setSpinnerData(spinnerData);
            dateSpinner.setSelectedItem(today);
        }
    }

    private class TablesRollupStateHandler extends RollupStateHandler {
        private Component component = null;
        private TableView sortableTableView = null;
        private TableView customTableView = null;
        private TableViewHeader sortableTableViewHeader = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                try {
                    component = (Component)wtkxSerializer.readObject(this, "tables.wtkx");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                sortableTableView = (TableView)wtkxSerializer.get("sortableTableView");
                customTableView = (TableView)wtkxSerializer.get("customTableView");
                sortableTableViewHeader = (TableViewHeader)wtkxSerializer.get("sortableTableViewHeader");

                rollup.setContent(component);

                // Set table header data
                TableView.ColumnSequence columns = sortableTableView.getColumns();
                columns.get(0).setHeaderData(new TableViewHeaderData("#"));
                columns.get(1).setHeaderData(new TableViewHeaderData("A"));
                columns.get(2).setHeaderData(new TableViewHeaderData("B"));
                columns.get(3).setHeaderData(new TableViewHeaderData("C"));
                columns.get(4).setHeaderData(new TableViewHeaderData("D"));

                // Populate table
                ArrayList<Object> tableData = new ArrayList<Object>(10000);

                for (int i = 0, n = tableData.getCapacity(); i < n; i++) {
                    TableRow tableRow = new TableRow();

                    tableRow.put("i", i);
                    tableRow.put("a", (int)Math.round(Math.random() * 10));
                    tableRow.put("b", (int)Math.round(Math.random() * 100));
                    tableRow.put("c", (int)Math.round(Math.random() * 1000));
                    tableRow.put("d", (int)Math.round(Math.random() * 10000));

                    tableData.add(tableRow);
                }

                sortableTableView.setTableData(tableData);

                // Install header press listener
                sortableTableViewHeader.getTableViewHeaderPressListeners().add(new TableView.SortHandler());

                customTableView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
                       if (button == Mouse.Button.LEFT) {
                           List<CustomTableRow> customTableData =
                               (List<CustomTableRow>)customTableView.getTableData();

                          int columnIndex = customTableView.getColumnAt(x);
                          if (columnIndex == 0) {
                             int rowIndex = customTableView.getRowAt(y);
                             CustomTableRow row = customTableData.get(rowIndex);

                             row.setA(!row.getA());
                             customTableData.update(rowIndex, row);
                          }
                       }

                       return false;
                    }
                });
            }

            return Vote.APPROVE;
        }
    }

    private class TreesRollupStateHandler extends RollupStateHandler {
        private Component component = null;
        private TreeView editableTreeView = null;
        private TreeView checkTreeView = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                try {
                    component = (Component)wtkxSerializer.readObject(this, "trees.wtkx");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                editableTreeView = (TreeView)wtkxSerializer.get("editableTreeView");
                checkTreeView = (TreeView)wtkxSerializer.get("checkTreeView");

                rollup.setContent(component);

                TreeBranch treeData = (TreeBranch)editableTreeView.getTreeData();
                treeData.setComparator(new TreeNodeComparator());

                checkTreeView.setDisabledNodeFilter(new Filter<TreeNode>() {
                    @Override
                    public boolean include(TreeNode treeNode) {
                        boolean include = false;

                        if (!(treeNode instanceof TreeBranch)) {
                            String text = treeNode.getText();

                            if (text != null) {
                                char firstCharacter = Character.toLowerCase(text.charAt(0));
                                include = (firstCharacter % 2 == 0);
                            }
                        }

                        return include;
                    }
                });
            }

            return Vote.APPROVE;
        }
    }

    private class DragDropRollupStateHandler extends RollupStateHandler {
        private Component component = null;
        private ImageView imageView1 = null;
        private ImageView imageView2 = null;
        private ImageView imageView3 = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                try {
                    component = (Component)wtkxSerializer.readObject(this, "dragdrop.wtkx");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                imageView1 = (ImageView)wtkxSerializer.get("imageView1");
                imageView2 = (ImageView)wtkxSerializer.get("imageView2");
                imageView3 = (ImageView)wtkxSerializer.get("imageView3");

                rollup.setContent(component);

                DragSource imageDragSource = new DragSource() {
                    private Image image = null;
                    private Point offset = null;
                    private LocalManifest content = null;

                    @Override
                    public boolean beginDrag(Component component, int x, int y) {
                        ImageView imageView = (ImageView)component;
                        image = imageView.getImage();

                        if (image != null) {
                            imageView.setImage((Image)null);
                            content = new LocalManifest();
                            content.putImage(image);
                            offset = new Point(x - (imageView.getWidth() - image.getWidth()) / 2,
                                y - (imageView.getHeight() - image.getHeight()) / 2);
                        }

                        return (image != null);
                    }

                    @Override
                    public void endDrag(Component component, DropAction dropAction) {
                        if (dropAction == null) {
                            ImageView imageView = (ImageView)component;
                            imageView.setImage(image);
                        }

                        image = null;
                        offset = null;
                        content = null;
                    }

                    @Override
                    public boolean isNative() {
                        return false;
                    }

                    @Override
                    public LocalManifest getContent() {
                        return content;
                    }

                    @Override
                    public Visual getRepresentation() {
                        return image;
                    }

                    @Override
                    public Point getOffset() {
                        return offset;
                    }

                    @Override
                    public int getSupportedDropActions() {
                        return DropAction.MOVE.getMask();
                    }
                };

                DropTarget imageDropTarget = new DropTarget() {
                    @Override
                    public DropAction dragEnter(Component component, Manifest dragContent,
                        int supportedDropActions, DropAction userDropAction) {
                        DropAction dropAction = null;

                        ImageView imageView = (ImageView)component;
                        if (imageView.getImage() == null
                            && dragContent.containsImage()
                            && DropAction.MOVE.isSelected(supportedDropActions)) {
                            dropAction = DropAction.MOVE;
                            component.getStyles().put("backgroundColor", "#f0e68c");
                        }

                        return dropAction;
                    }

                    @Override
                    public void dragExit(Component component) {
                        component.getStyles().put("backgroundColor", null);
                    }

                    @Override
                    public DropAction dragMove(Component component, Manifest dragContent,
                        int supportedDropActions, int x, int y, DropAction userDropAction) {
                        return (dragContent.containsImage() ? DropAction.MOVE : null);
                    }

                    @Override
                    public DropAction userDropActionChange(Component component, Manifest dragContent,
                        int supportedDropActions, int x, int y, DropAction userDropAction) {
                        return (dragContent.containsImage() ? DropAction.MOVE : null);
                    }

                    @Override
                    public DropAction drop(Component component, Manifest dragContent,
                        int supportedDropActions, int x, int y, DropAction userDropAction) {
                        DropAction dropAction = null;

                        if (dragContent.containsImage()) {
                            ImageView imageView = (ImageView)component;
                            try {
                                imageView.setImage(dragContent.getImage());
                                dropAction = DropAction.MOVE;
                            } catch(IOException exception) {
                                System.err.println(exception);
                            }
                        }

                        dragExit(component);

                        return dropAction;
                    }
                };

                imageView1.setDragSource(imageDragSource);
                imageView1.setDropTarget(imageDropTarget);

                imageView2.setDragSource(imageDragSource);
                imageView2.setDropTarget(imageDropTarget);

                imageView3.setDragSource(imageDragSource);
                imageView3.setDropTarget(imageDropTarget);
            }

            return Vote.APPROVE;
        }
    }

    private class AlertsRollupStateHandler extends RollupStateHandler {
        private Component component = null;
        private PushButton alertButton = null;
        private PushButton promptButton = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                try {
                    component = (Component)wtkxSerializer.readObject(this, "alerts.wtkx");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                alertButton = (PushButton)wtkxSerializer.get("alertButton");
                promptButton = (PushButton)wtkxSerializer.get("promptButton");

                rollup.setContent(component);

                alertButton.getButtonPressListeners().add(new ButtonPressListener() {
                    @Override
                    public void buttonPressed(Button button) {
                        Button.Group messageTypeGroup = Button.getNamedGroups().get("messageType");
                        Button selection = messageTypeGroup.getSelection();

                        Map<String, ?> userData;
                        try {
                            userData = JSONSerializer.parseMap((String)selection.getUserData().get("messageInfo"));
                        } catch (SerializationException exception) {
                            throw new RuntimeException(exception);
                        }

                        String messageType = (String)userData.get("messageType");

                        if (messageType == null) {
                            ArrayList<String> options = new ArrayList<String>();
                            options.add("OK");
                            options.add("Cancel");

                            Component body = null;
                            WTKXSerializer wtkxSerializer = new WTKXSerializer();
                            try {
                                body = (Component)wtkxSerializer.readObject(this, "alert.wtkx");
                            } catch(Exception exception) {
                                System.err.println(exception);
                            }

                            Alert alert = new Alert(MessageType.QUESTION, "Please select your favorite icon:",
                                options, body);
                            alert.setTitle("Select Icon");
                            alert.setSelectedOption(0);
                            alert.getDecorators().update(0, new ReflectionDecorator());

                            alert.open(window);
                        } else {
                            String message = (String)userData.get("message");
                            Alert.alert(MessageType.valueOf(messageType.toUpperCase()), message, window);
                        }
                    }
                });

                promptButton.getButtonPressListeners().add(new ButtonPressListener() {
                    @Override
                    public void buttonPressed(Button button) {
                        Button.Group messageTypeGroup = Button.getNamedGroups().get("messageType");
                        Button selection = messageTypeGroup.getSelection();

                        Map<String, ?> userData;
                        try {
                            userData = JSONSerializer.parseMap((String)selection.getUserData().get("messageInfo"));
                        } catch (SerializationException exception) {
                            throw new RuntimeException(exception);
                        }

                        String messageType = (String)userData.get("messageType");

                        if (messageType == null) {
                            ArrayList<String> options = new ArrayList<String>();
                            options.add("OK");
                            options.add("Cancel");

                            Component body = null;
                            WTKXSerializer wtkxSerializer = new WTKXSerializer();
                            try {
                                body = (Component)wtkxSerializer.readObject(this, "alert.wtkx");
                            } catch(Exception exception) {
                                System.err.println(exception);
                            }

                            Prompt prompt = new Prompt(MessageType.QUESTION, "Please select your favorite icon:",
                                options, body);
                            prompt.setTitle("Select Icon");
                            prompt.setSelectedOption(0);
                            prompt.getDecorators().update(0, new ReflectionDecorator());

                            prompt.open(window);
                        } else {
                            String message = (String)userData.get("message");
                            Prompt.prompt(MessageType.valueOf(messageType.toUpperCase()), message, window);
                        }
                    }
                });
            }

            return Vote.APPROVE;
        }
    }

    private Window window = null;
    private Rollup buttonsRollup;
    private Rollup listsRollup;
    private Rollup textRollup;
    private Rollup calendarsRollup;
    private Rollup navigationRollup;
    private Rollup splittersRollup;
    private Rollup menusRollup;
    private Rollup metersRollup;
    private Rollup spinnersRollup;
    private Rollup tablesRollup;
    private Rollup treesRollup;
    private Rollup dragDropRollup;
    private Rollup alertsRollup;

    public static void main(String[] args) {
        DesktopApplicationContext.main(KitchenSink.class, args);
    }

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        String terraColors = properties.get("terraColors");
        try {
            if (terraColors != null) {
                Theme.setTheme(new TerraTheme(terraColors));
            }
        } catch (Exception exception) {
            System.err.println("Unable to load custom colors from \"" + terraColors
                + "\": " + exception.getMessage());
        }

        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "kitchen_sink.wtkx");
        wtkxSerializer.bind(this, KitchenSink.class);

        buttonsRollup = (Rollup)wtkxSerializer.get("buttonsRollup");
        buttonsRollup.getRollupStateListeners().add(new ButtonsRollupStateHandler());

        listsRollup = (Rollup)wtkxSerializer.get("listsRollup");
        listsRollup.getRollupStateListeners().add(new ListsRollupStateHandler());

        textRollup = (Rollup)wtkxSerializer.get("textRollup");
        textRollup.getRollupStateListeners().add(new TextRollupStateHandler());

        calendarsRollup = (Rollup)wtkxSerializer.get("calendarsRollup");
        calendarsRollup.getRollupStateListeners().add(new CalendarsRollupStateHandler());

        navigationRollup = (Rollup)wtkxSerializer.get("navigationRollup");
        navigationRollup.getRollupStateListeners().add(new NavigationRollupStateHandler());

        splittersRollup = (Rollup)wtkxSerializer.get("splittersRollup");
        splittersRollup.getRollupStateListeners().add(new SplittersRollupStateHandler());

        menusRollup = (Rollup)wtkxSerializer.get("menusRollup");
        menusRollup.getRollupStateListeners().add(new MenusRollupStateHandler());

        metersRollup = (Rollup)wtkxSerializer.get("metersRollup");
        metersRollup.getRollupStateListeners().add(new MetersRollupStateHandler());

        spinnersRollup = (Rollup)wtkxSerializer.get("spinnersRollup");
        spinnersRollup.getRollupStateListeners().add(new SpinnersRollupStateHandler());

        tablesRollup = (Rollup)wtkxSerializer.get("tablesRollup");
        tablesRollup.getRollupStateListeners().add(new TablesRollupStateHandler());

        treesRollup = (Rollup)wtkxSerializer.get("treesRollup");
        treesRollup.getRollupStateListeners().add(new TreesRollupStateHandler());

        dragDropRollup = (Rollup)wtkxSerializer.get("dragDropRollup");
        dragDropRollup.getRollupStateListeners().add(new DragDropRollupStateHandler());

        alertsRollup = (Rollup)wtkxSerializer.get("alertsRollup");
        alertsRollup.getRollupStateListeners().add(new AlertsRollupStateHandler());

        window.open(display);

        // Start with the "Buttons" rollup expanded
        ApplicationContext.scheduleCallback(new Runnable() {
            @Override
            public void run() {
                buttonsRollup.setExpanded(true);
            }
        }, 0);
    }

    @Override
    public boolean shutdown(boolean optional) throws Exception {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void aboutRequested() {
        String about = "Origin: " + ApplicationContext.getOrigin()
            + "; JVM version: " + ApplicationContext.getJVMVersion();

        Prompt.prompt(about, window);
    }
}
