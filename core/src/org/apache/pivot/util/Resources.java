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
import org.apache.pivot.json.JSON;
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
        resourceMap = readJSONResource(resourceName + ".json");

        // Try to find resource for the language (e.g. resourceName_en)
        Map<String, Object> overrideMap = readJSONResource(resourceName + "_"
            + locale.getLanguage() + ".json");
        if (overrideMap != null) {
            if (resourceMap == null) {
                resourceMap = overrideMap;
            } else {
                applyOverrides(resourceMap, overrideMap);
            }
        }

        // Try to find resource for the entire locale (e.g. resourceName_en_GB)
        overrideMap = readJSONResource(resourceName + "_" + locale.toString() + ".json");
        if (overrideMap != null) {
            if (resourceMap == null) {
                resourceMap = overrideMap;
            } else {
                applyOverrides(resourceMap, overrideMap);
            }
        }

        if (resourceMap == null) {
            throw new MissingResourceException("Can't find resource for base name "
                + baseName + ", locale " + locale, baseName, "");
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
        Object o = JSON.get(resourceMap, key);
        if (o == null
            && parent != null) {
            o = parent.get(key);
        }

        return o;
    }

    public String getString(String key) {
        String s = JSON.getString(resourceMap, key);
        if (s == null
            && parent != null) {
            s = parent.getString(key);
        }

        return s;
    }

    /**
     * Gets a resource string with positional token substitution. Tokens of the
     * form <tt>{<i>N</i>}</tt> (where <tt>N</tt> is the variable argument
     * index) will be substituted with their corresponding String in the
     * variable arguments array.
     * <p>
     * For example, if resource string <tt>foo</tt> were defined to be
     * <tt>"{0} knows {1}, and {1} knows {0}."</tt>, then calling
     * <tt>getString("foo", "Jane", "John")</tt> would yield the string
     * "Jane knows John, and John knows Jane."
     *
     * @param key
     * The resource key
     *
     * @param args
     * Arguments referenced within the value of the resource string
     *
     * @return
     * The resource string after positional substitution has been performed
     */
    public String getString(String key, String... args) {
        StringBuilder buf = new StringBuilder(getString(key));

        for (int i = 0; i < args.length; i++) {
            String token = '{' + String.valueOf(i) + '}';
            String substitution = args[i];

            int tokenLength = token.length();
            int substituionLength = substitution.length();

            for (int j = buf.indexOf(token, 0); j != -1; j = buf.indexOf(token, j)) {
                buf.replace(j, j + tokenLength, substitution);
                j += substituionLength;
            }
        }

        return buf.toString();
    }

    public Number getNumber(String key) {
        Number n = JSON.getNumber(resourceMap, key);
        if (n == null
            && parent != null) {
            n = parent.getNumber(key);
        }

        return n;
    }

    public Short getShort(String key) {
        Short s = JSON.getShort(resourceMap, key);
        if (s == null
            && parent != null) {
            s = parent.getShort(key);
        }

        return s;
    }

    public Integer getInteger(String key) {
        Integer i = JSON.getInteger(resourceMap, key);
        if (i == null
            && parent != null) {
            i = parent.getInteger(key);
        }

        return i;
    }

    public Long getLong(String key) {
        Long l = JSON.getLong(resourceMap, key);
        if (l == null
            && parent != null) {
            l = parent.getLong(key);
        }

        return l;
    }

    public Float getFloat(String key) {
        Float f = JSON.getFloat(resourceMap, key);
        if (f == null
            && parent != null) {
            f = parent.getFloat(key);
        }

        return f;
    }

    public Double getDouble(String key) {
        Double d = JSON.getDouble(resourceMap, key);
        if (d == null
            && parent != null) {
            d = parent.getDouble(key);
        }

        return d;
    }

    public Boolean getBoolean(String key) {
        Boolean b = JSON.getBoolean(resourceMap, key);
        if (b == null
            && parent != null) {
            b = parent.getBoolean(key);
        }

        return b;
    }

    public List<?> getList(String key) {
        List<?> list = JSON.getList(resourceMap, key);
        if (list == null
            && parent != null) {
            list = parent.getList(key);
        }

        return list;
    }

    public Map<String, ?> getMap(String key) {
        Map<String, ?> map = JSON.getMap(resourceMap, key);
        if (map == null
            && parent != null) {
            map = parent.getMap(key);
        }

        return map;
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
        boolean containsKey = resourceMap.containsKey(key);

        if (!containsKey
            && parent != null) {
            return parent.containsKey(key);
        }

        return containsKey;
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
        Map<String, Object> resourceMap = null;

        InputStream inputStream = ThreadUtilities.getClassLoader().getResourceAsStream(name);

        if (inputStream != null) {
            JSONSerializer serializer = new JSONSerializer(charset);

            try {
                resourceMap = (Map<String, Object>)serializer.readObject(inputStream);
            } finally {
                inputStream.close();
            }
        }

        return resourceMap;
    }
}
