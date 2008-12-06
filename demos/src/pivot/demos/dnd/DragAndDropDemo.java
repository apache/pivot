package pivot.demos.dnd;

import pivot.collections.Dictionary;
import pivot.io.FileList;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.DropAction;
import pivot.wtk.DropTarget;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.ListView;
import pivot.wtk.Window;
import pivot.wtk.media.Image;
import pivot.wtkx.WTKXSerializer;

public class DragAndDropDemo implements Application {
    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = new Window((Component)wtkxSerializer.readObject(getClass().getResource("drag_and_drop.wtkx")));

        Label label = (Label)wtkxSerializer.getObjectByName("label");
        label.setDropTarget(new DropTarget() {
            public DropAction getDropAction(Component component, Class<?> dragContentType,
                int supportedDropActions, DropAction userDropAction, int x, int y) {
                return (dragContentType == String.class) ? DropAction.COPY : null;
            }

            public void showDropState(Component component, Class<?> dragContentType,
                DropAction dropAction) {
            }

            public void hideDropState(Component component) {
            }

            public void updateDropState(Component component, DropAction dropAction, int x, int y) {
            }

            public void drop(Component component, Object dragContent, DropAction dropAction,
                int x, int y) {
                Label label = (Label)component;
                label.setText((String)dragContent);
            }
        });

        ImageView imageView = (ImageView)wtkxSerializer.getObjectByName("imageView");
        imageView.setDropTarget(new DropTarget() {
            public DropAction getDropAction(Component component, Class<?> dragContentType,
                int supportedDropActions, DropAction userDropAction, int x, int y) {
                return (Image.class.isAssignableFrom(dragContentType)) ? DropAction.COPY : null;
            }

            public void showDropState(Component component, Class<?> dragContentType,
                DropAction dropAction) {
            }

            public void hideDropState(Component component) {
            }

            public void updateDropState(Component component, DropAction dropAction, int x, int y) {
            }

            public void drop(Component component, Object dragContent, DropAction dropAction,
                int x, int y) {
                ImageView imageView = (ImageView)component;
                imageView.setImage((Image)dragContent);
            }
        });

        ListView listView = (ListView)wtkxSerializer.getObjectByName("listView");
        listView.setDropTarget(new DropTarget() {
            public DropAction getDropAction(Component component, Class<?> dragContentType,
                int supportedDropActions, DropAction userDropAction, int x, int y) {
                return (dragContentType == FileList.class) ? DropAction.COPY : null;
            }

            public void showDropState(Component component, Class<?> dragContentType,
                DropAction dropAction) {
            }

            public void hideDropState(Component component) {
            }

            public void updateDropState(Component component, DropAction dropAction, int x, int y) {
            }

            public void drop(Component component, Object dragContent, DropAction dropAction,
                int x, int y) {
                ListView listView = (ListView)component;
                listView.setListData((FileList)dragContent);
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
