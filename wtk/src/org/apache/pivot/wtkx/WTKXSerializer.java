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
package org.apache.pivot.wtkx;

import java.io.IOException;
import java.net.URL;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;

/**
 * Loads an object hierarchy from an XML document.
 *
 * @deprecated
 * This class has been superseded by {@link org.apache.pivot.beans.BXMLSerializer}.
 */
@Deprecated
public class WTKXSerializer extends BXMLSerializer {
    public static final String WTKX_PREFIX = "wtkx";
    public static final String WTKX_EXTENSION = "wtkx";
    public static final String MIME_TYPE = "application/wtkx";

    static {
        getFileExtensions().put(WTKX_EXTENSION, MIME_TYPE);
        getMimeTypes().put(MIME_TYPE, WTKXSerializer.class);
    }

    public WTKXSerializer() {
        this(null);
    }

    public WTKXSerializer(Resources resources) {
        super(WTKX_PREFIX, WTKX.class);

        setResources(resources);
    }

    public Object readObject(String resourceName)
        throws IOException, SerializationException {
        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName is null.");
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL location = classLoader.getResource(resourceName);

        if (location == null) {
            throw new SerializationException("Could not find resource named \""
                + resourceName + "\".");
        }

        return readObject(location, getResources());
    }

    public Object readObject(Object baseObject, String resourceName)
        throws IOException, SerializationException {
        if (baseObject == null) {
            throw new IllegalArgumentException("baseObject is null.");
        }

        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName is null.");
        }

        Class<?> baseType = baseObject.getClass();
        return readObject(baseType.getResource(resourceName), getResources());
    }

    @Override
    public String getMIMEType(Object object) {
        return MIME_TYPE;
    }
}
