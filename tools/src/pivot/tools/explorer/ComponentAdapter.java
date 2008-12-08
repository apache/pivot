package pivot.tools.explorer;

import java.net.URL;
import java.util.Iterator;

import pivot.beans.BeanDictionary;
import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.wtk.Component;
import pivot.wtk.Component.Attributes;
import pivot.wtk.Component.StyleDictionary;

import pivot.wtk.ApplicationContext;
import pivot.wtk.media.Image;

public class ComponentAdapter {
    private Component component;
    private List<TableEntryAdapter> properties, styles, attributes;

    public ComponentAdapter(Component component) {
        if (component == null) {
            throw new IllegalArgumentException( "Component cannot be null");
        }

        this.component = component;
    }

    public Component getComponent() {
        return component;
    }

    public List<TableEntryAdapter> getProperties() {
        if (properties == null) {

            BeanDictionary beanDictionary = new BeanDictionary(component, true);
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

            attributes = new ArrayList<TableEntryAdapter>(TableEntryAdapter.COMPARATOR);
            Attributes attrs = component.getAttributes();
            if (attrs != null) {
                BeanDictionary beanDictionary = new BeanDictionary(attrs);
                for ( String s: beanDictionary ) {
                    attributes.add( new TableEntryAdapter(beanDictionary, s));
                }
            }


        }
        return attributes;
    }


    @Override
    public String toString() {
        return component.getClass().getSimpleName();
    }

    public Image getIcon() {
        // TODO Load component icon from application resources
        URL iconURL = null;

        Image icon = null;

        if (iconURL != null) {
            icon = (Image)ApplicationContext.getResourceCache().get(iconURL);
            if (icon == null) {
                icon = Image.load(iconURL);
                ApplicationContext.getResourceCache().put(iconURL, icon);
            }
        }

        return icon;
    }

    public String getText() {
        return toString() + (component.isDisplayable()? "": " (hidden)");
    }
}
