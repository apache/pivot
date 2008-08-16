package pivot.tools.explorer;

import java.net.URL;
import java.util.Iterator;

import pivot.beans.BeanDictionary;
import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.wtk.Component;
import pivot.wtk.ComponentInfo;
import pivot.wtk.Container;
import pivot.wtk.Component.Attributes;
import pivot.wtk.Component.StyleDictionary;
import pivot.wtk.content.TreeViewNodeRenderer;

public class ComponentAdapter
    extends ArrayList<ComponentAdapter>
    implements Dictionary<String,Object> {

	private static final long serialVersionUID = 1L;

	private Component component;
	private List<TableEntryAdapter> properties, styles, attributes;
	

	public ComponentAdapter( Component component, boolean buildHierarchy ) {
		super();

		if ( component == null ) {
			throw new IllegalArgumentException( "Component cannot be null");
		}

		this.component = component;


		if ( buildHierarchy && component instanceof Container ) {
			for ( Component c: ((Container)component) ) {
				add( new ComponentAdapter( c, true ));
			}
		}

	}

	public Component getComponent() {
		return component;
	}

	public List<TableEntryAdapter> getProperties() {
		if (properties == null) {
			
			BeanDictionary beanDictionary = new BeanDictionary(component);
			properties = new ArrayList<TableEntryAdapter>( TableEntryAdapter.COMPARATOR );

			for ( String s: beanDictionary ) {
				properties.add( new TableEntryAdapter( beanDictionary, s ));
			}


		}
		return properties;
	}

	public List<TableEntryAdapter> getStyles() {
		if (styles == null) {
			styles = new ArrayList<TableEntryAdapter>( TableEntryAdapter.COMPARATOR );
			StyleDictionary sd = component.getStyles();
			Iterator<String> i = sd.iterator();
			while( i.hasNext() ) {
				styles.add( new TableEntryAdapter( component.getStyles(), i.next() ));
			}
		}
		return styles;
	}
	
	public List<TableEntryAdapter> getAttributes() {
		if (attributes == null) {

			attributes = new ArrayList<TableEntryAdapter>( TableEntryAdapter.COMPARATOR );
			Attributes attrs = component.getAttributes();
			if (attrs != null) {
				BeanDictionary beanDictionary = new BeanDictionary(attrs);
				for ( String s: beanDictionary ) {
					attributes.add( new TableEntryAdapter( beanDictionary, s ));
				}
			}


		}
		return attributes;
	}


	@Override
	public String toString() {
		return component.getClass().getSimpleName();
	}

	public boolean containsKey(String key) {
		return TreeViewNodeRenderer.ICON_KEY.equals(key) ||
		       TreeViewNodeRenderer.LABEL_KEY.equals(key);
	}

	private URL url;

	public Object get(String key) {

		if ( TreeViewNodeRenderer.LABEL_KEY.equals(key) ) {
			return toString();
		} else if ( TreeViewNodeRenderer.ICON_KEY.equals(key) ){
			if ( url == null ) {
			    ComponentInfo componentInfo = component.getClass().getAnnotation( ComponentInfo.class );
				url = component.getClass().getResource( componentInfo != null? componentInfo.icon():"component.png");
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
