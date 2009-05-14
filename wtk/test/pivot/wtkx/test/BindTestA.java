package pivot.wtkx.test;

import pivot.wtk.Window;
import pivot.wtkx.Bindable;

public class BindTestA extends Bindable {
    @Load(resourceName="bind_test.wtkx")
    protected Window window;
}
