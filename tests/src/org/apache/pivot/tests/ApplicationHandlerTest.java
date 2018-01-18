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

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Window;

public class ApplicationHandlerTest implements Application, Application.UnprocessedKeyHandler {
    private Window window = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(ApplicationHandlerTest.class,
            "application_handler_test.bxml");
        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void keyTyped(char character) {
        System.out.println("Unprocessed key typed: " + character);

        if (character == 'a') {
            throw new RuntimeException("'a' typed");
        }
    }

    @Override
    public void keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        System.out.println("Unprocessed key pressed: " + keyCode + "; " + keyLocation);
    }

    @Override
    public void keyReleased(int keyCode, Keyboard.KeyLocation keyLocation) {
        System.out.println("Unprocessed key released: " + keyCode + "; " + keyLocation);
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(ApplicationHandlerTest.class, args);
    }

}
