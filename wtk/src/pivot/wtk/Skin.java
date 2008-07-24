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
package pivot.wtk;

import pivot.collections.Dictionary;

/**
 * Interface defining a "skin". A skin is the graphical representation
 * of a component. In MVC terminology, a skin represents the "view" of the
 * "model" data provided by a component. Components delegate a number of methods
 * to the skin, including all methods defined by the <tt>Visual</tt> interface
 * as well as style properties and layout. In conjunction with renderers
 * (implementations of the <tt>Renderer</tt> interface), skins define the
 * overall look and feel of an application.
 * <p>
 * <li>Skins are primarily responsible for the following:
 *      <ul>
 *      <li>Painting the component; if a container, this is effectively the
 *      background of the container - the Container class itself will paint the
 *      subcomponents.</li>
 *      <li>Layout of subcomponents; if not a container, <tt>layout()</tt> is
 *      a no-op (it exists solely to avoid type checking during layout).</li>
 *      </ul>
 * <li>Skins will often change their appearance in response to events fired by
 * the component; most commonly, this will be in response to data changes within
 * the component but may also be in response to input events (e.g. keyboard,
 * mouse). Skins should register for event notification in <tt>install()</tt>
 * and unregister in <tt>uninstall()</tt>.</li>
 * <li>Skins may (but are not required to) expose internal properties that
 * affect the appearance of the component as "style properties", similar to CSS
 * styles. For example, a component might provide styles to let a caller
 * set the foreground color and font.</li>
 * <li>Since callers are not allowed to interact with a component's skin
 * directly, access to styles is via the component's Styles collection, which
 * delegates to the Dictionary methods in the installed skin. Style values are
 * retrieved via <tt>get()</tt> and set via <tt>put()</tt>. Style members
 * should be defined as protected within the skin implementation for maximum
 * flexibility and efficiency (so they can easily be set or modified by a
 * subclass).</li>
 * <li>Skins are responsible for invalidating or repainting the component as
 * appropriate in response to events and style changes.</li>
 * <li>To keep the interface between skins and components simple, skins do not
 * fire events.</li>
 * <li>Skins may define string constants to represent style property keys.
 * These constants should be protected rather than private, so that callers
 * don't attempt to refer to them directly. This would create a dependency on
 * a specific skin, and a WTK application would fail to run if that skin class
 * was not present.</li>
 * </ul>
 *
 * @version 1.0 (4/17/2007)
 */
public interface Skin extends Visual, Dictionary<String, Object> {
    /**
     * Associates a skin with a component. The skin should register any event
     * listeners necessary to respond to changes in the component's state, and
     * should install any required subcomponents if the component is a
     * container.
     *
     * @param component
     * The component to which the skin is being attached. In general, skins
     * are specific to a particular component type and will throw an exception
     * when installed on the wrong component.
     *
     * @throws IllegalArgumentException
     * If the skin can't be installed on this component.
     */
    public void install(Component component);

    /**
     * Disassociates a skin from a component. The skin should unregister any
     * event listeners and subcomponents added in install().
     */
    public void uninstall();

    /**
     * If the component on which the skin is installed is a container, lays
     * out the container's children.
     */
    public void layout();

    public boolean mouseMove(int x, int y);
    public void mouseOver();
    public void mouseOut();

    public boolean mouseDown(Mouse.Button button, int x, int y);
    public boolean mouseUp(Mouse.Button button, int x, int y);
    public void mouseClick(Mouse.Button button, int x, int y, int count);

    public boolean mouseWheel(Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y);

    public boolean isFocusable();

    public void keyTyped(char character);

    public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation);
    public boolean keyReleased(int keyCode, Keyboard.KeyLocation keyLocation);

    /**
     * Returns the value of a style property.
     *
     * @param key
     * The name of the style property.
     *
     * @return
     * The value of the given property, or <tt>null</tt> if the property
     * does not exist. The property value may also be <tt>null</tt>; use
     * {@link #containsKey(String)} to distinguish between these two cases.
     */
    public Object get(String key);

    /**
     * Sets the value of a style property.
     *
     * @param key
     * The name of the style property.
     *
     * @param value
     * The new value for the style property.
     *
     * @return
     * The previous property value.
     */
    public Object put(String key, Object value);

    /**
     * Restores the style property to its default value.
     *
     * @param key
     * The name of the style property.
     *
     * @return
     * The previous property value.
     */
    public Object remove(String key);

    /**
     * Tests the existence of a style property.
     *
     * @return
     * <tt>true</tt> if the property exists; <tt>false</tt>, otherwise.
     */
    public boolean containsKey(String key);

    /**
     * Returns the type of a style property.
     *
     * @param key
     * The name of the property.
     *
     * @return
     * The property's type.
     */
    public Class<?> getType(String key);

    /**
     * Returns an iterator over the skin's style properties.
     *
     * @return
     * An iterator that enumerates the style properties.
     */
    public Iterable<String> getProperties();
}
