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
package org.apache.pivot.wtk.test;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.Label;
import pivot.wtk.TextInput;
import pivot.wtk.TextInputListener;
import pivot.wtk.Window;
import pivot.wtk.text.TextNode;
import pivot.wtk.text.validation.FloatRangeValidator;
import pivot.wtk.text.validation.IntRangeValidator;
import pivot.wtk.text.validation.RegexTextValidator;
import pivot.wtk.text.validation.Validator;
import pivot.wtkx.WTKXSerializer;

/**
 *
 * @author Noel Grandin
 */
public class TextInputValidatorTest implements Application {
    private Window window = null;
    private TextInput textinputFloatRange = null;
    private Label invalidLabel = null;
    private TextInput textinputIntRange = null;
    private TextInput textinputDateRegex = null;
    private TextInput textinputCustomBoolean = null;

    public void startup(Display display, Dictionary<String, String> properties) throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = new Window((Component)wtkxSerializer.readObject(
            getClass().getResource("textInputValidator_test.wtkx")));
        textinputFloatRange = (TextInput)wtkxSerializer.get("textinputFloatRange");
        textinputIntRange = (TextInput)wtkxSerializer.get("textinputIntRange");
        textinputDateRegex = (TextInput)wtkxSerializer.get("textinputDateRegex");
        textinputCustomBoolean = (TextInput)wtkxSerializer.get("textinputCustomBoolean");

        // standard float range model
        textinputFloatRange.setText("0.5");
        textinputFloatRange.setValidator(new FloatRangeValidator(0.3f, 2000f));

        // test the listener by updating a label
        textinputFloatRange.getTextInputListeners().add(new TextInputListener() {
            public void maximumLengthChanged(TextInput textInput, int previousMaximumLength) {
            }

            public void passwordChanged(TextInput textInput) {
            }

            public void promptChanged(TextInput textInput, String previousPrompt) {
            }

            public void textKeyChanged(TextInput textInput, String previousTextKey) {
            }

            public void textNodeChanged(TextInput textInput, TextNode previousTextNode) {
            }

            public void textSizeChanged(TextInput textInput, int previousTextSize) {
            }

            public void textValidChanged(TextInput textInput) {
                invalidLabel.setText(textInput.isTextValid() ? "valid" : "invalid");
            }

            public void textValidatorChanged(TextInput textInput, Validator validator) {
            }
        });

        invalidLabel = (Label)wtkxSerializer.get("invalidLabel");

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
            public boolean isValid(String s) {
                return "true".equals(s) || "false".equals(s);
            }
        });

        window.setTitle("Text Input Validator Test");
        window.setMaximized(true);
        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        window.close();
        return true;
    }

    public void resume() {
    }

    public void suspend() {
    }

    public static void main(String[] args) throws Exception {
        DesktopApplicationContext.main(new String[] { TextInputValidatorTest.class.getName() });
    }
}
