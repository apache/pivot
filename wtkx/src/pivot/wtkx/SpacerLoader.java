package pivot.wtkx;

import org.w3c.dom.Element;

import pivot.wtk.Component;
import pivot.wtk.Spacer;

class SpacerLoader extends Loader {
    public static final String SPACER_TAG = "Spacer";
    
    @Override
    protected Component load(Element element, ComponentLoader rootLoader)
        throws LoadException {
        return new Spacer();
    }
}
