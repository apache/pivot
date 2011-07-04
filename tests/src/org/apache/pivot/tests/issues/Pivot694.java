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
package org.apache.pivot.tests.issues;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Window;

public class Pivot694 implements Application {
    private Window window = null;
    private ListButton listButton1 = null;
    private PushButton pushButton1 = null;
    private ListButton listButton2 = null;
    private PushButton pushButton2 = null;

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        System.out.println("startup: start");

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window)bxmlSerializer.readObject(this.getClass(), "pivot_694.bxml");
        initializeFields(bxmlSerializer);
        window.open(display);

        System.out.println("startup: end");
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    private void initializeFields(BXMLSerializer serializer) {
        System.out.println("initializeFields: start");

        listButton1 = (ListButton)serializer.getNamespace().get("listButton1");
        pushButton1 = (PushButton)serializer.getNamespace().get("pushButton1");
        pushButton1.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                System.out.println("Clearing selection from " + button.getName());
                listButton1.clear();
            }
        });

        listButton2 = (ListButton)serializer.getNamespace().get("listButton2");
        pushButton2 = (PushButton)serializer.getNamespace().get("pushButton2");
        pushButton2.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                System.out.println("Clearing selection from " + button.getName());
                listButton2.clear();
            }
        });

        System.out.println("initializeFields: end");
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Pivot694.class, args);
    }

}
