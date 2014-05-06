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

import java.math.BigInteger;
import java.util.Locale;

/**
 * A validator for a <tt>long</tt> value limited to a range.
 * <p> {@link BigInteger} math is used here so that proper checks against
 * the limits of the type can be done.
 *
 * @see ComparableRangeValidator
 */
public class LongRangeValidator extends IntValidator {
    private long minValue, maxValue;

    public LongRangeValidator() {
        this.minValue = Long.MIN_VALUE;
        this.maxValue = Long.MAX_VALUE;
    }

    public LongRangeValidator(Locale locale) {
        super(locale);
        this.minValue = Long.MIN_VALUE;
        this.maxValue = Long.MAX_VALUE;
    }

    public LongRangeValidator(long minValue, long maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public LongRangeValidator(Locale locale, long minValue, long maxValue) {
        super(locale);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public long getMinimum() {
        return minValue;
    }

    public void setMinimum(long minValue) {
        this.minValue = minValue;
    }

    public long getMaximum() {
        return maxValue;
    }

    public void setMaximum(long maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public boolean isValid(String text) {
        boolean valid = false;

        if (super.isValid(text)) {
            BigInteger min = BigInteger.valueOf(minValue);
            BigInteger max = BigInteger.valueOf(maxValue);
            BigInteger value = new BigInteger(text);
            valid = value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
        }

        return valid;
    }

}
