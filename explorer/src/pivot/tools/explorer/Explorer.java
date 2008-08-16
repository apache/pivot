package pivot.tools.explorer;

import java.util.Locale;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.tools.explorer.tools.Collections;
import pivot.tools.explorer.tree.TreeNodeList;
import pivot.util.Resources;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Label;
import pivot.wtk.TableView;
import pivot.wtk.TreeView;
import pivot.wtk.TreeViewSelectionListener;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class Explorer extends ApplicationAdapter implements TreeViewSelectionListener {
    private Resources resources;
    private WTKXSerializer wtkxSerializer;

    private Window window;
    private TreeView componentTree;
    private TableView propertiesTable, stylesTable, attributesTable;
    private Label statusLabel;
    private Component attributesTab;

    @Override
    public void startup() throws Exception {

    	String className = getClass().getName().toLowerCase();
        resources = new Resources(className, Locale.getDefault());
        wtkxSerializer = new WTKXSerializer(resources);

        ApplicationContext applicationContext = ApplicationContext.getInstance();
        applicationContext.setTitle((String) resources.get("mainWindowName"));

        String resourceName = String.format("%s.wtkx", className.replace('.', '/'));

        window = new Window((Component) wtkxSerializer.readObject(resourceName));
        window.setMaximized(true);
        window.open();

        statusLabel = (Label) wtkxSerializer.getObjectByName("lbStatus");
        componentTree = (TreeView) wtkxSerializer.getObjectByName("trComponents");
        propertiesTable = (TableView) wtkxSerializer.getObjectByName("tbProperties");
        stylesTable = (TableView) wtkxSerializer.getObjectByName("tbStyles");
        attributesTable = (TableView) wtkxSerializer.getObjectByName("tbAttributes");
        attributesTab = (Component)wtkxSerializer.getObjectByName("tabAttributes");

        initComponentTree(Display.getInstance());
        Component.setFocusedComponent(componentTree);
    }

    @Override
    public void shutdown() throws Exception {
        if (window != null)
            window.close();
    }

    private void initComponentTree(Iterable<Component> components) {
        componentTree.getTreeViewSelectionListeners().add(this);

//        attributesTab.setEnabled( false );
        
        // build tree data
        List<ComponentAdapter> componentList = new ArrayList<ComponentAdapter>();
        for (Component c : components) {
            componentList.add(new ComponentAdapter(c, true));
        }
        componentTree.setTreeData(componentList);
        Sequence<Integer> rootPath = Collections.list(0);
        componentTree.setSelectedPath(rootPath);
        componentTree.expandBranch(rootPath);
        componentTree.setNodeRenderer( new ComponentNodeRenderer() );
        
    }

    public void selectionChanged(TreeView treeView) {
        Sequence<ComponentAdapter> nodePath = TreeNodeList.create(treeView,
            componentTree.getSelectedPath());

        statusLabel.setText(nodePath.toString());

        
        if (nodePath.getLength() > 0) {
            ComponentAdapter node = nodePath.get(nodePath.getLength() - 1);
            propertiesTable.setTableData(node.getProperties());
            stylesTable.setTableData(node.getStyles());
            
            List<TableEntryAdapter> attrs = node.getAttributes();
			attributesTable.setTableData(attrs);
			attributesTab.setDisplayable( attrs.getLength() > 0 );
            
            
        } else {
            List<TableEntryAdapter> emptyList = Collections.emptyList();
            propertiesTable.setTableData(emptyList);
            stylesTable.setTableData(emptyList);
        }
    }
}
