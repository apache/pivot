package pivot.charts.content;

import pivot.collections.ArrayList;

public class ValueSeries extends ArrayList<Object> {
    public static final long serialVersionUID = 0;

    private String name = null;

    public ValueSeries() {
        this(null);
    }

    public ValueSeries(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
