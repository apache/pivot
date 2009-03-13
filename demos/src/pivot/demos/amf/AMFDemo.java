/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.demos.amf;

import flex.messaging.io.amf.client.AMFConnection;

import pivot.collections.Dictionary;
import pivot.collections.adapter.ListAdapter;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.PushButton;
import pivot.wtk.TableView;
import pivot.wtk.TableViewHeader;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class AMFDemo implements Application {
	private Window window = null;

	private TableView productTableView = null;
	private TableViewHeader productTableViewHeader = null;
	private PushButton getDataButton = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = new Window((Component)wtkxSerializer.readObject(getClass().getResource("amf_demo.wtkx")));

        productTableView = (TableView)wtkxSerializer.getObjectByName("productTableView");

        productTableViewHeader = (TableViewHeader)wtkxSerializer.getObjectByName("productTableViewHeader");
        productTableViewHeader.getTableViewHeaderPressListeners().add(new TableView.SortHandler());

        getDataButton = (PushButton)wtkxSerializer.getObjectByName("getDataButton");

        getDataButton.getButtonPressListeners().add(new ButtonPressListener() {
        	@SuppressWarnings("unchecked")
        	public void buttonPressed(Button button) {
        		AMFConnection amfConnection = new AMFConnection();
        		String url = "http://localhost:8080/samples/messagebroker/amf";

        		Object result = null;
        		try {
        		    amfConnection.connect(url);
        		    result = amfConnection.call("product.getProducts");
        			java.util.ArrayList<Object> products = (java.util.ArrayList<Object>)result;
        			productTableView.setTableData(new ListAdapter<Object>(products));
        		} catch(Exception exception) {
        		    System.out.println(exception);
        		} finally {
        			amfConnection.close();
        		}
        	}
        });

        window.setMaximized(true);
        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        window.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
