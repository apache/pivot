package pivot.tools.explorer;

import java.util.Locale;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.tools.explorer.table.renderer.PropertyValueTableViewCellRenderer;
import pivot.tools.explorer.tree.TreeNodeList;
import pivot.tools.explorer.tree.renderer.ComponentNodeRenderer;
import pivot.tools.explorer.utils.Collections;
import pivot.util.Resources;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.Dialog;
import pivot.wtk.DialogStateListener;
import pivot.wtk.Dimensions;
import pivot.wtk.Display;
import pivot.wtk.Keyboard;
import pivot.wtk.Label;
import pivot.wtk.TableView;
import pivot.wtk.TreeView;
import pivot.wtk.TreeViewSelectionListener;
import pivot.wtk.Keyboard.KeyCode;
import pivot.wtk.Keyboard.KeyLocation;
import pivot.wtkx.WTKXSerializer;

public class Explorer implements Application, TreeViewSelectionListener {

	private Resources resources;

    private Display display;
    private Dictionary<String, String> properties;
    private Application application;

    private Dialog dialog;
    private TreeView componentTree;
    private TableView propertiesTable, stylesTable, attributesTable;
    private Label statusLabel;
    private Component attributesTab;

    public void startup(Display display, Dictionary<String, String> properties) throws Exception {

    	this.display = display;
    	this.properties = properties;

    	Application application = getSubjectApplication();
    	application.startup(display, properties);

    	// initialize Explorer
    	String className = getClass().getName().toLowerCase();
        resources = new Resources(className, Locale.getDefault());
        WTKXSerializer wtkxSerializer = new WTKXSerializer(resources);

        String resourceName = String.format("%s.wtkx", className.replace('.', '/'));
        dialog = createMainWindow( application, (Component) wtkxSerializer.readObject(resourceName));

        statusLabel = (Label) wtkxSerializer.getObjectByName("lbStatus");
        componentTree = (TreeView) wtkxSerializer.getObjectByName("trComponents");
        propertiesTable = (TableView) wtkxSerializer.getObjectByName("tbProperties");
        stylesTable = (TableView) wtkxSerializer.getObjectByName("tbStyles");
        attributesTable = (TableView) wtkxSerializer.getObjectByName("tbAttributes");
        attributesTab = (Component)wtkxSerializer.getObjectByName("tabAttributes");

        initComponentTree(componentTree, display);

        dialog.open(display, new DialogStateListener() {
            public boolean previewDialogClose(Dialog dialog, boolean result) {
                dialog.moveToBack();
                return false;
            }

            public void dialogClosed(Dialog dialog) {
                // No-op
            }
        });

        componentTree.requestFocus();
    }

    public boolean shutdown(boolean optional) throws Exception {
        dialog.close();
    	return application != null? application.shutdown(optional): true;
    }



	public void resume() throws Exception {
		if (application != null) application.resume();
	}

	public void suspend() throws Exception {
		if (application != null) application.suspend();
	}

	/**
	 * Return the application subject to exploring
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
    private Application getSubjectApplication() throws InstantiationException, IllegalAccessException, ClassNotFoundException {

    	String appClassName = properties.get("applicationClassName");
    	if ( appClassName == null ) {
    		throw new IllegalArgumentException("Application class name argument not found");
    	}

    	Object appObject = Class.forName(appClassName).newInstance();
    	if (!( appObject instanceof Application )) {
    		throw new IllegalArgumentException(String.format("'%s' is not an Application", appClassName));
    	}

    	return (Application)appObject;
    }

    /**
     * Creates and sets up the main explorer window
     * @param subjectApplication
     * @param content
     * @return
     */
    private Dialog createMainWindow(Application subjectApplication, Component content) {

    	final Dialog dialog = new Dialog(
            	String.format("Pivot Explorer ('%s')", subjectApplication.getClass().getName()),
            	content );
        dialog.setPreferredSize( new Dimensions( 600, 400 ));

        display.getComponentKeyListeners().add(new ComponentKeyListener() {
			public boolean keyPressed(Component component, int keyCode, KeyLocation keyLocation) {
				if (keyCode == KeyCode.E &&
					Keyboard.isPressed(Keyboard.Modifier.CTRL) &&
					Keyboard.isPressed(Keyboard.Modifier.ALT)) {
					dialog.moveToFront();
				}

				return false;
			}

			public boolean keyReleased(Component component, int keyCode, KeyLocation keyLocation) {
			    return false;
			}

			public void keyTyped(Component component, char character) {
			}
		});

        return dialog;

    }


    private void initComponentTree(final TreeView tree, Iterable<Component> components) {

    	tree.getTreeViewSelectionListeners().add(this);

    	//TODO: use preferences to toggle highlighting
    	tree.getComponentMouseListeners().add(new ComponentHighlighter(tree));

        // build tree data
        List<ComponentAdapter> componentList = new ArrayList<ComponentAdapter>();
        for (Component c : components) {
        	if ( c != dialog ) {
        		componentList.add(new ComponentAdapter(c, true));
        	}
        }
        tree.setTreeData(componentList);
        Sequence<Integer> rootPath = Collections.list(0);
        tree.setSelectedPath(rootPath);
        tree.expandBranch(rootPath);
        tree.setNodeRenderer( new ComponentNodeRenderer() );

    }

    public void selectionChanged(TreeView treeView) {
        Sequence<ComponentAdapter> nodePath = TreeNodeList.create(treeView,	treeView.getSelectedPath());
        statusLabel.setText(nodePath.toString());

        if (nodePath.getLength() > 0) {

            ComponentAdapter node = nodePath.get(nodePath.getLength() - 1);

            propertiesTable.setTableData(node.getProperties());
            stylesTable.setTableData(node.getStyles());

            List<TableEntryAdapter> attrs = node.getAttributes();
			attributesTable.setTableData(attrs);
			attributesTab.setDisplayable( attrs.getLength() > 0 );

        } else {
            List<TableEntryAdapter> emptyList = new ArrayList<TableEntryAdapter>(0);
            propertiesTable.setTableData(emptyList);
            stylesTable.setTableData(emptyList);
        }

        PropertyValueTableViewCellRenderer cellRenderer = new PropertyValueTableViewCellRenderer();
		propertiesTable.getColumns().get(1).setCellRenderer( cellRenderer );
        stylesTable.getColumns().get(1).setCellRenderer( cellRenderer );
        attributesTable.getColumns().get(1).setCellRenderer( cellRenderer );

    }

}
