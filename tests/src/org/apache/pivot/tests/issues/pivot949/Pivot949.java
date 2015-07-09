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
package org.apache.pivot.tests.issues.pivot949;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Window;

public class Pivot949 extends Application.Adapter {

    private Window window = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        System.out.println("Pivot949 startup(...)");
        System.out.println("\n"
            + "Note that this issue is related to include scripts (with absolute path) "
            + "from a bxml file.\n"
            + "Here we'll test both relative and absolute scripts.\n"
            + "\n");

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(Pivot949.class, "pivot_949.bxml");
        System.out.println("got window = " + window);
        window.open(display);
    }

    @Override
    public boolean shutdown(boolean b) throws Exception {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Pivot949.class, args);
    }

}
