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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * A validator for a double value.
 */
public class DoubleValidator extends DecimalValidator {
    private Locale locale = null;

    public DoubleValidator() {
        super(new DecimalFormat("0E0"));
    }

    public DoubleValidator(Locale locale) {
        this();
        this.locale = locale;
        ((DecimalFormat)format).setDecimalFormatSymbols(new DecimalFormatSymbols(locale));
    }

    @Override
    public boolean isValid(String text) {
        // We have to upper case because of the exponent symbol
        return super.isValid(locale == null ? text.toUpperCase() : text.toUpperCase(locale));
    }

}
