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
package pivot.wtkx;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import pivot.collections.ArrayList;
import pivot.collections.HashMap;
import pivot.collections.List;
import pivot.collections.Map;
import pivot.serialization.JSONSerializer;
import pivot.wtk.Component;
import pivot.wtk.Cursor;

/**
 * Bootstrap component loader.
 *
 * TODO Add support for namedStyles attribute.
 *
 * @author gbrown
 */
public class ComponentLoader extends Loader {
    private ComponentLoader parent = null;
    private String namespace = null;
    private Locale locale = null;

    private URL baseURL = null;
    private ResourceBundle resourceBundle = null;

    private ArrayList<Loader> loaders = new ArrayList<Loader>();

    private HashMap<String, Component> components = new HashMap<String, Component>();
    private HashMap<String, ComponentLoader> componentLoaders = new HashMap<String, ComponentLoader>();

    private static final HashMap<String, Class<? extends Loader>> loaderClasses =
        new HashMap<String, Class<? extends Loader>>();

    public static final String URL_PREFIX = "@";
    public static final String RESOURCE_KEY_PREFIX = "%";

    public static final String COMPONENT_TAG = "Component";
    public static final String COMPONENT_SRC_ATTRIBUTE = "src";
    public static final String COMPONENT_NAMESPACE_ATTRIBUTE = "namespace";
    public static final String COMPONENT_RESOURCE_BUNDLE_ATTRIBUTE = "resourceBundle";

    public static final String ID_ATTRIBUTE = "id";
    public static final String PREFERRED_WIDTH_ATTRIBUTE = "preferredWidth";
    public static final String PREFERRED_HEIGHT_ATTRIBUTE = "preferredHeight";
    public static final String DISPLAYABLE_ATTRIBUTE = "displayable";
    public static final String ENABLED_ATTRIBUTE = "enabled";
    public static final String CURSOR_ATTRIBUTE = "cursor";
    public static final String TOOLTIP_TEXT_ATTRIBUTE = "tooltipText";

    public static final String STYLES_ATTRIBUTE = "styles";

    public ComponentLoader() {
        this(null, null, Locale.getDefault());
    }

    public ComponentLoader(Locale locale) {
        this(null, null, locale);
    }

    private ComponentLoader(ComponentLoader parent, String namespace, Locale locale) {
        this.parent = parent;
        this.namespace = namespace;
        this.locale = locale;
    }

    public ComponentLoader getParent() {
        return parent;
    }

    public String getNamepsace() {
        return namespace;
    }

    public String getQualifiedNamepsace() {
        String qualifiedNamespace = this.namespace;

        if (qualifiedNamespace != null) {
            ComponentLoader ancestor = parent;

            while (ancestor != null) {
                if (ancestor.namespace != null) {
                    qualifiedNamespace = ancestor.namespace + "." + qualifiedNamespace;
                }

                ancestor = ancestor.parent;
            }
        }

        return qualifiedNamespace;
    }

    public Locale getLocale() {
        return locale;
    }

    public URL getResource(String name) {
        URL location = null;

        try {
            if (name.startsWith("/")) {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                location = classLoader.getResource(name.substring(1));
            } else {
                location = new URL(baseURL, name);
            }
        } catch(MalformedURLException exception) {
            // No-op
        }

        return location;
    }

    public Object getResourceObject(String key) {
        Object object = null;

        if (resourceBundle != null) {
            try {
                object = resourceBundle.getObject(key);
            } catch(MissingResourceException exception) {
                // No-op
            }
        }

        if (object == null) {
            if (parent == null) {
                throw new IllegalStateException("No resource bundle specified.");
            }

            object = parent.getResourceObject(key);
        }

        return object;
    }

    public String getResourceString(String key) {
        String string = null;

        if (resourceBundle != null) {
            try {
                string = resourceBundle.getString(key);
            } catch(MissingResourceException exception) {
                // No-op
            }
        }

        if (string == null) {
            if (parent != null) {
                string = parent.getResourceString(key);
            }
        }

        return string;
    }

    @SuppressWarnings("unchecked")
    public Object resolve(Object source) {
        Object resolved = null;

        if (source instanceof String) {
            String string = (String)source;

            if (string.startsWith(RESOURCE_KEY_PREFIX)) {
                resolved = getResourceString(string.substring(1));
            } else if (string.startsWith(URL_PREFIX)) {
                resolved = getResource(string.substring(1));
            } else {
                resolved = string;
            }
        } else {
            resolved = source;

            if (source instanceof List<?>) {
                List<Object> list = (List<Object>)source;

                for (int i = 0, n = list.getLength(); i < n; i++) {
                    list.update(i, resolve(list.get(i)));
                }
            }

            if (source instanceof Map<?, ?>) {
                Map<String, Object> map = (Map<String, Object>)source;

                for (String key : map) {
                    map.put(key, resolve(map.get(key)));
                }
            }
        }

        return resolved;
    }

    /**
     * Loads a component from an XML document.
     *
     * @param resourceName
     * The resource name of the source document.
     *
     * @return
     * The loaded component.
     */
    public Component load(String resourceName) throws LoadException {
        return load(resourceName, null);
    }

    /**
     * Loads a component from an XML document.
     *
     * @param resourceName
     * The resource name of the source document. This is a fully-qualified
     * resource name without a leading slash character ('/').
     *
     * @param resourceBundleBaseName
     * The base name of the resource bundle to use when performing resource key
     * substitution; may be <tt>null</tt>.
     *
     * @return
     * The loaded component.
     */
    public Component load(String resourceName, String resourceBundleBaseName)
        throws LoadException {
        ResourceBundle resourceBundle = (resourceBundleBaseName == null) ?
            null : ResourceBundle.getBundle(resourceBundleBaseName, locale);

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resourceURL = classLoader.getResource(resourceName);
        if (resourceURL == null) {
            throw new LoadException("Could not find resource named \""
                + resourceName + "\".");
        }

        return load(resourceURL, resourceBundle);
    }

    /**
     * Loads a component from an XML document.
     *
     * @param resourceURL
     * The location of the source document.
     *
     * @param resourceBundle
     * The resource bundle to use when performing resource key substitution;
     * may be null.
     *
     * @return
     * The loaded component.
     */
    private Component load(URL resourceURL, ResourceBundle resourceBundle) throws LoadException {
        if (resourceURL == null) {
            throw new IllegalArgumentException("resourceURL is null.");
        }

        Element element = null;

        DocumentBuilder builder;
        DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        factory.setIgnoringComments(true);

        baseURL = null;
        this.resourceBundle = null;

        loaders.clear();
        components.clear();
        componentLoaders.clear();

        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(resourceURL.toString());
            element = document.getDocumentElement();
            baseURL = new URL(resourceURL.getProtocol(), resourceURL.getHost(),
                resourceURL.getPort(), resourceURL.getPath());
            this.resourceBundle = resourceBundle;
        } catch (Exception exception) {
            throw new LoadException("Unable to parse XML source.", exception);
        }

        return load(element, this);
    }

    /**
     * Loads a component from an XML element. Maps the element's tag name to an
     * appropriate loader class; instantiates the loader and uses it to load
     * the component.
     *
     * @param element
     * The root element of the component to load.
     *
     * @param rootLoader
     * The class loader used to load this element's parent document.
     */
    @SuppressWarnings("unchecked")
    protected Component load(Element element, ComponentLoader rootLoader)
        throws LoadException {
        Component component = null;

        String tagName = element.getTagName();

        if (tagName.equals(COMPONENT_TAG)) {
            // Get the source attribute
            String src = element.getAttribute(COMPONENT_SRC_ATTRIBUTE);

            // Construct the URL for the component's source document
            URL componentURL = rootLoader.getResource(src);

            String namespace = element.getAttribute(COMPONENT_NAMESPACE_ATTRIBUTE);
            if (namespace.length() == 0) {
                throw new LoadException("A namespace attribute is required for subcomponents.");
            }

            if (namespace.contains(".")) {
                throw new LoadException("Namespace names may not contain \".\" characters.");
            }

            // Create a new namespace
            if (componentLoaders.containsKey(namespace)) {
                throw new LoadException("Namespace " + namespace + " is already in use.");
            }

            // Load the resource bundle for the component
            ResourceBundle componentResourceBundle = null;

            if (element.hasAttribute(COMPONENT_RESOURCE_BUNDLE_ATTRIBUTE)) {
                String resourceBundleAttribute = element.getAttribute(COMPONENT_RESOURCE_BUNDLE_ATTRIBUTE);
                componentResourceBundle = ResourceBundle.getBundle(resourceBundleAttribute, locale);
            }

            ComponentLoader componentLoader = new ComponentLoader(rootLoader, namespace, locale);
            rootLoader.componentLoaders.put(namespace, componentLoader);

            component = componentLoader.load(componentURL, componentResourceBundle);
        } else {
            // Retrieve the loader class for this tag
            Class<? extends Loader> loaderClass = loaderClasses.get(tagName);

            if (loaderClass == null) {
                throw new LoadException("No loader mapping found for " + tagName + ".");
            }

            try {
                // Create the loader and add it to the loaders list; this will
                // ensure that it is not garbage collected until all components
                // in this branch have been loaded
                Loader loader = loaderClass.newInstance();
                loaders.add(loader);

                // Load the component
                component = loader.load(element, rootLoader);

                if (element.hasAttribute(ID_ATTRIBUTE)) {
                    String id = element.getAttribute(ID_ATTRIBUTE);

                    if (components.containsKey(id)) {
                        throw new LoadException("Component ID " + id + " is already in use.");
                    }

                    rootLoader.components.put(id, component);
                }

                if (element.hasAttribute(PREFERRED_WIDTH_ATTRIBUTE)) {
                    int preferredWidth = Integer.parseInt(element.getAttribute(PREFERRED_WIDTH_ATTRIBUTE));
                    component.setPreferredWidth(preferredWidth);
                }

                if (element.hasAttribute(PREFERRED_HEIGHT_ATTRIBUTE)) {
                    int preferredHeight = Integer.parseInt(element.getAttribute(PREFERRED_HEIGHT_ATTRIBUTE));
                    component.setPreferredHeight(preferredHeight);
                }

                if (element.hasAttribute(DISPLAYABLE_ATTRIBUTE)) {
                    boolean displayable = Boolean.parseBoolean(element.getAttribute(DISPLAYABLE_ATTRIBUTE));
                    component.setDisplayable(displayable);
                }

                if (element.hasAttribute(ENABLED_ATTRIBUTE)) {
                    boolean enabled = Boolean.parseBoolean(element.getAttribute(ENABLED_ATTRIBUTE));
                    component.setEnabled(enabled);
                }

                if (element.hasAttribute(CURSOR_ATTRIBUTE)) {
                    String cursorAttribute = element.getAttribute(CURSOR_ATTRIBUTE);
                    Cursor cursor = Cursor.decode(cursorAttribute);
                    component.setCursor(cursor);
                }

                if (element.hasAttribute(TOOLTIP_TEXT_ATTRIBUTE)) {
                    String tooltipTextAttribute = element.getAttribute(TOOLTIP_TEXT_ATTRIBUTE);
                    component.setTooltipText(rootLoader.resolve(tooltipTextAttribute).toString());
                }

                if (element.hasAttribute(STYLES_ATTRIBUTE)) {
                    String stylesAttribute = element.getAttribute(STYLES_ATTRIBUTE);

                    StringReader stylesReader = null;
                    try {
                        stylesReader = new StringReader(stylesAttribute);
                        JSONSerializer jsonSerializer = new JSONSerializer();
                        Map<String, Object> styles = (Map<String, Object>)jsonSerializer.readObject(stylesReader);

                        for (String key : styles) {
                            component.getStyles().put(key, styles.get(key));
                        }
                    } catch (Exception exception) {
                        System.out.println("Unable to apply styles for " + tagName
                            + ": " + exception.getMessage());
                    } finally {
                        if (stylesReader != null) {
                            stylesReader.close();
                        }
                    }
                }
            } catch (Exception exception) {
                throw new LoadException("Unable to load component " + tagName + ".", exception);
            }
        }

        return component;
    }

    /**
     * Retrieves the component loader that loaded the specified namespace.
     *
     * @param name
     * The name of the component loader, relative to this loader. The loader's
     * name is the concatentation of its parent namespaces and its namespace,
     * separated by period characters (e.g. "foo.bar.baz").
     *
     * @return
     * The named component loader, or <tt>null</tt> if a loader with the given
     * name does not exist.
     */
    public ComponentLoader getComponentLoader(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null.");
        }

        ComponentLoader componentLoader = this;
        String[] namespacePath = name.split("\\.");

        for (int i = 0; i < namespacePath.length && componentLoader != null; i++) {
            String namespace = namespacePath[i];
            componentLoader = componentLoader.componentLoaders.get(namespace);
        }

        return componentLoader;
    }

    /**
     * Retrieves a named component.
     *
     * @param name
     * The name of the component, relative to this loader. The component's name
     * is the concatentation of its parent namespaces and its ID, separated by
     * period characters (e.g. "foo.bar.baz").
     *
     * @return
     * The named component, or <tt>null</tt> if a component with the given
     * name does not exist.
     */
    public Component getComponent(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null.");
        }

        Component component = null;
        ComponentLoader componentLoader = ComponentLoader.this;

        String[] namespacePath = name.split("\\.");
        int i = 0;
        int n = namespacePath.length - 1;

        while (i < n
            && componentLoader != null) {
            String namespace = namespacePath[i];
            componentLoader = componentLoader.componentLoaders.get(namespace);
            i++;
        }

        if (componentLoader != null) {
            component = componentLoader.components.get(namespacePath[i]);
        }

        return component;
    }

    static {
        loaderClasses.put(BorderLoader.BORDER_TAG, BorderLoader.class);
        loaderClasses.put(CardPaneLoader.CARD_PANE_TAG, CardPaneLoader.class);
        loaderClasses.put(CheckboxLoader.CHECKBOX_TAG, CheckboxLoader.class);
        loaderClasses.put(ExpanderLoader.EXPANDER_TAG, ExpanderLoader.class);
        loaderClasses.put(FlowPaneLoader.FLOW_PANE_TAG, FlowPaneLoader.class);
        loaderClasses.put(FormLoader.FORM_TAG, FormLoader.class);
        loaderClasses.put(ImageViewLoader.IMAGE_VIEW_TAG, ImageViewLoader.class);
        loaderClasses.put(LabelLoader.LABEL_TAG, LabelLoader.class);
        loaderClasses.put(LinkButtonLoader.LINK_BUTTON_TAG, LinkButtonLoader.class);
        loaderClasses.put(ListButtonLoader.LIST_BUTTON_TAG, ListButtonLoader.class);
        loaderClasses.put(ListViewLoader.LIST_VIEW_TAG, ListViewLoader.class);
        loaderClasses.put(MeterLoader.METER_TAG, MeterLoader.class);
        loaderClasses.put(PushButtonLoader.PUSH_BUTTON_TAG, PushButtonLoader.class);
        loaderClasses.put(RadioButtonLoader.RADIO_BUTTON_TAG, RadioButtonLoader.class);
        loaderClasses.put(RollupLoader.ROLLUP_TAG, RollupLoader.class);
        loaderClasses.put(ScrollPaneLoader.SCROLL_PANE_TAG, ScrollPaneLoader.class);
        loaderClasses.put(SpacerLoader.SPACER_TAG, SpacerLoader.class);
        loaderClasses.put(SplitPaneLoader.SPLIT_PANE_TAG, SplitPaneLoader.class);
        loaderClasses.put(SpinnerLoader.SPINNER_TAG, SpinnerLoader.class);
        loaderClasses.put(StackPaneLoader.STACK_PANE_TAG, StackPaneLoader.class);
        loaderClasses.put(TabPaneLoader.TAB_PANE_TAG, TabPaneLoader.class);
        loaderClasses.put(TablePaneLoader.TABLE_PANE_TAG, TablePaneLoader.class);
        loaderClasses.put(TableViewLoader.TABLE_VIEW_TAG, TableViewLoader.class);
        loaderClasses.put(TableViewHeaderLoader.TABLE_VIEW_HEADER_TAG, TableViewHeaderLoader.class);
        loaderClasses.put(TextInputLoader.TEXT_INPUT_TAG, TextInputLoader.class);
        loaderClasses.put(TreeViewLoader.TREE_VIEW_TAG, TreeViewLoader.class);
    }
}
