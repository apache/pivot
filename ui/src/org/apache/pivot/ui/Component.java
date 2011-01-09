/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.beans.PropertyNotFoundException;
import org.apache.pivot.io.SerializationException;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.scene.Group;
import org.apache.pivot.scene.Node;
import org.apache.pivot.util.ObservableMap;
import org.apache.pivot.util.ObservableMapAdapter;

/**
 * Abstract base class for components.
*/
public abstract class Component extends Group {
    // The currently installed skin and associated style adapter
    private Skin skin = null;
    private BeanAdapter styles = null;

    // Tooltip content and delay
    private Node tooltipContent = null;
    private int tooltipDelay = 1000;

    // User data
    private ObservableMap<String, Object> userData = ObservableMapAdapter.observableHashMap();

    // The component's automation ID
    private String automationID;

    // TODO Event listener lists

    // Typed and named styles
    private static HashMap<Class<? extends Component>, Map<String, ?>> typedStyles =
        new HashMap<Class<? extends Component>, Map<String,?>>();
    private static HashMap<String, Map<String, ?>> namedStyles = new HashMap<String, Map<String,?>>();

    /**
     * Returns the currently installed skin.
     *
     * @return
     * The currently installed skin.
     */
    protected Skin getSkin() {
        return skin;
    }

    /**
     * Sets the skin, replacing any previous skin.
     *
     * @param skin
     * The new skin.
     */
    protected void setSkin(Skin skin) {
        if (skin == null) {
            throw new IllegalArgumentException("skin is null.");
        }

        if (this.skin != null) {
            throw new IllegalStateException("Skin is already installed.");
        }

        this.skin = skin;

        // TODO Use a custom inner class that ignores missing property exceptions
        styles = new BeanAdapter(skin);

        skin.install(this);

        // Apply any defined type styles
        LinkedList<Class<?>> styleTypes = new LinkedList<Class<?>>();

        Class<?> type = getClass();
        while (type != Object.class) {
            styleTypes.add(0, type);
            type = type.getSuperclass();
        }

        for (Class<?> styleType : styleTypes) {
            Map<String, ?> styles = typedStyles.get(styleType);

            if (styles != null) {
                setStyles(styles);
            }
        }

        invalidate();
        repaint();
    }

    /**
     * Installs the skin for the given component class, as defined by the current
     * theme.
     *
     * @param componentClass
     */
    @SuppressWarnings("unchecked")
    protected void installSkin(Class<? extends Component> componentClass) {
        // Walk up component hierarchy from this type; if we find a match
        // and the super class equals the given component class, install
        // the skin. Otherwise, ignore - it will be installed later by a
        // subclass of the component class.
        Class<?> type = getClass();

        Theme theme = Theme.getTheme();
        Class<? extends Skin> skinClass =
            theme.getSkinClass((Class<? extends Component>)type);

        while (skinClass == null
            && type != componentClass
            && type != Component.class) {
            type = type.getSuperclass();

            if (type != Component.class) {
                skinClass = theme.getSkinClass((Class<? extends Component>)type);
            }
        }

        if (type == Component.class) {
            throw new IllegalArgumentException(componentClass.getName()
                + " is not an ancestor of " + getClass().getName());
        }

        if (skinClass == null) {
            throw new IllegalArgumentException("No skin mapping for "
                + componentClass.getName() + " found.");
        }

        if (type == componentClass) {
            try {
                setSkin(skinClass.newInstance());
            } catch(InstantiationException exception) {
                throw new IllegalArgumentException(exception);
            } catch(IllegalAccessException exception) {
                throw new IllegalArgumentException(exception);
            }
        }
    }

    /**
     * Returns the component's style map. Style names correspond to the Java
     * bean properties of the skin.
     * <p>
     * An attempt to set a non-existent style will produce a warning. This
     * allows an application to be dynamically themed without risk of failure
     * due to a {@link PropertyNotFoundException}.
     */
    public ObservableMap<String, Object> getStyles() {
        return styles;
    }

    /**
     * Applies a set of styles.
     *
     * @param styles
     * A map containing the styles to apply.
     */
    public void setStyles(Map<String, ?> styles) {
        if (styles == null) {
            throw new IllegalArgumentException("styles is null.");
        }

        for (Map.Entry<String, ?> entry : styles.entrySet()) {
            getStyles().put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Applies a set of named styles.
     *
     * @param styleName
     */
    public void setStyleName(String styleName) {
        if (styleName == null) {
            throw new IllegalArgumentException();
        }

        Map<String, ?> styles = namedStyles.get(styleName);

        if (styles == null) {
            System.err.println("Named style \"" + styleName + "\" does not exist.");
        } else {
            setStyles(styles);
        }
    }

    /**
     * Applies a set of named styles.
     *
     * @param styleNames
     */
    public void setStyleNames(String styleNames) {
        if (styleNames == null) {
            throw new IllegalArgumentException();
        }

        String[] styleNameArray = styleNames.split(",");

        for (int i = 0; i < styleNameArray.length; i++) {
            String styleName = styleNameArray[i];
            setStyleName(styleName.trim());
        }
    }

    /**
     * Returns the component's automation ID.
     *
     * @return
     * The component's automation ID, or <tt>null</tt> if the component does not
     * have an automation ID.
     */
    public String getAutomationID() {
        return automationID;
    }

    /**
     * Sets the component's automation ID. This value can be used to obtain a
     * reference to the component via {@link Automation#get(String)} when the
     * component is attached to a component hierarchy.
     *
     * @param automationID
     * The automation ID to use for the component, or <tt>null</tt> to clear the
     * automation ID.
     */
    public void setAutomationID(String automationID) {
        String previousAutomationID = this.automationID;
        this.automationID = automationID;

        if (getStage() != null) {
            if (previousAutomationID != null) {
                Automation.remove(previousAutomationID);
            }

            if (automationID != null) {
                Automation.add(automationID, this);
            }
        }
    }

    public Window getWindow() {
        return (Window)getAncestor(Window.class);
    }

    /**
     * Returns the component's tooltip content.
     *
     * @return
     * The component's tooltip content, or <tt>null</tt> if no tooltip is
     * specified.
     */
    public Node getTooltipContent() {
        return tooltipContent;
    }

    /**
     * Sets the component's tooltip content.
     *
     * @param tooltipContents
     * The component's tooltip content, or <tt>null</tt> for no tooltip.
     */
    public void setTooltipContent(Node tooltipContent) {
        Node previousTooltipContent = this.tooltipContent;

        if (previousTooltipContent != tooltipContent) {
            this.tooltipContent = tooltipContent;

            // TODO Fire event
        }
    }

    /**
     * Returns the component's tooltip delay.
     *
     * @return
     * The tooltip delay, in milliseconds.
     */
    public int getTooltipDelay() {
        return tooltipDelay;
    }

    /**
     * Sets the component's tooltip delay.
     *
     * @param tooltipDelay
     * The tooltip delay, in milliseconds.
     */
    public void setTooltipDelay(int tooltipDelay) {
        int previousTooltipDelay = this.tooltipDelay;

        if (previousTooltipDelay != tooltipDelay) {
            this.tooltipDelay = tooltipDelay;

            // TODO Fire event
        }
    }

    public ObservableMap<String, Object> getUserData() {
        return userData;
    }

    /**
     * Copies bound values from the bind context to the component. This
     * functionality must be provided by the subclass; the base implementation
     * is a no-op.
     *
     * @param context
     */
    public void load(Object context) {
        // TODO IMPORTANT If we don't have a Container class, we'll need to implement
        // load() and store() in a lot more places
    }

    /**
     * Copies bound values from the component to the bind context. This
     * functionality must be provided by the subclass; the base implementation
     * is a no-op.
     *
     * @param context
     */
    public void store(Object context) {
    }

    /**
     * Clears any bound values in the component.
     */
    public void clear() {
    }

    @Override
    public String toString() {
        String s = super.toString();

        if (automationID != null) {
            s += "#" + automationID;
        }

        return s;
    }

    /**
     * Returns the typed style dictionary.
     */
    public static Map<Class<? extends Component>, Map<String, ?>> getTypedStyles() {
        return typedStyles;
    }

    /**
     * Returns the named style dictionary.
     */
    public static Map<String, Map<String, ?>> getNamedStyles() {
        return namedStyles;
    }

    /**
     * Adds the styles from a named stylesheet to the named or typed style collections.
     *
     * @param resourceName
     */
    @SuppressWarnings("unchecked")
    public static void appendStylesheet(String resourceName) {
        // TODO Use .properties files with JSON map values rather than .json files

        ClassLoader classLoader = Component.class.getClassLoader();

        URL stylesheetLocation = classLoader.getResource(resourceName.substring(1));
        if (stylesheetLocation == null) {
            throw new RuntimeException("Unable to locate style sheet resource \"" + resourceName + "\".");
        }

        try {
            InputStream inputStream = stylesheetLocation.openStream();

            try {
                JSONSerializer serializer = new JSONSerializer();
                Map<String, Map<String, ?>> stylesheet =
                    (Map<String, Map<String, ?>>)serializer.readObject(inputStream);

                for (Map.Entry<String, Map<String, ?>> entry : stylesheet.entrySet()) {
                    String name = entry.getKey();
                    Map<String, ?> styles = entry.getValue();

                    int i = name.lastIndexOf('.') + 1;
                    if (Character.isUpperCase(name.charAt(i))) {
                        // Assume the current package if none specified
                        if (!name.contains(".")) {
                            name = Component.class.getPackage().getName() + "." + name;
                        }

                        Class<?> type = null;
                        try {
                            type = Class.forName(name);
                        } catch (ClassNotFoundException exception) {
                            // No-op
                        }

                        if (type != null
                            && Component.class.isAssignableFrom(type)) {
                            Component.getTypedStyles().put((Class<? extends Component>)type, styles);
                        }
                    } else {
                        Component.getNamedStyles().put(name, styles);
                    }
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }
    }
}
