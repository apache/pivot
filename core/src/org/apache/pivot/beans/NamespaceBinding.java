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
package org.apache.pivot.beans;

/**
 * Represents a binding relationship between an object property and a namespace
 * path.
 */
public class NamespaceBinding {
    private Object object;
    private String key;
    private String path;

    public NamespaceBinding(Object object, String key, String path) {
        this.object = object;
        this.key = key;
        this.path = path;
    }

    /**
     * Returns the bound object.
     */
    public Object getObject() {
        return object;
    }

    /**
     * Returns the bound property.
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the namespace path to which the object property is bound.
     */
    public String getPath() {
        return path;
    }

    /**
     * Binds the object property to the namespace path.
     */
    public void bind() {
        // TODO
    }

    /**
     * Unbinds the object property from the namespace path.
     */
    public void unbind() {
        // TODO
    }
}
