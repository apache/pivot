package pivot.wtk.test;

import java.awt.Font;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.Sequence;
import pivot.serialization.PlainTextSerializer;
import pivot.util.MIMEType;
import pivot.util.concurrent.TaskExecutionException;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.DragSource;
import pivot.wtk.DropAction;
import pivot.wtk.DropTarget;
import pivot.wtk.Frame;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Label;
import pivot.wtk.Point;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.Visual;
import pivot.wtk.data.ByteArrayTransport;
import pivot.wtk.data.Manifest;
import pivot.wtk.data.Transport;

public class NativeDragDropTest implements Application {
    private Frame frame = null;

    public void startup(final Display display, Dictionary<String, String> properties)
        throws Exception {
        final Label label = new Label("Drag Text Here");
        label.getStyles().put("font", new Font("Arial", Font.PLAIN, 24));
        label.getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
        label.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

        label.setDragSource(new DragSource() {
            private ArrayList<Transport> content = null;

            public boolean beginDrag(Component component, int x, int y) {
                content = new ArrayList<Transport>();

                PlainTextSerializer serializer = new PlainTextSerializer();
                content.add(new ByteArrayTransport(label.getText(), serializer));
                return true;
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
                DropAction dropAction = null;

                contentIndex = dragContent.getIndex("text/plain");
                if (contentIndex != -1) {
                    frame.getStyles().put("backgroundColor", "#ffcccc");
                    dropAction = DropAction.COPY;
                }

                return dropAction;
            }

            public void dragExit(Component component) {
                frame.getStyles().put("backgroundColor", "#ffffff");
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
                        Label label = (Label)component;
                        label.setText((String)readTask.execute());
                    } catch(TaskExecutionException exception) {
                        // No-op; we couldn't retrieve the text
                    }
                }

                dragExit(component);

                return dropAction;
            }
        });

        frame = new Frame(label);
        frame.open(display);
    }

    public boolean shutdown(boolean optional) {
        frame.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
