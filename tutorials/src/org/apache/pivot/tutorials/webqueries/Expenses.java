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
package org.apache.pivot.tutorials.webqueries;

import java.net.URL;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;

/**
 * Query servlet tutorial client application.
 */
public final class Expenses implements Application {
    private String hostname = null;
    private int port = 0;
    private boolean secure = false;

    private ExpensesWindow expensesWindow = null;

    public static final String HOSTNAME_KEY = "hostname";
    public static final String PORT_KEY = "port";
    public static final String SECURE_KEY = "secure";

    public Expenses() {
    }

    @Override
    public void startup(final Display display, final Map<String, String> properties) throws Exception {
        // Get startup properties
        URL origin = ApplicationContext.getOrigin();
        if (origin == null) {
            System.out.println(
                "Warning: Origin null, so for this application to run you have to set the following properties:\n"
              + SECURE_KEY + ", " + HOSTNAME_KEY + ", " + PORT_KEY + "\n");
            System.exit(1);
            return; // make Eclipse's null checker happy
        }

        if (properties.containsKey(SECURE_KEY)) {
            secure = Boolean.parseBoolean(properties.get(SECURE_KEY));
        } else {
            secure = origin.getProtocol().equals("HTTPS");
        }

        if (properties.containsKey(HOSTNAME_KEY)) {
            hostname = properties.get(HOSTNAME_KEY);
        } else {
            hostname = origin.getHost();
        }

        if (properties.containsKey(PORT_KEY)) {
            port = Integer.parseInt(properties.get(PORT_KEY));
        } else {
            port = origin.getPort();
        }

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        expensesWindow = (ExpensesWindow) bxmlSerializer.readObject(ExpensesWindow.class,
            "expenses_window.bxml", true);
        expensesWindow.setExpensesApplication(this);
        expensesWindow.open(display);
    }

    @Override
    public boolean shutdown(final boolean optional) {
        if (expensesWindow != null) {
            expensesWindow.close();
        }

        return false;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public boolean isSecure() {
        return secure;
    }

    public static void main(final String[] args) {
        DesktopApplicationContext.main(Expenses.class, args);
    }

}
