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
package org.apache.pivot.tests;

// import java.text.DecimalFormat;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.validation.BigDecimalValidator;
import org.apache.pivot.wtk.validation.ComparableRangeValidator;
import org.apache.pivot.wtk.validation.DoubleValidator;
import org.apache.pivot.wtk.validation.EmptyTextValidator;
import org.apache.pivot.wtk.validation.FloatRangeValidator;
import org.apache.pivot.wtk.validation.FloatValidator;
import org.apache.pivot.wtk.validation.IntRangeValidator;
import org.apache.pivot.wtk.validation.NotEmptyTextValidator;
import org.apache.pivot.wtk.validation.RegexTextValidator;
import org.apache.pivot.wtk.validation.Validator;

// import java.util.Formatter;

/**
 * Text input validator test.
 */
public final class TextInputValidatorTest implements Application {
    private Locale locale = Locale.getDefault(); // the default locale

    private Window window = null;
    private TextInput textinputLocale = null;
    private TextInput textinputComparableBigDecimal = null;
    private TextInput textinputComparableRange = null;
    private Label invalidComparableRangeLabel = null;
    private TextInput textinputDouble = null;
    private TextInput textinputFloat = null;
    private TextInput textinputFloatRange = null;
    private Label invalidLabel = null;
    private TextInput textinputIntRange = null;
    private TextInput textinputDateRegex = null;
    private TextInput textinputCustomBoolean = null;
    private TextInput textinputNotEmptyText = null;
    private TextInput textinputEmptyText = null;

    private String validText(final TextInput textInput) {
        return (textInput.isTextValid() ? "valid" : "invalid");
    }

    @Override
    public void startup(final Display display, final Map<String, String> properties) throws Exception {
        System.out.println("Starting TextInputValidatorTest ...");
        System.out.println("current Locale is " + locale);

        // sample different ways to format numbers in i18n compatible way
        NumberFormat nf = NumberFormat.getInstance();
        //
        // String customDecimalPattern = ""###,###.###"";
        // DecimalFormat df = new DecimalFormat(customDecimalPattern);
        //
        // StringBuffer sb = new StringBuffer();
        // Formatter formatter = new Formatter(sb, locale);
        // String customDecimalFormat = "%,.3f";
        //

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = new Window((Component) bxmlSerializer.readObject(
            getClass().getResource("text_input_validator_test.bxml")));

        Map<String, Object> namespace = bxmlSerializer.getNamespace();

        textinputLocale = (TextInput) namespace.get("textinputLocale");

        textinputComparableBigDecimal = (TextInput) namespace.get("textinputComparableBigDecimal");
        textinputComparableRange = (TextInput) namespace.get("textinputComparableRange");
        textinputDouble = (TextInput) namespace.get("textinputDouble");
        textinputFloat = (TextInput) namespace.get("textinputFloat");
        textinputFloatRange = (TextInput) namespace.get("textinputFloatRange");
        textinputIntRange = (TextInput) namespace.get("textinputIntRange");
        textinputDateRegex = (TextInput) namespace.get("textinputDateRegex");
        textinputCustomBoolean = (TextInput) namespace.get("textinputCustomBoolean");
        textinputNotEmptyText = (TextInput) namespace.get("textinputNotEmptyText");
        textinputEmptyText = (TextInput) namespace.get("textinputEmptyText");

        textinputLocale.setText(locale.toString());

        String testValue = "123456789.0";
        // new, validate a value but using BigDecimalValidator (subclass of ComparableValidator)
        textinputComparableBigDecimal.setText("1e300"); // huge value, and outside double range ...
        BigDecimalValidator bdComp = new BigDecimalValidator();
        System.out.println("BigDecimalValidator: created instance with value: " + bdComp);
        bdComp.setAutoTrim(true); // enable auto-trim of input string, before validating
        System.out.println("BigDecimalValidator: enable auto-trim of input string, before validating");
        textinputComparableBigDecimal.setValidator(bdComp);

        // new, validate in a range but using ComparableRangeValidator
        textinputComparableRange.setText(nf.format(new BigDecimal(testValue)));
        ComparableRangeValidator<BigDecimal> bdCompRange = new ComparableRangeValidator<>(
            new BigDecimal("2.0"), new BigDecimal("123456789"));
        System.out.println("ComparableRangeValidator: created instance with value: " + bdCompRange);
        bdCompRange.setAutoTrim(true); // enable auto-trim of input string, before validating
        System.out.println("ComparableRangeValidator: enable auto-trim of input string, before validating");
        textinputComparableRange.setValidator(bdCompRange);
        textinputComparableRange.getTextInputListeners().add(new TextInputListener() {
            @Override
            public void textValidChanged(final TextInput textInput) {
                invalidComparableRangeLabel.setText(validText(textInput));
            }
        });
        invalidComparableRangeLabel = (Label) namespace.get("invalidComparableRangeLabel");

        textinputDouble.setText("+\u221E"); // positive infinity text
        textinputDouble.setValidator(new DoubleValidator());

        // textinputFloat.setText("123456.789");
        // new, show different ways to format decimal values in i18n format
        Double value = Double.valueOf(testValue);
        // textinputFloat.setText(value.toString());
        // textinputFloat.setText(String.format(customDecimalFormat, value)); // sample using String.format
        // formatter.format(customDecimalFormat, value); // sample using Formatter
        // textinputFloat.setText(sb.toString()); // sample using Formatter
        // textinputFloat.setText(nf.format(value)); // sample using NumberFormat
        // textinputFloat.setText(df.format(value)); // sample using DecimalFormat
        textinputFloat.setText(nf.format(value)); // using this as a sample
        textinputFloat.setValidator(new FloatValidator());

        // standard float range model
        // note that float approximations could give errors,
        // try to increment/decrement the initial value near a range end, to see problems ...
        textinputFloatRange.setText(nf.format(Float.valueOf(testValue)));
        textinputFloatRange.setValidator(new FloatRangeValidator(2.0f, 123456789f));

        // test the listener by updating a label
        textinputFloatRange.getTextInputListeners().add(new TextInputListener() {
            @Override
            public void textValidChanged(final TextInput textInput) {
                invalidLabel.setText(validText(textInput));
            }
        });

        invalidLabel = (Label) namespace.get("invalidLabel");

        // standard int range model
        textinputIntRange.setText("0");
        textinputIntRange.setValidator(new IntRangeValidator(0, 100));

        // validate using a date regex.
        textinputDateRegex.setText("2009-09-01");
        textinputDateRegex.setValidator(new RegexTextValidator(
            "(19|20)\\d\\d[- /.](0[1-9]|1[012])[-/.](0[1-9]|[12][0-9]|3[01])"));

        // creating a custom model that only accepts "true" or "false"
        textinputCustomBoolean.setText("true");
        textinputCustomBoolean.setValidator(new Validator() {
            @Override
            public boolean isValid(final String s) {
                return "true".equals(s) || "false".equals(s);
            }
        });

        // validate any not-empty text
        textinputNotEmptyText.setText("  Not Empty, and with spaces  ");
        textinputNotEmptyText.setValidator(new NotEmptyTextValidator());

        // validate any empty text, edge case
        textinputEmptyText.setText("    ");
        textinputEmptyText.setValidator(new EmptyTextValidator());

        window.setTitle("Text Input Validator Test");
        window.setMaximized(true);
        window.open(display);
    }

    @Override
    public boolean shutdown(final boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public static void main(final String[] args) {
        DesktopApplicationContext.main(new String[] {
            TextInputValidatorTest.class.getName()
        });
    }

}
