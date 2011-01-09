/*
 * Contains code originally developed for Apache Pivot under the Apache
 * License, Version 2.0:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.apache.pivot.xml;

/**
 * Abstract base class for XML nodes.
 */
public abstract class Node {
    private Element parent = null;

    /**
     * Returns the parent element of the node.
     */
    public Element getParent() {
        return parent;
    }

    /**
     * Sets the parent element of the node.
     *
     * @param parent
     */
    protected void setParent(Element parent) {
        this.parent = parent;
    }
}
