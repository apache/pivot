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
 * A validator for an int value limited to a range.
 */
public class IntRangeValidator extends IntValidator {
    private int minValue, maxValue;

    public IntRangeValidator() {
        this.minValue = Integer.MIN_VALUE;
        this.maxValue = Integer.MAX_VALUE;
    }

    public IntRangeValidator(Locale locale) {
        super(locale);
        this.minValue = Integer.MIN_VALUE;
        this.maxValue = Integer.MAX_VALUE;
    }

    public IntRangeValidator(int minValue, int maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public IntRangeValidator(Locale locale, int minValue, int maxValue) {
        super(locale);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public int getMinimum() {
        return minValue;
    }

    public void setMinimum(int minValue) {
        this.minValue = minValue;
    }

    public int getMaximum() {
        return maxValue;
    }

    public void setMaximum(int maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public boolean isValid(String text) {
        boolean valid = false;

        if (super.isValid(text)) {
            final int i = textToObject(text);
            valid = (i >= minValue && i <= maxValue);
        }

        return valid;
    }

    private final Integer textToObject(String text) {
        return parseNumber(text).intValue();
    }
}
