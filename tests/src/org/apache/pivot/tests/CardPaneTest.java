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

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonGroupListener;
import org.apache.pivot.wtk.CardPane;
import org.apache.pivot.wtk.CardPaneListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.Style;

public class CardPaneTest implements Application {
    private Frame frame = null;
    private Sheet sheet = null;

    private CardPane cardPane = null;
    private ButtonGroup sizeGroup = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        frame = new Frame(new BoxPane());
        frame.getStyles().put(Style.padding, 0);
        frame.setTitle("Component Pane Test");
        frame.setPreferredSize(800, 600);
        frame.setLocation(20, 20);

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        sheet = (Sheet) bxmlSerializer.readObject(CardPaneTest.class, "card_pane_test.bxml");
        cardPane = (CardPane) bxmlSerializer.getNamespace().get("cardPane");
        sizeGroup = (ButtonGroup) bxmlSerializer.getNamespace().get("sizeGroup");

        sizeGroup.getButtonGroupListeners().add(new ButtonGroupListener() {
            @Override
            public void selectionChanged(ButtonGroup buttonGroup, Button previousSelection) {
                final Button selection = buttonGroup.getSelection();
                int selectedIndex = selection == null ? -1 : selection.getParent().indexOf(
                    selection);

                cardPane.getCardPaneListeners().add(new CardPaneListener() {
                    @Override
                    public Vote previewSelectedIndexChange(CardPane cardPaneArgument,
                        int selectedIndexArgument) {
                        if (selection != null) {
                            selection.getParent().setEnabled(false);
                        }

                        return Vote.APPROVE;
                    }

                    @Override
                    public void selectedIndexChangeVetoed(CardPane cardPaneArgument, Vote reason) {
                        if (selection != null && reason == Vote.DENY) {
                            selection.getParent().setEnabled(true);
                        }
                    }

                    @Override
                    public void selectedIndexChanged(CardPane cardPaneArgument,
                        int previousSelectedIndex) {
                        if (selection != null) {
                            selection.getParent().setEnabled(true);
                        }
                    }
                });

                cardPane.setSelectedIndex(selectedIndex);
            }
        });

        frame.open(display);

        ApplicationContext.queueCallback(() -> sheet.open(frame));
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (frame != null) {
            frame.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(CardPaneTest.class, args);
    }
}
