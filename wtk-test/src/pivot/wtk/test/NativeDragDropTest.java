package pivot.wtk.test;

import java.awt.Font;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Display;
import pivot.wtk.DragSource;
import pivot.wtk.DropAction;
import pivot.wtk.DropTarget;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Label;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.Visual;
import pivot.wtk.Window;

public class NativeDragDropTest implements Application {
    private Window window = null;

    public void startup(final Display display, Dictionary<String, String> properties)
        throws Exception {
        final Label label = new Label("Drag Text Here");
        label.getStyles().put("font", new Font("Arial", Font.PLAIN, 24));
        label.getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
        label.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

        label.setDragSource(new DragSource() {
            public boolean beginDrag(Component component, int x, int y) {
                return true;
            }

            public void endDrag(DropAction dropAction) {
            }

            public Object getContent() {
                return label.getText();
            }

            public Class<?> getContentType() {
                return String.class;
            }

            public Visual getRepresentation() {
                return null;
            }

            public Dimensions getOffset() {
                return null;
            }

            public int getSupportedDropActions() {
                return DropAction.COPY.getMask();
            }
        });

        label.setDropTarget(new DropTarget() {
            public DropAction getDropAction(Component component, Class<?> contentType,
                int x, int y) {
                return DropAction.COPY;
            }

            public void drop(Component component, Object content, int x, int y) {
                String text = (content == null) ? null : content.toString();
                Label label = (Label)component;
                label.setText(text);
            }
        });

        window = new Window(label);
        window.setMaximized(true);
        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
