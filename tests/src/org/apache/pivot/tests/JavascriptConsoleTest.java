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

import java.io.IOException;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Window;

public class JavascriptConsoleTest extends Application.Adapter {
    private Display display = null;
    private Window window = null;

    @Override
    public void startup(Display displayArgument, Map<String, String> properties) throws Exception {
        System.out.println("startup: start");

        this.display = displayArgument;

        BXMLSerializer bxmlSerializer = new BXMLSerializer();

        // add a reference to the application itself in bxml namespace, to be used by JS inside bxml files
        bxmlSerializer.getNamespace().put("application", this);
        System.out.println("put a reference to application in serializer namespace");

        window = loadWindow("javascript_console_test.bxml", bxmlSerializer);
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

    private void initializeFields(BXMLSerializer serializer) {
        System.out.println("initializeFields: start");

        System.out.println("got BXMLSerializer instance = " + serializer);

        System.out.println("initializeFields: end");
    }

    protected Window loadWindow(String fileName, BXMLSerializer bxmlSerializer)
        throws SerializationException, IOException {
        if (bxmlSerializer == null) {
            bxmlSerializer = new BXMLSerializer();
        }
        return (Window)bxmlSerializer.readObject(JavascriptConsoleTest.class, fileName);
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(JavascriptConsoleTest.class, args);
    }

}
