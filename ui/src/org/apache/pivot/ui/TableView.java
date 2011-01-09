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
package org.apache.pivot.ui;

import org.apache.pivot.util.ObservableList;
import org.apache.pivot.util.ObservableListAdapter;

// TODO Not abstract; just for prototyping
public abstract class TableView extends Component {
    public static class Column {
        // TODO Create properties, fire events directly from this class,
        // not via TableView
    }

    // TODO Observers will need to listen for changes on the list as well as
    // on individual items (columns)
    private ObservableList<Column> columns = ObservableListAdapter.observableArrayList();

    public ObservableList<Column> getColumns() {
        return columns;
    }

    public void setColumns(ObservableList<Column> columns) {
        if (columns == null) {
            throw new IllegalArgumentException();
        }

        this.columns = columns;

        // TODO Fire event
    }
}
