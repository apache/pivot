package pivot.wtk.test;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Display;
import pivot.wtk.FlowPane;
import pivot.wtk.PushButton;
import pivot.wtk.Window;

public class PushButtonTest implements Application {
	private Window window = null;

	public void startup(Display display, Dictionary<String, String> properties)
		throws Exception {
		window = new Window();
		FlowPane flowPane = new FlowPane();

		PushButton pushButton = new PushButton("OK");
		pushButton.getStyles().put("preferredAspectRatio", 3.0f);
		flowPane.add(pushButton);

		window.setContent(flowPane);

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
