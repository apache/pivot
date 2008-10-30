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
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class AMFDemo implements Application {
	private Window window = null;

	private TableView productTableView = null;
	private PushButton getDataButton = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = new Window((Component)wtkxSerializer.readObject(getClass().getResource("amf_demo.wtkx")));

        productTableView = (TableView)wtkxSerializer.getObjectByName("productTableView");
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
