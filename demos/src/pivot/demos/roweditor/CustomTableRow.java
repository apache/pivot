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
package pivot.demos.roweditor;

import pivot.beans.BeanDictionary;
import pivot.collections.Dictionary;
import pivot.util.CalendarDate;

public class CustomTableRow implements Dictionary<String, Object> {
    private CalendarDate calendarDate = null;
    private ExpenseType type = null;
    private double amount = 0;
    private String description = null;

    private BeanDictionary beanDictionary = new BeanDictionary(this);

    public CalendarDate getDate() {
        return calendarDate;
    }

    public void setDate(CalendarDate calendarDate) {
        this.calendarDate = calendarDate;
    }

    public final void setDate(String calendarDate) {
        setDate(new CalendarDate(calendarDate));
    }

    public ExpenseType getType() {
        return type;
    }

    public void setType(ExpenseType type) {
        this.type = type;
    }

    public final void setType(String type) {
        setType(ExpenseType.valueOf(type));
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public final void setAmount(String amount) {
        setAmount(Double.parseDouble(amount));
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object get(String key) {
        return beanDictionary.get(key);
    }

    public Object put(String key, Object value) {
        return beanDictionary.put(key, value);
    }

    public Object remove(String key) {
        return beanDictionary.remove(key);
    }

    public boolean containsKey(String key) {
        return beanDictionary.containsKey(key);
    }

    public boolean isEmpty() {
        return beanDictionary.isEmpty();
    }
}
