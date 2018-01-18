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
package org.apache.pivot.tests.issues.pivot861;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;

/**
 * Test application to show the memory leak (the subject for this bug). For more
 * info, look at LeakTestWindow.
 */
public class Pivot861 implements Application {

    @Override
    public void startup(final Display display, Map<String, String> args) throws Exception {
        System.out.println("Pivot861 startup(...)");
        System.out.println("\n"
            + "Attention: now the application will go in an infinite loop, to be able to see the memory leak.\n"
            + "Note that probably you'll have to kill the application from outside (kill the Java process).\n"
            + "\n");

        // add some sleep to let users see the warning messages in console ...
        Thread.sleep(2000);

        LeakTestWindow window = LeakTestWindow.create();
        window.open(display);
    }

    @Override
    public boolean shutdown(boolean b) throws Exception {
        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Pivot861.class, args);
    }

}
