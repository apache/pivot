/*
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
package org.apache.pivot.wtk.validation;

import java.util.Locale;

/**
 * Generic validator version for a Comparable value limited in a range.
 */
public class ComparableRangeValidator<T extends Comparable<T>> extends ComparableValidator<T> {
    private T minValue;
    private T maxValue;

    public ComparableRangeValidator() {
        this(Locale.getDefault());
    }

    public ComparableRangeValidator(Locale locale) {
        super(locale);
        this.minValue = null;
        this.maxValue = null;
    }

    public ComparableRangeValidator(T minValue, T maxValue) {
        this(Locale.getDefault(), minValue, maxValue);
    }

    public ComparableRangeValidator(Locale locale, T minValue, T maxValue) {
        super(locale);
        setMinimum(minValue);
        setMaximum(maxValue);

        if (maxValue.compareTo(minValue)< 0) {
            throw new IllegalArgumentException("maxValue must be higher or equals than minValue");
        }
    }

    public T getMinimum() {
        return minValue;
    }

    public void setMinimum(T minValue) {
        if (minValue == null) {
            throw new IllegalArgumentException("minValue must be not null");
        }
        this.minValue = minValue;
    }

    public T getMaximum() {
        return maxValue;
    }

    public void setMaximum(T maxValue) {
        if (minValue == null) {
            throw new IllegalArgumentException("minValue must be not null");
        }
        this.maxValue = maxValue;
    }

    @Override
    public boolean isValid(String text) {
        boolean valid = false;

        if (super.isValid(text)) {
            @SuppressWarnings("unchecked")
            final Comparable<T> value = (Comparable<T>) textToComparable(text);
            if (value != null) {
                valid = (value.compareTo(minValue) >= 0 && value.compareTo(maxValue) <= 0);
            }
        }

        return valid;
    }

    @Override
    public String toString() {
        return ( this.getClass().getSimpleName() + "(" + minValue + "," + maxValue + ")" );
    }

}
