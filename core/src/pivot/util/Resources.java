package pivot.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

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

	public Resources(String baseName) throws IOException,
			SerializationException {
		this(baseName, Locale.getDefault());
	}

	public Resources(String baseName, Locale locale) throws IOException,
			SerializationException {
		if (locale == null) {
			throw new IllegalArgumentException("locale is null.");
		}

		this.baseName = baseName;
		this.locale = locale;

		resourceMap = readJSONResource(baseName);

		// try to find resource for the country
		try {
			String localeName = buildName(baseName, locale.getCountry());
			applyOverrides(resourceMap, readJSONResource(localeName));
		} catch (IllegalArgumentException e) {
			// this could happen, a warning will have already been generated
		}

		// try to find resource for language in the country
		try {
			String localeName = buildName(baseName, locale.getCountry(), locale
					.getLanguage());
			applyOverrides(resourceMap, readJSONResource(localeName));
		} catch (IllegalArgumentException e) {
			// this could happen, a warning will have already been generated
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

		for (String key : sourceMap) {
			if (overridesMap.containsKey(key)) {
				// an override is present!

				Object source = sourceMap.get(key);
				Object override = overridesMap.get(key);

				if (isMap(source, override)) {
					applyOverrides((Map<String, Object>) source,
							(Map<String, Object>) override);
				} else {
					sourceMap.put(key, overridesMap.get(key));
				}

			}
		}

	}

	private String buildName(String baseName, String... extras) {

		// determine the extension, if any
		String ext = "";
		String fileName = baseName;
		int extIndex = baseName.lastIndexOf(".");
		if (extIndex > -1) {
			ext = baseName.substring(extIndex);
			fileName = baseName.substring(0, extIndex);
		}

		StringBuffer newName = new StringBuffer();
		newName.append(fileName);

		for (String extra : extras) {
			newName.append("_").append(extra);
		}
		newName.append(ext);

		return newName.toString();
	}

	private boolean isNullOrMap(Object o) {
		return null == o || o instanceof Map;
	}

	private boolean isMap(Object source, Object override) {
		return isNullOrMap(source) && isNullOrMap(override);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> readJSONResource(String baseName)
			throws IOException, SerializationException {
		InputStream in = getClass().getClassLoader().getResourceAsStream(
				baseName);
		if (null == in) {
			System.out.println("Localisation warning: No resource at "
					+ baseName);
			throw new IllegalArgumentException("No resource at " + baseName);
		}
		JSONSerializer serializer = new JSONSerializer();
		Map<String, Object> resourceMap = null;
		try {
			resourceMap = (Map<String, Object>) serializer.readObject(in);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// can't do anything about this at this point, so ignore
			}
		}
		return resourceMap;
	}
}
