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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;

/**
 * Represents a set of localizable resources.
 */
public class Resources extends AbstractMap<String, String> {
    private final Resources parent;
    private final String baseName;
    private final Locale locale;
    private final Charset charset;

    private Properties properties = null;

    public static final String DEFAULT_CHARSET_NAME = "UTF-8";
    public static final String PROPERTIES_EXTENSION = "properties";
    public static final int BUFFER_SIZE = 2048;

    public Resources(String baseName) throws IOException {
        this(baseName, Locale.getDefault());
    }

    public Resources(String baseName, Locale locale) throws IOException {
        this(baseName, locale, Charset.forName(DEFAULT_CHARSET_NAME));
    }

    public Resources(String baseName, Charset charset) throws IOException {
        this(baseName, Locale.getDefault(), charset);
    }

    public Resources(String baseName, Locale locale, Charset charset) throws IOException {
        this(null, baseName, locale, charset);
    }

    public Resources(Resources parent, String baseName) throws IOException {
        this(parent, baseName, Locale.getDefault());
    }

    public Resources(Resources parent, String baseName, Locale locale) throws IOException {
        this(parent, baseName, locale, Charset.forName(DEFAULT_CHARSET_NAME));
    }

    public Resources(Resources parent, String baseName, Charset charset) throws IOException {
        this(parent, baseName, Locale.getDefault(), charset);
    }

    /**
     * Creates a new resource bundle.
     *
     * @param parent
     * The parent resource bundle. If a resource value cannot be located in
     * this resource bundle, the parent bundle will be searched. May be
     * <tt>null</tt> to specify no parent.
     *
     * @param baseName
     * The base name of this resource bundle, as a fully qualified class name.
     *
     * @param locale
     * The locale to use when loading this resource bundle.
     *
     * @param charset
     * The character encoding to use when reading this resource bundle.
     */
    public Resources(Resources parent, String baseName, Locale locale, Charset charset)
        throws IOException {
        if (baseName == null) {
            throw new IllegalArgumentException("baseName is null");
        }

        if (locale == null) {
            throw new IllegalArgumentException("locale is null");
        }

        if (charset == null) {
            throw new IllegalArgumentException("charset is null.");
        }

        if (parent != null
            && !parent.locale.equals(locale)) {
            throw new IllegalArgumentException("Parent locale is not the same as locale.");
        }

        this.parent = parent;
        this.baseName = baseName;
        this.locale = locale;
        this.charset = charset;

        String resourceName = baseName.replace('.', '/');
        properties = readProperties(resourceName + "." + PROPERTIES_EXTENSION);

        // Look for language-specific properties
        Properties languageOverrides = readProperties(resourceName + "_" + locale.getLanguage()
            + "." + PROPERTIES_EXTENSION);
        if (languageOverrides != null) {
            applyOverrides(languageOverrides);
        }

        // Look for region-specific properties
        Properties regionOverrides = readProperties(resourceName + "_" + locale.toString()
            + "." + PROPERTIES_EXTENSION);
        if (regionOverrides != null) {
            applyOverrides(regionOverrides);
        }

        if (properties == null) {
            throw new MissingResourceException("Can't find resources for \"" + baseName + "\".",
                null, null);
        }
    }

    public Resources getParent() {
        return parent;
    }

    public String getBaseName() {
        return baseName;
    }

    public Locale getLocale() {
        return locale;
    }

    public Charset getCharset() {
        return charset;
    }

    @Override
    public String get(Object key) {
        return (properties.containsKey(key)) ?
            (String)properties.get(key) : (parent == null) ? null : parent.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return properties.containsKey(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> keySet() {
        Set<?> keys = Collections.unmodifiableSet(properties.keySet());
        return (Set<String>)keys;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Map.Entry<String, String>> entrySet() {
        Set<?> entries = Collections.unmodifiableSet(properties.entrySet());
        return (Set<Map.Entry<String, String>>)entries;
    }

    private Properties readProperties(String resourceName) throws IOException {
        Properties properties = null;

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resourceName);

        if (inputStream != null) {
            Reader reader = new BufferedReader(new InputStreamReader(inputStream, charset), BUFFER_SIZE);

            properties = new Properties();
            try {
                properties.load(reader);
            } finally {
                reader.close();
            }
        }

        return properties;
    }

    private void applyOverrides(Properties overrides) {
        if (properties == null) {
            properties = overrides;
        } else {
            for (Object key : overrides.keySet()) {
                if (properties.containsKey(key)) {
                    properties.put(key, overrides.get(key));
                }
            }
        }
    }
}
