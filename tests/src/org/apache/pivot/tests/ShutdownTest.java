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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.DialogCloseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.MessageType;

public class ShutdownTest implements Application {
    private Display display = null;
    private Alert alert = null;
    private boolean cancelShutdown = true;

    @Override
    public void startup(Display displayArgument, Map<String, String> properties) {
        this.display = displayArgument;
        cancelShutdown = true;

        System.out.println("startup()");
    }

    @Override
    public boolean shutdown(boolean optional) {
        System.out.println("shutdown()");

        if (alert == null) {
            ArrayList<String> options = new ArrayList<>();
            options.add("Yes");
            options.add("No");

            alert = new Alert(MessageType.QUESTION, "Cancel shutdown?", options);
            alert.setModal(false);
            alert.open(display, new DialogCloseListener() {
                @Override
                public void dialogClosed(Dialog dialog, boolean modal) {
                    if (!(dialog instanceof Alert)) {
                        return;
                    }

                    Alert alertLocal = (Alert) dialog;
                    if (alertLocal.getResult()) {
                        if (alertLocal.getSelectedOptionIndex() == 1) {
                            cancelShutdown = false;
                            DesktopApplicationContext.exit();
                        }
                    }

                    ShutdownTest.this.alert = null;
                }
            });
        }

        return cancelShutdown;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(ShutdownTest.class, args);
    }

}
