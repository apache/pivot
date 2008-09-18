/*
 * Copyright (c) 2008 VMware, Inc.
 *
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
package pivot.wtk.test;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Dialog;
import pivot.wtk.DialogStateListener;
import pivot.wtk.Display;

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

        Alert alert = new Alert(Alert.Type.QUESTION, "Shutdown?", options);
        alert.open(display, new DialogStateListener() {
            public boolean previewDialogClose(Dialog dialog, boolean result) {
                return true;
            }

            public void dialogClosed(Dialog dialog) {
                Alert alert = (Alert)dialog;

                if (alert.getResult()) {
                    if (alert.getSelectedOption() == 0) {
                        shutdown = true;
                        ApplicationContext.exit();
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
