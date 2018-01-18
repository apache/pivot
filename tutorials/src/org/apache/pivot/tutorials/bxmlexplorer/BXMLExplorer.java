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
package org.apache.pivot.tutorials.bxmlexplorer;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.Window;

public class BXMLExplorer implements Application {

    /**
     * For the moment editing is experimental, so use a property to turn it on.
     */
    static final boolean ENABLE_EDITING = Boolean.getBoolean("edit");

    private BXMLExplorerWindow window = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (BXMLExplorerWindow) bxmlSerializer.readObject(BXMLExplorer.class,
            "bxml_explorer_window.bxml", true);
        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    static void displayLoadException(Throwable exception, Window window) {
        String message = exception.getClass().getName();

        TextArea body = null;
        String bodyText = exception.getMessage();
        if (bodyText != null && bodyText.length() > 0) {
            body = new TextArea();
            body.setText(bodyText);
            body.setEditable(false);
        }

        Alert.alert(MessageType.ERROR, message, null, body, window, null);
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(BXMLExplorer.class, args);
    }
}
