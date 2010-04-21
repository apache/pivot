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
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * A validator for decimal values.
 */
public class DecimalValidator extends FormattedValidator<NumberFormat> {

    public DecimalValidator(DecimalFormat format) {
        super(format);
    }

    public DecimalValidator() {
        super(NumberFormat.getInstance());
    }

    public DecimalValidator(Locale locale) {
        super(NumberFormat.getInstance(locale));
    }

    /** helper method that wraps the ParseException in a RuntimeException. */
    protected final Number parseNumber(String text) {
        try {
            return format.parse(text);
        } catch (ParseException ex) {
            // this should never happen
            throw new RuntimeException(ex);
        }
    }
}
