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

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.ApplicationWithProperties;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Window;

public class ApplicationWithPropertiesTest extends ApplicationWithProperties.Adapter {
    public static final String SAMPLE_PROP_KEY = "sample_object_from_external_env";
    public static final String SAMPLE_CLASSLOADER_PROP_KEY = "sample_classloader_from_external_env";

    private Window window = null;
    private Label sampleLabel = null;

    @Override
    public void startup(final Display display, final Map<String, String> properties)
        throws Exception {
        System.out.println("application startup(...)");

        // initializeProperties(properties);

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(ApplicationWithPropertiesTest.class,
            "application_with_properties_test.bxml");
        initializeFields(bxmlSerializer);
        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        System.out.println("application shutdown(" + optional + ")");

        if (window != null) {
            window.close();
        }

        return false;
    }

    private void initializeFields(BXMLSerializer serializer) {
        System.out.println("initializeFields: start");

        sampleLabel = (Label) serializer.getNamespace().get("label");
        System.out.println("label, original text (from bxml) = \"" + sampleLabel.getText() + "\"");

        updateLabel((String) this.getProperties().get(SAMPLE_PROP_KEY));
        setClassLoaderFromExternalProperty((ClassLoader) this.getProperties().get(SAMPLE_CLASSLOADER_PROP_KEY));

        System.out.println("initializeFields: end");
    }

    private void updateLabel(String text) {
        System.out.println("updateLabel with text = \"" + text + "\"");
        sampleLabel.setText(text);
    }

    private void setClassLoaderFromExternalProperty(ClassLoader classLoader) {
        System.out.println("setClassLoaderFromExternalProperty with value = " + classLoader);
        // this.classLoader = classLoader;
    }

    public static void main(String[] args) {
        System.out.println("main(...)");

        String sample = "Hello from external environment.";

        // instead of the usual call, here I create an instance of my
        // application, and then set some properties (from this "external"
        // environment) in it. Last, I set that instance in usual launcher, but
        // using a new execution method.
        ApplicationWithProperties application = new ApplicationWithPropertiesTest();
        System.out.println("application instance = " + application);

        application.getProperties().put(SAMPLE_PROP_KEY, sample);
        System.out.println("application sample external property \"" + SAMPLE_PROP_KEY + "\" = \""
            + application.getProperties().get(SAMPLE_PROP_KEY) + "\"");
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        application.getProperties().put(SAMPLE_CLASSLOADER_PROP_KEY, classLoader);
        System.out.println("application sample external classloader property \"" + SAMPLE_CLASSLOADER_PROP_KEY + "\" = \""
                + application.getProperties().get(SAMPLE_CLASSLOADER_PROP_KEY) + "\"");

        System.out.println("Executing application ...");
        DesktopApplicationContext.main(application, args);
    }
}
