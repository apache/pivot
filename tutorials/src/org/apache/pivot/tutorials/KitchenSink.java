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
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Comparator;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.DragSource;
import org.apache.pivot.wtk.DropAction;
import org.apache.pivot.wtk.DropTarget;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.LocalManifest;
import org.apache.pivot.wtk.Manifest;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Rollup;
import org.apache.pivot.wtk.RollupStateListener;
import org.apache.pivot.wtk.Slider;
import org.apache.pivot.wtk.SliderValueListener;
import org.apache.pivot.wtk.SortDirection;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.Visual;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.CalendarDateSpinnerData;
import org.apache.pivot.wtk.content.ListItem;
import org.apache.pivot.wtk.content.NumericSpinnerData;
import org.apache.pivot.wtk.content.TableViewHeaderData;
import org.apache.pivot.wtk.content.TableViewRowComparator;
import org.apache.pivot.wtk.content.TreeBranch;
import org.apache.pivot.wtk.content.TreeNode;
import org.apache.pivot.wtk.effects.ReflectionDecorator;
import org.apache.pivot.wtk.media.Image;

public class KitchenSink implements Application, Application.AboutHandler {

    /**
     * A sample of a Custom Table Row. <p> Note that this is public because it's
     * references in one of bxml files of this application.
     */
    public static final class CustomTableRow {
        private boolean a = false;
        private Image b = null;
        private String c = null;

        public boolean getA() {
            return this.a;
        }

        public void setA(boolean a) {
            this.a = a;
        }

        public Image getB() {
            return this.b;
        }

        public void setB(Image b) {
            this.b = b;
        }

        public void setB(URL bURL) {
            setB(Image.loadFromCache(bURL));
        }

        public String getC() {
            return this.c;
        }

        public void setC(String c) {
            this.c = c;
        }
    }

    /**
     * Orders TreeNode instances by their name using string comparison.
     */
    private static final class TreeNodeComparator implements Comparator<TreeNode>, Serializable {
        private static final long serialVersionUID = 1L;

        public TreeNodeComparator() {
        }

        @Override
        public int compare(TreeNode treeNode1, TreeNode treeNode2) {
            String text1 = treeNode1.getText();
            String text2 = treeNode2.getText();

            int result;

            if (text1 == null && text2 == null) {
                result = 0;
            } else if (text1 == null) {
                result = -1;
            } else if (text2 == null) {
                result = 1;
            } else {
                result = text1.compareToIgnoreCase(text2);
            }

            return result;
        }
    }

    private abstract class RollupStateHandler implements RollupStateListener {
        public RollupStateHandler() {
        }

        @Override
        public void expandedChangeVetoed(Rollup rollup, Vote reason) {
            // No-op
        }

        @Override
        public void expandedChanged(Rollup rollup) {
            // No-op
        }
    }

    private class InfoRollupStateHandler extends RollupStateHandler {
        Component component = null;
        Label infoPivotVersion = null;
        Label infoPivotOrigin = null;
        Label infoJavaVersion = null;

        public InfoRollupStateHandler() {
        }

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (this.component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    this.component = (Component) bxmlSerializer.readObject(KitchenSink.class,
                        "info.bxml");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                this.infoPivotVersion = (Label) bxmlSerializer.getNamespace().get(
                    "info-pivot-version");
                this.infoPivotVersion.setText(this.infoPivotVersion.getText()
                    + ApplicationContext.getPivotVersion().toString());

                this.infoPivotOrigin = (Label) bxmlSerializer.getNamespace().get(
                    "info-pivot-origin");
                String origin = (ApplicationContext.getOrigin() != null) ? ApplicationContext.getOrigin().toString()
                    : "";
                this.infoPivotOrigin.setText(this.infoPivotOrigin.getText() + "\"" + origin + "\"");

                this.infoJavaVersion = (Label) bxmlSerializer.getNamespace().get(
                    "info-java-version");
                // String javaVersion =
                // ApplicationContext.getJVMVersion().toString();
                String javaVersion = System.getProperty("java.version");
                this.infoJavaVersion.setText(this.infoJavaVersion.getText() + javaVersion);

                rollup.setContent(this.component);
            }

            return Vote.APPROVE;
        }
    }

    private class ButtonsRollupStateHandler extends RollupStateHandler {
        private Component component = null;

        public ButtonsRollupStateHandler() {
        }

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (this.component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    this.component = (Component) bxmlSerializer.readObject(KitchenSink.class,
                        "buttons.bxml");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                rollup.setContent(this.component);
            }

            return Vote.APPROVE;
        }
    }

    private class ListsRollupStateHandler extends RollupStateHandler {
        Component component = null;
        ListView editableListView = null;
        ListView iconListView = null;
        ListView checkedListView = null;
        ListButton iconListButton = null;

        public ListsRollupStateHandler() {
        }

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (this.component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    this.component = (Component) bxmlSerializer.readObject(KitchenSink.class,
                        "lists.bxml");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                this.editableListView = (ListView) bxmlSerializer.getNamespace().get(
                    "editableListView");
                this.iconListView = (ListView) bxmlSerializer.getNamespace().get("iconListView");
                this.checkedListView = (ListView) bxmlSerializer.getNamespace().get(
                    "checkedListView");
                this.iconListButton = (ListButton) bxmlSerializer.getNamespace().get(
                    "iconListButton");

                rollup.setContent(this.component);

                @SuppressWarnings("unchecked")
                List<ListItem> listData = (List<ListItem>) this.editableListView.getListData();
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

                this.iconListView.setDisabledItemFilter(disabledItemFilter);
                this.iconListButton.setDisabledItemFilter(disabledItemFilter);

                this.checkedListView.setItemChecked(0, true);
                this.checkedListView.setItemChecked(2, true);
                this.checkedListView.setItemChecked(3, true);
            }

            return Vote.APPROVE;
        }
    }

    private class TextRollupStateHandler extends RollupStateHandler {
        Component component = null;

        public TextRollupStateHandler() {
        }

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (this.component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    this.component = (Component) bxmlSerializer.readObject(KitchenSink.class,
                        "text.bxml");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                rollup.setContent(this.component);
            }

            return Vote.APPROVE;
        }
    }

    private class CalendarsRollupStateHandler extends RollupStateHandler {
        Component component = null;

        public CalendarsRollupStateHandler() {
        }

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (this.component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    this.component = (Component) bxmlSerializer.readObject(KitchenSink.class,
                        "calendars.bxml");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                rollup.setContent(this.component);
            }

            return Vote.APPROVE;
        }
    }

    private class ColorChoosersRollupStateHandler extends RollupStateHandler {
        Component component = null;

        public ColorChoosersRollupStateHandler() {
        }

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (this.component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    this.component = (Component) bxmlSerializer.readObject(KitchenSink.class,
                        "color_choosers.bxml");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                rollup.setContent(this.component);
            }

            return Vote.APPROVE;
        }
    }

    private class NavigationRollupStateHandler extends RollupStateHandler {
        Component component = null;

        public NavigationRollupStateHandler() {
        }

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (this.component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    this.component = (Component) bxmlSerializer.readObject(KitchenSink.class,
                        "navigation.bxml");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                rollup.setContent(this.component);
            }

            return Vote.APPROVE;
        }
    }

    private class SplittersRollupStateHandler extends RollupStateHandler {
        Component component = null;

        public SplittersRollupStateHandler() {
        }

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (this.component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    this.component = (Component) bxmlSerializer.readObject(KitchenSink.class,
                        "splitters.bxml");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                rollup.setContent(this.component);
            }

            return Vote.APPROVE;
        }
    }

    private class MenusRollupStateHandler extends RollupStateHandler {
        Component component = null;
        ImageView menuImageView = null;
        Menu.Item helpAboutMenuItem = null;

        Menu.Section menuSection = null;
        ButtonGroup imageMenuGroup = null;

        public MenusRollupStateHandler() {
        }

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (this.component == null) {
                Action.getNamedActions().put("selectImageAction", new Action() {
                    @Override
                    public String getDescription() {
                        return "Select Image Action";
                    }

                    @Override
                    public void perform(Component source) {
                        Button selectedItem = MenusRollupStateHandler.this.imageMenuGroup.getSelection();

                        String imageName = (String) selectedItem.getUserData().get("image");
                        URL imageURL = getClass().getResource(imageName);

                        // Update the image
                        MenusRollupStateHandler.this.menuImageView.setImage(Image.loadFromCache(imageURL));
                    }
                });

                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    this.component = (Component) bxmlSerializer.readObject(KitchenSink.class,
                        "menus.bxml");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                this.menuImageView = (ImageView) bxmlSerializer.getNamespace().get("menuImageView");
                this.helpAboutMenuItem = (Menu.Item) bxmlSerializer.getNamespace().get(
                    "helpAboutMenuItem");

                rollup.setContent(this.component);

                try {
                    this.menuSection = (Menu.Section) bxmlSerializer.readObject(KitchenSink.class,
                        "menu_section.bxml");
                    this.imageMenuGroup = (ButtonGroup) bxmlSerializer.getNamespace().get(
                        "imageMenuGroup");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                this.menuImageView.setMenuHandler(new MenuHandler() {
                    @Override
                    public boolean configureContextMenu(Component comp, Menu menu, int x, int y) {
                        menu.getSections().add(MenusRollupStateHandler.this.menuSection);
                        return false;
                    }
                });

                this.helpAboutMenuItem.getButtonPressListeners().add(new ButtonPressListener() {
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
        Component component = null;
        ActivityIndicator activityIndicator1 = null;
        ActivityIndicator activityIndicator2 = null;
        ActivityIndicator activityIndicator3 = null;

        public MetersRollupStateHandler() {
        }

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (this.component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    this.component = (Component) bxmlSerializer.readObject(KitchenSink.class,
                        "meters.bxml");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                this.activityIndicator1 = (ActivityIndicator) bxmlSerializer.getNamespace().get(
                    "activityIndicator1");
                this.activityIndicator2 = (ActivityIndicator) bxmlSerializer.getNamespace().get(
                    "activityIndicator2");
                this.activityIndicator3 = (ActivityIndicator) bxmlSerializer.getNamespace().get(
                    "activityIndicator3");

                rollup.setContent(this.component);

                KitchenSink.this.metersRollup.getRollupStateListeners().add(
                    new RollupStateListener() {
                        @Override
                        public Vote previewExpandedChange(Rollup roll) {
                            return Vote.APPROVE;
                        }

                        @Override
                        public void expandedChangeVetoed(Rollup roll, Vote reason) {
                            // No-op
                        }

                        @Override
                        public void expandedChanged(Rollup roll) {
                            MetersRollupStateHandler.this.activityIndicator1.setActive(roll.isExpanded());
                            MetersRollupStateHandler.this.activityIndicator2.setActive(roll.isExpanded());
                            MetersRollupStateHandler.this.activityIndicator3.setActive(roll.isExpanded());
                        }
                    });
            }

            return Vote.APPROVE;
        }
    }

    private class SpinnersRollupStateHandler extends RollupStateHandler {
        Component component = null;

        Spinner numericSpinner = null;
        Spinner dateSpinner = null;

        Slider redSlider = null;
        Slider greenSlider = null;
        Slider blueSlider = null;
        Border colorBorder = null;

        public SpinnersRollupStateHandler() {
        }

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (this.component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    this.component = (Component) bxmlSerializer.readObject(KitchenSink.class,
                        "spinners.bxml");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                this.numericSpinner = (Spinner) bxmlSerializer.getNamespace().get("numericSpinner");
                this.dateSpinner = (Spinner) bxmlSerializer.getNamespace().get("dateSpinner");

                this.redSlider = (Slider) bxmlSerializer.getNamespace().get("redSlider");
                this.greenSlider = (Slider) bxmlSerializer.getNamespace().get("greenSlider");
                this.blueSlider = (Slider) bxmlSerializer.getNamespace().get("blueSlider");
                this.colorBorder = (Border) bxmlSerializer.getNamespace().get("colorBorder");

                rollup.setContent(this.component);

                initializeNumericSpinner(this.numericSpinner);
                initializeDateSpinner(this.dateSpinner);

                SliderValueListener sliderValueListener = new SliderValueListener() {
                    @Override
                    public void valueChanged(Slider slider, int previousValue) {
                        Color color = new Color(
                            SpinnersRollupStateHandler.this.redSlider.getValue(),
                            SpinnersRollupStateHandler.this.greenSlider.getValue(),
                            SpinnersRollupStateHandler.this.blueSlider.getValue());
                        SpinnersRollupStateHandler.this.colorBorder.getStyles().put(
                            Style.backgroundColor, color);
                    }
                };

                this.redSlider.getSliderValueListeners().add(sliderValueListener);
                this.greenSlider.getSliderValueListeners().add(sliderValueListener);
                this.blueSlider.getSliderValueListeners().add(sliderValueListener);

                Color color = new Color(this.redSlider.getValue(), this.greenSlider.getValue(),
                    this.blueSlider.getValue());
                this.colorBorder.getStyles().put(Style.backgroundColor, color);
            }

            return Vote.APPROVE;
        }

        private void initializeNumericSpinner(Spinner spinner) {
            NumericSpinnerData numericSpinnerData = new NumericSpinnerData(0, 256, 4);
            spinner.setSpinnerData(numericSpinnerData);
            spinner.setSelectedIndex(0);
        }

        private void initializeDateSpinner(Spinner spinner) {
            CalendarDate lowerBound = new CalendarDate(2008, 0, 0);
            CalendarDate upperBound = new CalendarDate(2019, 11, 30);
            CalendarDateSpinnerData spinnerData = new CalendarDateSpinnerData(lowerBound,
                upperBound);

            CalendarDate today = new CalendarDate();
            spinner.setSpinnerData(spinnerData);
            spinner.setSelectedItem(today);
        }
    }

    private class TablesRollupStateHandler extends RollupStateHandler {
        Component component = null;
        TableView sortableTableView = null;
        TableView customTableView = null;

        public TablesRollupStateHandler() {
        }

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (this.component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    this.component = (Component) bxmlSerializer.readObject(KitchenSink.class,
                        "tables.bxml");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                this.sortableTableView = (TableView) bxmlSerializer.getNamespace().get(
                    "sortableTableView");
                this.customTableView = (TableView) bxmlSerializer.getNamespace().get(
                    "customTableView");

                rollup.setContent(this.component);

                // Set table header data
                TableView.ColumnSequence columns = this.sortableTableView.getColumns();
                columns.get(0).setHeaderData(new TableViewHeaderData("#"));
                columns.get(1).setHeaderData(new TableViewHeaderData("A"));
                columns.get(2).setHeaderData(new TableViewHeaderData("B"));
                columns.get(3).setHeaderData(new TableViewHeaderData("C"));
                columns.get(4).setHeaderData(new TableViewHeaderData("D"));

                // Populate table
                ArrayList<Object> tableData = new ArrayList<>(10000);

                for (int i = 0, n = tableData.getCapacity(); i < n; i++) {
                    HashMap<String, Integer> tableRow = new HashMap<>();

                    tableRow.put("i", i);
                    tableRow.put("a", (int) Math.round(Math.random() * 10));
                    tableRow.put("b", (int) Math.round(Math.random() * 100));
                    tableRow.put("c", (int) Math.round(Math.random() * 1000));
                    tableRow.put("d", (int) Math.round(Math.random() * 10000));

                    tableData.add(tableRow);
                }

                this.sortableTableView.setTableData(tableData);
                this.sortableTableView.getTableViewSortListeners().add(new TableViewSortListener() {
                    @Override
                    public void sortAdded(TableView tableView, String columnName) {
                        resort(tableView);
                    }

                    @Override
                    public void sortUpdated(TableView tableView, String columnName,
                        SortDirection previousSortDirection) {
                        resort(tableView);
                    }

                    @Override
                    public void sortRemoved(TableView tableView, String columnName,
                        SortDirection sortDirection) {
                        resort(tableView);
                    }

                    @Override
                    public void sortChanged(TableView tableView) {
                        resort(tableView);
                    }

                    private void resort(TableView tableView) {
                        @SuppressWarnings("unchecked")
                        List<Object> tableDataOfTableView = (List<Object>) tableView.getTableData();
                        tableDataOfTableView.setComparator(new TableViewRowComparator(tableView));
                    }
                });

                this.customTableView.getComponentMouseButtonListeners().add(
                    new ComponentMouseButtonListener() {
                        @Override
                        public boolean mouseClick(Component comp, Mouse.Button button, int x,
                            int y, int count) {
                            if (button == Mouse.Button.LEFT) {
                                @SuppressWarnings("unchecked")
                                List<CustomTableRow> customTableData =
                                    (List<CustomTableRow>) TablesRollupStateHandler.this.customTableView.getTableData();

                                int columnIndex = TablesRollupStateHandler.this.customTableView.getColumnAt(x);
                                if (columnIndex == 0) {
                                    int rowIndex = TablesRollupStateHandler.this.customTableView.getRowAt(y);
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
        Component component = null;
        TreeView editableTreeView = null;
        TreeView checkTreeView = null;

        public TreesRollupStateHandler() {
        }

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (this.component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    this.component = (Component) bxmlSerializer.readObject(KitchenSink.class,
                        "trees.bxml");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                this.editableTreeView = (TreeView) bxmlSerializer.getNamespace().get(
                    "editableTreeView");
                this.checkTreeView = (TreeView) bxmlSerializer.getNamespace().get("checkTreeView");

                rollup.setContent(this.component);

                TreeBranch treeData = (TreeBranch) this.editableTreeView.getTreeData();
                treeData.setComparator(new TreeNodeComparator());

                this.checkTreeView.setDisabledNodeFilter(new Filter<TreeNode>() {
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

                this.checkTreeView.setDisabledCheckmarkFilter(new Filter<TreeNode>() {
                    @Override
                    public boolean include(TreeNode treeNode) {
                        return (treeNode instanceof TreeBranch);
                    }
                });
            }

            return Vote.APPROVE;
        }
    }

    private class DragDropRollupStateHandler extends RollupStateHandler {
        Component component = null;
        ImageView imageView1 = null;
        ImageView imageView2 = null;
        ImageView imageView3 = null;

        public DragDropRollupStateHandler() {
        }

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (this.component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    this.component = (Component) bxmlSerializer.readObject(KitchenSink.class,
                        "dragdrop.bxml");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                this.imageView1 = (ImageView) bxmlSerializer.getNamespace().get("imageView1");
                this.imageView2 = (ImageView) bxmlSerializer.getNamespace().get("imageView2");
                this.imageView3 = (ImageView) bxmlSerializer.getNamespace().get("imageView3");

                rollup.setContent(this.component);

                DragSource imageDragSource = new DragSource() {
                    private Image image = null;
                    private Point offset = null;
                    private LocalManifest content = null;

                    @Override
                    public boolean beginDrag(Component comp, int x, int y) {
                        ImageView imageView = (ImageView) comp;
                        this.image = imageView.getImage();

                        if (this.image != null) {
                            imageView.setImage((Image) null);
                            this.content = new LocalManifest();
                            this.content.putImage(this.image);
                            this.offset = new Point(x
                                - (imageView.getWidth() - this.image.getWidth()) / 2, y
                                - (imageView.getHeight() - this.image.getHeight()) / 2);
                        }

                        return (this.image != null);
                    }

                    @Override
                    public void endDrag(Component comp, DropAction dropAction) {
                        if (dropAction == null) {
                            ImageView imageView = (ImageView) comp;
                            imageView.setImage(this.image);
                        }

                        this.image = null;
                        this.offset = null;
                        this.content = null;
                    }

                    @Override
                    public boolean isNative() {
                        return false;
                    }

                    @Override
                    public LocalManifest getContent() {
                        return this.content;
                    }

                    @Override
                    public Visual getRepresentation() {
                        return this.image;
                    }

                    @Override
                    public Point getOffset() {
                        return this.offset;
                    }

                    @Override
                    public int getSupportedDropActions() {
                        return DropAction.MOVE.getMask();
                    }
                };

                DropTarget imageDropTarget = new DropTarget() {
                    @Override
                    public DropAction dragEnter(Component comp, Manifest dragContent,
                        int supportedDropActions, DropAction userDropAction) {
                        DropAction dropAction = null;

                        ImageView imageView = (ImageView) comp;
                        if (imageView.getImage() == null && dragContent.containsImage()
                            && DropAction.MOVE.isSelected(supportedDropActions)) {
                            dropAction = DropAction.MOVE;
                            comp.getStyles().put(Style.backgroundColor, "#f0e68c");
                        }

                        return dropAction;
                    }

                    @Override
                    public void dragExit(Component comp) {
                        comp.getStyles().put(Style.backgroundColor, null);
                    }

                    @Override
                    public DropAction dragMove(Component comp, Manifest dragContent,
                        int supportedDropActions, int x, int y, DropAction userDropAction) {
                        ImageView imageView = (ImageView) comp;
                        return (imageView.getImage() == null && dragContent.containsImage() ? DropAction.MOVE
                            : null);
                    }

                    @Override
                    public DropAction userDropActionChange(Component comp, Manifest dragContent,
                        int supportedDropActions, int x, int y, DropAction userDropAction) {
                        ImageView imageView = (ImageView) comp;
                        return (imageView.getImage() == null && dragContent.containsImage() ? DropAction.MOVE
                            : null);
                    }

                    @Override
                    public DropAction drop(Component comp, Manifest dragContent,
                        int supportedDropActions, int x, int y, DropAction userDropAction) {
                        DropAction dropAction = null;

                        ImageView imageView = (ImageView) comp;
                        if (imageView.getImage() == null && dragContent.containsImage()) {
                            try {
                                imageView.setImage(dragContent.getImage());
                                dropAction = DropAction.MOVE;
                            } catch (IOException exception) {
                                System.err.println(exception);
                            }
                        }

                        dragExit(comp);

                        return dropAction;
                    }
                };

                this.imageView1.setDragSource(imageDragSource);
                this.imageView1.setDropTarget(imageDropTarget);

                this.imageView2.setDragSource(imageDragSource);
                this.imageView2.setDropTarget(imageDropTarget);

                this.imageView3.setDragSource(imageDragSource);
                this.imageView3.setDropTarget(imageDropTarget);
            }

            return Vote.APPROVE;
        }
    }

    private class AlertsRollupStateHandler extends RollupStateHandler {
        Component component = null;
        PushButton alertButton = null;
        PushButton promptButton = null;
        ButtonGroup messageTypeGroup = null;

        public AlertsRollupStateHandler() {
        }

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (this.component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    this.component = (Component) bxmlSerializer.readObject(KitchenSink.class,
                        "alerts.bxml");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                this.alertButton = (PushButton) bxmlSerializer.getNamespace().get("alertButton");
                this.promptButton = (PushButton) bxmlSerializer.getNamespace().get("promptButton");
                this.messageTypeGroup = (ButtonGroup) bxmlSerializer.getNamespace().get(
                    "messageTypeGroup");

                rollup.setContent(this.component);

                this.alertButton.getButtonPressListeners().add(new ButtonPressListener() {
                    @Override
                    public void buttonPressed(Button button) {
                        Button selection = AlertsRollupStateHandler.this.messageTypeGroup.getSelection();

                        Map<String, ?> userData;
                        try {
                            userData = JSONSerializer.parseMap((String) selection.getUserData().get(
                                "messageInfo"));
                        } catch (SerializationException exception) {
                            throw new RuntimeException(exception);
                        }

                        String messageType = (String) userData.get("messageType");

                        if (messageType == null) {
                            ArrayList<String> options = new ArrayList<>();
                            options.add("OK");
                            options.add("Cancel");

                            Component body = null;
                            BXMLSerializer serializer = new BXMLSerializer();
                            try {
                                body = (Component) serializer.readObject(KitchenSink.class,
                                    "alert.bxml");
                            } catch (Exception exception) {
                                System.err.println(exception);
                            }

                            Alert alert = new Alert(MessageType.QUESTION,
                                "Please select your favorite icon:", options, body);
                            alert.setTitle("Select Icon");
                            alert.setSelectedOptionIndex(0);
                            alert.getDecorators().update(0, new ReflectionDecorator());
                            alert.open(KitchenSink.this.window);
                        } else {
                            String message = (String) userData.get("message");
                            Alert.alert(MessageType.valueOf(messageType.toUpperCase()), message,
                                KitchenSink.this.window);
                        }
                    }
                });

                this.promptButton.getButtonPressListeners().add(new ButtonPressListener() {
                    @Override
                    public void buttonPressed(Button button) {
                        Button selection = AlertsRollupStateHandler.this.messageTypeGroup.getSelection();

                        Map<String, ?> userData;
                        try {
                            userData = JSONSerializer.parseMap((String) selection.getUserData().get(
                                "messageInfo"));
                        } catch (SerializationException exception) {
                            throw new RuntimeException(exception);
                        }

                        String messageType = (String) userData.get("messageType");

                        if (messageType == null) {
                            ArrayList<String> options = new ArrayList<>();
                            options.add("OK");
                            options.add("Cancel");

                            Component body = null;
                            BXMLSerializer serializer = new BXMLSerializer();
                            try {
                                body = (Component) serializer.readObject(KitchenSink.class,
                                    "alert.bxml");
                            } catch (Exception exception) {
                                System.err.println(exception);
                            }

                            Prompt prompt = new Prompt(MessageType.QUESTION,
                                "Please select your favorite icon:", options, body);
                            prompt.setTitle("Select Icon");
                            prompt.setSelectedOptionIndex(0);
                            prompt.getDecorators().update(0, new ReflectionDecorator());
                            prompt.open(KitchenSink.this.window);
                        } else {
                            String message = (String) userData.get("message");
                            Prompt.prompt(MessageType.valueOf(messageType.toUpperCase()), message,
                                KitchenSink.this.window);
                        }
                    }
                });
            }

            return Vote.APPROVE;
        }
    }

    Window window = null;
    Rollup infoRollup;
    Rollup buttonsRollup;
    Rollup listsRollup;
    Rollup textRollup;
    Rollup calendarsRollup;
    Rollup colorChoosersRollup;
    Rollup navigationRollup;
    Rollup splittersRollup;
    Rollup menusRollup;
    Rollup metersRollup;
    Rollup spinnersRollup;
    Rollup tablesRollup;
    Rollup treesRollup;
    Rollup dragDropRollup;
    Rollup alertsRollup;

    @Override
    public void aboutRequested() {
        String about = "Origin: " + ApplicationContext.getOrigin() + "; JVM version: "
            + ApplicationContext.getJVMVersion();

        Prompt.prompt(about, this.window);
    }

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        this.window = (Window) bxmlSerializer.readObject(KitchenSink.class, "kitchen_sink.bxml");
        bxmlSerializer.bind(this, KitchenSink.class);

        this.infoRollup = (Rollup) bxmlSerializer.getNamespace().get("infoRollup");
        this.infoRollup.getRollupStateListeners().add(new InfoRollupStateHandler());

        this.buttonsRollup = (Rollup) bxmlSerializer.getNamespace().get("buttonsRollup");
        this.buttonsRollup.getRollupStateListeners().add(new ButtonsRollupStateHandler());

        this.listsRollup = (Rollup) bxmlSerializer.getNamespace().get("listsRollup");
        this.listsRollup.getRollupStateListeners().add(new ListsRollupStateHandler());

        this.textRollup = (Rollup) bxmlSerializer.getNamespace().get("textRollup");
        this.textRollup.getRollupStateListeners().add(new TextRollupStateHandler());

        this.calendarsRollup = (Rollup) bxmlSerializer.getNamespace().get("calendarsRollup");
        this.calendarsRollup.getRollupStateListeners().add(new CalendarsRollupStateHandler());

        this.colorChoosersRollup = (Rollup) bxmlSerializer.getNamespace().get("colorChoosersRollup");
        this.colorChoosersRollup.getRollupStateListeners().add(
            new ColorChoosersRollupStateHandler());

        this.navigationRollup = (Rollup) bxmlSerializer.getNamespace().get("navigationRollup");
        this.navigationRollup.getRollupStateListeners().add(new NavigationRollupStateHandler());

        this.splittersRollup = (Rollup) bxmlSerializer.getNamespace().get("splittersRollup");
        this.splittersRollup.getRollupStateListeners().add(new SplittersRollupStateHandler());

        this.menusRollup = (Rollup) bxmlSerializer.getNamespace().get("menusRollup");
        this.menusRollup.getRollupStateListeners().add(new MenusRollupStateHandler());

        this.metersRollup = (Rollup) bxmlSerializer.getNamespace().get("metersRollup");
        this.metersRollup.getRollupStateListeners().add(new MetersRollupStateHandler());

        this.spinnersRollup = (Rollup) bxmlSerializer.getNamespace().get("spinnersRollup");
        this.spinnersRollup.getRollupStateListeners().add(new SpinnersRollupStateHandler());

        this.tablesRollup = (Rollup) bxmlSerializer.getNamespace().get("tablesRollup");
        this.tablesRollup.getRollupStateListeners().add(new TablesRollupStateHandler());

        this.treesRollup = (Rollup) bxmlSerializer.getNamespace().get("treesRollup");
        this.treesRollup.getRollupStateListeners().add(new TreesRollupStateHandler());

        this.dragDropRollup = (Rollup) bxmlSerializer.getNamespace().get("dragDropRollup");
        this.dragDropRollup.getRollupStateListeners().add(new DragDropRollupStateHandler());

        this.alertsRollup = (Rollup) bxmlSerializer.getNamespace().get("alertsRollup");
        this.alertsRollup.getRollupStateListeners().add(new AlertsRollupStateHandler());

        this.window.open(display);

        // Start with the "Info" rollup expanded
        ApplicationContext.scheduleCallback(new Runnable() {
            @Override
            public void run() {
                KitchenSink.this.infoRollup.setExpanded(true);
            }
        }, 0);
    }

    @Override
    public boolean shutdown(boolean optional) throws Exception {
        if (this.window != null) {
            this.window.close();
        }

        return false;
    }

    // Useful to run this as a Java Application directly from the desktop
    public static void main(String[] args) {
        DesktopApplicationContext.main(KitchenSink.class, args);
    }

}
