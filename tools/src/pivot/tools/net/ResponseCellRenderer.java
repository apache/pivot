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
package pivot.tools.net;

import pivot.wtk.TableView;
import pivot.wtk.content.TableViewCellRenderer;

/**
 * Renders an <tt>Request</tt> in the form of <tt>[status code] [status message]</tt>.
 *
 * @author tvolkert
 */
public class ResponseCellRenderer extends TableViewCellRenderer {
    @Override
    public void render(Object value, TableView tableView, TableView.Column column,
        boolean rowSelected, boolean rowHighlighted, boolean rowDisabled) {
        Transaction transaction = (Transaction)value;
        Response response = transaction.getResponse();

        StringBuilder buf = new StringBuilder();
        buf.append(response.getStatusCode());
        buf.append(" ");
        buf.append(response.getStatusMessage());

        setText(buf.toString());
        renderStyles(tableView, rowSelected, rowDisabled);
    }
}
