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
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.ButtonDataRenderer;

public class Pivot721 implements Application {

    private Window window;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(Pivot721.class, "pivot_721.bxml");

        // force fill into button renderer, but only in some buttons ...
        ButtonDataRenderer filledButtonDataRenderer = new ButtonDataRenderer();
        filledButtonDataRenderer.setFillIcon(true);
        PushButton button3 = (PushButton) bxmlSerializer.getNamespace().get("button3");
        // ((ButtonDataRenderer)button3.getDataRenderer()).setFillIcon(true); //
        // ok, but note that all buttons share a common renderer instance
        button3.setDataRenderer(filledButtonDataRenderer); // set/use the
                                                           // customized
                                                           // renderer instance
        PushButton button4 = (PushButton) bxmlSerializer.getNamespace().get("button4");
        button4.setDataRenderer(filledButtonDataRenderer); // set/use the
                                                           // customized
                                                           // renderer instance

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
        DesktopApplicationContext.main(Pivot721.class, args);
    }

}
