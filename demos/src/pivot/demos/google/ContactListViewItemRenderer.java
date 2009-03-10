package pivot.demos.google;

import java.awt.Color;
import java.awt.Font;

import com.google.gdata.data.contacts.ContactEntry;

import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Insets;
import pivot.wtk.Label;
import pivot.wtk.ListView;
import pivot.wtk.VerticalAlignment;

public class ContactListViewItemRenderer extends Label
	implements ListView.ItemRenderer {
    public ContactListViewItemRenderer() {
        getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        getStyles().put("padding", new Insets(2));
    }

    public void render(Object item, ListView listView, boolean selected,
        boolean checked, boolean highlighted, boolean disabled) {
    	ContactEntry contactEntry = (ContactEntry)item;
    	String text = contactEntry.getTitle().getPlainText();
    	setText(text);

        Object font = listView.getStyles().get("font");
        if (font instanceof Font) {
            getStyles().put("font", font);
        }

        Object color = null;
        if (listView.isEnabled() && !disabled) {
            if (selected) {
                if (listView.isFocused()) {
                    color = listView.getStyles().get("selectionColor");
                } else {
                    color = listView.getStyles().get("inactiveSelectionColor");
                }
            } else {
                color = listView.getStyles().get("color");
            }
        } else {
            color = listView.getStyles().get("disabledColor");
        }

        if (color instanceof Color) {
            getStyles().put("color", color);
        }
    }
}
