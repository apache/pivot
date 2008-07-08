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

import pivot.wtk.Application;
import pivot.wtk.Display;
import pivot.wtk.PushButton;
import pivot.wtk.TablePane;
import pivot.wtk.Window;

/**
 * Demonstrates a possible issue with TablePane. A one-row, two-column table is
 * created. The row height is set to 1* so it fills the vertical space. Column
 * 0 is given a default width (-1), and the width of column 1 is set to 1* so
 * it occupies the remaining horizontal space after column 0 is accounted for.
 *
 * When the program is run, column 0 is not visible. If given an explicit width,
 * column 0 appears, and the other cell is sized appropriately.
 */
public class TablePaneTest implements Application {
    private Window window = null;

    public void startup() throws Exception {
        TablePane tablePane = new TablePane();
        tablePane.getRows().add(new TablePane.Row(1, true));
        tablePane.getColumns().add(new TablePane.Column(-1));
        tablePane.getColumns().add(new TablePane.Column(1, true));

        tablePane.setCellComponent(0, 0, new PushButton("Hello"));
        tablePane.setCellComponent(0, 1, new PushButton("World"));

        window = new Window();
        window.setTitle("TableView Test");
        window.setContent(tablePane);
        window.getAttributes().put(Display.MAXIMIZED_ATTRIBUTE, Boolean.TRUE);

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
