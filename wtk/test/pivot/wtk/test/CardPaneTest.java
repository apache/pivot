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
package pivot.wtk.test;

import pivot.collections.Dictionary;
import pivot.util.Vote;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.CardPane;
import pivot.wtk.CardPaneListener;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.FlowPane;
import pivot.wtk.Frame;
import pivot.wtk.Orientation;
import pivot.wtk.Sheet;
import pivot.wtkx.WTKXSerializer;

public class CardPaneTest implements Application {
    private Frame frame = null;
    private Sheet sheet = null;
    private CardPane cardPane = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
    	Frame frame = new Frame(new FlowPane());
    	frame.getStyles().put("padding", 0);
    	frame.setTitle("Card Pane Test");
    	frame.setPreferredSize(800, 600);
    	frame.setLocation(20, 20);

        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        sheet = new Sheet((Component)wtkxSerializer.readObject(getClass().getResource("card_pane_test.wtkx")));

        cardPane = (CardPane)wtkxSerializer.getObjectByName("cardPane");

        Button.Group sizeGroup = Button.getGroup("sizeGroup");
        sizeGroup.getGroupListeners().add(new Button.GroupListener() {
        	public void selectionChanged(Button.Group buttonGroup, Button previousSelection) {
        		final Button selection = buttonGroup.getSelection();
        		int selectedIndex = selection == null ? -1 : selection.getParent().indexOf(selection);

        		cardPane.getCardPaneListeners().add(new CardPaneListener() {
        		    public void orientationChanged(CardPane cardPane, Orientation previousOrientation) {

        		    }

        			public Vote previewSelectedIndexChange(CardPane cardPane, int selectedIndex) {
        				if (selection != null) {
        					selection.getParent().setEnabled(false);
        				}

        				return Vote.APPROVE;
        			}

        			public void selectedIndexChangeVetoed(CardPane cardPane, Vote reason) {
        				if (selection != null
    						&& reason == Vote.DENY) {
        					selection.getParent().setEnabled(true);
        				}
        			}

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
        sheet.open(frame);
    }

    public boolean shutdown(boolean optional) {
    	frame.close();
        return true;
    }

    public void resume() {
    }

    public void suspend() {
    }
}
