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

    /**
     * Helper method that wraps the {@link ParseException} in a {@link RuntimeException}.
     *
     * @param text The text to parse.
     * @return The number parsed from the text.
     * @throws RuntimeException if there was a parsing error.
     */
    protected final Number parseNumber(final String text) {
        String textToParse;
        try {
            // The default DecimalFormat doesn't support leading "+" sign,
            // and there is no setting in DecimalFormatSymbols for a positive
            // sign either, so just do it ourselves.
            // We have to upper case because of the exponent symbol
            if (text.length() > 1 && text.charAt(0) == '+') {
                textToParse = text.substring(1).toUpperCase(locale);
            } else {
                textToParse = text.toUpperCase(locale);
            }
            return format.parse(textToParse);
        } catch (ParseException ex) {
            // this should never happen
            throw new RuntimeException(ex);
        }
    }

    /**
     * Helper method that returns the widest number real instance, and extract
     * later values depending on the precision needed.
     *
     * @param text The text to convert.
     * @return The decimal equivalent of the text or {@code null} if the text
     * cannot be converted.
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

    /**
     * Set the autoTrim mode, before parsing the text (default false).
     *
     * @param autoTrim The new auto trim value ({@code true} to trim
     * leading and trailing blanks before parsing text).
     */
    public void setAutoTrim(boolean autoTrim) {
        this.autoTrim = autoTrim;
    }

    /**
     * Tell the autoTrim mode.
     *
     * @return {@code true} if autoTrim is enabled, otherwise {@code false} (default).
     */
    public boolean isAutoTrim() {
        return autoTrim;
    }

}
