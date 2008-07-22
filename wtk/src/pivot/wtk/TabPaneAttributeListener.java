package pivot.wtk;

import pivot.wtk.media.Image;

public interface TabPaneAttributeListener {
    public void iconChanged(TabPane tabPane, Component component, Image previousIcon);
    public void labelChanged(TabPane tabPane, Component component, String previousLabel);
}
