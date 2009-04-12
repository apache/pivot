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
package pivot.wtk.test;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Dialog;
import pivot.wtk.DialogCloseListener;
import pivot.wtk.Display;
import pivot.wtk.MessageType;

public class ReflectionDecoratorTest implements Application {
    Display display = null;
    boolean shutdown = false;

    public void startup(Display display, Dictionary<String, String> properties) {
        this.display = display;
        System.out.println("startup()");
    }

    public boolean shutdown(boolean optional) {
        System.out.println("shutdown()");

        ArrayList<String> options = new ArrayList<String>();
        options.add("OK");
        options.add("Cancel");

        Alert alert = new Alert(MessageType.QUESTION, "Shutdown?", options);
        alert.open(display, new DialogCloseListener() {
            public void dialogClosed(Dialog dialog) {
                Alert alert = (Alert)dialog;

                if (alert.getResult()) {
                    if (alert.getSelectedOption() == 0) {
                        shutdown = true;
                        DesktopApplicationContext.exit();
                    }
                }
            }
        });

        return shutdown;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
