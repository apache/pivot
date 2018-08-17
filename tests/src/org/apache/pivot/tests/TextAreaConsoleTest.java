/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
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

import java.io.IOException;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Console;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.util.TextAreaOutputStream;


public class TextAreaConsoleTest implements Application {
    @BXML private Window window;
    @BXML private PushButton logMessageButton;
    @BXML private TextArea consoleArea;
    private Console console;
    private int line = 1;

    @Override
    public void startup(Display display, Map<String, String> properties) {
        BXMLSerializer serializer = new BXMLSerializer();
        try {
            serializer.readObject(TextAreaConsoleTest.class, "console_test.bxml");
            serializer.bind(this);
        } catch (IOException | SerializationException ex) {
            throw new RuntimeException(ex);
        }
        console = new Console(new TextAreaOutputStream(consoleArea).toPrintStream());
        logMessageButton.getButtonPressListeners().add(
            (button) -> console.log(String.format("%1$d. Hello, World!", line++)));
        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }
        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(TextAreaConsoleTest.class, args);
    }
}
