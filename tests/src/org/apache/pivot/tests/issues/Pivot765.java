package org.apache.pivot.tests.issues;

import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.*;

/**
 * This test will check that the previewWindowOpen method is called
 * before the ListPopup of the MenuButton is opened. This is crucial because
 * one need to populate the Menu before the Window opens, so that correct sizing
 * and layout can be performed.
 */
public class Pivot765 implements Application {
    private boolean menuPopulated = false;

    public static void main(String[] args) {
        DesktopApplicationContext.main( new String[] { Pivot765.class.getName() });
    }

    public void startup(final Display display, Map<String, String> properties) throws Exception {
        final MenuButton button = new MenuButton();
        button.setButtonData("Populate menu and open!");
        Window window = new Window(button);

        button.getListPopup().getWindowStateListeners().add(new WindowStateListener.Adapter() {
            public Vote previewWindowOpen(Window window) {
                Menu menu = new Menu();
                Menu.Section section = new Menu.Section();
                menu.getSections().add(section);
                section.add(new Menu.Item("A dynamically added menu item"));
                button.setMenu(menu);

                menuPopulated = true;
                return Vote.APPROVE;
            }

            public void windowOpened(Window window) {
                if (!menuPopulated)
                    Alert.alert("Window was opened before the menu was populated." +
                            "Either previewWindowOpen threw an exception, or it wasn't called before the Window was opened.", window);
            }

            public void windowClosed(Window window, Display display, Window owner) {
                // Remove menu for subsequent open attempt
                button.setMenu(null);
                menuPopulated = false;
            }
        });


        window.open(display);
    }

    public boolean shutdown(boolean optional) throws Exception {
        return false;
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }
}
