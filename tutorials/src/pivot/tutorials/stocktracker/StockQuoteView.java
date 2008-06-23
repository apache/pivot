package pivot.tutorials.stocktracker;

import java.text.DecimalFormat;
import pivot.collections.Dictionary;

public class StockQuoteView implements Dictionary<String, Object> {
    private StockQuote stockQuote = null;

    private static DecimalFormat valueFormat = new DecimalFormat("$0.00");
    private static DecimalFormat changeFormat = new DecimalFormat("+0.00;-0.00");
    private static DecimalFormat volumeFormat = new DecimalFormat();

    public StockQuoteView(StockQuote stockQuote) {
        this.stockQuote = stockQuote;
    }

    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        if (stockQuote == null) {
            value = "";
        } else {
            value = stockQuote.get(key);

            if (key.equals(StockQuote.VALUE_KEY)
                || key.equals(StockQuote.OPENING_VALUE_KEY)
                || key.equals(StockQuote.HIGH_VALUE_KEY)
                || key.equals(StockQuote.LOW_VALUE_KEY)) {
                try {
                    value = valueFormat.format((Number)value);
                } catch(Exception exception) {
                    value = "";
                }
            } else if (key.equals(StockQuote.CHANGE_KEY)) {
                try {
                    value = changeFormat.format((Number)value);
                } catch(Exception exception) {
                    value = "";
                }
            } else if (key.equals(StockQuote.VOLUME_KEY)) {
                try {
                    value = volumeFormat.format((Number)value);
                } catch(Exception exception) {
                    value = "";
                }
            } else {
                value = value.toString();
            }
        }


        return value;
    }

    public Object put(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    public Object remove(String key) {
        throw new UnsupportedOperationException();
    }

    public boolean containsKey(String key) {
        return (stockQuote == null ? true : stockQuote.containsKey(key));
    }

    public boolean isEmpty() {
        return (stockQuote == null ? false : stockQuote.isEmpty());
    }
}

