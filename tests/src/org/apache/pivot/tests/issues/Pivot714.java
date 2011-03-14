package org.apache.pivot.tests.issues;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.CalendarButton;
import org.apache.pivot.wtk.CalendarButtonSelectionListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.DialogCloseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.Window;

public class Pivot714 implements Application {

    private Frame frame;
    private Dialog result;
    private Window owner;
    private DialogCloseListener dcl;

    public Pivot714() {

    }

    public Window getWindow(final Window owner) {
        try {
            this.owner = owner;
            final BXMLSerializer bxmlSerializer = new BXMLSerializer();
            result = (Dialog)bxmlSerializer.readObject(Pivot714.class.getResource("pivot_714.bxml"));
            final ListButton motif = (ListButton)bxmlSerializer.getNamespace().get("motif");
            ArrayList<String> al = new ArrayList<String>();
            al.add("One");
            al.add("Two");
            motif.setListData(al);
            CalendarButton cbDate = (CalendarButton)bxmlSerializer.getNamespace().get("date");
            dcl = (new DialogCloseListener() {
                public void dialogClosed(Dialog dialog, boolean modal) {
                }
            });
            cbDate.getCalendarButtonSelectionListeners().add(new CalendarButtonSelectionListener() {
                @Override
                public void selectedDateChanged(CalendarButton calendarButton, CalendarDate previousSelectedDate) {
                }
            });

            return result;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        frame = new Frame();
        frame.setTitle("Pivot714");

        result = (Dialog) getWindow(frame.getRootOwner());

        frame.setPreferredSize(640, 480);
        frame.open(display);

        result.open(owner, dcl);
    }

   @Override
    public boolean shutdown(boolean optional) {
       if (result != null) {
           result.close();
       }

       if (frame != null) {
            frame.close();
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
        DesktopApplicationContext.main(Pivot714.class, args);
    }

}
