package pivot.tools.explorer;

import java.net.URL;
import java.util.Iterator;

import pivot.beans.BeanInfo;
import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.tools.explorer.properties.AbstractTableEntryAdapter;
import pivot.wtk.Component;
import pivot.wtk.Container;
import pivot.wtk.Component.StyleDictionary;
import pivot.wtk.content.TreeViewNodeRenderer;

public class ComponentAdapter 
    extends ArrayList<ComponentAdapter> 
    implements Dictionary<String,Object> { 
		
	private Component component;
	private List<AbstractTableEntryAdapter> properties, styles;
	
	public ComponentAdapter( Component component, boolean buildHierarchy ) {
		super();
		
		if ( component == null ) {
			throw new IllegalArgumentException( "Component cannot be null");
		}
		
		this.component = component;
		
		if ( buildHierarchy && component instanceof Container ) {
			for ( Component c: ((Container)component).getComponents()) {
				add( new ComponentAdapter( c, true ));
			}
		}
		
	}

	public Component getComponent() {
		return component;
	}
	
	public List<AbstractTableEntryAdapter> getProperties() {
		if (properties == null) {
			properties = new ArrayList<AbstractTableEntryAdapter>();
			for ( String s: component.getProperties() ) {
				properties.add( new PropertyTableEntryAdapter( component, s ));
			}
			
		}
		return properties;
	}
	
	public List<AbstractTableEntryAdapter> getStyles() {
		if (styles == null) {
			styles = new ArrayList<AbstractTableEntryAdapter>();
			StyleDictionary sd = component.getStyles();
			Iterator<String> i = sd.iterator();
			while( i.hasNext() ) {
				styles.add( new StyleTableEntryAdapter( component, i.next() ));
			}
		}
		return styles;
	}

	
	@Override
	public String toString() {
		return component.getClass().getSimpleName();
	}

	public boolean containsKey(String key) {
		return TreeViewNodeRenderer.ICON_URL_KEY.equals(key) ||
		       TreeViewNodeRenderer.LABEL_KEY.equals(key);
	}

	private URL url;
	
	public Object get(String key) {
		
		if ( TreeViewNodeRenderer.LABEL_KEY.equals(key) ) {
			return toString();
		} else if ( TreeViewNodeRenderer.ICON_URL_KEY.equals(key) ){
			if ( url == null ) {
				BeanInfo beanInfo = component.getClass().getAnnotation( BeanInfo.class );
				url = component.getClass().getResource( beanInfo != null? beanInfo.icon():"component.png");
			}
			return url;
		} else {
			return null;
		}
		
	}

	public boolean isEmpty() {
		return false;
	}

	public Object put(String key, Object value) {
		return null;
	}

	public Object remove(String key) {
		return null;
	}


}
