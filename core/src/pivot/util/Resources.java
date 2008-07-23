package pivot.util;

import java.util.Locale;

import pivot.collections.Dictionary;

/**
 * TODO This class will function similarly to java.util.ResourceBundle, only it
 * will load resources from JSON files.
 *
 * It will treat any JSON string that begins with a tilde (~) character as a
 * localized resource. It will prepend an appropriate locale path to the
 * value and convert it to a URL (e.g. /en_GB/foo.png). It will only return
 * valid URLs - if a localized resource with the corresponding name can't be
 * found, it will be converted to null.
 */
public class Resources implements Dictionary<String, Object> {
    private String baseName = null;
    private Locale locale = null;

    public Resources(String baseName) {
        this(baseName, Locale.getDefault());
    }

    public Resources(String baseName, Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException("locale is null.");
        }

        this.baseName = baseName;
        this.locale = locale;
    }

    public String getBaseName() {
        return baseName;
    }

    public Locale getLocale() {
        return locale;
    }

    public Object get(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object put(String key, Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object remove(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean containsKey(String key) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }
}
