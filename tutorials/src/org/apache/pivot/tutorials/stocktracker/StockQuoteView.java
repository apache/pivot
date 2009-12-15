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
package org.apache.pivot.tutorials.stocktracker;

import java.text.DecimalFormat;

import org.apache.pivot.beans.BeanDictionary;

public class StockQuoteView extends BeanDictionary {
    private static final DecimalFormat valueFormat = new DecimalFormat("$0.00");
    private static final DecimalFormat changeFormat = new DecimalFormat("+0.00;-0.00");
    private static final DecimalFormat volumeFormat = new DecimalFormat();

    public StockQuoteView(StockQuote stockQuote) {
        super(stockQuote);
    }

    @Override
    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;
        StockQuote stockQuote = (StockQuote)getBean();

        if (stockQuote == null) {
            value = "";
        } else {
            value = super.get(key);

            if (key.equals("value")
                || key.equals("openingValue")
                || key.equals("highValue")
                || key.equals("lowValue")
                || key.equals("change")
                || key.equals("volume")) {
                try {
                    Float floatValue = (Float)value;
                    if (floatValue.isNaN()) {
                        value = "n/a";
                    } else {
                        value = valueFormat.format(floatValue);
                    }
                } catch(Exception exception) {
                    value = "";
                }
            } else if (key.equals("change")) {
                try {
                    value = changeFormat.format(value);
                } catch(Exception exception) {
                    value = "";
                }
            } else if (key.equals("volume")) {
                try {
                    value = volumeFormat.format(value);
                } catch(Exception exception) {
                    value = "";
                }
            } else {
                if (value != null) {
                    value = value.toString();
                }
            }
        }

        return value;
    }
}

