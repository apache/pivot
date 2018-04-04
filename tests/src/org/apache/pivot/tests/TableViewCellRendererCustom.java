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

import org.apache.pivot.json.JSON;
import org.apache.pivot.wtk.content.TableViewCellRenderer;

/**
 * Minimal sample for a customized version of table cell renderer. Renders cell
 * contents as a string, but in this case, transformed. <br/> Note that here
 * it's possible to &quot;extends Label implements TableView.CellRenderer&quot;,
 * or even to extend directly TableViewCellRenderer (because it already extends
 * Label and implements TableView.CellRenderer).
 */
public final class TableViewCellRendererCustom extends TableViewCellRenderer {

    @Override
    public String toString(final Object row, final String columnName) {
        Object cellData = JSON.get(row, columnName);
        if (cellData == null) {
            return null;
        }

        String text = cellData.toString();
        // return new StringBuffer(text).reverse().toString(); // reverse text
        return text.toUpperCase(); // to upper text
    }

}
