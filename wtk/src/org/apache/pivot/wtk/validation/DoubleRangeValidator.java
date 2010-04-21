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
 * A validator for a double value limited to a range.
 */
public class DoubleRangeValidator extends DoubleValidator {
    private double minValue, maxValue;

    public DoubleRangeValidator() {
        this.minValue = -Double.MIN_VALUE;
        this.maxValue = Double.MAX_VALUE;
    }

    public DoubleRangeValidator(Locale locale) {
        super(locale);
        this.minValue = -Double.MAX_VALUE;
        this.maxValue = Double.MAX_VALUE;
    }

    public DoubleRangeValidator(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public DoubleRangeValidator(Locale locale, double minValue, double maxValue) {
        super(locale);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public double getMinimum() {
        return minValue;
    }

    public void setMinimum(double minValue) {
        this.minValue = minValue;
    }

    public double getMaximum() {
        return maxValue;
    }

    public void setMaximum(double maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public boolean isValid(String text) {
        boolean valid = false;

        if (super.isValid(text)) {
            final double f = textToObject(text);
            valid = (f >= minValue && f <= maxValue);
        }

        return valid;
    }

    private final Double textToObject(String text) {
        return parseNumber(text).doubleValue();
    }
}
