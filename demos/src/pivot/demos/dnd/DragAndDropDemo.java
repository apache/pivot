package pivot.demos.dnd;

import java.awt.image.BufferedImage;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.net.URIListSerializer;
import pivot.serialization.PlainTextSerializer;
import pivot.util.MIMEType;
import pivot.util.concurrent.TaskExecutionException;
import pivot.wtk.Application;
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
import pivot.wtk.data.ByteArrayTransport;
import pivot.wtk.data.Manifest;
import pivot.wtk.data.Transport;
import pivot.wtk.media.BufferedImageSerializer;
import pivot.wtk.media.Image;
import pivot.wtk.media.Picture;
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
            private ArrayList<Transport> content = null;

            public boolean beginDrag(Component component, int x, int y) {
                String text = label.getText();
                if (text != null) {
                    content = new ArrayList<Transport>();

                    PlainTextSerializer serializer = new PlainTextSerializer();
                    content.add(new ByteArrayTransport(label.getText(), serializer));
                }

                return (content != null);
            }

            public void endDrag(Component component, DropAction dropAction) {
                content = null;
            }

            public boolean isNative() {
                return true;
            }

            public Sequence<Transport> getContent() {
                return content;
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
            private int contentIndex = -1;

            public DropAction dragEnter(Component component, Manifest dragContent,
                int supportedDropActions, DropAction userDropAction) {
                if (DropAction.COPY.isSelected(supportedDropActions)) {
                    contentIndex = dragContent.getIndex("text/plain");
                }

                return (contentIndex == -1 ? null : DropAction.COPY);
            }

            public void dragExit(Component component) {
                contentIndex = -1;
            }

            public DropAction dragMove(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (contentIndex == -1 ? null : DropAction.COPY);
            }

            public DropAction userDropActionChange(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (contentIndex == -1 ? null : DropAction.COPY);
            }

            public DropAction drop(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                DropAction dropAction = null;

                if (contentIndex != -1) {
                    MIMEType mimeType = MIMEType.decode(dragContent.getMIMEType(contentIndex));
                    PlainTextSerializer serializer = new PlainTextSerializer(mimeType.get("charset"));
                    Manifest.ReadTask readTask = new Manifest.ReadTask(dragContent, contentIndex, serializer);

                    try {
                        label.setText((String)readTask.execute());
                    } catch(TaskExecutionException exception) {
                        // No-op; we couldn't retrieve the text
                    }
                }

                dragExit(component);

                return dropAction;
            }
        });

        PushButton copyTextButton = (PushButton)wtkxSerializer.getObjectByName("copyTextButton");
        copyTextButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                String text = label.getText();
                ArrayList<Transport> clipboardContent = new ArrayList<Transport>();
                clipboardContent.add(new ByteArrayTransport(text, new PlainTextSerializer()));
                Clipboard.setContent(clipboardContent);
            }
        });

        PushButton pasteTextButton = (PushButton)wtkxSerializer.getObjectByName("pasteTextButton");
        pasteTextButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Manifest clipboardContent = Clipboard.getContent();

                if (clipboardContent != null) {
                    int textIndex = clipboardContent.getIndex("text/plain");

                    if (textIndex != -1) {
                        // Paste the string representation of the content
                        MIMEType mimeType = MIMEType.decode(clipboardContent.getMIMEType(textIndex));
                        PlainTextSerializer serializer = new PlainTextSerializer(mimeType.get("charset"));

                        Manifest.ReadTask readTask = new Manifest.ReadTask(clipboardContent, textIndex, serializer);

                        String text = null;
                        try {
                            text = (String)readTask.execute();
                        } catch(TaskExecutionException exception) {
                            // No-op; we couldn't retrieve the text
                        }

                        label.setText(text);
                    }
                }
            }
        });

        // Images
        final ImageView imageView = (ImageView)wtkxSerializer.getObjectByName("imageView");
        imageView.setDragSource(new DragSource() {
            private ArrayList<Transport> content = null;

            public boolean beginDrag(Component component, int x, int y) {
                Image image = imageView.getImage();

                if (image instanceof Picture) {
                    Picture picture = (Picture)image;
                    content = new ArrayList<Transport>();

                    BufferedImageSerializer serializer = new BufferedImageSerializer();
                    serializer.setOutputFormat(BufferedImageSerializer.Format.PNG);
                    content.add(new ByteArrayTransport(picture.getBufferedImage(), serializer));
                }

                return (content != null);
            }

            public void endDrag(Component component, DropAction dropAction) {
                content = null;
            }

            public boolean isNative() {
                return true;
            }

            public Sequence<Transport> getContent() {
                return content;
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
            private int contentIndex = -1;

            public DropAction dragEnter(Component component, Manifest dragContent,
                int supportedDropActions, DropAction userDropAction) {
                if (DropAction.COPY.isSelected(supportedDropActions)) {
                    contentIndex = dragContent.getIndex(BufferedImageSerializer.Format.PNG.getMIMEType());
                }

                return (contentIndex == -1 ? null : DropAction.COPY);
            }

            public void dragExit(Component component) {
                contentIndex = -1;
            }

            public DropAction dragMove(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (contentIndex == -1 ? null : DropAction.COPY);
            }

            public DropAction userDropActionChange(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (contentIndex == -1 ? null : DropAction.COPY);
            }

            public DropAction drop(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                DropAction dropAction = null;

                if (contentIndex != -1) {
                    BufferedImageSerializer serializer = new BufferedImageSerializer();
                    Manifest.ReadTask readTask = new Manifest.ReadTask(dragContent, contentIndex, serializer);

                    try {
                        imageView.setImage(new Picture((BufferedImage)readTask.execute()));
                        dropAction = DropAction.COPY;
                    } catch(TaskExecutionException exception) {
                        // No-op; we couldn't set the image
                    }
                }

                dragExit(component);

                return dropAction;
            }
        });

        PushButton copyImageButton = (PushButton)wtkxSerializer.getObjectByName("copyImageButton");
        copyImageButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Image image = imageView.getImage();
                if (image instanceof Picture) {
                    Picture picture = (Picture)image;

                    ArrayList<Transport> clipboardContent = new ArrayList<Transport>();
                    BufferedImageSerializer serializer = new BufferedImageSerializer();
                    serializer.setOutputFormat(BufferedImageSerializer.Format.PNG);
                    clipboardContent.add(new ByteArrayTransport(picture.getBufferedImage(), serializer));
                    Clipboard.setContent(clipboardContent);
                }
            }
        });

        PushButton pasteImageButton = (PushButton)wtkxSerializer.getObjectByName("pasteImageButton");
        pasteImageButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Manifest clipboardContent = Clipboard.getContent();

                if (clipboardContent != null) {
                    int imageIndex = clipboardContent.getIndex(BufferedImageSerializer.Format.PNG.getMIMEType());

                    if (imageIndex != -1) {
                        BufferedImageSerializer serializer = new BufferedImageSerializer();
                        Manifest.ReadTask readTask = new Manifest.ReadTask(clipboardContent, imageIndex, serializer);

                        try {
                            imageView.setImage(new Picture((BufferedImage)readTask.execute()));
                        } catch(TaskExecutionException exception) {
                            // No-op; we couldn't set the image
                        }
                    }
                }
            }
        });

        // Files
        final ListView listView = (ListView)wtkxSerializer.getObjectByName("listView");
        listView.setDragSource(new DragSource() {
            private ArrayList<Transport> content = null;

            public boolean beginDrag(Component component, int x, int y) {
                ListView listView = (ListView)component;
                List<?> urlList = listView.getListData();

                if (urlList.getLength() > 0) {
                    content = new ArrayList<Transport>();

                    URIListSerializer serializer = new URIListSerializer();
                    content.add(new ByteArrayTransport(urlList, serializer));
                }

                return (content != null);
            }

            public void endDrag(Component component, DropAction dropAction) {
                content = null;
            }

            public boolean isNative() {
                return true;
            }

            public Sequence<Transport> getContent() {
                return content;
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
            private int contentIndex = -1;

            public DropAction dragEnter(Component component, Manifest dragContent,
                int supportedDropActions, DropAction userDropAction) {
                if (DropAction.COPY.isSelected(supportedDropActions)) {
                    contentIndex = dragContent.getIndex("text/uri-list");
                }

                return (contentIndex == -1 ? null : DropAction.COPY);
            }

            public void dragExit(Component component) {
                contentIndex = -1;
            }

            public DropAction dragMove(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (contentIndex == -1 ? null : DropAction.COPY);
            }

            public DropAction userDropActionChange(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (contentIndex == -1 ? null : DropAction.COPY);
            }

            public DropAction drop(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                DropAction dropAction = null;

                if (contentIndex != -1) {
                    MIMEType mimeType = MIMEType.decode(dragContent.getMIMEType(contentIndex));
                    URIListSerializer serializer = new URIListSerializer(mimeType.get("charset"));
                    Manifest.ReadTask readTask = new Manifest.ReadTask(dragContent, contentIndex, serializer);

                    try {
                        ListView listView = (ListView)component;
                        listView.setListData((List<?>)readTask.execute());
                    } catch(TaskExecutionException exception) {
                        // No-op; we couldn't retrieve the list
                    }
                }

                dragExit(component);

                return dropAction;
            }
        });

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
