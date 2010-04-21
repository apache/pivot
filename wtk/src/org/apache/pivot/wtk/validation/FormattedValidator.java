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
import java.text.ParsePosition;

/**
 * A validator for a {@link java.text.Format}'ed value.
 * <p>
 * This class is mostly intended to be a base-class for other validators.
 */
public class FormattedValidator<F extends Format> implements Validator {
    protected final F format;

    public FormattedValidator(F format) {
        this.format = format;
    }

    @Override
    public boolean isValid(String text) {
        final ParsePosition pos = new ParsePosition(0);
        Object obj = format.parseObject(text, pos);

        // the text is only valid is we successfully parsed ALL of it. Don't want trailing bits of
        // not-valid text.
        return obj != null && pos.getErrorIndex() == -1 && pos.getIndex() == text.length();
    }
}
