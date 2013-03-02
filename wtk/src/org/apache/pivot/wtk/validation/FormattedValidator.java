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

import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

/**
 * A validator for a {@link java.text.Format}'ed value.
 * <p>
 * This class is mostly intended to be a base-class for other validators.
 * Subclasses will set a different {@link java.text.Format} object, which
 * will be used in the {@link #isValid} method of this base class to do the
 * validation.
 */
public class FormattedValidator<F extends Format> implements Validator {
    protected final F format;
    protected final Locale locale;

    public FormattedValidator(F format) {
        this(format, null);
    }

    public FormattedValidator(F format, Locale locale) {
        if (format == null) {
            throw new IllegalArgumentException("format is null.");
        }
        this.format = format;

        if (locale == null) {
            this.locale = Locale.getDefault();
        } else {
            this.locale = locale;
        }

    }

    @Override
    public boolean isValid(final String text) {
        String textToParse = text;
        final ParsePosition pos = new ParsePosition(0);
        if (format instanceof NumberFormat) {
            // We have to upper case because of the exponent symbol
            textToParse = textToParse.toUpperCase(locale);
        }
        Object obj = format.parseObject(textToParse, pos);

        // The text is only valid if we successfully parsed ALL of it. Don't want trailing bits of
        // not-valid text.
        return obj != null && pos.getErrorIndex() == -1 && pos.getIndex() == text.length();
    }
}
