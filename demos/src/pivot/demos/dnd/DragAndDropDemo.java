package pivot.demos.dnd;

import pivot.collections.Dictionary;
import pivot.io.FileList;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Clipboard;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.DragSource;
import pivot.wtk.DropAction;
import pivot.wtk.DropTarget;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.ListView;
import pivot.wtk.Point;
import pivot.wtk.PushButton;
import pivot.wtk.Visual;
import pivot.wtk.Window;
import pivot.wtk.media.Image;
import pivot.wtkx.WTKXSerializer;

public class DragAndDropDemo implements Application {
    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = new Window((Component)wtkxSerializer.readObject(getClass().getResource("drag_and_drop.wtkx")));

        // Text
        final Label label = (Label)wtkxSerializer.getObjectByName("label");
        label.setDragSource(new DragSource() {
            public boolean beginDrag(Component component, int x, int y) {
                return (label.getText() != null);
            }

            public void endDrag(DropAction dropAction) {
            }

            public boolean isNative() {
                return true;
            }

            public Object getContent() {
                return label.getText();
            }

            public Visual getRepresentation() {
                return null;
            }

            public Point getOffset() {
                return null;
            }

            public int getSupportedDropActions() {
                return DropAction.COPY.getMask();
            }
        });

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
                label.setText((String)dragContent);
            }
        });

        PushButton copyTextButton = (PushButton)wtkxSerializer.getObjectByName("copyTextButton");
        copyTextButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                String text = label.getText();
                if (text != null) {
                    Clipboard.setContent(text);
                }
            }
        });

        PushButton pasteTextButton = (PushButton)wtkxSerializer.getObjectByName("pasteTextButton");
        pasteTextButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Object content = Clipboard.getContent();
                if (content instanceof String) {
                    label.setText((String)content);
                } else {
                    ApplicationContext.beep();
                }
            }
        });

        // Images
        final ImageView imageView = (ImageView)wtkxSerializer.getObjectByName("imageView");
        imageView.setDragSource(new DragSource() {
            public boolean beginDrag(Component component, int x, int y) {
                return (imageView.getImage() != null);
            }

            public void endDrag(DropAction dropAction) {
            }

            public boolean isNative() {
                return true;
            }

            public Object getContent() {
                return imageView.getImage();
            }

            public Visual getRepresentation() {
                return null;
            }

            public Point getOffset() {
                return null;
            }

            public int getSupportedDropActions() {
                return DropAction.COPY.getMask();
            }
        });

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
                imageView.setImage((Image)dragContent);
            }
        });

        PushButton copyImageButton = (PushButton)wtkxSerializer.getObjectByName("copyImageButton");
        copyImageButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Image image = imageView.getImage();
                if (image != null) {
                    Clipboard.setContent(image);
                }
            }
        });

        PushButton pasteImageButton = (PushButton)wtkxSerializer.getObjectByName("pasteImageButton");
        pasteImageButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Object content = Clipboard.getContent();
                if (content instanceof Image) {
                    imageView.setImage((Image)content);
                } else {
                    ApplicationContext.beep();
                }
            }
        });

        // Files
        final ListView listView = (ListView)wtkxSerializer.getObjectByName("listView");
        listView.setDragSource(new DragSource() {
            public boolean beginDrag(Component component, int x, int y) {
                return (listView.getListData() != null);
            }

            public void endDrag(DropAction dropAction) {
            }

            public boolean isNative() {
                return true;
            }

            public Object getContent() {
                return listView.getListData();
            }

            public Visual getRepresentation() {
                return null;
            }

            public Point getOffset() {
                return null;
            }

            public int getSupportedDropActions() {
                return DropAction.COPY.getMask();
            }
        });

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
                listView.setListData((FileList)dragContent);
            }
        });

        PushButton copyFilesButton = (PushButton)wtkxSerializer.getObjectByName("copyFilesButton");
        copyFilesButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                FileList files = (FileList)listView.getListData();
                if (files != null) {
                    Clipboard.setContent(files);
                }
            }
        });

        PushButton pasteFilesButton = (PushButton)wtkxSerializer.getObjectByName("pasteFilesButton");
        pasteFilesButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Object content = Clipboard.getContent();
                if (content instanceof FileList) {
                    listView.setListData((FileList)content);
                } else {
                    ApplicationContext.beep();
                }
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
