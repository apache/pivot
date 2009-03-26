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
package pivot.wtk.text.validation;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * A validator for decimal values.
 *
 * @author Noel Grandin
 */
public abstract class DecimalValidator extends FormattedValidator<NumberFormat> {

    protected DecimalValidator(DecimalFormat format) {
        super(format);
    }

    protected DecimalValidator() {
        super(NumberFormat.getInstance());
    }

    /** helper for textToObject */
    protected final Number parse(String text) {
        try {
            return format.parse(text);
        } catch (ParseException ex) {
            // this should never happen
            throw new RuntimeException(ex);
        }
    }
}
