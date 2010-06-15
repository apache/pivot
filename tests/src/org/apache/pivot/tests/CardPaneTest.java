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
package org.apache.pivot.tests;

import org.apache.pivot.beans.BeanSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonGroupListener;
import org.apache.pivot.wtk.CardPane;
import org.apache.pivot.wtk.CardPaneListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.Sheet;

public class CardPaneTest implements Application {
    private Frame frame = null;
    private Sheet sheet = null;

    private CardPane cardPane = null;
    private ButtonGroup sizeGroup = null;

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        frame = new Frame(new BoxPane());
        frame.getStyles().put("padding", 0);
        frame.setTitle("Component Pane Test");
        frame.setPreferredSize(800, 600);
        frame.setLocation(20, 20);

        BeanSerializer wtkxSerializer = new BeanSerializer();
        sheet = (Sheet)wtkxSerializer.readObject(this, "card_pane_test.wtkx");
        cardPane = (CardPane)wtkxSerializer.get("cardPane");
        sizeGroup = (ButtonGroup)wtkxSerializer.get("sizeGroup");

        sizeGroup.getButtonGroupListeners().add(new ButtonGroupListener.Adapter() {
            @Override
            public void selectionChanged(ButtonGroup buttonGroup, Button previousSelection) {
                final Button selection = buttonGroup.getSelection();
                int selectedIndex = selection == null ? -1 : selection.getParent().indexOf(selection);

                cardPane.getCardPaneListeners().add(new CardPaneListener.Adapter() {
                    @Override
                    public Vote previewSelectedIndexChange(CardPane cardPane, int selectedIndex) {
                        if (selection != null) {
                            selection.getParent().setEnabled(false);
                        }

                        return Vote.APPROVE;
                    }

                    @Override
                    public void selectedIndexChangeVetoed(CardPane cardPane, Vote reason) {
                        if (selection != null
                            && reason == Vote.DENY) {
                            selection.getParent().setEnabled(true);
                        }
                    }

                    @Override
                    public void selectedIndexChanged(CardPane cardPane, int previousSelectedIndex) {
                        if (selection != null) {
                            selection.getParent().setEnabled(true);
                        }
                    }
                });

                cardPane.setSelectedIndex(selectedIndex);
            }
        });

        frame.open(display);

        ApplicationContext.queueCallback(new Runnable() {
            @Override
            public void run() {
                sheet.open(frame);
            }
        });
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (frame != null) {
            frame.close();
        }

        return false;
    }

    @Override
    public void resume() {
    }

    @Override
    public void suspend() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(CardPaneTest.class, args);
    }
}
