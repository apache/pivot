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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtkx.Bindable;
import org.apache.pivot.wtkx.WTKX;

public class DetailPane extends BoxPane implements Bindable {
    @WTKX private Label valueLabel = null;
    @WTKX private Label changeLabel = null;
    @WTKX private Label openingValueLabel = null;
    @WTKX private Label highValueLabel = null;
    @WTKX private Label lowValueLabel = null;
    @WTKX private Label volumeLabel = null;

    private Resources resources = null;

    private DecimalFormat valueFormat = new DecimalFormat("$0.00");
    private DecimalFormat changeFormat = new DecimalFormat("+0.00;-0.00");
    private DecimalFormat volumeFormat = new DecimalFormat();

    @Override
    public void initialize(Resources resources) {
        this.resources = resources;
    }

    @Override
    public void load(Dictionary<String, ?> context) {
        super.load(context);

        String notApplicable = resources.getString("notApplicable");

        float value = JSONSerializer.getFloat(context, "value");
        valueLabel.setText(Float.isNaN(value) ? notApplicable : valueFormat.format(value));

        float openingValue = JSONSerializer.getFloat(context, "openingValue");
        openingValueLabel.setText(Float.isNaN(openingValue) ? notApplicable : valueFormat.format(openingValue));

        float highValue = JSONSerializer.getFloat(context, "highValue");
        highValueLabel.setText(Float.isNaN(highValue) ? notApplicable : valueFormat.format(highValue));

        float lowValue = JSONSerializer.getFloat(context, "lowValue");
        lowValueLabel.setText(Float.isNaN(lowValue) ? notApplicable : valueFormat.format(lowValue));

        float change = JSONSerializer.getFloat(context, "change");
        changeLabel.setText(changeFormat.format(change));

        int volume = JSONSerializer.getInteger(context, "volume");
        volumeLabel.setText(volumeFormat.format(volume));
    }
}
