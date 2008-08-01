package pivot.tools.explorer;

import java.util.Locale;
import java.util.ResourceBundle;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.tools.explorer.tree.TreeNodeList;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Label;
import pivot.wtk.TableView;
import pivot.wtk.TreeView;
import pivot.wtk.TreeViewSelectionListener;
import pivot.wtk.Window;
import pivot.wtkx.ComponentLoader;


public class Explorer extends ApplicationAdapter implements TreeViewSelectionListener {

	private ResourceBundle resourceBundle   = ResourceBundle.getBundle( getClass().getName(), Locale.getDefault());
	private ComponentLoader componentLoader = new ComponentLoader();

	private Window    window;
	private TreeView  componentTree;
	private TableView propertiesTable, stylesTable;
	private Label     statusLabel;

	@Override
	public void startup() throws Exception {

		ApplicationContext applicationContext = ApplicationContext.getInstance();
		applicationContext.setTitle(resourceBundle.getString("main.window.name"));

		String className    = getClass().getName().toLowerCase();
        String resourceName = String.format( "%s.wtkx", className.replace('.','/') );

        window = new Window(componentLoader.load( resourceName, className ));
		window.setMaximized(true);
        window.open();

        statusLabel     = (Label)     componentLoader.getComponent("lbStatus");
        componentTree   = (TreeView)  componentLoader.getComponent("trComponents");
        propertiesTable = (TableView) componentLoader.getComponent("tbProperties");
        stylesTable     = (TableView) componentLoader.getComponent("tbStyles");

        initComponentTree( Display.getInstance().getComponents() );
        Component.setFocusedComponent(componentTree);

	}


	@Override
	public void shutdown() throws Exception {
		if ( window != null) window.close();
	}

	private void initComponentTree( Iterable<Component> components ) {
		componentTree.getTreeViewSelectionListeners().add(this);

		// build tree data of component adapters
		List<ComponentAdapter> componentList = new ArrayList<ComponentAdapter>();
		for( Component c:  components) {
			componentList.add( new ComponentAdapter( c, true ));
		}
        componentTree.setTreeData( componentList );

        Sequence<Integer> pathToFirstElement = new ArrayList<Integer>( new Integer[]{new Integer(0)});
		componentTree.setSelectedPath(pathToFirstElement);
        componentTree.expandBranch(pathToFirstElement);
	}

	public void selectionChanged(TreeView treeView) {

		Sequence<ComponentAdapter> nodePath = TreeNodeList.create(treeView, componentTree.getSelectedPath());
		statusLabel.setText( nodePath.toString() );
		if ( nodePath.getLength() > 0 ) {
			ComponentAdapter node = nodePath.get( nodePath.getLength()-1 );
			propertiesTable.setTableData( node.getProperties() );
			stylesTable.setTableData( node.getStyles() );
		}
//		else {
//			ArrayList<Object> emptyList = new ArrayList<Object>();
//			tbProperties.setTableData( emptyList);
//			tbStyles.setTableData( emptyList);
//		}

	}

}
