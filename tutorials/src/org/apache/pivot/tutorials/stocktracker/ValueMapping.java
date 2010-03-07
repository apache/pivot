package org.apache.pivot.tutorials.stocktracker;

import java.text.DecimalFormat;

import org.apache.pivot.wtk.Label;

/**
 * Formats a float as a dollar value.
 */
public class ValueMapping implements Label.TextBindMapping {
    private static final DecimalFormat FORMAT = new DecimalFormat("$0.00");

    @Override
    public String toString(Object value) {
        return Float.isNaN((Float)value) ? null : FORMAT.format(value);
    }

    @Override
    public Object valueOf(String text) {
        throw new UnsupportedOperationException();
    }
}
