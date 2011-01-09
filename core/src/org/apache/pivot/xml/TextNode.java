/*
 * Contains code originally developed for Apache Pivot under the Apache
 * License, Version 2.0:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.apache.pivot.xml;

/**
 * Class representing an XML text node.
 */
public class TextNode extends Node {
    private String text;

    public TextNode(String text) {
        if (text == null) {
            throw new IllegalArgumentException();
        }

        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        boolean equals = false;

        if (this == o) {
            equals = true;
        } else if (o instanceof TextNode) {
            TextNode textNode = (TextNode)o;
            equals = (text.equals(textNode.text));
        }

        return equals;
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }

    @Override
    public String toString() {
        return text;
    }
}
