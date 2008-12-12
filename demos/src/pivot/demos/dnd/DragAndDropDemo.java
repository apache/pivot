package pivot.demos.dnd;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.ListView;
import pivot.wtk.PushButton;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class DragAndDropDemo implements Application {
    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = new Window((Component)wtkxSerializer.readObject(getClass().getResource("drag_and_drop.wtkx")));

        // Text
        final Label label = (Label)wtkxSerializer.getObjectByName("label");
        label.setDragSource(null); // TODO
        label.setDropTarget(null); // TODO

        PushButton copyTextButton = (PushButton)wtkxSerializer.getObjectByName("copyTextButton");
        copyTextButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                // TODO
            }
        });

        PushButton pasteTextButton = (PushButton)wtkxSerializer.getObjectByName("pasteTextButton");
        pasteTextButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                // TODO
            }
        });

        // Images
        final ImageView imageView = (ImageView)wtkxSerializer.getObjectByName("imageView");
        imageView.setDragSource(null); // TODO
        imageView.setDropTarget(null); // TODO

        PushButton copyImageButton = (PushButton)wtkxSerializer.getObjectByName("copyImageButton");
        copyImageButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                // TODO
            }
        });

        PushButton pasteImageButton = (PushButton)wtkxSerializer.getObjectByName("pasteImageButton");
        pasteImageButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                // TODO
            }
        });

        // Files
        final ListView listView = (ListView)wtkxSerializer.getObjectByName("listView");
        listView.setDragSource(null); // TODO
        listView.setDropTarget(null); // TODO

        PushButton copyFilesButton = (PushButton)wtkxSerializer.getObjectByName("copyFilesButton");
        copyFilesButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                // TODO
            }
        });

        PushButton pasteFilesButton = (PushButton)wtkxSerializer.getObjectByName("pasteFilesButton");
        pasteFilesButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                // TODO
            }
        });

        // Open the window
        window.setTitle("Drag and Drop Demo");
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
