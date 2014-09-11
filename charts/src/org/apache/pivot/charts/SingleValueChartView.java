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
package org.apache.pivot.charts;

import java.util.EnumMap;

import org.apache.pivot.collections.List;

/**
 * Base class for charts that display just a single value but with bounds and
 * state limits (normal, warning, critical).
 */
public class SingleValueChartView extends ChartView {
    private double lowerBound = -1.0d;
    private double upperBound = -1.0d;

    public static enum Range {
        NORMAL, WARNING, CRITICAL
    }

    public static class ValueRange {
        private double lower;
        private double upper;

        public ValueRange(double lower, double upper) {
            this.lower = lower;
            this.upper = upper;
        }

        public double getLower() {
            return lower;
        }

        public double getUpper() {
            return upper;
        }
    }

    private EnumMap<Range, ValueRange> ranges = new EnumMap<>(Range.class);

    public Number getLowerBound() {
        return Double.valueOf(lowerBound);
    }

    public void setLowerBound(Number value) {
        this.lowerBound = value.doubleValue();
    }

    public Number getUpperBound() {
        return Double.valueOf(upperBound);
    }

    public void setUpperBound(Number value) {
        this.upperBound = value.doubleValue();
    }

    public ValueRange getValueBounds() {
        return new ValueRange(this.lowerBound, this.upperBound);
    }

    public void setValueBounds(ValueRange bounds) {
        this.lowerBound = bounds.getLower();
        this.upperBound = bounds.getUpper();
    }

    public ValueRange getValueRange(Range range) {
        return ranges.get(range);
    }

    public void setValueRange(Range range, ValueRange values) {
        ranges.put(range, values);
    }

    public void setValueRange(Range range, double lower, double upper) {
        ranges.put(range, new ValueRange(lower, upper));
    }

    public Number getValue() {
        if (chartData == null) {
            return null;
        } else if (chartData.getLength() != 1) {
            throw new IllegalStateException("Only one value can be displayed.");
        }
        Object value = chartData.get(0);
        if (value instanceof Number) {
            return (Number) value;
        } else if (value instanceof String) {
            return Double.valueOf((String) value);
        } else {
            return Double.valueOf(value.toString());
        }
    }

    @SuppressWarnings("unchecked")
    public void setValue(Number value) {
        if (chartData.getLength() == 1) {
            ((List<Object>) chartData).update(0, value);
        } else {
            ((List<Object>) chartData).insert(value, 0);
        }
    }

}
