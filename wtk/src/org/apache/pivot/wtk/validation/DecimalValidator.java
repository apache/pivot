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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * A validator for decimal values.
 */
public class DecimalValidator extends FormattedValidator<NumberFormat> {
    private boolean autoTrim = false;

    public DecimalValidator(DecimalFormat format) {
        super(format);
    }

    public DecimalValidator(DecimalFormat format, Locale locale) {
        super(format, locale);
    }

    public DecimalValidator() {
        super(NumberFormat.getInstance());
    }

    public DecimalValidator(Locale locale) {
        super(NumberFormat.getInstance(locale), locale);
    }

    /** helper method that wraps the ParseException in a RuntimeException. */
    protected final Number parseNumber(final String text) {
        String textToParse;
        try {
            // We have to upper case because of the exponent symbol
            textToParse = text.toUpperCase(locale);
            return format.parse(textToParse);
        } catch (ParseException ex) {
            // this should never happen
            throw new RuntimeException(ex);
        }
    }

    /** helper method that returns the widest number real instance,
     * and extract later values depending on the precision needed.
     */
    protected final BigDecimal textToBigDecimal(final String text) {
        BigDecimal bd;
        try {
            if (!autoTrim) {
                bd = new BigDecimal(parseNumber(text).toString());
            } else {
                bd = new BigDecimal(parseNumber(text.trim()).toString());
            }
        } catch (Exception e) {
            // ignore it
            bd = null;
        }
        return bd;
    }

    /** set the autoTrim mode, before parsing the text (default false) */
    public void setAutoTrim(boolean autoTrim) {
        this.autoTrim = autoTrim;
    }

    /** tell the autoTrim mode
     * @return true if autoTrim is enabled, otherwise false (default)
     * */
    public boolean isAutoTrim() {
        return autoTrim;
    }

}
