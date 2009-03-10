/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;

import pivot.collections.Dictionary;
import pivot.collections.Map;
import pivot.serialization.JSONSerializer;
import pivot.serialization.SerializationException;

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
 *
 * @author brindy
 * @author gbrown
 */
public class Resources implements Dictionary<String, Object>, Iterable<String> {
    private String baseName = null;
    private Locale locale = null;
    private Charset charset = null;

    private Map<String, Object> resourceMap = null;

    public Resources(String baseName)
        throws IOException, SerializationException {
        this(baseName, Locale.getDefault(), Charset.defaultCharset());
    }

    public Resources(String baseName, Locale locale)
        throws IOException, SerializationException {
        this(baseName, locale, Charset.defaultCharset());
    }

    public Resources(String baseName, String charsetName)
        throws IOException, SerializationException {
        this(baseName, Locale.getDefault(), charsetName);
    }

    public Resources(String baseName, Charset charset)
        throws IOException, SerializationException {
        this(baseName, Locale.getDefault(), charset);
    }

    public Resources(String baseName, Locale locale, String charsetName)
        throws IOException, SerializationException {
        this(baseName, locale, Charset.forName(charsetName));
    }

    /**
     * Creates a new resource bundle.
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
    public Resources(String baseName, Locale locale, Charset charset) throws IOException,
        SerializationException {

        if (locale == null) {
            throw new IllegalArgumentException("locale is null");
        }

        if (charset == null) {
            throw new IllegalArgumentException("charset is null.");
        }

        this.baseName = baseName;
        this.locale = locale;
        this.charset = charset;

        String resourceName = baseName.replace('.', '/');
        resourceMap = readJSONResource(resourceName + ".json");
        if (resourceMap == null) {
            throw new MissingResourceException("Can't find resource for base name "
                + baseName + ", locale " + locale, baseName, "");
        }

        // try to find resource for the language (e.g. resourceName_en)
        Map<String, Object> overrideMap = readJSONResource(resourceName + "_"
            + locale.getLanguage() + ".json");
        if (overrideMap != null) {
            applyOverrides(resourceMap, overrideMap);
        }

        // try to find resource for the entire locale (e.g. resourceName_en_GB)
        overrideMap = readJSONResource(resourceName + "_" + locale.toString() + ".json");
        if (null != overrideMap) {
            applyOverrides(resourceMap, overrideMap);
        }
    }

    public String getBaseName() {
        return baseName;
    }

    public Locale getLocale() {
        return locale;
    }

    public Object get(String key) {
        return JSONSerializer.getValue(resourceMap, key);
    }

    public Object put(String key, Object value) {
        throw new UnsupportedOperationException("Resources instances are immutable");
    }

    public Object remove(String key) {
        throw new UnsupportedOperationException("Resources instances are immutable");
    }

    public boolean containsKey(String key) {
        return resourceMap.containsKey(key);
    }

    public boolean isEmpty() {
        return resourceMap.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private void applyOverrides(Map<String, Object> sourceMap,
        Map<String, Object> overridesMap) {

        for (String key : overridesMap) {
            if (sourceMap.containsKey(key)) {
                Object source = sourceMap.get(key);
                Object override = overridesMap.get(key);

                if (source instanceof Map && override instanceof Map) {
                    applyOverrides((Map<String, Object>) source,
                        (Map<String, Object>) override);
                } else {
                    sourceMap.put(key, overridesMap.get(key));
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readJSONResource(String name) throws IOException,
        SerializationException {
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

    public Iterator<String> iterator() {
        return new ImmutableIterator<String>(resourceMap.iterator());
    }
}
