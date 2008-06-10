package pivot.tutorials.lists;

import pivot.collections.Dictionary;
import pivot.collections.Sequence;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Label;
import pivot.wtk.ListView;
import pivot.wtk.ListViewSelectionListener;
import pivot.wtk.Span;
import pivot.wtk.Window;
import pivot.wtkx.ComponentLoader;

public class ListViews implements Application {
    private Window window = null;

    @SuppressWarnings("unchecked")
    public void startup() throws Exception {
        ComponentLoader.initialize();
        ComponentLoader componentLoader = new ComponentLoader();

        Component content =
            componentLoader.load("pivot/tutorials/lists/list_views.wtkx");

        final Label selectionLabel =
            (Label)componentLoader.getComponent("selectionLabel");

        ListView listView = (ListView)componentLoader.getComponent("listView");
        listView.getListViewSelectionListeners().add(new ListViewSelectionListener() {
            public void selectionChanged(ListView listView) {
                String selectionText = "";

                Sequence<Span> selectedRanges = listView.getSelectedRanges();
                for (int i = 0, n = selectedRanges.getLength(); i < n; i++) {
                    Span selectedRange = selectedRanges.get(i);

                    for (int j = selectedRange.getStart();
                        j <= selectedRange.getEnd();
                        j++) {
                        Object item = listView.getListData().get(j);
                        Dictionary<String, Object> dictionary =
                            (Dictionary<String, Object>)item;

                        if (selectionText.length() > 0) {
                            selectionText += ", ";
                        }

                        selectionText += dictionary.get("label");
                    }
                }

                selectionLabel.setText(selectionText);
            }
        });

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
