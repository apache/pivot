package pivot.tutorials.databinding;

import java.io.InputStream;

import pivot.collections.Dictionary;
import pivot.collections.Map;
import pivot.serialization.JSONSerializer;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Form;
import pivot.wtk.Label;
import pivot.wtk.PushButton;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class DataBinding implements Application {
	private Window window = null;
	private Form form = null;
	private PushButton loadJavaButton = null;
	private PushButton loadJSONButton = null;
	private Label sourceLabel = null;

	private static final Contact CONTACT = new Contact("101", "Joe Smith",
		new Address("123 Main St.", "Cambridge", "MA", "02142"),
		"(617) 555-1234", "joe_smith@foo.com",
		new IMAccount("jsmith1234", "AIM"));

	public void startup(Display display, Dictionary<String, String> properties)
		throws Exception {
		// Load the UI
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = new Window((Component)wtkxSerializer.readObject(getClass().getResource("data_binding.wtkx")));
        form = (Form)wtkxSerializer.getObjectByName("form");
        loadJavaButton = (PushButton)wtkxSerializer.getObjectByName("loadJavaButton");
        loadJSONButton = (PushButton)wtkxSerializer.getObjectByName("loadJSONButton");
        sourceLabel = (Label)wtkxSerializer.getObjectByName("sourceLabel");

        // Open the window
        window.setMaximized(true);
        window.open(display);

        loadJavaButton.getButtonPressListeners().add(new ButtonPressListener() {
        	public void buttonPressed(Button button) {
        		form.load(CONTACT);
        		sourceLabel.setText("Java");
        	}
        });

        loadJSONButton.getButtonPressListeners().add(new ButtonPressListener() {
        	@SuppressWarnings("unchecked")
        	public void buttonPressed(Button button) {
        		JSONSerializer serializer = new JSONSerializer();
        		InputStream inputStream = getClass().getResourceAsStream("contact.json");

        		try {
            		form.load((Map<String, Object>)serializer.readObject(inputStream));
            		sourceLabel.setText("JSON");
        		} catch(Exception exception) {
        			System.out.println(exception);
        		}

        		button.setEnabled(true);
        	}
        });
	}

	public boolean shutdown(boolean optional) {
		window.close();
		return false;
	}


	public void suspend() {
	}

	public void resume() {
	}
}
