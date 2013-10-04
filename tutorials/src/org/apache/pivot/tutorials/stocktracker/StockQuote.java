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

/**
 * JavaBean class representing a Stock Quote.
 */
public class StockQuote {
    private String symbol = null;
    private String companyName = null;
    private float value = 0;
    private float openingValue = 0;
    private float highValue = 0;
    private float lowValue = 0;
    private float change = 0;
    private float volume = 0;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public void setValue(String value) {
        try {
            setValue(Float.parseFloat(value));
        } catch (NumberFormatException exception) {
            setValue(Float.NaN);
        }
    }

    public float getOpeningValue() {
        return openingValue;
    }

    public void setOpeningValue(float openingValue) {
        this.openingValue = openingValue;
    }

    public void setOpeningValue(String openingValue) {
        try {
            setOpeningValue(Float.parseFloat(openingValue));
        } catch (NumberFormatException exception) {
            setOpeningValue(Float.NaN);
        }
    }

    public float getHighValue() {
        return highValue;
    }

    public void setHighValue(float highValue) {
        this.highValue = highValue;
    }

    public void setHighValue(String highValue) {
        try {
            setHighValue(Float.parseFloat(highValue));
        } catch (NumberFormatException exception) {
            setHighValue(Float.NaN);
        }
    }

    public float getLowValue() {
        return lowValue;
    }

    public void setLowValue(float lowValue) {
        this.lowValue = lowValue;
    }

    public void setLowValue(String lowValue) {
        try {
            setLowValue(Float.parseFloat(lowValue));
        } catch (NumberFormatException exception) {
            setLowValue(Float.NaN);
        }
    }

    public float getChange() {
        return change;
    }

    public void setChange(float change) {
        this.change = change;
    }

    public void setChange(String change) {
        try {
            setChange(Float.parseFloat(change));
        } catch (NumberFormatException exception) {
            setChange(Float.NaN);
        }
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setVolume(String volume) {
        try {
            setVolume(Float.parseFloat(volume));
        } catch (NumberFormatException exception) {
            setVolume(Float.NaN);
        }
    }
}
