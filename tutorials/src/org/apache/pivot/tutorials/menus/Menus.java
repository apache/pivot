package org.apache.pivot.tutorials.menus;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Panel;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;

public class Menus implements Application {
    private Window window = null;
    private TabPane tabPane = null;

    public Menus() {
        Action.getNamedActions().put("fileNew", new Action() {
            @Override
            public void perform() {
                // TODO Read document.wtkx

                Panel panel = new Panel();
                tabPane.getTabs().add(panel);
                TabPane.setLabel(panel, "Document " + tabPane.getTabs().getLength());
            }
        });

        Action.getNamedActions().put("fileOpen", new Action() {
            @Override
            public void perform() {
                FileBrowserSheet fileBrowserSheet = new FileBrowserSheet(FileBrowserSheet.Mode.OPEN);
                fileBrowserSheet.open(window);
            }
        });

        Action.getNamedActions().put("cut", new Action() {
            @Override
            public void perform() {
                TextInput textInput = (TextInput)window.getFocusDescendant();
                textInput.cut();
            }
        });

        Action.getNamedActions().put("copy", new Action() {
            @Override
            public void perform() {
                TextInput textInput = (TextInput)window.getFocusDescendant();
                textInput.copy();
            }
        });

        Action.getNamedActions().put("paste", new Action() {
            @Override
            public void perform() {
                TextInput textInput = (TextInput)window.getFocusDescendant();
                textInput.paste();
            }
        });
    }

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "menus.wtkx");

        tabPane = (TabPane)wtkxSerializer.get("tabPane");

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Menus.class, args);
    }
}
