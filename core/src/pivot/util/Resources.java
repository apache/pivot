package pivot.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.MissingResourceException;

import pivot.collections.Dictionary;
import pivot.collections.Map;
import pivot.serialization.JSONSerializer;
import pivot.serialization.SerializationException;

/**
 * This class reads a JSON resource at {@link #baseName} using
 * {@link ClassLoader#getResourceAsStream(String)}. It applies localisation to
 * the resource using a method similar to that of
 * {@link java.util.ResourceBundle} in that it loads the base resource, then
 * applies a country specified resource over-writing the values in the base
 * using the country specified. It then does the same for country/language
 * specific.
 *
 * @see java.util.ResourceBundle
 *
 * @author brindy
 */
public class Resources implements Dictionary<String, Object> {

    private String baseName = null;
    private Locale locale = null;
    private Map<String, Object> resourceMap = null;

    /**
     * This constructor calls {@link #Resources(String, Locale)} with the given
     * baseName and whatever is returned from {@link Locale#getDefault()}
     *
     * @see #Resources(String, Locale)
     */
    public Resources(String baseName) throws IOException,
            SerializationException {
        this(baseName, Locale.getDefault());
    }

    /**
     * Full constructor for a Resources instance.
     *
     * @param baseName
     *            the base name of this resource as a fully qualified class name
     * @param locale
     *            the locale to use when reading this resource
     * @throws IOException
     *             if there is a problem when reading the resource
     * @throws SerializationException
     *             if there is a problem deserializing the resource from its
     *             JSON format
     * @throws NullPointerException
     *             if baseName or locale is null
     * @throws MissingResourceException
     *             if no resource for the specified base name can be found
     */
    public Resources(String baseName, Locale locale) throws IOException,
            SerializationException {

        if (locale == null) {
            throw new NullPointerException("Locale is null");
        }

        this.baseName = baseName;
        this.locale = locale;

        String resourceName = baseName.replaceAll("\\.", "/");
        resourceMap = readJSONResource(resourceName + ".json");
        if (null == resourceMap) {
            throw new MissingResourceException(
                    "Can't find resource for base name " + baseName
                            + ", locale " + locale, baseName, "");
        }

        // try to find resource for the language (e.g. resourceName_en)
        Map<String, Object> overrideMap = readJSONResource(resourceName + "_"
                + locale.getLanguage() + ".json");
        if (null != overrideMap) {
            applyOverrides(resourceMap, overrideMap);
        }

        // try to find resource for the entire locale (e.g. resourceName_en_GB)
        overrideMap = readJSONResource(resourceName + "_" + locale.toString()
                + ".json");
        if (null != overrideMap) {
            applyOverrides(resourceMap, overrideMap);
        }
    }

    public boolean containsKey(String key) {
        return resourceMap.containsKey(key);
    }

    public Object get(String key) {
        return resourceMap.get(key);
    }

    public String getBaseName() {
        return baseName;
    }

    public Locale getLocale() {
        return locale;
    }

    public boolean isEmpty() {
        return resourceMap.isEmpty();
    }

    public Object put(String key, Object value) {
        throw new UnsupportedOperationException(
                "Resources instances are immutable");
    }

    public Object remove(String key) {
        throw new UnsupportedOperationException(
                "Resources instances are immutable");
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
    private Map<String, Object> readJSONResource(String name)
            throws IOException, SerializationException {
        InputStream in = getClass().getClassLoader().getResourceAsStream(name);
        if (null == in) {
            return null;
        }
        JSONSerializer serializer = new JSONSerializer();
        Map<String, Object> resourceMap = null;
        try {
            resourceMap = (Map<String, Object>) serializer.readObject(in);
        } finally {
            in.close();
        }
        return resourceMap;
    }
}
