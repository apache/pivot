package pivot.charts.content;

import pivot.collections.HashMap;

public class CategorySeries extends HashMap<String, Object> {
    public static final long serialVersionUID = 0;

    public static final String NAME_KEY = "name";

    public CategorySeries() {
        this(null);
    }

    public CategorySeries(String name) {
        put(NAME_KEY, name);
    }
}
