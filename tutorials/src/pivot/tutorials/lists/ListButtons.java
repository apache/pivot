package pivot.tutorials.lists;

import java.net.URL;
import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.ImageView;
import pivot.wtk.ListButton;
import pivot.wtk.ListButtonSelectionListener;
import pivot.wtk.Window;
import pivot.wtk.media.Image;
import pivot.wtkx.ComponentLoader;

public class ListButtons implements Application {
    private class ListButtonSelectionHandler
        implements ListButtonSelectionListener {
        @SuppressWarnings("unchecked")
        public void selectedIndexChanged(ListButton listButton, int previousIndex) {
            int index = listButton.getSelectedIndex();

            if (index != -1) {
                Object item = listButton.getListData().get(index);
                Dictionary<String, Object> dictionary =
                    (Dictionary<String, Object>)item;

                // Get the image URL for the selected item
                URL imageURL = (URL)dictionary.get("imageURL");
                ApplicationContext applicationContext =
                    ApplicationContext.getInstance();

                // If the image has not been added to the resource cache yet,
                // add it
                Image image =
                    (Image)applicationContext.getResources().get(imageURL);

                if (image == null) {
                    image = Image.load(imageURL);
                    applicationContext.getResources().put(imageURL, image);
                }

                // Update the image
                imageView.setImage(image);
            }
        }

    }

    private ImageView imageView = null;
    private Window window = null;

    public void startup() throws Exception {
        ComponentLoader.initialize();
        ComponentLoader componentLoader = new ComponentLoader();

        Component content =
            componentLoader.load("pivot/tutorials/lists/list_buttons.wtkx");

        imageView = (ImageView)componentLoader.getComponent("imageView");

        ListButton listButton =
            (ListButton)componentLoader.getComponent("listButton");

        listButton.getListButtonSelectionListeners().add(new
            ListButtonSelectionHandler());

        listButton.setSelectedIndex(0);

        window = new Window();
        window.setContent(content);
        window.getAttributes().put(Display.MAXIMIZED_ATTRIBUTE,
            Boolean.TRUE);
        window.open();
    }

    public void shutdown() throws Exception {
        window.close();
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }
}
