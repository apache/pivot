/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.CardPane;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.FlowPane;
import pivot.wtk.Frame;
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
    	frame.setMaximized(true);

        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        sheet = new Sheet((Component)wtkxSerializer.readObject(getClass().getResource("card_pane_test.wtkx")));
        // sheet.getDecorators().removeAll();

        cardPane = (CardPane)wtkxSerializer.getObjectByName("cardPane");

        Button.Group sizeGroup = Button.getGroup("sizeGroup");
        sizeGroup.getGroupListeners().add(new Button.GroupListener() {
        	public void selectionChanged(Button.Group buttonGroup, Button previousSelection) {
        		Button selection = buttonGroup.getSelection();
        		int selectedIndex = selection.getParent().indexOf(selection);
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
