package pivot.tutorials.stocktracker;

import pivot.collections.Dictionary;
import pivot.collections.HashMap;

public class StockQuote implements Dictionary<String, Object> {
    private HashMap<String, Object> values = new HashMap<String, Object>();

    public static final String SYMBOL_KEY = "symbol";
    public static final String COMPANY_NAME_KEY = "companyName";
    public static final String VALUE_KEY = "value";
    public static final String OPENING_VALUE_KEY = "openingValue";
    public static final String HIGH_VALUE_KEY = "highValue";
    public static final String LOW_VALUE_KEY = "lowValue";
    public static final String CHANGE_KEY = "change";
    public static final String VOLUME_KEY = "volume";

    public float getChange() {
        return (values.containsKey(CHANGE_KEY) ? (Float)values.get(CHANGE_KEY) : 0);
    }

    public Object get(String key) {
        return values.get(key);
    }

    public Object put(String key, Object value) {
        if (key.equals(VALUE_KEY)
            || key.equals(OPENING_VALUE_KEY)
            || key.equals(HIGH_VALUE_KEY)
            || key.equals(LOW_VALUE_KEY)
            || key.equals(CHANGE_KEY)
            || key.equals(VOLUME_KEY)) {
            try {
                value = Float.parseFloat((String)value);
            } catch(NumberFormatException exception) {
                value = 0f;
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

