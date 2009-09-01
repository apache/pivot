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
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;

/**
 * Reads a JSON resource at {@link #baseName} using
 * {@link ClassLoader#getResourceAsStream(String)}. It applies localization to
 * the resource using a method similar to that of
 * {@link java.util.ResourceBundle} in that it loads the base resource, then
 * applies a country specified resource over-writing the values in the base
 * using the country specified. It then does the same for country/language
 * specific.
 *
 * @see java.util.ResourceBundle
 */
public class Resources implements Dictionary<String, Object>, Iterable<String> {
    private final Resources parent;
    private final String baseName;
    private final Locale locale;
    private final Charset charset;

    private Map<String, Object> resourceMap = null;

    public Resources(String baseName) throws IOException, SerializationException {
        this(null, baseName, Locale.getDefault(), Charset.defaultCharset());
    }

    public Resources(Resources parent, String baseName) throws IOException,
        SerializationException {
        this(parent, baseName, Locale.getDefault(), Charset.defaultCharset());
    }

    public Resources(String baseName, Locale locale) throws IOException,
        SerializationException {
        this(null, baseName, locale, Charset.defaultCharset());
    }

    public Resources(Resources parent, String baseName, Locale locale) throws IOException,
        SerializationException {
        this(parent, baseName, locale, Charset.defaultCharset());
    }

    public Resources(String baseName, String charsetName) throws IOException,
        SerializationException {
        this(baseName, Locale.getDefault(), charsetName);
    }

    public Resources(Resources parent, String baseName, String charsetName) throws IOException,
        SerializationException {
        this(parent, baseName, Locale.getDefault(), charsetName);
    }

    public Resources(String baseName, Charset charset) throws IOException,
        SerializationException {
        this(null, baseName, Locale.getDefault(), charset);
    }

    public Resources(Resources parent, String baseName, Charset charset) throws IOException,
        SerializationException {
        this(parent, baseName, Locale.getDefault(), charset);
    }

    public Resources(String baseName, Locale locale, String charsetName) throws IOException,
        SerializationException {
        this(null, baseName, locale, Charset.forName(charsetName));
    }

    public Resources(Resources parent, String baseName, Locale locale, String charsetName)
        throws IOException, SerializationException {
        this(parent, baseName, locale, Charset.forName(charsetName));
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
     * If baseName or locale is null.
     *
     * @throws MissingResourceException
     * If no resource for the specified base name can be found.
     */
    public Resources(Resources parent, String baseName, Locale locale, Charset charset)
        throws IOException, SerializationException {
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
        resourceMap = readJSONResource(resourceName + ".json");
        if (resourceMap == null) {
            throw new MissingResourceException(
                    "Can't find resource for base name " + baseName
                            + ", locale " + locale, baseName, "");
        }

        // try to find resource for the language (e.g. resourceName_en)
        Map<String, Object> overrideMap = readJSONResource(resourceName + "_"
                + locale.getLanguage() + ".json");
        if (overrideMap != null) {
            applyOverrides(resourceMap, overrideMap);
        }

        // try to find resource for the entire locale (e.g. resourceName_en_GB)
        overrideMap = readJSONResource(resourceName + "_" + locale.toString()
                + ".json");
        if (null != overrideMap) {
            applyOverrides(resourceMap, overrideMap);
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
    public Object get(String key) {
        Object o = JSONSerializer.get(resourceMap, key);
        if (o == null && parent != null) {
            return parent.get(key);
        }
        return o;
    }

    public String getString(String key) {
        String s = JSONSerializer.getString(resourceMap, key);
        if (s == null && parent != null) {
            return parent.getString(key);
        }
        return s;
    }

    public Number getNumber(String key) {
        Number n = JSONSerializer.getNumber(resourceMap, key);
        if (n == null && parent != null) {
            return parent.getNumber(key);
        }
        return n;
    }

    public Short getShort(String key) {
        Short s = JSONSerializer.getShort(resourceMap, key);
        if (s == null && parent != null) {
            return parent.getShort(key);
        }
        return s;
    }

    public Integer getInteger(String key) {
        Integer i = JSONSerializer.getInteger(resourceMap, key);
        if (i == null && parent != null) {
            return parent.getInteger(key);
        }
        return i;
    }

    public Long getLong(String key) {
        Long l = JSONSerializer.getLong(resourceMap, key);
        if (l == null && parent != null) {
            return parent.getLong(key);
        }
        return l;
    }

    public Float getFloat(String key) {
        Float f = JSONSerializer.getFloat(resourceMap, key);
        if (f == null && parent != null) {
            return parent.getFloat(key);
        }
        return f;
    }

    public Double getDouble(String key) {
        Double d = JSONSerializer.getDouble(resourceMap, key);
        if (d == null && parent != null) {
            return parent.getDouble(key);
        }
        return d;
    }

    public Boolean getBoolean(String key) {
        Boolean b = JSONSerializer.getBoolean(resourceMap, key);
        if (b == null && parent != null) {
            return parent.getBoolean(key);
        }
        return b;
    }

    public List<?> getList(String key) {
        List<?> list = JSONSerializer.getList(resourceMap, key);
        if (list == null && parent != null) {
            return parent.getList(key);
        }
        return list;
    }

    public Map<String, ?> getMap(String key) {
        Map<String, ?> map = JSONSerializer.getMap(resourceMap, key);
        if (map == null && parent != null) {
            return parent.getMap(key);
        }
        return map;
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException(
                "Resources instances are immutable");
    }

    @Override
    public Object remove(String key) {
        throw new UnsupportedOperationException(
                "Resources instances are immutable");
    }

    @Override
    public boolean containsKey(String key) {
        boolean containsKey = resourceMap.containsKey(key);
        if (!containsKey && parent != null) {
            return parent.containsKey(key);
        }
        return containsKey;
    }

    @Override
    public boolean isEmpty() {
        boolean empty = resourceMap.isEmpty();
        if (empty && parent != null) {
            return parent.isEmpty();
        }
        return empty;
    }

    @Override
    public Iterator<String> iterator() {
        return new ImmutableIterator<String>(resourceMap.iterator());
    }

    @SuppressWarnings( { "unchecked" })
    private void applyOverrides(Map<String, Object> sourceMap,
            Map<String, Object> overridesMap) {

        for (String key : overridesMap) {
            if (sourceMap.containsKey(key)) {
                Object source = sourceMap.get(key);
                Object override = overridesMap.get(key);

                if (source instanceof Map<?, ?>
                        && override instanceof Map<?, ?>) {
                    applyOverrides((Map<String, Object>) source,
                            (Map<String, Object>) override);
                } else {
                    sourceMap.put(key, overridesMap.get(key));
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readJSONResource(String name)
            throws IOException, SerializationException {
        InputStream in = getClass().getClassLoader().getResourceAsStream(name);
        if (in == null) {
            return null;
        }

        JSONSerializer serializer = new JSONSerializer(charset);
        Map<String, Object> resourceMap = null;
        try {
            resourceMap = (Map<String, Object>) serializer.readObject(in);
        } finally {
            in.close();
        }

        return resourceMap;
    }
}
