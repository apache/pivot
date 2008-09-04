package pivot.wtk.test;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Dialog;
import pivot.wtk.DialogResultListener;
import pivot.wtk.Display;

public class ReflectionDecoratorTest implements Application {
    Display display = null;
    boolean shutdown = false;

    public void startup(Display display, Dictionary<String, String> properties) {
        this.display = display;
        System.out.println("startup()");
    }

    public boolean shutdown(boolean optional) {
        System.out.println("shutdown()");

        ArrayList<String> options = new ArrayList<String>();
        options.add("OK");
        options.add("Cancel");

        Alert alert = new Alert(Alert.Type.QUESTION, "Shutdown?", options);
        alert.open(display, new DialogResultListener() {
            public void resultReceived(Dialog dialog) {
                Alert alert = (Alert)dialog;

                if (alert.getResult()) {
                    if (alert.getSelectedOption() == 0) {
                        shutdown = true;
                        ApplicationContext.exit();
                    }
                }
            }
        });

        return shutdown;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
