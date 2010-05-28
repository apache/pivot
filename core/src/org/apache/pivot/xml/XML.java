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
package org.apache.pivot.xml;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;

/**
 * Contains utility methods for working with XML structures.
 */
public class XML {
    /**
     * Returns the element matching a given path.
     *
     * @param root
     * The element from which to begin the search.
     *
     * @param path
     * A path of the form:
     * <pre>
     * tag[n]/tag[n]/...
     * </pre>
     * The bracketed index values are optional and refer to the <i>n</i>th
     * occurrence of the given tag name within its parent element. If
     * omitted, the path refers to the first occurrence of the named
     * element (i.e. the element at index 0).
     *
     * @return
     * The matching element, or {@code null} if no such element exists.
     */
    public static Element getElement(Element root, String path) {
        if (root == null) {
            throw new IllegalArgumentException("root is null.");
        }

        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        if (path.length() == 0) {
            throw new IllegalArgumentException("path is empty.");
        }

        ArrayList<String> pathComponents = new ArrayList<String>(path.split("/"));
        Element current = root;

        for (int i = 0, n = pathComponents.getLength(); i < n; i++) {
            String pathComponent = pathComponents.get(i);

            String tagName;
            int index;
            int leadingBracketIndex = pathComponent.indexOf('[');
            if (leadingBracketIndex == -1) {
                tagName = pathComponent;
                index = 0;
            } else {
                tagName = pathComponent.substring(0, leadingBracketIndex);

                int trailingBracketIndex = pathComponent.lastIndexOf(']');
                if (trailingBracketIndex == -1) {
                    throw new IllegalArgumentException("Unterminated index identifier.");
                }

                index = Integer.parseInt(pathComponent.substring(leadingBracketIndex + 1,
                    trailingBracketIndex));
            }


            int j = 0;
            int k = 0;
            for (Node node : current) {
                if (node instanceof Element) {
                    Element element = (Element)node;

                    if (element.getName().equals(tagName)) {
                        if (k == index) {
                            break;
                        }

                        k++;
                    }
                }

                j++;
            }

            if (j < current.getLength()) {
                current = (Element)current.get(j);
            } else {
                current = null;
                break;
            }
        }

        return current;
    }

    /**
     * Returns the sub-elements of a descendant of {@code root} whose tag names
     * match the given name.
     *
     * @param root
     * The element from which to begin the search.
     *
     * @param path
     * The path to the descendant, relative to {@code root}.
     *
     * @param name
     * The tag name to match.
     *
     * @return
     * The matching elements, or {@code null} if no such descendant exists.
     *
     * @see #getElement(Element, String)
     * @see Element#getElements(String)
     */
    public static List<Element> getElements(Element root, String path, String name) {
        Element element = getElement(root, path);
        return (element == null) ? null : element.getElements(name);
    }

    /**
     * Returns the text content of a descendant of {@code root}.
     *
     * @param root
     * The element from which to begin the search.
     *
     * @param path
     * The path to the descendant, relative to {@code root}.
     *
     * @return
     * The text of the descedant, or {@code null} if no such descendant
     * exists.
     *
     * @see #getElement(Element, String)
     * @see Element#getText()
     */
    public static String getText(Element root, String path) {
        Element element = getElement(root, path);
        return (element == null) ? null : element.getText();
    }

}
