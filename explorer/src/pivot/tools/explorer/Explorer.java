package pivot.tools.explorer;

import java.util.Locale;
import java.util.ResourceBundle;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.tools.explorer.tree.TreeViewUtils;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
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
	private Component content;
	private TreeView  trComponents;
	private TableView tbProperties, tbStyles;
	private Label     lbStatus;
	
	@Override
	public void startup() throws Exception {
				
		ApplicationContext applicationContext = ApplicationContext.getInstance();
		applicationContext.setTitle(resourceBundle.getString("main.window.name"));
		
		String resourceName     = getClass().getName().toLowerCase();
        String wtkxResourceName = String.format( "%s.wtkx", resourceName.replace('.','/') );
		content = componentLoader.load( wtkxResourceName, resourceName );

        window = new Window(content);
		window.setMaximized(true); 
        window.open();
		
        lbStatus     = (Label)     componentLoader.getComponent("lbStatus");
        trComponents = (TreeView)  componentLoader.getComponent("trComponents");
        tbProperties = (TableView) componentLoader.getComponent("tbProperties");
        tbStyles     = (TableView) componentLoader.getComponent("tbStyles");
        
        initComponentTree();
        Component.setFocusedComponent(trComponents);
        
	}

	@Override
	public void shutdown() throws Exception {
		if ( window != null) window.close();
	}

	private void initComponentTree() {
		trComponents.getTreeViewSelectionListeners().add(this);
        trComponents.setTreeData(oneItemList( new ComponentAdapter(content, true)) );
        List<Integer> pathToFirstElement = oneItemList(0);
		trComponents.setSelectedPath(pathToFirstElement);
        trComponents.expandBranch(pathToFirstElement);
	}
	
	private <T> List<T> oneItemList( T item ) {
		List<T> lst = new ArrayList<T>();
		lst.add( item );
		return lst;
	}
	
	public void selectionChanged(TreeView treeView) {
		
		Sequence<Integer> indexPath = trComponents.getSelectedPath();
		lbStatus.setText( TreeViewUtils.getStringPath(trComponents, indexPath));
		
		Sequence<ComponentAdapter> nodePath = TreeViewUtils.getNodePath(trComponents, indexPath);
		if ( nodePath.getLength() > 0 ) {
			ComponentAdapter node = nodePath.get( nodePath.getLength()-1 );
			tbProperties.setTableData( node.getProperties() );
			tbStyles.setTableData( node.getStyles() );
		}
//		else {
//			ArrayList<Object> emptyList = new ArrayList<Object>();
//			tbProperties.setTableData( emptyList);
//			tbStyles.setTableData( emptyList);
//		}
		
	}

}
