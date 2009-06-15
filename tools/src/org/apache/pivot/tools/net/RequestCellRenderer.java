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
package org.apache.pivot.tools.net;

import pivot.wtk.TableView;
import pivot.wtk.content.TableViewCellRenderer;

/**
 * Renders an <tt>Request</tt> in the form of <tt>[method] [resource]</tt>.
 *
 * @author tvolkert
 */
public class RequestCellRenderer extends TableViewCellRenderer {
    @Override
    public void render(Object value, TableView tableView, TableView.Column column,
        boolean rowSelected, boolean rowHighlighted, boolean rowDisabled) {
        renderStyles(tableView, rowSelected, rowDisabled);

        if (value != null) {
            Transaction transaction = (Transaction)value;
            Request httpRequest = transaction.getRequest();

            StringBuilder buf = new StringBuilder();
            buf.append(httpRequest.getMethod());
            buf.append(" ");
            buf.append(httpRequest.getLocation().getPath());

            setText(buf.toString());
            renderStyles(tableView, rowSelected, rowDisabled);
        }
    }
}
