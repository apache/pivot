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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;

/**
 * Represents a set of localizable resources.
 */
public class Resources implements Dictionary<String, Object>, Iterable<String> {
    private final Resources parent;
    private final String baseName;
    private final Locale locale;
    private final Charset charset;

    private Map<String, Object> resourceMap = null;

    public static final String DEFAULT_CHARSET_NAME = "UTF-8";

    public Resources(String baseName) throws IOException, SerializationException {
        this(null, baseName, Locale.getDefault(), Charset.forName(DEFAULT_CHARSET_NAME));
    }

    public Resources(Resources parent, String baseName) throws IOException,
        SerializationException {
        this(parent, baseName, Locale.getDefault(), Charset.forName(DEFAULT_CHARSET_NAME));
    }

    public Resources(String baseName, Locale locale) throws IOException,
        SerializationException {
        this(null, baseName, locale, Charset.forName(DEFAULT_CHARSET_NAME));
    }

    public Resources(Resources parent, String baseName, Locale locale) throws IOException,
        SerializationException {
        this(parent, baseName, locale, Charset.forName(DEFAULT_CHARSET_NAME));
    }

    public Resources(String baseName, Charset charset) throws IOException,
        SerializationException {
        this(null, baseName, Locale.getDefault(), charset);
    }

    public Resources(Resources parent, String baseName, Charset charset) throws IOException,
        SerializationException {
        this(parent, baseName, Locale.getDefault(), charset);
    }

    /**
     * Creates a new resource bundle.
     *
     * @param parent
     * The parent resource defer to if a resource cannot be found in this
     * instance or null.
     *
     * @param baseName
     * The base name of this resource as a fully qualified class name.
     *
     * @param locale
     * The locale to use when reading this resource.
     *
     * @param charset
     * The character encoding to use when reading this resource.
     *
     * @throws IOException
     * If there is a problem when reading the resource.
     *
     * @throws SerializationException
     * If there is a problem deserializing the resource from its JSON format.
     *
     * @throws IllegalArgumentException
     * If baseName or locale or charset is null.
     *
     * @throws MissingResourceException
     * If no resource for the specified base name can be found.
     */
    public Resources(Resources parent, String baseName, Locale locale, Charset charset)
        throws IOException, SerializationException {
        if (baseName == null) {
            throw new IllegalArgumentException("baseName is null");
        }

        if (locale == null) {
            throw new IllegalArgumentException("locale is null");
        }

        if (charset == null) {
            throw new IllegalArgumentException("charset is null.");
        }

        this.parent = parent;
        this.baseName = baseName;
        this.locale = locale;
        this.charset = charset;

        String resourceName = baseName.replace('.', '/');
        this.resourceMap = readJSONResource(resourceName + "." + JSONSerializer.JSON_EXTENSION);

        // Try to find resource for the language (e.g. resourceName_en)
        Map<String, Object> overrideMap = readJSONResource(resourceName + "_"
            + locale.getLanguage() + "." + JSONSerializer.JSON_EXTENSION);
        if (overrideMap != null) {
            if (this.resourceMap == null) {
                this.resourceMap = overrideMap;
            } else {
                applyOverrides(this.resourceMap, overrideMap);
            }
        }

        // Try to find resource for the entire locale (e.g. resourceName_en_GB)
        overrideMap = readJSONResource(resourceName + "_" + locale.toString() + "." + JSONSerializer.JSON_EXTENSION);
        if (overrideMap != null) {
            if (this.resourceMap == null) {
                this.resourceMap = overrideMap;
            } else {
                applyOverrides(this.resourceMap, overrideMap);
            }
        }

        if (this.resourceMap == null) {
            throw new MissingResourceException("Can't find resource for base name "
                + baseName + ", locale " + locale, baseName, "");
        }
    }

    public Resources getParent() {
        return this.parent;
    }

    public String getBaseName() {
        return this.baseName;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public Charset getCharset() {
        return this.charset;
    }

    @Override
    public Object get(String key) {
        return (this.resourceMap.containsKey(key)) ?
            this.resourceMap.get(key) : (this.parent == null) ? null : this.parent.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException("Resources are immutable.");
    }

    @Override
    public Object remove(String key) {
        throw new UnsupportedOperationException("Resources are immutable.");
    }

    @Override
    public boolean containsKey(String key) {
        return this.resourceMap.containsKey(key)
            || (this.parent != null
                && this.parent.containsKey(key));
    }

    @Override
    public Iterator<String> iterator() {
        return new ImmutableIterator<String>(this.resourceMap.iterator());
    }

    @SuppressWarnings("unchecked")
    private void applyOverrides(Map<String, Object> sourceMap, Map<String, Object> overridesMap) {
        for (String key : overridesMap) {
            if (sourceMap.containsKey(key)) {
                Object source = sourceMap.get(key);
                Object override = overridesMap.get(key);

                if (source instanceof Map<?, ?>
                    && override instanceof Map<?, ?>) {
                    applyOverrides((Map<String, Object>)source, (Map<String, Object>)override);
                } else {
                    sourceMap.put(key, overridesMap.get(key));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readJSONResource(String name)
        throws IOException, SerializationException {
        Map<String, Object> resourceMapFromResource = null;

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(name);

        if (inputStream != null) {
            JSONSerializer serializer = new JSONSerializer(this.charset);

            try {
                resourceMapFromResource = (Map<String, Object>)serializer.readObject(inputStream);
            } finally {
                inputStream.close();
            }
        }

        return resourceMapFromResource;
    }
}
