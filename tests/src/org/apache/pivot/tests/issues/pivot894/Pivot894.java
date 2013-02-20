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
package org.apache.pivot.tests.issues.pivot894;

import java.awt.EventQueue;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.CardPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.GridPane;
import org.apache.pivot.wtk.GridPane.Row;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.ButtonData;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.CardPaneSkin;

public class Pivot894 extends Application.Adapter {
    // global counter, just to know how many iterations the application is doing
    static int num = 0;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        System.out.println("public startup(...)");
        System.out.println("\n"
            + "Attention: now the application will go in an infinite loop, to be able to see the memory leak.\n"
            + "Note that probably you'll have to kill the application from outside (kill the Java process).\n"
            + "\n"
        );

        // add some sleep to let users see the warning messages in console ...
        Thread.sleep(2000);


        final CardPane cardPane = new CardPane();
        cardPane.getStyles().put("selectionChangeEffect", CardPaneSkin.SelectionChangeEffect.HORIZONTAL_SLIDE);

        final Window window = new Window(cardPane);
        window.open(display);

        DesktopApplicationContext.scheduleRecurringCallback(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("switcher-thread");

                System.out.println("Run num " + num++);  // temp


                /*
                //
                // method 1:
                //
                // Seems to be working just fine
                final GridPane grid = new GridPane(3);
                grid.getRows().add(createGridRow());
                grid.getRows().add(createGridRow());
                grid.getRows().add(createGridRow());
                 */


                //
                // method 2:
                //
                try {
                    // Before the fixes for PIVOT-861 (part two) it was causing out of memory ...
                    //
                    // Note that this has been moved to another issue, but the problem is due to the usage
                    // of dataRenderer tags (and then instancing ButtonDataRenderer) in the loaded bxml,
                    // so probably even this test will be updated ...
                    //
                    final GridPane grid = (GridPane) new BXMLSerializer().readObject(Pivot894.class, "btn_grid.bxml");

                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Iterator<Component> iterator = cardPane.iterator();
                            List<Component> deprecated = new ArrayList<Component>();
                            while (iterator.hasNext()) {
                                Component card = iterator.next();
                                if (!card.isShowing()) {
                                    deprecated.add(card);
                                }
                            }

                            for (Component card : deprecated) {
                                cardPane.remove(card);
                            }

                            cardPane.setSelectedIndex(cardPane.add(grid));

                            System.out.println(cardPane.getSelectedIndex());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @SuppressWarnings("unused")
            private Row createGridRow() {
                Row row = new Row();
                try {
                    // note that this method doesn't use ApplicationContext cache for images ...
                    row.add(new PushButton(new ButtonData(Image.load(new File("clock_icon.png").toURI().toURL()), "Clock")));
                    row.add(new PushButton(new ButtonData(Image.load(new File("clock_icon.png").toURI().toURL()), "Clock")));
                    row.add(new PushButton(new ButtonData(Image.load(new File("clock_icon.png").toURI().toURL()), "Clock")));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (TaskExecutionException e) {
                    e.printStackTrace();
                }
                return row;
            };
        }, 100);
    }

    @Override
    public boolean shutdown(boolean optional) throws Exception {
        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Pivot894.class, args);
    }

}
