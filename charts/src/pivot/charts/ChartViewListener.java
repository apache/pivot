/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.charts;

import pivot.collections.List;

public interface ChartViewListener {
    public void chartDataChanged(ChartView chartView, List<?> previousChartData);
    public void seriesNameKeyChanged(ChartView chartView, String previousSeriesNameKey);
    public void titleChanged(ChartView chartView, String previousTitle);
    public void horizontalAxisLabelChanged(ChartView chartView, String previousHorizontalAxisLabel);
    public void verticalAxisLabelChanged(ChartView chartView, String previousVerticalAxisLabel);
    public void showLegendChanged(ChartView chartView);
}
