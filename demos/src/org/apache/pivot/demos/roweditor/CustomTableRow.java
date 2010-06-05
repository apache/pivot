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
package org.apache.pivot.demos.roweditor;

import org.apache.pivot.util.CalendarDate;

/**
 * Custom table row data.
 */
public class CustomTableRow {
    private CalendarDate calendarDate = null;
    private String type = null;
    private double amount = 0;
    private String description = null;

    public CalendarDate getDate() {
        return calendarDate;
    }

    public void setDate(CalendarDate calendarDate) {
        this.calendarDate = calendarDate;
    }

    public final void setDate(String calendarDate) {
        setDate(CalendarDate.decode(calendarDate));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public final void setAmount(String amount) {
        if (amount == null || amount.length() == 0) {
            setAmount(0);
        } else {
            setAmount(Double.parseDouble(amount));
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
