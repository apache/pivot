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
package pivot.tutorials;

import java.awt.Color;
import java.net.URL;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.collections.Map;
import pivot.collections.Sequence;
import pivot.serialization.JSONSerializer;
import pivot.util.CalendarDate;
import pivot.util.Vote;
import pivot.wtk.Action;
import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Border;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.Dimensions;
import pivot.wtk.DragSource;
import pivot.wtk.DropAction;
import pivot.wtk.DropTarget;
import pivot.wtk.ImageView;
import pivot.wtk.Insets;
import pivot.wtk.Keyboard;
import pivot.wtk.ListView;
import pivot.wtk.Menu;
import pivot.wtk.MenuPopup;
import pivot.wtk.MessageType;
import pivot.wtk.Mouse;
import pivot.wtk.Point;
import pivot.wtk.Popup;
import pivot.wtk.Prompt;
import pivot.wtk.PushButton;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Bounds;
import pivot.wtk.ScrollPane;
import pivot.wtk.Slider;
import pivot.wtk.SliderValueListener;
import pivot.wtk.Spinner;
import pivot.wtk.TableView;
import pivot.wtk.TableViewHeader;
import pivot.wtk.TextInput;
import pivot.wtk.TreeView;
import pivot.wtk.Visual;
import pivot.wtk.Window;
import pivot.wtk.WindowStateListener;
import pivot.wtk.content.CalendarDateSpinnerData;
import pivot.wtk.content.NumericSpinnerData;
import pivot.wtk.content.TableRow;
import pivot.wtk.content.TableViewHeaderData;
import pivot.wtk.content.TreeNode;
import pivot.wtk.content.TreeViewNodeRenderer;
import pivot.wtk.effects.ReflectionDecorator;
import pivot.wtk.media.Image;
import pivot.wtkx.WTKXSerializer;

public class Demo implements Application {
    private class TreeViewEditHandler
        implements ComponentMouseButtonListener, ComponentKeyListener {
        Sequence<Integer> path = null;
        boolean armed = false;
        Popup popup = null;

        private int getNodeLabelOffset() {
            int nodeLabelOffset = editableTreeView.getNodeOffset(path);

            TreeViewNodeRenderer nodeRenderer =
                (TreeViewNodeRenderer)editableTreeView.getNodeRenderer();

            nodeLabelOffset += ((Insets)nodeRenderer.getStyles().get("padding")).left;
            if (nodeRenderer.getShowIcon()) {
                nodeLabelOffset += nodeRenderer.getIconWidth();
                nodeLabelOffset += (Integer)nodeRenderer.getStyles().get("spacing");
            }

            return nodeLabelOffset;
        }

        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            path = editableTreeView.getNodeAt(y);

            armed = (popup == null
                && path != null
                && Keyboard.getModifiers() == 0
                && editableTreeView.isPathSelected(path)
                && x >= getNodeLabelOffset());

            return false;
        }

        @SuppressWarnings("unchecked")
        public boolean mouseClick(Component component, Mouse.Button button, int x, int y,
            int count) {
            if (armed
                && count == 1) {
                List<Object> treeData = (List<Object>)editableTreeView.getTreeData();
                TreeNode nodeData = (TreeNode)Sequence.Tree.get(treeData, path);

                Bounds nodeLabelBounds = editableTreeView.getNodeBounds(path);
                int nodeLabelOffset = getNodeLabelOffset();
                nodeLabelBounds.x += nodeLabelOffset;
                nodeLabelBounds.width -= nodeLabelOffset;

                Bounds viewportBounds = editableTreeViewScrollPane.getViewportBounds();

                TextInput textInput = new TextInput();
                textInput.setText(nodeData.getText());
                textInput.setPreferredWidth(Math.min(nodeLabelBounds.width,
                    viewportBounds.width - nodeLabelBounds.x));
                textInput.getComponentKeyListeners().add(this);

                Point treeViewCoordinates =
                    editableTreeView.mapPointToAncestor(window.getDisplay(), 0, 0);

                popup = new Popup(textInput);
                popup.setLocation(treeViewCoordinates.x + nodeLabelBounds.x,
                    treeViewCoordinates.y + nodeLabelBounds.y
                    + (nodeLabelBounds.height - textInput.getPreferredHeight(-1)) / 2);

                // Ensure that we clear the popup reference
                popup.getWindowStateListeners().add(new WindowStateListener() {
                    public Vote previewWindowOpen(Window window, Display display) {
                        return Vote.APPROVE;
                    }

                    public void windowOpenVetoed(Window window, Vote reason) {
                    }

                    public void windowOpened(Window window) {
                    }

                    public Vote previewWindowClose(Window window) {
                        return Vote.APPROVE;
                    }

                    public void windowCloseVetoed(Window window, Vote reason) {
                    }

                    public void windowClosed(Window window, Display display) {
                        popup = null;
                    }
                });

                popup.open(editableTreeView);

                textInput.requestFocus();
            }

            armed = false;

            return false;
        }

        public boolean keyTyped(Component component, char character) {
            return false;
        }

        @SuppressWarnings("unchecked")
        public boolean keyPressed(Component component, int keyCode,
            Keyboard.KeyLocation keyLocation) {
            if (keyCode == Keyboard.KeyCode.ENTER) {
                List<Object> treeData = (List<Object>)editableTreeView.getTreeData();
                TreeNode nodeData = (TreeNode)Sequence.Tree.get(treeData, path);

                TextInput textInput = (TextInput)component;
                String text = textInput.getText();

                nodeData.setText(text);

                popup.close();
                editableTreeView.requestFocus();
            }

            if (keyCode == Keyboard.KeyCode.ESCAPE) {
                popup.close();
                editableTreeView.requestFocus();
            }

            return false;
        }

        public boolean keyReleased(Component component, int keyCode,
            Keyboard.KeyLocation keyLocation) {
            return false;
        }
    }

    private static class ImageDragHandler implements DragSource {
        ImageView imageView = null;
        private Image image = null;
        private Dimensions offset = null;

        public Object beginDrag(Component component, int x, int y) {
            imageView = (ImageView)component;
            image = imageView.getImage();

            if (image != null) {
                imageView.setImage((Image)null);
                offset = new Dimensions(x - (imageView.getWidth() - image.getWidth()) / 2,
                    y - (imageView.getHeight() - image.getHeight()) / 2);
            }

            return image;
        }

        public void endDrag(DropAction dropAction) {
            if (dropAction == null) {
                imageView.setImage(image);
            }
        }

        public Visual getRepresentation() {
            return image;
        }

        public Dimensions getOffset() {
            return offset;
        }
    }

    private static class ImageDropHandler implements DropTarget {
        public DropAction getDropAction(Component component, Class<?> contentType,
            int x, int y) {
            DropAction dropAction = null;

            if (Image.class.isAssignableFrom(contentType)) {
                dropAction = DropAction.MOVE;
            }

            return dropAction;
        }

        public void drop(Component component, Object content, int x, int y) {
            ImageView imageView = (ImageView)component;

            if (imageView.getImage() == null) {
                imageView.setImage((Image)content);
                imageView.getStyles().put("backgroundColor", null);
            }
        }
    }

    private static class ImageMouseHandler implements ComponentMouseListener {
        public static final Color DROP_HIGHLIGHT_COLOR = new Color(0xf0, 0xe6, 0x8c);

        public boolean mouseMove(Component component, int x, int y) {
            return false;
        }

        public void mouseOver(Component component) {
            Class<?> dragContentType = Mouse.getDragContentType();
            if (dragContentType != null
                && Image.class.isAssignableFrom(dragContentType)) {
                ImageView imageView = (ImageView)component;

                if (imageView.getImage() == null) {
                    component.getStyles().put("backgroundColor", DROP_HIGHLIGHT_COLOR);
                }
            }
        }

        public void mouseOut(Component component) {
            component.getStyles().put("backgroundColor", null);
        }
    }

    private ListView checkedListView = null;

    private MenuPopup menuPopup = null;
    private ImageView menuImageView = null;

    private Slider redSlider = null;
    private Slider greenSlider = null;
    private Slider blueSlider = null;
    private Border colorBorder = null;

    private TableView sortableTableView = null;
    private TableView customTableView = null;
    private TableViewHeader sortableTableViewHeader = null;

    private TreeView editableTreeView = null;
    private ScrollPane editableTreeViewScrollPane = null;

    private PushButton alertButton = null;
    private PushButton promptButton = null;

    private Window window = null;

    public void startup(final Display display, Dictionary<String, String> properties) throws Exception {
        // pivot.wtk.Theme.setTheme(new pivot.wtk.skin.terra.TerraTheme("test"));

        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        Component content = (Component)wtkxSerializer.readObject("pivot/tutorials/demo.wtkx");

        new Action("selectImageAction") {
            public String getDescription() {
                return "Select Image Action";
            }

            public void perform() {
                Button.Group imageMenuGroup = Button.getGroup("imageMenuGroup");
                Button selectedItem = imageMenuGroup.getSelection();

                String imageName = (String)selectedItem.getUserData();

                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                URL imageURL = classLoader.getResource(imageName);

                // If the image has not been added to the resource cache yet,
                // add it
                Image image = (Image)ApplicationContext.getResourceCache().get(imageURL);

                if (image == null) {
                    image = Image.load(imageURL);
                    ApplicationContext.getResourceCache().put(imageURL, image);
                }

                // Update the image
                menuImageView.setImage(image);
            }
        };

        checkedListView = (ListView)wtkxSerializer.getObjectByName("lists.checkedListView");
        checkedListView.setItemChecked(0, true);
        checkedListView.setItemChecked(2, true);
        checkedListView.setItemChecked(3, true);

        menuImageView = (ImageView)wtkxSerializer.getObjectByName("menus.imageView");
        menuImageView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener() {
            public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
                if (button == Mouse.Button.RIGHT) {
                    menuPopup.open(display, component.mapPointToAncestor(display, x, y));
                }

                return false;
            }

            public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
                return false;
            }

            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
                return false;
            }
        });

        // Spinners
        Spinner numericSpinner = (Spinner)wtkxSerializer.getObjectByName("spinners.numericSpinner");
        initializeNumericSpinner(numericSpinner);

        Spinner dateSpinner = (Spinner)wtkxSerializer.getObjectByName("spinners.dateSpinner");
        initializeDateSpinner(dateSpinner);

        // Sliders
        SliderValueListener sliderValueListener = new SliderValueListener() {
        	public void valueChanged(Slider slider, int previousValue) {
        		Color color = new Color(redSlider.getValue(), greenSlider.getValue(),
    				blueSlider.getValue());
        		colorBorder.getStyles().put("backgroundColor", color);
        	}
        };

        redSlider = (Slider)wtkxSerializer.getObjectByName("spinners.redSlider");
        redSlider.getSliderValueListeners().add(sliderValueListener);

        greenSlider = (Slider)wtkxSerializer.getObjectByName("spinners.greenSlider");
        greenSlider.getSliderValueListeners().add(sliderValueListener);

        blueSlider = (Slider)wtkxSerializer.getObjectByName("spinners.blueSlider");
        blueSlider.getSliderValueListeners().add(sliderValueListener);

    	Color color = new Color(redSlider.getValue(), greenSlider.getValue(),
			blueSlider.getValue());
        colorBorder = (Border)wtkxSerializer.getObjectByName("spinners.colorBorder");
		colorBorder.getStyles().put("backgroundColor", color);

        sortableTableView = (TableView)wtkxSerializer.getObjectByName("tables.sortableTableView");
        sortableTableViewHeader = (TableViewHeader)wtkxSerializer.getObjectByName("tables.sortableTableViewHeader");
        customTableView = (TableView)wtkxSerializer.getObjectByName("tables.customTableView");
        initializeTableViews();

        editableTreeView = (TreeView)wtkxSerializer.getObjectByName("trees.editableTreeView");
        editableTreeViewScrollPane = (ScrollPane)wtkxSerializer.getObjectByName("trees.editableTreeViewScrollPane");
        initializeEditableTreeView();

        ImageDragHandler imageDragHandler = new ImageDragHandler();
        ImageDropHandler imageDropHandler = new ImageDropHandler();
        ImageMouseHandler imageMouseHandler = new ImageMouseHandler();

        ImageView imageView1 = (ImageView)wtkxSerializer.getObjectByName("dragdrop.imageView1");
        imageView1.setDragSource(imageDragHandler);
        imageView1.setDropTarget(imageDropHandler);
        imageView1.getComponentMouseListeners().add(imageMouseHandler);

        ImageView imageView2 = (ImageView)wtkxSerializer.getObjectByName("dragdrop.imageView2");
        imageView2.setDragSource(imageDragHandler);
        imageView2.setDropTarget(imageDropHandler);
        imageView2.getComponentMouseListeners().add(imageMouseHandler);

        ImageView imageView3 = (ImageView)wtkxSerializer.getObjectByName("dragdrop.imageView3");
        imageView3.setDragSource(imageDragHandler);
        imageView3.setDropTarget(imageDropHandler);
        imageView3.getComponentMouseListeners().add(imageMouseHandler);

        alertButton = (PushButton)wtkxSerializer.getObjectByName("alerts.alertButton");
        promptButton = (PushButton)wtkxSerializer.getObjectByName("alerts.promptButton");
        initializeAlertButtons();

        menuPopup = new MenuPopup((Menu)wtkxSerializer.readObject("pivot/tutorials/menu_popup.wtkx"));

        window = new Window();
        window.setTitle("Pivot Demo");
        window.setMaximized(true);
        window.setContent(content);

        window.open(display);
    }

    @SuppressWarnings("unchecked")
    private void initializeTableViews() {
        // Set table header data
        TableView.ColumnSequence columns = sortableTableView.getColumns();
        columns.get(0).setHeaderData(new TableViewHeaderData("#"));
        columns.get(1).setHeaderData(new TableViewHeaderData("A"));
        columns.get(2).setHeaderData(new TableViewHeaderData("B"));
        columns.get(3).setHeaderData(new TableViewHeaderData("C"));
        columns.get(4).setHeaderData(new TableViewHeaderData("D"));

        // Populate table
        ArrayList<Object> tableData = new ArrayList<Object>();

        for (int i = 0; i < 10000; i++) {
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

        // Load images for custom table
        final List<TableRow> customTableData = (List<TableRow>)customTableView.getTableData();
        for (int i = 0, n = customTableData.getLength(); i < n; i++) {
            TableRow tableRow = customTableData.get(i);
            tableRow.put("b", Image.load((URL)tableRow.get("b")));
        }

        customTableView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener() {
            public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            	return false;
            }

            public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            	return false;
            }

            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            	if (button == Mouse.Button.LEFT) {
            		int columnIndex = customTableView.getColumnAt(x);
            		if (columnIndex == 0) {
            			int rowIndex = customTableView.getRowAt(y);
            			TableRow row = customTableData.get(rowIndex);

            			Object a = row.get("a");
            			if (a instanceof String) {
            				a = Boolean.parseBoolean((String)a);
            			}

            			row.put("a", !(Boolean)a);
            			customTableData.update(rowIndex, row);
            		}
            	}

            	return false;
            }
        });
    }

    private void initializeEditableTreeView() {
        TreeViewEditHandler treeViewEditHandler = new TreeViewEditHandler();
        editableTreeView.getComponentMouseButtonListeners().add(treeViewEditHandler);
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
        dateSpinner.setSelectedValue(today);
    }

    @SuppressWarnings("unchecked")
    private void initializeAlertButtons() {
        alertButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Button.Group messageTypeGroup = Button.getGroup("messageType");
                Button selection = messageTypeGroup.getSelection();

                Map<String, ?> userData = JSONSerializer.parseMap((String)selection.getUserData());
                String messageType = (String)userData.get("type");

                if (messageType.equals("custom")) {
                    ArrayList<String> options = new ArrayList<String>();
                    options.add("OK");
                    options.add("Cancel");

                    Component body = null;
                    WTKXSerializer wtkxSerializer = new WTKXSerializer();
                    try {
                        body = (Component)wtkxSerializer.readObject("pivot/tutorials/alert.wtkx");
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

                Map<String, ?> userData = JSONSerializer.parseMap((String)selection.getUserData());
                String messageType = (String)userData.get("type");

                if (messageType.equals("custom")) {
                    ArrayList<String> options = new ArrayList<String>();
                    options.add("OK");
                    options.add("Cancel");

                    Component body = null;
                    WTKXSerializer wtkxSerializer = new WTKXSerializer();
                    try {
                        body = (Component)wtkxSerializer.readObject("pivot/tutorials/alert.wtkx");
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

    public boolean shutdown(boolean optional) throws Exception {
        window.close();
        return true;
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }
}
