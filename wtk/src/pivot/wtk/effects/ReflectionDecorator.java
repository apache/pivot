package pivot.wtk.effects;

import java.awt.Graphics2D;

import pivot.wtk.Component;
import pivot.wtk.ComponentListener;
import pivot.wtk.Container;
import pivot.wtk.Cursor;
import pivot.wtk.Decorator;
import pivot.wtk.Display;
import pivot.wtk.Skin;

public class ReflectionDecorator implements Decorator {
    private static class ComponentHandler implements ComponentListener {
        public void skinClassChanged(Component component, Class<? extends Skin> previousSkinClass) {
            // No-op
        }

        public void parentChanged(Component component, Container previousParent) {
            // TODO
        }

        public void sizeChanged(Component component, int previousWidth, int previousHeight) {
            // TODO
        }

        public void locationChanged(Component component, int previousX, int previousY) {
            // TODO
        }

        public void visibleChanged(Component component) {
            // TODO
        }

        public void styleUpdated(Component component, String styleKey, Object previousValue) {
            // No-op
        }

        public void cursorChanged(Component component, Cursor previousCursor) {
            // No-op
        }

        public void tooltipTextChanged(Component component, String previousTooltipText) {
            // No-op
        }
    }

    private Component component = null;

    public void install(Component component) {
        // TODO Auto-generated method stub

    }

    public void uninstall() {
        // TODO Auto-generated method stub

    }

    public Graphics2D prepare(Component component, Graphics2D graphics) {
        // TODO Auto-generated method stub
        return null;
    }

    public void update() {
        // TODO Auto-generated method stub

    }

}
