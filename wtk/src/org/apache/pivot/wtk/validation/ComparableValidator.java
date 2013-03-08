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
 * A validator for a Comparable value.
 */
public class ComparableValidator<T extends Comparable<T>> extends DecimalValidator {

    public ComparableValidator() {
        super();
    }

    public ComparableValidator(Locale locale) {
        super(locale);
    }

//    @Override
//    public boolean isValid(String text) {
//        if (!super.isValid(text))
//            return false;
//
//        return true;
//    }

    @Override
    public boolean isValid(String text) {
        boolean valid = false;

        if (super.isValid(text)) {
            @SuppressWarnings("unchecked")
            final Comparable<T> value = (Comparable<T>) textToComparable(text);
            valid = (value != null);
        }

        return valid;
    }

    protected final Comparable<?> textToComparable(String text) {
        return textToBigDecimal(text);
    }

}
