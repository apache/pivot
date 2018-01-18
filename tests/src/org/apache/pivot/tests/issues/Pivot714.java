/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.tests.issues;

import java.io.IOException;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
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

    public Window getWindow(final Window ownerArgument) {
        this.owner = ownerArgument;
        final BXMLSerializer bxmlSerializer = new BXMLSerializer();
        try {
            result = (Dialog) bxmlSerializer.readObject(Pivot714.class.getResource("pivot_714.bxml"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SerializationException e) {
            e.printStackTrace();
        }

        final ListButton motif = (ListButton) bxmlSerializer.getNamespace().get("motif");

        ArrayList<String> al = new ArrayList<>();
        al.add("One");
        al.add("Two");
        motif.setListData(al);

        CalendarButton cbDate = (CalendarButton) bxmlSerializer.getNamespace().get("date");
        dcl = (new DialogCloseListener() {
            @Override
            public void dialogClosed(Dialog dialog, boolean modal) {
                // empty block
            }
        });
        cbDate.getCalendarButtonSelectionListeners().add(new CalendarButtonSelectionListener() {
            @Override
            public void selectedDateChanged(CalendarButton calendarButton,
                CalendarDate previousSelectedDate) {
                // empty block
            }
        });

        return result;
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

    public static void main(String[] args) {
        DesktopApplicationContext.main(Pivot714.class, args);
    }

}
