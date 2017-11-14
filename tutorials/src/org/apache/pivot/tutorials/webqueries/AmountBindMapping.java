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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.TextInput;

/**
 * Maps double values to strings and vice versa.
 */
public class AmountBindMapping implements TextInput.TextBindMapping {
    private NumberFormat amountFormat = new DecimalFormat();

    @Override
    public String toString(Object value) {
        return amountFormat.format(value);
    }

    @Override
    public Object valueOf(String text) {
        return Float.valueOf(text);
    }

    public NumberFormat getAmountFormat() {
        return amountFormat;
    }

    public void setAmountFormat(NumberFormat amountFormat) {
        Utils.checkNull(amountFormat, "amountFormat");

        this.amountFormat = amountFormat;
    }

    public void setAmountFormat(String amountFormat) {
        setAmountFormat(new DecimalFormat(amountFormat));
    }
}
