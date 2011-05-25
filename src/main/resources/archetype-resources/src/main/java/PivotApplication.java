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
package ${package};

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.wtk.*;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Display;

/**
 * This demo application can either be started with mvn exec:java
 * or by starting org.apache.pivot.wtk.DesktopApplicationContext with
 * ${package}.PivotApplication as the first argument.
 * For simplicity, a main method is included in this class as well.
 * 
 * Remember to run mvn compile before running mvn exec:java
 */
public class PivotApplication implements Application {

	PivotApplicationWindow window = null;


	/**
	 * Command-line Entry point, as Application.
	 */
	public static void main(String[] args) {
		DesktopApplicationContext.main(new String[] { "${package}.PivotApplication" });
	}

    /**
     * Called when the application is starting up.
     *
     * @param display
     * The display on which this application was started.
     *
     * @param properties
     * Initialization properties passed to the application.
     */
    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer serializer = new BXMLSerializer();
        window = (PivotApplicationWindow) serializer.readObject(getClass().getResource("PivotApplicationWindow.bxml"));
        window.open(display);
    }

    /**
     * Called when the application is being shut down.
     *
     * @param optional
     * If <tt>true</tt>, the shutdown may be cancelled by returning a value of
     * <tt>true</tt>.
     *
     * @return
     * <tt>true</tt> to cancel shutdown, <tt>false</tt> to continue.
     */
    @Override
    public boolean shutdown(boolean optional) throws Exception {
        if (window != null) {
            window.close();
        }

        return false;
    }

    /**
     * Called to notify the application that it is being suspended.
     */
    @Override
    public void suspend() throws Exception {
    }

    /**
     * Called when a suspended application has been resumed.
     */
    @Override
    public void resume() throws Exception {
    }

}
