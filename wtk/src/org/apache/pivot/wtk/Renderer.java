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
package org.apache.pivot.wtk;

import org.apache.pivot.collections.Dictionary;

/**
 * Base interface for "renderers". Renderers are used to customize the
 * appearance of a component's content.
 *
 * <p>There are several components in Pivot that accept an arbitrary Java
 * object or objects to show as part of their appearance, such as the label
 * of a {@link LinkButton}, the items in a the drop-down menu of a {@link
 * ListButton}, or the cells in a {@link TableView}. By default these
 * components know how to display strings, but you can define a custom
 * renderer to change the default behaviour and/or use non-string data.
 *
 * <p>The exact details of the renderer subinterfaces differ according to the
 * needs of the component, but they all use the the following framework:
 *
 * <ul>
 * <li>A <code>render</code> method, which is called when the component
 * needs to discover the appearance of a data item, and </li>
 * <li>A <code>toString</code> method, which is called when the user wants
 * to navigate via keyboard. (This, of course, may not make sense for data
 * that do not contain an apparent textual component.)</li>
 * </ul>
 *
 * <p>The <code>render</code> method is called during layout and paint
 * operations. It is passed the data object to be rendered, the component
 * performing the rendering, and possibly additional parameters giving
 * context specific to the component, such as whether the object is
 * "selected", or the relative location of a cell inside the component. The
 * renderer's job is to change its internal state using the data object and
 * be prepared to respond to subsequent calls querying the size of the
 * rendering or to paint it. Some components may also call the
 * <code>render</code> method with an object of <code>null</code> when
 * doing layout, if it makes the assumption that all the data elements will
 * be the same size in one or both dimensions, such as the elements of a
 * list.
 *
 * <p>Although not strictly required, the most convenient way to define a
 * renderer is by subclassing a <code>Component</code>, since Components
 * are already able to respond to layout and paint methods. That means all
 * the <code>render</code> method has to do is to modify the Component to
 * include the visually interesting part(s) of the data object in an
 * appropriate place, such as by setting the text of a Label to an
 * identifying string from the object, or setting the image of an ImageView
 * to an icon contained in the object.
 *
 * <p>N.B. If you base a renderer on a Component, you need to call validate on
 * it in order for it to paint correctly, because your renderer doesn't
 * have a parent to take care of that for you. Since all components that
 * call renderers call <code>setSize</code> before calling
 * <code>paint</code>, the canonical way to handle this is by including the
 * following override in your renderer:
 * <pre>     @Override
 *     public void setSize(int width, int height) {
 *         super.setSize(width, height);
 *         validate();
 *     }</pre>
 * <p>Note that you don't always need the additional parameters to
 * <code>render</code> if your renderer is simple enough. For example,
 * {@link ListView} passes parameters that tell whether the item is
 * selected or highlighted, but it also sets the background of the area
 * occupied by the renderer according to those parameters. Thus, if your
 * render has a transparent background, the selection state will be
 * apparent. Of course, you may want to adjust font or border colors within
 * the renderer to be harmonious with that different background.
 */
public interface Renderer extends ConstrainedVisual {
    /**
     * Returns the renderer's style dictionary.
     */
    public Dictionary<String, Object> getStyles();
}
