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

import org.apache.pivot.util.ListenerList;

/**
 * XML serializer listener interface.
 */
public interface XMLSerializerListener {
    /**
     * XML Serializer listeners.
     */
    public static class Listeners extends ListenerList<XMLSerializerListener>
        implements XMLSerializerListener {
        @Override
        public void beginElement(XMLSerializer xmlSerializer, Element element) {
            forEach(listener -> listener.beginElement(xmlSerializer, element));
        }

        @Override
        public void endElement(XMLSerializer xmlSerializer) {
            forEach(listener -> listener.endElement(xmlSerializer));
        }

        @Override
        public void readTextNode(XMLSerializer xmlSerializer, TextNode textNode) {
            forEach(listener -> listener.readTextNode(xmlSerializer, textNode));
        }
    }

    /**
     * XML serializer listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements XMLSerializerListener {
        @Override
        public void beginElement(XMLSerializer xmlSerializer, Element element) {
            // empty block
        }

        @Override
        public void endElement(XMLSerializer xmlSerializer) {
            // empty block
        }

        @Override
        public void readTextNode(XMLSerializer xmlSerializer, TextNode textNode) {
            // empty block
        }
    }

    /**
     * Called when the serializer has begun reading an element.
     *
     * @param xmlSerializer The active serializer.
     * @param element The element we are beginning to read.
     */
    default void beginElement(XMLSerializer xmlSerializer, Element element) {
    }

    /**
     * Called when the serializer has finished reading an element.
     *
     * @param xmlSerializer The active serializer.
     */
    default void endElement(XMLSerializer xmlSerializer) {
    }

    /**
     * Called when the serializer has read a text node.
     *
     * @param xmlSerializer The current serializer.
     * @param textNode The text node that was just read.
     */
    default void readTextNode(XMLSerializer xmlSerializer, TextNode textNode) {
    }
}
