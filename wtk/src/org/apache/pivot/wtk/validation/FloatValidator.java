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
 * A validator for a float value.
 */
public class FloatValidator extends DecimalValidator {
    public FloatValidator() {
    }

    public FloatValidator(Locale locale) {
        super(locale);
    }

    @Override
    public boolean isValid(String text) {
        if (!super.isValid(text))
            return false;

        /*
         * DecimalFormat will parse the number as a double. Make sure the
         * resulting number is withing range for a float.
         */
        Number number = parseNumber(text);
        if (number.doubleValue() > Float.MAX_VALUE) {
            return false;
        }
        if (number.doubleValue() < -Float.MAX_VALUE) {
            return false;
        }
        return true;
    }
}
