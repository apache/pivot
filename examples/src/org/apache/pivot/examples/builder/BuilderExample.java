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
package org.apache.pivot.examples.builder;

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TabPane;

public class BuilderExample implements Application {
    private MyWindow myWindow = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        myWindow = buildWindow();
        myWindow.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (myWindow != null) {
            myWindow.close();
        }

        return false;
    }

    private static MyWindow buildWindow() {
        final HashMap<String, Object> namespace = new HashMap<>();

        return new MyWindow() {
            {
                setContent(new TabPane() {
                    {
                        namespace.put("tabPane", this);

                        getTabs().add(new Label() {
                            {
                                setText("Label 1");
                                TabPane.setTabData(this, "Label 1");
                            }
                        });

                        getTabs().add(new Label() {
                            {
                                setText("Label 2");
                                TabPane.setTabData(this, "Label 2");
                            }
                        });

                        getTabs().add(new Label() {
                            {
                                setText("Label 3");
                                TabPane.setTabData(this, "Label 3");
                            }
                        });

                        setSelectedIndex(2);
                    }
                });

                setTitle("Builder Example");
                setMaximized(true);

                initialize(namespace, null, null);
            }
        };
    }
}
