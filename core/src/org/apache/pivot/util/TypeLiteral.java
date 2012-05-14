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
package org.apache.pivot.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Represents a generic type {@code T}. Java doesn't yet provide a way to
 * represent generic types, so this class does. Clients create a subclass
 * of this class, which enables retrieval the type information even at runtime.
 * <p>
 * For example, to get a reference to a generic type {@code List<String>}, you
 * create an empty anonymous inner class, like so:
 * <p>
 * {@code Type genericType = (new TypeLiteral<List<String>>() &#123;&#125;).getType();}
 * <p>
 * This class is a drastically reduced derivation from
 * <a href="http://code.google.com/p/google-guice/">Google Guice</a>'s
 * {@code TypeLiteral} class, written by Bob Lee and Jesse Wilson.
 */
public class TypeLiteral<T> {
    private final Type type;

    /**
     * Constructs a new type literal. Derives represented class from type
     * parameter.
     * <p>
     * Clients create an empty anonymous subclass. Doing so embeds the type
     * parameter in the anonymous class's type hierarchy so we can reconstitute it
     * at runtime despite erasure.
     */
    protected TypeLiteral() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType)) {
            throw new RuntimeException("Missing type parameter.");
        }

        ParameterizedType parameterizedType = (ParameterizedType)genericSuperclass;
        this.type = parameterizedType.getActualTypeArguments()[0];
    }

    /**
     * Gets underlying {@code Type} instance.
     */
    public final Type getType() {
        return this.type;
    }
}
