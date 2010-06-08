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
package org.apache.pivot.tutorials.webqueries;

import java.io.IOException;

import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.Bindable;
import org.apache.pivot.wtkx.WTKXSerializer;

public class ExpensesWindow extends Window implements Bindable {
    private ExpenseSheet expenseSheet = null;

    @Override
    public void initialize(Resources resource) {
        try {
            Resources resources = new Resources(ExpenseSheet.class.getName());
            WTKXSerializer wtkxSerializer = new WTKXSerializer(resources);
            expenseSheet = (ExpenseSheet)wtkxSerializer.readObject(this, "expense_sheet.wtkx");
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        // TODO Event handlers
    }
}
