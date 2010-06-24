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

import org.apache.pivot.beans.BeanSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;

/**
 * Query servlet tutorial client application.
 */
public class Expenses implements Application {
    private String hostname = null;
    private int port = 0;
    private boolean secure = false;

    private ExpensesWindow expensesWindow = null;

    public static final String HOSTNAME_KEY = "hostname";
    public static final String PORT_KEY = "port";
    public static final String SECURE_KEY = "secure";

    private static Expenses instance = null;

    public Expenses() {
        instance = this;
    }

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        // Get startup properties
        URL origin = ApplicationContext.getOrigin();

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

        if (properties.containsKey(SECURE_KEY)) {
            secure = Boolean.parseBoolean(properties.get(SECURE_KEY));
        } else {
            secure = origin.getProtocol().equals("HTTPS");
        }

        BeanSerializer beanSerializer = new BeanSerializer(new Resources(ExpensesWindow.class.getName()));
        expensesWindow = (ExpensesWindow)beanSerializer.readObject(this, "expenses_window.bxml");
        expensesWindow.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (expensesWindow != null) {
            expensesWindow.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
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

    public static Expenses getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Expenses.class, args);
    }
}
