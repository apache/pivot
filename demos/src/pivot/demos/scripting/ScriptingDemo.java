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
package pivot.demos.scripting;

import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.Window;
import pivot.wtkx.WTKX;
import pivot.wtkx.WTKXSerializer;

public class ScriptingDemo implements Application {
    public static class MyButtonPressListener implements ButtonPressListener {
        public void buttonPressed(Button button) {
            System.out.println("[Java] A button was clicked.");
        }
    }

    private Window window = null;

    @WTKX private String foo;
    @WTKX private List<?> listData;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "scripting_demo.wtkx");
        wtkxSerializer.bind(this);

        System.out.println("foo = " + (foo == null ? null : "\"" + foo + "\""));
        System.out.println("listData.getLength() = " + listData.getLength());

        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(ScriptingDemo.class, args);
    }
}
