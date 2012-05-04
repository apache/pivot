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
import org.apache.pivot.wtk.validation.FloatRangeValidator;
import org.apache.pivot.wtk.validation.IntRangeValidator;
import org.apache.pivot.wtk.validation.NotEmptyTextValidator;
import org.apache.pivot.wtk.validation.RegexTextValidator;
import org.apache.pivot.wtk.validation.Validator;

/**
 * Text input validator test.
 */
public class TextInputValidatorTest  extends Application.Adapter {
    private Window window = null;
    private TextInput textinputFloatRange = null;
    private Label invalidLabel = null;
    private TextInput textinputIntRange = null;
    private TextInput textinputDateRegex = null;
    private TextInput textinputCustomBoolean = null;
    private TextInput textinputNotEmptyText = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = new Window((Component)bxmlSerializer.readObject(
            getClass().getResource("text_input_validator_test.bxml")));

        textinputFloatRange = (TextInput)bxmlSerializer.getNamespace().get("textinputFloatRange");
        textinputIntRange = (TextInput)bxmlSerializer.getNamespace().get("textinputIntRange");
        textinputDateRegex = (TextInput)bxmlSerializer.getNamespace().get("textinputDateRegex");
        textinputCustomBoolean = (TextInput)bxmlSerializer.getNamespace().get("textinputCustomBoolean");
        textinputNotEmptyText = (TextInput)bxmlSerializer.getNamespace().get("textinputNotEmptyText");

        // standard float range model
        textinputFloatRange.setText("0.5");
        textinputFloatRange.setValidator(new FloatRangeValidator(0.3f, 2000f));

        // test the listener by updating a label
        textinputFloatRange.getTextInputListeners().add(new TextInputListener.Adapter() {
            @Override
            public void textValidChanged(TextInput textInput) {
                invalidLabel.setText(textInput.isTextValid() ? "valid" : "invalid");
            }
        });

        invalidLabel = (Label)bxmlSerializer.getNamespace().get("invalidLabel");

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
            public boolean isValid(String s) {
                return "true".equals(s) || "false".equals(s);
            }
        });

        // validate any not-empty text
        textinputNotEmptyText.setText("  Not Empty, and with spaces  ");
        textinputNotEmptyText.setValidator(new NotEmptyTextValidator());


        window.setTitle("Text Input Validator Test");
        window.setMaximized(true);
        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public static void main(String[] args) throws Exception {
        DesktopApplicationContext.main(new String[] { TextInputValidatorTest.class.getName() });
    }

}
