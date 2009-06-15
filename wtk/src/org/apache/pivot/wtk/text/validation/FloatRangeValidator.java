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
package org.apache.pivot.wtk.text.validation;

/**
 * A validator for a float value limited to a range.
 *
 * @author Noel Grandin
 */
public class FloatRangeValidator extends FloatValidator {
    private float minValue, maxValue;

    public FloatRangeValidator() {
        this.minValue = 0;
        this.maxValue = 1;
    }

    public FloatRangeValidator(float minValue, float maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public float getMinimum() {
        return minValue;
    }

    public void setMinimum(float minValue) {
        this.minValue = minValue;
    }

    public float getMaximum() {
        return maxValue;
    }

    public void setMaximum(float maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public boolean isValid(String text) {
        boolean valid = false;

        if (super.isValid(text)) {
            final float f = textToObject(text);
            valid = (f >= minValue && f <= maxValue);
        }

        return valid;
    }
}
