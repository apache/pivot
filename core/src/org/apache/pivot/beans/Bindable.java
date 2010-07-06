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
 * Allows {@link BXMLSerializer} to initialize an instance of a deserialized class.
 */
public interface Bindable {
    /**
     * Called to initialize the class after it has been completely
     * processed and bound by the serializer.
     *
     * @param namespace
     * The serializer's namespace. The bindable object can use this to extract named
     * values defined in the BXML file. Alternatively, the {@link BXML} annotation
     * can be used by trusted code to automatically map namespace values to member
     * variables.
     *
     * @param location
     * The location of the BXML source. May be <tt>null</tt> if the location of the
     * source is not known.
     *
     * @param resources
     * The resources that were used to localize the deserialized content. May be
     * <tt>null</tt> if no resources were specified.
     */
    public void initialize(Map<String, Object> namespace, URL location, Resources resources);
}
