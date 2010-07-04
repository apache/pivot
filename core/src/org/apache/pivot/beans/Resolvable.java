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

import java.net.URL;

import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;

/**
 * Interface representing a resolvable include. Serializers that want to
 * support BXML's resolution operators should implement this interface.
 */
public interface Resolvable {
    /**
     * Returns's the serializer's namespace.
     */
    public Map<String, Object> getNamespace();

    /**
     * Sets the serializer's namespace. This is used for variable resolution.
     *
     * @param namespace
     */
    public void setNamespace(Map<String, Object> namespace);

    /**
     * Returns the serializer's location.
     */
    public URL getLocation();

    /**
     * Sets the serializer's location. This is used for URL resolution.
     *
     * @param location
     */
    public void setLocation(URL location);

    /**
     * Returns the serializer's resource bundle.
     */
    public Resources getResources();

    /**
     * Sets the serializer's resource bundle. This is used for resource
     * resolution.
     *
     * @param resources
     */
    public void setResources(Resources resources);
}
