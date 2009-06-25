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
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.DragSource;
import org.apache.pivot.wtk.DropAction;
import org.apache.pivot.wtk.DropTarget;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.LocalManifest;
import org.apache.pivot.wtk.Manifest;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuPopup;
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
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.Visual;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.CalendarDateSpinnerData;
import org.apache.pivot.wtk.content.ListItem;
import org.apache.pivot.wtk.content.NumericSpinnerData;
import org.apache.pivot.wtk.content.TableRow;
import org.apache.pivot.wtk.content.TableViewHeaderData;
import org.apache.pivot.wtk.content.TreeBranch;
import org.apache.pivot.wtk.effects.ReflectionDecorator;
import org.apache.pivot.wtk.effects.WatermarkDecorator;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.text.Document;
import org.apache.pivot.wtk.text.PlainTextSerializer;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

public class KitchenSink implements Application, Application.About {
    private abstract class RollupStateHandler
        implements RollupStateListener {
        public void expandedChangeVetoed(Rollup rollup, Vote reason) {
            // No-op
        }

        public void expandedChanged(Rollup rollup) {
            // No-op
        }
    }

    private class ButtonsRollupStateHandler extends RollupStateHandler {
        private Component component = null;

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

        @WTKX private ListView editableListView;
        @WTKX private ListView iconListView;
        @WTKX private ListView checkedListView;

        @SuppressWarnings("unchecked")
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

                rollup.setContent(component);
                wtkxSerializer.bind(this, ListsRollupStateHandler.class);

                List<ListItem> listData = (List<ListItem>)editableListView.getListData();
                listData.setComparator(new Comparator<ListItem>() {
                    public int compare(ListItem listItem1, ListItem listItem2) {
                        String text1 = listItem1.getText();
                        String text2 = listItem2.getText();
                        return text1.compareToIgnoreCase(text2);
                    }
                });

                iconListView.setItemDisabled(3, true);
                iconListView.setItemDisabled(4, true);

                checkedListView.setItemChecked(0, true);
                checkedListView.setItemChecked(2, true);
                checkedListView.setItemChecked(3, true);
            }

            return Vote.APPROVE;
        }
    }

    private class TextRollupStateHandler extends RollupStateHandler {
        private Component component = null;

        @WTKX private TextArea textArea;

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

                rollup.setContent(component);
                wtkxSerializer.bind(this, TextRollupStateHandler.class);

                PlainTextSerializer plainTextSerializer = new PlainTextSerializer("UTF-8");
                InputStream inputStream = getClass().getResourceAsStream("text_area.txt");

                Document document = null;
                try {
                    document = plainTextSerializer.readObject(inputStream);
                } catch(Exception exception) {
                    System.out.println(exception);
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

        @WTKX private ImageView menuImageView;
        @WTKX(id="menuBar.helpAboutMenuItem") private Menu.Item helpAboutMenuItem;

        private MenuPopup menuPopup = null;

        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                new Action("selectImageAction") {
                    public String getDescription() {
                        return "Select Image Action";
                    }

                    public void perform() {
                        Button.Group imageMenuGroup = Button.getGroup("imageMenuGroup");
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
                };

                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                try {
                    component = (Component)wtkxSerializer.readObject(this, "menus.wtkx");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                rollup.setContent(component);
                wtkxSerializer.bind(this, MenusRollupStateHandler.class);

                try {
                    menuPopup = (MenuPopup)wtkxSerializer.readObject(this, "menu_popup.wtkx");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                menuImageView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter() {
                    @Override
                    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
                        if (button == Mouse.Button.RIGHT
                            || (button == Mouse.Button.LEFT
                                && Keyboard.isPressed(Keyboard.Modifier.CTRL))) {
                            menuPopup.open(window, component.mapPointToAncestor(component.getDisplay(), x, y));
                        }

                        return false;
                    }
                });

                helpAboutMenuItem.getButtonPressListeners().add(new ButtonPressListener() {
                    public void buttonPressed(Button button) {
                        handleAbout();
                    }
                });
            }

            return Vote.APPROVE;
        }
    }

    private class MetersRollupStateHandler extends RollupStateHandler {
        private Component component = null;

        @WTKX private ActivityIndicator activityIndicator1;
        @WTKX private ActivityIndicator activityIndicator2;
        @WTKX private ActivityIndicator activityIndicator3;

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

                rollup.setContent(component);
                wtkxSerializer.bind(this, MetersRollupStateHandler.class);

                metersRollup.getRollupStateListeners().add(new RollupStateListener() {
                    public Vote previewExpandedChange(Rollup rollup) {
                        return Vote.APPROVE;
                    }

                    public void expandedChangeVetoed(Rollup rollup, Vote reason) {
                        // No-op
                    }

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

        @WTKX private Spinner numericSpinner;
        @WTKX private Spinner dateSpinner;

        @WTKX private Slider redSlider;
        @WTKX private Slider greenSlider;
        @WTKX private Slider blueSlider;
        @WTKX private Border colorBorder;

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

                rollup.setContent(component);
                wtkxSerializer.bind(this, SpinnersRollupStateHandler.class);

                initializeNumericSpinner(numericSpinner);
                initializeDateSpinner(dateSpinner);

                SliderValueListener sliderValueListener = new SliderValueListener() {
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

        @WTKX private TableView sortableTableView;
        @WTKX private TableView customTableView;
        @WTKX private TableViewHeader sortableTableViewHeader;

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

                rollup.setContent(component);
                wtkxSerializer.bind(this, TablesRollupStateHandler.class);

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

        @WTKX private TreeView editableTreeView;

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

                rollup.setContent(component);
                wtkxSerializer.bind(this, TreesRollupStateHandler.class);

                TreeBranch treeData = (TreeBranch)editableTreeView.getTreeData();
                treeData.setComparator(new TreeNodeComparator());
            }

            return Vote.APPROVE;
        }
    }

    private class DragDropRollupStateHandler extends RollupStateHandler {
        private Component component = null;

        @WTKX private ImageView imageView1;
        @WTKX private ImageView imageView2;
        @WTKX private ImageView imageView3;

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

                rollup.setContent(component);
                wtkxSerializer.bind(this, DragDropRollupStateHandler.class);

                DragSource imageDragSource = new DragSource() {
                    private Image image = null;
                    private Point offset = null;
                    private LocalManifest content = null;

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

                    public void endDrag(Component component, DropAction dropAction) {
                        if (dropAction == null) {
                            ImageView imageView = (ImageView)component;
                            imageView.setImage(image);
                        }

                        image = null;
                        offset = null;
                        content = null;
                    }

                    public boolean isNative() {
                        return false;
                    }

                    public LocalManifest getContent() {
                        return content;
                    }

                    public Visual getRepresentation() {
                        return image;
                    }

                    public Point getOffset() {
                        return offset;
                    }

                    public int getSupportedDropActions() {
                        return DropAction.MOVE.getMask();
                    }
                };

                DropTarget imageDropTarget = new DropTarget() {
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

                    public void dragExit(Component component) {
                        component.getStyles().put("backgroundColor", null);
                    }

                    public DropAction dragMove(Component component, Manifest dragContent,
                        int supportedDropActions, int x, int y, DropAction userDropAction) {
                        return (dragContent.containsImage() ? DropAction.MOVE : null);
                    }

                    public DropAction userDropActionChange(Component component, Manifest dragContent,
                        int supportedDropActions, int x, int y, DropAction userDropAction) {
                        return (dragContent.containsImage() ? DropAction.MOVE : null);
                    }

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

        @WTKX private PushButton alertButton;
        @WTKX private PushButton promptButton;

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

                rollup.setContent(component);
                wtkxSerializer.bind(this, AlertsRollupStateHandler.class);

                alertButton.getButtonPressListeners().add(new ButtonPressListener() {
                    public void buttonPressed(Button button) {
                        Button.Group messageTypeGroup = Button.getGroup("messageType");
                        Button selection = messageTypeGroup.getSelection();

                        Map<String, ?> userData;
                        try {
                            userData = JSONSerializer.parseMap((String)selection.getUserData().get("messageInfo"));
                        } catch (SerializationException exception) {
                            throw new RuntimeException(exception);
                        }

                        String messageType = (String)userData.get("type");

                        if (messageType.equals("custom")) {
                            ArrayList<String> options = new ArrayList<String>();
                            options.add("OK");
                            options.add("Cancel");

                            Component body = null;
                            WTKXSerializer wtkxSerializer = new WTKXSerializer();
                            try {
                                body = (Component)wtkxSerializer.readObject(this, "alert.wtkx");
                            } catch(Exception exception) {
                                System.out.println(exception);
                            }

                            Alert alert = new Alert(MessageType.QUESTION, "Please select your favorite icon:",
                                options, body);
                            alert.setTitle("Select Icon");
                            alert.setSelectedOption(0);
                            alert.getDecorators().update(0, new ReflectionDecorator());

                            alert.open(window);
                        } else {
                            String message = (String)userData.get("message");
                            Alert.alert(MessageType.decode(messageType), message, window);
                        }
                    }
                });

                promptButton.getButtonPressListeners().add(new ButtonPressListener() {
                    public void buttonPressed(Button button) {
                        Button.Group messageTypeGroup = Button.getGroup("messageType");
                        Button selection = messageTypeGroup.getSelection();

                        Map<String, ?> userData;
                        try {
                            userData = JSONSerializer.parseMap((String)selection.getUserData().get("messageInfo"));
                        } catch (SerializationException exception) {
                            throw new RuntimeException(exception);
                        }

                        String messageType = (String)userData.get("type");

                        if (messageType.equals("custom")) {
                            ArrayList<String> options = new ArrayList<String>();
                            options.add("OK");
                            options.add("Cancel");

                            Component body = null;
                            WTKXSerializer wtkxSerializer = new WTKXSerializer();
                            try {
                                body = (Component)wtkxSerializer.readObject(this, "alert.wtkx");
                            } catch(Exception exception) {
                                System.out.println(exception);
                            }

                            Prompt prompt = new Prompt(MessageType.QUESTION, "Please select your favorite icon:",
                                options, body);
                            prompt.setTitle("Select Icon");
                            prompt.setSelectedOption(0);
                            prompt.getDecorators().update(0, new ReflectionDecorator());

                            prompt.open(window);
                        } else {
                            String message = (String)userData.get("message");
                            Prompt.prompt(MessageType.decode(messageType), message, window);
                        }
                    }
                });
            }

            return Vote.APPROVE;
        }
    }

    private Window window = null;

    @WTKX private Rollup buttonsRollup;
    @WTKX private Rollup listsRollup;
    @WTKX private Rollup textRollup;
    @WTKX private Rollup calendarsRollup;
    @WTKX private Rollup navigationRollup;
    @WTKX private Rollup splittersRollup;
    @WTKX private Rollup menusRollup;
    @WTKX private Rollup metersRollup;
    @WTKX private Rollup spinnersRollup;
    @WTKX private Rollup tablesRollup;
    @WTKX private Rollup treesRollup;
    @WTKX private Rollup dragDropRollup;
    @WTKX private Rollup alertsRollup;

    public static void main(String[] args) {
        DesktopApplicationContext.main(KitchenSink.class, args);
    }

    public void startup(Display display, Map<String, String> properties) throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "kitchen_sink.wtkx");
        wtkxSerializer.bind(this, KitchenSink.class);

        buttonsRollup.getRollupStateListeners().add(new ButtonsRollupStateHandler());
        listsRollup.getRollupStateListeners().add(new ListsRollupStateHandler());
        textRollup.getRollupStateListeners().add(new TextRollupStateHandler());
        calendarsRollup.getRollupStateListeners().add(new CalendarsRollupStateHandler());
        navigationRollup.getRollupStateListeners().add(new NavigationRollupStateHandler());
        splittersRollup.getRollupStateListeners().add(new SplittersRollupStateHandler());
        menusRollup.getRollupStateListeners().add(new MenusRollupStateHandler());
        metersRollup.getRollupStateListeners().add(new MetersRollupStateHandler());
        spinnersRollup.getRollupStateListeners().add(new SpinnersRollupStateHandler());
        tablesRollup.getRollupStateListeners().add(new TablesRollupStateHandler());
        treesRollup.getRollupStateListeners().add(new TreesRollupStateHandler());
        dragDropRollup.getRollupStateListeners().add(new DragDropRollupStateHandler());
        alertsRollup.getRollupStateListeners().add(new AlertsRollupStateHandler());

        window.open(display);

        // Start with the "Buttons" rollup expanded
        ApplicationContext.scheduleCallback(new Runnable() {
            public void run() {
                buttonsRollup.setExpanded(true);
            }
        }, 0);
    }

    public boolean shutdown(boolean optional) throws Exception {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public void handleAbout() {
        String about = "Origin: " + ApplicationContext.getOrigin()
            + "; JVM version: " + ApplicationContext.getJVMVersion();

        Prompt.prompt(about, window);
    }
}
