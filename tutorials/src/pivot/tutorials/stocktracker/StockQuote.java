package pivot.tutorials.stocktracker;

import pivot.collections.Dictionary;
import pivot.collections.HashMap;

public class StockQuote implements Dictionary<String, Object> {
    private HashMap<String, Object> values = new HashMap<String, Object>();

    public static final String SYMBOL_KEY = "symbol";
    public static final String VALUE_KEY = "value";
    public static final String CHANGE_KEY = "change";

    public Object get(String key) {
        return values.get(key);
    }

    public Object put(String key, Object value) {
        if (key.equals(VALUE_KEY)
            || key.equals(CHANGE_KEY)) {
            try {
                value = Float.parseFloat((String)value);
            } catch(NumberFormatException exception) {
                value = null;
            }
        }

        return values.put(key, value);
    }

    public Object remove(String key) {
        return values.remove(key);
    }

    public boolean containsKey(String key) {
        return values.containsKey(key);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }
}

