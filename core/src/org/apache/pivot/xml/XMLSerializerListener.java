/*
 * Contains code originally developed for Apache Pivot under the Apache
 * License, Version 2.0:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.apache.pivot.xml;

/**
 * XML serializer listener interface.
 */
public interface XMLSerializerListener {
    /**
     * XML serializer listener adapter.
     */
    public static class Adapter implements XMLSerializerListener {
        @Override
        public void beginElement(XMLSerializer xmlSerializer, Element element) {
        }

        @Override
        public void endElement(XMLSerializer xmlSerializer) {
        }

        @Override
        public void readTextNode(XMLSerializer xmlSerializer, TextNode textNode) {
        }
    }

    /**
     * Called when the serializer has begun reading an element.
     *
     * @param xmlSerializer
     * @param element
     */
    public void beginElement(XMLSerializer xmlSerializer, Element element);

    /**
     * Called when the serializer has finished reading an element.
     *
     * @param xmlSerializer
     */
    public void endElement(XMLSerializer xmlSerializer);

    /**
     * Called when the serializer has read a text node.
     *
     * @param xmlSerializer
     * @param textNode
     */
    public void readTextNode(XMLSerializer xmlSerializer, TextNode textNode);
}
