package pivot.tools.explorer;

import java.util.Locale;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.tools.explorer.tree.TreeNodeList;
import pivot.tools.explorer.utils.Collections;
import pivot.util.Resources;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.Dialog;
import pivot.wtk.DialogCloseHandler;
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

    private Application application;

    private Dialog dialog;
    private TreeView componentTree;
    private TableView propertiesTable, stylesTable, attributesTable;
    private Label statusLabel;
    private Component attributesTab;

    public void startup() throws Exception {

    	Application application = getSubjectApplication();
    	application.startup();

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

        initComponentTree(componentTree, Display.getInstance());

        dialog.open();
        Component.setFocusedComponent(componentTree);

    }

    public void shutdown() throws Exception {
        dialog.close();
    	if (application != null) application.shutdown();
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

    	String appClassName = ApplicationContext.getInstance().getProperty("applicationClassName");
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

        dialog.setDialogCloseHandler( new DialogCloseHandler(){
    			public boolean close(Dialog dialog, boolean result) {
    				dialog.moveToBack();
    				return false;
 		}});

        Display.getInstance().getComponentKeyListeners().add(new ComponentKeyListener() {

			public void keyPressed(Component component, int keyCode, KeyLocation keyLocation) {

				if (keyCode == KeyCode.E &&
					Keyboard.isPressed(Keyboard.Modifier.CTRL) &&
					Keyboard.isPressed(Keyboard.Modifier.ALT)) {
					dialog.moveToFront();
				}
			}

			public void keyReleased(Component component, int keyCode, KeyLocation keyLocation) {
			}

			public void keyTyped(Component component, char character) {
			}
		});

        return dialog;

    }

    private void initComponentTree(TreeView tree, Iterable<Component> components) {
    	tree.getTreeViewSelectionListeners().add(this);

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
    }

}
