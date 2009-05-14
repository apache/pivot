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
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.FlowPane;
import pivot.wtk.Frame;
import pivot.wtk.Sheet;
import pivot.wtkx.Bindable;

public class ComponentPaneTest extends Bindable implements Application {
    private Frame frame = null;

    @Load(resourceName="component_pane_test.wtkx") private Sheet sheet;
    @Bind(fieldName="sheet") private Sheet.ComponentPane componentPane;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        bind();

    	frame = new Frame(new FlowPane());
    	frame.getStyles().put("padding", 0);
    	frame.setTitle("Component Pane Test");
    	frame.setPreferredSize(800, 600);
    	frame.setLocation(20, 20);

        Button.Group sizeGroup = Button.getGroup("sizeGroup");
        sizeGroup.getGroupListeners().add(new Button.GroupListener() {
        	public void selectionChanged(Button.Group buttonGroup, Button previousSelection) {
        		final Button selection = buttonGroup.getSelection();
        		int selectedIndex = selection == null ? -1 : selection.getParent().indexOf(selection);

        		componentPane.getComponentPaneListeners().add(new Sheet.ComponentPaneListener.Adapter() {
        			public Vote previewSelectedIndexChange(Sheet.ComponentPane componentPane, int selectedIndex) {
        				if (selection != null) {
        					selection.getParent().setEnabled(false);
        				}

        				return Vote.APPROVE;
        			}

        			public void selectedIndexChangeVetoed(Sheet.ComponentPane componentPane, Vote reason) {
        				if (selection != null
    						&& reason == Vote.DENY) {
        					selection.getParent().setEnabled(true);
        				}
        			}

        			public void selectedIndexChanged(Sheet.ComponentPane componentPane, int previousSelectedIndex) {
        				if (selection != null) {
        					selection.getParent().setEnabled(true);
        				}
        			}
        		});

        		componentPane.setSelectedIndex(selectedIndex);
        	}
        });

        frame.open(display);
        sheet.open(frame);
    }

    public boolean shutdown(boolean optional) {
    	if (frame != null) {
    	    frame.close();
    	}

        return true;
    }

    public void resume() {
    }

    public void suspend() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(ComponentPaneTest.class, args);
    }
}
