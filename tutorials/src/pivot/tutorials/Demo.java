package pivot.tutorials;

import java.awt.Color;
import java.util.Comparator;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.Dimensions;
import pivot.wtk.DragDropManager;
import pivot.wtk.DragHandler;
import pivot.wtk.DropAction;
import pivot.wtk.DropHandler;
import pivot.wtk.ImageView;
import pivot.wtk.Insets;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.Point;
import pivot.wtk.Popup;
import pivot.wtk.PushButton;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Rectangle;
import pivot.wtk.ScrollPane;
import pivot.wtk.SortDirection;
import pivot.wtk.TableView;
import pivot.wtk.TableViewHeader;
import pivot.wtk.TableViewHeaderPressListener;
import pivot.wtk.TextInput;
import pivot.wtk.TreeView;
import pivot.wtk.Visual;
import pivot.wtk.Window;
import pivot.wtk.WindowStateListener;
import pivot.wtk.content.TableRow;
import pivot.wtk.content.TableViewHeaderData;
import pivot.wtk.content.TreeViewNodeRenderer;
import pivot.wtk.media.Image;
import pivot.wtkx.ComponentLoader;
import pivot.wtkx.LoadException;

public class Demo implements Application {
    private static class RandomDataComparator implements Comparator<Object> {
        private String columnName = null;
        private SortDirection sortDirection = null;

        public RandomDataComparator(String columnName, SortDirection sortDirection) {
            this.columnName = columnName;
            this.sortDirection = sortDirection;
        }

        public int compare(Object o1, Object o2) {
            TableRow tr1 = (TableRow)o1;
            TableRow tr2 = (TableRow)o2;

            int i1 = (Integer)tr1.get(columnName);
            int i2 = (Integer)tr2.get(columnName);

            return (i1 - i2) * (sortDirection == SortDirection.ASCENDING ? 1 : -1);
        }
    }

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

        public void mouseDown(Component component, Mouse.Button button, int x, int y) {
        }

        public void mouseUp(Component component, Mouse.Button button, int x, int y) {
            path = editableTreeView.getNodeAt(y);

            armed = (popup == null
                && path != null
                && Keyboard.getModifiers() == 0
                && editableTreeView.isPathSelected(path)
                && x >= getNodeLabelOffset());
        }

        @SuppressWarnings("unchecked")
        public void mouseClick(Component component, Mouse.Button button, int x, int y,
            int count) {
            if (armed
                && count == 1) {
                List<Object> treeData = (List<Object>)editableTreeView.getTreeData();
                Dictionary<String, Object> nodeData = (Dictionary<String, Object>)Sequence.Tree.get(treeData, path);

                Rectangle nodeLabelBounds = editableTreeView.getNodeBounds(path);
                int nodeLabelOffset = getNodeLabelOffset();
                nodeLabelBounds.x += nodeLabelOffset;
                nodeLabelBounds.width -= nodeLabelOffset;

                Rectangle viewportBounds = editableTreeViewScrollPane.getViewportBounds();

                TextInput textInput = new TextInput();
                textInput.setText((String)nodeData.get("label"));
                textInput.setPreferredWidth(Math.min(nodeLabelBounds.width,
                    viewportBounds.width - nodeLabelBounds.x));
                textInput.getComponentKeyListeners().add(this);

                Point treeViewCoordinates = editableTreeView.mapPointToAncestor(Display.getInstance(), 0, 0);

                popup = new Popup(textInput);
                popup.setLocation(treeViewCoordinates.x + nodeLabelBounds.x,
                    treeViewCoordinates.y + nodeLabelBounds.y
                    + (nodeLabelBounds.height - textInput.getPreferredHeight(-1)) / 2);

                // Ensure that we clear the popup reference
                popup.getWindowStateListeners().add(new WindowStateListener() {
                    public void windowOpened(Window window) {
                    }

                    public void windowClosed(Window window) {
                        popup = null;
                    }
                });

                popup.open(editableTreeView);

                Component.setFocusedComponent(textInput);
            }

            armed = false;
        }

        public void keyTyped(Component component, char character) {
        }

        @SuppressWarnings("unchecked")
        public void keyPressed(Component component, int keyCode,
            Keyboard.KeyLocation keyLocation) {
            if (keyCode == Keyboard.KeyCode.ENTER) {
                List<Object> treeData = (List<Object>)editableTreeView.getTreeData();
                Dictionary<String, Object> nodeData = (Dictionary<String, Object>)Sequence.Tree.get(treeData, path);

                TextInput textInput = (TextInput)component;
                String text = textInput.getText();

                nodeData.put("label", text);

                popup.close();
                Component.setFocusedComponent(editableTreeView);
            }

            if (keyCode == Keyboard.KeyCode.ESCAPE) {
                popup.close();
                Component.setFocusedComponent(editableTreeView);
            }
        }

        public void keyReleased(Component component, int keyCode,
            Keyboard.KeyLocation keyLocation) {
        }
    }

    private static class ImageDragHandler implements DragHandler {
        ImageView imageView = null;
        private Image image = null;
        private Dimensions offset = null;

        public boolean beginDrag(Component component, int x, int y) {
            imageView = (ImageView)component;
            image = imageView.getImage();

            if (image != null) {
                imageView.setImage(null);
                offset = new Dimensions(x - (imageView.getWidth() - image.getWidth()) / 2,
                    y - (imageView.getHeight() - image.getHeight()) / 2);
            }

            return (image != null);
        }

        public void endDrag(DropAction dropAction) {
            if (dropAction == null) {
                imageView.setImage(image);
            }
        }

        public Object getContent() {
            return image;
        }

        public Visual getRepresentation() {
            return image;
        }

        public Dimensions getOffset() {
            return offset;
        }

        public int getSupportedDropActions() {
            return DropAction.MOVE.getMask();
        }
    }

    private static class ImageDropHandler implements DropHandler {
        public DropAction drop(Component component, int x, int y) {
            DropAction dropAction = null;

            Object dragContent = DragDropManager.getInstance().getContent();
            if (dragContent instanceof Image) {
                ImageView imageView = (ImageView)component;

                if (imageView.getImage() == null) {
                    imageView.setImage((Image)dragContent);
                    imageView.getStyles().put("backgroundColor", null);
                    dropAction = DropAction.MOVE;
                }
            }

            return dropAction;
        }
    }

    private static class ImageMouseHandler implements ComponentMouseListener {
        public static final Color DROP_HIGHLIGHT_COLOR = new Color(0xf0, 0xe6, 0x8c);

        public void mouseMove(Component component, int x, int y) {
            // No-op
        }

        public void mouseOver(Component component) {
            DragDropManager dragDropManager = DragDropManager.getInstance();

            if (dragDropManager.isActive()) {
                Object dragContent = dragDropManager.getContent();
                ImageView imageView = (ImageView)component;

                if (dragContent instanceof Image
                    && imageView.getImage() == null) {
                    component.getStyles().put("backgroundColor", DROP_HIGHLIGHT_COLOR);
                }
            }
        }

        public void mouseOut(Component component) {
            component.getStyles().put("backgroundColor", null);
        }
    }

    private TableView sortableTableView = null;
    private TableViewHeader sortableTableViewHeader = null;

    private TreeView editableTreeView = null;
    private ScrollPane editableTreeViewScrollPane = null;

    private PushButton errorAlertButton = null;
    private PushButton warningAlertButton = null;
    private PushButton questionAlertButton = null;
    private PushButton infoAlertButton = null;
    private PushButton customAlertButton = null;

    private Window window = null;

    public void startup() throws Exception {
        ComponentLoader.initialize();

        ComponentLoader componentLoader = new ComponentLoader();
        Component component = componentLoader.load("pivot/tutorials/demo.wtkx");

        sortableTableView = (TableView)componentLoader.getComponent("tables.sortableTableView");
        sortableTableViewHeader = (TableViewHeader)componentLoader.getComponent("tables.sortableTableViewHeader");
        initializeSortableTableView();

        editableTreeView = (TreeView)componentLoader.getComponent("trees.editableTreeView");
        editableTreeViewScrollPane = (ScrollPane)componentLoader.getComponent("trees.editableTreeViewScrollPane");
        initializeEditableTreeView();

        ImageDragHandler imageDragHandler = new ImageDragHandler();
        ImageDropHandler imageDropHandler = new ImageDropHandler();
        ImageMouseHandler imageMouseHandler = new ImageMouseHandler();

        ImageView imageView1 = (ImageView)componentLoader.getComponent("dragdrop.imageView1");
        imageView1.setDragHandler(imageDragHandler);
        imageView1.setDropHandler(imageDropHandler);
        imageView1.getComponentMouseListeners().add(imageMouseHandler);

        ImageView imageView2 = (ImageView)componentLoader.getComponent("dragdrop.imageView2");
        imageView2.setDragHandler(imageDragHandler);
        imageView2.setDropHandler(imageDropHandler);
        imageView2.getComponentMouseListeners().add(imageMouseHandler);

        ImageView imageView3 = (ImageView)componentLoader.getComponent("dragdrop.imageView3");
        imageView3.setDragHandler(imageDragHandler);
        imageView3.setDropHandler(imageDropHandler);
        imageView3.getComponentMouseListeners().add(imageMouseHandler);

        errorAlertButton = (PushButton)componentLoader.getComponent("alerts.errorAlertButton");
        warningAlertButton = (PushButton)componentLoader.getComponent("alerts.warningAlertButton");
        questionAlertButton = (PushButton)componentLoader.getComponent("alerts.questionAlertButton");
        infoAlertButton = (PushButton)componentLoader.getComponent("alerts.infoAlertButton");
        customAlertButton = (PushButton)componentLoader.getComponent("alerts.customAlertButton");
        initializeAlertButtons();

        window = new Window();
        window.getAttributes().put(Display.MAXIMIZED_ATTRIBUTE, Boolean.TRUE);
        window.setContent(component);

        window.open();
    }

    @SuppressWarnings("unchecked")
    private void initializeSortableTableView() {
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
        sortableTableViewHeader.getTableViewHeaderPressListeners().add(new TableViewHeaderPressListener() {
            public void headerPressed(TableViewHeader tableViewHeader, int index) {
                TableView tableView = tableViewHeader.getTableView();
                TableView.ColumnSequence columns = tableView.getColumns();
                TableView.Column column = columns.get(index);

                Object headerData = column.getHeaderData();
                if (headerData instanceof TableViewHeaderData) {
                    SortDirection sortDirection = column.getSortDirection();

                    if (sortDirection == null
                        || sortDirection == SortDirection.DESCENDING) {
                        sortDirection = SortDirection.ASCENDING;
                    } else {
                        sortDirection = SortDirection.DESCENDING;
                    }

                    ((List<Object>)tableView.getTableData()).setComparator(new RandomDataComparator(column.getName(), sortDirection));

                    for (int i = 0, n = columns.getLength(); i < n; i++) {
                        column = columns.get(i);
                        column.setSortDirection(i == index ? sortDirection : null);
                    }
                }
            }
        });
    }

    private void initializeEditableTreeView() {
        TreeViewEditHandler treeViewEditHandler = new TreeViewEditHandler();
        editableTreeView.getComponentMouseButtonListeners().add(treeViewEditHandler);
    }

    private void initializeAlertButtons() {
        errorAlertButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Alert.alert(Alert.Type.ERROR, "This is an error alert.", window);
            }
        });

        warningAlertButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Alert.alert(Alert.Type.WARNING, "This is a warning alert.", window);
            }
        });

        questionAlertButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Alert.alert(Alert.Type.QUESTION, "This is a question alert.", window);
            }
        });

        infoAlertButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Alert.alert(Alert.Type.INFO, "This is an info alert.", window);
            }
        });

        customAlertButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Alert alert = new Alert(Alert.Type.QUESTION, "Please select your favorite icon:");

                ComponentLoader componentLoader = new ComponentLoader();
                try {
                    alert.setBody(componentLoader.load("pivot/tutorials/alert.wtkx"));
                } catch(LoadException loadException) {
                    System.out.println("Unexpected exception: " + loadException);
                }

                alert.setTitle("Select Icon");
                alert.open(window);

                ArrayList<String> optionData = new ArrayList<String>();
                optionData.add("OK");
                optionData.add("Cancel");
                alert.setOptionData(optionData);
            }
        });
    }

    public void shutdown() throws Exception {
        window.close();
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }
}
